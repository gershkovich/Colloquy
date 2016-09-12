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

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;
import us.colloquy.model.DocumentPointer;
import us.colloquy.model.Letter;
import us.colloquy.model.Person;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter Gershkovich on 12/20/15.
 */
public class LetterParser
{
    /**
     * This is the hart of parsing process
     *
     * @param documentPointer
     * @param letters
     */
    public static void parseLetters(DocumentPointer documentPointer, List<Letter> letters, Person person)
    {
        File input = new File(documentPointer.getUri());

        List<Letter> rejectedLetters =  new ArrayList<>();   //a list for rejected letter to review and improve the algorithm


        try
        {
            Document doc = Jsoup.parse(input, "UTF-8"); //document now contains files split by tags with their entire content

            String previousYear = "";

            Letter letter = null;

            for (Element element : doc.getElementsByClass("section")) //we are interested in sections
            {

                if (element.getElementsByTag("p").size() == 0)  //that simply means that there is no content and that is not a letter that can be considered
                {
                    continue;
                }

                String sectionId = element.id();

                letter = new Letter();

                if (StringUtils.isNotEmpty(sectionId))  //section should have an id in most cases
                {

                } else
                {
                    String oldLetter = "";
                }

                String header = element.text();

                boolean useSection = true;

                if (StringUtils.isNotEmpty(header) && header.matches("(.*ДЕЛОВЫЕ БУМАГИ.*|.*ПРИЛОЖЕНИЕ.*|.*СПИСОК ПИСЕМ.*|.*АЛФАВИТНЫЙ УКАЗАТЕЛЬ.*)"))
                {
                    useSection = false; //ignore irrelevant sections
                }



                if (useSection)
                {
                      //used for test
//                    if ("Письма. 1859 г.".equalsIgnoreCase(documentPointer.getSourse()))
//                    {
//                        String stop = "";
//                    }

//                if (letter !=null)
//                { //letter is created - this only can happen if the file is new - we assume incorrectly that letters don't cross file boundary


                    letter.setSource(documentPointer.getSourse());

                    StringBuilder content = new StringBuilder();

                    for (Element child : element.children())
                    {

//                        for (Attribute att : child.attributes())
//                        {
//                            //System.out.println(att.getKey() + " " + att.getValue());
//                        }

                        String className = child.className();

                        String childId = child.id();

                        if (StringUtils.isNotEmpty(sectionId) &&
                                ((StringUtils.isNotEmpty(className) && className.contains("center")) ||
                                        (StringUtils.isNotEmpty(childId) && childId.contains("sigil_toc_id"))))
                        {
                            String toWhom = child.getElementsByTag("strong").text();

                            if (person == null && letter.getTo().size() == 0) //allow it only once per letter
                            {
                                if (StringUtils.isEmpty(toWhom))
                                {
                                    System.out.print(toWhom);
                                    System.out.print("\t");
                                    toWhom = оldCyrillicFilter(child.text());
                                    System.out.println(toWhom);
                                }

                                String[] toWhomArray = toWhom.split("(\\sи\\s)|(,)");

                                for (String to : toWhomArray)
                                {
                                    RussianDate.parseToWhom(letter, to);
                                }

                            } else if (person != null)
                            {
                                letter.getTo().add(person);

                                if (StringUtils.isNotEmpty(toWhom) && toWhom.matches(".{0,3}\\d{1,4}.{0,3}"))
                                {

                                    letter.setId(toWhom.replaceAll("\\D", ""));

                                }


                            }

                            //check if there is anything else here and find date and place - it will be replaced if exists below


                            String entireText = оldCyrillicFilter(child.text());

                            String tail = entireText.replace(toWhom, "");

                            if (StringUtils.isNotEmpty(tail))
                            {
                                RussianDate.parseDateAndPlace(letter, tail, previousYear);
                            }

                            // System.out.println("two whom\t " +  child.getElementsByTag("strong").text() );

                        }

                        if ("Data".equalsIgnoreCase(child.className()))
                        {

                            previousYear = parseDateAndPlace(previousYear, letter, child);

                            // System.out.println("when and where\t " + child.getElementsByTag("em").text());


                        } else if ("table".equalsIgnoreCase(child.tagName()))  //try to see if Date is there
                        {
                            Element dateElement = child.select("p.Data").first();

                            if (dateElement != null)
                            {

                                previousYear = parseDateAndPlace(previousYear, letter, child);

                                // System.out.println("when and where\t " + child.getElementsByTag("em").text());


                            } else {

                                 dateElement = child.select("p").first();

                                 if (dateElement.text().matches("\\d{4}\\s?г.\\s{1,2}[А-яA-z]{3,9}\\s{1,2}\\d{1,2}\\.?\\s{0,2}.{0,25}")
                                         && letter.getDate() == null) //we still don't have date
                                 {
                                     previousYear = parseDateAndPlace(previousYear, letter, child);
                                 }

                            }

                        } else if (child.text().matches("\\d{4}\\s?г.\\s{1,2}[А-яA-z]{3,9}\\s{1,2}\\d{1,2}\\.?\\s{0,2}.{0,25}")
                                && letter.getDate() == null) //we still don't have date
                        {
                            previousYear = parseDateAndPlace(previousYear, letter, child);
                        } else if ("petit".equalsIgnoreCase(child.className()) || "Textpetit_otstup".equalsIgnoreCase(child.className()))
                        {
                            letter.getNotes().add(child.text());

                        } else
                        {
                            //System.out.println(child.text() );
                            content.append(оldCyrillicFilter(child.text()) + "\n");

                        }

//                  System.out.println(child.tag() + "\n" );
//                  System.out.println(child.outerHtml() + "\n" + child.text());
                    }

                    letter.setContent(content.toString());

                    if (letter.getDate() !=null &&
                            StringUtils.isNotEmpty(letter.getContent()) && letter.getContent().length() > 10) //at least some content is necessary
                    {
                        if (StringUtils.isEmpty(letter.getId()))
                        {
                            letter.setId(sectionId != null ? sectionId : "unknown - " + (int) (Math.random() * 100));
                            ///  System.out.println(letter.toString());
                        }

                        letters.add(letter);
                    } else
                    {
                        rejectedLetters.add(letter);
                    }
                }
            }


//            ObjectWriter ow = new com.fasterxml.jackson.databind.ObjectMapper().writer().withDefaultPrettyPrinter();
//
//            for (Letter letter : letters)
//            {
////                if (letter.getDate() == null)
////                {
//                for (Person person : letter.getTo())
//                {
////                        if (StringUtils.isNotEmpty(person.getLastName()))
////                        {
//                    String json = ow.writeValueAsString(letter);
//
//                    System.out.println(json);
////                        }
//                }
//                //}
//
//            }


        } catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("----------------------------------------------------------");

        System.out.println(documentPointer.toString() + " Letters: " + letters.size() + " Rejected letters: " + rejectedLetters.size());

        for (Letter letter: rejectedLetters)
        {
            System.out.println(letter.toString());
        }

    }

    private static String parseDateAndPlace(String previousYear, Letter letter, Element child)
    {
        if (child.getElementsByTag("em") != null && StringUtils.isNotEmpty(child.getElementsByTag("em").text()))
        {
            String letterDatePlace = оldCyrillicFilter(child.getElementsByTag("em").text());

            if ("567".equalsIgnoreCase(letter.getId()))
            {
                String test = "";
            }

            RussianDate.parseDateAndPlace(letter, letterDatePlace, previousYear);

            if (letter.getDate() != null)
            {
                LocalDate localDate = letter.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                int year = localDate.getYear();
                previousYear = year + "";
            }
        } else if (StringUtils.isNotEmpty(child.text()))
        {
            String letterDatePlace = оldCyrillicFilter(child.text());

            RussianDate.parseDateAndPlace(letter, letterDatePlace, previousYear);

            if (letter.getDate() != null)
            {
                LocalDate localDate = letter.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                int year = localDate.getYear();
                previousYear = year + "";
            }
        }

        return previousYear;
    }

    private static String оldCyrillicFilter(String text)
    {

        return text.replaceAll("\\u0463", "е").replaceAll("\\u0462", "Е").
                replaceAll("(\\u042A|\\u044A)\\b", "").replaceAll("\\u0456", "и").replaceAll("\\u0406", "И");
    }

    @Test
    public void testOldCyrillicFilter()
    {

      String conv = оldCyrillicFilter("Въ маѣ мѣсяцѣ текущаго года я вмѣстѣ съ учениками первой и второй Казанской гимназіи подвергался испытанію, съ цѣлью поступить въ число студентовъ Казанскаго университета разряда арабско-турецкой словесности. Но какъ на этомъ испытаніи не оказалъ надлежащихъ свѣдѣній въ исторіи, статистикѣ, то и прошу покорнѣйше ваше превосходительство дозволить мнѣ нынѣ снова экзаменоваться въ этихъ предметахъ.");


        Assert.assertTrue(conv.equals("В мае месяце текущаго года я вместе с учениками первой и второй Казанской гимназии подвергался испытанию, с целью поступить в число студентов Казанскаго университета разряда арабско-турецкой словесности. Но как на этом испытании не оказал надлежащих сведений в истории, статистике, то и прошу покорнейше ваше превосходительство дозволить мне ныне снова экзаменоваться в этих предметах."));

        // note that spelling is old and that will prevent correct search for such text it may be desirable to have both old and new spelling.

    }

}
