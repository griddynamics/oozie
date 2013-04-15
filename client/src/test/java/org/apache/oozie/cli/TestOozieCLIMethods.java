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
package org.apache.oozie.cli;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.oozie.client.BundleJob;
import org.apache.oozie.client.CoordinatorAction;
import org.apache.oozie.client.CoordinatorJob;
import org.apache.oozie.client.WorkflowAction;
import org.apache.oozie.client.WorkflowJob;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;

public class TestOozieCLIMethods extends TestCase {

    static final String jobIdPattern = "Job ID[\\s|:]+";
    static final String jobNamePattern = "Job Name[\\s|:]+";
    static final String workflowNamePattern = "Workflow Name[\\s|:]+";
    static final String appPathPattern = "App Path[\\s|:]+";
    static final String statusPattern = "Status[\\s|:]+RUNNING";
    static final String actionIdPattern = "ID[\\s|:]+";
    static final String actionJobIdPattern = "Job ID[\\s|:]+";
    static final String actionNamePattern = "Name[\\s|:]+";

    static class DataObject {
        String deamonName;
        String appName;
        String appPath;
    }

    private abstract class OutputReaderTemplate {
        protected String read() throws IOException {
            ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
            PipedOutputStream pipeOut = new PipedOutputStream();
            PipedInputStream pipeIn = new PipedInputStream(pipeOut, 1024 * 50);
            System.setOut(new PrintStream(pipeOut));
            execute();
            pipeOut.close();
            ByteStreams.copy(pipeIn, outBytes);
            pipeIn.close();
            return new String(outBytes.toByteArray());
        }

        abstract void execute() throws IOException;
    }

    /**
     *
     * Create {@code CoordinatorJob} implementation with
     * {@code CoordinatorActions} implementation, call
     * {@code new OozieCLI().printCoordJob() }, and validate {@code System.out}
     * output
     *
     */
    public void testValidatePrintCoordJobMethodOutput() throws IOException {
        final DataObject dtObject = new DataObject() {
            {
                this.deamonName = "testCoordJob";
                this.appName = "testCoordinatorJobApp";
                this.appPath = "testCoordinatorJobAppPath";
            }
        };

        CoordinatorJob coordJob = createCoordinator(dtObject);

        assertPrintCoordJobOutput(readCoordJobOutput(coordJob, true), dtObject);
        assertPrintCoordJobOutput(readCoordJobOutput(coordJob, false), dtObject);
    }

    /**
     *
     * Create {@code CoordinatorAction} implementation, call
     * {@code new OozieCLI().printCoordAction() }, and validate
     * {@code System.out} output
     *
     */
    public void testValidateReadPrintCoordActionOutput() throws IOException {
        final DataObject dtObject = new DataObject() {
            {
                this.deamonName = "testCoordinatorAction";
                this.appName = "testCoordinatorJobApp";
                this.appPath = "testCoordinatorJobAppPath";
            }
        };

        CoordinatorAction coordinatorAction = createCoordinatorAction(dtObject);
        assertPrintCoordActionOutput(readPrintCoordAction(coordinatorAction), dtObject);
    }

    /**
     *
     * Create {@code WorkflowJob} implementation with {@code WorkflowAction}
     * implementation, call {@code new OozieCLI().printJob() }, and validate
     * {@code System.out} output
     *
     */
    public void testValidatePrintJobOutput() throws IOException {
        final DataObject dtObject = new DataObject() {
            {
                this.deamonName = "testWorkflowJob";
                this.appName = "testWorkflowJobApp";
                this.appPath = "testWorkflowJobAppPath";
            }
        };

        WorkflowJob workflowJob = createWorkflowJob(dtObject);
        assertPrintWorkflowJobOutput(readWorkflowJobOutput(workflowJob, true), dtObject);
        assertPrintWorkflowJobOutput(readWorkflowJobOutput(workflowJob, false), dtObject);
    }

    /**
     * Create {@code WorkflowAction} implementation and,
     * call {@code new OozieCLI().printWorkflowAction() }
     * and validate {@code System.out} output
     *
     */
    public void testValidatePrintWorkflowActionOutput() throws IOException {
        final DataObject dtObject = new DataObject() {
            {
                this.deamonName = "testWorkflowAction111";
                this.appName = "testWorkflowActionAppName";
                this.appPath = "unused";
            }
        };

        WorkflowAction workflowAction = createWorkflowAction(dtObject);
        assertPrintWorkflowActionOutput(readWorkflowActionOutput(workflowAction, true), dtObject);
        assertPrintWorkflowActionOutput(readWorkflowActionOutput(workflowAction, false), dtObject);
    }

    /**
     * Create {@code CoordinatorJob} implementation,
     * call {@code new OozieCLI().printCoordJobs() }
     * and validate {@code System.out} output
     *
     */
    public void testValidatePrintCoordJobsOutput() throws IOException {
        final DataObject dtObject1 = new DataObject() {
            {
                this.deamonName = "testCoordJob1";
                this.appName = "testCoordinatorJobApp1";
                this.appPath = "testCoordinatorJobAppPath1";
            }
        };
        final DataObject dtObject2 = new DataObject() {
            {
                this.deamonName = "testCoordJob2";
                this.appName = "testCoordinatorJobApp2";
                this.appPath = "testCoordinatorJobAppPath2";
            }
        };

        final ImmutableList<CoordinatorJob> coordJobs =
                ImmutableList.of(createCoordinator(dtObject1), createCoordinator(dtObject2));
        assertPrintCoordJobsOutput(readCoordinatorsJobOutput(coordJobs, true), ImmutableList.of(dtObject1, dtObject2));
        assertPrintCoordJobsOutput(readCoordinatorsJobOutput(coordJobs, false), ImmutableList.of(dtObject1, dtObject2));
    }

    /**
     *
     * Create {@code CoordinatorJob} implementation,
     * call {@code new OozieCLI().printJobs() }
     * and validate {@code System.out} output
     *
     */
    public void testValidatePrintJobsOutput() throws IOException {
        final DataObject dtObject1 = new DataObject() {
            {
                this.deamonName = "testWorkflowJob1";
                this.appName = "testWorkflowJobApp1";
                this.appPath = "testWorkflowJobAppPath1";
            }
        };
        final DataObject dtObject2 = new DataObject() {
            {
                this.deamonName = "testWorkflowJob1";
                this.appName = "testWorkflowJobApp1";
                this.appPath = "testWorkflowJobAppPath1";
            }
        };

        ImmutableList<WorkflowJob> workflowJobs = ImmutableList.of(createWorkflowJob(dtObject1), createWorkflowJob(dtObject2));
        assertPrintWorkflowJobOutput(readWorkflowJobsOutput(workflowJobs, true), workflowJobs);
        assertPrintWorkflowJobOutput(readWorkflowJobsOutput(workflowJobs, false), workflowJobs);
    }

    /**
     *
     *
     *
     */
    public void testValidationPrintBundleJobsOutput() throws IOException {
        final DataObject dtObject1 = new DataObject() {
            {
                this.deamonName = "testBundleJob1";
                this.appName = "testBundleJobApp1";
                this.appPath = "testBundleJobAppPath1";
            }
        };

        BundleJob bundleJob = createBundleJob(dtObject1);
        ImmutableList<BundleJob> bundleJobs = ImmutableList.of(bundleJob);

        assertPrintBundleJobOutput(readBundleJobsOutput(bundleJobs, true), bundleJobs);
        assertPrintBundleJobOutput(readBundleJobsOutput(bundleJobs, false), bundleJobs);
    }

    private String readBundleJobsOutput(final ImmutableList<BundleJob> bundleJobs, final boolean verbose) throws IOException {
        return new OutputReaderTemplate() {
            @Override
            void execute() throws IOException {
                new OozieCLI().printBundleJobs(bundleJobs, null, verbose);
            }
        }.read();
    }

    private String readWorkflowJobsOutput(final ImmutableList<WorkflowJob> workflowJobs, final boolean verbose) throws IOException {
        return new OutputReaderTemplate() {
            @Override
            void execute() throws IOException {
                new OozieCLI().printJobs(workflowJobs, null, verbose);
            }
        }.read();
    }

    private String readCoordinatorsJobOutput(final ImmutableList<CoordinatorJob> coordJobs, final boolean verbose)
            throws IOException {
        return new OutputReaderTemplate() {
            @Override
            void execute() throws IOException {
                new OozieCLI().printCoordJobs(coordJobs, null, verbose);
            }
        }.read();
    }

    private String readWorkflowActionOutput(final WorkflowAction workflowAction, final boolean verbose) throws IOException {
        return new OutputReaderTemplate() {
            @Override
            void execute() throws IOException {
                new OozieCLI().printWorkflowAction(workflowAction, null, verbose);
            }
        }.read();
    }

    private String readWorkflowJobOutput(final WorkflowJob workflowJob,
            final boolean verbose) throws IOException {
        return new OutputReaderTemplate() {
            @Override
            void execute() throws IOException {
                new OozieCLI().printJob(workflowJob, null, verbose);
            }
        }.read();
    }

    private String readPrintCoordAction(final CoordinatorAction coordinatorAction)
            throws IOException {
        return new OutputReaderTemplate() {
            @Override
            void execute() throws IOException {
                new OozieCLI().printCoordAction(coordinatorAction, null);
            }
        }.read();
    }

    private String readCoordJobOutput(final CoordinatorJob coordJob, final boolean verbose)
            throws IOException {
        return new OutputReaderTemplate() {
            @Override
            void execute() throws IOException {
                new OozieCLI().printCoordJob(coordJob, null, verbose);
            }
        }.read();
    }

    private void assertPrintBundleJobOutput(String string, ImmutableList<BundleJob> bundleJobsString) {
        //TO DO: Implement regexp check's
        assertTrue(true);
    }

    private void assertPrintWorkflowJobOutput(String workflowJobOutput, ImmutableList<WorkflowJob> dtObjects) {
        //TO DO: Implement regexp check's
        assertTrue(true);
    }

    private void assertPrintCoordJobsOutput(String coordJobsOutput, ImmutableList<DataObject> dtObjects) {
        //TO DO: Implement regexp check's
        assertTrue(true);
    }

    
    private void assertPrintWorkflowActionOutput(String workflowActionOutput, DataObject dtObject) {
        assertTrue("assertPrintWorkflowJobOutput ID error", Pattern.compile(actionIdPattern + dtObject.deamonName)
                .matcher(workflowActionOutput).find());
        assertTrue("assertPrintWorkflowJobOutput Name error", Pattern.compile(actionNamePattern + dtObject.appName)
                .matcher(workflowActionOutput).find());
    }

    private void assertPrintWorkflowJobOutput(String workflowJobOutput,
            DataObject dtObject) {
        assertTrue("assertPrintWorkflowJobOutput Job ID error", Pattern.compile(jobIdPattern + dtObject.deamonName)
                        .matcher(workflowJobOutput).find());
        assertTrue("assertPrintWorkflowJobOutput Job Name error", Pattern.compile(workflowNamePattern + dtObject.appName)
                        .matcher(workflowJobOutput).find());
        assertTrue("assertPrintWorkflowJobOutput App Path error", Pattern.compile(appPathPattern + dtObject.appPath)
                        .matcher(workflowJobOutput).find());
    }

    private void assertPrintCoordActionOutput(String output, DataObject dtObject) {
        assertTrue("assertPrintCoordActionOutput Job ID error", Pattern.compile(actionIdPattern + dtObject.deamonName)
                .matcher(output).find());
        assertTrue("assertPrintCoordActionOutput ID error ", Pattern.compile(actionJobIdPattern + dtObject.appName)
                .matcher(output).find());
    }

    private void assertPrintCoordJobOutput(String line, DataObject dtObject) {
        assertTrue("assertPrintCoordJobOutput Job ID error", Pattern.compile(jobIdPattern + dtObject.deamonName)
                .matcher(line).find());
        assertTrue("assertPrintCoordJobOutput Job Name error", Pattern.compile(jobNamePattern + dtObject.appName)
                .matcher(line).find());
        assertTrue("assertPrintCoordJobOutput App Path error", Pattern.compile(appPathPattern + dtObject.appPath)
                .matcher(line).find());
        assertTrue("assertPrintCoordJobOutput Status error", Pattern.compile(statusPattern).matcher(line).find());
    }

    private BundleJob createBundleJob(DataObject dtObject) {
        BundleJob bundleJobMock = mock(BundleJob.class);
        when(bundleJobMock.getId()).thenReturn(dtObject.deamonName);
        when(bundleJobMock.getAppName()).thenReturn(dtObject.appName);
        when(bundleJobMock.getAppPath()).thenReturn(dtObject.appPath);
        when(bundleJobMock.getStatus()).thenReturn(org.apache.oozie.client.Job.Status.RUNNING);
        return bundleJobMock;
    }

    private CoordinatorJob createCoordinator(DataObject dtObject) {
        CoordinatorJob coordinatorJobMock = mock(CoordinatorJob.class);
        when(coordinatorJobMock.getId()).thenReturn(dtObject.deamonName);
        when(coordinatorJobMock.getAppName()).thenReturn(dtObject.appName);
        when(coordinatorJobMock.getAppPath()).thenReturn(dtObject.appPath);
        when(coordinatorJobMock.getConcurrency()).thenReturn(15);
        when(coordinatorJobMock.getStatus()).thenReturn(CoordinatorJob.Status.RUNNING);
        when(coordinatorJobMock.getUser()).thenReturn("test");
        when(coordinatorJobMock.getGroup()).thenReturn("test-group");        
                
        ImmutableList.Builder<CoordinatorAction> builder = ImmutableList
                .builder();

        for (final String id : Arrays.asList("1", "2"))
            builder.add(createCoordinatorAction(new DataObject() {
                {
                    this.deamonName = id;
                    this.appName = "testCoordinatorAction";
                }
            }));

        when(coordinatorJobMock.getActions()).thenReturn(builder.build());
        return coordinatorJobMock;
    }

    private WorkflowJob createWorkflowJob(DataObject dtObject) {
        WorkflowJob workflowJobMock = mock(WorkflowJob.class);
        when(workflowJobMock.getId()).thenReturn(dtObject.deamonName);
        when(workflowJobMock.getAppName()).thenReturn(dtObject.appName);
        when(workflowJobMock.getAppPath()).thenReturn(dtObject.appPath);
        when(workflowJobMock.getStatus()).thenReturn(WorkflowJob.Status.RUNNING);
        WorkflowAction ac = createWorkflowAction(dtObject);
        WorkflowAction ac0 = createWorkflowAction(dtObject);
        when(workflowJobMock.getActions()).thenReturn(Arrays.asList(ac, ac0));
        return workflowJobMock;
    }

    private static CoordinatorAction createCoordinatorAction(DataObject dtObject) {
        CoordinatorAction crdActionMock = mock(CoordinatorAction.class);
        when(crdActionMock.getId()).thenReturn(dtObject.deamonName);
        when(crdActionMock.getJobId()).thenReturn(dtObject.appName);
        when(crdActionMock.getActionNumber()).thenReturn(11);
        when(crdActionMock.getStatus()).thenReturn(CoordinatorAction.Status.SUBMITTED);
        return crdActionMock;
    }

    private static WorkflowAction createWorkflowAction(DataObject dtObject) {
        WorkflowAction workflowActionMock = mock(WorkflowAction.class);
        when(workflowActionMock.getId()).thenReturn(dtObject.deamonName);
        when(workflowActionMock.getName()).thenReturn(dtObject.appName);                
        return workflowActionMock;
    }
}
