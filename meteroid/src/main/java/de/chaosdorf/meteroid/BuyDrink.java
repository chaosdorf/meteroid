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

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.Icon;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.shamanland.fab.FloatingActionButton;
import com.shamanland.fab.ShowHideOnScroll;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import de.chaosdorf.meteroid.controller.MeteroidAdapter;
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
	private final AtomicBoolean intentHandled = new AtomicBoolean(false);
	private final AtomicReference<BuyableItem> buyingItem = new AtomicReference<BuyableItem>(null);

	private User user;
	private ActivityBuyDrinkBinding binding;
	private ShortcutManager shortcutManager;
	private IntentIntegrator barcodeIntegrator;
	
	private static final String ACTION_BUY = "de.chaosdorf.meteroid.ACTION_BUY";
	private static final String ACTION_SCAN = "de.chaosdorf.meteroid.ACTION_SCAN";
	private static final String EXTRA_BUYABLE_ITEM_IS_DRINK = "de.chaosdorf.meteroid.EXTRA_BUYABLE_ITEM_IS_DRINK";
	private static final String EXTRA_BUYABLE_ITEM_ID = "de.chaosdorf.meteroid.EXTRA_BUYABLE_ITEM_ID";
	private static final String EXTRA_BUYABLE_ITEM_PRICE = "de.chaosdorf.meteroid.EXTRA_BUYABLE_ITEM_PRICE";

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_buy_drink);
		binding.setUser(user);
		binding.setDECIMALFORMAT(DECIMAL_FORMAT);

		barcodeIntegrator = new IntentIntegrator(this);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			binding.fab.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View view)
				{
					scanBarcode();
				}
			});
			binding.fab.setVisibility(View.VISIBLE);
		}
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
		{
			if(!config.multiUserMode)
			{
				shortcutManager = getSystemService(ShortcutManager.class);
				// make sure all pinned shortcuts are enabled as we have a current user
				List<ShortcutInfo> shortcuts = shortcutManager.getPinnedShortcuts();
				// if this were Java 8 I could use a stream :(
				List<String> shortcutIDs = new ArrayList<>();
				for(ShortcutInfo shortcut : shortcuts) {
					shortcutIDs.add(shortcut.getId());
				}
				shortcutManager.enableShortcuts(shortcutIDs);
			}
		}
		
		reload();
	}
	
	private void handleIntent(List<BuyableItem> buyableItemList)
	{
		if (intentHandled.compareAndSet(false, true))
		{
			Intent intent = getIntent();
			if(intent != null)
			{
				String action = intent.getAction();
				if(action != null)
				{
					if(action.equals(ACTION_BUY)) // shortcut
					{
						handleBuyIntent(buyableItemList, intent);
					}
					else if(action.equals(ACTION_SCAN))
					{
						scanBarcode();
					}
				}
			}
		}
	}
	
	private void handleBuyIntent(List<BuyableItem> buyableItemList, Intent intent)
	{
		BuyableItem itemSelected = null;
		if(intent.getBooleanExtra(EXTRA_BUYABLE_ITEM_IS_DRINK, false))
		{
			int id = intent.getIntExtra(EXTRA_BUYABLE_ITEM_ID, -1);
			if(id == -1)
			{
				Utility.displayToastMessage(this, getResources().getString(R.string.buy_drink_invalid_intent));
				return;
			}
			for(BuyableItem item: buyableItemList)
			{
				if(item.isDrink())
				{
					if(((Drink)item).getId() == id) {
						itemSelected = item;
						break;
					}
				}
			}
		}
		else
		{
			double price = intent.getDoubleExtra(EXTRA_BUYABLE_ITEM_PRICE, 0.0);
			if(price == 0.0)
			{
				Utility.displayToastMessage(this, getResources().getString(R.string.buy_drink_invalid_intent));
				return;
			}
			for(BuyableItem item: buyableItemList)
			{
				if(!item.isDrink())
				{
					if(item.getPrice() == price)
					{
						itemSelected = item;
						break;
					}
				}
			}
		}
		if(itemSelected == null)
		{
			Utility.displayToastMessage(this, getResources().getString(R.string.buy_drink_invalid_intent));
			return;
		}
		buy(itemSelected);
	}
	
	private void buy(BuyableItem buyableItem)
	{
		buyingItem.set(buyableItem);
		setProgressBarIndeterminateVisibility(true);
		if(buyableItem.isDrink())
		{
			new LongRunningIORequest<Void>(this, LongRunningIOTask.BUY_DRINK, connection.getAPI().buy(config.userID, ((Drink)buyableItem).getId()));
		}
		else
		{
			new LongRunningIORequest<Void>(this, LongRunningIOTask.ADD_MONEY, connection.getAPI().deposit(config.userID, -buyableItem.getPrice()));
		}
		
		if(shortcutManager != null)
		{
			ShortcutInfo shortcut = shortcutForItem(buyableItem);
			updateShortcuts(shortcut);
			shortcutManager.reportShortcutUsed(shortcut.getId());
		}
	}
	
	private void scanBarcode()
	{
		if(shortcutManager != null)
		{
			Intent intent = new Intent(this, this.getClass());
			intent.setAction(ACTION_SCAN);
			ShortcutInfo shortcut = new ShortcutInfo.Builder(this, "barcode")
				.setShortLabel(getResources().getString(R.string.shortcut_scan))
				.setIcon(Icon.createWithResource(this, R.drawable.button_barcode))
				.setIntent(intent)
				.build();
			updateShortcuts(shortcut);
			shortcutManager.reportShortcutUsed(shortcut.getId());
		}
		barcodeIntegrator.initiateScan();
	}
	
	private ShortcutInfo shortcutForItem(BuyableItem item)
	{
		String id = null;
		Intent intent = new Intent(this, this.getClass());
		intent.setAction(ACTION_BUY);
		intent.putExtra(EXTRA_BUYABLE_ITEM_IS_DRINK, item.isDrink());
		if(item.isDrink())
		{
			Drink drink = (Drink)item;
			id = "d" + drink.getId();
			intent.putExtra(EXTRA_BUYABLE_ITEM_ID, drink.getId());
		}
		else
		{
			id = "m" + item.getLogoUrl(null);
			intent.putExtra(EXTRA_BUYABLE_ITEM_PRICE, item.getPrice());
		}
		return new ShortcutInfo.Builder(this, id)
			.setShortLabel(item.getName())
			.setIcon(Icon.createWithResource(this, R.drawable.default_drink)) // TODO
			.setIntent(intent)
			.build();
	}
	
	/**
	* Updates the dynamic shortcuts.
	* If a shortcut with this ID does not yet exist, it is added and some other one is removed.
	* If a shortcut with this ID does already exist, nothing happens.
	*/
	@TargetApi(Build.VERSION_CODES.N_MR1)
	private void updateShortcuts(ShortcutInfo shortcut) {
		assert shortcutManager != null;
		List<ShortcutInfo> shortcuts = shortcutManager.getDynamicShortcuts();
		for(ShortcutInfo current : shortcuts) {
			if(current.getId().equals(shortcut.getId())) {
				// nothing to do here
				return;
			}
		}
		
		// Do we have enough space to add another one?
		// getMaxShortcutCountPerActivity gives us the upper bound of how many shortcuts we can set.
		// But not every launcher can display as much. WTF.
		// At least the AOSP launcher can display 4. Let's hope that others can do the same.
		int maxShortcuts = Math.min(shortcutManager.getMaxShortcutCountPerActivity(), 4);
		if(shortcuts.size() >= maxShortcuts) {
			// if not, we have to remove one first
			// pick the last one per default - it's the one we put there the first, I think
			String idToRemove = shortcuts.get(shortcuts.size() - 1).getId();
			shortcutManager.removeDynamicShortcuts(Arrays.asList(idToRemove));
		}
		// then add our new one
		shortcutManager.addDynamicShortcuts(Arrays.asList(shortcut));
	}
	
	public void reload()
	{
		binding.gridView.setVisibility(View.GONE);
		binding.listView.setVisibility(View.GONE);
		binding.buyDrinkError.setVisibility(View.GONE);
		binding.progressBar.setVisibility(View.VISIBLE);
		new LongRunningIORequest<User>(this, LongRunningIOTask.GET_USER, connection.getAPI().getUser(config.userID));
		new LongRunningIORequest<List<Drink>>(this, LongRunningIOTask.GET_DRINKS, connection.getAPI().listDrinks());
	}
	
	private void pickUsername()
	{
		config.userID = config.NO_USER_ID;
		config.save();
		if(shortcutManager != null) {
			// if we have no user, we can't buy drinks
			// removing dynamic shortcuts is easy
			shortcutManager.removeAllDynamicShortcuts();
			// but if the user pinned them, that's going to be a bit more difficult
			List<ShortcutInfo> shortcuts = shortcutManager.getPinnedShortcuts();
			// if this were Java 8 I could use a stream :(
			List<String> shortcutIDs = new ArrayList<>();
			for(ShortcutInfo shortcut : shortcuts) {
				shortcutIDs.add(shortcut.getId());
			}
			shortcutManager.disableShortcuts(shortcutIDs);
		}
		if(!config.multiUserMode)
		{
			Utility.startActivity(this, PickUsername.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.buydrink, menu);
		MenuUtility.setChecked(menu, R.id.use_grid_view, config.useGridView);
		MenuUtility.setChecked(menu, R.id.multi_user_mode, config.multiUserMode);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				pickUsername();
				break;
			case R.id.action_reload:
				reload();
				break;
			case R.id.action_edit:
				Utility.startActivity(this, UserSettings.class);
				break;
			case R.id.action_barcode:
				scanBarcode();
				break;
			case R.id.edit_hostname:
				Utility.startActivity(this, SetHostname.class);
				break;
			case R.id.reset_username:
				pickUsername();
				break;
			case R.id.use_grid_view:
				Utility.toggleUseGridView(this);
				item.setChecked(config.useGridView);
				recreate();
				break;
			case R.id.about:
				Utility.startActivity(this, About.class);
				break;
		}
		return super.onOptionsItemSelected(item);
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

				final BuyableItemAdapter buyableItemAdapter = new BuyableItemAdapter(buyableItemList);
				if (config.useGridView)
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
					(config.useGridView? binding.gridView : binding.listView)
					.setOnTouchListener(new ShowHideOnScroll(binding.fab));
				}
				handleIntent(buyableItemList);
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
					if (config.multiUserMode && user.getRedirect())
					{
						pickUsername();
						break;
					}
					if(!buyableItem.getActive())
					{
						new LongRunningIORequest<List<Drink>>(this, LongRunningIOTask.GET_DRINKS, connection.getAPI().listDrinks());
					}
				}
				new LongRunningIORequest<User>(this, LongRunningIOTask.UPDATE_USER, connection.getAPI().getUser(config.userID));
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
				new LongRunningIORequest<User>(this, LongRunningIOTask.UPDATE_USER, connection.getAPI().getUser(config.userID));
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
			final BuyableItem buyableItem = (BuyableItem) (config.useGridView ? binding.gridView.getItemAtPosition(index) : binding.listView.getAdapter().getItem(index));
			if (buyableItem != null)
			{
				buy(buyableItem);
			} else {
				isBuying.set(false);
				System.err.println("Touched item was null, ignoring.");
			}
		}
	}

	private class BuyableItemAdapter extends MeteroidAdapter<BuyableItem>
	{
		private final List<BuyableItem> drinkList;
		private final LayoutInflater inflater;

		BuyableItemAdapter(final List<BuyableItem> drinkList)
		{
			super(activity, R.layout.activity_buy_drink, drinkList);
			this.drinkList = drinkList;
			this.inflater = activity.getLayoutInflater();
		}

		public View getView(final int position, final View convertView, final ViewGroup parent)
		{
			View view = convertView;
			if (view == null)
			{
				view = inflater.inflate(config.useGridView ? R.layout.activity_buy_drink_item_gridview : R.layout.activity_buy_drink_item, parent, false);
			}
			if (view == null)
			{
				return null;
			}

			final BuyableItem buyableItem = drinkList.get(position);

			final ImageView icon = (ImageView) view.findViewById(R.id.icon);
			Utility.loadBuyableItemImage(activity, icon, buyableItem, config.hostname);

			final TextView label = (TextView) view.findViewById(R.id.label);
			label.setText(createLabel(buyableItem, config.useGridView));

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
				new LongRunningIORequest<Void>(this, LongRunningIOTask.BUY_DRINK, connection.getAPI().buy_barcode(config.userID, scanResult.getContents()));
			}
		}
	}

}
