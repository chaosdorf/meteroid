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

package de.chaosdorf.meteroid.util;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.chaosdorf.meteroid.R;
import de.chaosdorf.meteroid.imageloader.ImageLoaderSingleton;
import de.chaosdorf.meteroid.model.BuyableItem;
import de.chaosdorf.meteroid.model.User;

public class Utility
{
	public static void displayToastMessage(final Activity activity, final String message)
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
			}
		});
	}

	public static void startActivity(final Activity activity, Class<?> classType)
	{
		final Intent intent = new Intent(activity, classType);
		activity.startActivity(intent);
		activity.finish();
	}

	public static void resetUsername(final Activity activity)
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		prefs.edit().remove("userid").apply();
	}

	public static boolean toggleUseGridView(final Activity activity)
	{
		return Utility.toogleBooleanSharedPreference(activity, "use_grid_view", false, R.string.menu_use_grid_view_enabled, R.string.menu_use_grid_view_disabled);
	}

	public static boolean toggleMultiUserMode(final Activity activity)
	{
		return Utility.toogleBooleanSharedPreference(activity, "multi_user_mode", false, R.string.menu_multi_user_mode_enabled, R.string.menu_multi_user_mode_disabled);
	}

	private static boolean toogleBooleanSharedPreference(final Activity activity, final String prefName, final boolean defaultValue, final int enabledMessageID, final int disabledMessageID)
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		final boolean newState = !prefs.getBoolean(prefName, defaultValue);
		prefs.edit().putBoolean(prefName, newState).apply();
		displayToastMessage(activity, activity.getResources().getString(newState ? enabledMessageID : disabledMessageID));
		return newState;
	}

	public static void loadGravatarImage(final Activity activity, final ImageView icon, final User user)
	{
		String email = null;
		if (user != null)
		{
			email = user.getEmail();
		}
		if (email != null)
		{
			email = email.trim().toLowerCase();
		}
		if (email != null && email.length() > 0)
		{
			try
			{
				final URL url = new URL("http://www.gravatar.com/avatar/" + md5Hex(email) + "?d=404");
				ImageLoaderSingleton.getInstance(activity).displayImage(url, icon, ImageLoaderSingleton.getUserDefaultImage());
				return;
			}
			catch (MalformedURLException ignored)
			{
			}
		}
		icon.setImageBitmap(ImageLoaderSingleton.getUserDefaultImage());
	}

	public static void loadBuyableItemImage(final Activity activity, final ImageView icon, final BuyableItem buyableItem)
	{
		icon.setContentDescription(buyableItem.getName());
		if (buyableItem.isDrink())
		{
			final URL url;
			try
			{
				url = new URL(buyableItem.getLogoUrl());
				ImageLoaderSingleton.getInstance(activity).displayImage(url, icon, ImageLoaderSingleton.getDrinkDefaultImage());
			}
			catch (MalformedURLException ignored)
			{
				icon.setImageBitmap(ImageLoaderSingleton.getDrinkDefaultImage());
			}
			return;
		}
		final int iconID = activity.getResources().getIdentifier(buyableItem.getLogoUrl(), "drawable", activity.getPackageName());
		icon.setImageResource(iconID > 0 ? iconID : R.drawable.euro_5);
	}

	private static String arrayToHex(final byte[] array)
	{
		StringBuilder sb = new StringBuilder();
		for (final byte anArray : array)
		{
			sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
		}
		return sb.toString();
	}

	private static String md5Hex(final String message)
	{
		try
		{
			return arrayToHex(MessageDigest.getInstance("MD5").digest(message.getBytes("CP1252")));
		}
		catch (NoSuchAlgorithmException ignored)
		{
		}
		catch (UnsupportedEncodingException ignored)
		{
		}
		return null;
	}
}
