package de.chaosdorf.meteroid.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;
import de.chaosdorf.meteroid.R;

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
}
