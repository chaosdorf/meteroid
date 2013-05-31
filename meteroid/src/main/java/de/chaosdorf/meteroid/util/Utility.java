package de.chaosdorf.meteroid.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

	public static void CopyStream(InputStream is, OutputStream os)
	{
		final int buffer_size = 1024;
		try
		{
			byte[] bytes = new byte[buffer_size];
			while (true)
			{
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
				{
					break;
				}
				os.write(bytes, 0, count);
			}
		}
		catch (Exception ex)
		{
			// do nothing
		}
	}

	public static List<User> parseAllUsersFromJSON(final String json)
	{
		final List<User> list = new ArrayList<User>();
		try
		{
			final JSONArray jsonArray = new JSONArray(json);
			for (int i = 0; i < jsonArray.length(); i++)
			{
				final User user = Utility.parseUserFromJSON(jsonArray.getJSONObject(i));
				if (user != null)
				{
					list.add(user);
				}
			}
		}
		catch (JSONException ignored)
		{
		}
		return list;
	}

	public static User parseUserFromJSON(final JSONObject jsonObject)
	{
		try
		{
			return new User(
					jsonObject.getInt("id"),
					jsonObject.getString("name"),
					jsonObject.getString("email"),
					jsonObject.getLong("balance_cents"),
					new Date(),
					new Date()
			);
		}
		catch (JSONException e)
		{
			return null;
		}
	}

	public static String getGravatarURL(final User user)
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
