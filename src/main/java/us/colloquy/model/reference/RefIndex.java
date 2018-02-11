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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter Gershkovich on 6/18/17.
 */
public class RefIndex
{
    private String volume;

    private List<String> letters = new ArrayList<>();

    public String getVolume()
    {
        return volume;
    }

    public void setVolume(String volume)
    {
        this.volume = volume;
    }

    public List<String> getLetters()
    {
        return letters;
    }

    public void setLetters(List<String> letters)
    {
        this.letters = letters;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        for (String letter: letters)
        {
            if (sb.length() > 0)
            {
                sb.append(", ");
            }
            sb.append(letter);
        }

        return "RefIndex{" +
                "volume='" + volume + '\'' +
                ", letters=" + sb.toString() +
                '}';
    }
}
