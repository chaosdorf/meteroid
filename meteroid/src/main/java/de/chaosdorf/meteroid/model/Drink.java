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

package de.chaosdorf.meteroid.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class Drink implements BuyableItem
{
	private final int id;
	private final String name;
	private final String logoUrl;

	private final double bottleSize;
	private final String caffeine;
	private final double donationRecommendation;

	private final Date createdAt;
	private final Date updatedAt;

	public Drink(final int id, final String name, final String logoUrl, final double bottleSize, final String caffeine, final double donationRecommendation, final Date created_at, final Date updated_at, final URL baseURL)
	{
		this.id = id;
		this.name = name;
		this.logoUrl = createLogoURL(logoUrl, baseURL);
		this.bottleSize = bottleSize;
		this.caffeine = caffeine;
		this.donationRecommendation = donationRecommendation;
		this.createdAt = created_at;
		this.updatedAt = updated_at;
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getLogoUrl()
	{
		return logoUrl;
	}

	public double getBottleSize()
	{
		return bottleSize;
	}

	public String getCaffeine()
	{
		return caffeine;
	}

	public double getDonationRecommendation()
	{
		return donationRecommendation;
	}

	public Date getCreatedAt()
	{
		return createdAt;
	}

	public Date getUpdatedAt()
	{
		return updatedAt;
	}

	public boolean isDrink()
	{
		return true;
	}

	private String createLogoURL(final String logoUrl, final URL baseURL)
	{
		if (logoUrl.isEmpty())
		{
			return null;
		}
		try
		{
			return new URL(baseURL, logoUrl).toString();
		}
		catch (MalformedURLException ignored)
		{
		}
		return null;
	}
}
