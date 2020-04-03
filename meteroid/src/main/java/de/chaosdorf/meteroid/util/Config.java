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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Config
{
    public static final int NO_USER_ID = -1;
    private static final String TAG = "Config";
    
    private SharedPreferences prefs;
    public String hostname = null;
    public String apiVersion = null;
    public boolean multiUserMode = false;
    public boolean useGridView = false;
    public int userID = NO_USER_ID;
    private int version = 0;
    
    private static volatile Config instance;
    
    private Config(Context context)
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        hostname = prefs.getString("hostname", hostname);
        apiVersion = prefs.getString("api_version", apiVersion);
        multiUserMode = prefs.getBoolean("multi_user_mode", multiUserMode);
        useGridView = prefs.getBoolean("use_grid_view", useGridView);
        userID = prefs.getInt("userid", userID);
        version = prefs.getInt("config_version", version);
        migrate();
    }
    
    public static Config getInstance(Context context)
    {
        if(instance == null)
        {
            instance = new Config(context);
        }
        return instance;
    }
    
    public void save()
    {
        Log.d(TAG, "Saving config.");
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("hostname", this.hostname);
        edit.putString("api_version", this.apiVersion);
        edit.putBoolean("multi_user_mode", this.multiUserMode);
        edit.putBoolean("use_grid_view", this.useGridView);
        edit.putInt("userid", this.userID);
        edit.putInt("config_version", this.version);
        edit.apply();
    }
    
    private void migrate()
    {
        if(version == 0)
        {
            Log.d(TAG, "Migrating config from v0 to v1: Adding version.");
            version = 1;
            save();
        }
        if(version == 1)
        {
            Log.d(TAG, "Migrating config from v1 to v2: Setting userID to -1, if none was set.");
            if(userID == 0)
            {
                userID = -1;
            }
            version = 2;
            save();
        }
        if(version == 2)
        {
            Log.d(TAG, "Migrating config from v2 to v3: Guessing apiVersion, if none was set.");
            if(hostname != null)
            {
                if(apiVersion == null)
                {
                    apiVersion = Utility.guessApiVersion(hostname);
                }
            }
            version = 3;
            save();
        }
    }
}
