package de.chaosdorf.meteroid.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.chaosdorf.meteroid.model.User;

public class UserController
{
	public static List<User> parseAllUsersFromJSON(final String json)
	{
		final List<User> list = new ArrayList<User>();
		try
		{
			final JSONArray jsonArray = new JSONArray(json);
			for (int i = 0; i < jsonArray.length(); i++)
			{
				final User user = parseUserFromJSONObject(jsonArray.getJSONObject(i));
				if (user != null)
				{
					list.add(user);
				}
			}
		}
		catch (JSONException ignored)
		{
			// do nothing
		}
		return list;
	}

	public static User parseUserFromJSON(final String json)
	{
		try
		{
			return parseUserFromJSONObject(new JSONObject(json));
		}
		catch (JSONException e)
		{
			return null;
		}
	}

	private static User parseUserFromJSONObject(final JSONObject jsonObject)
	{
		try
		{
			return new User(
					jsonObject.getInt("id"),
					jsonObject.getString("name"),
					jsonObject.getString("email"),
					jsonObject.getLong("balance_cents"),
					new Date(),
					new Date()
			);
		}
		catch (JSONException e)
		{
			return null;
		}
	}
}
