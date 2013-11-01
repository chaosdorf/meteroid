package de.chaosdorf.meteroid.util;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.chaosdorf.meteroid.R;
import de.chaosdorf.meteroid.imageloader.ImageLoader;
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

	public static void resetHostname(final Activity activity)
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		prefs.edit().remove("hostname").apply();
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

	public static void loadGravatarImage(final ImageLoader imageLoader, final ImageView icon, final User user)
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
		if (email == null || email.length() == 0)
		{
			imageLoader.DisplayImage(null, icon);
		}
		else
		{
			imageLoader.DisplayImage("http://www.gravatar.com/avatar/" + md5Hex(email), icon);
		}
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
