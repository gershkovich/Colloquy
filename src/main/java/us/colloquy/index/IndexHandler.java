/*
 * Copyright (c) 2016. Tatyana Gershkovich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.colloquy.index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import us.colloquy.model.DiaryEntry;
import us.colloquy.model.DocumentPointer;
import us.colloquy.model.Letter;
import us.colloquy.model.Person;
import us.colloquy.util.DiaryParser;
import us.colloquy.util.LetterParser;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by Peter Gershkovich on 12/20/15.
 */
public class IndexHandler
{
    /**
     * Run this method to load typical Tolstoy letters
     */
    @Test
    public void loadLetterIndex()
    {
        Properties properties = loadProperties();

        List<Letter> letterList = new ArrayList<>();

        //get аll letters
        Set<DocumentPointer> documentPointers = new TreeSet<>();

        String homeDir = System.getProperty("user.home");

//        getURIForAllLetters(documentPointers, homeDir + "/Documents/Tolstoy/unzipLettersSpecial/pisma_k_Sofie_83-84(1)", true);
//        Person person = new Person();
//        person.setLastName("Толстая");
//        person.setFirstName("Софья");
//        person.setPaternalName("Андреевна");
//        person.setOriginalEntry("Толстой Софье Андрееевне");


//        getURIForAllLetters(documentPointers, System.getProperty("user.home") + "/Documents/Tolstoy/unzipLettersSpecial/Pisma_k_Chertkovu_toma_87-89", true);
//        getURIForAllLetters(documentPointers, System.getProperty("user.home") + "/Documents/Tolstoy/unzipLettersSpecial/Pisma_k_Chertkovu_toma_87-89", true);
//        Person person = new Person();
//        person.setLastName("Чертков");
//        person.setFirstName("Владимир");
//        person.setPaternalName("Григорьевич");
//        person.setOriginalEntry("Черткову В.Г.");


        getURIForAllLetters(documentPointers, System.getProperty("user.home") + "/Documents/Tolstoy/unzipLetters", false);
        Person person = null;


        for (DocumentPointer pointer : documentPointers)
        {
            LetterParser.parseLetters(pointer, letterList, person);   //test case "temp/OEBPS/Text/0001_1006_2002.xhtml"

        }

        System.out.println("Total number of letters: " + letterList.size());

        //code below to check a few letters
        int i = 0;
        for (Letter letter : letterList)
        {
            i++;


            if (letter.getDate() == null)
            {
                System.out.println(" ------------------------------------------------------------------");
                System.out.println(letter.toString());

            }

//            if (i > 15)
//            {
//                break;
//            }
        }

        // write your code here
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", properties.getProperty("elastic_cluster_name")).build();

        //open index balk load all letters and process them
        try (Client client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(properties.getProperty("elastic_ip_address")), 9300)))
        {
            BulkRequestBuilder bulkRequest = client.prepareBulk();

            //this is strait forward indexing - for test and validation just comment it out
            indexLetters(letterList, client, bulkRequest);


        } catch (Throwable t)
        {
            t.printStackTrace();
        }
    }


    @Test
    public void loadDiariesIndex()
    {
        Properties properties = loadProperties();

        List<DiaryEntry> diaryEntries = new ArrayList<>();

        //get аll letters
        Set<DocumentPointer> documentPointers = new TreeSet<>();

      //  getURIForAllDiaries(documentPointers, System.getProperty("user.home") + "/Documents/Tolstoy/openDiaries");


        getURIForAllDiaries(documentPointers, System.getProperty("user.home") + "/Documents/Tolstoy/90-volume-set/diaries/uzip");

        for (DocumentPointer pointer : documentPointers)
        {
            DiaryParser.parseDiaries(pointer, diaryEntries);   //test case "temp/OEBPS/Text/0001_1006_2002.xhtml"

        }

        System.out.println("Total number of letters: " + diaryEntries.size());

        //code below to check a few letters
        int i = 0;
        for (DiaryEntry diaryEntry : diaryEntries)
        {
            i++;


            if (diaryEntry.getDate() == null)
            {
                System.out.println(" ------------------------------------------------------------------");
                System.out.println(diaryEntry.toString());

            }

//            if (i > 15)
//            {
//                break;
//            }
        }

        // write your code here
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", properties.getProperty("elastic_cluster_name")).build();

        //open index balk load all letters and process them
        try (Client client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(properties.getProperty("elastic_ip_address")), 9300)))
        {
            BulkRequestBuilder bulkRequest = client.prepareBulk();

            //this is strait forward indexing - for test and validation just comment it out
            indexDocuments(diaryEntries, client, bulkRequest);


        } catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    private void indexDocuments(List<DiaryEntry> diaryEntries, Client client, BulkRequestBuilder bulkRequest) throws JsonProcessingException
    {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();


        for (DiaryEntry diaryEntry : diaryEntries)
        {
            String json = ow.writeValueAsString(diaryEntry);

            bulkRequest.add(client.prepareIndex("tolstoy", "diaries")
                    .setSource(json)
            );

        }

        BulkResponse bulkResponse = bulkRequest.get();

        if (bulkResponse.hasFailures())
        {
            // process failures by iterating through each bulk response item
            for (BulkItemResponse br : bulkResponse.getItems())
            {
                System.out.println(br.getFailureMessage());
            }

        }
    }

    private void indexLetters(List<Letter> letterList, Client client, BulkRequestBuilder bulkRequest) throws JsonProcessingException
    {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();


        for (Letter letter : letterList)
        {
            String json = ow.writeValueAsString(letter);

            String name = "unk";

            if (letter.getTo().size() > 0)
            {
                name = letter.getTo().get(0).getLastName();

            }

            String id = letter.getId() + "-" + letter.getDate() + "-" + letter.getSource();

            bulkRequest.add(client.prepareIndex("tolstoy", "letters", id)
                    .setSource(json)
            );

        }

        BulkResponse bulkResponse = bulkRequest.get();

        if (bulkResponse.hasFailures())
        {
            // process failures by iterating through each bulk response item
            for (BulkItemResponse br : bulkResponse.getItems())
            {
                System.out.println(br.getFailureMessage());
            }

        }
    }

    @Test
    public void mapIndex()
    {

        String indexName = "tolstoy";

        String type = "letters";

        String mapping = "{\n" +
                "      \"letters\": {\n" +
                "        \"properties\": {\n" +
                "          \"content\": {\n" +
                "            \"type\": \"string\"\n" +
                "          },\n" +
                "          \"date\": {\n" +
                "            \"type\": \"date\"\n" +
//                "            \"format\": \"yyyy-MM-dd\"\n" +
                "          },\n" +
                "          \"id\": {\n" +
                "            \"type\": \"string\"\n" +
                "          },\n" +
                "          \"notes\": {\n" +
                "            \"type\": \"string\"\n" +
                "          },\n" +
                "          \"place\": {\n" +
                "            \"type\": \"string\"\n" +
                "          },\n" +
                "          \"to\": {\n" +
                "             \"properties\": {\n" +
                "              \"firstName\": {\n" +
                "                \"type\": \"string\"\n" +
                "              },\n" +
                "              \"lastName\": {\n" +
                "                \"type\": \"string\"\n" +
                "              },\n" +
                "              \"originalEntry\": {\n" +
                "                \"type\": \"string\"\n" +
                "              },\n" +
                "              \"paternalName\": {\n" +
                "                \"type\": \"string\"\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          \"source\": {\n" +
                "            \"type\": \"string\"\n" +
                "          }\n" +
                "        }\n" +
                "        }\n" +
                "      }";


        /*
        I added diaries type index via sense
        PUT /tolstoy/diaries/_mapping
{
   "diaries": {
      "properties": {
         "id": {
            "type": "string"
         },"date": {
            "type": "date"
         },"place": {
            "type": "string"
         },"entry": {
            "type": "string"
         },
         "source": {
            "type": "string"
         }
      }
   }
}

         */


        Properties properties = loadProperties();

        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", properties.getProperty("elastic_cluster_name")).build();


        try (Client client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(properties.getProperty("elastic_ip_address")), 9300)))
        {
            // re-create index if it is  already exists.
            if (client.admin().indices().prepareExists(indexName).execute().actionGet().isExists())
            {

                final DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);

                final DeleteIndexResponse deleteIndexResponse = client.admin().indices().delete(deleteIndexRequest).actionGet();

                if (!deleteIndexResponse.isAcknowledged())
                {
                    System.out.println("Not deleted");
                } else
                {
                    System.out.println(indexName + " is deleted ");
                }
            }

            client.admin().indices().prepareCreate(indexName).execute().actionGet();

            PutMappingResponse response = client.admin().indices().preparePutMapping(indexName).setType(type).setSource(mapping).execute().actionGet();

            if (!response.isAcknowledged())
            {
                System.out.println("Something strange happens");
            }

        } catch (Throwable t)
        {
            t.printStackTrace();
        }
    }


    public void getURIForAllLetters(Set<DocumentPointer> uriList, String letterDirectory, boolean useOnlyNumber)
    {
        ///Documents/Tolstoy/diaries

        Path pathToLetters = FileSystems.getDefault().getPath(letterDirectory);

        List<Path> results = new ArrayList<>();

        int maxDepth = 6;

        try (Stream<Path> stream = Files.find(pathToLetters, maxDepth, (path, attr) -> {
            return String.valueOf(path).endsWith(".ncx");
        }))
        {


            stream.forEach(results::add);

//            String joined = stream
//                    .sorted()
//                    .map(String::valueOf)
//                    .collect(Collectors.joining("; "));
//
//            System.out.println("\nFound: " + joined);

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("files: " + results.size());


        try
        {

            for (Path res : results)
            {
                Path parent = res.getParent();

//                System.out.println("---------------------------------------------");
//                System.out.println(parent.toString());
                //use jsoup to list all files that contain something useful
                Document doc = Jsoup.parse(res.toFile(), "UTF-8");

                String title = "";

                for (Element element : doc.getElementsByTag("docTitle"))
                {
                    //Letter letter = new Letter();

                    // StringBuilder content = new StringBuilder();

                    for (Element child : element.children())
                    {
                        title = child.text();
                        // System.out.println("Title: " + title);
                    }
                }


                for (Element element : doc.getElementsByTag("navPoint"))
                {
                    //Letter letter = new Letter();

                    // StringBuilder content = new StringBuilder();

                    for (Element child : element.children())
                    {
                        String label = child.text();

                        if (StringUtils.isNotEmpty(label))
                        {
                            if (label.matches("ПИСЬМА"))
                            {
                                System.out.println("------------------");
                            }


                            String url = child.getElementsByTag("content").attr("src");

                            if (label.matches(".*\\d{1,3}.*[А-Яа-я]+.*") &&
                                    StringUtils.isNotEmpty(url))
                            {
                                DocumentPointer documentPointer = new DocumentPointer(parent.toString()
                                        + File.separator + url.replaceAll("#.*", ""), title);

                                uriList.add(documentPointer);
//                                System.out.println("nav point: " + label + " src " + parent.toString()
//                                        + System.lineSeparator() + url.replaceAll("#.*",""));


                            } else if (label.matches(".*\\d{1,3}.*") &&
                                    StringUtils.isNotEmpty(url) && useOnlyNumber)
                            {
                                DocumentPointer documentPointer = new DocumentPointer(parent.toString()
                                        + File.separator + url.replaceAll("#.*", ""), title);

                                uriList.add(documentPointer);
//                                System.out.println("nav point: " + label + " src " + parent.toString()
//                                        + System.lineSeparator() + url.replaceAll("#.*",""));


                            } else
                            {
                                // System.out.println("nav point: " + label + " src " + child.getElementsByTag("content").attr("src"));
                            }


                        }
                    }
                }

            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

//        System.out.println("Size: " + uriList.size());

//        for (DocumentPointer pointer : uriList)
//        {
//            //parse and
//            System.out.println(pointer.getSourse() + "\t" + pointer.getUri());
//        }
    }


    public Properties loadProperties()
    {
        Properties properties = new Properties();

        try (InputStream propertiesFileInputStream = IndexHandler.class.getClassLoader().getResourceAsStream("properties.xml"))
        {

            if (propertiesFileInputStream != null)
            {
                properties.loadFromXML(propertiesFileInputStream);

//                properties.list(System.out);

                propertiesFileInputStream.close();


            } else
            {

                System.out.print("Property file not available");

            }

        } catch (Exception e)
        {
            e.printStackTrace();

        }
        return properties;
    }





    public void getURIForAllDiaries(Set<DocumentPointer> documentPointers, String letterDirectory)
    {


        Path pathToLetters = FileSystems.getDefault().getPath(letterDirectory);

        List<Path> results = new ArrayList<>();

        int maxDepth = 6;

        try (Stream<Path> stream = Files.find(pathToLetters, maxDepth, (path, attr) -> {
            return String.valueOf(path).endsWith(".ncx");
        }))
        {


            stream.forEach(results::add);


        } catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("files: " + results.size());


        try
        {

            for (Path res : results)
            {
                Path parent = res.getParent();

//                System.out.println("---------------------------------------------");
//                System.out.println(parent.toString());
                //use jsoup to list all files that contain something useful
                Document doc = Jsoup.parse(res.toFile(), "UTF-8");

                String title = "";

                for (Element element : doc.getElementsByTag("docTitle"))
                {
                    //Letter letter = new Letter();

                    // StringBuilder content = new StringBuilder();

                    for (Element child : element.children())
                    {
                        title = child.text();
                        // System.out.println("Title: " + title);
                    }
                }

                //  System.out.println("==========================   " + res.toString() + " ==========================");


                boolean startPrinting = false;

                boolean newFile = true;

                for (Element element : doc.getElementsByTag("navPoint"))
                {

                    //get nav label and content

                    Element navLabelElement = element.select("navLabel").first();
                    Element srsElement = element.select("content").first();

                    String navLabel = "";
                    String srs = "";


                    if (navLabelElement != null)
                    {
                        navLabel = navLabelElement.text().replaceAll("\\*","").trim();
                    }

                    if (srsElement != null)
                    {
                        srs = srsElement.attr("src");
                    }

                    if ("КОММЕНТАРИИ".matches(navLabel))

                    {
                        startPrinting = false;

                        // System.out.println("----------------- end of file pointer ---------------");
                    }


                    if (StringUtils.isNotEmpty(navLabel) && navLabel.matches("ДНЕВНИК.*|ЗАПИСНЫЕ КНИЖ.*") && newFile)
                    {
                        newFile = false;
                        startPrinting = true;
                    }

                    if (startPrinting && !navLabel.matches("(ПРЕДИСЛОВИЕ|РЕДАКЦИОННЫЕ ПОЯСНЕНИЯ)"))
                    {
                        // System.out.println("----------------- file pointer ---------------");
                        //   System.out.println(navLabel + "\t" + srs);

                        DocumentPointer documentPointer = new DocumentPointer(parent.toString()
                                + File.separator + srs.replaceAll("#.*", ""), title);

                        documentPointers.add(documentPointer);
                    }

                }

                //   System.out.println("==========================   END OF FILE ==========================");

            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println("Size: " + documentPointers.size());

      //  for (DocumentPointer pointer : documentPointers)
       // {
            //parse and
       //     System.out.println(pointer.getSourse() + "\t" + pointer.getUri());
        }


}
