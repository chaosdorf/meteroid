/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Chaosdorf e.V.
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

package de.chaosdorf.meteroid.longrunningio;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class LongRunningIOPost extends AsyncTask<Void, Void, String>
{
	private final LongRunningIOCallback callback;
	private final LongRunningIOTask id;
	private final String url;
	private final List<BasicNameValuePair> postData;

	public LongRunningIOPost(final LongRunningIOCallback callback, final LongRunningIOTask id, final String url, final List<BasicNameValuePair> postData)
	{
		this.callback = callback;
		this.id = id;
		this.url = url;
		this.postData = postData;
	}

	@Override
	protected String doInBackground(Void... params)
	{
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpPost httpPost = new HttpPost(url);
		try
		{
			httpPost.setEntity(new UrlEncodedFormEntity(postData));
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return null;
		}
		try
		{
			HttpResponse response = httpClient.execute(httpPost, localContext);
			int code = response.getStatusLine().getStatusCode();
			if (code >= 400 && code <= 599)
			{
				callback.displayErrorMessage(id, response.getStatusLine().getReasonPhrase());
				return null;
			}
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity, HTTP.UTF_8);
		}
		catch (Exception e)
		{
			callback.displayErrorMessage(id, e.getLocalizedMessage());
			return null;
		}
	}

	@Override
	protected void onPostExecute(String result)
	{
		callback.processIOResult(id, result);
	}
}
