/*
 * Copyright (c) 2017. Tatyana Gershkovich
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import us.colloquy.model.DiaryEntry;
import us.colloquy.model.Letter;

import java.net.InetAddress;
import java.util.List;
import java.util.Properties;

/**
 * Created by Peter Gershkovich on 12/23/17.
 */
public class ElasticLoader
{
    public static void uploadLettersToElasticServer(Properties properties, List<Letter> letterList)
    {
        Settings settings = Settings.builder()
                .put("cluster.name", properties.getProperty("elastic_cluster_name")).build();


        try (TransportClient client = new PreBuiltTransportClient(settings).
                addTransportAddress(new TransportAddress(InetAddress.getByName(properties.getProperty("elastic_ip_address")), 9300)))
        {

            BulkRequestBuilder bulkRequest = client.prepareBulk();

            //this is strait forward indexing - for test and validation just comment it out
            indexLetters(letterList, client, bulkRequest);


        } catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    private static void indexLetters(List<Letter> letterList, Client client, BulkRequestBuilder bulkRequest) throws JsonProcessingException
    {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();


        for (Letter letter : letterList)
        {
            String json = ow.writeValueAsString(letter);

            String name = "unk";

            if (letter.getTo().size() > 0)
            {
                name = letter.getTo().get(0).getLastName();

            }

            String id = letter.getId() + "-" + letter.getDate() + "-" + letter.getSource();

            bulkRequest.add(client.prepareIndex("tolstoy_letters", "letters", id)
                    .setSource(json, XContentType.JSON)
            );
        }

        BulkResponse bulkResponse = bulkRequest.get();

        if (bulkResponse.hasFailures())
        {
            // process failures by iterating through each bulk response item
            for (BulkItemResponse br : bulkResponse.getItems())
            {
                System.out.println(br.getFailureMessage());
            }
        }
    }


    public static void uploadDiariesToElasticServer(Properties properties, List<DiaryEntry> diaryEntries)
    {
        Settings settings = Settings.builder()
                .put("cluster.name", properties.getProperty("elastic_cluster_name")).build();

        try (TransportClient client = new PreBuiltTransportClient(settings).
                addTransportAddress(new TransportAddress(InetAddress.getByName(properties.getProperty("elastic_ip_address")), 9300)))
        {

            BulkRequestBuilder bulkRequest = client.prepareBulk();

            //this is strait forward indexing - for test and validation just comment it out
            indexDiaries(diaryEntries, client, bulkRequest);


        } catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    private static void indexDiaries(List<DiaryEntry> diaryEntryList, Client client, BulkRequestBuilder bulkRequest) throws JsonProcessingException
    {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        for (DiaryEntry diary : diaryEntryList)
        {
            String json = ow.writeValueAsString(diary);

            String id = diary.getId() + "-" + diary.getDate() + "-" + diary.getSource();

            bulkRequest.add(client.prepareIndex("tolstoy_diaries", "diaries", id)
                    .setSource(json, XContentType.JSON)
            );
        }

        BulkResponse bulkResponse = bulkRequest.get();

        if (bulkResponse.hasFailures())
        {
            // process failures by iterating through each bulk response item
            for (BulkItemResponse br : bulkResponse.getItems())
            {
                System.out.println(br.getFailureMessage());
            }
        }
    }

}
