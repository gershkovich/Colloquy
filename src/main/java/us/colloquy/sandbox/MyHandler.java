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
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MyHandler extends DefaultHandler {
    private ContentHandler handler;

    public MyHandler(ContentHandler handler) {
        assert handler != null;

        this.handler = handler;
    }

    protected MyHandler() {
        this(new DefaultHandler());
    }

    protected void setContentHandler(ContentHandler handler) {
        assert handler != null;

        this.handler = handler;
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        try {
            this.handler.startPrefixMapping(prefix, uri);
        } catch (SAXException var4) {
            this.handleException(var4);
        }

    }

    public void endPrefixMapping(String prefix) throws SAXException {
        try {
            this.handler.endPrefixMapping(prefix);
        } catch (SAXException var3) {
            this.handleException(var3);
        }

    }

    public void processingInstruction(String target, String data) throws SAXException {
        try {
            this.handler.processingInstruction(target, data);
        } catch (SAXException var4) {
            this.handleException(var4);
        }

    }

    public void setDocumentLocator(Locator locator) {
        this.handler.setDocumentLocator(locator);
    }

    public void startDocument() throws SAXException {
        try {
            this.handler.startDocument();
        } catch (SAXException var2) {
            this.handleException(var2);
        }

    }

    public void endDocument() throws SAXException {
        try {
            this.handler.endDocument();
        } catch (SAXException var2) {
            this.handleException(var2);
        }

    }

    public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
        try {


            this.handler.startElement(uri, localName, name, atts);
        } catch (SAXException var6) {
            this.handleException(var6);
        }

    }

    public void endElement(String uri, String localName, String name) throws SAXException {
        try {
            this.handler.endElement(uri, localName, name);
        } catch (SAXException var5) {
            this.handleException(var5);
        }

    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            this.handler.characters(ch, start, length);
        } catch (SAXException var5) {
            this.handleException(var5);
        }

    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        try {
            this.handler.ignorableWhitespace(ch, start, length);
        } catch (SAXException var5) {
            this.handleException(var5);
        }

    }

    public void skippedEntity(String name) throws SAXException {
        try {
            this.handler.skippedEntity(name);
        } catch (SAXException var3) {
            this.handleException(var3);
        }

    }

    public String toString() {
        return this.handler.toString();
    }

    protected void handleException(SAXException exception) throws SAXException {
        throw exception;
    }
}