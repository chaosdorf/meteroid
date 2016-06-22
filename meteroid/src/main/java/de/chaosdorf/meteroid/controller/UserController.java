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

	public static String userToJSONPostParams(final User user)
	{
		final JSONObject jo = new JSONObject();
		try
		{
			JSONObject ujo = new JSONObject();
			ujo.put("name", user.getName());
			ujo.put("email", user.getEmail());
			ujo.put("balance", String.valueOf(user.getBalance()));
			jo.put("user", ujo);
		}
		catch (JSONException e){}
		return jo.toString();
	}
}
