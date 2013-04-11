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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.security.Permission;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.apache.hadoop.fs.FileUtil;
import org.apache.oozie.test.XTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class TestOozieDBCLI extends XTestCase {
    private SecurityManager SECURITY_MANAGER;
    private static String url = "jdbc:derby:target/test-data/oozietests/org.apache.oozie.tools.TestOozieDBCLI/data.db;create=true";

    @BeforeClass
    protected void setUp() throws Exception {
        SECURITY_MANAGER = System.getSecurityManager();
        FileUtil.fullyDelete(new File("target/test-data/oozietests/org.apache.oozie.tools.TestOozieDBCLI/data.db"));
        File oozieConfig = new File("src/test/resources/hsqldb-oozie-site.xml");
        System.setProperty("oozie.test.config.file", oozieConfig.getAbsolutePath());
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Connection conn = DriverManager.getConnection(url, "sa", "");
        conn.close();

        super.setUp();

    }

    @AfterClass
    protected void tearDown() throws Exception {
        System.setSecurityManager(SECURITY_MANAGER);
        super.tearDown();

    }


    private void execSQL(String sql) throws Exception {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Connection conn = DriverManager.getConnection(url, "sa", "");

        Statement st = conn.createStatement();
        st.executeUpdate(sql);

        st.close();
        conn.close();
    }

  
    public void test1() throws Exception {
        
        File createSql=new File(getTestCaseConfDir() + File.separator + "out.sql");
        String[] args = { "create", "-sqlfile",createSql.getAbsolutePath(), "-run" };
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
        assertTrue(createSql.exists());

        ByteArrayOutputStream data = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(data));
        try {
            String[] argsv = { "version" };
            OozieDBCLI.main(argsv);

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
        assertTrue(data.toString().contains("db.version: 1"));
        System.setOut(oldOut);

        File upgrage = new File(getTestCaseConfDir() + File.separator + "update.sql");
     
        execSQL("DROP table OOZIE_SYS");

        try {
            String[] argsv = { "upgrade", "-sqlfile", upgrage.getAbsolutePath(), "-run" };
            OozieDBCLI.main(argsv);

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
        assertTrue(upgrage.exists());
        File postupgrade=new File(getTestCaseConfDir() + File.separator + "postUpdate.sql");
        try {
            String[] argsv = { "postupgrade", "-sqlfile", postupgrade.getAbsolutePath(), "-run" };
            OozieDBCLI.main(argsv);

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
        assertTrue(postupgrade.exists());

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
