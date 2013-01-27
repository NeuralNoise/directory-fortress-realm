/*
 * Copyright (c) 2009-2013, JoshuaTree. All Rights Reserved.
 */

package us.jts.sentry.tomcat;

import us.jts.fortress.rbac.Session;

import java.util.HashMap;

/**
 *  Contains the Fortress RBAC session that has been created on behalf of an end user who has
 *  signed onto Tomcat system.  The session contains the User's active roles and other security attributes.
 *
 *
 *@author     Shawn McKinney
 */
public class TcPrincipal
		 implements java.security.Principal, java.io.Serializable
{
	private HashMap<String, Session> context;
	private String name;


	/**
	 *  Constructor for the TcPrincipal object.  Accepts a HashMap which
     *  contains the Fortress session.
	 *
	 *@param  name     contains the userId of User who signed onto Tomcat.
	 *@param  context  Instantiated HashMap that contains the User's Fortress session data.
	 */
	public TcPrincipal(String name, HashMap<String, Session> context)
	{
		if (context == null || name == null)
		{
			throw new NullPointerException(TcPrincipal.class.getName() + " Null Map passed to constructor");
		}
		this.context = context;
		this.name = name;
	}


	/**
	 *  Return the HashMap to the caller.  This HashMap contains the User's Fortress session data.
	 *
	 *@return    HashMap reference to security session data.
	 */
	public final HashMap<String, Session> getContext()
	{
		return context;
	}


	/**
	 *  Return the userId of the end User who has signed onto Tomcat and is represented by this principal object.
	 *
	 *@return    Contains the end userId.
	 */
	public final String getName()
	{
		return name;
	}


	/**
	 *  Set a new HashMap reference into this Principal object.
	 *
	 *@param  context  HashMap reference to security session data.
	 */
	public final void setContext(HashMap<String, Session> context)
	{
		this.context = context;
	}


	/**
	 *  This method will return a simple string containing the name of class concatenated with the userId.
	 *
	 *@return    Return the concatenated value of Principal class name + userId.
	 */
	public final String toString()
	{
		return (TcPrincipal.class.getName() + ":" + name);
	}


	/**
	 *  Determine if the caller supplied a reference to a security Principal that is equal to the current value.
	 *
	 *@param  o  Contains reference to the Principal.
	 *@return    true if the userId on both Principal objects is equal, false otherwise.
	 */
	public final boolean equals(Object o)
	{
		if (o == null)
		{
			return false;
		}
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof TcPrincipal))
		{
			return false;
		}
		TcPrincipal that = (TcPrincipal) o;

		if (this.getName().equals(that.getName()))
		{
			return true;
		}
		return false;
	}


	/**
	 *  Compute the hashcode for the current userId asserted into this Principal object.
	 *
	 *@return    Description of the Return Value
	 */
	public final int hashCode()
	{
		return name.hashCode();
	}
}
