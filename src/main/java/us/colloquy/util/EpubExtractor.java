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

package us.colloquy.util;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import us.colloquy.model.DocumentPointer;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by Peter Gershkovich on 9/10/16.
 */
public class EpubExtractor
{

    public  static void getURIForAllLetters(Set<DocumentPointer> uriList, String letterDirectory, boolean useOnlyNumber)
    {

        Path pathToLetters = FileSystems.getDefault().getPath(letterDirectory);

        List<Path> results = new ArrayList<>();

        int maxDepth = 6;

        try (Stream<Path> stream = Files.find(pathToLetters, maxDepth, (path, attr) -> String.valueOf(path).endsWith(".ncx")))
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

                for (Element element : doc.getElementsByTag("avantitul"))
                {


                    for (Element child : element.children())
                    {
                        String label = child.text();

                        if (StringUtils.isNotEmpty(label))
                        {
                            if (label.matches("Подготовлено на основе электронной копии.*"))
                            {
                                System.out.println("------------------   " + label);
                            }
                        }
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
                                System.out.println("------------------ " + "Письма" + " -------------------");

                            } else if (label.contains("ДЕЛОВЫЕ БУМАГИ"))
                            {
                                break;
                            }


                            String url = child.getElementsByTag("content").attr("src");

                            if (label.matches(".*\\d{1,3}.*[А-Яа-яA-Za-z]+.*") &&
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

}
