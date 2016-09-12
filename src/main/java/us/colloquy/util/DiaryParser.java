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

import us.colloquy.model.DiaryEntry;
import us.colloquy.model.DocumentPointer;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Peter Gershkovich on 12/27/15.
 */
public class DiaryParser
{

    ///Documents/Tolstoy/openDiaries/dnevnik_1893(2)/OEBPS/Text/0001_1006_2001.xhtml

    private static Pattern yearPattern = Pattern.compile(".{0,20}(\\d{4}).{0,20}");

    @Test
    public void useJsoup()
    {
        //File input = new File(System.getProperty("user.home") + "/Documents/Tolstoy/openDiaries/dnevnik_1893(2)/OEBPS/Text/0001_1006_2001.xhtml");
        //   File input = new File(System.getProperty("user.home") + "/IdeaProjects/ElasticTest/temp/dnevnik_1862(1)/OEBPS/Text/0001_1006_2001.xhtml");

        File input = new File(System.getProperty("user.home") + "/Documents/Tolstoy/90-volume-set/diaries/uzip/dnevnik_1881-1887_vol_49/OEBPS/Text/0001_1011_2005.xhtml");

        String previousYear = "";

        String sourse = "pointer";

        List<DiaryEntry> diaryEntrys = new ArrayList<>();

        try
        {
            Document doc = Jsoup.parse(input, "UTF-8");

            for (Element element : doc.getElementsByClass("section"))
            {
                DiaryEntry diaryEntry = null;

                StringBuilder contentBuilder = new StringBuilder();

                for (Element child : element.children())
                {
//                    for (Attribute att : child.attributes())
//                    {
//                        //   System.out.println(att.getKey() + " " + att.getValue());
//                    }
                    //we need to assume that each element is a continuation unless the entry is a date that starts a new entry
                    //the problem is to distinguish between an entry that contains date and place vs date within an entry

                    //lets try to see if element is a date

                    DiaryEntry diaryEntryToCollectDate = new DiaryEntry();

                    //we send it in two cases when text matches year or when text has em element
                    Element em = child.select("em").first();

                    if (em == null && StringUtils.isNotEmpty(child.text()))
                    {
                        Matcher m = yearPattern.matcher(child.text());

                        if (m.find())
                        {
                            child.text(m.group(1));
                            previousYear = parseDateAndPlace(previousYear, diaryEntryToCollectDate, child);
                        }
                    }

                    if (em != null)
                    {
                        previousYear = parseDateAndPlace(previousYear, diaryEntryToCollectDate, child);
                    }

                    if (diaryEntryToCollectDate.getDate() != null) //this is the begginng of a new entry
                    {
                        System.out.println("Found date: " + diaryEntryToCollectDate.getDate());
                        //create new DiaryEntry
                        if (diaryEntry != null)
                        {
                            diaryEntry.setEntry(contentBuilder.toString());   //add consecutive entries here
                            diaryEntrys.add(diaryEntry);
                        }

                        diaryEntry = new DiaryEntry();
                        diaryEntry.setSource(sourse);
                        diaryEntry.setDate(diaryEntryToCollectDate.getDate());
                        diaryEntry.setPlace(diaryEntryToCollectDate.getPlace());

                        contentBuilder = new StringBuilder();

                    }

                    if (StringUtils.isNotEmpty(child.text()) && child.text().length() > 8)
                    {
                        contentBuilder.append(child.text() + "\n");

                    }
//
//                    System.out.println(child.tag() + "\n");
//                    System.out.println(child.outerHtml() + "\n" + child.text());
                }

                //whatever we still have, add here:
                if (StringUtils.isNotEmpty(contentBuilder.toString()) && diaryEntry != null)
                {
                    diaryEntry.setEntry(contentBuilder.toString());
                    diaryEntrys.add(diaryEntry);
                }
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        for (DiaryEntry diaryEntry : diaryEntrys)
        {
            System.out.println(diaryEntry.toString());
        }
    }


    /**
     * This is the hart of parsing process
     *
     * @param documentPointer
     * @param diaryEntries
     */
    public static void parseDiaries(DocumentPointer documentPointer, List<DiaryEntry> diaryEntries)
    {
        File input = new File(documentPointer.getUri());

        String previousYear = "";

        try
        {
            Document doc = Jsoup.parse(input, "UTF-8");

            for (Element element : doc.getElementsByClass("section"))
            {
                DiaryEntry diaryEntry = null;

                StringBuilder contentBuilder = new StringBuilder();

                for (Element child : element.children())
                {
                    DiaryEntry diaryEntryToCollectDate = new DiaryEntry();

                    //we send it in two cases when text matches year or when text has em element
                    Element em = child.select("em").first();

                    if (em == null && StringUtils.isNotEmpty(child.text()))
                    {
                        Matcher m = yearPattern.matcher(child.text());

                        if (m.find())
                        {
                            //sanity check
                            int year = Integer.valueOf(m.group(1));
                            {
                                if (year > 1840 && year < 1911)
                                {
                                    child.text(year + "");

                                    String prevYearTmp = parseDateAndPlace(previousYear, diaryEntryToCollectDate, child);

                                    if (StringUtils.isNotEmpty(prevYearTmp))
                                    {
                                        previousYear = prevYearTmp;
                                    }
                                }
                            }


                        }
                    }

                    if (em != null)
                    {
                        String prevYearTmp = parseDateAndPlace(previousYear, diaryEntryToCollectDate, child);

                        if (StringUtils.isNotEmpty(prevYearTmp))
                        {
                            previousYear = prevYearTmp;
                        }
                    }

                    if (diaryEntryToCollectDate.getDate() != null) //this is the begginng of a new entry
                    {
                        // System.out.println("Found date: " + diaryEntryToCollectDate.getDate());
                        //create new DiaryEntry
                        if (diaryEntry != null)
                        {
                            diaryEntry.setEntry(contentBuilder.toString());   //add consecutive entries here
                            if (StringUtils.isNotEmpty(diaryEntry.getEntry()))
                            {
                                diaryEntries.add(diaryEntry);
                            }
                        }

                        diaryEntry = new DiaryEntry();
                        diaryEntry.setSource(оldCyrillicFilter(documentPointer.getSourse()));
                        diaryEntry.setDate(diaryEntryToCollectDate.getDate());
                        diaryEntry.setPlace(оldCyrillicFilter(diaryEntryToCollectDate.getPlace()));

                        contentBuilder = new StringBuilder();

                    }

                    if (StringUtils.isNotEmpty(child.text()) && child.text().length() > 8)
                    {
                        contentBuilder.append(child.text() + "\n");

                    }
//
//                    System.out.println(child.tag() + "\n");
//                    System.out.println(child.outerHtml() + "\n" + child.text());
                }

                //whatever we still have, add here:
                if (StringUtils.isNotEmpty(contentBuilder.toString().trim()) && diaryEntry != null)
                {
                    diaryEntry.setEntry(оldCyrillicFilter(contentBuilder.toString()));
                    diaryEntries.add(diaryEntry);
                }
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("------------------------------   " + documentPointer.getUri() + "   ----------------------------");

        //  System.out.println(documentPointer.toString() + " Letters: " + diaryEntries.size() + " Rejected letters: " + rejectedEntries.size());

        for (DiaryEntry diaryEntry : diaryEntries)
        {
            System.out.println(diaryEntry.toString());
        }

        System.out.println("------------------------------  total documents cumulative " + diaryEntries.size() + "   ----------------------------");


    }

    private static String parseDateAndPlace(String previousYear, DiaryEntry diaryEntry, Element child)
    {
        if (child.getElementsByTag("em") != null && StringUtils.isNotEmpty(child.getElementsByTag("em").text()))
        {
            String letterDatePlace = оldCyrillicFilter(child.getElementsByTag("em").text()).replaceAll("\\*\\[\\]", "");

            RussianDate.parseDateAndPlace(diaryEntry, letterDatePlace, previousYear);

            if (diaryEntry.getDate() != null)
            {
                LocalDate localDate = diaryEntry.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                int year = localDate.getYear();
                previousYear = year + "";
            }
        } else if (StringUtils.isNotEmpty(child.text()))
        {
            String letterDatePlace = оldCyrillicFilter(child.text()).replaceAll("\\*\\[\\]", "");

            RussianDate.parseDateAndPlace(diaryEntry, letterDatePlace, previousYear);

            if (diaryEntry.getDate() != null)
            {
                LocalDate localDate = diaryEntry.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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

        оldCyrillicFilter("Въ маѣ мѣсяцѣ текущаго года я вмѣстѣ съ учениками первой и второй Казанской гимназіи подвергался испытанію, съ цѣлью поступить въ число студентовъ Казанскаго университета разряда арабско-турецкой словесности. Но какъ на этомъ испытаніи не оказалъ надлежащихъ свѣдѣній въ исторіи, статистикѣ, то и прошу покорнѣйше ваше превосходительство дозволить мнѣ нынѣ снова экзаменоваться въ этихъ предметахъ.");

    }
}
