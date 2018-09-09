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

import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import us.colloquy.model.DiaryEntry;
import us.colloquy.model.DocumentPointer;
import us.colloquy.model.Letter;
import us.colloquy.model.Person;
import us.colloquy.util.*;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static us.colloquy.util.ElasticLoader.uploadLettersToElasticServer;

/**
 * Created by Peter Gershkovich on 12/20/15.
 */
public class IndexHandler
{
    /**
     * Run this method to load typical Tolstoy letters
     * Should be run after creating tolstoy index see command below
     */

    @Test
    public void loadLetterIndex()
    {
        Properties properties = loadProperties();

        List<Letter> letterList = new ArrayList<>();

        List<Letter> rejectedLetters = new ArrayList<>();   //a list for rejected letter to review and improve the algorithm

        //get аll letters
        Set<DocumentPointer> documentPointers = new TreeSet<>();

        String homeDir = System.getProperty("user.home");

//        getURIForAllLetters(documentPointers, homeDir + "/Documents/Tolstoy/unzipLettersSpecial/pisma_k_Sofie_83-84(1)", true);
//        Person person = new Person();
//        person.setLastName("Толстая");
//        person.setFirstName("Софья");
//        person.setPaternalName("Андреевна");
//        person.setOriginalEntry("Толстой Софье Андрееевне");


        getURIForAllLetters(documentPointers, System.getProperty("user.home") + "/Documents/Tolstoy/unzipLettersSpecial/Pisma_k_Chertkovu_toma_87-89", true);
        getURIForAllLetters(documentPointers, System.getProperty("user.home") + "/Documents/Tolstoy/unzipLettersSpecial/Pisma_k_Chertkovu_toma_87-89", true);
        Person person = new Person();
        person.setLastName("Чертков");
        person.setFirstName("Владимир");
        person.setPaternalName("Григорьевич");
        person.setOriginalEntry("Черткову В.Г.");

//        getURIForAllLetters(documentPointers, System.getProperty("user.home") + "/Documents/Tolstoy/unzipLetters", false);
//
//        Person person = null;

        Map<String, String> toWhomMap = null;

        //load map of addressees
        try
        {

            toWhomMap = ResourceLoader.loadToWhomMap("references/toWhom.json");

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        for (DocumentPointer pointer : documentPointers)
        {
            if (toWhomMap != null)
            {
                LetterParser.parseLetters(pointer, letterList, person, toWhomMap, rejectedLetters);   //test case "temp/OEBPS/Text/0001_1006_2002.xhtml"
            } else
            {
                System.out.println("ToWhom Map must be initialized.");
            }

        }

        System.out.println("Total number of letters: " + letterList.size());

        Set<String> locations = new TreeSet<>();

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

            if (StringUtils.isNotEmpty(letter.getPlace()))
            {

                locations.add(letter.getPlace());

            } else
            {
                System.out.println(" ------------------------------------------------------------------");
                System.out.println(letter.toString());

            }

//            if (i > 15)
//            {
//                break;
//            }
        }

        for (String loc : locations)
        {
            System.out.println(loc);
        }

        // write your code here
//        Settings settings = Settings.settingsBuilder()
//                .put("cluster.name", properties.getProperty("elastic_cluster_name")).build();
//
//        //open index balk load all letters and process them
//        try (Client client = TransportClient.builder().settings(settings).build()
//                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(properties.getProperty("elastic_ip_address")), 9300)))
//        {

        if (properties.getProperty("upload_to_elastic").equalsIgnoreCase("true"))
        {
            uploadLettersToElasticServer(properties, letterList);
        }
    }


    @Test
    public void createLettersIndex() throws Exception
    {
        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http"))))
        {

            IndexingUtils.makeIndex("lntolstoy-letters", "letters", client);
        }
    }


    @Test
    public void createDiariesIndex() throws Exception
    {
        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http"))))
        {

            IndexingUtils.makeIndex("lntolstoy-diaries", "diaries", client);
        }
    }




    @Test
    public void loadDiariesIndex()
    {
        Properties properties = loadProperties();

        List<DiaryEntry> diaryEntries = new ArrayList<>();

        List<DocumentPointer> documentPointers = new ArrayList<>();

        //  getURIForAllDiaries(documentPointers, System.getProperty("user.home") + "/Documents/Tolstoy/openDiaries");
        //
        String strPathToDiaries = "samples/diaries";

        //find all volumes with diaries
        Path pathToDiaries = FileSystems.getDefault().getPath(strPathToDiaries);

        List<Path> listOfDiaryVolumes = new ArrayList<>();

        int maxDepth = 1;

        try (Stream<Path> stream = Files.find(pathToDiaries, maxDepth, (path, attr) ->
        {
            return String.valueOf(path).contains("dnevnik");
        }))
        {
            stream.forEach(listOfDiaryVolumes::add);


        } catch (IOException e)
        {
            e.printStackTrace();
        }

        int diaryCounter = 0;

        File  entries_debug = new File("entries_debug.txt");

        entries_debug.delete();

        try (FileWriter fw = new FileWriter("entries_debug.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter outDebug = new PrintWriter(bw))
        {
            outDebug.println("start file");
            //more code

            for (Path path : listOfDiaryVolumes)
            {
                documentPointers.clear();

                getURIForAllDiaries(documentPointers, path);

                diaryEntries.clear();

                for (DocumentPointer pointer : documentPointers)
                {
                    List<DiaryEntry> diaryEntriesInPointer = new ArrayList<>();

                    DiaryParser.parseDiaries(pointer, diaryCounter++, diaryEntriesInPointer, outDebug);   //test case "temp/OEBPS/Text/0001_1006_2002.xhtml"

                    //find if anything is wrong in a pointer

                    for (DiaryEntry diaryEntry : diaryEntriesInPointer)
                    {
                        if (diaryEntry.getDate() == null || diaryEntry.getEntry().length() < 20)
                        {
                            System.out.println(" Missing entry:  --------------- " + pointer.getUri() + " ---------------------------------");
                            System.out.println(diaryEntry.toString());

                        }
                    }

                    diaryEntries.addAll(diaryEntriesInPointer);
                }

                System.out.println("Total number of diaries in " + path.getFileName() + ": " + diaryEntries.size());

                //code below to set ids and check a few letters
                int i = 0;

                for (DiaryEntry diaryEntry : diaryEntries)
                {
                    i++;

                    String id = "D-" + path.getFileName().toString().replaceAll(".*_", "") + "-" + i;

                    diaryEntry.setId(id);
                }

                System.out.println("Total number of diaries in volume: " + diaryEntries.size());

                //export json files

                try
                {
                    ObjectWriter ow = new com.fasterxml.jackson.databind.ObjectMapper().writer().withDefaultPrettyPrinter();


                    String json = ow.writeValueAsString(diaryEntries);

                    System.out.println("-------------------- Start exporting diaries in Volume " + path.getFileName() + " ------------- ");

                    //   String origin = diaryEntries.get(0).getSource();

                    String fileName = "parsed/diaries/" + path.getFileName() + ".json";

                    try (Writer out = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(fileName), "UTF-8")))
                    {
                        out.write(json);

                    }

                    System.out.println("-------------------- End of export in Volume " + path.getFileName() + " ------------- ");


                } catch (Exception e)
                {
                    e.printStackTrace();
                }


                if (properties.getProperty("upload_to_elastic").equalsIgnoreCase("true"))
                {

                    ElasticLoader.uploadDiariesToElasticServer(properties, diaryEntries);
                }
            }

            outDebug.println("end file");
            //more code
        } catch (IOException e)
        {
            //exception handling left as an exercise for the reader
        }

    }

    public void getURIForAllLetters(Set<DocumentPointer> uriList, String letterDirectory, boolean useOnlyNumber)
    {
        ///Documents/Tolstoy/diaries

        Path pathToLetters = FileSystems.getDefault().getPath(letterDirectory);

        List<Path> results = new ArrayList<>();

        int maxDepth = 6;

        try (Stream<Path> stream = Files.find(pathToLetters, maxDepth, (path, attr) ->
        {
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


    public void getURIForAllDiaries(List<DocumentPointer> documentPointers, Path pathToLetters)
    {
        List<Path> results = new ArrayList<>();

        int maxDepth = 6;

        try (Stream<Path> stream = Files.find(pathToLetters, maxDepth, (path, attr) ->
        {
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
                        navLabel = navLabelElement.text().replaceAll("\\*", "").trim();
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
