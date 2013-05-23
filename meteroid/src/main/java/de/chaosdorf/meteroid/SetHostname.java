package de.chaosdorf.meteroid;

import android.app.Activity;
import android.content.Context;
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

public class SetHostname extends Activity
{
	private Context context = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		context = this;
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
			Selection.setSelection(editTextHostname, editTextHostname.length());
		}

		final Button saveButton = (Button) findViewById(R.id.save_button);
		saveButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				String newHostname = editText.getText().toString();
				if (!newHostname.endsWith("/"))
				{
					newHostname += "/";
				}
				if (newHostname.equals("http://"))
				{
					Utility.displayErrorMessage("Please enter a hostname", context);
					return;
				}
				if (!URLUtil.isHttpUrl(newHostname))
				{
					Utility.displayErrorMessage("Invalid hostname entered", context);
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
