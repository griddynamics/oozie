package org.apache.oozie.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URI;

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

public class TestOozieSharelibCLI extends XTestCase {
    private SecurityManager SECURITY_MANAGER;
    private String outPath = "outFolder";
    private Services services = null;
    private Path dstPath = null;
    private FileSystem fs;

    @BeforeClass
    protected void setUp() throws Exception {
        SECURITY_MANAGER = System.getSecurityManager();
        new LauncherSecurityManager();
        super.setUp();

    }

    @AfterClass
    protected void tearDown() throws Exception {
        System.setSecurityManager(SECURITY_MANAGER);
        super.tearDown();

    }

    public void testHelp() throws Exception {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        PrintStream oldPrintStream = System.out;
        System.setOut(new PrintStream(data));
        try {
            String[] argsh = { "help" };
            assertEquals(0, execOozieSharelibCLICommands(argsh));
            assertTrue(data.toString().contains("oozie-setup.sh create <OPTIONS> : create oozie sharelib"));
            assertTrue(data.toString().contains("oozie-setup.sh upgrade <OPTIONS> : upgrade oozie sharelib"));
            assertTrue(data.toString().contains(" oozie-setup.sh help"));
        }
        finally {
            System.setOut(oldPrintStream);
        }

    }

    public void testOozieSharelibCLI() throws Exception {

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
        assertEquals(0, execOozieSharelibCLICommands(argsc));

        FileSystem fs = getTargetFileSysyem();

        assertEquals(9, fs.getFileStatus(new Path(getDistPath(), "file1")).getLen());
        assertEquals(10, fs.getFileStatus(new Path(getDistPath(), "file2")).getLen());

    }

    public void testFakeCommand() throws Exception {
        
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        PrintStream oldPrintStream = System.err;
        System.setErr(new PrintStream(data));
        try {
            String[] argsf = { "fakeCommand" };
            assertEquals(1, execOozieSharelibCLICommands(argsf));
            assertTrue(data.toString().contains("Invalid sub-command: invalid sub-command [fakeCommand]"));
            assertTrue(data.toString().contains("use 'help [sub-command]' for help details"));
        }
        finally {
            System.setErr(oldPrintStream);
        }

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

    private int execOozieSharelibCLICommands(String[] args) throws Exception {
        try {
            OozieSharelibCLI.main(args);
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
