package de.chaosdorf.meteroid.model;

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

	public Drink(final int id, final String name, final String logoUrl, final double bottleSize, final String caffeine, final double donationRecommendation, final Date created_at, final Date updated_at)
	{
		this.id = id;
		this.name = name;
		this.logoUrl = createLogoURL(logoUrl);
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

	private String createLogoURL(final String logoUrl)
	{
		if (logoUrl.isEmpty())
		{
			if (bottleSize == 0.5)
			{
				return "drink_0l5";
			}
			else
			{
				return "drink_0l33";
			}
		}
		else if (!logoUrl.startsWith("drink_"))
		{
			return "drink_" + logoUrl;
		}
		return logoUrl;
	}
}
