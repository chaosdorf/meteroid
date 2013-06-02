package de.chaosdorf.meteroid.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.chaosdorf.meteroid.R;
import de.chaosdorf.meteroid.model.Drink;
import de.chaosdorf.meteroid.model.User;

public class Utility
{
	public static void displayToastMessage(final Activity activity, final CharSequence message)
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

	public static void displayAlertDialog(final Activity activity, final CharSequence message)
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setMessage(message).setCancelable(false).setPositiveButton(
						activity.getResources().getText(R.string.ok),
						new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int id)
							{
								// do nothing
							}
						});
				builder.show();
			}
		});
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
