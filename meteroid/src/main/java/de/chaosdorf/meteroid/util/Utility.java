package de.chaosdorf.meteroid.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;
import de.chaosdorf.meteroid.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

	public static Bitmap getGravatarBitbamp(final User user)
	{
		return getBitmapFromURL(getGravatarURL(user));
	}

	private static Bitmap getBitmapFromURL(final String src)
	{
		try
		{
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			return BitmapFactory.decodeStream(input);
		}
		catch (IOException ignored)
		{
		}
		return null;
	}

	private static String getGravatarURL(final User user)
	{
		final String email = user.getEmail().trim().toLowerCase();
		if (email == null || email.length() == 0)
		{
			return "http://www.gravatar.com/avatar/?f=y";
		}
		return ("http://www.gravatar.com/avatar/" + md5Hex(email));
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
