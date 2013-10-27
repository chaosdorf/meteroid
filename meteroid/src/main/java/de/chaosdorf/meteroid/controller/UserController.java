package de.chaosdorf.meteroid.controller;

import org.apache.http.message.BasicNameValuePair;
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
			return list;
		}
		catch (JSONException ignored)
		{
			return null;
		}
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
					jsonObject.getDouble("balance"),
					new Date(),
					new Date()
			);
		}
		catch (JSONException ignored)
		{
			return null;
		}
	}

	public static List<BasicNameValuePair> userToPostParams(final User user)
	{
		List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		pairs.add(new BasicNameValuePair("user[name]", user.getName()));
		pairs.add(new BasicNameValuePair("user[email]", user.getEmail()));
		pairs.add(new BasicNameValuePair("user[balance]", String.valueOf(user.getBalance())));
		return pairs;
	}
}
