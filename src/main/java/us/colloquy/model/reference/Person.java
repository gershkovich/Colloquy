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

package us.colloquy.model.reference;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Peter Gershkovich on 12/14/15.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Person
{
    private String firstName = "";
    private String lastName= "";
    private String paternalName= "";
    private String title="";
    private String maidenName="";

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getPaternalName()
    {
        return paternalName;
    }

    public void setPaternalName(String paternalName)
    {
        this.paternalName = paternalName;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getMaidenName()
    {
        return maidenName;
    }

    public void setMaidenName(String maidenName)
    {
        this.maidenName = maidenName;
    }

    @Override
    public String toString()
    {

        return lastName + " " + firstName + " " + paternalName;

    }
}
