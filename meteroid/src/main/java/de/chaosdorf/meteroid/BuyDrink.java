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

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Build;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import de.chaosdorf.meteroid.controller.MoneyController;
import de.chaosdorf.meteroid.databinding.ActivityBuyDrinkBinding;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOCallback;
import de.chaosdorf.meteroid.longrunningio.LongRunningIORequest;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOTask;
import de.chaosdorf.meteroid.model.BuyableItem;
import de.chaosdorf.meteroid.model.User;
import de.chaosdorf.meteroid.model.Drink;
import de.chaosdorf.meteroid.util.MenuUtility;
import de.chaosdorf.meteroid.util.Utility;
import de.chaosdorf.meteroid.MeteroidNetworkActivity;

public class BuyDrink extends MeteroidNetworkActivity implements AdapterView.OnItemClickListener, LongRunningIOCallback
{
	private final AtomicBoolean isBuying = new AtomicBoolean(true);
	private final AtomicReference<BuyableItem> buyingItem = new AtomicReference<BuyableItem>(null);

	private User user;
	private ActivityBuyDrinkBinding binding;

	private boolean useGridView;
	private boolean multiUserMode;
	
	private IntentIntegrator barcodeIntegrator;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_buy_drink);
		binding.setUser(user);
		binding.setDECIMALFORMAT(DECIMAL_FORMAT);

		barcodeIntegrator = new IntentIntegrator(this);

		useGridView = prefs.getBoolean("use_grid_view", false);
		multiUserMode = prefs.getBoolean("multi_user_mode", false);

		binding.buttonBack.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				Utility.resetUsername(activity);
				Utility.startActivity(activity, PickUsername.class);
			}
		});

		binding.buttonReload.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				reload();
			}
		});

		binding.buttonEdit.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				Utility.startActivity(activity, UserSettings.class);
			}
		});
		
		binding.buttonBarcode.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				barcodeIntegrator.initiateScan();
			}
		});

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			binding.buttonReload.setVisibility(View.GONE);
			binding.buttonBack.setVisibility(View.GONE);
			binding.buttonEdit.setVisibility(View.GONE);
			binding.buttonBarcode.setVisibility(View.GONE);
		}
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			binding.fab.hide(false);
			binding.fab.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View view)
				{
					barcodeIntegrator.initiateScan();
				}
			});
			binding.fab.setVisibility(View.VISIBLE);
		}
		
		reload();
	}
	
	public void reload()
	{
		binding.gridView.setVisibility(View.GONE);
		binding.listView.setVisibility(View.GONE);
		binding.progressBar.setVisibility(View.VISIBLE);
		new LongRunningIORequest<User>(this, LongRunningIOTask.GET_USER, api.getUser(userID));
		new LongRunningIORequest<List<Drink>>(this, LongRunningIOTask.GET_DRINKS, api.listDrinks());
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
				Utility.resetUsername(this);
				Utility.startActivity(this, PickUsername.class);
				break;
			case R.id.action_reload:
				reload();
				break;
			case R.id.action_edit:
				Utility.startActivity(this, UserSettings.class);
				break;
			case R.id.action_barcode:
				barcodeIntegrator.initiateScan();
				break;
			case R.id.edit_hostname:
				Utility.startActivity(this, SetHostname.class);
				break;
			case R.id.reset_username:
				Utility.resetUsername(this);
				Utility.startActivity(this, PickUsername.class);
				break;
			case R.id.use_grid_view:
				useGridView = Utility.toggleUseGridView(this);
				item.setChecked(useGridView);
				Utility.startActivity(this, BuyDrink.class);
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
				Utility.resetUsername(this);
				Utility.startActivity(this, MainActivity.class);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onDestroy()
	{
		buyingItem.set(null);
		if (binding.gridView != null)
		{
			binding.gridView.setAdapter(null);
		}
		if (binding.listView != null)
		{
			binding.listView.setAdapter(null);
		}
		super.onDestroy();
	}

	@Override
	public void displayErrorMessage(final LongRunningIOTask task, final String message)
	{
		buyingItem.set(null);
		if (task == LongRunningIOTask.GET_USER || task == LongRunningIOTask.UPDATE_USER)
		{
			Utility.displayToastMessage(this, getResources().getString(R.string.error_user_not_found) + " " + message);
		}
		else
		{
			Utility.displayToastMessage(this, message);
		}
		binding.buyDrinkError.setVisibility(View.VISIBLE);
		binding.gridView.setVisibility(View.GONE);
		binding.listView.setVisibility(View.GONE);
		binding.progressBar.setVisibility(View.GONE);
	}

	@Override
	public void processIOResult(final LongRunningIOTask task, final Object result)
	{
		switch (task)
		{
			// Parse user data
			case GET_USER:
			case UPDATE_USER:
			{
				user = (User)result;
				binding.setUser(user);
				((BuyDrink)activity).user = user;
				if (task == LongRunningIOTask.GET_USER)
				{
					Utility.loadUserImage(this, binding.icon, user);
				}
				isBuying.set(false);
				setProgressBarIndeterminateVisibility(false);
				break;
			}
		
			// Parse drinks
			case GET_DRINKS:
			{
				final List<BuyableItem> buyableItemList = (List<BuyableItem>)result;
				MoneyController.addMoney(buyableItemList);
				Collections.sort(buyableItemList, new BuyableComparator());

				final BuyableItemAdapter buyableItemAdapter = new BuyableItemAdapter(buyableItemList);
				if (useGridView)
				{
					binding.gridView.setAdapter(buyableItemAdapter);
					binding.gridView.setOnItemClickListener(this);
					binding.gridView.setVisibility(View.VISIBLE);
				}
				else
				{
					binding.listView.setAdapter(buyableItemAdapter);
					binding.listView.setOnItemClickListener(this);
					binding.listView.setVisibility(View.VISIBLE);
				}
				binding.progressBar.setVisibility(View.GONE);
				if(binding.fab != null)
				{
					binding.fab.attachToListView(useGridView? binding.gridView : binding.listView);
					binding.fab.show();
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
					Utility.displayToastMessage(this,
						String.format(
									getResources().getString(R.string.buy_drink_bought_drink),
									buyableItem.getName(),
									DECIMAL_FORMAT.format(buyableItem.getPrice())
							)
					);
					// Adjust the displayed balance to give an immediate user feedback
					if (user != null)
					{
						user.setBalance(user.getBalance() - buyableItem.getPrice());
					}
					if (multiUserMode)
					{
						Utility.startActivity(this, PickUsername.class);
						break;
					}
				}
				new LongRunningIORequest<User>(this, LongRunningIOTask.UPDATE_USER, api.getUser(userID));
				if(!buyableItem.getActive())
				{
					new LongRunningIORequest<List<Drink>>(this, LongRunningIOTask.GET_DRINKS, api.listDrinks());
				}
				break;
			}
			
			// Added money
			case ADD_MONEY:
			{
				final BuyableItem buyableItem = buyingItem.get();
				if (buyableItem != null)
				{
					buyingItem.set(null);
					Utility.displayToastMessage(this,
							String.format(
									getResources().getString(R.string.buy_drink_added_money),
									DECIMAL_FORMAT.format(-buyableItem.getPrice())
							)
					);
					// Adjust the displayed balance to give an immediate user feedback
					if (user != null)
					{
						user.setBalance(user.getBalance() - buyableItem.getPrice());
					}
				}
				new LongRunningIORequest<User>(this, LongRunningIOTask.UPDATE_USER, api.getUser(userID));
				break;
			}
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> adapterView, final View view, final int index, final long l)
	{
		if (index < 0 || isBuying.get())
		{
			Utility.displayToastMessage(this, getResources().getString(R.string.buy_drink_pending));
			return;
		}
		if (isBuying.compareAndSet(false, true))
		{
			final BuyableItem buyableItem = (BuyableItem) (useGridView ? binding.gridView.getItemAtPosition(index) : binding.listView.getAdapter().getItem(index));
			if (buyableItem != null)
			{
				buyingItem.set(buyableItem);
				setProgressBarIndeterminateVisibility(true);
				if(buyableItem.isDrink())
				{
					new LongRunningIORequest<Void>(this, LongRunningIOTask.BUY_DRINK, api.buy(userID, ((Drink)buyableItem).getId()));
				}
				else
				{
					new LongRunningIORequest<Void>(this, LongRunningIOTask.ADD_MONEY, api.deposit(userID, -buyableItem.getPrice()));
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
			Utility.loadBuyableItemImage(activity, icon, buyableItem, hostname);

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
			label.append(DECIMAL_FORMAT.format(-buyableItem.getPrice()));
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
	
	// the barcode scan result
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if(scanResult != null)
		{
			if(scanResult.getContents() != null)
			{
				System.err.println("Scanned barcode: " + scanResult.toString());
				new LongRunningIORequest<Void>(this, LongRunningIOTask.BUY_DRINK, api.buy_barcode(userID, scanResult.getContents()));
			}
		}
	}

	private class BuyableComparator implements Comparator<BuyableItem>
	{
		@Override
		public int compare(final BuyableItem buyableItem, final BuyableItem buyableItem2)
		{
			return (int) Math.round(buyableItem2.getPrice() * 100 - buyableItem.getPrice() * 100);
		}
	}
}
