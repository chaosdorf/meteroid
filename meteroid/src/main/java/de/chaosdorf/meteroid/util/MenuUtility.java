package de.chaosdorf.meteroid.util;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

public class MenuUtility
{
	public static void setChecked(final Menu menu, final int itemID, final boolean status)
	{
		final MenuItem menuItem = menu.findItem(itemID);
		if (menuItem != null)
		{
			menuItem.setChecked(status);
		}
	}

	public static boolean onClickMultiUserMode(final Activity activity, final MenuItem item)
	{
		final boolean multiUserMode = Utility.toggleMultiUserMode(activity);
		item.setChecked(multiUserMode);
		if (multiUserMode)
		{
			Utility.resetUsername(activity);
		}
		return multiUserMode;
	}
}
