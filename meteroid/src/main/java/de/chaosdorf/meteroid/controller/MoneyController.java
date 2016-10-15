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

import java.util.List;

import de.chaosdorf.meteroid.model.BuyableItem;
import de.chaosdorf.meteroid.model.Money;

public class MoneyController
{
	public static void addMoney(List<BuyableItem> itemList)
	{
		itemList.add(new Money("50 Cent", "euro_05", -0.5));
		itemList.add(new Money("1 Euro", "euro_1", -1));
		itemList.add(new Money("2 Euro", "euro_2", -2));
		itemList.add(new Money("5 Euro", "euro_5", -5));
		itemList.add(new Money("10 Euro", "euro_10", -10));
		itemList.add(new Money("20 Euro", "euro_20", -20));
		itemList.add(new Money("50 Euro", "euro_50", -50));
	}
}
