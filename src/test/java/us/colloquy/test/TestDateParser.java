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

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import us.colloquy.model.Letter;
import us.colloquy.model.Person;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Peter Gershkovich on 7/26/17.
 */
public class TestDateParser
{
    final static Pattern patternId = Pattern.compile("\\*\\s?(\\d{1,3}[а-я]{0,1})");

    final static Pattern patternId2 = Pattern.compile("^(\\d{1,3}[а-я]{0,1})");

    /*
    1863…1870 гг. Я. П.?
    1870 г. Декабря 29…31. Я. П.
    864 г. Апреля 1...15. Я. П.
    1871 г. Марта 3—4. Я. П.
    1874 г. Апрель, вторая половина. Я. П.

    1874 г. Декабрь? Я. П.


    См. в т. 30.

    См. письма за

    <p><em>1883 г.</em></p> //V__I__Alekseevu_181 k \\in  "/Users/petergershkovich/IdeaProjects/Colloquy/samples/volumes/test/tom_63_pisma/OEBPS/Text/0001_1011_2004.xhtml"


    <p><em>1884 г.</em></p>




     */



    /*
    * 92а. А. А. Берсу. //"/Users/petergershkovich/IdeaProjects/Colloquy/samples/volumes/test/tom_61_pisma/OEBPS/Text/0001_1010_2002.xhtml"
     */


    @Test
    public void parseId() throws Exception
    {

        System.out.println(getId("*92а. А. А. Берсу."));
        System.out.println(getId("26. А. А. Толстой."));
        System.out.println(getId("* 174. Д. К. Рихау."));




    }

    private static String getId(String filteredString)
    {

        String id = "";
        Matcher m = patternId.matcher(filteredString);

        if (m.find())
        {
            if (StringUtils.isNotEmpty(m.group(1)))
            {
                id = m.group(1);
            }

        }

        if (StringUtils.isEmpty(id))
        {

            m = patternId2.matcher(filteredString);

            if (m.find())
            {
                if (StringUtils.isNotEmpty(m.group(1)))
                {
                    id = m.group(1);
                }

            }

        }


        return id;

    }

}
