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

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Date;

import de.chaosdorf.meteroid.controller.UserController;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOPost;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOPatch;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOGet;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOTask;
import de.chaosdorf.meteroid.model.User;
import de.chaosdorf.meteroid.util.Utility;
import de.chaosdorf.meteroid.MeteroidNetworkActivity;

public class UserSettings extends MeteroidNetworkActivity
{
	private final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

	private Activity activity = null;
	private TextView usernameText;
	private TextView emailText;
	private TextView balanceText;
	private SharedPreferences prefs;
	private int userID;
	private String hostname = null;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		activity = this;
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_user_settings);

		usernameText = (TextView) findViewById(R.id.username);
		emailText = (TextView) findViewById(R.id.email);
		balanceText = (TextView) findViewById(R.id.balance);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		userID = prefs.getInt("userid", 0);
		hostname = prefs.getString("hostname", null);

		final ImageButton backButton = (ImageButton) findViewById(R.id.button_back);
		backButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				Utility.startActivity(activity, PickUsername.class);
			}
		});

		final ImageButton saveButton = (ImageButton) findViewById(R.id.button_save);
		saveButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				saveUser();
			}
		});

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			ActionBar actionBar = getActionBar();
			if(actionBar != null)
			{
				actionBar.setDisplayHomeAsUpEnabled(true);
				backButton.setVisibility(View.GONE);
				saveButton.setVisibility(View.GONE);
			}
		}

		if(userID != 0) //existing user
		{
			makeReadOnly();
			new LongRunningIOGet(this, LongRunningIOTask.GET_USER, hostname + "users/" + userID + ".json");
		}

	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				Utility.resetUsername(this);
				Utility.startActivity(this, PickUsername.class);
				break;
			case R.id.action_save:
				saveUser();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(final int keyCode, @NotNull final KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			Utility.resetUsername(this);
			Utility.startActivity(this, MainActivity.class);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void makeReadOnly()
	{
		usernameText.setEnabled(false);
		emailText.setEnabled(false);
		balanceText.setEnabled(false);
		setProgressBarIndeterminateVisibility(true);
	}

	private void makeWritable()
	{
		setProgressBarIndeterminateVisibility(false);
		usernameText.setEnabled(true);
		emailText.setEnabled(true);
		balanceText.setEnabled(true);
	}

	private void saveUser()
	{
		makeReadOnly();

		final CharSequence username = usernameText.getText();
		if (username == null || username.length() == 0)
		{
			Utility.displayToastMessage(this, getResources().getString(R.string.user_settings_empty_username));
			return;
		}

		final CharSequence email = emailText.getText();
		String emailValue = "";
		if (email != null && email.length() > 0)
		{
			emailValue = email.toString();
		}

		double balanceValue = 0;
		final CharSequence balance = balanceText.getText();
		if (balance != null && balance.length() > 0)
		{
			try
			{
				balanceValue = Double.parseDouble(balance.toString());
			}
			catch (NumberFormatException ignored)
			{
				Utility.displayToastMessage(this, getResources().getString(R.string.user_settings_balance_no_double));
				return;
			}
		}

		final User user = new User(userID,
				username.toString(),
				emailValue,
				balanceValue,
				new Date(),
				new Date()
		);

		final String hostname = prefs.getString("hostname", null);

		if(userID == 0) //new user
		{
			new LongRunningIOPost(
				this,
				LongRunningIOTask.ADD_USER,
				hostname + "users.json",
				UserController.userToJSONPostParams(user)
			);
		}
		else
		{
			new LongRunningIOPatch(
				this,
				LongRunningIOTask.EDIT_USER,
				hostname + "users/" + user.getId() + ".json",
				UserController.userToJSONPostParams(user)
			);
		}
	}

	@Override
	public void displayErrorMessage(final LongRunningIOTask task, final String message)
	{
		makeWritable();
		Utility.displayToastMessage(this, message);
	}

	@Override
	public void processIOResult(final LongRunningIOTask task, final String json)
	{
		final UserSettings usersettings = this;
		if (json != null)
		{
			switch(task)
			{
				case ADD_USER:
					Utility.startActivity(usersettings, PickUsername.class);
					break;
				case EDIT_USER:
					Utility.startActivity(usersettings, BuyDrink.class);
					break;
				case GET_USER:
					User user = UserController.parseUserFromJSON(json);
					usernameText.setText(user.getName());
					emailText.setText(user.getEmail());
					balanceText.setText(DECIMAL_FORMAT.format(user.getBalance()));
					makeWritable();
					break;
			}
		}
	}
}
