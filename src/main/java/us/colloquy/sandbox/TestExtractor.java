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


import com.fasterxml.jackson.databind.ObjectWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.xml.XMLParser;
import org.apache.tika.sax.*;
import org.apache.tika.sax.xpath.Matcher;
import org.apache.tika.sax.xpath.MatchingContentHandler;
import org.apache.tika.sax.xpath.XPathParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import us.colloquy.model.Letter;
import us.colloquy.model.Person;
import us.colloquy.util.RussianDate;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter Gershkovich on 12/1/15.
 */
public class TestExtractor
{
    @Test
    public void extract()
    {
        final List<String> chunks = new ArrayList<>();
        chunks.add("");

        ContentHandler handler = new MyHandler()
        {
            @Override
            public void characters(char[] ch, int start, int length)
            {
                String lastChunk = chunks.get(chunks.size() - 1);

                String thisStr = new String(ch, start, length);


                chunks.add(thisStr);

            }

            @Override
            public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException
            {

                if ("div".equalsIgnoreCase(name))
                {
                    System.out.println("div ended");

                }
            }

            @Override
            public void endElement(String uri, String localName, String name) throws SAXException
            {

                if ("div".equalsIgnoreCase(name))
                {
                    System.out.println("div ended");
                }
            }
        };

        Parser parser = new XMLParser();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        File file = new File("temp/OEBPS/Text/0001_1006_2002.xhtml");

        try (InputStream stream = new ByteArrayInputStream(FileUtils.readFileToByteArray(file)))
        {

            parser.parse(stream, handler, metadata, context);

        } catch (Throwable e)
        {
            e.printStackTrace();
        }

        for (String ch : chunks)
        {
            System.out.println(ch);
        }

    }

    @Test
    public void getContent() throws TikaException, SAXException, IOException
    {

        File file = new File("temp/OEBPS/Text/0001_1006_2002.xhtml");
        InputStream input = new ByteArrayInputStream(FileUtils.readFileToByteArray(file));
        ContentHandler text = new BodyContentHandler();//<co id="html.text.co"/>
        LinkContentHandler links = new LinkContentHandler();//<co id="html.link.co"/>

        ContentHandler handler = new TeeContentHandler(links, text);//<co id="html.merge"/>
        Metadata metadata = new Metadata();
        Parser parser = new XMLParser();
        ParseContext context = new ParseContext();
        parser.parse(input, handler, metadata, context);//<co id="html.parse"/>

        listAvailableMetaDataFields(metadata);

        System.out.println("Title: " + metadata.get(Metadata.TITLE));
        // System.out.println("Body: " + text.toString());

        String[] contentArray = text.toString().split("\n");

        for (String line : contentArray)
        {
            System.out.println(line);

        }
        // System.out.println("Links: " + links.getLinks());

    }


    private void listAvailableMetaDataFields(final Metadata metadata)
    {
        for (int i = 0; i < metadata.names().length; i++)
        {
            String name = metadata.names()[i];
            System.out.println(name + " : " + metadata.get(name));
        }
    }

    @Test
    public void parseOnePartToHTML() throws IOException, SAXException, TikaException
    {
        // Only get things under html -> body -> div (class=header)
        XPathParser xhtmlParser = new XPathParser("", XHTMLContentHandler.XHTML);
        Matcher divContentMatcher = xhtmlParser.parse("*");
        ContentHandler handler = new MatchingContentHandler(
                new ToXMLContentHandler(), divContentMatcher);

        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();

        File file = new File("temp/OEBPS/Text/0001_1006_2002.xhtml");


        try (InputStream stream = new ByteArrayInputStream(FileUtils.readFileToByteArray(file)))
        {
            parser.parse(stream, handler, metadata);
            System.out.println(handler.toString());
        }
    }

    @Test
    public void processXml() throws IOException, SAXException, TikaException
    {

        //detecting the file type
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputstream = new FileInputStream(new File("temp/OEBPS/Text/0001_1006_2002.xhtml"));
        ParseContext pcontext = new ParseContext();

        //Xml parser
        XMLParser xmlparser = new XMLParser();
        xmlparser.parse(inputstream, handler, metadata, pcontext);
        System.out.println("Contents of the document:" + handler.toString());
        System.out.println("Metadata of the document:");
        String[] metadataNames = metadata.names();

        for (String name : metadataNames)
        {
            System.out.println(name + ": " + metadata.get(name));
        }
    }

    @Test
    public void useJsoup()
    {

        String homeDir = System.getProperty("user.home");

        System.out.println(homeDir);

        //JSOUP API allows to extract all  elements of letters in files

       // File input = new File("samples/OEBPS/Text/0001_1006_2001.xhtml");

        File input = new File("samples/pisma-1904/OEBPS/Text/0001_1006_2002.html");

        try
        {
            Document doc = Jsoup.parse(input, "UTF-8");

            List<Letter> letters = new ArrayList<>(); //our model contains only a subset of fields

            String previousYear = "";

            for (Element element : doc.getElementsByClass("section"))
            {
                Letter letter = new Letter();

                StringBuilder content = new StringBuilder();

                for (Element child : element.children())
                {

                    for (Attribute att : child.attributes())
                    {
                        System.out.println(att.getKey() + " " + att.getValue());
                    }

                    if ("center".equalsIgnoreCase(child.className()))
                    {
                        String toWhom = child.getElementsByTag("strong").text();

                        if (StringUtils.isEmpty(toWhom))
                        {
                            toWhom = child.text();
                            // System.out.println(toWhom);
                        }

                        String[] toWhomArray = toWhom.split("(\\sи\\s)|(,)");

                        for (String to : toWhomArray)
                        {
                            RussianDate.parseToWhom(letter, to);  //here we need to recognize a russian name and store that but for now we store the content
                        }

                        //check if there is anything else here and find date and place - it will be replaced if exists below

                        String entireText = child.text();

                        String tail = entireText.replace(toWhom, "");

                        if (StringUtils.isNotEmpty(tail))
                        {
                            RussianDate.parseDateAndPlace(letter, tail, previousYear);  //a parser that figures out date and place if they are present
                        }

                        // System.out.println("two whom\t " +  child.getElementsByTag("strong").text() );

                    } else if ("Data".equalsIgnoreCase(child.className()))
                    {

                        if (child.getElementsByTag("em") != null && StringUtils.isNotEmpty(child.getElementsByTag("em").text()))
                        {
                            RussianDate.parseDateAndPlace(letter, child.getElementsByTag("em").text(), previousYear);  //most often date and place are enclosed in em tag

                            if (letter.getDate() != null)
                            {
                                LocalDate localDate = letter.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                int year = localDate.getYear();
                                previousYear = year + "";
                            }
                        }

                        // System.out.println("when and where\t " + child.getElementsByTag("em").text());


                    } else if ("petit".equalsIgnoreCase(child.className()) || "Textpetit_otstup".equalsIgnoreCase(child.className()))
                    {
                        letter.getNotes().add(child.text());

                    } else
                    {
                        //System.out.println(child.text() );
                        content.append(child.text()).append("\n");

                    }

//                  System.out.println(child.tag() + "\n" );
//                  System.out.println(child.outerHtml() + "\n" + child.text());
                }

                letter.setContent(content.toString());
                letters.add(letter);
            }


            ObjectWriter ow = new com.fasterxml.jackson.databind.ObjectMapper().writer().withDefaultPrettyPrinter();

            for (Letter letter : letters)
            {
//                if (letter.getDate() == null)
//                {
                for (Person person : letter.getTo())
                {
//                        if (StringUtils.isNotEmpty(person.getLastName()))
//                        {
                    String json = ow.writeValueAsString(letter);

                    System.out.println(json);
//                        }
                }
                //}

            }


        } catch (IOException e)
        {
            e.printStackTrace();
        }


    }


}
