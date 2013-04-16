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
import java.io.Writer;
import java.net.URI;
import java.security.Permission;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.oozie.service.HadoopAccessorService;
import org.apache.oozie.service.ServiceException;
import org.apache.oozie.service.Services;
import org.apache.oozie.service.WorkflowAppService;
import org.apache.oozie.test.XTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class TestOozieDBCLI extends XTestCase {
    private SecurityManager SECURITY_MANAGER;
    private static String url = "jdbc:derby:target/test-data/oozietests/org.apache.oozie.tools.TestOozieDBCLI/data.db;create=true";
    private String outPath = "outFolder";
    private Services services = null;
    private Path dstPath = null;
    private FileSystem fs;

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

    public void testOozieDBCLI() throws Exception {
        new LauncherSecurityManager();

        File createSql = new File(getTestCaseConfDir() + File.separator + "out.sql");
        String[] argsC = { "create", "-sqlfile", createSql.getAbsolutePath(), "-run" };
        int result=execOozieDBCLICommands(argsC);
        assertEquals(0, result);
        assertTrue(createSql.exists());

        ByteArrayOutputStream data = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        try {
            System.setOut(new PrintStream(data));
            String[] argsv = { "version" };
            result=execOozieDBCLICommands(argsv);
            assertEquals(0, result);

            assertTrue(data.toString().contains("db.version: 1"));

            data.reset();
            String[] argsh = { "help" };
            result=execOozieDBCLICommands(argsh);
            assertEquals(0, result);
            assertTrue(data.toString().contains("ooziedb.sh create <OPTIONS> : create Oozie DB schema"));
            assertTrue(data.toString().contains("ooziedb.sh upgrade <OPTIONS> : upgrade Oozie DB"));
            assertTrue(data.toString().contains("ooziedb.sh postupgrade <OPTIONS> : post upgrade Oozie DB"));

            data.reset();
            String[] argsw = { "invalidCommand" };
            result=execOozieDBCLICommands(argsw);
            assertEquals(1, result);

        }
        finally {
            System.setOut(oldOut);
        }

        File upgrage = new File(getTestCaseConfDir() + File.separator + "update.sql");
        execSQL("DROP table OOZIE_SYS");
        String[] argsu = { "upgrade", "-sqlfile", upgrage.getAbsolutePath(), "-run" };
        execOozieDBCLICommands(argsu);

        assertTrue(upgrage.exists());
        File postupgrade = new File(getTestCaseConfDir() + File.separator + "postUpdate.sql");
        String[] argspu = { "postupgrade", "-sqlfile", postupgrade.getAbsolutePath(), "-run" };
        execOozieDBCLICommands(argspu);
        assertTrue(postupgrade.exists());

    }

    public void testOozieSharelibCLI() throws Exception {
        new LauncherSecurityManager();

        String[] argsh = { "help" };
        execOozieSharelibCLICommands(argsh);

        File libDirectory = new File(getTestCaseConfDir() + File.separator + "lib");

        if (!libDirectory.exists()) {
            libDirectory.mkdirs();
        }
        else {
            FileUtil.fullyDelete(libDirectory);
            libDirectory.mkdirs();
        }

        writeFile(libDirectory, "file1", "test File");
        writeFile(libDirectory, "file2", "test File2");

        String[] argsc = { "create", "-fs", outPath, "-locallib", libDirectory.getParentFile().getAbsolutePath() };
        execOozieSharelibCLICommands(argsc);

        FileSystem fs = getTargetFileSysyem();

        assertEquals(9, fs.getFileStatus(new Path(getDistPath(), "file1")).getLen());
        assertEquals(10, fs.getFileStatus(new Path(getDistPath(), "file2")).getLen());

    }

    public void testOozieSharelibCLItarGz() throws Exception {
        new LauncherSecurityManager();

        String oozieHome = System.getProperty(OozieSharelibCLI.OOZIE_HOME);
        File libDirectory = new File(oozieHome);

        System.out.println("oozieHome:" + oozieHome);
        if (!libDirectory.exists()) {
            libDirectory.mkdirs();
        }
        File source = new File("src/test/resources");
        System.out.println("source:" + source.getAbsolutePath());
        System.out.println("source2:" + source.listFiles());
        
        for (File file : source.listFiles()) {
            System.out.println("source file:" + file.getAbsolutePath());
        }

        FileUtils.copyDirectory(source, libDirectory);
        for (File file : libDirectory.listFiles()) {
            System.out.println("oozieHome file:" + file.getAbsolutePath());
        }
        FileUtils.moveFile(new File(libDirectory.getAbsolutePath()+File.separator+"oozie-sharelib-test.tar.gz.bin"), new File(libDirectory.getAbsolutePath()+File.separator+"oozie-sharelib-test.tar.gz"));
        Collection<File> files = FileUtils.listFiles(libDirectory, new WildcardFileFilter("oozie-sharelib*.tar.gz"), null);
        for (File file : files) {
            System.out.println("oozieHome file:" + file.getAbsolutePath());
        }
        assertFalse(files.isEmpty());
        System.out.println("oozieHome conteins:" + libDirectory.listFiles().toString());

        FileSystem fs = getTargetFileSysyem();
        fs.delete(getDistPath(), true);

        String[] argsc = { "create", "-fs", outPath };
        execOozieSharelibCLICommands(argsc);

        assertEquals(9, fs.getFileStatus(new Path(getDistPath(), "file1")).getLen());
        assertEquals(10, fs.getFileStatus(new Path(getDistPath(), "file2")).getLen());

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

    private Path getDistPath() throws Exception {
        if (dstPath == null) {
            WorkflowAppService lwas = getServices().get(WorkflowAppService.class);
            dstPath = lwas.getSystemLibPath();
        }
        return dstPath;
    }

    private void writeFile(File folder, String filename, String content) throws Exception {
        File file = new File(folder.getAbsolutePath() + File.separator + filename);
        Writer writer = new FileWriter(file);
        writer.write(content);
        writer.flush();
        writer.close();

    }

    private void execOozieSharelibCLICommands(String[] args) throws Exception {
        try {
            OozieSharelibCLI.main(args);
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
