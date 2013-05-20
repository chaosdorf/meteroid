package de.chaosdorf.meteroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity
{
	public static void displayErrorMessage(CharSequence message, Context context)
	{
		// display error message
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message).setCancelable(false).setPositiveButton(
				context.getResources().getText(R.string.ok),
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						// do nothing
					}
				});
		builder.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		final SharedPreferences prefs = getPreferences(MODE_PRIVATE);

		final String hostname = prefs.getString("hostname", null);
		final String username = prefs.getString("username", null);

		if (hostname == null)
		{
			setContentView(R.layout.activity_set_hostname);
			final Button saveButton = (Button) findViewById(R.id.save_button);
			saveButton.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					final Editable editTextHostname = ((EditText) findViewById(R.id.hostname)).getText();
					if (editTextHostname != null)
					{
						String newHostname = editTextHostname.toString();
						if (!newHostname.endsWith("/"))
						{
							newHostname += "/";
						}
						if (URLUtil.isValidUrl(newHostname))
						{
							prefs.edit().putString("hostname", newHostname);
							setContentView(R.layout.activity_pick_username);
							return;
						}
					}
					displayErrorMessage("Foobar", getApplicationContext());
				}
			});
		}
		else if (username == null)
		{
			setContentView(R.layout.activity_pick_username);
			new LongRunningGetIO().execute();
		}
		else
		{
			setContentView(R.layout.activity_main);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class LongRunningGetIO extends AsyncTask<Void, Void, String>
	{
		protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException
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

		@Override
		protected String doInBackground(Void... params)
		{
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			HttpGet httpGet = new HttpGet("http://mete.chaosdorf.dn42/users.json");
			String json;
			try
			{
				HttpResponse response = httpClient.execute(httpGet, localContext);
				HttpEntity entity = response.getEntity();
				json = getASCIIContentFromEntity(entity);
			}
			catch (Exception e)
			{
				return e.getLocalizedMessage();
			}
			return json;
		}

		protected void onPostExecute(String results)
		{
			if (results != null)
			{
				TextView et = (TextView) findViewById(R.id.result);
				et.setText(results);
			}
		}
	}
}
