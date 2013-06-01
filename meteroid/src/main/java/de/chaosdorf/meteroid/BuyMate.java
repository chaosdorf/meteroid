package de.chaosdorf.meteroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;

import de.chaosdorf.meteroid.interfaces.LongRunningGetIOCallback;
import de.chaosdorf.meteroid.model.LongRunningIOTask;
import de.chaosdorf.meteroid.model.User;
import de.chaosdorf.meteroid.util.ImageLoader;
import de.chaosdorf.meteroid.util.LongRunningGetIO;
import de.chaosdorf.meteroid.util.Utility;

public class BuyMate extends Activity implements LongRunningGetIOCallback
{
	private Activity activity = null;
	private ImageLoader imageLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		activity = this;
		setContentView(R.layout.activity_buy_mate);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		imageLoader = new ImageLoader(activity.getApplicationContext(), 80);
		final String hostname = prefs.getString("hostname", null);

		final TextView label = (TextView) findViewById(R.id.username);
		label.setText(prefs.getString("username", null));

		final int userID = prefs.getInt("userid", 0);
		new LongRunningGetIO(this, LongRunningIOTask.GET_USER, hostname + "users/" + userID + ".json").execute();
		new LongRunningGetIO(this, LongRunningIOTask.GET_DRINKS, hostname + "drinks.json").execute();
	}

	@Override
	public void displayErrorMessage(final String message)
	{
		Utility.displayToastMessage(activity, message);
	}

	@Override
	public void processIOResult(final LongRunningIOTask task, final String json)
	{
		if (json != null)
		{
			switch (task)
			{
				// Parse user data
				case GET_USER:
					final User user = Utility.parseUserFromJSON(json);
					final ImageView icon = (ImageView) findViewById(R.id.icon);

					Utility.setGravatarImage(imageLoader, icon, user);

					final DecimalFormat df = new DecimalFormat("0.00 '€'");
					final TextView balance = (TextView) findViewById(R.id.balance);
					balance.setText(Html.fromHtml(df.format(user.getBalance_cents() / 100.0)));
					break;

				// Parse drinks
				case GET_DRINKS:
					break;
			}
		}
	}
}
