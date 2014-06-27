/*
 * This work is part of OpenLDAP Software <http://www.openldap.org/>.
 *
 * Copyright 1998-2014 The OpenLDAP Foundation.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted only as authorized by the OpenLDAP
 * Public License.
 *
 * A copy of this license is available in the file LICENSE in the
 * top-level directory of the distribution or, alternatively, at
 * <http://www.OpenLDAP.org/license.html>.
 */

package org.openldap.fortress.rbac;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openldap.fortress.util.attr.VUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openldap.fortress.AccelMgr;
import org.openldap.fortress.AccelMgrFactory;
import org.openldap.fortress.GlobalErrIds;
import org.openldap.fortress.SecurityException;
import org.openldap.fortress.util.LogUtil;


/**
 * AccelMgrImpl Tester.  This module executes RBAC accelerator client side extended operations.
 * It requires the OpenLDAP RBAC Accelerator Overlay to be configured on the server-side.
 *
 * @author Shawn McKinney
 */
public class AccelMgrImplTest extends TestCase
{
    private static final String CLS_NM = AccelMgrImplTest.class.getName();
    private static final Logger LOG = LoggerFactory.getLogger( CLS_NM );

    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest( new AccelMgrImplTest( "testCreateSession" ) );
        suite.addTest( new AccelMgrImplTest( "testCreateSessionWithRole" ) );
        suite.addTest( new AccelMgrImplTest( "testCheckAccess" ) );
        suite.addTest( new AccelMgrImplTest( "testAddActiveRole" ) );
        suite.addTest( new AccelMgrImplTest( "testDropActiveRole" ) );
        return suite;
    }

    public AccelMgrImplTest( String name )
    {
        super( name );
    }

    public void setUp() throws Exception
    {
        super.setUp();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetSession() throws Exception
    {
        //TODO: Test goes here...
    }

    public void testGetToken() throws Exception
    {
        //TODO: Test goes here...
    }

    /**
     *
     */
    public void testCreateSession()
    {
        // public Session createSession(User user, boolean isTrusted)
        createSessions( "CREATE-SESS TU1_UPD TR1", UserTestData.USERS_TU1_UPD, RoleTestData.ROLES_TR1 );
        createSessions( "CREATE-SESS TU3 TR3", UserTestData.USERS_TU3, RoleTestData.ROLES_TR3 );
        createSessions( "CREATE-SESS TU4 TR2", UserTestData.USERS_TU4, RoleTestData.ROLES_TR2 );
    }


    /**
     * @param msg
     * @param uArray
     * @param rArray
     */
    public static void createSessions( String msg, String[][] uArray, String[][] rArray )
    {
        LogUtil.logIt( msg );
        try
        {
            AccelMgr accelMgr = AccelMgrFactory.createInstance( TestUtils.getContext() );
            for ( String[] usr : uArray )
            {
                User user = UserTestData.getUser( usr );
                Session session = accelMgr.createSession( user, false );
                assertNotNull( session );
                accelMgr.deleteSession( session );
                // now try negative test case:
                try
                {
                    User userBad = new User( user.getUserId(), "badpw".toCharArray() );
                    accelMgr.createSession( userBad, false );
                    fail( CLS_NM + ".createSessions failed negative test" );
                }
                catch ( SecurityException se )
                {
                    assertTrue( CLS_NM + "createSessions excep id check", se.getErrorId() == GlobalErrIds
                        .USER_PW_INVLD );
                    // pass
                }
            }
            LOG.debug( "createSessions successful" );
        }
        catch ( SecurityException ex )
        {
            LOG.error( "createSessions: failed with SecurityException rc=" + ex.getErrorId() + ", " +
                "msg=" + ex.getMessage(), ex );
            fail( ex.getMessage() );
        }
    }


    /**
     *
     */
    public void testCreateSessionWithRole()
    {
        // public Session createSession(User user, boolean isTrusted)
        createSessionsWithRoles( "CR-SESS-WRLS TU1_UPD TR1", UserTestData.USERS_TU1_UPD, RoleTestData.ROLES_TR1 );
        createSessionsWithRoles( "CR-SESS-WRLS TU3 TR3", UserTestData.USERS_TU3, RoleTestData.ROLES_TR3 );
        createSessionsWithRoles( "CR-SESS-WRLS TU4 TR2", UserTestData.USERS_TU4, RoleTestData.ROLES_TR2 );
    }


    /**
     * @param msg
     * @param uArray
     * @param rArray
     */
    public static void createSessionsWithRoles( String msg, String[][] uArray, String[][] rArray )
    {
        LogUtil.logIt( msg );
        try
        {
            AccelMgr accelMgr = AccelMgrFactory.createInstance( TestUtils.getContext() );
            for ( String[] usr : uArray )
            {
                User user = UserTestData.getUser( usr );
                List<UserRole> rlsRequested = new ArrayList<>();
                int cnt = 0;
                for ( String[] rle : rArray )
                {
                    rlsRequested.add( RoleTestData.getUserRole( user.getUserId(), rle ) );
                    user.setRoles( rlsRequested );
                    Session session = accelMgr.createSession( user, false );
                    assertTrue( CLS_NM + ".createSessionsWithRoles failed role search USER [" + user.getUserId() + "]" +
                        " CNT [" + ++cnt + "] size [" + session.getRoles().size() + "]",
                        cnt == session.getRoles().size() );
                    accelMgr.deleteSession( session );
                }
            }
            LOG.debug( "createSessionsWithRoles successful" );
        }
        catch ( SecurityException ex )
        {
            LOG.error( "createSessionsWithRoles: failed with SecurityException rc=" + ex.getErrorId() + ", " +
                "msg=" + ex.getMessage(), ex );
            fail( ex.getMessage() );
        }
    }


    /**
     *
     */
    public void testCheckAccess()
    {
        checkAccess( "CHCK-ACS TU3 TOB3 TOP3 ", UserTestData.USERS_TU3, PermTestData.OBJS_TOB3,
            PermTestData.OPS_TOP3, PermTestData.OBJS_TOB2, PermTestData.OPS_TOP1 );

        checkAccess( "CHCK-ACS TU3 TO3 TOP1 ", UserTestData.USERS_TU3, PermTestData.OBJS_TOB3, PermTestData.OPS_TOP3,
            PermTestData.OBJS_TOB2, PermTestData.OPS_TOP1 );

        checkAccess( "CHCK-ACS TU4 TO4 TOP1 ", UserTestData.USERS_TU4, PermTestData.OBJS_TOB2, PermTestData.OPS_TOP2,
            PermTestData.OBJS_TOB2, PermTestData.OPS_TOP1 );

        checkAccess( "CHCK-ACS TU1_UPD TO1 TOP1 ", UserTestData.USERS_TU1_UPD, PermTestData.OBJS_TOB1,
            PermTestData.OPS_TOP1, PermTestData.OBJS_TOB3, PermTestData.OPS_TOP3 );
    }


    /**
     * @param msg
     * @param uArray
     * @param oArray
     * @param opArray
     * @param oArrayBad
     * @param opArrayBad
     */
    public static void checkAccess( String msg, String[][] uArray, String[][] oArray, String[][] opArray,
        String[][] oArrayBad, String[][] opArrayBad )
    {
        LogUtil.logIt( msg );
        try
        {
            AccelMgr accelMgr = AccelMgrFactory.createInstance( TestUtils.getContext() );
            for ( String[] usr : uArray )
            {
                User user = UserTestData.getUser( usr );
                Session session = accelMgr.createSession( user, false );
                assertNotNull( session );
                int i = 0;
                for ( String[] obj : oArray )
                {
                    int j = 0;
                    for ( String[] op : opArray )
                    {
                        Permission goodPerm;
                        if( VUtil.isNotNullOrEmpty( PermTestData.getObjId( opArray[j] ) ) )
                        {
                            // with an objectId:
                            goodPerm = new Permission(
                                PermTestData.getName( obj ),
                                PermTestData.getName( op ),
                                PermTestData.getObjId( opArray[j] ) );
                        }
                        else
                        {
                            // without an objectId:
                            goodPerm = new Permission(
                                PermTestData.getName( obj ),
                                PermTestData.getName( op ) );
                        }

                        // Positive test case, call checkAccess method, should return 'true':
                        assertTrue( CLS_NM + ".checkAccess failed userId [" + user.getUserId() + "] Perm objName [" +
                            PermTestData.getName( obj ) + "] operationName [" + PermTestData.getName( op ) + "]",
                            accelMgr.checkAccess( session, goodPerm ) );

                        Permission badPerm = new Permission(
                            PermTestData.getName( oArrayBad[i] ),
                            PermTestData.getName( opArrayBad[j]),
                            PermTestData.getObjId( opArrayBad[j] ) );

                        // Negative test case, call checkAccess method again, should return 'false':
                        assertFalse( CLS_NM + ".checkAccess failed userId [" + user.getUserId() + "] Perm objName [" +
                            PermTestData.getName( oArrayBad[i] ) + "] operationName [" + PermTestData.getName(
                            opArrayBad[j] ) + "]", accelMgr.checkAccess( session, badPerm ) );
                        j++;
                    }
                    i++;
                }
                accelMgr.deleteSession( session );
            }
            LOG.debug( "checkAccess successful" );
        }
        catch ( SecurityException ex )
        {
            LOG.error( "checkAccess: failed with SecurityException rc=" + ex.getErrorId() + ", " +
                "msg=" + ex.getMessage(), ex );
            fail( ex.getMessage() );
        }
    }


    /**
     *
     */
    public void testAddActiveRole()
    {
        // public void addActiveRole(Session session, String role)
        addActiveRoles( "ADD-ACT-RLS TU1_UPD TR1 bad:TR2", UserTestData.USERS_TU1_UPD, RoleTestData.ROLES_TR1,
            RoleTestData.ROLES_TR2 );
        addActiveRoles( "ADD-ACT-RLS TU3 TR3 bad:TR1:", UserTestData.USERS_TU3, RoleTestData.ROLES_TR3,
            RoleTestData.ROLES_TR1 );
        addActiveRoles( "ADD-ACT-RLS TU4 TR2 bad:TR1", UserTestData.USERS_TU4, RoleTestData.ROLES_TR2,
            RoleTestData.ROLES_TR1 );
    }


    /**
     * @param msg
     * @param uArray
     * @param rPosArray
     * @param rNegArray
     */
    public static void addActiveRoles( String msg, String[][] uArray, String[][] rPosArray, String[][] rNegArray )
    {
        LogUtil.logIt( msg );
        try
        {
            AccelMgr accelMgr = AccelMgrFactory.createInstance( TestUtils.getContext() );
            for ( String[] usr : uArray )
            {
                User user = UserTestData.getUser( usr );
                Session session = accelMgr.createSession( user, false );
                assertNotNull( session );
                // Attempt to activate roles that aren't assigned to user:
                for ( String[] badRle : rNegArray )
                {
                    try
                    {
                        // Add Role (this better fail):
                        accelMgr.addActiveRole( session, new UserRole( user.getUserId(),
                            RoleTestData.getName( badRle ) ) );
                        String error = "addActiveRoles failed negative test 1 User [" + user.getUserId() + "] Role ["
                            + RoleTestData.getName( badRle ) + "]";
                        LOG.info( error );
                        fail( error );
                    }
                    catch ( SecurityException se )
                    {
                        assertTrue( CLS_NM + "addActiveRoles excep id check",
                            se.getErrorId() == GlobalErrIds.URLE_ACTIVATE_FAILED );
                        // pass
                    }
                }
                // remove all roles from the user's session:
                int ctr = rPosArray.length;
                for ( String[] rle : rPosArray )
                {
                    // Drop Role:
                    accelMgr.dropActiveRole( session, new UserRole( user.getUserId(), RoleTestData.getName( rle ) ) );
                    // Drop Role again: (This better fail because role  has already been deactivated from user's
                    // session)
                    try
                    {
                        // Drop Role3 (this better fail):
                        accelMgr.dropActiveRole( session, new UserRole( user.getUserId(),
                            RoleTestData.getName( rle ) ) );
                        String error = "addActiveRoles failed negative test 2 User [" + user.getUserId() + "] Role ["
                            + RoleTestData.getName( rle ) + "]";
                        LOG.info( error );
                        fail( error );
                    }
                    catch ( SecurityException se )
                    {
                        assertTrue( CLS_NM + "addActiveRoles excep id check",
                            se.getErrorId() == GlobalErrIds.URLE_NOT_ACTIVE );
                    }
                }
                // Now activate the list of assigned roles:
                ctr = 0;
                for ( String[] rle : rPosArray )
                {
                    // Activate Role(s):
                    accelMgr.addActiveRole( session, new UserRole( user.getUserId(), RoleTestData.getName( rle ) ) );
                    // TODO: this does not work with RAO - fix me.
                    try
                    {
                        // Activate Role again (this should throw SecurityException):
                        accelMgr.addActiveRole( session, new UserRole( user.getUserId(),
                            RoleTestData.getName( rle ) ) );
                        String error = "addActiveRoles failed test 3 User [" + user.getUserId() + "] Role [" +
                            RoleTestData.getName( rle ) + "]";
                        LOG.info( error );
                        fail( error );
                    }
                    catch ( SecurityException se )
                    {
                        assertTrue( CLS_NM + "addActiveRoles excep id check",
                            se.getErrorId() == GlobalErrIds.URLE_ALREADY_ACTIVE );
                        // this is good
                    }
                }
                accelMgr.deleteSession( session );
            }
        }
        catch ( SecurityException ex )
        {
            LOG.error( "addActiveRoles: failed with SecurityException rc=" + ex.getErrorId() + ", " +
                "msg=" + ex.getMessage(), ex );
            fail( ex.getMessage() );
        }
    }


    /**
     *
     */
    public void testDropActiveRole()
    {
        // public void dropActiveRole(Session session, String role)
        dropActiveRoles( "DRP-ACT-RLS TU1_UPD TR1 bad:TR2", UserTestData.USERS_TU1_UPD, RoleTestData.ROLES_TR1 );
        dropActiveRoles( "DRP-ACT-RLS TU3 TR3 bad:TR1", UserTestData.USERS_TU3, RoleTestData.ROLES_TR3 );
        dropActiveRoles( "DRP-ACT-RLS TU4 TR2 bad:TR1", UserTestData.USERS_TU4, RoleTestData.ROLES_TR2 );
    }


    /**
     * @param msg
     * @param uArray
     * @param rArray
     */
    public static void dropActiveRoles( String msg, String[][] uArray, String[][] rArray )
    {
        LogUtil.logIt( msg );
        try
        {
            AccelMgr accelMgr = AccelMgrFactory.createInstance( TestUtils.getContext() );
            for ( String[] usr : uArray )
            {
                User user = UserTestData.getUser( usr );
                Session session = accelMgr.createSession( user, false );
                assertNotNull( session );
                // remove all roles from the user's session:
                int ctr = rArray.length;
                for ( String[] rle : rArray )
                {
                    // Drop Role:
                    accelMgr.dropActiveRole( session, new UserRole( user.getUserId(), RoleTestData.getName( rle ) ) );
                    // Drop Role again: (This better fail because role  has already been deactivated from user's
                    // session)
                    try
                    {
                        // Drop Role3 (this better fail):
                        accelMgr.dropActiveRole( session, new UserRole( user.getUserId(),
                            RoleTestData.getName( rle ) ) );
                        String error = "dropActiveRoles failed negative test 2 User [" + user.getUserId() + "] Role [" + RoleTestData.getName( rle ) + "]";
                        LOG.info( error );
                        fail( error );
                    }
                    catch ( SecurityException se )
                    {
                        assertTrue( "dropActiveRoles excep id check", se.getErrorId() == GlobalErrIds.URLE_NOT_ACTIVE );
                    }
                }
                accelMgr.deleteSession( session );
            }
        }
        catch ( SecurityException ex )
        {
            LOG.error( "dropActiveRoles: failed with SecurityException rc=" + ex.getErrorId() + ", msg=" + ex.getMessage(), ex );
            fail( ex.getMessage() );
        }
    }
}