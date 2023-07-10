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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.Date;

import de.chaosdorf.meteroid.databinding.ActivityUserSettingsBinding;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOCallback;
import de.chaosdorf.meteroid.longrunningio.LongRunningIORequest;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOTask;
import de.chaosdorf.meteroid.model.User;
import de.chaosdorf.meteroid.util.Utility;
import de.chaosdorf.meteroid.MeteroidNetworkActivity;

public class UserSettings extends MeteroidNetworkActivity
{
	private User user;
	private ActivityUserSettingsBinding binding;
	private final ObservableBoolean writable = new ObservableBoolean(false);

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_user_settings);
		setSupportActionBar(binding.toolbar);
		binding.setUser(user);
		binding.setDECIMALFORMAT(DECIMAL_FORMAT);
		binding.setWritable(writable);
		
		makeReadOnly();
		final UserSettings userSettings = this;
		new LongRunningIORequest<User>(new LongRunningIOCallback<User>() {
			@Override
			public void displayErrorMessage(LongRunningIOTask task, String message)
			{
				Toast.makeText(
					userSettings,
					R.string.user_settings_failed_fetch_details,
					Toast.LENGTH_LONG
				).show();
				userSettings.finish();
			}
			
			@Override
			public void processIOResult(LongRunningIOTask task, User user)
			{
				userSettings.user = user;
				binding.setUser(user);
				makeWritable();
			}
		}, LongRunningIOTask.GET_USER, (config.userID != config.NO_USER_ID)? connection.getAPI().getUser(config.userID): connection.getAPI().getUserDefaults());
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				finish();
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
		boolean writable = this.writable.get();
		MenuItem saveItem = menu.findItem(R.id.action_save);
		saveItem.setEnabled(writable);
		MenuItem deleteItem = menu.findItem(R.id.action_delete);
		deleteItem.setEnabled(writable);
		deleteItem.setVisible(config.userID != config.NO_USER_ID);
		return true;
	}

	@Override
	public boolean onKeyDown(final int keyCode, @NotNull final KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void makeReadOnly()
	{
		writable.set(false);
		invalidateOptionsMenu();
	}

	private void makeWritable()
	{
		writable.set(true);
		invalidateOptionsMenu();
	}

	private void deleteUser()
	{
		if(config.userID == config.NO_USER_ID) //new user
		{
			new MaterialAlertDialogBuilder(this)
				.setMessage(R.string.user_settings_cant_delete_non_existing_user)
				.setPositiveButton(android.R.string.ok, null) // Do nothing on click.
				.create().show();
		}
		else
		{
			final UserSettings userSettings = this;
			new MaterialAlertDialogBuilder(this)
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
									config.userID = config.NO_USER_ID;
									config.save();
									makeWritable();
									Utility.displayToastMessage(userSettings, getResources().getString(R.string.user_settings_deleted_user));
									Utility.startActivity(userSettings, PickUsername.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
									finish();
								}
							},
							LongRunningIOTask.DELETE_USER,
							connection.getAPI().deleteUser(config.userID));
					}
				})
				.setNegativeButton(android.R.string.cancel, null) // Do nothing on click.
				.create().show();
		}
	}

	private void saveUser()
	{
		makeReadOnly();

		final CharSequence username = binding.username.getText();
		if (username == null || username.length() == 0)
		{
			Utility.displayToastMessage(this, getResources().getString(R.string.user_settings_empty_username));
			makeWritable();
			return;
		}

		double balanceValue = 0;
		final CharSequence balance = binding.balance.getText();
		if (balance != null)
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
		
		user.setBalance(balanceValue);

		final UserSettings userSettings = this;
		if(config.userID == config.NO_USER_ID) //new user
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
						Utility.startActivity(userSettings, PickUsername.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
						finish();
					}
				},
				LongRunningIOTask.ADD_USER,
				connection.getAPI().createUser(user)
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
						Utility.startActivity(userSettings, BuyDrink.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
						finish();
					}
				},
				LongRunningIOTask.EDIT_USER,
				connection.getAPI().editUser(user.getId(), user)
			);
		}
	}

	public void displayErrorMessage(final LongRunningIOTask task, final String message)
	{
		makeWritable();
		Utility.displayToastMessage(this, message);
	}
}
