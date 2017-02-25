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

package us.colloquy.model;

/**
 * Created by Peter Gershkovich on 12/26/15.
 */
public class DocumentPointer implements Comparable<DocumentPointer>
{
    private String uri;
    private String sourse;

    public DocumentPointer(String uri, String sourse)
    {
        this.uri = uri;
        this.sourse = sourse;
    }

    public String getUri()
    {
        return uri;
    }

    public void setUri(String uri)
    {
        this.uri = uri;
    }

    public String getSourse()
    {
        return sourse;
    }

    public void setSourse(String sourse)
    {
        this.sourse = sourse;
    }

    @Override
    public int compareTo(DocumentPointer o)
    {
        return this.getUri().compareTo(o.getUri());
    }

    @Override
    public String toString()
    {
        return sourse + "\t" + uri;
    }
}
