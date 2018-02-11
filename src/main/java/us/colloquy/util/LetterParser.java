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

import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static us.colloquy.util.CommonTextParser.оldCyrillicFilter;

/**
 * Created by Peter Gershkovich on 12/20/15.
 */
public class LetterParser
{


    final static Pattern patternId = Pattern.compile("\\*\\s?(\\d{1,3}[а-я]{0,1})");
    final static Pattern patternId2 = Pattern.compile("^(\\d{1,3}[а-я]{0,1})");


    /**
     * This is the hart of parsing process
     *
     * @param documentPointer
     * @param letters
     * @param toWhomMap
     */

    public static void parseLetters(DocumentPointer documentPointer, List<Letter> letters, Person person, Map<String, String> toWhomMap, List<Letter> rejectedLetters)
    {
        File input = new File(documentPointer.getUri());

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

                } else   //we try to find if a child node contains section id
                {
                    String oldLetter = "";

                    Elements elementChildren = element.getElementsByClass("center");

                    for (Element child : elementChildren)
                    {
                        if (child.id() != null && child.id().contains("sigil_toc_id"))
                        {
                            sectionId = child.id();
                        }
                    }

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
                    letter.setDocumentPointer(documentPointer.getUri());

                    StringBuilder content = new StringBuilder();

                    for (Element child : element.children())
                    {
                        replaceSupTag(child);

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

                            if (childId.contains("sigil_toc_id_126"))
                            {
                                String stop = "";

                            }

                            String toWhom = child.getElementsByTag("strong").text();

                            if (toWhom.contains("193. М. С. Башилову."))
                            {
                                String stop = "";

                            }

                            //find to whom from the index
                            String volume = letter.getSource().replaceAll("\\D", "").trim();

                            //typically this entry contains a one to three digits occasionally followed by a small case letter
                            if (StringUtils.isNotEmpty(toWhom))
                            {

                                String letterId = extractId(toWhom.trim());

                                String toWhomFromMap = null;

                                if (volume.equalsIgnoreCase("83") || volume.equalsIgnoreCase("84"))
                                {
                                    toWhomFromMap = "Толстой Софье Андреевне";

                                    person = new Person();
                                    person.setLastName("Толстой");
                                    person.setFirstName("Софье");
                                    person.setPaternalName("Андреевне");
                                    person.setOriginalEntry("not found");

                                } else if (volume.equalsIgnoreCase("85") || volume.equalsIgnoreCase("86")
                                        || volume.equalsIgnoreCase("87") || volume.equalsIgnoreCase("88") || volume.equalsIgnoreCase("89"))
                                {
                                    toWhomFromMap = "Черткову Владимиру Григорьевичу";

                                    person = new Person();
                                    person.setLastName("Черткову");
                                    person.setFirstName("Владимиру");
                                    person.setPaternalName("Григорьевичу");
                                    person.setOriginalEntry("not found");


                                } else
                                {

                                    toWhomFromMap = toWhomMap.get(volume + "-" + letterId);
                                }

                                letter.setToWhom(toWhomFromMap);

                                letter.setId(volume + "-" + letterId);
                            }

                            if (person == null && letter.getTo().size() == 0) //allow it only once per letter
                            {
                                if (StringUtils.isEmpty(toWhom))
                                {
                                    System.out.print("Unable to figure out to whom: " + toWhom);
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

                                    // letter.setId(toWhom.replaceAll("\\D", ""));

                                }

                            }

                            //check if there is anything else here and find date and place - it will be replaced if exists below


                            String entireText = оldCyrillicFilter(child.text());

                            String tail = entireText.replace(toWhom, "");

                            if (StringUtils.isNotEmpty(tail) && letter.getDate() == null) //only if date is not already set
                            {
                                RussianDate.parseDateAndPlace(letter, tail, previousYear);
                            }

                            // System.out.println("two whom\t " +  child.getElementsByTag("strong").text() );

                        }

                        if ("Data".equalsIgnoreCase(child.className()))
                        {

                            //find out if child has text or it is an em element
                            String directChild = child.text();

                            if (person !=null)
                            {
                                person.setOriginalEntry(directChild);
                                letter.getTo().clear();
                                letter.getTo().add(person);

                            } else
                            {
                                System.out.println("Error - " + letter);
                            }

                            previousYear = parseDateAndPlace(previousYear, letter, child);

                            // System.out.println("when and where\t " + child.getElementsByTag("em").text());

                            //if we don't have original entry at this point make data from that child
                            if (letter.getTo().size() > 0 )
                            {
                                if (StringUtils.isNotEmpty(child.text()))
                                {
                                    letter.getTo().get(0).setOriginalEntry(child.text());
                                }
                            }

                        } else if ("table".equalsIgnoreCase(child.tagName()))  //try to see if Date is there
                        {
                            Element dateElement = child.select("p.Data").first();

                            if (dateElement != null)
                            {

                                previousYear = parseDateAndPlace(previousYear, letter, child);

                                String directChild = child.text();

                                letter.getTo().get(0).setOriginalEntry(directChild);

                                // System.out.println("when and where\t " + child.getElementsByTag("em").text());


                            } else
                            {

                                dateElement = child.select("p").first();

                                if (dateElement.text().matches("\\d{4}\\s?г.\\s{1,2}[А-яA-z]{3,9}\\s{1,2}\\d{1,2}\\.?\\s{0,2}.{0,25}")
                                        && letter.getDate() == null) //we still don't have date
                                {
                                    previousYear = parseDateAndPlace(previousYear, letter, child);

                                    String directChild = child.text();

                                    letter.getTo().get(0).setOriginalEntry(directChild);
                                }

                            }

                        } else if (child.text().matches("\\d{4}\\s?г.\\s{1,2}[А-яA-z]{3,9}\\s{1,2}\\d{1,2}\\.?\\s{0,2}.{0,25}")
                                && letter.getDate() == null) //we still don't have date
                        {
                            previousYear = parseDateAndPlace(previousYear, letter, child);

                            String directChild = child.text();

                            letter.getTo().get(0).setOriginalEntry(directChild);


                        } else if ("petit".equalsIgnoreCase(child.className()) || "Textpetit_otstup".equalsIgnoreCase(child.className()))
                        {
                            letter.getNotes().add(оldCyrillicFilter(child.text()));

                        } else
                        {

                            String text = child.text();

                            if (StringUtils.isNotEmpty(text))
                            {
                                if (letter.getTo().size() > 0 && StringUtils.isNotEmpty(letter.getTo().get(0).getOriginalEntry())
                                        && !letter.getTo().get(0).getOriginalEntry().equalsIgnoreCase(text))
                                {
                                    //System.out.println(child.text() );
                                    content.append(оldCyrillicFilter(child.text()) + "\n");
                                }
                            }


                        }

//                  System.out.println(child.tag() + "\n" );
//                  System.out.println(child.outerHtml() + "\n" + child.text());
                    }

                    letter.setContent(content.toString());

                    if (letter.getDate() != null && letter.getToWhom() != null &&
                            StringUtils.isNotEmpty(letter.getContent()) && letter.getContent().length() > 10) //at least some content is necessary
                    {
                        if (StringUtils.isEmpty(letter.getId()))
                        {
                            if (StringUtils.isEmpty(letter.getId()))
                            {
                                letter.setId(sectionId != null ? sectionId : "unknown - " + (int) (Math.random() * 100));
                            }
                            ///  System.out.println(letter.toString());
                        }

                        letters.add(letter);

                    } else
                    {
                        if (StringUtils.isEmpty(letter.getId()))
                        {
                            letter.setId(sectionId != null ? sectionId : "unknown - " + (int) (Math.random() * 100));
                        }

                        rejectedLetters.add(letter);
                    }
                }
            }


        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private static void replaceSupTag(Element child)
    {
        Elements elements = child.getElementsByTag("sup");

        for(Element e : elements)
        {
            String value = e.text();

            e.replaceWith(new TextNode("[" + value + "]", null));
        }



    }

    private static String extractId(String filteredString)
    {
        String id = "";
        Matcher m = patternId.matcher(filteredString);

        if (m.find())
        {
            if (StringUtils.isNotEmpty(m.group(1)))
            {
                id = m.group(1);
            }

        }

        if (StringUtils.isEmpty(id))
        {
            m = patternId2.matcher(filteredString);

            if (m.find())
            {
                if (StringUtils.isNotEmpty(m.group(1)))
                {
                    id = m.group(1);
                }

            }

        }


        return id.trim();

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


    @Test
    public void testOldCyrillicFilter()
    {

        String conv = оldCyrillicFilter("Въ маѣ мѣсяцѣ текущаго года я вмѣстѣ съ учениками первой и второй Казанской гимназіи подвергался испытанію, съ цѣлью поступить въ число студентовъ Казанскаго университета разряда арабско-турецкой словесности. Но какъ на этомъ испытаніи не оказалъ надлежащихъ свѣдѣній въ исторіи, статистикѣ, то и прошу покорнѣйше ваше превосходительство дозволить мнѣ нынѣ снова экзаменоваться въ этихъ предметахъ.");


        Assert.assertTrue(conv.equals("В мае месяце текущаго года я вместе с учениками первой и второй Казанской гимназии подвергался испытанию, с целью поступить в число студентов Казанскаго университета разряда арабско-турецкой словесности. Но как на этом испытании не оказал надлежащих сведений в истории, статистике, то и прошу покорнейше ваше превосходительство дозволить мне ныне снова экзаменоваться в этих предметах."));

        // note that spelling is old and that will prevent correct search for such text it may be desirable to have both old and new spelling.

    }

}
