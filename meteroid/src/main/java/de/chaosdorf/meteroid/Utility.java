package de.chaosdorf.meteroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class Utility
{
	public static void displayErrorMessage(CharSequence message, Context context)
	{
		// display error message
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message).setCancelable(false).setPositiveButton(
				context.getResources().getText(R.string.ok),
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						// do nothing
					}
				});
		builder.show();
	}
}
