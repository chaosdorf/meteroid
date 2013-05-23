package de.chaosdorf.meteroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		final SharedPreferences prefs = getPreferences(MODE_PRIVATE);

		final String hostname = prefs.getString("hostname", null);
		final String username = prefs.getString("username", null);

		if (hostname == null)
		{
			Intent intent = new Intent(this, SetHostname.class);
			startActivity(intent);
		}
		else if (username == null)
		{
			Intent intent = new Intent(this, SetHostname.class);
			startActivity(intent);
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
}
