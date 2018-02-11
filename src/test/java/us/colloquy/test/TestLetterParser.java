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

package us.colloquy.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.junit.Before;
import org.junit.Test;
import us.colloquy.model.DocumentPointer;
import us.colloquy.model.Letter;
import us.colloquy.model.Person;
import us.colloquy.model.reference.ToWhom;
import us.colloquy.util.EpubExtractor;
import us.colloquy.util.ResourceLoader;
import us.colloquy.util.RussianDate;
import us.colloquy.util.Unzip;

import java.io.*;
import java.net.InetAddress;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static us.colloquy.util.ElasticLoader.uploadLettersToElasticServer;

/**
 * Created by Peter Gershkovich on 9/10/16.
 */
public class TestLetterParser
{

    final Properties properties = new Properties();

    /**
     * Load properties in from the properties file.
     */
    @Before
    public void setUp()
    {

        ResourceLoader.loadProperties(properties);

    }

    /**
     * The first step is to decompress and archive to some location from where all letters will be processed
     */
    @Test
    public void decompressEPUBFiles()
    {
        try
        {
            Unzip.unZipFilesFromTo(System.getProperty("user.home") + "/IdeaProjects/Colloquy/samples/volumes",
                    System.getProperty("user.home") + "/IdeaProjects/Colloquy/samples/volumes/expanded" , "epub");


        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }



    /**
     * Run this method to test parsing of Tolstoy's letters
     */
    @Test
    public void parseDocuments()
    {
        Map<String,String> toWhomMap = null;


        //load map of addressees (this is something that comes from a parsed last volume of Tolstoy's 90 volume addition (91st volume is the index of names))
        try {

            toWhomMap =  ResourceLoader.loadToWhomMap("references/toWhom.json");

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        List<Letter> entireLetterList = new ArrayList<>();

        List<Letter> rejectedLetters =  new ArrayList<>();   //a list for rejected letter to review and improve the algorithm

        Set<DocumentPointer> documentPointers = new TreeSet<>(); //a document pointer points to a particular file that contains letters

        String homeDir = System.getProperty("user.home");

        EpubExtractor.getURIForAllLetters(documentPointers, homeDir + "/IdeaProjects/Colloquy/samples/volumes/expanded/tom_83_pisma_sa_tolstoy", true);
        EpubExtractor.getURIForAllLetters(documentPointers, homeDir + "/IdeaProjects/Colloquy/samples/volumes/expanded/tom_84_pisma_sa_tolstoy", true);


//        EpubExtractor.getURIForAllLetters(documentPointers, System.getProperty("user.home") + "/Documents/Tolstoy/unzipLettersSpecial/Pisma_k_Chertkovu_toma_87-89", true);
//        EpubExtractor.getURIForAllLetters(documentPointers, System.getProperty("user.home") + "/Documents/Tolstoy/unzipLettersSpecial/Pisma_k_Chertkovu_toma_87-89", true);


        EpubExtractor.getURIForAllLetters(documentPointers, homeDir + "/IdeaProjects/Colloquy/samples/volumes/expanded/85_tom_chertkovy", true);
        EpubExtractor.getURIForAllLetters(documentPointers, homeDir + "/IdeaProjects/Colloquy/samples/volumes/expanded/86_tom_chertkovy", true);
        EpubExtractor.getURIForAllLetters(documentPointers, homeDir + "/IdeaProjects/Colloquy/samples/volumes/expanded/87_tom_chertkovy", true);
        EpubExtractor.getURIForAllLetters(documentPointers, homeDir + "/IdeaProjects/Colloquy/samples/volumes/expanded/88_tom_chertkovy", true);
        EpubExtractor.getURIForAllLetters(documentPointers, homeDir + "/IdeaProjects/Colloquy/samples/volumes/expanded/89_tom_chertkovy", true);




        //Here we collect all URIs to files containing letter details are in EpubExtractor
//        -----  UNCOMMENT for ALL OTHER LETTERS
      EpubExtractor.getURIForAllLetters(documentPointers, homeDir + "/IdeaProjects/Colloquy/samples/volumes/expanded", false);
//
//
        Person person = null;
        //------------

        Map<String, List<DocumentPointer>> volumeMap = new LinkedHashMap<>();

        //now we collect all URI pointers to each decompressed volume a volume can contain more than one source and we create a map here
        for (DocumentPointer pointer : documentPointers)
        {
            if (volumeMap.containsKey(pointer.getSourse()))
            {
                volumeMap.get(pointer.getSourse()).add(pointer);

            } else
            {
                List<DocumentPointer> dp = new ArrayList<>();
                dp.add(pointer);
                volumeMap.put(pointer.getSourse(), dp);
            }

        }

        //for each volume

        for (String volume: volumeMap.keySet())
        {
            List<Letter> letterList = new ArrayList<>();

            //and for each document pointer
            for (DocumentPointer dp: volumeMap.get(volume))
            {
                //for every pointer we get a letter - this is the core code for parsing letters
                //PAY ATTENTION
                us.colloquy.util.LetterParser.parseLetters(dp, letterList, person, toWhomMap, rejectedLetters);
                //test case "temp/OEBPS/Text/0001_1006_2002.xhtml"
            }

            entireLetterList.addAll(letterList);

            System.out.println("---------------------------------------------------------- rejected ");

            System.out.println("All selected letters:" + entireLetterList.size() + ";" +
                    " Letters: " + letterList.size() + "; Total number of rejected letters: " + rejectedLetters.size());

            try
            {
                ObjectWriter ow = new com.fasterxml.jackson.databind.ObjectMapper().writer().withDefaultPrettyPrinter();

                System.out.println("-------------------- Start rejected letters ------------- ");

                String json = ow.writeValueAsString(rejectedLetters);

                System.out.println(json);

                System.out.println("-------------------- End rejected letters ------------- ");

                json = ow.writeValueAsString(letterList);

                System.out.println("-------------------- Start processing selected letters ------------- ");

                //write json to file if export is set to true
                if (properties.getProperty("export_to_json").equalsIgnoreCase("true"))
                {
                    try (Writer out = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream("parsed/letters/" + volume.replaceAll("Полное собрание сочинений. ", "").replaceAll("Том", "Volume")
                                    .trim().replaceAll("\\s", "_").trim() + ".json"), "UTF-8")))
                    {
                        out.write(json);

                    }
                }

                //print to a file
                // System.out.println(json);

                System.out.println("-------------------- End letters ------------- ");


            } catch (Exception e)
            {
                e.printStackTrace();
            }

            System.out.println("Total number of letters: " + letterList.size());

        }

        //code below to check a few letters
        //testFewLetters(entireLetterList);


        if (properties.getProperty("upload_to_elastic").equalsIgnoreCase("true"))
        {
            uploadLettersToElasticServer(properties, entireLetterList);
        }

//        // write your code here
//        Settings settings = Settings.settingsBuilder()
//                .put("cluster.name", properties.getProperty("elastic_cluster_name")).build();
//
//        //open index balk load all letters and process them
//        try (Client client = TransportClient.builder().settings(settings).build()
//                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(properties.getProperty("elastic_ip_address")), 9300)))
//        {
//            BulkRequestBuilder bulkRequest = client.prepareBulk();
//
//            //this is strait forward indexing - for test and validation just comment it out
//            indexLetters(letterList, client, bulkRequest);
//
//
//        } catch (Throwable t)
//        {
//            t.printStackTrace();
//        }
    }

    private void testFewLetters(List<Letter> entireLetterList)
    {
        int i = 0;

        for (Letter letter : entireLetterList)
        {
            i++;

            if (letter.getDate() == null)
            {
                System.out.println(" ------------------------------------------------------------------ no date");
                System.out.println(letter.toString());

            }

            if (i > 15)
            {
                break;
            }
        }


        Pattern pattern =
                Pattern.compile("([a-zA-Z]+\\s?)");


        ObjectWriter ow = new com.fasterxml.jackson.databind.ObjectMapper().writer().withDefaultPrettyPrinter();

        for (Letter letter : entireLetterList)
        {
//                if (letter.getDate() == null)
//                {

            for (Person p : letter.getTo())
            {
//                        if (StringUtils.isNotEmpty(person.getLastName()))
//                        {
                String json = null;

                try
                {
                    json = ow.writeValueAsString(letter);

                } catch (JsonProcessingException e)
                {
                    e.printStackTrace();
                }

              //  System.out.println(json);

              //  System.out.println("Content: " + letter.getContent());
//                        }

                Matcher m = pattern.matcher(letter.getContent());

                StringBuffer sb = new StringBuffer();

                while (m.find()) {

                    m.appendReplacement(sb, "{" + m.group(1) + "}");
                }
                m.appendTail(sb);

             //   System.out.println("Content: " + sb.toString());
            }


            //}

        }
    }


    @Test
    public void testUnderlineEngLetter() throws Exception
    {
        Pattern patternToWhomLetterMissmatch =
                Pattern.compile("(\\d{1,4})\\.\\s{1,2}([\\p{IsCyrillic}HAETOPKXCBM]*)\\.\\s{1,3}([\\p{IsCyrillic}HAETOPKXCBM]*)\\.\\s{1,2}" +
                        "([\\p{IsCyrillic}]*)(.*)");

        Pattern pattern =
                Pattern.compile("([a-zA-Z]+\\s?)");

        String testSt = "Владимир Васильевич,\n" +
                "Очень рады будем вашему приезду, а также милого Ильяса.1 Только, пожалуйста, известите за день до вашего отъезда из Петерб[урга], чтобы мы могли приготовить, что нужно.\n" +
                "Как хорошо, что здоровье ваше справилось.\n" +
                "Так до свиданья.2\n" +
                "Лев Толстой.\n" +
                "25 дек. 1904.\n" +
                "На обороте секретки: Петербург, Владимиру Васильевичу Стасову, Публичная библиотека.";




        String line = "Владимир Васильевич this is great Владимир Васильевич\n test Владимир Васильевич another one";

        Matcher m = pattern.matcher(line);

        int i = 0;

        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            System.out.println();
          //  System.out.println(m.group(2));
            i ++;
            m.appendReplacement(sb, "{" + m.group(1) + "}");
        }
        m.appendTail(sb);

        System.out.println(sb);
    }


    @Test
    public void parseLocation() throws Exception
    {
        /*
        <h2 class="center" id="sigil_toc_id_28"><strong>1866</strong></h2>
<p><strong>68. А. Н. Бибикову.</strong><em> Февраля 4. Москва.</em> Упоминается в письме Толстого к Т. А. Ергольской от 4 февраля.</p>
<p><strong>69. Е. Л. Маркову.</strong> <em>Март — апреля начало. Я. П.</em> Упоминается в письме Е. Л. Маркова к Толстому от 11 апреля: «Благодарю вас за ваше дружелюбное письмо, Лев Николаевич, которое было особенно дорого получить в Крыму».</p>
<p><strong>70. А. Е. Берсу.</strong><em> Апреля 4. Я. П.</em> Упоминается в письме Толстого к М. С. Башилову от 4 апреля.</p>
<p><strong>71. А. Е. Берсу.</strong><em> Мая 11...13. Я. П.</em> Упоминается в письме А. Е. Берса к Толстому от 16 мая.</p>
<p><strong>72. И. И. Орлову.</strong><em> Мая 1...20? Я. П.</em> Упоминается в письме Толстого к Д. А. Дьякову от 23 мая.</p>
<p><strong>73. А. Е. Берсу.</strong><em> Мая 23...25. Я. П.</em> Упоминается в письме А. Е. Берса к Т. А. Кузминской от 27 мая: «Сегодня получил письмо от Толстого, в котором он объявляет нам, что Соня обочлась целым месяцем и изволила 22 мая родить сына Илью».</p>
<p><strong>74—75. А. Е. Берсу.</strong> <em>Мая конец — июня начало Я. П. </em>Упоминаются в письме А. Е. Берса к Толстому от 6 июня: «Благодарю тебя... за частые известия об Софье. Я даже не успел еще ответить тебе на два последние твои письма».</p>
<p><strong>76. А. Е. Берсу.</strong><em> Июня средина. Я. П.</em> Упоминается в письме А. Е. Берса к Толстому от 25 июня: «Приехав в Москву 22 июня вечером, я нашел два письма от милой моей Тани и твою приписочку, в которой сказано, чтобы я скорее прислал тебе 500 р.».</p>
<p><strong>77—78. А. А. Толстой.</strong><em> Июля средина — конец. Я. П.</em> Упоминаются в статье Толстого «Воспоминание о суде над солдатом Шибуниным»: «...я написал... А. А. Толстой, прося ее ходатайствовать перед государем... но по рассеянности не написал имени полка, в котором происходило дело... Она написала это мне, я поторопился ответить...» (Б, II, стр. 98).</p>
<p><strong>79—80. А. Е. Берсу.</strong><em> Июля конец. Я. П. </em>Упоминаются в письме А. Е. Берса к Л. Н. и С. А. Толстым от 3 августа: «Ваши два последние письма были весьма неутешительны».</p>
<p><strong>81. Л. А. Берс.</strong><em> Сентября 12...14? Я. П. </em>Упоминается в письме А. Е. Берса к Л. Н. и С. А. Толстым от 18 сентября: «Вчера отпраздновали именины мама. Ваши письма получили мы накануне... Я так и обмер, когда прочел, что ты берешь с собой на охоту Колокольцова...»</p>
<p><strong>82. Т. А. Берс.</strong><em> Октября 27...29? Я. П. </em>Упоминается в письме Т. А. Берс к С. А. Толстой от 1 ноября.</p>
<p><strong>83. А. А. Берсу.</strong><em> Ноября 12. Москва.</em> Упоминается в письме к С. А. Толстой от 12 ноября (т. 83, № 56).</p>
<p><strong>84. Т. А. Берс.</strong><em> Ноября 19...21. Я. П.</em> Упоминается в письме Т. А. Берс к Л. Н. и С. А. Толстым от 23 ноября.</p>
         */


        Letter letter = new Letter();

        String previousYear = "1866";
        
        String tail = "1899 г. Октября 22. Я. П.";


        tail = "1883 г. Середина декабря. Москва.";

        RussianDate.parseDateAndPlace(letter, tail, previousYear);  //a parser that figures out date and place if they are pres

        System.out.println(letter);


    }


    @Test
    public void testParseAddressee() throws Exception
    {
//        Pattern patternToWhomLetterMissmatch =
//                Pattern.compile("(\\.\\s{1,2}([\\p{IsCyrillic}HAETOPKXCBM]*)\\.\\s{1,3}([\\p{IsCyrillic}HAETOPKXCBM]*)\\.\\s{1,2}" +
//                        "([\\p{IsCyrillic}]*)(.*)");

        Pattern patternToWhom =
                Pattern.compile("(\\s{1,2}([а-я]))");


        String line = "Ба да лбекову Фридуну Хану";

     
        Matcher m = patternToWhom.matcher(line);

        int i = 0;

        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            System.out.println();
            //  System.out.println(m.group(2));
            i ++;
            m.appendReplacement(sb, "{" + m.group(1) + "}");
        }
        m.appendTail(sb);

        System.out.println(sb);
    }


}
