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
package org.apache.oozie.action.hadoop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.apache.oozie.util.XConfiguration;
/**
 * Test SqoopMain class should print into console information about database,
 * run execute commands for database :import, export...
 */
public class TestSqoopMain extends MainTestCase {
    private SecurityManager SECURITY_MANAGER;

    protected void setUp() throws Exception {
        super.setUp();
        SECURITY_MANAGER = System.getSecurityManager();
    }

    protected void tearDown() throws Exception {
        System.setSecurityManager(SECURITY_MANAGER);
        super.tearDown();
    }

    public Void call() throws Exception {

        XConfiguration jobConfiguration = new XConfiguration();
        Class.forName("org.hsqldb.jdbcDriver");
        Connection conn = DriverManager.getConnection(getLocalJdbcUri(), "sa", "");
        Statement st = conn.createStatement();
        st.executeUpdate("CREATE TABLE TableForTest (ID INTEGER PRIMARY KEY, Name VARCHAR(256))");
        st.close();
        conn.close();

        String[] val = { "list-tables", "--connect", getLocalJdbcUri(), "--driver", "org.hsqldb.jdbcDriver", "--username", "sa" };

        MapReduceMain.setStrings(jobConfiguration, "oozie.sqoop.args", val);

        File actionXml = new File(getTestCaseDir(), "action.xml");
        File output = new File(getTestCaseDir(), "output.properties");
        OutputStream os = new FileOutputStream(actionXml);

        jobConfiguration.writeXml(os);
        os.close();

        setSystemProperty("oozie.action.conf.xml", actionXml.getAbsolutePath());
        setSystemProperty("oozie.launcher.job.id", "" + System.currentTimeMillis());
        setSystemProperty("oozie.action.output.properties", output.getAbsolutePath());

        new LauncherSecurityManager();

        String[] args = {};
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        PrintStream oldPrintStream = System.out;
        System.setOut(new PrintStream(data));
        try {
            SqoopMain.main(args);
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
        finally {
            System.setOut(oldPrintStream);
        }
        assertTrue(data.toString().contains("TableForTest".toUpperCase()));
        return null;
    }

    private String getLocalJdbcUri() {
        File dataBaseFile = new File(getTestCaseDir(), "db.hsqldb");
        return "jdbc:hsqldb:file:" + dataBaseFile.getAbsolutePath() + ";shutdown=true";
    }
}
