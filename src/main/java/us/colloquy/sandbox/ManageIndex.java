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

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Before;
import org.junit.Test;
import us.colloquy.util.ResourceLoader;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

/**
 * Created by Peter Gershkovich on 11/27/15.
 */
public class ManageIndex
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



    @Test
    public void runScrolQuery()

    {

        String indexName = "tolstoy";
        String type = "chronicle";


        /*
        {
    "mytype": {
        "properties": {
            "created": {
                "type":   "multi_field",
                "fields": {
                    "created": { "type": "string" },
                    "date":    { "type": "date"   }
                }
            }
        }
    }
}


 {"mytype": {
         "properties": {
            "created": {
                "type": "multi_field",
                  "fields": {
                     "created": {"type":"string"}
                     "date":{"type":"date"}
                     }
                     }
                     }
                     }
                     }

                      "event": "Чтение книги Э. Ренана «L’avenir de la science» [«Будущее науки»]. «Вся блестит умом и тонкими, верными, глубокими замечаниями о самых важных предметах»; но «самоуверенность ученого непогрешимого поразительна» (п. к H. Н. Страхову 7 янв., Юб. 65, № 206; Д 25 янв.).",
          "eventType": [],
          "people": [
            "Э. Ренан"
          ],
          "place": "Ясная Поляна",
          "eventTime": -2492967600000,
          "eventPeriod": null
        }

         */

        String mapping = "{\n" +
                "         \"chronicle\": {\n" +
                "            \"properties\": {\n" +
                "               \"event\": {\n" +
                "                  \"type\": \"string\"\n" +
                "               },\n" +
                "               \"eventType\": {\n" +
                "                  \"type\": \"string\"\n" +
                "               },\n" +
                "               \"people\": {\n" +
                "                  \"type\": \"string\"\n" +
                "               },\n" +
                "               \"place\": {\n" +
                "                  \"type\": \"string\"\n" +
                "               },\n" +
                "               \"eventTime\": {\n" +
                "                  \"type\": \"date\"\n" +
                "               }" +
                "            }\n" +
                "         }\n" +
                "      }";


//        Settings settings = Settings.settingsBuilder()
//                .put("cluster.name", properties.getProperty("elastic_cluster_name")).build();
//
//        try (Client client = TransportClient.builder().settings(settings).build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300)))
//        {

        Settings settings = Settings.builder()
                .put("cluster.name", "humanity").build();


        try (   TransportClient client = new PreBuiltTransportClient(settings).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300)))
        {


            //create index if it is not already exists.
            if (client.admin().indices().prepareExists(indexName).execute().actionGet().isExists())
            {
                final DeleteIndexRequest deleteIndexRequest=new DeleteIndexRequest(indexName);

                final DeleteIndexResponse deleteIndexResponse= client.admin().indices().delete(deleteIndexRequest).actionGet();

                if (!deleteIndexResponse.isAcknowledged()) {
                    System.out.println("Not deleted");
                }
                else {
                    System.out.println( indexName + " is deleted ");
                }

            }

            client.admin().indices().prepareCreate(indexName).execute().actionGet();

            PutMappingResponse response = client.admin().indices().preparePutMapping(indexName).setType(type).setSource(mapping).execute().actionGet();

            if (!response.isAcknowledged())
            {
                System.out.println("Something strange happens");
            }

        } catch (IOException e)
        {
            System.out.println("Unable to create mapping");
        }

    }


}
