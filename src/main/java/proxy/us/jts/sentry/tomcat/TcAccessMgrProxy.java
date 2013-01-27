/*
 * Copyright (c) 2009-2013, JoshuaTree. All Rights Reserved.
 */

package us.jts.sentry.tomcat;

import us.jts.sentry.util.CpUtil;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.realm.RealmBase;

import java.security.Principal;
import java.net.URLClassLoader;
import java.util.logging.Logger;

import us.jts.sentry.util.ChildFirstUrlClassLoader;

/**
 * This class extends the Tomcat 4-6.x RealmBase class and provides Java EE security sevices within the Tomcat container.
 * This class is a "proxy" for the {@link us.jts.sentry.tomcat.TcAccessMgrImpl} class which isolates dependencies from the Tomcat
 * runtime environment by loading the implementation on a URLClassLoader.
 *
 * @author Shawn McKinney
 */
public class TcAccessMgrProxy extends RealmBase
{
    private static final String CLS_NM = TcAccessMgrProxy.class.getName();
    private static final Logger log = Logger.getLogger(CLS_NM);
    private static final String REALM_IMPL = "us.jts.sentry.tomcat.TcAccessMgrImpl";
    private static final String REALM_CLASSPATH = "REALM_CLASSPATH";
    private static final String JBOSS_AGENT = "jboss";
    private static String container = "Catalina";
    private String realmClasspath;
    private TcAccessMgr realm;

    /**
     * Gets the containerType attribute of the TcAccessMgrProxy object
     *
     * @return The containerType value
     */
    public String getContainerType()
    {
        return container;
    }


    /**
     * Sets the containerType attribute of the TcAccessMgrProxy object
     *
     * @param container The new containerType value
     */
    public void setContainerType(String container)
    {
        log.info(CLS_NM + ".setContainerType <" + container + ">");
        this.container = container;
    }


    /**
     * Gets the realmClasspath attribute of the TcAccessMgrProxy object
     *
     * @return The realmClasspath value
     */
    public String getRealmClasspath()
    {
        log.info(CLS_NM + ".getRealmClasspath <" + realmClasspath + ">");
        return realmClasspath;
    }


    /**
     * Sets the realmClasspath attribute of the TcAccessMgrProxy object
     *
     * @param rCpth The new realmClasspath value
     */
    public void setRealmClasspath(String rCpth)
    {
        log.info(CLS_NM + ".setRealmClasspath <" + rCpth + ">");
        this.realmClasspath = rCpth;
    }


    /**
     * This method will load the Fortress Tomcat implementation on a URL classloader.  Methods on the implementation are
     * wrapped by methods on this class and are accessed via the {@code realm} instance variable of this class.
     */
    private void initialize()
    {
        try
        {
            URLClassLoader ucl = null;
            if (container.equalsIgnoreCase(JBOSS_AGENT))
            {
                log.info(CLS_NM + ".initialize JBoss policy agent");
                ucl = new us.jts.sentry.util.ChildFirstUrlClassLoader(CpUtil.getRealmClasspath(REALM_CLASSPATH), this.getClass().getClassLoader());
            }
            else
            {
                log.info(CLS_NM + ".initialize Tomcat policy agent");
                if (realmClasspath != null && realmClasspath.length() > 0)
                {
                    ucl = new URLClassLoader(CpUtil.parseRealmClasspath(realmClasspath), this.getClass().getClassLoader());
                }
                else
                {
                    ucl = new URLClassLoader(CpUtil.getRealmClasspath(REALM_CLASSPATH), this.getClass().getClassLoader());
                }
            }
            log.info(CLS_NM + ".initialize - instantiate policy agent name: " + REALM_IMPL);
            Class sc = ucl.loadClass(REALM_IMPL);
            realm = (TcAccessMgr) sc.newInstance();
            log.info(CLS_NM + " J2EE policy agent initialization successful");
        }
        catch (java.lang.ClassNotFoundException e)
        {
            String error = CLS_NM + ".initialize caught java.lang.ClassNotFoundException=" + e.toString();
            log.severe(error);
            throw new java.lang.RuntimeException(error, e);
        }
        catch (java.lang.InstantiationException ie)
        {
            String error = CLS_NM + ".initialize caught java.lang.InstantiationException=" + ie.toString();
            log.severe(error);
            throw new java.lang.RuntimeException(error, ie);
        }
        catch (java.lang.IllegalAccessException iae)
        {
            String error = CLS_NM + ".initialize caught java.lang.IllegalAccessException=" + iae.toString();
            log.severe(error);
            throw new java.lang.RuntimeException(error, iae);
        }
    }


    /**
     * Gets the info attribute of the TcAccessMgrProxy object
     *
     * @return The info value
     */
    @Override
    public String getInfo()
    {
        return info;
    }


    /**
     * Perform user authentication and evaluate password policies.
     *
     * @param userId   Contains the userid of the user signing on.
     * @param password Contains the user's password.
     * @return Principal which contains the Fortress RBAC session data.
     */
    @Override
    public Principal authenticate(String userId, String password)
    {
        if(realm == null)
        {
            throw new RuntimeException(CLS_NM + "authenticate detected Fortress Tomcat Realm not initialized correctly.  Check your Fortress Realm configuration");
        }
        return realm.authenticate(userId, password.toCharArray());
    }


    /**
     * Determine if given Role is contained within User's Tomcat Principal object.  This method does not need to hit
     * the ldap server as the User's activated Roles are loaded into {@link us.jts.sentry.tomcat.TcPrincipal#setContext(java.util.HashMap)}
     *
     * @param principal Contains User's Tomcat RBAC Session data that includes activated Roles.
     * @param role  Maps to {@code us.jts.fortress.rbac.Role#name}.
     * @return True if Role is found in TcPrincipal, false otherwise.
     */
    public boolean hasRole(Principal principal, String role)
    {
        if(realm == null)
        {
            throw new RuntimeException(CLS_NM + "authenticate detected Fortress Tomcat Realm not initialized correctly.  Check your Fortress Realm configuration");
        }
        return realm.hasRole(principal, role);
    }


    /**
     * Gets the name attribute of the TcAccessMgrProxy object
     *
     * @return The name value
     */
    @Override
    protected String getName()
    {
        return (CLS_NM);
    }


    /**
     * Gets the password attribute of the TcAccessMgrProxy object
     *
     * @param username Description of the Parameter
     * @return The password value
     */
    @Override
    protected String getPassword(String username)
    {
        return (null);
    }


    /**
     * Gets the principal attribute of the TcAccessMgrProxy object
     *
     * @param username Description of the Parameter
     * @return The principal value
     */
    @Override
    protected Principal getPrincipal(String username)
    {
        return (null);
    }


    /**
     * This is the Tomcat 4 - 6 way of initializing the sentry component.
     *
     * @throws LifecycleException Description of the Exception
     */
    @Override
    public synchronized void start()
        throws LifecycleException
    {
        try
        {
            initialize();
            StandardServer server = (StandardServer) ServerFactory.getServer();
        }
        catch (Throwable e)
        {
            String error = CLS_NM + ".start caught Throwable=" + e;
            log.severe(error);
            e.printStackTrace();
            throw new LifecycleException(error);
        }
        super.start();
    }


    /**
     * This is the Tomcat 4 - 6 way of stopping the sentry component.
     *
     * @throws LifecycleException Description of the Exception
     */
    @Override
	public synchronized void stop()
		throws LifecycleException
	{
        realm = null;
		super.stop();
	}
}