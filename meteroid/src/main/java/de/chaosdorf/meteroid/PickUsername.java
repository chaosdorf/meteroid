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
import android.content.Context;
import android.os.Bundle;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

import de.chaosdorf.meteroid.longrunningio.LongRunningIOCallback;
import de.chaosdorf.meteroid.longrunningio.LongRunningIORequest;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOTask;
import de.chaosdorf.meteroid.model.User;
import de.chaosdorf.meteroid.util.MenuUtility;
import de.chaosdorf.meteroid.util.Utility;
import de.chaosdorf.meteroid.MeteroidNetworkActivity;

public class PickUsername extends MeteroidNetworkActivity implements AdapterView.OnItemClickListener, LongRunningIOCallback<List<User>>
{
	private static final int NEW_USER_ID = -1;

	private ProgressBar progressBar = null;
	private GridView gridView = null;
	private boolean multiUserMode = false;
	private boolean editHostnameOnBackButton = false;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_username);

		progressBar = (ProgressBar) findViewById(R.id.progress_bar);
		gridView = (GridView) findViewById(R.id.grid_view);

		multiUserMode = prefs.getBoolean("multi_user_mode", false);

		final ImageButton backButton = (ImageButton) findViewById(R.id.button_back);
		backButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				Utility.startActivity(activity, SetHostname.class);
			}
		});

		final ImageButton reloadButton = (ImageButton) findViewById(R.id.button_reload);
		reloadButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				Utility.startActivity(activity, PickUsername.class);
			}
		});

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			reloadButton.setVisibility(View.GONE);
			backButton.setVisibility(View.GONE);
		}

		new LongRunningIORequest<List<User>>(this, LongRunningIOTask.GET_USERS, api.listUsers());
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.pickusername, menu);
		MenuUtility.setChecked(menu, R.id.multi_user_mode, multiUserMode);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				Utility.startActivity(this, SetHostname.class);
				break;
			case R.id.action_reload:
				Utility.startActivity(this, PickUsername.class);
				break;
			case R.id.action_add:
				Utility.startActivity(this, UserSettings.class);
				break;
			case R.id.edit_hostname:
				Utility.startActivity(this, SetHostname.class);
				break;
			case R.id.multi_user_mode:
				multiUserMode = MenuUtility.onClickMultiUserMode(this, item);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(final int keyCode, @NotNull final KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (editHostnameOnBackButton)
			{
				Utility.startActivity(this, SetHostname.class);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onDestroy()
	{
		if (gridView != null)
		{
			gridView.setAdapter(null);
		}
		super.onDestroy();
	}

	@Override
	public void displayErrorMessage(final LongRunningIOTask task, final String message)
	{
		Utility.displayToastMessage(this, message);
		final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.pick_username_error);
		linearLayout.setVisibility(View.VISIBLE);
		gridView.setVisibility(View.GONE);
		editHostnameOnBackButton = true;
		progressBar.setVisibility(View.GONE);
	}

	@Override
	public void processIOResult(final LongRunningIOTask task, final List<User> result)
	{
		if (task == LongRunningIOTask.GET_USERS)
		{
			final List<User> itemList = result;
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			{
				itemList.add(new User(NEW_USER_ID, getResources().getString(R.string.pick_username_new_user), "", 0, true));
			}
			final UserAdapter userAdapter = new UserAdapter(itemList);

			gridView.setAdapter(userAdapter);
			gridView.setOnItemClickListener(this);
			progressBar.setVisibility(View.GONE);
			gridView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> adapterView, final View view, final int index, final long l)
	{
		final User user = (User) gridView.getItemAtPosition(index);
		if (user != null && user.getName() != null)
		{
			if (user.getId() == NEW_USER_ID)
			{
				Utility.startActivity(this, UserSettings.class);
			}
			else
			{
				prefs.edit().putInt("userid", user.getId()).apply();
				Utility.startActivity(this, BuyDrink.class);
			}
		}
	}

	public class UserAdapter extends ArrayAdapter<User>
	{
		private final List<User> userList;
		private final LayoutInflater inflater;

		UserAdapter(final List<User> userList)
		{
			super(activity, R.layout.activity_pick_username, userList);
			this.userList = userList;
			this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public View getView(final int position, final View convertView, final ViewGroup parent)
		{
			View view = convertView;
			if (view == null)
			{
				view = inflater.inflate(R.layout.activity_pick_username_item, parent, false);
			}
			if (view == null)
			{
				return null;
			}

			final User user = userList.get(position);
			final ImageView icon = (ImageView) view.findViewById(R.id.icon);
			final TextView label = (TextView) view.findViewById(R.id.label);

			Utility.loadUserImage(activity, icon, user);
			icon.setContentDescription(user.getName());
			label.setText(user.getName());

			if (user.getId() == NEW_USER_ID)
			{
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				{
					icon.setImageDrawable(getDrawable(R.drawable.add_user));
				}
				else
				{
					// This is only called on KitKat and lower.
					icon.setImageDrawable(getResources().getDrawable(R.drawable.add_user));
				}
			}

			return view;
		}
	}
}
