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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.chaosdorf.meteroid.controller.DrinkController;
import de.chaosdorf.meteroid.controller.UserController;
import de.chaosdorf.meteroid.imageloader.ImageLoader;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOCallback;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOGet;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOTask;
import de.chaosdorf.meteroid.model.Drink;
import de.chaosdorf.meteroid.model.User;
import de.chaosdorf.meteroid.util.Utility;

public class BuyDrink extends Activity implements LongRunningIOCallback, AdapterView.OnItemClickListener
{
	private final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00 '\u20AC'");
	private final AtomicBoolean isBuying = new AtomicBoolean(false);
	private final AtomicBoolean isBuyingDrink = new AtomicBoolean(false);
	private Activity activity = null;
	private ListView listView = null;
	private String hostname = null;
	private int userID = 0;
	private boolean multiUserMode;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		activity = this;
		setContentView(R.layout.activity_buy_drink);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		hostname = prefs.getString("hostname", null);
		userID = prefs.getInt("userid", 0);
		multiUserMode = prefs.getBoolean("multi_user_mode", false);

		final ImageButton backButton = (ImageButton) findViewById(R.id.button_back);
		backButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				Intent intent = new Intent(activity, PickUsername.class);
				startActivity(intent);
				finish();
			}
		});

		final ImageButton reloadButton = (ImageButton) findViewById(R.id.button_reload);
		reloadButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				Intent intent = new Intent(activity, BuyDrink.class);
				startActivity(intent);
				finish();
			}
		});

		new LongRunningIOGet(this, LongRunningIOTask.GET_USER, hostname + "users/" + userID + ".json").execute();
		new LongRunningIOGet(this, LongRunningIOTask.GET_DRINKS, hostname + "drinks.json").execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.buydrink, menu);
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
			case R.id.reset_username:
				Utility.resetUsername(activity);
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
		if (listView != null)
		{
			listView.setAdapter(null);
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
				if (task == LongRunningIOTask.GET_USER || task == LongRunningIOTask.UPDATE_USER)
				{
					Utility.displayToastMessage(activity, getResources().getString(R.string.error_user_not_found) + " " + message);
				}
				else
				{
					Utility.displayToastMessage(activity, message);
				}
				final TextView textView = (TextView) findViewById(R.id.buy_drink_error);
				textView.setVisibility(View.VISIBLE);
				if (listView != null)
				{
					listView.setVisibility(View.GONE);
				}
			}
		});
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
				case UPDATE_USER:
				{
					final User user = UserController.parseUserFromJSON(json);
					final TextView balance = (TextView) findViewById(R.id.balance);
					if (task == LongRunningIOTask.GET_USER)
					{
						final TextView label = (TextView) findViewById(R.id.username);
						final ImageLoader imageLoader = new ImageLoader(activity.getApplicationContext(), 80);
						final ImageView icon = (ImageView) findViewById(R.id.icon);
						label.setText(user.getName());
						Utility.loadGravatarImage(imageLoader, icon, user);
					}
					balance.setText(DECIMAL_FORMAT.format(user.getBalance()));
					if (task == LongRunningIOTask.UPDATE_USER && multiUserMode && isBuyingDrink.get())
					{
						Intent intent = new Intent(activity, PickUsername.class);
						startActivity(intent);
						finish();
					}
					break;
				}

				// Parse drinks
				case GET_DRINKS:
				{
					final List<Drink> drinks = DrinkController.parseAllDrinksFromJSON(json);
					final DrinkAdapter drinkAdapter = new DrinkAdapter(drinks);
					listView = (ListView) findViewById(R.id.list_view);

					drinks.addAll(DrinkController.getMoneyList());
					Collections.sort(drinks, new DrinkComparator());

					listView.setAdapter(drinkAdapter);
					listView.setOnItemClickListener(this);
					break;
				}

				// Bought drink
				case PAY_DRINK:
				{
					Utility.displayToastMessage(activity, getResources().getString(isBuyingDrink.get() ? R.string.buy_drink_bought_drink : R.string.buy_drink_added_money));
					new LongRunningIOGet(this, LongRunningIOTask.UPDATE_USER, hostname + "users/" + userID + ".json").execute();
					isBuying.set(false);
					break;
				}
			}
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> adapterView, final View view, final int index, final long l)
	{
		if (index < 0 || isBuying.get())
		{
			Utility.displayToastMessage(activity, getResources().getString(R.string.buy_drink_pending));
			return;
		}
		if (isBuying.compareAndSet(false, true))
		{
			final Drink drink = (Drink) listView.getAdapter().getItem(index);
			if (drink != null)
			{
				isBuyingDrink.set(drink.getDonationRecommendation() > 0);
				new LongRunningIOGet(this, LongRunningIOTask.PAY_DRINK, hostname + "users/" + userID + "/deposit?amount=" + (-drink.getDonationRecommendation())).execute();
			}
		}
	}

	private class DrinkAdapter extends ArrayAdapter<Drink>
	{
		private final List<Drink> drinkList;
		private final LayoutInflater inflater;

		DrinkAdapter(final List<Drink> drinkList)
		{
			super(activity, R.layout.activity_buy_drink, drinkList);
			this.drinkList = drinkList;
			this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			if (view == null)
			{
				view = inflater.inflate(R.layout.activity_buy_drink_item, parent, false);
			}
			if (view == null)
			{
				return null;
			}

			final Drink drink = drinkList.get(position);
			String logo = drink.getLogoUrl();
			boolean isDrink = false;
			if (!logo.startsWith("euro_"))
			{
				isDrink = true;
				logo = "drink_" + logo;
			}
			else if (logo.startsWith("drink_"))
			{
				isDrink = true;
			}
			final StringBuilder drinkLabel = new StringBuilder();
			drinkLabel.append((drink.getDonationRecommendation() < 0) ? "+" : "")
					.append(DECIMAL_FORMAT.format(-drink.getDonationRecommendation()))
					.append(isDrink ? " (" + drink.getName() + ")" : "");
			final int drinkIconID = getResources().getIdentifier(logo, "drawable", getPackageName());
			final ImageView icon = (ImageView) view.findViewById(R.id.icon);
			final TextView label = (TextView) view.findViewById(R.id.label);

			icon.setContentDescription(drink.getName());
			icon.setImageResource(drinkIconID > 0 ? drinkIconID : R.drawable.drink_0l33);
			label.setText(drinkLabel.toString());

			return view;
		}
	}

	private class DrinkComparator implements Comparator<Drink>
	{
		@Override
		public int compare(Drink drink, Drink drink2)
		{
			return (int) Math.round(drink2.getDonationRecommendation() * 100 - drink.getDonationRecommendation() * 100);
		}
	}
}
