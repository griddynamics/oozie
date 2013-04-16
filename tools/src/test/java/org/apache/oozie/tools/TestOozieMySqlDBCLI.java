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
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.oozie.service.HadoopAccessorService;
import org.apache.oozie.service.ServiceException;
import org.apache.oozie.service.Services;
import org.apache.oozie.test.XTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class TestOozieMySqlDBCLI extends XTestCase {
    private SecurityManager SECURITY_MANAGER;
    private static String url = "jdbc:mysql:fake";
    private String outPath = "outFolder";
    private Services services = null;
    private Path dstPath = null;
    private FileSystem fs;

    @BeforeClass
    protected void setUp() throws Exception {
        SECURITY_MANAGER = System.getSecurityManager();
        DriverManager.registerDriver( new FakeDriver());

        File oozieConfig = new File("src/test/resources/fake-oozie-site.xml");
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

   
    /**
     * addfasdf
     * @throws Exception 
     */
    public void testCreateMysql() throws Exception{
        new LauncherSecurityManager();
        File createSql = new File(getTestCaseConfDir() + File.separator + "create.sql");
        String[] argsC = { "create", "-sqlfile", createSql.getAbsolutePath(), "-run" };
        int result=execOozieDBCLICommands(argsC);
        assertEquals(0, result);
        assertTrue(createSql.exists());

    }

    public void testUpdateMysql() throws Exception{
        new LauncherSecurityManager();
        FakeConnection.CREATE=false;
        File upgrage = new File(getTestCaseConfDir() + File.separator + "update.sql");
        String[] argsu = { "upgrade", "-sqlfile", upgrage.getAbsolutePath(), "-run" };

        int result=execOozieDBCLICommands(argsu);
        assertEquals(0, result);
        assertTrue(upgrage.exists());

    }

    private FileSystem getTargetFileSysyem() throws Exception {
        if (fs == null) {
            HadoopAccessorService has = getServices().get(HadoopAccessorService.class);
            URI uri = new Path(outPath).toUri();
            Configuration fsConf = has.createJobConf(uri.getAuthority());
            fs = has.createFileSystem(System.getProperty("user.name"), uri, fsConf);
        }
        return fs;

    }

    private Services getServices() throws ServiceException {
        if (services == null) {
            services = new Services();
            services.getConf().set(Services.CONF_SERVICE_CLASSES,
                    "org.apache.oozie.service.LiteWorkflowAppService, org.apache.oozie.service.HadoopAccessorService");
            services.init();
        }
        return services;
    }



    private int  execOozieDBCLICommands(String[] args) {
        try {
            OozieDBCLI.main(args);

        }
        catch (SecurityException ex) {
            if (LauncherSecurityManager.getExitInvoked()) {
                System.out.println("Intercepting System.exit(" + LauncherSecurityManager.getExitCode() + ")");
                System.err.println("Intercepting System.exit(" + LauncherSecurityManager.getExitCode() + ")");
                return LauncherSecurityManager.getExitCode();
               
            }
            else {
                throw ex;
            }
        }
        return 1;
    }
}



