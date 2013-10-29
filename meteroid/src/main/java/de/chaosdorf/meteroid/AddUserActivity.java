package de.chaosdorf.meteroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		activity = this;
		setContentView(R.layout.activity_add_user);
		Button addButton = (Button) findViewById(R.id.button_add_user);

		final TextView usernameText = (TextView) findViewById(R.id.username);
		final TextView emailText = (TextView) findViewById(R.id.email);
		final TextView balanceText = (TextView) findViewById(R.id.balance);
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		addButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				final String hostname = prefs.getString("hostname", null);
				final CharSequence username = usernameText.getText();
				final CharSequence email = emailText.getText();
				final CharSequence balance = balanceText.getText();
				if (username == null || username.length() == 0)
				{
					Utility.displayToastMessage(activity, getResources().getString(R.string.add_user_empty_username));
					return;
				}
				String emailValue = "";
				if (email != null && email.length() > 0)
				{
					emailValue = email.toString();
				}
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
	public void displayErrorMessage(LongRunningIOTask task, String message)
	{
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
