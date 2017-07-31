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



import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import us.colloquy.model.reference.RefIndex;
import us.colloquy.model.reference.References;
import us.colloquy.model.reference.ToWhom;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Peter Gershkovich on 9/11/16.
 */
public class ResourceLoader
{

   private static final Logger logger = LogManager.getLogger(ResourceLoader.class.getName());

    public static void loadProperties(Properties properties)
    {
        InputStream propertiesFileInputStream = null;

        if (properties == null)
        {
            properties = new Properties();
        }

        try
        {
            propertiesFileInputStream = ResourceLoader.class.getClassLoader().getResourceAsStream("properties.xml");

            if (propertiesFileInputStream != null)
            {
                properties.loadFromXML(propertiesFileInputStream);

                propertiesFileInputStream.close();

                logger.info("Properties have been loaded.");


            } else
            {

               logger.error("Properties not loaded available");

            }

        } catch (Exception e)
        {
            e.printStackTrace();

        } finally
        {

            if (propertiesFileInputStream != null)
            {
                try
                {
                    propertiesFileInputStream.close();

                } catch (IOException e)
                {
                    // Do nothing.
                }
            }
        }
    }

    public static Map<String, String> loadToWhomMap (String toWhomJsonFile) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally

        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        References ref = mapper.readValue(new File(toWhomJsonFile), References.class);


//        JsonFactory f = new JsonFactory();
//        JsonParser jp = f.createParser(json);
//        // advance stream to START_ARRAY first:
//        jp.nextToken();
//         // and then each time, advance to opening START_OBJECT
//        while (jp.nextToken() == JsonToken.START_OBJECT)) {
//        Foo foobar = mapper.readValue(jp, Foo.class);
//           // process
//            // after binding, stream points to closing END_OBJECT
//          }

        Map<String, String> toWhomMap = new HashMap<>();

        for (ToWhom toWhom : ref.getDocuments())
        {
            if (toWhom.getRefIndexList().size() > 0)
            {
                for (RefIndex idx : toWhom.getRefIndexList())
                {
                    String volume =  idx.getVolume().trim();

                    for (String letterId: idx.getLetters())
                    {
                        toWhomMap.put(volume + "-" + letterId.trim(), toWhom.getOriginalEntry());
                        if (letterId.contains("-"))
                        {
                            toWhomMap.put(volume + "-" + letterId.replaceAll("-","").trim(), toWhom.getOriginalEntry());

                        }
                    }

                }

            }
        }

      return toWhomMap;
    }

}
