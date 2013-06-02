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
					jsonObject.getString("logoUrl"),
					jsonObject.getDouble("bottleSize"),
					jsonObject.getString("caffeine"),
					jsonObject.getDouble("donationRecommendation"),
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
