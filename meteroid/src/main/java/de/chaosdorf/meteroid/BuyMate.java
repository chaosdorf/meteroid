package de.chaosdorf.meteroid;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

public class BuyMate extends Activity implements LongRunningIOCallback, AdapterView.OnItemClickListener
{
	private final DecimalFormat df = new DecimalFormat("0.00 '\u20AC'");
	private Activity activity = null;
	private ListView listView;
	private AtomicBoolean isBuying = new AtomicBoolean(false);
	private String hostname;
	private int userID;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		activity = this;
		setContentView(R.layout.activity_buy_mate);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		final TextView label = (TextView) findViewById(R.id.username);
		label.setText(prefs.getString("username", null));

		hostname = prefs.getString("hostname", null);
		userID = prefs.getInt("userid", 0);

		new LongRunningIOGet(this, LongRunningIOTask.GET_USER, hostname + "users/" + userID + ".json").execute();
		new LongRunningIOGet(this, LongRunningIOTask.GET_DRINKS, hostname + "drinks.json").execute();
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
					final ImageLoader imageLoader = new ImageLoader(activity.getApplicationContext(), 80);
					final User user = UserController.parseUserFromJSON(json);
					final ImageView icon = (ImageView) findViewById(R.id.icon);
					final TextView balance = (TextView) findViewById(R.id.balance);

					Utility.loadGravatarImage(imageLoader, icon, user);
					balance.setText(df.format(user.getBalanceCents() / 100.0));
					break;

				// Parse drinks
				case GET_DRINKS:
					final List<Drink> drinks = DrinkController.parseAllDrinksFromJSON(json);
					final DrinkAdapter drinkAdapter = new DrinkAdapter(drinks);
					listView = (ListView) findViewById(R.id.list_view);

					drinks.addAll(DrinkController.getMoneyList());
					Collections.sort(drinks, new DrinkComparator());

					listView.setAdapter(drinkAdapter);
					listView.setOnItemClickListener(this);
					break;

				// Bought drink
				case PAY_DRINK:
					Utility.displayToastMessage(activity, getResources().getString(R.string.buy_mate_bought_drink));
					new LongRunningIOGet(this, LongRunningIOTask.GET_USER, hostname + "users/" + userID + ".json").execute();
					isBuying.set(false);
					break;
			}
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> adapterView, final View view, final int index, final long l)
	{
		if (index < 0 || isBuying.get())
		{
			Utility.displayToastMessage(activity, getResources().getString(R.string.buy_mate_pending));
			return;
		}
		if (isBuying.compareAndSet(false, true))
		{
			final Drink drink = (Drink) listView.getAdapter().getItem(index);
			if (drink != null)
			{
				new LongRunningIOGet(this, LongRunningIOTask.PAY_DRINK, hostname + "users/" + userID + "/deposit?amount=" + (-drink.getDonationRecommendation() * 100));
			}
		}
	}

	private class DrinkAdapter extends ArrayAdapter<Drink>
	{
		private final List<Drink> drinkList;
		private final LayoutInflater inflater;

		DrinkAdapter(final List<Drink> drinkList)
		{
			super(activity, R.layout.activity_buy_mate, drinkList);
			this.drinkList = drinkList;
			this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			if (view == null)
			{
				view = inflater.inflate(R.layout.activity_buy_mate_drink, parent, false);
			}
			if (view == null)
			{
				return null;
			}

			final Drink drink = drinkList.get(position);
			String logo = drink.getLogoUrl();
			if (!logo.startsWith("euro_") && !logo.startsWith("drink_"))
			{
				logo = "drink_" + logo;
			}
			final int drinkIconID = getResources().getIdentifier(logo, "drawable", getPackageName());
			final ImageView icon = (ImageView) view.findViewById(R.id.icon);
			final TextView label = (TextView) view.findViewById(R.id.label);

			icon.setContentDescription(drink.getName());
			icon.setImageResource(drinkIconID > 0 ? drinkIconID : R.drawable.drink_0l33);
			label.setText(df.format(-drink.getDonationRecommendation()));

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
