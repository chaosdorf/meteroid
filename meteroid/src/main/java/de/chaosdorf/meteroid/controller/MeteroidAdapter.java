/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2016 Chaosdorf e.V.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

package de.chaosdorf.meteroid.controller;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.chaosdorf.meteroid.model.MeteroidItem;

public class MeteroidAdapter<T extends MeteroidItem> extends ArrayAdapter<T> implements SectionIndexer {
    private final List<T> objects;
    private final List<String> sections;
    private final Map<String, Integer> indexForSection;
    
    public MeteroidAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
        this.objects = objects;
        indexForSection = new HashMap<String, Integer>();
        sections = new ArrayList<String>();
        computeSections();
    }
    
    private void computeSections() {
        for(int i = 0; i < objects.size(); i++) {
            MeteroidItem item = objects.get(i);
            if(!item.getActive()) {
                if(!indexForSection.containsKey("-")) {
                   indexForSection.put("-", i);
                    sections.add("-");
                    break;
                }
            } else {
                String firstCharOfName = getFirstChar(item);
                if(!indexForSection.containsKey(firstCharOfName)) {
                    indexForSection.put(firstCharOfName, i);
                    sections.add(firstCharOfName);
                }
            }
        }
    }
    
    private String getFirstChar(MeteroidItem item) {
        return item.getName().substring(0, 1).toUpperCase();
    }
    
    public String[] getSections() {
        return sections.toArray(new String[0]);
    }
    
    public int getSectionForPosition(int position) {
        try {
            MeteroidItem item = objects.get(position);
            if(!item.getActive()) {
                return sections.size() - 1; // "-"
            }
            String firstCharOfName = getFirstChar(item);
            return sections.indexOf(firstCharOfName);
        } catch (IndexOutOfBoundsException exc) {
            return 0;
        }
    }
    
    public int getPositionForSection(int sectionIndex) {
        if(sectionIndex >= sections.size()) {
            return objects.size() - 1;
        }
        if(sectionIndex < 0) {
            return 0;
        }
        return indexForSection.get(sections.get(sectionIndex));
    }
}