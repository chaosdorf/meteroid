package de.chaosdorf.meteroid.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.chaosdorf.meteroid.model.Drink;

public class DrinkController
{
	public static List<Drink> getMoneyList()
	{
		final List<Drink> list = new ArrayList<Drink>();
		list.add(new Drink(0, "5 Euro", "euro_5", 0, "", -5, null, null));
		list.add(new Drink(0, "10 Euro", "euro_10", 0, "", -10, null, null));
		list.add(new Drink(0, "20 Euro", "euro_20", 0, "", -20, null, null));
		list.add(new Drink(0, "50 Euro", "euro_50", 0, "", -50, null, null));
		return list;
	}

	public static List<Drink> parseAllDrinksFromJSON(final String json)
	{
		final List<Drink> list = new ArrayList<Drink>();
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
