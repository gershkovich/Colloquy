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
import us.colloquy.model.DateAndPlace;
import us.colloquy.model.DiaryEntry;
import us.colloquy.model.Letter;
import us.colloquy.model.Person;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Peter Gershkovich on 12/5/15.
 */
public class RussianDate
{

    final static Pattern patternToWhom = Pattern.compile("(\\d{1,4})\\.\\s{1,2}([\\p{IsCyrillic}]*)\\.\\s{1,3}([\\p{IsCyrillic}]*)\\.\\s{1,2}" +
            "([\\p{IsCyrillic}]*)(.*)");

    final static Pattern patternToWhomLetterMissmatch =
            Pattern.compile("(\\d{1,4})\\.\\s{1,2}([\\p{IsCyrillic}HAETOPKXCBM]*)\\.\\s{1,3}([\\p{IsCyrillic}HAETOPKXCBM]*)\\.\\s{1,2}" +
                    "([\\p{IsCyrillic}]*)(.*)");


    final static Pattern patternToWhomName = Pattern.compile("([\\p{IsCyrillic}])\\.?\\s{1,3}([\\p{IsCyrillic}])\\.?\\s{1,3}([\\p{IsCyrillic}]*)(.*)");

    final static Pattern cyrillic = Pattern.compile("([\\p{IsCyrillic}]*)");


    final static Pattern patternToWhomForeign = Pattern.compile("(\\d{1,4})\\.\\s{0,3}([\\p{IsCyrillic}]*)\\.?\\s{0,3}([\\p{IsCyrillic}]*)(.*[A-Za-z]{2,10}.*)");

    final static Pattern patternToWhomNumberAnything = Pattern.compile("(\\d{1,3})\\.\\s{0,3}(.*)");

    public static DateAndPlace parseDateAndPlace(String dateStr, String lastYearUsed)
    {
        String previouslyUsedYear = lastYearUsed;

        String[] dateLocationElements = dateStr.split("\\s|\\.\\.\\.");

        DateAndPlace dap = new DateAndPlace();

        assembleDate(previouslyUsedYear, dateLocationElements, dap);


        String elCleared = dateStr.replaceAll("[?*\\[\\]]", ""); //clear date string of all extra characters


        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", new Locale("ru"));

        try
        {
            if (StringUtils.isNotEmpty(dap.getYear()) && StringUtils.isNotEmpty(dap.getMonth()) &&
                    StringUtils.isNotEmpty(dap.getDay()))
            {
                dap.setDate(format.parse(dap.getDateString()));
            } else if (StringUtils.isNotEmpty(dap.getYear()) && StringUtils.isNotEmpty(dap.getMonth()))

            {
                dap.setDay("1");
                dap.setDate(format.parse(dap.getDateString()));
            } else if (StringUtils.isNotEmpty(dap.getYear()) && elCleared.matches("(\\d{4}\\s{0,3}г\\.?.{0,30})|" +
                    "(\\d{4}\\.\\.\\.\\d{4}\\s{0,3}г{1,2}.{0,30})|" +
                    "(\\d{4}—\\d{4}\\s{0,3}г{1,2}.{0,30})"))

            {   //we do that only if entry contains one year

                dap.setDay("1");
                dap.setMonth("Января");
                dap.setDate(format.parse(dap.getDateString()));
            }

        } catch (ParseException e)
        {

            e.printStackTrace();
            return null;
        }

        return dap;

    }


    public static void parseDateAndPlace(Letter letter, String dateStr, String lastYearUsed)
    {
        String previouslyUsedYear = lastYearUsed;

        String[] dateLocationElements = dateStr.split("\\s|\\.\\.\\.");

        DateAndPlace dap = new DateAndPlace();

        assembleDate(previouslyUsedYear, dateLocationElements, dap);

        letter.setPlace(dap.getPlace().toString());

        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", new Locale("ru"));

        String elCleared = dateStr.replaceAll("[?*\\[\\]]", ""); //clear date string of all extra characters

        try
        {
            if (StringUtils.isNotEmpty(dap.getYear()) && StringUtils.isNotEmpty(dap.getMonth()) &&
                    StringUtils.isNotEmpty(dap.getDay()))
            {
                dap.setDate(format.parse(dap.getDateString()));

                letter.setDate(dap.getDate());
            } else if (StringUtils.isNotEmpty(dap.getYear()) && StringUtils.isNotEmpty(dap.getMonth()))
            {
                dap.setDay("1");

                dap.setDate(format.parse(dap.getDateString()));

                letter.setDate(dap.getDate());
            } else if (StringUtils.isNotEmpty(dap.getYear()) && elCleared.matches("(\\d{4}\\s{0,3}г\\.?.{0,30})|" +
                    "(\\d{4}\\.\\.\\.\\d{4}\\s{0,3}г{1,2}.{0,30})|" +
                    "(\\d{4}—\\d{4}\\s{0,3}г{1,2}.{0,30})"))

            {   //we do that only if entry contains one year


                dap.setDay("1");
                dap.setMonth("Января");
                dap.setDate(format.parse(dap.getDateString()));
                letter.setDate(dap.getDate());
            }


        } catch (ParseException e)
        {

            e.printStackTrace();
        }

    }

    public static void parseDateAndPlace(DiaryEntry diaryEntry, String dateStr, String lastYearUsed)
    {
        String previouslyUsedYear = lastYearUsed;

        String[] dateLocationElements = dateStr.split("(\\s)|(\\.)|(/)");

        DateAndPlace dap = new DateAndPlace();

        assembleDate(previouslyUsedYear, dateLocationElements, dap);

        diaryEntry.setPlace(dap.getPlace().toString());

        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", new Locale("ru"));

        String elCleared = dateStr.replaceAll("[?*\\[\\]]", "").trim(); //clear date string of all extra characters

        try
        {
            if (StringUtils.isNotEmpty(dap.getYear()) && StringUtils.isNotEmpty(dap.getMonth()) &&
                    StringUtils.isNotEmpty(dap.getDay()))
            {
                dap.setDate(format.parse(dap.getDateString()));

                diaryEntry.setDate(dap.getDate());

            } else if (StringUtils.isNotEmpty(dap.getYear()) && StringUtils.isNotEmpty(dap.getMonth()))
            {
                dap.setDay("1");

                dap.setDate(format.parse(dap.getDateString()));

                diaryEntry.setDate(dap.getDate());
            } else if (StringUtils.isNotEmpty(dap.getYear()) && elCleared.matches("(\\d{4}\\s{0,3}г\\.?.{0,30})|" +
                    "(\\d{4}\\.\\.\\.\\d{4}\\s{0,3}г{1,2}.{0,30})|" +
                    "(\\d{4}—\\d{4}\\s{0,3}г{1,2}.{0,30})|(\\d{4})"))

            {   //we do that only if entry contains one year


                dap.setDay("1");
                dap.setMonth("Января");
                dap.setDate(format.parse(dap.getDateString()));
                diaryEntry.setDate(dap.getDate());
            }


        } catch (ParseException e)
        {

            e.printStackTrace();
        }

    }


    private static void assembleDate(String previouslyUsedYear, String[] dateLocationElements, DateAndPlace dap)
    {
        for (String el : dateLocationElements)
        {
            if (el.contains("?"))
            {
                dap.setApproximate(true);
            }

            String elCleared = el.replaceAll("[?*\\[\\]]", "");

            if (elCleared.matches("\\d{1,2}\\.?"))
            {
                try
                {
                    int dayInt = Integer.valueOf(elCleared.replaceAll("\\D", ""));

                    if (dayInt < 32)
                    {
                        dap.setDay(dayInt + ""); //todo not ideal need validation by months but is probably fine here
                    }

                } catch (NumberFormatException ignored)
                {
                }


            } else if (elCleared.matches("\\d{4}\\.?"))
            {
                dap.setYear(elCleared.replaceAll("\\D", ""));

                previouslyUsedYear = dap.getYear();

            } else if (elCleared.matches("\\d{1,2}(—|-)\\d{1,2}\\.?"))
            {
                dap.setDay(elCleared.replaceAll("(—|-).*", "").replaceAll("\\D", ""));


            } else
            {

                String month = matchRussianMonth(elCleared, dap.getDay());

                if (StringUtils.isNotEmpty(month))
                {
                    dap.setMonth(month);

                } else if (!elCleared.matches("(г|от)\\.?"))
                {
                    if (dap.getPlace().length() > 0)
                    {
                        dap.getPlace().append(" ");
                    }

                    dap.getPlace().append(elCleared);
                }
            }

            if (StringUtils.isEmpty(dap.getYear()))
            {
                dap.setYear(previouslyUsedYear);
            }

        }
    }


    private static String matchRussianMonth(String el, String dateValue)
    {
        String filteredMonth = "";

        if (StringUtils.isNotEmpty(el) &&
                el.matches("[\\p{IsCyrillic}Map]*\\.?"))
        {
            filteredMonth = el.replaceAll("p", "р").replaceAll("a", "а").replaceAll("M", "М").replaceAll("\\.", "").trim();

            if ("Июн".equalsIgnoreCase(filteredMonth))
            {
                String stop = "";
            }

            //check if it matches beginnings of a month otherwise mark it as a par of a place
            String[] months = {"январь", "января", "февраль", "февраля", "март", "марта", "апрель", "апреля", "май", "мая", "июнь", "июня",
                    "июль", "июля", "август", "августа", "сентябрь", "сентября", "октябрь", "октября", "ноябрь", "ноября",
                    "декабрь", "декабря"};

            for (String month : months)
            {
                if (month.equalsIgnoreCase(filteredMonth) ||
                        (filteredMonth.length() > 2 && month.matches("(?iu)" + filteredMonth + "[А-я]{0,6}")
                                && StringUtils.isNotEmpty(dateValue))) //we cant assume that a fragment without prior date is a month so we check if date is already present otherwise we will ignore abbreviations
                {
                    return month;
                }
            }
        }

        return "";

    }

    private static void matchYear(String string, StringBuilder russianDateBuilder, Pattern p)
    {
        Matcher m = p.matcher(string);

        if (m.find())
        {

            if (StringUtils.isNotEmpty(m.group(1)))
            {
                russianDateBuilder.append("1 Января ");
                russianDateBuilder.append(m.group(1));
            }

        }
    }


    public static void parseToWhom(Letter letter, String toWhom)
    {

        //* 229. Л. Л. Толстому.

        //A. A. Фету. mixed characters

        //  System.out.println(toWhom);

        String filteredString = toWhom.replaceAll("[\\*\\[\\]]", "").trim();

        if (toWhom.contains("?"))
        {
            System.out.println("approximate info.");
        }

        boolean nameIsSet = setLetterBySlavicNameMach(letter, toWhom);  //first try


        if (!nameIsSet)
        {
            nameIsSet = setLetterBySlavicNameLangErr(letter, toWhom);  //language recognition error H english confused with russian.
        }

        if (!nameIsSet)
        {
            nameIsSet = setLetterByForeignPattern(letter, toWhom);  //language recognition error H english confused with russian.
        }

        if (!nameIsSet)
        {
            //1. Ректору Казанского университета Н. И. Лобачевскому.
            nameIsSet = setLetterByNumberAndTextPattern(letter, toWhom);  //language recognition error H english confused with russian.
        }
    }

    private static boolean setLetterByNumberAndTextPattern(Letter letter, String filteredString)
    {
        Person person = new Person();

        Matcher m = patternToWhomNumberAnything.matcher(filteredString);

        if (m.find())
        {
            letter.setId(m.group(1));

//            if (StringUtils.isNotEmpty(m.group(2)) && m.group(2).length() > 1)
//            {
//               convertFirstNameToNominativeCase(m.group(2), person);
//
//            } else
//            {
//                person.setFirstName(m.group(2));
//            }
//
//            if (StringUtils.isNotEmpty(m.group(3)))
//            {
//             //   Matcher cyr = cyrillic.matcher(m.group(3));
//
////                if (cyr.find())
////                {
////                    person.setPaternalName(m.group(2));
////
////                }
//
//                String lastName = m.group(3).trim();
//
//                // convertLastNameToNominativeCase(lastName, person);
//
//                person.setLastName(lastName);
//
//            } else
//            {
//                String lastName = m.group(2).trim();
//
//                //convertLastNameToNominativeCase(lastName, person);
//
//                person.setLastName(lastName);
//            }

            person.setOriginalEntry(filteredString);

            letter.getTo().add(person);
        }

        return StringUtils.isNotEmpty(person.getLastName());

    }

    private static boolean setLetterByForeignPattern(Letter letter, String filteredString)
    {
        Person person = new Person();

        Matcher m = patternToWhomForeign.matcher(filteredString);

        if (m.find())
        {
            letter.setId(m.group(1));

            if (StringUtils.isNotEmpty(m.group(2)) && m.group(2).length() > 1)
            {
                convertFirstNameToNominativeCase(m.group(2), person);

            } else
            {
                person.setFirstName(m.group(2));
            }

            if (StringUtils.isNotEmpty(m.group(3)))
            {
                //   Matcher cyr = cyrillic.matcher(m.group(3));

//                if (cyr.find())
//                {
//                    person.setPaternalName(m.group(2));
//
//                }

                String lastName = m.group(3).trim();

                // convertLastNameToNominativeCase(lastName, person);

                person.setLastName(lastName);

            } else
            {
                String lastName = m.group(2).trim();

                //convertLastNameToNominativeCase(lastName, person);

                person.setLastName(lastName);
            }

            person.setOriginalEntry(filteredString);

            letter.getTo().add(person);
        }

        return StringUtils.isNotEmpty(person.getLastName());

    }


    private static boolean setLetterBySlavicNameLangErr(Letter letter, String filteredString)
    {
        Matcher m = patternToWhomLetterMissmatch.matcher(filteredString);

        Person person = new Person();

        person.setOriginalEntry(filteredString);



        if (m.find())
        {

            System.out.println("Language recognition error detected in addressee: " + filteredString);

            // System.out.println(m.group(1) + "\t" + m.group(2) + "\t" + m.group(3) + "\t" + m.group(4) + "\t" + m.group(5));

            letter.setId(m.group(1));

            if (StringUtils.isNotEmpty(m.group(2)))
            {
                String firstName = m.group(2).trim();

                if (StringUtils.isNotEmpty(firstName) && firstName.length() > 1)
                {
                    //convertFirstNameToNominativeCase(firstName, person);   //that did not work may need revision

                    person.setFirstName(firstName);

                } else
                {
                    //replacement for typical errors
                    person.setFirstName(firstName.replace("H", "Н").replace("A", "А").replace("E", "Е")
                            .replace("T", "Т").replace("O", "О").replace("P", "Р")
                            .replace("K", "К").replace("X", "Х").replace("C", "С").replace("B", "В").replace("M", "М"));

                }
            }

            if (StringUtils.isNotEmpty(m.group(4)))
            {
                Matcher cyr = cyrillic.matcher(m.group(4));  //means russian last name - in that case we replace first and paternal name as well

                if (cyr.find())
                {
                    String paternalName = m.group(3);

                    if (paternalName.length() > 1)
                    {
                        person.setPaternalName(paternalName);

                    } else
                    {
                        person.setPaternalName(paternalName.replace("H", "Н").replace("A", "А").replace("E", "Е")
                                .replace("T", "Т").replace("O", "О").replace("P", "Р")
                                .replace("K", "К").replace("X", "Х").replace("C", "С").replace("B", "В").replace("M", "М"));

                    }
                }

                String lastName = m.group(4).trim();

                convertLastNameToNominativeCase(lastName, person);   //

            } else
            {
                String lastName = m.group(3).trim();

                convertLastNameToNominativeCase(lastName, person);
            }

            letter.getTo().add(person);

        }

        return StringUtils.isNotEmpty(person.getLastName());
    }

    private static boolean setLetterBySlavicNameMach(Letter letter, String filteredString)
    {

        Person person = new Person();

        person.setOriginalEntry(filteredString);


        Matcher m = patternToWhom.matcher(filteredString);


        if (m.find())
        {
            // System.out.println(m.group(1) + "\t" + m.group(2) + "\t" + m.group(3) + "\t" + m.group(4) + "\t" + m.group(5));

            letter.setId(m.group(1));


            if (StringUtils.isNotEmpty(m.group(2)))
            {
                String firstName = m.group(2).trim();

                if (StringUtils.isNotEmpty(firstName) && firstName.length() > 1)
                {
                    //convertFirstNameToNominativeCase(firstName, person);
                    person.setFirstName(firstName);

                } else
                {
                    person.setFirstName(firstName);
                }
            }

            if (StringUtils.isNotEmpty(m.group(4)))
            {
                Matcher cyr = cyrillic.matcher(m.group(4));

                if (cyr.find())
                {
                    String paternalName = m.group(3);

                    if (paternalName.length() > 1)
                    {
                        person.setPaternalName(paternalName);

                    } else
                    {
                        if (paternalName.matches("H"))
                        {
                            person.setPaternalName("Н"); //русская Н

                        } else
                        {
                            person.setPaternalName(paternalName);
                        }
                    }
                }

                String lastName = m.group(4).trim();

                convertLastNameToNominativeCase(lastName, person);

            } else
            {
                String lastName = m.group(3).trim();

                convertLastNameToNominativeCase(lastName, person);
            }

            letter.getTo().add(person);

        }

        return StringUtils.isNotEmpty(person.getLastName());
    }

    private static void convertLastNameToNominativeCase(String lastName, Person person)
    {

        if (lastName.endsWith("ой"))
        {
            person.setLastName(lastName.replaceAll("ой$", "a"));

        } else if (lastName.endsWith("му"))
        {
            person.setLastName(lastName.replaceAll("ому$", "ой"));

        } else if (lastName.endsWith("у"))
        {
            person.setLastName(lastName.replaceAll("у$", ""));

        } else
        {
            person.setLastName(lastName);
        }

        person.setLastName(lastName); //todo think about this conversion perhaps it is better handled by name recognition and matching to a name index.
    }

    private static void convertFirstNameToNominativeCase(String firstName, Person person)
    {
        if (firstName.endsWith("ой"))
        {
            person.setFirstName(firstName.replaceAll("ой$", "a"));

        } else if (firstName.endsWith("у"))
        {
            person.setFirstName(firstName.replaceAll("у$", ""));

        } else
        {
            person.setFirstName(firstName);
        }

        person.setFirstName(firstName);

    }
}
