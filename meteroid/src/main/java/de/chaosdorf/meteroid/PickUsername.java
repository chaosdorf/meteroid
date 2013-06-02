package de.chaosdorf.meteroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import de.chaosdorf.meteroid.controller.UserController;
import de.chaosdorf.meteroid.imageloader.ImageLoader;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOCallback;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOGet;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOTask;
import de.chaosdorf.meteroid.model.User;
import de.chaosdorf.meteroid.util.Utility;

public class PickUsername extends Activity implements LongRunningIOCallback, View.OnClickListener, AdapterView.OnItemClickListener
{
	private Activity activity = null;
	private ListView listView = null;
	private UserAdapter userAdapter = null;
	private Button chooseButton = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		activity = this;
		setContentView(R.layout.activity_pick_username);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final String hostname = prefs.getString("hostname", null);

		new LongRunningIOGet(this, LongRunningIOTask.GET_USERS, hostname + "users.json").execute();
	}

	@Override
	public void onDestroy()
	{
		listView.setAdapter(null);
		super.onDestroy();
	}

	@Override
	public void displayErrorMessage(final String message)
	{
		Utility.displayToastMessage(activity, message);
	}

	@Override
	public void processIOResult(final LongRunningIOTask task, final String json)
	{
		if (task == LongRunningIOTask.GET_USERS && json != null)
		{
			final List<User> itemList = UserController.parseAllUsersFromJSON(json);
			userAdapter = new UserAdapter(itemList);

			listView = (ListView) findViewById(R.id.list_view);
			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			listView.setAdapter(userAdapter);
			listView.setOnItemClickListener(this);

			chooseButton = (Button) findViewById(R.id.save_button);
			chooseButton.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(final View view)
	{
		if (view != chooseButton)
		{
			return;
		}
		if (listView.getCheckedItemPosition() <= 0)
		{
			return;
		}
		final User user = (User) listView.getAdapter().getItem(listView.getCheckedItemPosition());
		if (user != null && user.getName() != null)
		{
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			prefs.edit().putString("username", user.getName()).putInt("userid", user.getId()).apply();
			Intent intent = new Intent(view.getContext(), MainActivity.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l)
	{
		userAdapter.notifyDataSetChanged();
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
			this.imageLoader = new ImageLoader(activity.getApplicationContext(), 50);
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
			final CheckedTextView checkBox = (CheckedTextView) view.findViewById(R.id.checkstate);

			Utility.loadGravatarImage(imageLoader, icon, user);
			label.setText(user.getName());
			checkBox.setChecked(listView.getCheckedItemPosition() == position);

			return view;
		}
	}
}
