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

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Callback;
import okhttp3.Call;
import okhttp3.Response;

public class LongRunningIOPost
{
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	public LongRunningIOPost(final LongRunningIOCallback callback, final LongRunningIOTask id, final String url, final String postData)
	{
		OkHttpClient client = new OkHttpClient();
		RequestBody reqbody = RequestBody.create(JSON, postData);
		Request req = new Request.Builder().url(url).post(reqbody).build();
		client.newCall(req).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				callback.displayErrorMessage(id, e.getLocalizedMessage());
			}

			@Override
			public void onResponse(Call call, Response resp)
			{
				if(resp.isSuccessful())
				{
					try
					{
						callback.processIOResult(id, resp.body().string());
					}
					catch(IOException e)
					{
						callback.displayErrorMessage(id, e.getLocalizedMessage());
					}
				}
				else
				{
					callback.displayErrorMessage(id, resp.message());
				}
			}
		});
	}
}
