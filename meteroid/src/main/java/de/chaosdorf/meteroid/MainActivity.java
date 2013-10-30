package de.chaosdorf.meteroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import de.chaosdorf.meteroid.util.Utility;

public class MainActivity extends Activity
{
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final String hostname = prefs.getString("hostname", null);
		final int userID = prefs.getInt("userid", 0);

		if (hostname == null)
		{
			// Set hostname if not done yet
			Utility.startActivity(this, SetHostname.class);
		}
		else if (userID == 0)
		{
			// Pick username if not done yet
			Utility.startActivity(this, PickUsername.class);
		}
		else
		{
			// Ready to buy some drinks
			Utility.startActivity(this, BuyDrink.class);
		}
	}
}
