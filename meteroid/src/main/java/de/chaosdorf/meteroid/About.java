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

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MenuItem;
import android.view.View;

import com.artitk.licensefragment.ListViewLicenseFragment;
import com.artitk.licensefragment.model.CustomUI;
import com.artitk.licensefragment.model.License;
import com.artitk.licensefragment.model.LicenseID;
import com.artitk.licensefragment.model.LicenseType;

import de.chaosdorf.meteroid.databinding.ActivityAboutBinding;

public class About extends AppCompatActivity
{
	private ActivityAboutBinding binding;
	private ListViewLicenseFragment licenseFragment;
	private Vibrator vibrator;
	private final ObservableBoolean glassEmpty = new ObservableBoolean(false);
	private static final String FALLBACK_VERSION_NAME = "UNKNOWN";
	
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_about);
		binding.setGlassEmpty(glassEmpty);
		
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		
		String versionName = FALLBACK_VERSION_NAME;
		try
		{
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionName = info.versionName;
		}
		catch(NameNotFoundException ignored) {}
		binding.setVersionName(versionName);
		
		ActionBar actionBar = getSupportActionBar();
		if(actionBar != null)
		{
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		
		binding.appIcon.setOnLongClickListener(new View.OnLongClickListener()
		{
			private static final int VIBRATOR_DURATION = 500; // ms
			
			@Override
			public boolean onLongClick(View view)
			{
				if(vibrator != null && vibrator.hasVibrator())
				{
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
					{
						vibrator.vibrate(VibrationEffect.createOneShot(VIBRATOR_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
					}
					else
					{
						vibrator.vibrate(VIBRATOR_DURATION);
					}
				}
				glassEmpty.set(!glassEmpty.get());
				return true;
			}
		});
		licenseFragment = ListViewLicenseFragment.newInstance(new int[] {
			LicenseID.RETROFIT,
			LicenseID.GSON,
			LicenseID.PICASSO
		});
		ArrayList<License> licenseList = new ArrayList<>();
		licenseList.add(new License(
			this, "ZXing", LicenseType.APACHE_LICENSE_20,
			"2007-2020", "ZXing authors"
		));
		licenseList.add(new License(
			this, "Floating Action Button", LicenseType.APACHE_LICENSE_20,
			"2014", "ShamanLand.Com"
		));
		licenseList.add(new License(
			this, "Sly Calendar View", LicenseType.MIT_LICENSE,
			"2018", "psinetron, LuckyWins"
		));
		licenseList.add(new License(
			this, "Android Jetpack", LicenseType.APACHE_LICENSE_20,
			"2018", "The Android Open Source Project"
		));
		licenseList.add(new License(
			this, "Material Components for Android", LicenseType.APACHE_LICENSE_20,
			"2021", "The Android Open Source Project"
		));
		licenseFragment.addCustomLicense(licenseList);
		CustomUI customLicenseUI = new CustomUI()
			.setTitleBackgroundColor(
				getResources().getColor(R.color.colorPrimary)
			)
			.setTitleTextColor(
				getResources().getColor(android.R.color.primary_text_dark)
			)
			.setLicenseBackgroundColor(
				getResources().getColor(R.color.colorPrimaryDark)
			)
			.setLicenseTextColor(
				getResources().getColor(android.R.color.secondary_text_dark)
			);
		licenseFragment.setCustomUI(customLicenseUI);
		getFragmentManager()
			.beginTransaction()
			.add(R.id.license_fragment_container, licenseFragment)
			.commit();
	}
	
	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
