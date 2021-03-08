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
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import com.squareup.picasso.Picasso;

import de.chaosdorf.meteroid.R;
import de.chaosdorf.meteroid.model.BuyableItem;
import de.chaosdorf.meteroid.model.User;

public class Utility
{
	public static String guessApiVersion(final String hostname)
	{
		if(hostname.contains("api/v1"))
		{
			return "v1";
		}
		else
		{
			return "legacy";
		}
	}
	
	public static void displayToastMessage(final Activity activity, final String message)
	{
		Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
	}

	public static void startActivity(final Activity activity, Class<?> classType)
	{
		startActivity(activity, classType, 0);
	}

	public static void startActivity(final Activity activity, Class<?> classType, int flags)
	{
		final Intent intent = new Intent(activity, classType);
		intent.addFlags(flags);
		activity.startActivity(intent);
	}

	public static boolean toggleUseGridView(final Activity activity)
	{
		Config config = Config.getInstance(activity);
		config.useGridView = !config.useGridView;
		config.save();
		final boolean newState = config.useGridView;
		displayToastMessage(activity, activity.getResources().getString(newState ? R.string.menu_use_grid_view_enabled : R.string.menu_use_grid_view_disabled));
		return newState;
	}

	public static boolean toggleMultiUserMode(final Activity activity)
	{
		Config config = Config.getInstance(activity);
		config.multiUserMode = !config.multiUserMode;
		config.save();
		final boolean newState = config.multiUserMode;
		displayToastMessage(activity, activity.getResources().getString(newState ? R.string.menu_multi_user_mode_enabled : R.string.menu_multi_user_mode_disabled));
		return newState;
	}

	public static void loadUserImage(final Activity activity, final ImageView icon, final User user)
	{
		loadGravatarImage(activity, icon, user);
		if(!user.getActive())
		{
			icon.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
		}
	}

	private static void loadGravatarImage(final Activity activity, final ImageView icon, final User user)
	{
		String email = null;
		Uri uri = null;
		if (user != null)
		{
			email = user.getEmail();
		}
		if (email != null)
		{
			email = email.trim().toLowerCase(Locale.US);
		}
		if (email != null && email.length() > 0)
		{
			uri = new Uri.Builder()
				.scheme("https")
				.authority("www.gravatar.com")
				.appendPath("avatar")
				.appendPath(md5Hex(email))
				.appendQueryParameter("d", "404")
				.build();
			Picasso.get().load(uri).placeholder(R.drawable.default_user).into(icon);
		}
		else
		{
			icon.setImageResource(R.drawable.default_user);
		}
	}

	public static void loadBuyableItemImage(final Activity activity, final ImageView icon, final BuyableItem buyableItem, final String hostname)
	{
		loadBuyableItemImage_(activity, icon, buyableItem, hostname);
		if(!buyableItem.getActive())
		{
			icon.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
		}
	}

	private static void loadBuyableItemImage_(final Activity activity, final ImageView icon, final BuyableItem buyableItem, final String hostname)
	{
		icon.setContentDescription(buyableItem.getName());
		Uri uri = null;
		if (buyableItem.isDrink())
		{
			uri = Uri.parse(buyableItem.getLogoUrl(hostname));
			Picasso.get().load(uri).error(R.drawable.default_drink).into(icon);
			return;
		}
		final int iconID = activity.getResources().getIdentifier(buyableItem.getLogoUrl(hostname), "drawable", activity.getPackageName());
		icon.setImageResource(iconID > 0 ? iconID : R.drawable.euro_5);
	}

	private static String arrayToHex(final byte[] array)
	{
		return String.format("%032x", new BigInteger(1, array));
	}

	private static String md5Hex(final String message)
	{
		try
		{
			return arrayToHex(MessageDigest.getInstance("MD5").digest(message.getBytes("CP1252")));
		}
		catch (NoSuchAlgorithmException | UnsupportedEncodingException exc)
		{
			throw new AssertionError("Can't happen: " + exc.toString(), exc);
		}
	}
}
