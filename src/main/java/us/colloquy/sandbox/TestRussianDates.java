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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.junit.Test;
import us.colloquy.model.DateAndPlace;
import us.colloquy.model.Letter;
import us.colloquy.util.RussianDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertTrue;


/**
 * Created by Peter Gershkovich on 12/5/15.
 */
public class TestRussianDates
{
    @Test
    public void parseRussianDate()
    {

        String[] russianDates = { "1845—1855 ? гг."};

        String previouslyUsedYear = "1862";

        for (String dateStr : russianDates)
        {

            DateAndPlace dap =  RussianDate.parseDateAndPlace(dateStr, "1863");

            System.out.println(dateStr + "\t" + dap.toString() + "\t" + dap.getDate());

        }


        // Pattern p =  Pattern.compile("([\\p{IsCyrillic}]{1,8})[\\s,.\\[\\]]{1,3}(\\d{1,2})[\\s\\[]{1,3}(\\d{4})(.*)");


        String russDate = "12 Сентябрь 1862";

        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", new Locale("ru"));

        //   DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(new Locale("ru")).parse(russDate);

        try
        {
            System.out.println(format.parse(russDate));


            //  System.out.println(timePoint.toString());


        } catch (ParseException e)
        {
            e.printStackTrace();
        }


        String date =
                DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
                        .withLocale(new Locale("ru"))
                        .format(LocalDate.of(2014, 2, 28));
        System.out.println(date); // output: 28 февраля 2014 г.

    }




    @Test
    public void testToWhom()
    {

        String[] toWhomVariants = {"* 229. Л. Л. Толстому.", "224. М. А. Шмидт и О. А. Баршевой.", "* 221. Н. Н. Ге (сыну).",
                "* 226. Гамильтону Кэмп[беллу] (Hamilton Camp[bell]).", "* 227. Алонзо Холлистеру (Alonzo Hollister).", "* 234. А. Н. Дунаеву.",
                "230. Н. Н. Страхову."};

        // String [] toWhomVariants = {"230. H. Н. Страхову."};


        List<Letter> letterList = new ArrayList<>();

        for (String toWhomString : toWhomVariants)
        {

            //get rid of sq ([]) brackets that indicate best guess and get rid of *
            toWhomString = toWhomString.replaceAll("[\\*\\[\\]]", "").trim();

            Letter letter = new Letter();

            //split array by letter и that separates two people
            String[] toWhomArray = toWhomString.split("(\\sи\\s)|(,)");

            //find parenthetic expression that indicates additional info (translation, clarification e.g. сын)

            for (String to : toWhomArray)
            {
                RussianDate.parseToWhom(letter, to);


            }

            letterList.add(letter);

        }

        ObjectWriter ow = new com.fasterxml.jackson.databind.ObjectMapper().writer().withDefaultPrettyPrinter();

        for (Letter letter : letterList)
        {
            try
            {
                String json = ow.writeValueAsString(letter);
                System.out.println(json);

            } catch (JsonProcessingException e)
            {
                e.printStackTrace();
            }
        }


    }

    @Test
    public void testMatchDateString()
    {
      assertTrue( "1859 г. Сентября 11. Никольское-Вяземское.".matches("\\d{4}\\s?г.\\s{1,2}[А-яA-z]{3,9}\\s{1,2}\\d{1,2}\\.?\\s{0,2}.{0,25}"));

        assertTrue( "1869 г. Мая 10. Я. П".matches("\\d{4}\\s?г.\\s{1,2}[А-яA-z]{3,9}\\s{1,2}\\d{1,2}\\.?\\s{0,2}.{0,25}"));

    }


}
