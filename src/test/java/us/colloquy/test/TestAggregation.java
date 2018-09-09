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

package us.colloquy.test;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import us.colloquy.util.ResourceLoader;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;

/**
 * Created by Peter Gershkovich on 3/19/17.
 */
public class TestAggregation
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
    public String getLetterHistogram()
    {
        String[] indices = new String[2];

        indices[0] = "tolstoy_letters";
        indices[1] = "tolstoy_diaries";

        StringBuilder sb = new StringBuilder();

        sb.append("date");
        sb.append(",");
        sb.append("letters");

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        try (RestHighLevelClient elasticClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(properties.getProperty("elastic_ip_address"), 9200, "http"),
                        new HttpHost(properties.getProperty("elastic_ip_address"), 9201, "http"))))
        {

            SearchRequest searchRequest = new SearchRequest(indices);

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            searchSourceBuilder.query(QueryBuilders.matchAllQuery());

            DateHistogramAggregationBuilder dhb = AggregationBuilders.dateHistogram("day").field("date")
                    .dateHistogramInterval(DateHistogramInterval.DAY);

            searchSourceBuilder.aggregation(dhb);

            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);

            Aggregations aggregations = searchResponse.getAggregations();

            ParsedDateHistogram agg = aggregations.get("day");

            String stop = "";

            // For each entry
            for ( Histogram.Bucket entry : agg.getBuckets() )
            {
                DateTime day = ( DateTime ) entry.getKey();                    // bucket key

                long docCount = entry.getDocCount();            // Doc count

                if ( docCount > 0 )
                {

                    sb.append("\n");

                    sb.append(formatter.format(day.toDate()));
                    sb.append(",");
                    sb.append(docCount);
                }
            }


        } catch ( Exception e )
        {
            e.printStackTrace();
        }

        return sb.toString();

    }


//    @Test
//    public void AggregateResults() throws Exception
//    {
//
//        Settings settings = Settings.builder()
//                .put("cluster.name", properties.getProperty("elastic_cluster_name")).build();
//
//
//        try (   TransportClient client = new PreBuiltTransportClient(settings).addTransportAddress(new TransportAddress(InetAddress.getByName(properties.getProperty("elastic_ip_address")), 9300)))
//        {
//
//            SearchResponse sr = client.prepareSearch("tolstoy")
//                  //  .setQuery( /* your query */ )
//                    .addAggregation(AggregationBuilders.dateHistogram("date_agg").field("date")
//                            .dateHistogramInterval(DateHistogramInterval.DAY))
//                    .execute().actionGet();
//
//
//            InternalDateHistogram terms = sr.getAggregations().get("date_agg");
//            List<InternalDateHistogram.Bucket> buckets = terms.getBuckets();
//
//
//
//                for (Histogram.Bucket bucket : buckets) {
//                    if (bucket.getDocCount() > 0)
//                    System.out.println(bucket.getKeyAsString().replaceAll("T.*",",") + bucket.getDocCount());
//                }
//
//
//
//        } catch (Throwable t)
//        {
//            t.printStackTrace();
//        }
//
//
//    }
}



