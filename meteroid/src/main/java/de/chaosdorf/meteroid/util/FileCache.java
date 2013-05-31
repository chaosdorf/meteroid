package de.chaosdorf.meteroid.util;

import android.content.Context;

import java.io.File;

public class FileCache
{
	private File cacheDir;

	public FileCache(Context context)
	{
		// Find the dir to save cached images
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
		{
			cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "meteroid");
		}
		else
		{
			cacheDir = context.getCacheDir();
		}
		if (cacheDir != null && !cacheDir.exists())
		{
			cacheDir.mkdirs();
		}
	}

	public File getFile(String url)
	{
		// I identify images by hashcode. Not a perfect solution, good for the demo.
		// Another possible solution (thanks to grantland)
		// String filename = URLEncoder.encode(url);
		String filename = String.valueOf(url.hashCode());
		return new File(cacheDir, filename);
	}

	public void clear()
	{
		File[] files = cacheDir.listFiles();
		if (files == null)
		{
			return;
		}
		for (File f : files)
		{
			f.delete();
		}
	}
}