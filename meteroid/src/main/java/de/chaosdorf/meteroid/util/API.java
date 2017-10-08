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

package de.chaosdorf.meteroid.util;

import retrofit2.http.*;
import retrofit2.Call;

import java.util.List;

import de.chaosdorf.meteroid.model.*;

public interface API
{
	
	/*// list (some) audits
	@GET("audits.json")
	Call<List<Audit>> listAudits(
		@Query("start_date[year]") int fromYear,
		@Query("start_date[month]") int fromMonth,
		@Query("start_date[day]") int fromDay,
		@Query("end_date[year]") int toYear,
		@Query("end_date[month]") int toMonth,
		@Query("end_date[day]") int toDay
	);*/
	
	// list all drinks
	@GET("drinks.json")
	Call<List<Drink>> listDrinks();
	
	// retrieves information about a drinks
	@GET("drinks/{did}.json")
	Call<Drink> getDrink(@Path("did") int did);
	
	// TODO: create and modify a drink
	
	// deletes a drink
	@DELETE("drinks/{did}.json")
	Call<Void> deleteDrink(@Path("did") int did);
	
	// lists all users
	@GET("users.json")
	Call<List<User>> listUsers();
	
	// retrieves information about a user
	@GET("users/{uid}.json")
	Call<User> getUser(@Path("uid") int uid);
	
	// returns the defaults for creating new users
	@GET("users/new.json")
	Call<User> getUserDefaults();
	
	// creates a new user
	@POST("users.json")
	Call<User> createUser(
		@Body User user
	);
	
	// modifys an existing user
	@PATCH("users/{uid}.json")
	Call<Void> editUser(
		@Path("uid") int uid,
		@Body User user
	);
	
	// deletes an existing user
	@DELETE("users/{uid}.json")
	Call<Void> deleteUser(@Path("uid") int uid);
	
	// deposits money
	@GET("users/{uid}/deposit.json")
	Call<Void> deposit(
		@Path("uid") int uid,
		@Query("amount") double amount
	);
	
	// removes money from the balance
	@GET("users/{uid}/pay.json")
	Call<Void> pay(
		@Path("uid") int uid,
		@Query("amount") double amount
	);
	
	// buys a drink
	@GET("users/{uid}/buy.json")
	Call<Void> buy(
		@Path("uid") int uid,
		@Query("drink") int did
	);
	
	// buys a drink by barcode
	@FormUrlEncoded
	@POST("users/{uid}/buy_barcode.json")
	Call<Void> buy_barcode(
		@Path("uid") int uid,
		@Field("barcode") String barcode
	);
	
	/*// retrieves various statistics
	@GET("users/stats.json")
	Call<UsersStats> getUsersStats();*/

}