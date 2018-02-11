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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter Gershkovich on 6/18/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToWhom
{

    private String originalEntry;

    private List<Person> personList = new ArrayList<>();

    private List<RefIndex> refIndexList = new ArrayList<>();

    public List<Person> getPersonList()
    {
        return personList;
    }

    public void setPersonList(List<Person> personList)
    {
        this.personList = personList;
    }

    public List<RefIndex> getRefIndexList()
    {
        return refIndexList;
    }

    public void setRefIndexList(List<RefIndex> refIndexList)
    {
        this.refIndexList = refIndexList;
    }

    public String getOriginalEntry()
    {
        return originalEntry;
    }

    public void setOriginalEntry(String originalEntry)
    {
        this.originalEntry = originalEntry;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        for (Person person: personList)
        {
            if (sb.length() > 0)
            {
                sb.append(", ");
            }

            sb.append(person);
        }

        return "ToWhom{ originalEntry=" + originalEntry +
                "\npersonList=" + personList +
                ", refIndexList=" + refIndexList +
                '}';
    }
}
