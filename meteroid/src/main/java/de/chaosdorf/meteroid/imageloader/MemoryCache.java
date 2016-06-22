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

import android.graphics.Bitmap;
import android.util.Log;

import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MemoryCache
{
	private static final String TAG = "MemoryCache";

	// Last argument true for LRU ordering
	private final Map<String, Bitmap> cache = Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(10, 1.5f, true));

	private long size = 0; // Current allocated size
	private long limit = 1000000; // Max memory in bytes

	public MemoryCache()
	{
		// Use 25% of available heap size
		setLimit(Runtime.getRuntime().maxMemory() / 4);
	}

	public void setLimit(final long newLimit)
	{
		limit = newLimit;
		Log.i(TAG, "MemoryCache will use up to " + (limit / 1024. / 1024.) + " MB");
	}

	public Bitmap get(final URL url)
	{
		return cache.get(url.toString());
	}

	public void put(final URL url, final Bitmap bitmap)
	{
		final String id = url.toString();
		try
		{
			if (cache.containsKey(id))
			{
				size -= getSizeInBytes(cache.get(id));
			}
			cache.put(id, bitmap);
			size += getSizeInBytes(bitmap);
			checkSize();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	public void clear()
	{
		try
		{
			// NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78
			cache.clear();
			size = 0;
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
		}
	}

	private void checkSize()
	{
		Log.i(TAG, "cache size=" + size + " length=" + cache.size());
		if (size > limit)
		{
			// Least recently accessed item will be the first one iterated
			Iterator<Entry<String, Bitmap>> iter = cache.entrySet().iterator();
			while (iter.hasNext())
			{
				Entry<String, Bitmap> entry = iter.next();
				size -= getSizeInBytes(entry.getValue());
				iter.remove();
				if (size <= limit)
				{
					break;
				}
			}
			Log.i(TAG, "Clean cache. New size " + cache.size());
		}
	}

	private long getSizeInBytes(final Bitmap bitmap)
	{
		if (bitmap == null)
		{
			return 0;
		}
		return bitmap.getRowBytes() * bitmap.getHeight();
	}
}