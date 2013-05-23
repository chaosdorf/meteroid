package de.chaosdorf.meteroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
		final Editable editTextHostname = (editText != null) ? editText.getText() : null;

		final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		final String hostname = prefs.getString("hostname", null);

		if (hostname != null && editText != null)
		{
			editText.setText(hostname);
			if (editTextHostname != null)
			{
				Selection.setSelection(editTextHostname, hostname.length());
			}
		}

		final Button saveButton = (Button) findViewById(R.id.save_button);
		saveButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				if (editTextHostname == null)
				{
					Utility.displayErrorMessage("No hostname entered", context);
					return;
				}
				String newHostname = editTextHostname.toString();
				if (!newHostname.endsWith("/"))
				{
					newHostname += "/";
				}
				if (!URLUtil.isValidUrl(newHostname))
				{
					Utility.displayErrorMessage("Invalid hostname entered", context);
					return;
				}
				prefs.edit().putString("hostname", newHostname);
				Intent intent = new Intent(view.getContext(), MainActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}
}
