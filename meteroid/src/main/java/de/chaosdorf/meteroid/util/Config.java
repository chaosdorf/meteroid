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

package de.chaosdorf.meteroid.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Config
{
    private SharedPreferences prefs;
    public String hostname;
    public boolean multiUserMode;
    public boolean useGridView;
    public int userID;
    
    public Config(Activity activity)
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        hostname = prefs.getString("hostname", null);
        multiUserMode = prefs.getBoolean("multi_user_mode", false);
        useGridView = prefs.getBoolean("use_grid_view", false);
        userID = prefs.getInt("userid", 0);
    }
    
    public void save()
    {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("hostname", this.hostname);
        edit.putBoolean("multi_user_mode", this.multiUserMode);
        edit.putBoolean("use_grid_view", this.useGridView);
        edit.putInt("userid", this.userID);
        edit.apply();
    }
}
