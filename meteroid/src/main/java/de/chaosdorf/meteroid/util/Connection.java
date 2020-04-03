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

import java.util.List;

import android.util.Log;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import de.chaosdorf.meteroid.longrunningio.LongRunningIOCallback;
import de.chaosdorf.meteroid.longrunningio.LongRunningIORequest;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOTask;
import de.chaosdorf.meteroid.model.User;

public class Connection
{
    private static final String TAG = "Connection";
    private Config config;
    private API api;
    
    private static volatile Connection instance;
    
    private Connection(Config config)
    {
        this.config = config;
        upgradeAPIversion();
        api = initializeRetrofit(config.hostname);
    }
    
    public static Connection getInstance(Config config)
    {
        if(instance == null)
        {
            instance = new Connection(config);
        }
        return instance;
    }
    
    public void reset()
    {
        Log.d(TAG, "Resetting connection...");
        upgradeAPIversion();
        api = initializeRetrofit(config.hostname);
    }
    
    public API getAPI()
    {
        return api;
    }
    
    private void upgradeAPIversion()
    {
        if(config.apiVersion == null)
        {
            Log.w(TAG, "API version is null. This shouldn't happen.");
            config.apiVersion = Utility.guessApiVersion(config.hostname); // TODO: Do this properly.
            config.save();
        }
        if(config.apiVersion.equals("legacy"))
        {
            Log.d(TAG, "Trying to upgrade the API version from 'legacy' to 'v1'...");
            if(config.hostname.contains("api/v1"))
            {
                Log.e(TAG, "API version is configured as 'legacy', but seems like 'v1'. This is most certainly a bug.");
            }
            final String test_hostname = config.hostname + "api/v1/";
            final API test_api = initializeRetrofit(test_hostname);
            new LongRunningIORequest<List<User>>(new LongRunningIOCallback<List<User>>()
            {
                @Override
                public void displayErrorMessage(LongRunningIOTask task, String message)
                {
                    Log.w(TAG, "The server doesn't seem to support 'v1' (or the request failed): "  + message);
                    api = initializeRetrofit(config.hostname);
                }
                
                @Override
                public void processIOResult(LongRunningIOTask task, List<User> users)
                {
                    Log.i(TAG, "The server seems to support the newer API version 'v1'. Upgrading.");
                    config.hostname = test_hostname;
                    config.apiVersion = "v1";
                    config.save();
                    api = test_api;
                }
            }, LongRunningIOTask.GET_USERS, test_api.listUsers());
        }
    }
    
    private API initializeRetrofit(String url)
    {
        Log.d(TAG, "Opening connection to " + url + "...");
        return new Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(API.class);
    }
}
