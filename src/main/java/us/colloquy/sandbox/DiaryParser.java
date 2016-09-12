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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by Peter Gershkovich on 12/5/15.
 */
public class DiaryParser
{
    @Test
    public void useJsoup()
    {
        File input = new File(System.getProperty("user.home") + "/IdeaProjects/ElasticTest/temp/dnevnik_1862(1)/OEBPS/Text/0001_1006_2001.xhtml");

        try
        {
            Document doc = Jsoup.parse(input, "UTF-8");

            for (Element element: doc.getElementsByClass("section") )
            {
                for ( Element child: element.children())
                {
                    for (Attribute att: child.attributes())
                    {
                        System.out.println(att.getKey() + " " + att.getValue());
                    }
                    System.out.println(child.tag() + "\n" );
                    System.out.println(child.outerHtml() + "\n" + child.text());
                }
            }


        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
