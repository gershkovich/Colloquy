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
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.Before;
import org.junit.Test;
import us.colloquy.model.DocumentPointer;
import us.colloquy.model.Letter;
import us.colloquy.model.Person;
import us.colloquy.util.EpubExtractor;
import us.colloquy.util.ResourceLoader;
import us.colloquy.util.Unzip;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.*;

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
            Unzip.unZipFilesFromTo(System.getProperty("user.home") + "/IdeaProjects/Colloquy/samples/letters",
                    System.getProperty("user.home") + "/IdeaProjects/Colloquy/samples/letters/expanded" , "epub");



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

        List<Letter> letterList = new ArrayList<>();

        //get аll letters
        Set<DocumentPointer> documentPointers = new TreeSet<>();

        String homeDir = System.getProperty("user.home");

//        EpubExtractor.getURIForAllLetters(documentPointers, homeDir + "/Documents/Tolstoy/unzipLettersSpecial/pisma_k_Sofie_83-84(1)", true);
//        Person person = new Person();
//        person.setLastName("Толстой");
//        person.setFirstName("Софье");
//        person.setPaternalName("Андреевне");


//        getURIForAllLetters(documentPointers, System.getProperty("user.home") + "/Documents/Tolstoy/unzipLettersSpecial/Pisma_k_Chertkovu_toma_87-89", true);
//        getURIForAllLetters(documentPointers, System.getProperty("user.home") + "/Documents/Tolstoy/unzipLettersSpecial/Pisma_k_Chertkovu_toma_87-89", true);
//        Person person = new Person();
//        person.setLastName("Черткову");
//        person.setFirstName("В");
//        person.setPaternalName("Г");


        //Here we collect all URIs to files containing letter details are in EpubExtractor
        EpubExtractor.getURIForAllLetters(documentPointers, homeDir + "/IdeaProjects/Colloquy/samples/letters/expanded", false);

        Person person = null;

        for (DocumentPointer pointer : documentPointers)
        {
            //for every pointer we get letter - this is the core code for extracting letters
            us.colloquy.util.LetterParser.parseLetters(pointer, letterList, person);   //test case "temp/OEBPS/Text/0001_1006_2002.xhtml"

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



        ObjectWriter ow = new com.fasterxml.jackson.databind.ObjectMapper().writer().withDefaultPrettyPrinter();
//
        for (Letter letter : letterList)
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

                System.out.println(json);
//                        }
            }
            //}

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


}
