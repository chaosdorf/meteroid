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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import androidx.core.util.Pair;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.material.datepicker.MaterialDatePicker;

import de.chaosdorf.meteroid.databinding.ActivityAuditsBinding;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOCallback;
import de.chaosdorf.meteroid.longrunningio.LongRunningIORequest;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOTask;
import de.chaosdorf.meteroid.model.Audit;
import de.chaosdorf.meteroid.model.AuditsInfo;
import de.chaosdorf.meteroid.model.Drink;
import de.chaosdorf.meteroid.util.Utility;

public class Audits extends MeteroidNetworkActivity implements LongRunningIOCallback
{
	private ActivityAuditsBinding binding;
	private Calendar fromCalendar;
	private Calendar untilCalendar;
	private List<Audit> audits = null;
	private HashMap<Integer, Drink> drinks = new HashMap<>();
	
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_audits);
		setSupportActionBar(binding.toolbar);
		binding.setDECIMALFORMAT(DECIMAL_FORMAT);
		fromCalendar = Calendar.getInstance();
		fromCalendar.add(Calendar.DAY_OF_MONTH, -14);
		untilCalendar = Calendar.getInstance();
		untilCalendar.add(Calendar.DAY_OF_MONTH, 1);
		binding.setFromCalendar(fromCalendar);
		binding.setUntilCalendar(untilCalendar);
		binding.setDATEFORMAT(DateFormat.getDateInstance());
		
		MaterialDatePicker<Pair<Long, Long>> datePicker = MaterialDatePicker
			.Builder
			.dateRangePicker()
			.setSelection(new Pair(
				fromCalendar.getTimeInMillis(), untilCalendar.getTimeInMillis()
			))
			.build();
		datePicker.addOnPositiveButtonClickListener(
			(Pair<Long, Long> s) -> this.onDateSelected(s)
		);
		binding.buttonModifyDate.setOnClickListener(v -> 
			datePicker.show(this.getSupportFragmentManager(), "picker")
		);
		
		binding.swiperefresh.setEnabled(true);
		binding.swiperefresh.setOnRefreshListener(() -> reload());
		
		reload();
	}
	
	public void onDateSelected(Pair<Long, Long> selection) {
		if(selection.first == null || selection.second == null) {
			return;
		}
		fromCalendar.setTimeInMillis(selection.first);
		fromCalendar.add(Calendar.DATE, 1);
		binding.setFromCalendar(fromCalendar);
		untilCalendar.setTimeInMillis(selection.second);
		untilCalendar.add(Calendar.DATE, 1);
		binding.setUntilCalendar(untilCalendar);
		reload();
	}
	
	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.audits, menu);
		return true;
	}
	
	public void reload()
	{
		binding.error.setVisibility(View.GONE);
		binding.auditsDisplay.setVisibility(View.GONE);
		binding.progressBar.setVisibility(View.VISIBLE);
		binding.swiperefresh.setRefreshing(true);
		new LongRunningIORequest<AuditsInfo>(
			this, LongRunningIOTask.GET_AUDITS, connection.getAPI().listAudits(
				config.userID != config.NO_USER_ID? config.userID : null,
				fromCalendar.get(Calendar.YEAR),
				fromCalendar.get(Calendar.MONTH) + 1,
				fromCalendar.get(Calendar.DATE), 
				untilCalendar.get(Calendar.YEAR),
				untilCalendar.get(Calendar.MONTH) + 1,
				untilCalendar.get(Calendar.DATE)
			)
		);
		new LongRunningIORequest<List<Drink>>(
			this,
			LongRunningIOTask.GET_DRINKS,
			connection.getAPI().listDrinks()
		);
	}
	
	@Override
	public void displayErrorMessage(final LongRunningIOTask task, final String message)
	{
		Utility.displayToastMessage(this, message);
		binding.error.setVisibility(View.VISIBLE);
		binding.progressBar.setVisibility(View.GONE);
		binding.swiperefresh.setRefreshing(false);
	}
	
	@Override
	public void processIOResult(final LongRunningIOTask task, final Object result)
	{
		if(task == LongRunningIOTask.GET_AUDITS) {
			final AuditsInfo info = (AuditsInfo) result;
			audits = info.getAudits();
			binding.setAuditsInfo(info);
			if(!drinks.isEmpty()) {
				// FIXME: This will fail if there are no drinks.
				completeFetch();
			}
		} else if(task == LongRunningIOTask.GET_DRINKS) {
			this.drinks.clear();
			for(Drink d : (List<Drink>) result) {
				this.drinks.put(d.getId(), d);
			}
			if(audits != null) {
				completeFetch();
			}
		}
	}
	
	private void completeFetch() {
		final AuditsAdapter auditsAdapter = new AuditsAdapter(audits, drinks);
		binding.listView.setAdapter(auditsAdapter);
		binding.progressBar.setVisibility(View.GONE);
		binding.auditsDisplay.setVisibility(View.VISIBLE);
		binding.swiperefresh.setRefreshing(false);
	}
	
	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				finish();
				break;
			case R.id.action_reload:
				reload();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onDestroy()
	{
		if(binding.listView != null)
		{
			binding.listView.setAdapter(null);
		}
		super.onDestroy();
	}
	
	private class AuditsAdapter extends ArrayAdapter<Audit>
	{
		private final List<Audit> auditList;
		private final Map<Integer, Drink> drinks;
		private final LayoutInflater inflater;
		private final DateFormat dateFormat = DateFormat.getDateTimeInstance();
		
		AuditsAdapter(final List<Audit> auditList, final Map<Integer, Drink> drinks)
		{
			super(activity, R.layout.activity_audits, auditList);
			this.auditList = auditList;
			this.drinks = drinks;
			this.inflater = activity.getLayoutInflater();
		}
		
		public View getView(
			final int position, final View convertView, final ViewGroup parent
		)
		{
			View view = convertView;
			if (view == null)
			{
				view = inflater.inflate(
					R.layout.activity_audits_item, parent, false
				);
			}
			if (view == null)
			{
				return null;
			}
			final Audit audit = auditList.get(position);
			TextView timestamp = view.findViewById(R.id.timestamp);
			timestamp.setText(dateFormat.format(audit.getCreatedAt()));
			TextView amount = view.findViewById(R.id.amount);
			amount.setText(DECIMAL_FORMAT.format(audit.getDifference()));
			TextView drinkView = view.findViewById(R.id.drink);
			Drink drink = drinks.get(audit.getDrink());
			drinkView.setText(drink == null? "n/a" : drink.getName());
			return view;
		}
	}
}
