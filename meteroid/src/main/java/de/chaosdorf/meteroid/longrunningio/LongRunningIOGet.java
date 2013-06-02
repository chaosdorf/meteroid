package de.chaosdorf.meteroid.longrunningio;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class LongRunningIOGet extends AsyncTask<Void, Void, String>
{
	private final LongRunningIOCallback callback;
	private final LongRunningIOTask id;
	private final String url;

	public LongRunningIOGet(final LongRunningIOCallback callback, final LongRunningIOTask id, final String url)
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
		try
		{
			HttpResponse response = httpClient.execute(httpGet, localContext);
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity, HTTP.UTF_8);
		}
		catch (Exception e)
		{
			callback.displayErrorMessage(e.getLocalizedMessage());
			return null;
		}
	}

	@Override
	protected void onPostExecute(String result)
	{
		callback.processIOResult(id, result);
	}
}
