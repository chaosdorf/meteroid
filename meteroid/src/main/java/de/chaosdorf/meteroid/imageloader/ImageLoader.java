/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Chaosdorf e.V.
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader
{
	private final int REQUIRED_SIZE;

	private final MemoryCache memoryCache;
	private final FileCache fileCache;
	private final ExecutorService executorService;

	private final Handler handler;
	private final Map<ImageView, String> imageViews;

	public ImageLoader(final Context context, final int requiredSize)
	{
		REQUIRED_SIZE = requiredSize;

		memoryCache = new MemoryCache();
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);

		handler = new Handler();
		imageViews = new ConcurrentHashMap<ImageView, String>();
	}

	public void displayImage(final String url, final ImageView imageView, final Bitmap defaultBitmap)
	{
		if (url == null)
		{
			imageView.setImageBitmap(defaultBitmap);
			return;
		}
		final Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null)
		{
			imageView.setImageBitmap(bitmap);
		}
		else
		{
			imageViews.put(imageView, url);
			queuePhoto(url, imageView, defaultBitmap);
		}
	}

	public void clearCache()
	{
		memoryCache.clear();
		fileCache.clear();
	}

	private void queuePhoto(final String url, final ImageView imageView, final Bitmap defaultBitmap)
	{
		final PhotoToLoad p = new PhotoToLoad(url, imageView, defaultBitmap);
		executorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(final String url)
	{
		// Create file from URL
		final File file = fileCache.getFile(url);

		// Read bitmap from SD
		if (file.exists())
		{
			final Bitmap bitmap = decodeFile(file);
			if (bitmap != null)
			{
				return bitmap;
			}
		}

		// Read bitmap from web
		try
		{
			final URL imageUrl = new URL(url);
			final HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			conn.connect();
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
			{
				return null;
			}
			final InputStream is = conn.getInputStream();
			final OutputStream os = new FileOutputStream(file);
			copyStream(is, os);
			os.close();
			is.close();
			conn.disconnect();
			return decodeFile(file);
		}
		catch (Throwable t)
		{
			if (t instanceof OutOfMemoryError)
			{
				memoryCache.clear();
				fileCache.clear();
			}
			t.printStackTrace();
		}
		return null;
	}

	private void copyStream(final InputStream is, final OutputStream os)
	{
		final int buffer_size = 1024;
		final byte[] bytes = new byte[buffer_size];
		try
		{
			while (true)
			{
				final int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
				{
					break;
				}
				os.write(bytes, 0, count);
			}
		}
		catch (Exception ignored)
		{
		}
	}

	// Decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(final File file)
	{
		// Decode image size
		final BitmapFactory.Options options1 = new BitmapFactory.Options();
		options1.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file.getAbsolutePath(), options1);

		// Find the correct scale value. It should be the power of 2.
		final int scale = calculateInSampleSize(options1.outWidth, options1.outHeight);

		// Decode with inSampleSize
		final BitmapFactory.Options options2 = new BitmapFactory.Options();
		options2.inSampleSize = scale;
		final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options2);
		if (bitmap == null)
		{
			return null;
		}
		return Bitmap.createScaledBitmap(bitmap, REQUIRED_SIZE, REQUIRED_SIZE, false);
	}

	private int calculateInSampleSize(int width, int height)
	{
		int scale = 1;
		while (true)
		{
			if (width / 2 < REQUIRED_SIZE && height / 2 < REQUIRED_SIZE)
			{
				break;
			}
			width /= 2;
			height /= 2;
			scale *= 2;
		}
		return scale;
	}

	private boolean imageViewReused(final PhotoToLoad photoToLoad)
	{
		final String tag = imageViews.get(photoToLoad.imageView);
		return (tag == null || !tag.equals(photoToLoad.url));
	}

	// Task for the queue
	private class PhotoToLoad
	{
		final public String url;
		final public ImageView imageView;
		final public Bitmap defaultImage;

		public PhotoToLoad(final String url, final ImageView imageView, final Bitmap defaultImage)
		{
			this.url = url;
			this.imageView = imageView;
			this.defaultImage = defaultImage;
		}
	}

	private class PhotosLoader implements Runnable
	{
		final PhotoToLoad photoToLoad;

		PhotosLoader(final PhotoToLoad photoToLoad)
		{
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run()
		{
			Bitmap bmp = null;
			try
			{
				if (imageViewReused(photoToLoad))
				{
					return;
				}
				bmp = getBitmap(photoToLoad.url);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
			finally
			{
				if (bmp == null)
				{
					bmp = photoToLoad.defaultImage;
				}
				memoryCache.put(photoToLoad.url, bmp);
				if (!imageViewReused(photoToLoad))
				{
					handler.post(new BitmapDisplayer(bmp, photoToLoad));
				}
			}
		}
	}

	// Used to display bitmap in the UI thread
	private class BitmapDisplayer implements Runnable
	{
		final Bitmap bitmap;
		final PhotoToLoad photoToLoad;

		public BitmapDisplayer(final Bitmap bitmap, final PhotoToLoad photoToLoad)
		{
			this.bitmap = bitmap;
			this.photoToLoad = photoToLoad;
		}

		public void run()
		{
			if (imageViewReused(photoToLoad))
			{
				return;
			}
			if (bitmap != null)
			{
				photoToLoad.imageView.setImageBitmap(bitmap);
			}
			else
			{
				photoToLoad.imageView.setImageBitmap(photoToLoad.defaultImage);
			}
		}
	}
}
