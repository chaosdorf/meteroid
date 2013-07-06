package de.chaosdorf.meteroid.longrunningio;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
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
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(postData));
        } catch (UnsupportedEncodingException e) {
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
