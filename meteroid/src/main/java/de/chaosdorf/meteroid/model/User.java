package de.chaosdorf.meteroid.model;

import java.util.Date;

public class User
{
	private final int id;
	private final String name;
	private final String email;
	private final long balanceCents;
	private final Date createdAt;
	private final Date updatedAt;

	public User(final int id, final String name, final String email, final long balanceCents, final Date createdAt, final Date updated_at)
	{
		this.id = id;
		this.name = name;
		this.email = email;
		this.balanceCents = balanceCents;
		this.createdAt = createdAt;
		this.updatedAt = updated_at;
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

	public long getBalanceCents()
	{
		return balanceCents;
	}

	public Date getCreatedAt()
	{
		return createdAt;
	}

	public Date getUpdatedAt()
	{
		return updatedAt;
	}
}
