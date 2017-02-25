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

import java.util.Date;

/**
 * Created by Peter Gershkovich on 12/19/15.
 */
public class DateAndPlace
{
    private String day;
    private String month;
    private String year;
    private StringBuilder place = new StringBuilder();
    private boolean approximate;
    private Date date;

    public String getDay()
    {
        return day;
    }

    public void setDay(String day)
    {
        this.day = day;
    }

    public String getMonth()
    {
        return month;
    }

    public void setMonth(String month)
    {
        this.month = month;
    }

    public String getYear()
    {
        return year;
    }

    public void setYear(String year)
    {
        this.year = year;
    }

    public StringBuilder getPlace()
    {
        return place;
    }

    public void setPlace(StringBuilder place)
    {
        this.place = place;
    }

    public boolean isApproximate()
    {
        return approximate;
    }

    public void setApproximate(boolean approximate)
    {
        this.approximate = approximate;
    }

    @Override
    public String toString()
    {
        return day + " " + month + " " + year + "\t" + place;
    }

    public String getDateString()
    {
        return day + " " + month + " " + year;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }
}
