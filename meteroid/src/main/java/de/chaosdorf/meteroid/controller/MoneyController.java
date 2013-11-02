package de.chaosdorf.meteroid.controller;

import java.util.List;

import de.chaosdorf.meteroid.model.BuyableItem;
import de.chaosdorf.meteroid.model.Money;

public class MoneyController
{
	public static void addMoney(List<BuyableItem> itemList)
	{
		itemList.add(new Money("5 Euro", "euro_5", -5));
		itemList.add(new Money("10 Euro", "euro_10", -10));
		itemList.add(new Money("20 Euro", "euro_20", -20));
		itemList.add(new Money("50 Euro", "euro_50", -50));
	}
}
