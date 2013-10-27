package de.chaosdorf.meteroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Selection;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;

import de.chaosdorf.meteroid.util.Utility;

public class SetHostname extends Activity
{
	private Activity activity = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		activity = this;
		setContentView(R.layout.activity_set_hostname);

		final EditText editText = (EditText) findViewById(R.id.hostname);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final String hostname = prefs.getString("hostname", null);

		if (editText != null)
		{
			if (hostname != null)
			{
				editText.setText(hostname);
			}
			final Editable editTextHostname = editText.getText();
			if (editTextHostname != null)
			{
				Selection.setSelection(editTextHostname, editTextHostname.length());
			}
		}

		final Button saveButton = (Button) findViewById(R.id.save_button);
		saveButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				if (editText == null)
				{
					Utility.displayToastMessage(activity, getResources().getString(R.string.set_hostname_empty));
					return;
				}
				final Editable editTextHostname = editText.getText();
				if (editTextHostname == null)
				{
					Utility.displayToastMessage(activity, getResources().getString(R.string.set_hostname_empty));
					return;
				}
				String newHostname = editTextHostname.toString();
				if (newHostname.equals("http://"))
				{
					Utility.displayToastMessage(activity, getResources().getString(R.string.set_hostname_empty));
					return;
				}
				if (!newHostname.endsWith("/"))
				{
					newHostname += "/";
				}
				if (!(URLUtil.isHttpUrl(newHostname) || URLUtil.isHttpsUrl(newHostname)))
				{
					Utility.displayToastMessage(activity, getResources().getString(R.string.set_hostname_invalid));
					return;
				}
				prefs.edit().putString("hostname", newHostname).apply();
				Intent intent = new Intent(view.getContext(), MainActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}
}
