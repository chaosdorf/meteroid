package de.chaosdorf.meteroid.util;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;

import de.chaosdorf.meteroid.interfaces.LongRunningGetIOCallback;
import de.chaosdorf.meteroid.enums.LongRunningIOTask;

public class LongRunningGetIO extends AsyncTask<Void, Void, String>
{
	private final LongRunningGetIOCallback callback;
	private final LongRunningIOTask id;
	private final String url;

	public LongRunningGetIO(final LongRunningGetIOCallback callback, final LongRunningIOTask id, final String url)
	{
		this.callback = callback;
		this.id = id;
		this.url = url;
	}

	@Override
	protected String doInBackground(Void... params)
	{
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet(url);
		String json;
		try
		{
			HttpResponse response = httpClient.execute(httpGet, localContext);
			HttpEntity entity = response.getEntity();
			json = getASCIIContentFromEntity(entity);
		}
		catch (Exception e)
		{
			callback.displayErrorMessage(e.getLocalizedMessage());
			return null;
		}
		return json;
	}

	@Override
	protected void onPostExecute(String result)
	{
		callback.processIOResult(id, result);
	}

	private String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException
	{
		InputStream in = entity.getContent();
		StringBuilder out = new StringBuilder();
		int n = 1;
		while (n > 0)
		{
			byte[] b = new byte[4096];
			n = in.read(b);
			if (n > 0)
			{
				out.append(new String(b, 0, n));
			}
		}
		return out.toString();
	}
}
