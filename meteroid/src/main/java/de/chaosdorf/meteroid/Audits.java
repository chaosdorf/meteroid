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
import android.app.Activity;
import androidx.databinding.DataBindingUtil;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import de.chaosdorf.meteroid.databinding.ActivityAuditsBinding;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOCallback;
import de.chaosdorf.meteroid.longrunningio.LongRunningIORequest;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOTask;
import de.chaosdorf.meteroid.model.AuditsInfo;
import de.chaosdorf.meteroid.util.Utility;

public class Audits extends MeteroidNetworkActivity implements LongRunningIOCallback<AuditsInfo>
{
	private ActivityAuditsBinding binding;
	
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_audits);
		
		binding.buttonBack.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				finish();
			}
		});
		
		binding.buttonReload.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				reload();
			}
		});
		
		ActionBar actionBar = getActionBar();
		if(actionBar != null)
		{
			actionBar.setDisplayHomeAsUpEnabled(true);
			binding.buttonBack.setVisibility(View.GONE);
			binding.buttonReload.setVisibility(View.GONE);
		}
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			binding.swiperefresh.setEnabled(true);
			binding.swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
			{
				@Override
				public void onRefresh()
				{
					reload();
				}
			});
		}
		
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
		binding.progressBar.setVisibility(View.VISIBLE);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			binding.swiperefresh.setRefreshing(true);
		}
		new LongRunningIORequest<AuditsInfo>(
			this, LongRunningIOTask.GET_AUDITS, connection.getAPI().listAudits(
				config.userID, null, null, null, null, null, null
			)
		);
	}
	
	@Override
	public void displayErrorMessage(final LongRunningIOTask task, final String message)
	{
		Utility.displayToastMessage(this, message);
		binding.error.setVisibility(View.VISIBLE);
		binding.progressBar.setVisibility(View.GONE);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			binding.swiperefresh.setRefreshing(false);
		}
	}
	
	@Override
	public void processIOResult(final LongRunningIOTask task, final AuditsInfo result)
	{
		assert task == LongRunningIOTask.GET_AUDITS;
		// TODO: pass data to binding
		binding.progressBar.setVisibility(View.GONE);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			binding.swiperefresh.setRefreshing(false);
		}
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
}
