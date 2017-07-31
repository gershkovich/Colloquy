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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import us.colloquy.model.reference.RefIndex;
import us.colloquy.model.reference.References;
import us.colloquy.model.reference.ToWhom;
import us.colloquy.util.ResourceLoader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Peter Gershkovich on 7/15/17.
 */
public class TestLoadReference
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
    public void loadReference() throws Exception
    {

        Map<String, String>  toWhomMap = ResourceLoader.loadToWhomMap("references/toWhom.json");

              

        System.out.println(toWhomMap.get("59-1"));

        System.out.println(toWhomMap.get("72-182"));

        System.out.println(toWhomMap.get("72-162-а"));

        System.out.println(toWhomMap.get("72-182а"));

        System.out.println(toWhomMap.get("72-162а"));



    }
}
