package de.chaosdorf.meteroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import de.chaosdorf.meteroid.interfaces.LongRunningGetIOCallback;
import de.chaosdorf.meteroid.model.User;
import de.chaosdorf.meteroid.util.LongRunningGetIO;
import de.chaosdorf.meteroid.util.Utility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

		new LongRunningGetIO(this, hostname + "users.json").execute();
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
	public void processIOResult(final String json)
	{
		if (json != null)
		{
			itemList = createItemList(json);
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
		/*
		final User user = (User) listView.getAdapter().getItem(listView.getCheckedItemPosition());
		if (user != null && user.getName() != null)
		{
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			prefs.edit().putString("username", user.getName()).apply();
			Intent intent = new Intent(view.getContext(), MainActivity.class);
			startActivity(intent);
			finish();
		}
		*/
	}

	@Override
	public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l)
	{
		userAdapter.notifyDataSetChanged();
	}

	private List<User> createItemList(final String json)
	{
		final List<User> list = new ArrayList<User>();
		try
		{
			final JSONArray jsonArray = new JSONArray(json);
			for (int i = 0; i < jsonArray.length(); i++)
			{
				final JSONObject jsonObject = jsonArray.getJSONObject(i);
				final User user = new User(
						jsonObject.getInt("id"),
						jsonObject.getString("name"),
						jsonObject.getString("email"),
						jsonObject.getLong("balance_cents"),
						new Date(),
						new Date()
				);
				list.add(user);
			}
		}
		catch (JSONException ignored)
		{
		}
		return list;
	}

	private class UserAdapter extends ArrayAdapter<User>
	{
		UserAdapter()
		{
			super(activity, R.layout.activity_pick_username, itemList);
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			View row = convertView;
			if (row == null)
			{
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.activity_pick_username_item, parent, false);
			}
			if (row == null)
			{
				return null;
			}

			//final ImageView icon = (ImageView) row.findViewById(R.id.icon);
			final TextView label = (TextView) row.findViewById(R.id.label);
			final CheckedTextView checkBox = (CheckedTextView) row.findViewById(R.id.checkstate);
			final User user = itemList.get(position);

			//icon.setImageBitmap(Utility.getGravatarBitbamp(user));
			label.setText(user.getName());
			checkBox.setChecked(listView.getCheckedItemPosition() == position);

			return (row);
		}
	}
}
