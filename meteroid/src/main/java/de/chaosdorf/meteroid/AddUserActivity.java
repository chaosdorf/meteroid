/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Chaosdorf e.V.
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

import de.chaosdorf.meteroid.controller.UserController;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOCallback;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOPost;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOTask;
import de.chaosdorf.meteroid.model.User;
import de.chaosdorf.meteroid.util.Utility;

public class AddUserActivity extends Activity implements LongRunningIOCallback
{
	private Activity activity = null;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		activity = this;
		setContentView(R.layout.activity_add_user);

		final TextView usernameText = (TextView) findViewById(R.id.username);
		final TextView emailText = (TextView) findViewById(R.id.email);
		final TextView balanceText = (TextView) findViewById(R.id.balance);
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		final ImageButton backButton = (ImageButton) findViewById(R.id.button_back);
		backButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				Utility.startActivity(activity, PickUsername.class);
			}
		});

		final ImageButton addButton = (ImageButton) findViewById(R.id.button_add_user);
		addButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				final CharSequence username = usernameText.getText();
				if (username == null || username.length() == 0)
				{
					Utility.displayToastMessage(activity, getResources().getString(R.string.add_user_empty_username));
					return;
				}

				final CharSequence email = emailText.getText();
				String emailValue = "";
				if (email != null && email.length() > 0)
				{
					emailValue = email.toString();
				}

				final CharSequence balance = balanceText.getText();
				if (balance == null || balance.length() == 0)
				{
					Utility.displayToastMessage(activity, getResources().getString(R.string.add_user_empty_balance));
					return;
				}
				double balanceValue;
				try
				{
					balanceValue = Double.parseDouble(balance.toString());
				}
				catch (NumberFormatException ignored)
				{
					Utility.displayToastMessage(activity, getResources().getString(R.string.add_user_empty_balance));
					return;
				}

				final User user = new User(0,
						username.toString(),
						emailValue,
						balanceValue,
						new Date(),
						new Date()
				);

				final String hostname = prefs.getString("hostname", null);
				new LongRunningIOPost(
						AddUserActivity.this,
						LongRunningIOTask.ADD_USER,
						hostname + "users",
						UserController.userToPostParams(user)
				).execute();
			}
		});
	}

	@Override
	public boolean onKeyDown(final int keyCode, @NotNull final KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			Utility.resetUsername(activity);
			Utility.startActivity(activity, MainActivity.class);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void displayErrorMessage(final LongRunningIOTask task, final String message)
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				Utility.displayToastMessage(activity, message);
			}
		});
	}

	@Override
	public void processIOResult(final LongRunningIOTask task, final String json)
	{
		if (task == LongRunningIOTask.ADD_USER && json != null)
		{
			startActivity(new Intent(getApplicationContext(), PickUsername.class));
		}
	}
}
