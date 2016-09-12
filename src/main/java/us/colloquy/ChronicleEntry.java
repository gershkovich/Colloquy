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

package us.colloquy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Peter Gershkovich on 11/26/15.
 */
public class ChronicleEntry
{
    private String event;
    private List<String> eventType = new ArrayList<>();
    private List<String> people = new ArrayList<>();
    private String place;
    private Date eventTime;
    private EventPeriod eventPeriod;

    public String getEvent()
    {
        return event;
    }

    public void setEvent(String event)
    {
        this.event = event;
    }

    public List<String> getEventType()
    {
        return eventType;
    }

    public void setEventType(List<String> eventType)
    {
        this.eventType = eventType;
    }

    public List<String> getPeople()
    {
        return people;
    }

    public void setPeople(List<String> people)
    {
        this.people = people;
    }

    public String getPlace()
    {
        return place;
    }

    public void setPlace(String place)
    {
        this.place = place;
    }

    public Date getEventTime()
    {
        return eventTime;
    }

    public void setEventTime(Date eventTime)
    {
        this.eventTime = eventTime;
    }

    public EventPeriod getEventPeriod()
    {
        return eventPeriod;
    }

    public void setEventPeriod(EventPeriod eventPeriod)
    {
        this.eventPeriod = eventPeriod;
    }
}
