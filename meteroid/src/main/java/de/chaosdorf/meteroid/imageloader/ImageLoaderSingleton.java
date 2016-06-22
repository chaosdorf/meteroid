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

package de.chaosdorf.meteroid.imageloader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

import java.util.concurrent.TimeUnit;

import de.chaosdorf.meteroid.R;

public class ImageLoaderSingleton
{
	private static final long REFRESH_INTERVAL = TimeUnit.MINUTES.toNanos(15);

	private static ImageLoader instance = null;
	private static long refreshTimestamp = System.nanoTime();

	private static Bitmap userDefaultImage = null;
	private static Bitmap drinkDefaultImage = null;

	public static ImageLoader getInstance(final Activity activity)
	{
		if (instance == null)
		{
			ensureInstance(activity);
		}
		if (refreshTimestamp < System.nanoTime())
		{
			refreshTimestamp = System.nanoTime() + REFRESH_INTERVAL;
			instance.clearCache();
		}
		return instance;
	}

	public static Bitmap getUserDefaultImage()
	{
		return userDefaultImage;
	}

	public static Bitmap getDrinkDefaultImage()
	{
		return drinkDefaultImage;
	}

	private static synchronized void ensureInstance(final Activity activity)
	{
		if (instance == null)
		{
			if (activity == null)
			{
				return;
			}
			final Context context = activity.getApplicationContext();
			if (context == null)
			{
				return;
			}

			// Set default images
			userDefaultImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_user);
			drinkDefaultImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_drink);

			// Set instance of ImageLoader
			instance = new ImageLoader(context, 80);
		}
	}
}
