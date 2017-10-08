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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.Date;

import de.chaosdorf.meteroid.longrunningio.LongRunningIOCallback;
import de.chaosdorf.meteroid.longrunningio.LongRunningIORequest;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOTask;
import de.chaosdorf.meteroid.model.User;
import de.chaosdorf.meteroid.util.Utility;
import de.chaosdorf.meteroid.MeteroidNetworkActivity;

public class UserSettings extends MeteroidNetworkActivity
{
	private TextView usernameText;
	private TextView emailText;
	private TextView balanceText;
	private CheckBox activeCheck;
	private CheckBox auditCheck;
	private CheckBox redirectCheck;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_user_settings);

		usernameText = (TextView) findViewById(R.id.username);
		emailText = (TextView) findViewById(R.id.email);
		balanceText = (TextView) findViewById(R.id.balance);
		activeCheck = (CheckBox) findViewById(R.id.active);
		auditCheck = (CheckBox) findViewById(R.id.audit);
		redirectCheck = (CheckBox) findViewById(R.id.redirect);

		final ImageButton backButton = (ImageButton) findViewById(R.id.button_back);
		backButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				goBack();
			}
		});
		
		final ImageButton deleteButton = (ImageButton) findViewById(R.id.button_delete);
		deleteButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				deleteUser();
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
				deleteButton.setVisibility(View.GONE);
				saveButton.setVisibility(View.GONE);
			}
		}
		
		makeReadOnly();
		final UserSettings userSettings = this;
		new LongRunningIORequest<User>(new LongRunningIOCallback<User>() {
			@Override
			public void displayErrorMessage(LongRunningIOTask task, String message)
			{
				userSettings.displayErrorMessage(task, message);
			}
			
			@Override
			public void processIOResult(LongRunningIOTask task, User user)
			{
				usernameText.setText(user.getName());
				emailText.setText(user.getEmail());
				balanceText.setText(DECIMAL_FORMAT.format(user.getBalance()));
				activeCheck.setChecked(user.getActive());
				auditCheck.setChecked(user.getAudit());
				redirectCheck.setChecked(user.getRedirect());
				makeWritable();
			}
		}, LongRunningIOTask.GET_USER, (userID != 0)? api.getUser(userID): api.getUserDefaults());
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				goBack();
				break;
			case R.id.action_save:
				saveUser();
				break;
			case R.id.action_delete:
				deleteUser();
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
			goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void makeReadOnly()
	{
		usernameText.setEnabled(false);
		emailText.setEnabled(false);
		balanceText.setEnabled(false);
		activeCheck.setEnabled(false);
		auditCheck.setEnabled(false);
		redirectCheck.setEnabled(false);
		setProgressBarIndeterminateVisibility(true);
	}

	private void makeWritable()
	{
		setProgressBarIndeterminateVisibility(false);
		usernameText.setEnabled(true);
		emailText.setEnabled(true);
		balanceText.setEnabled(true);
		activeCheck.setEnabled(true);
		auditCheck.setEnabled(true);
		redirectCheck.setEnabled(true);
	}

	private void goBack()
	{
		if(userID == 0) //new user
		{
			Utility.startActivity(this, PickUsername.class);
		}
		else
		{
			Utility.startActivity(this, BuyDrink.class);
		}
	}
	
	private void deleteUser()
	{
		if(userID == 0) //new user
		{
			new AlertDialog.Builder(this)
				.setMessage(R.string.user_settings_cant_delete_non_existing_user)
				.setPositiveButton(android.R.string.ok, null) // Do nothing on click.
				.create().show();
		}
		else
		{
			final UserSettings userSettings = this;
			new AlertDialog.Builder(this)
				.setMessage(R.string.user_settings_confirm_delete_user)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int id)
					{
						// really delete the user
						makeReadOnly();
						new LongRunningIORequest<Void>(new LongRunningIOCallback<Void>() {
								@Override
								public void displayErrorMessage(LongRunningIOTask task, String message)
								{
									userSettings.displayErrorMessage(task, message);
								}
								
								@Override
								public void processIOResult(LongRunningIOTask task, Void result)
								{
									Utility.resetUsername(userSettings);
									makeWritable();
									Utility.displayToastMessage(userSettings, getResources().getString(R.string.user_settings_deleted_user));
									Utility.startActivity(userSettings, PickUsername.class);
								}
							},
							LongRunningIOTask.DELETE_USER,
							api.deleteUser(userID));
					}
				})
				.setNegativeButton(android.R.string.cancel, null) // Do nothing on click.
				.create().show();
		}
	}

	private void saveUser()
	{
		makeReadOnly();

		final CharSequence username = usernameText.getText();
		if (username == null || username.length() == 0)
		{
			Utility.displayToastMessage(this, getResources().getString(R.string.user_settings_empty_username));
			makeWritable();
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
				balanceValue = ((Number)DECIMAL_FORMAT.parse(balance.toString())).doubleValue();
			}
			catch(ParseException ignored)
			{
				Utility.displayToastMessage(this, getResources().getString(R.string.user_settings_balance_no_double));
				makeWritable();
				return;
			}
		}
		
		boolean activeValue = activeCheck.isChecked();
		boolean auditValue = auditCheck.isChecked();
		boolean redirectValue = redirectCheck.isChecked();

		final User user = new User(userID,
				username.toString(),
				emailValue,
				balanceValue,
				activeValue,
				auditValue,
				redirectValue
		);

		final UserSettings userSettings = this;
		if(userID == 0) //new user
		{
			new LongRunningIORequest<User>(new LongRunningIOCallback<User>() {
					@Override
					public void displayErrorMessage(LongRunningIOTask task, String message)
					{
						userSettings.displayErrorMessage(task, message);
					}
					
					@Override
					public void processIOResult(LongRunningIOTask task, User result)
					{
						Utility.startActivity(userSettings, PickUsername.class);
					}
				},
				LongRunningIOTask.ADD_USER,
				api.createUser(user)
			);
		}
		else
		{
			new LongRunningIORequest<Void>(new LongRunningIOCallback<Void>() {
					@Override
					public void displayErrorMessage(LongRunningIOTask task, String message)
					{
						userSettings.displayErrorMessage(task, message);
					}
					
					@Override
					public void processIOResult(LongRunningIOTask task, Void result)
					{
						Utility.startActivity(userSettings, BuyDrink.class);
					}
				},
				LongRunningIOTask.EDIT_USER,
				api.editUser(user.getId(), user)
			);
		}
	}

	public void displayErrorMessage(final LongRunningIOTask task, final String message)
	{
		makeWritable();
		Utility.displayToastMessage(this, message);
	}
}
