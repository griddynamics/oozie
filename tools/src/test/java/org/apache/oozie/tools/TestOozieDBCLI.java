/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oozie.tools;

import java.io.File;
import java.security.Permission;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.apache.oozie.test.XTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class TestOozieDBCLI extends XTestCase {
    private SecurityManager SECURITY_MANAGER;

    @BeforeClass
    protected void setUp() throws Exception {
        SECURITY_MANAGER = System.getSecurityManager();
        File oozieConfig = new File("src/test/resources/hsqldb-oozie-site.xml");
        System.setProperty("oozie.test.config.file", oozieConfig.getAbsolutePath());
        Class.forName("org.hsqldb.jdbcDriver");
        Connection conn = DriverManager.getConnection(
                "jdbc:hsqldb:mem:target/test-data/oozietests/org.apache.oozie.tools.TestOozieDBCLI/db.hsqldb;create=true", "sa", "");

        Statement st = conn.createStatement();
        st.executeUpdate("CREATE USER oozie PASSWORD oozie ADMIN");
      //  st.executeUpdate("CREATE TABLE TableForTest (ID INTEGER PRIMARY KEY, Name VARCHAR(256))");

        st.close();
        conn.close();
        super.setUp();

    }

    @AfterClass
    protected void tearDown() throws Exception {
        System.setSecurityManager(SECURITY_MANAGER);
        super.tearDown();

    }

    public void test1() {
        String[] args = { "create", "-sqlfile", "out.sql", "-run" };
        new LauncherSecurityManager();
        try {
            OozieDBCLI.main(args);
        }
        catch (SecurityException ex) {
            if (LauncherSecurityManager.getExitInvoked()) {
                System.out.println("Intercepting System.exit(" + LauncherSecurityManager.getExitCode() + ")");
                System.err.println("Intercepting System.exit(" + LauncherSecurityManager.getExitCode() + ")");
                if (LauncherSecurityManager.getExitCode() != 0) {
                    fail();
                }
            }
            else {
                throw ex;
            }
        }
    }

}

class LauncherSecurityManager extends SecurityManager {
    private static boolean exitInvoked;
    private static int exitCode;
    private SecurityManager securityManager;

    public LauncherSecurityManager() {
        reset();
        securityManager = System.getSecurityManager();
        System.setSecurityManager(this);
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        if (securityManager != null) {
            // check everything with the original SecurityManager
            securityManager.checkPermission(perm, context);
        }
    }

    @Override
    public void checkPermission(Permission perm) {
        if (securityManager != null) {
            // check everything with the original SecurityManager
            securityManager.checkPermission(perm);
        }
    }

    @Override
    public void checkExit(int status) throws SecurityException {
        exitInvoked = true;
        exitCode = status;
        throw new SecurityException("Intercepted System.exit(" + status + ")");
    }

    public static boolean getExitInvoked() {
        return exitInvoked;
    }

    public static int getExitCode() {
        return exitCode;
    }

    public static void reset() {
        exitInvoked = false;
        exitCode = 0;
    }
}
