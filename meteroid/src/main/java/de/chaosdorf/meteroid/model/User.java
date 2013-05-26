package de.chaosdorf.meteroid.model;

import java.util.Date;

public class User
{
	private final int id;
	private final String name;
	private final String email;
	private final long balance_cents;
	private final Date created_at;
	private final Date updated_at;

	public User(final int id, final String name, final String email, final long balance_cents, final Date created_at, final Date updated_at)
	{
		this.id = id;
		this.name = name;
		this.email = email;
		this.balance_cents = balance_cents;
		this.created_at = created_at;
		this.updated_at = updated_at;
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getEmail()
	{
		return email;
	}

	public long getBalance_cents()
	{
		return balance_cents;
	}

	public Date getCreated_at()
	{
		return created_at;
	}

	public Date getUpdated_at()
	{
		return updated_at;
	}
}
