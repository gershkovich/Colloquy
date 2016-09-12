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

package us.colloquy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;


public class Main {

    public static void main(String[] args) {
	// write your code here
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "humanity").build();

        try (Client client = TransportClient.builder().settings(settings).build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300)))
        {


            //Client client =    new TransportClient(settings);

// on shutdown

            /*
            {
  "settings": {
    "analysis": {
      "filter": {
        "russian_stop": {
          "type":       "stop",
          "stopwords":  "_russian_"
        },
        "russian_keywords": {
          "type":       "keyword_marker",
          "keywords":   []
        },
        "russian_stemmer": {
          "type":       "stemmer",
          "language":   "russian"
        }
      },
      "analyzer": {
        "russian": {
          "tokenizer":  "standard",
          "filter": [
            "lowercase",
            "russian_stop",
            "russian_keywords",
            "russian_stemmer"
          ]
        }
      }
    }
  }
}
             */

// on startup

//        Node node = nodeBuilder().clusterName("pg_cluster").client(false).node();
//
//        Client client = node.client();

            String indexConf = "{\n" +
                    "    \"index\" : {\n" +
                    "        \"analysis\" : {\n" +
                    "            \"analyzer\" : {\n" +
                    "                \"my_analyzer\" : {\n" +
                    "                    \"type\" : \"snowball\",\n" +
                    "                    \"language\" : \"English\"\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "}" ;

            /*
            PUT /movies
{
  "mappings": {
    "movie": {
      "properties": {
        "title": {
          "type":       "string"
        },
        "title_br": {
            "type":     "string",
            "analyzer": "brazilian"
        },
        "title_cz": {
            "type":     "string",
            "analyzer": "czech"
        },
        "title_en": {
            "type":     "string",
            "analyzer": "english"
        },
        "title_es": {
            "type":     "string",
            "analyzer": "spanish"
        }
      }
    }
  }
             */


            ChronicleEntry chronicleEntry1 =  getChronicle();

            /*



»	4	т. начал было продолжать статью об искус-

стве, но «слишком глубоко запахал» (Д 5 янв.).

»	»	Лесков сообщает Т. о получении его письма

с «ободряющими строчками» по поводу
             */


            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

            String json = ow.writeValueAsString(chronicleEntry1);


           // System.out.println(json);

            IndexResponse response = client.prepareIndex("tolstoy", "chronicle")
                    .setSource(json)
                    .execute()
                    .actionGet();


           System.out.println(response.getIndex());
// on shutdown
        } catch (Throwable t)
        {
           t.printStackTrace();
        }
       // node.close();
    }

    public static ChronicleEntry getChronicle() throws ParseException
    {
        SimpleDateFormat sd = new SimpleDateFormat("MM/dd/yyy");


        ChronicleEntry cr = new ChronicleEntry();


        cr.setEvent("Чтение книги Э. Ренана «L’avenir de la science» [«Будущее науки»]. «Вся блестит умом и тонкими, верными, глубокими замечаниями о самых важных предметах»; но «самоуверенность ученого непогрешимого поразительна» (п. к H. Н. Страхову 7 янв., Юб. 65, № 206; Д 25 янв.).");


        cr.setEvent("В статью о непротивлении [«Царство божие\n" +
                "внутри вас»] Т. пишет о церкви (Д).");
        cr.setEventTime(sd.parse("01/02/1891"));
        cr.getPeople().add("");
        cr.setPlace("Ясная Поляна");


        return cr;
    }
}
