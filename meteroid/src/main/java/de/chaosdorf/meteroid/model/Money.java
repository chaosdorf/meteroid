package de.chaosdorf.meteroid.model;

public class Money implements BuyableItem
{
	private final String name;
	private final String logoUrl;

	private final double donationRecommendation;

	public Money(String name, String logoUrl, double donationRecommendation)
	{
		this.name = name;
		this.logoUrl = logoUrl;
		this.donationRecommendation = donationRecommendation;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getLogoUrl()
	{
		return logoUrl;
	}

	@Override
	public double getDonationRecommendation()
	{
		return donationRecommendation;
	}

	@Override
	public boolean isDrink()
	{
		return false;
	}
}
