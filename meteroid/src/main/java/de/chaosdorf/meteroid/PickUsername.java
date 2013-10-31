package de.chaosdorf.meteroid;

import android.app.Activity;
import android.content.Context;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;
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
	private static final int NEW_USER_ID = -1;

	private Activity activity = null;
	private GridView gridView = null;
	private boolean multiUserMode = false;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		activity = this;
		setContentView(R.layout.activity_pick_username);

		gridView = (GridView) findViewById(R.id.grid_view);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final String hostname = prefs.getString("hostname", null);
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

		new LongRunningIOGet(this, LongRunningIOTask.GET_USERS, hostname + "users.json").execute();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
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
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.reset_hostname:
				Utility.resetHostname(activity);
				Utility.startActivity(activity, SetHostname.class);
				break;
			case R.id.multi_user_mode:
				multiUserMode = Utility.toggleMultiUserMode(activity);
				item.setChecked(multiUserMode);
				if (multiUserMode)
				{
					Utility.resetUsername(activity);
				}
				break;
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
				final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.pick_username_error);
				linearLayout.setVisibility(View.VISIBLE);
				gridView.setVisibility(View.GONE);
			}
		});
	}

	@Override
	public void processIOResult(final LongRunningIOTask task, final String json)
	{
		if (task == LongRunningIOTask.GET_USERS && json != null)
		{
			final List<User> itemList = UserController.parseAllUsersFromJSON(json);
			itemList.add(new User(NEW_USER_ID, getResources().getString(R.string.pick_username_new_user), "", 0, new Date(), new Date()));
			final UserAdapter userAdapter = new UserAdapter(itemList);

			gridView.setAdapter(userAdapter);
			gridView.setOnItemClickListener(this);
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
				Utility.startActivity(activity, AddUserActivity.class);
			}
			else
			{
				final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				prefs.edit().putInt("userid", user.getId()).apply();
				Utility.startActivity(activity, BuyDrink.class);
			}
		}
	}

	public class UserAdapter extends ArrayAdapter<User>
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

			Utility.loadGravatarImage(imageLoader, icon, user);
			icon.setContentDescription(user.getName());
			label.setText(user.getName());

			if (user.getId() == NEW_USER_ID)
			{
				icon.setImageDrawable(getResources().getDrawable(R.drawable.add_person));
			}

			return view;
		}
	}
}
