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

import org.junit.Before;
import org.junit.Test;
import us.colloquy.util.ResourceLoader;

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

    }


}
