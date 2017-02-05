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

package de.chaosdorf.meteroid.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
					jsonObject.getDouble("donation_recommendation")
		);
		}
		catch (JSONException ignored)
		{
			return null;
		}
	}
}
