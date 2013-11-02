package de.chaosdorf.meteroid.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.chaosdorf.meteroid.model.BuyableItem;
import de.chaosdorf.meteroid.model.Drink;

public class DrinkController
{
	public static List<BuyableItem> parseAllDrinksFromJSON(final String json)
	{
		final List<BuyableItem> list = new ArrayList<BuyableItem>();
		try
		{
			final JSONArray jsonArray = new JSONArray(json);
			for (int i = 0; i < jsonArray.length(); i++)
			{
				final Drink drink = parseDrinkFromJSONObject(jsonArray.getJSONObject(i));
				if (drink != null)
				{
					list.add(drink);
				}
			}
			return list;
		}
		catch (JSONException ignored)
		{
			return null;
		}
	}

	private static Drink parseDrinkFromJSONObject(final JSONObject jsonObject)
	{
		try
		{
			return new Drink(
					jsonObject.getInt("id"),
					jsonObject.getString("name"),
					jsonObject.getString("logo_url"),
					jsonObject.getDouble("bottle_size"),
					jsonObject.getString("caffeine"),
					jsonObject.getDouble("donation_recommendation"),
					new Date(),
					new Date()
			);
		}
		catch (JSONException ignored)
		{
			return null;
		}
	}
}
