package de.chaosdorf.meteroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import de.chaosdorf.meteroid.interfaces.LongRunningGetIOCallback;
import de.chaosdorf.meteroid.util.LongRunningGetIO;
import de.chaosdorf.meteroid.util.Utility;

public class PickUsername extends Activity implements LongRunningGetIOCallback
{
	private Activity activity = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		activity = this;
		setContentView(R.layout.activity_pick_username);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final String hostname = prefs.getString("hostname", null);

		new LongRunningGetIO(this, hostname + "users.json").execute();

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

	@Override
	public void displayErrorMessage(final String message)
	{
		Utility.displayToastMessage(activity, message);
	}

	@Override
	public void processIOResult(final String json)
	{
		if (json != null)
		{
			Utility.displayToastMessage(activity, "Result: " + json);
		}
	}
}
