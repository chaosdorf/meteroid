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

package de.chaosdorf.meteroid.longrunningio;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Callback;
import okhttp3.Call;
import okhttp3.Response;

import de.chaosdorf.meteroid.MeteroidNetworkActivity;

public class LongRunningIOBase
{
	protected OkHttpClient client;

	public LongRunningIOBase()
	{
		client = new OkHttpClient();
	}

	protected Callback newCallback(final MeteroidNetworkActivity callback, final LongRunningIOTask id)
	{
		return new Callback()
		{
			@Override
			public void onFailure(final Call call, final IOException e)
			{
				callback.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						callback.displayErrorMessage(id, e.getLocalizedMessage());
					}
				});
			}

			@Override
			public void onResponse(final Call call, final Response resp)
			{
				if(resp.isSuccessful())
				{
					try
					{
						final String response = resp.body().string();
						callback.runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								callback.processIOResult(id, response);
							}
						});
					}
					catch(final IOException e)
					{
						callback.runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								callback.displayErrorMessage(id, e.getLocalizedMessage());
							}
						});
					}
				}
				else
				{
					callback.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							callback.displayErrorMessage(id, resp.message());
						}
					});
				}
			}
		};
	}
}
