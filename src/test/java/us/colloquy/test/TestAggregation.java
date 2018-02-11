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

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Before;
import org.junit.Test;
import us.colloquy.util.ResourceLoader;

import java.net.InetAddress;
import java.util.Collection;
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
    public void AggregateResults() throws Exception
    {

        Settings settings = Settings.builder()
                .put("cluster.name", properties.getProperty("elastic_cluster_name")).build();


        try (   TransportClient client = new PreBuiltTransportClient(settings).addTransportAddress(new TransportAddress(InetAddress.getByName(properties.getProperty("elastic_ip_address")), 9300)))
        {

            SearchResponse sr = client.prepareSearch("tolstoy")
                  //  .setQuery( /* your query */ )
                    .addAggregation(AggregationBuilders.dateHistogram("date_agg").field("date")
                            .dateHistogramInterval(DateHistogramInterval.DAY))
                    .execute().actionGet();


            InternalDateHistogram terms = sr.getAggregations().get("date_agg");
            List<InternalDateHistogram.Bucket> buckets = terms.getBuckets();

          

                for (Histogram.Bucket bucket : buckets) {
                    if (bucket.getDocCount() > 0)
                    System.out.println(bucket.getKeyAsString().replaceAll("T.*",",") + bucket.getDocCount());
                }



        } catch (Throwable t)
        {
            t.printStackTrace();
        }


    }
}



