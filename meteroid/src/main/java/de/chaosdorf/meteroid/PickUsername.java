package de.chaosdorf.meteroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.chaosdorf.meteroid.controller.UserController;
import de.chaosdorf.meteroid.imageloader.ImageLoader;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOCallback;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOGet;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOTask;
import de.chaosdorf.meteroid.model.User;
import de.chaosdorf.meteroid.util.Utility;

public class PickUsername extends Activity implements LongRunningIOCallback, AdapterView.OnItemClickListener
{
	private Activity activity = null;
	private GridView gridView = null;
	private boolean multiUserMode = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		activity = this;
		setContentView(R.layout.activity_pick_username);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final String hostname = prefs.getString("hostname", null);
		multiUserMode = prefs.getBoolean("multi_user_mode", false);

		new LongRunningIOGet(this, LongRunningIOTask.GET_USERS, hostname + "users.json").execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.pickusername, menu);
		final MenuItem menuItem = menu.findItem(R.id.multi_user_mode);
		if (menuItem != null)
		{
			menuItem.setChecked(multiUserMode);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		boolean resetView = true;
		switch (item.getItemId())
		{
			case R.id.reset_hostname:
				Utility.resetHostname(activity);
				break;
			case R.id.multi_user_mode:
				multiUserMode = Utility.toggleMultiUserMode(activity);
				item.setChecked(multiUserMode);
				if (multiUserMode)
				{
					Utility.resetUsername(activity);
				}
				resetView = false;
				break;
		}
		if (resetView)
		{
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}
		return super.onOptionsItemSelected(item);
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
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				Utility.displayToastMessage(activity, message);
				final TextView textView = (TextView) findViewById(R.id.pick_username_error);
				textView.setVisibility(View.VISIBLE);
			}
		});
	}

	@Override
	public void processIOResult(final LongRunningIOTask task, final String json)
	{
		if (task == LongRunningIOTask.GET_USERS && json != null)
		{
			final List<User> itemList = UserController.parseAllUsersFromJSON(json);
			final UserAdapter userAdapter = new UserAdapter(itemList);

			gridView = (GridView) findViewById(R.id.grid_view);
			gridView.setAdapter(userAdapter);
			gridView.setOnItemClickListener(this);
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l)
	{
		final User user = (User) gridView.getItemAtPosition(i);
		if (user != null && user.getName() != null)
		{
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			prefs.edit().putInt("userid", user.getId()).apply();
			Intent intent = new Intent(view.getContext(), MainActivity.class);
			startActivity(intent);
			finish();
		}
	}

	private class UserAdapter extends ArrayAdapter<User>
	{
		private final List<User> userList;
		private final LayoutInflater inflater;
		private final ImageLoader imageLoader;

		UserAdapter(final List<User> userList)
		{
			super(activity, R.layout.activity_pick_username, userList);
			this.userList = userList;
			this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.imageLoader = new ImageLoader(activity.getApplicationContext(), 80);
		}

		public View getView(int position, View convertView, ViewGroup parent)
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

			Utility.loadGravatarImage(imageLoader, icon, user);
			icon.setContentDescription(user.getName());
			label.setText(user.getName());

			return view;
		}
	}
}
