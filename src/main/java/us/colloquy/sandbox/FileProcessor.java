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

package us.colloquy.sandbox;


import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import us.colloquy.model.DocumentPointer;
import us.colloquy.util.Unzip;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by Peter Gershkovich on 12/3/15.
 */
public class FileProcessor
{
    @Test
    public void listAllFiles()
    {
        ///Documents/Tolstoy/diaries


        try
        {
            Unzip.unZipFilesFromTo(System.getProperty("user.home") + "/Documents/Tolstoy/90-volume-set/diaries", System.getProperty("user.home") + "/Documents/Tolstoy/90-volume-set/diaries/uzip", "epub");
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    @Test
    public void listAllUzipedFiles()
    {
        ///Documents/Tolstoy/diaries
        //System.getProperty("user.home") + "/Documents/Tolstoy/unzipLetters"

        Path pathToLetters = FileSystems.getDefault().getPath(System.getProperty("user.home") + "/Documents/Tolstoy/openDiaries");

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

        Set<String> uriList = new TreeSet<>();

        try
        {

            for (Path res : results)
            {
                Path parent = res.getParent();

                System.out.println("---------------------------------------------");
                System.out.println(parent.toString());
                //use jsoup to list all files that contain something useful
                Document doc = Jsoup.parse(res.toFile(), "UTF-8");

                for (Element element : doc.getElementsByTag("docTitle"))
                {
                    //Letter letter = new Letter();

                    // StringBuilder content = new StringBuilder();

                    for (Element child : element.children())
                    {

                        System.out.println("Title: " + child.text());
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

                                uriList.add(parent.toString()
                                        + File.separator + url.replaceAll("#.*", ""));
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

        System.out.println("Size: " + uriList.size());

        for (String uri : uriList)
        {
            //parse and
            System.out.println(uri);
        }

    }


    @Test
    public void listAllUzipedFilesContent()
    {
        ///Documents/Tolstoy/diaries

        Path pathToLetters = FileSystems.getDefault().getPath(System.getProperty("user.home") + "/Documents/Tolstoy/unzipLetters");

        List<Path> results = new ArrayList<>();

        int maxDepth = 6;

        try (Stream<Path> stream = Files.find(pathToLetters, maxDepth, (path, attr) -> {
            return String.valueOf(path).endsWith(".opf");
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

        Set<String> uriList = new TreeSet<>();

        try
        {

            for (Path res : results)
            {
                Path parent = res.getParent();

                System.out.println("---------------------------------------------");
                System.out.println(parent.toString());
                //use jsoup to list all files that contain something useful
                Document doc = Jsoup.parse(res.toFile(), "UTF-8");

                for (Element element : doc.getElementsByTag("dc:title"))
                {
                    //Letter letter = new Letter();

                    // StringBuilder content = new StringBuilder();
                    System.out.println(element.text());

//                    for (Element child : element.children())
//                    {
//                       System.out.println(child.tagName() + "\t" + child.text());
//                    }
                }

//                for (Element element : doc.getElementsByTag("navPoint"))
//                {
//                    //Letter letter = new Letter();
//
//                    // StringBuilder content = new StringBuilder();
//
//                    for (Element child : element.children())
//                    {
//                        String label = child.text();
//
//                        if (StringUtils.isNotEmpty(label))
//                        {
//                            if (label.matches("ПИСЬМА"))
//                            {
//                                System.out.println("------------------");
//                            }
//
//
//                            String url = child.getElementsByTag("content").attr("src");
//
//                            if (label.matches(".*\\d{1,3}.*[А-Яа-я]+.*") &&
//                                    StringUtils.isNotEmpty(url) )
//                            {
//
//                                uriList.add(parent.toString()
//                                        + File.separator + url.replaceAll("#.*",""));
////                                System.out.println("nav point: " + label + " src " + parent.toString()
////                                        + System.lineSeparator() + url.replaceAll("#.*",""));
//
//
//                            } else
//                            {
//                                // System.out.println("nav point: " + label + " src " + child.getElementsByTag("content").attr("src"));
//                            }
//
//
//                        }
//                    }
//                }

            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println("Size: " + uriList.size());

        for (String uri : uriList)
        {
            //parse and
            System.out.println(uri);
        }

    }


    @Test
    public void getURIForAllDiaries()
    {

        Set<DocumentPointer> uriList = new HashSet<>();
        //String letterDirectory = System.getProperty("user.home") + "/Documents/Tolstoy/openDiaries";

        //

        String letterDirectory = System.getProperty("user.home") + "/Documents/Tolstoy/90-volume-set/diaries/uzip/dnevnik_1881-1887_vol_49";


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
                        title = navLabel;
                    }

                    if (startPrinting)
                    {
                        // System.out.println("----------------- file pointer ---------------");
                        //   System.out.println(navLabel + "\t" + srs);

                        DocumentPointer documentPointer = new DocumentPointer(parent.toString()
                                + File.separator + srs.replaceAll("#.*", ""), title);

                        uriList.add(documentPointer);
                    }


//                    for (Element child : element.children())
//                    {
//                        String label = child.text();
//
//                        if (StringUtils.isNotEmpty(label))
//                        {
//                            if (label.matches("ДНЕВНИК\\s\\d{4}.*"))
//                            {
//                                System.out.println("------------------");
//                            }

//
//                            String url = child.getElementsByTag("content").attr("src");
//
//                            if (label.matches(".*\\d{1,3}.*[А-Яа-я]+.*") &&
//                                    StringUtils.isNotEmpty(url))
//                            {
//                                DocumentPointer letterPointer = new DocumentPointer(parent.toString()
//                                        + File.separator + url.replaceAll("#.*", ""), title);
//
//                                uriList.add(letterPointer);
////                                System.out.println("nav point: " + label + " src " + parent.toString()
////                                        + System.lineSeparator() + url.replaceAll("#.*",""));
//
//
//                            } else if (label.matches(".*\\d{1,3}.*") &&
//                                    StringUtils.isNotEmpty(url) && useOnlyNumber)
//                            {
//                                DocumentPointer letterPointer = new DocumentPointer(parent.toString()
//                                        + File.separator + url.replaceAll("#.*", ""), title);
//
//                                uriList.add(letterPointer);
////                                System.out.println("nav point: " + label + " src " + parent.toString()
////                                        + System.lineSeparator() + url.replaceAll("#.*",""));
//
//
//                            } else
//                            {
//                                // System.out.println("nav point: " + label + " src " + child.getElementsByTag("content").attr("src"));
//                            }
//
//
//                        }
//                        }
                }

                //   System.out.println("==========================   END OF FILE ==========================");

            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println("Size: " + uriList.size());

        for (DocumentPointer pointer : uriList)
        {
            //parse and
            System.out.println(pointer.getSourse() + "\t" + pointer.getUri());
        }
    }
}
