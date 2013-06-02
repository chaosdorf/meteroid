package de.chaosdorf.meteroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
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
import de.chaosdorf.meteroid.enums.LongRunningIOTask;
import de.chaosdorf.meteroid.interfaces.LongRunningGetIOCallback;
import de.chaosdorf.meteroid.model.User;
import de.chaosdorf.meteroid.util.ImageLoader;
import de.chaosdorf.meteroid.util.LongRunningGetIO;
import de.chaosdorf.meteroid.util.Utility;

public class PickUsername extends Activity implements LongRunningGetIOCallback, View.OnClickListener, AdapterView.OnItemClickListener
{
	List<User> itemList = null;
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

		new LongRunningGetIO(this, LongRunningIOTask.GET_USERS, hostname + "users.json").execute();
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
			itemList = UserController.parseAllUsersFromJSON(json);
			userAdapter = new UserAdapter();

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
		private LayoutInflater inflater = null;
		private ImageLoader imageLoader;

		UserAdapter()
		{
			super(activity, R.layout.activity_pick_username, itemList);
			inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			imageLoader = new ImageLoader(activity.getApplicationContext(), 50);
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

			final User user = itemList.get(position);
			final ImageView icon = (ImageView) view.findViewById(R.id.icon);
			final TextView label = (TextView) view.findViewById(R.id.label);
			final CheckedTextView checkBox = (CheckedTextView) view.findViewById(R.id.checkstate);

			Utility.loadGravatarImage(imageLoader, icon, user);
			label.setText(Html.fromHtml(user.getName()));
			checkBox.setChecked(listView.getCheckedItemPosition() == position);

			return view;
		}
	}
}
