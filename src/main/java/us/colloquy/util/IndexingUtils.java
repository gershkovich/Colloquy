/*
 * Copyright (c) 2018. Tatyana Gershkovich
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

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;

/**
 * Created by Peter Gershkovich on 9/8/18.
 */
public class IndexingUtils
{

    public static void makeIndex(String indexName, String type, RestHighLevelClient elasticClient) throws IOException
    {
        CreateIndexRequest indexRequest = new CreateIndexRequest(indexName);

        GetIndexRequest getIndexRequest = new GetIndexRequest();

        getIndexRequest.indices(indexName);

        if ( elasticClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT) )
        {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);

            DeleteIndexResponse deleteIndexResponse = elasticClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT );
        }

        String mapping = null;

        if ( "diaries".equalsIgnoreCase(type) )
        {
            mapping = DIARIES_MAPPING;

        } else if ("letters".equalsIgnoreCase(type))
        {
            mapping = LETTERS_MAPPING;
        }

        indexRequest.mapping(type, mapping, XContentType.JSON);

        CreateIndexResponse createIndexResponse = elasticClient.indices().create(indexRequest);

        System.out.println("Index created!" + createIndexResponse.index());
    }


    static String DIARIES_MAPPING = "{\n" +
            "      \"diaries\": {\n" +
            "        \"properties\": {\n" +
            "          \"date\": {\n" +
            "            \"type\": \"date\"\n" +
            "          },\n" +
            "          \"entry\": {\n" +
            "            \"type\": \"text\"\n" +
            "          },\n" +
            "          \"id\": {\n" +
            "            \"type\": \"keyword\"\n" +
            "          },\n" +
            "          \"place\": {\n" +
            "            \"type\": \"text\"\n" +
            "          },\n" +
            "          \"source\": {\n" +
            "            \"type\": \"text\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    }";




    static String LETTERS_MAPPING = "{\"letters\": {\n" +
            "        \"properties\": {\n" +
            "          \"content\": {\n" +
            "            \"type\": \"text\"\n" +
            "          },\n" +
            "          \"date\": {\n" +
            "            \"type\": \"date\"\n" +
            "          },\n" +
            "          \"documentPointer\": {\n" +
            "            \"type\": \"keyword\"\n" +
            "          },\n" +
            "          \"id\": {\n" +
            "            \"type\": \"keyword\"\n" +
            "          },\n" +
            "          \"notes\": {\n" +
            "            \"type\": \"text\"\n" +
            "          },\n" +
            "          \"place\": {\n" +
            "            \"type\": \"text\"\n" +
            "          },\n" +
            "          \"source\": {\n" +
            "            \"type\": \"text\"\n" +
            "          },\n" +
            "          \"to\": {\n" +
            "            \"properties\": {\n" +
            "              \"firstName\": {\n" +
            "                \"type\": \"text\"\n" +
            "              },\n" +
            "              \"lastName\": {\n" +
            "                \"type\": \"text\"\n" +
            "              },\n" +
            "              \"originalEntry\": {\n" +
            "                \"type\": \"text\"\n" +
            "              },\n" +
            "              \"paternalName\": {\n" +
            "                \"type\": \"text\"\n" +
            "              }\n" +
            "            }\n" +
            "          },\n" +
            "          \"toWhom\": {\n" +
            "            \"type\": \"text\"\n" +
            "          }\n" +
            "        }\n" +
            "      }}";


}
