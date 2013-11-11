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