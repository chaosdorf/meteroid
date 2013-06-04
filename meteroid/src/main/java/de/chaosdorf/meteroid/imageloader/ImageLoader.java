package de.chaosdorf.meteroid.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.chaosdorf.meteroid.R;

public class ImageLoader
{
	private final int REQUIRED_SIZE;

	private final Bitmap stubBitmap;

	private final MemoryCache memoryCache;
	private final FileCache fileCache;
	private final ExecutorService executorService;

	private final Handler handler;
	private final Map<ImageView, String> imageViews;

	public ImageLoader(final Context context, final int requiredSize)
	{
		REQUIRED_SIZE = requiredSize;

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = calculateInSampleSize(REQUIRED_SIZE, REQUIRED_SIZE);
		final Bitmap tmpBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.stub, options);
		stubBitmap = Bitmap.createScaledBitmap(tmpBitmap, REQUIRED_SIZE, REQUIRED_SIZE, true);

		memoryCache = new MemoryCache();
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);

		handler = new Handler();
		imageViews = new ConcurrentHashMap<ImageView, String>();
	}

	public void DisplayImage(String url, ImageView imageView)
	{
		final Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null)
		{
			imageView.setImageBitmap(bitmap);
		}
		else
		{
			if (url != null)
			{
				imageViews.put(imageView, url);
				queuePhoto(url, imageView);
			}
			imageView.setImageBitmap(stubBitmap);
		}
	}

	private void queuePhoto(String url, ImageView imageView)
	{
		final PhotoToLoad p = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(String url)
	{
		final File f = fileCache.getFile(url);

		// From SD cache
		Bitmap b = decodeFile(f);
		if (b != null)
		{
			return b;
		}

		// From web
		try
		{
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			copyStream(is, os);
			os.close();
			conn.disconnect();
			return decodeFile(f);
		}
		catch (Throwable ex)
		{
			ex.printStackTrace();
			if (ex instanceof OutOfMemoryError)
			{
				memoryCache.clear();
			}
			return null;
		}
	}

	private void copyStream(InputStream is, OutputStream os)
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

	// Decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f)
	{
		try
		{
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			FileInputStream stream1 = new FileInputStream(f);
			BitmapFactory.decodeStream(stream1, null, o);
			stream1.close();

			// Find the correct scale value. It should be the power of 2.
			int scale = calculateInSampleSize(o.outWidth, o.outHeight);

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			FileInputStream stream2 = new FileInputStream(f);
			Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
			stream2.close();
			return Bitmap.createScaledBitmap(bitmap, REQUIRED_SIZE, REQUIRED_SIZE, false);
		}
		catch (FileNotFoundException e)
		{
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
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

	boolean imageViewReused(PhotoToLoad photoToLoad)
	{
		final String tag = imageViews.get(photoToLoad.imageView);
		return (tag == null || !tag.equals(photoToLoad.url));
	}

	public void clearCache()
	{
		memoryCache.clear();
		fileCache.clear();
	}

	// Task for the queue
	private class PhotoToLoad
	{
		final public String url;
		final public ImageView imageView;

		public PhotoToLoad(String u, ImageView i)
		{
			url = u;
			imageView = i;
		}
	}

	class PhotosLoader implements Runnable
	{
		final PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad)
		{
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run()
		{
			try
			{
				if (imageViewReused(photoToLoad))
				{
					return;
				}
				Bitmap bmp = getBitmap(photoToLoad.url);
				memoryCache.put(photoToLoad.url, bmp);
				if (imageViewReused(photoToLoad))
				{
					return;
				}
				BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
				handler.post(bd);
			}
			catch (Throwable th)
			{
				th.printStackTrace();
			}
		}
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable
	{
		final Bitmap bitmap;
		final PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p)
		{
			bitmap = b;
			photoToLoad = p;
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
				photoToLoad.imageView.setImageBitmap(stubBitmap);
			}
		}
	}
}
