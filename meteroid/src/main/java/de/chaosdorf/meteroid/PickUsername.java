package de.chaosdorf.meteroid;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class PickUsername extends Activity
{
	private Context context = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.activity_pick_username);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final String hostname = prefs.getString("hostname", null);

		new LongRunningGetIO(context, hostname + "users.json").execute();

		/*
		final Button saveButton = (Button) findViewById(R.id.save_button);
		saveButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				prefs.edit().putString("username", null).apply();
				Intent intent = new Intent(view.getContext(), MainActivity.class);
				startActivity(intent);
				finish();
			}
		});
		*/
	}
}
