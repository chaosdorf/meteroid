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
import android.databinding.DataBindingUtil;
import android.content.Context;
import android.os.Bundle;
import android.os.Build;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import android.support.v4.widget.SwipeRefreshLayout;

import com.shamanland.fab.FloatingActionButton;
import com.shamanland.fab.ShowHideOnScroll;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

import de.chaosdorf.meteroid.controller.MeteroidAdapter;
import de.chaosdorf.meteroid.databinding.ActivityPickUsernameBinding;
import de.chaosdorf.meteroid.databinding.ActivityPickUsernameItemBinding;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOCallback;
import de.chaosdorf.meteroid.longrunningio.LongRunningIORequest;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOTask;
import de.chaosdorf.meteroid.model.User;
import de.chaosdorf.meteroid.util.MenuUtility;
import de.chaosdorf.meteroid.util.Utility;
import de.chaosdorf.meteroid.MeteroidNetworkActivity;

public class PickUsername extends MeteroidNetworkActivity implements AdapterView.OnItemClickListener, LongRunningIOCallback<List<User>>
{
	private boolean editHostnameOnBackButton = false;
	private ActivityPickUsernameBinding binding;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_pick_username);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			binding.swiperefresh.setEnabled(true);
			binding.swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
			{
				@Override
				public void onRefresh()
				{
					reload();
				}
			});
			binding.gridView.setOnTouchListener(new ShowHideOnScroll(binding.fab));
			binding.fab.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View view)
				{
					Utility.startActivity(activity, UserSettings.class);
				}
			});
			binding.fab.setVisibility(View.VISIBLE);
		}

		reload();
	}
	
	public void reload()
	{
		binding.pickUsernameError.setVisibility(View.GONE);
		binding.gridView.setVisibility(View.GONE);
		binding.progressBar.setVisibility(View.VISIBLE);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			binding.swiperefresh.setRefreshing(true);
		}
		new LongRunningIORequest<List<User>>(this, LongRunningIOTask.GET_USERS, connection.getAPI().listUsers());
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.pickusername, menu);
		MenuUtility.setChecked(menu, R.id.multi_user_mode, config.multiUserMode);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_reload:
				reload();
				break;
			case R.id.action_add:
				Utility.startActivity(this, UserSettings.class);
				break;
			case android.R.id.home:
			case R.id.edit_hostname:
				Utility.startActivity(this, SetHostname.class);
				break;
			case R.id.multi_user_mode:
				Utility.toggleMultiUserMode(this);
				item.setChecked(config.multiUserMode);
				break;
			case R.id.about:
				Utility.startActivity(this, About.class);
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
		if (binding.gridView != null)
		{
			binding.gridView.setAdapter(null);
		}
		super.onDestroy();
	}

	@Override
	public void displayErrorMessage(final LongRunningIOTask task, final String message)
	{
		Utility.displayToastMessage(this, message);
		binding.pickUsernameError.setVisibility(View.VISIBLE);
		binding.gridView.setVisibility(View.GONE);
		editHostnameOnBackButton = true;
		binding.progressBar.setVisibility(View.GONE);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			binding.swiperefresh.setRefreshing(false);
		}
	}

	@Override
	public void processIOResult(final LongRunningIOTask task, final List<User> result)
	{
		if (task == LongRunningIOTask.GET_USERS)
		{
			final List<User> itemList = result;
			editHostnameOnBackButton = false;
			final UserAdapter userAdapter = new UserAdapter(itemList);

			binding.gridView.setAdapter(userAdapter);
			binding.gridView.setOnItemClickListener(this);
			binding.progressBar.setVisibility(View.GONE);
			binding.gridView.setVisibility(View.VISIBLE);
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			{
				binding.swiperefresh.setRefreshing(false);
			}
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> adapterView, final View view, final int index, final long l)
	{
		final User user = (User) binding.gridView.getItemAtPosition(index);
		if (user != null && user.getName() != null)
		{
			config.userID = user.getId();
			config.save();
			Utility.startActivity(this, BuyDrink.class);
		}
	}

	public class UserAdapter extends MeteroidAdapter<User>
	{
		private final List<User> userList;
		private final LayoutInflater inflater;

		UserAdapter(final List<User> userList)
		{
			super(activity, R.layout.activity_pick_username, userList);
			this.userList = userList;
			this.inflater = activity.getLayoutInflater();
		}
		

		public View getView(final int position, final View convertView, final ViewGroup parent)
		{
			View view = convertView;
			ActivityPickUsernameItemBinding itemBinding;
			if (view == null)
			{
				itemBinding = ActivityPickUsernameItemBinding.inflate(
					inflater, parent, false
				);
				view = itemBinding.getRoot();
			}
			else
			{
				itemBinding = ActivityPickUsernameItemBinding.bind(view);
			}
			if (view == null | itemBinding == null)
			{
				return null;
			}

			final User user = userList.get(position);

			Utility.loadUserImage(activity, itemBinding.icon, user);
			itemBinding.icon.setContentDescription(user.getName());
			itemBinding.label.setText(user.getName());

			return view;
		}
	}
}
