/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2016 Chaosdorf e.V.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

package de.chaosdorf.meteroid;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Build;
import android.text.Editable;
import android.text.Selection;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.URLUtil;

import de.chaosdorf.meteroid.databinding.ActivitySetHostnameBinding;
import de.chaosdorf.meteroid.util.Config;
import de.chaosdorf.meteroid.util.Connection;
import de.chaosdorf.meteroid.util.Utility;

public class SetHostname extends Activity
{
	private Activity activity = null;
	private Config config;
	private Connection connection;
	private ActivitySetHostnameBinding binding;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		activity = this;
		binding = DataBindingUtil.setContentView(this, R.layout.activity_set_hostname);

		config = Config.getInstance(getApplicationContext());

		if (binding.hostname != null)
		{
			if (config.hostname != null)
			{
				binding.hostname.setText(config.hostname);
			}
			final Editable editTextHostname = binding.hostname.getText();
			if (editTextHostname != null)
			{
				Selection.setSelection(editTextHostname, editTextHostname.length());
			}
		}

		binding.buttonSave.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				saveHostname();
			}
		});

		ActionBar actionBar = getActionBar();
		if(actionBar != null)
		{
			binding.buttonSave.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_save:
				saveHostname();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.settings, menu);
		
		// the delete item doesn't make sense here
		final MenuItem deleteItem = menu.findItem(R.id.action_delete);
		if(deleteItem != null)
		{
			deleteItem.setVisible(false);
			deleteItem.setEnabled(false);
		}
		
		return true;
	}

	public void saveHostname()
	{
		final Editable editTextHostname = binding.hostname.getText();
		if (editTextHostname == null)
		{
			Utility.displayToastMessage(activity, getResources().getString(R.string.set_hostname_empty));
			return;
		}
		String newHostname = editTextHostname.toString();
		if (newHostname.equals("http://") || newHostname.equals("https://"))
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
		// TODO: Do this properly.
		final String url = newHostname;
		if(URLUtil.isHttpUrl(url)) {
			new AlertDialog.Builder(this)
				.setMessage(R.string.set_hostname_continue_http)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int id)
					{
						saveAndExit(url);
					}
				})
				.setNegativeButton(android.R.string.cancel, null) // Do nothing on click.
				.create().show();
		} else {
			saveAndExit(url);
		}
	}
	
	private void saveAndExit(String newHostname) {
		config.hostname = newHostname;
		config.apiVersion = Utility.guessApiVersion(newHostname);
		config.save();
		connection = Connection.getInstance(config);
		connection.reset();
		Utility.startActivity(activity, PickUsername.class);
	}
}
