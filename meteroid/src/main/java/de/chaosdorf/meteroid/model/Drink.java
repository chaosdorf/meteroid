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

package de.chaosdorf.meteroid.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class Drink implements BuyableItem
{
	private final int id;
	private final String name;
	private final String logo_url;

	private final double bottle_size;
	private final String barcode;
	private final String caffeine;
	private final double price;
	private final boolean active;

	public Drink(final int id, final String name, final String logo_url, final double bottle_size, final String barcode, final String caffeine, final double price, final boolean active)
	{
		this.id = id;
		this.name = name;
		this.logo_url = logo_url;
		this.bottle_size = bottle_size;
		this.barcode = barcode;
		this.caffeine = caffeine;
		this.price = price;
		this.active = active;
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getLogoUrl(String hostname)
	{
		try
		{
			return createLogoURL(logo_url, new URL(hostname));
		}
		catch (MalformedURLException ignored)
		{
			return null;
		}
	}

	public double getBottleSize()
	{
		return bottle_size;
	}
	
	public String getBarcode()
	{
		return barcode;
	}

	public String getCaffeine()
	{
		return caffeine;
	}

	public double getPrice()
	{
		return price;
	}

	public boolean isDrink()
	{
		return true;
	}
	
	public boolean getActive()
	{
		return active;
	}

	private String createLogoURL(final String logoUrl, final URL baseURL) throws MalformedURLException
	{
		if (logoUrl.isEmpty())
		{
			return null;
		}
		return new URL(baseURL, logoUrl).toString();
	}
}
