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

import android.app.Activity;
import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
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
import android.widget.ListView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import de.chaosdorf.meteroid.controller.DrinkController;
import de.chaosdorf.meteroid.controller.MoneyController;
import de.chaosdorf.meteroid.controller.UserController;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOCallback;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOGet;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOTask;
import de.chaosdorf.meteroid.model.BuyableItem;
import de.chaosdorf.meteroid.model.User;
import de.chaosdorf.meteroid.model.Drink;
import de.chaosdorf.meteroid.util.MenuUtility;
import de.chaosdorf.meteroid.util.Utility;

public class BuyDrink extends Activity implements LongRunningIOCallback, AdapterView.OnItemClickListener
{
	private final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00 '\u20AC'");

	private final AtomicBoolean isBuying = new AtomicBoolean(true);
	private final AtomicReference<BuyableItem> buyingItem = new AtomicReference<BuyableItem>(null);

	private Activity activity = null;
	private GridView gridView = null;
	private ListView listView = null;
	private String hostname = null;

	private int userID = 0;
	private User user;

	private boolean useGridView;
	private boolean multiUserMode;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		activity = this;
		setContentView(R.layout.activity_buy_drink);

		gridView = (GridView) findViewById(R.id.grid_view);
		listView = (ListView) findViewById(R.id.list_view);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		hostname = prefs.getString("hostname", null);
		userID = prefs.getInt("userid", 0);
		useGridView = prefs.getBoolean("use_grid_view", false);
		multiUserMode = prefs.getBoolean("multi_user_mode", false);

		final ImageButton backButton = (ImageButton) findViewById(R.id.button_back);
		backButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				Utility.resetUsername(activity);
				Utility.startActivity(activity, PickUsername.class);
			}
		});

		final ImageButton reloadButton = (ImageButton) findViewById(R.id.button_reload);
		reloadButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				Utility.startActivity(activity, BuyDrink.class);
			}
		});

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			reloadButton.setVisibility(View.GONE);
			backButton.setVisibility(View.GONE);
		}

		new LongRunningIOGet(this, LongRunningIOTask.GET_USER, hostname + "users/" + userID + ".json");
		new LongRunningIOGet(this, LongRunningIOTask.GET_DRINKS, hostname + "drinks.json");
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.buydrink, menu);
		MenuUtility.setChecked(menu, R.id.use_grid_view, useGridView);
		MenuUtility.setChecked(menu, R.id.multi_user_mode, multiUserMode);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				Utility.resetUsername(activity);
				Utility.startActivity(activity, PickUsername.class);
				break;
			case R.id.action_reload:
				Utility.startActivity(activity, BuyDrink.class);
				break;
			case R.id.edit_hostname:
				Utility.startActivity(activity, SetHostname.class);
				break;
			case R.id.reset_username:
				Utility.resetUsername(activity);
				Utility.startActivity(activity, PickUsername.class);
				break;
			case R.id.use_grid_view:
				useGridView = Utility.toggleUseGridView(activity);
				item.setChecked(useGridView);
				Utility.startActivity(activity, BuyDrink.class);
				break;
			case R.id.multi_user_mode:
				multiUserMode = MenuUtility.onClickMultiUserMode(this, item);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(final int keyCode, @NotNull final KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (multiUserMode)
			{
				Utility.resetUsername(activity);
				Utility.startActivity(activity, MainActivity.class);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onDestroy()
	{
		buyingItem.set(null);
		if (gridView != null)
		{
			gridView.setAdapter(null);
		}
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
				buyingItem.set(null);
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
				gridView.setVisibility(View.GONE);
				listView.setVisibility(View.GONE);
			}
		});
	}

	@Override
	public void processIOResult(final LongRunningIOTask task, final String json)
	{
		if (json != null)
		{
			final BuyDrink buydrink = this;
			runOnUiThread(new Runnable()
			{
				public void run()
				{
					switch (task)
					{
						// Parse user data
						case GET_USER:
						case UPDATE_USER:
						{
							user = UserController.parseUserFromJSON(json);
							if (task == LongRunningIOTask.GET_USER)
							{
								final TextView label = (TextView) findViewById(R.id.username);
								final ImageView icon = (ImageView) findViewById(R.id.icon);
								label.setText(user.getName());
								Utility.loadGravatarImage(activity, icon, user);
							}
							final TextView balance = (TextView) findViewById(R.id.balance);
							balance.setText(DECIMAL_FORMAT.format(user.getBalance()));
							isBuying.set(false);
							break;
						}

						// Parse drinks
						case GET_DRINKS:
						{
							final List<BuyableItem> buyableItemList = DrinkController.parseAllDrinksFromJSON(json, hostname);
							MoneyController.addMoney(buyableItemList);
							Collections.sort(buyableItemList, new BuyableComparator());

							final BuyableItemAdapter buyableItemAdapter = new BuyableItemAdapter(buyableItemList);
							if (useGridView)
							{
								gridView.setAdapter(buyableItemAdapter);
								gridView.setOnItemClickListener(buydrink);
								gridView.setVisibility(View.VISIBLE);
							}
							else
							{
								listView.setAdapter(buyableItemAdapter);
								listView.setOnItemClickListener(buydrink);
								listView.setVisibility(View.VISIBLE);
							}
							break;
						}

						// Bought drink
						case BUY_DRINK:
						{
							final BuyableItem buyableItem = buyingItem.get();
							if (buyableItem != null)
							{
								buyingItem.set(null);
								Utility.displayToastMessage(activity,
										String.format(
												getResources().getString(R.string.buy_drink_bought_drink),
												buyableItem.getName(),
												DECIMAL_FORMAT.format(buyableItem.getDonationRecommendation())
										)
								);
								// Adjust the displayed balance to give an immediate user feedback
								if (user != null)
								{
									final TextView balance = (TextView) findViewById(R.id.balance);
									balance.setText(DECIMAL_FORMAT.format(user.getBalance() - buyableItem.getDonationRecommendation()));
								}
								if (multiUserMode)
								{
									Utility.startActivity(activity, PickUsername.class);
									break;
								}
							}
							new LongRunningIOGet(buydrink, LongRunningIOTask.UPDATE_USER, hostname + "users/" + userID + ".json");
							break;
						}

						// Added money
						case ADD_MONEY:
						{
							final BuyableItem buyableItem = buyingItem.get();
							if (buyableItem != null)
							{
								buyingItem.set(null);
								Utility.displayToastMessage(activity,
										String.format(
												getResources().getString(R.string.buy_drink_added_money),
												buyableItem.getName(),
												DECIMAL_FORMAT.format(buyableItem.getDonationRecommendation())
										)
								);
								// Adjust the displayed balance to give an immediate user feedback
								if (user != null)
								{
									final TextView balance = (TextView) findViewById(R.id.balance);
									balance.setText(DECIMAL_FORMAT.format(user.getBalance() - buyableItem.getDonationRecommendation()));
								}
							}
							new LongRunningIOGet(buydrink, LongRunningIOTask.UPDATE_USER, hostname + "users/" + userID + ".json");
							break;
						}
					}
				}
			});
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
			final BuyableItem buyableItem = (BuyableItem) (useGridView ? gridView.getItemAtPosition(index) : listView.getAdapter().getItem(index));
			if (buyableItem != null)
			{
				buyingItem.set(buyableItem);
				if(buyableItem.isDrink())
				{
					new LongRunningIOGet(this, LongRunningIOTask.BUY_DRINK, hostname + "users/" + userID + "/buy.json?drink=" + ((Drink)buyableItem).getId());
				}
				else
				{
					new LongRunningIOGet(this, LongRunningIOTask.ADD_MONEY, hostname + "users/" + userID + "/deposit.json?amount=" + (-buyableItem.getDonationRecommendation()));
				}
			}
		}
	}

	private class BuyableItemAdapter extends ArrayAdapter<BuyableItem>
	{
		private final List<BuyableItem> drinkList;
		private final LayoutInflater inflater;

		BuyableItemAdapter(final List<BuyableItem> drinkList)
		{
			super(activity, R.layout.activity_buy_drink, drinkList);
			this.drinkList = drinkList;
			this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public View getView(final int position, final View convertView, final ViewGroup parent)
		{
			View view = convertView;
			if (view == null)
			{
				view = inflater.inflate(useGridView ? R.layout.activity_buy_drink_item_gridview : R.layout.activity_buy_drink_item, parent, false);
			}
			if (view == null)
			{
				return null;
			}

			final BuyableItem buyableItem = drinkList.get(position);

			final ImageView icon = (ImageView) view.findViewById(R.id.icon);
			Utility.loadBuyableItemImage(activity, icon, buyableItem);

			final TextView label = (TextView) view.findViewById(R.id.label);
			label.setText(createLabel(buyableItem, useGridView));

			return view;
		}

		private String createLabel(final BuyableItem buyableItem, final boolean useGridView)
		{
			final StringBuilder label = new StringBuilder();
			if (!buyableItem.isDrink())
			{
				label.append("+");
			}
			label.append(DECIMAL_FORMAT.format(-buyableItem.getDonationRecommendation()));
			if (buyableItem.isDrink())
			{
				if (useGridView)
				{
					label.append("\n");
				}
				label.append(" (").append(buyableItem.getName()).append(")");
			}
			return label.toString();
		}
	}

	private class BuyableComparator implements Comparator<BuyableItem>
	{
		@Override
		public int compare(final BuyableItem buyableItem, final BuyableItem buyableItem2)
		{
			return (int) Math.round(buyableItem2.getDonationRecommendation() * 100 - buyableItem.getDonationRecommendation() * 100);
		}
	}
}
