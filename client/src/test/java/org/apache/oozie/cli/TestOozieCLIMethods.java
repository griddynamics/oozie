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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
        BundleJobImpl bundleJob = new BundleJobImpl();
        bundleJob.setId(dtObject.deamonName);
        bundleJob.setAppName(dtObject.appName);
        bundleJob.setAppPath(dtObject.appPath);
        bundleJob.setStatus(org.apache.oozie.client.Job.Status.RUNNING);
        return bundleJob;
    }

    private CoordinatorJob createCoordinator(DataObject dtObject) {
        CoordinatorJobImpl coordJob = new CoordinatorJobImpl();
        coordJob.setId(dtObject.deamonName);
        coordJob.setAppName(dtObject.appName);
        coordJob.setAppPath(dtObject.appPath);
        coordJob.setConcurrency(15);
        coordJob.setStatus(CoordinatorJob.Status.RUNNING);
        coordJob.setCreatedTime(new Date());
        coordJob.setLastModifiedTime(new Date());
        coordJob.setUser("test");
        coordJob.setGroup("test-group");
        coordJob.setStartTime(new Date());
        coordJob.setEndTime(new Date());

        ImmutableList.Builder<CoordinatorAction> builder = ImmutableList
                .builder();

        for (final String id : Arrays.asList("1", "2"))
            builder.add(createCoordinatorAction(new DataObject() {
                {
                    this.deamonName = id;
                    this.appName = "testCoordinatorAction";
                }
            }));

        coordJob.setActions(builder.build());
        return coordJob;
    }

    private WorkflowJob createWorkflowJob(DataObject dtObject) {
        WorkflowJobImpl workflowJob = new WorkflowJobImpl();
        workflowJob.setId(dtObject.deamonName);
        workflowJob.setAppName(dtObject.appName);
        workflowJob.setAppPath(dtObject.appPath);
        workflowJob.setStatus(WorkflowJob.Status.RUNNING);
        workflowJob.setActions(ImmutableList.of(createWorkflowAction(dtObject),
                createWorkflowAction(dtObject)));
        return workflowJob;
    }

    private static CoordinatorAction createCoordinatorAction(DataObject dtObject) {
        CoordinatorActionImpl action = new CoordinatorActionImpl();
        action.setId(dtObject.deamonName);
        action.setJobId(dtObject.appName);
        action.setActionNumber(11);
        action.setStatus(CoordinatorAction.Status.SUBMITTED);
        action.setNominalTime(new Date());
        action.setLastModifiedTime(new Date());
        action.setCreatedTime(new Date());
        return action;
    }

    private static WorkflowAction createWorkflowAction(DataObject dtObject) {
        WorkflowActionImpl workflowActionImpl = new WorkflowActionImpl();
        workflowActionImpl.setId(dtObject.deamonName);
        workflowActionImpl.setName(dtObject.appName);
        return workflowActionImpl;
    }

    static final class BundleJobImpl implements BundleJob {
        String id;
        String appPath;
        String appName;
        org.apache.oozie.client.Job.Status status;

        public void setAppPath(String appPath) {
            this.appPath = appPath;
        }

        @Override
        public String getAppPath() {
            return appPath;
        }

        @Override
        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        @Override
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public void setStatus(Status status) {
            this.status = status;
        }

        @Override
        public String getConf() {
            return "Conf";
        }

        @Override
        public String getUser() {
            return "User";
        }

        @Override
        @Deprecated
        public String getGroup() {
            return "Group";
        }

        @Override
        public String getAcl() {
            return "Acl";
        }

        @Override
        public String getConsoleUrl() {
            return "ConsoleUrl";
        }

        @Override
        public Date getStartTime() {
            return new Date();
        }

        @Override
        public Date getEndTime() {
            return new Date();
        }

        @Override
        public void setPending() {
        }

        @Override
        public void resetPending() {
        }

        @Override
        public Date getPauseTime() {
            return new Date();
        }

        @Override
        public String getExternalId() {
            return "ExternalId";
        }

        @Override
        public Timeunit getTimeUnit() {
            return Timeunit.HOUR;
        }

        @Override
        public int getTimeout() {
            return 0;
        }

        @Override
        public List<CoordinatorJob> getCoordinators() {
            return ImmutableList.of();
        }

        @Override
        public Date getKickoffTime() {
            return new Date();
        }

        @Override
        public Date getCreatedTime() {
            return new Date();
        }
    }

    static final class WorkflowJobImpl implements WorkflowJob {
        String id;
        String appName;
        String appPath;
        WorkflowJob.Status status;
        ImmutableList<WorkflowAction> actions;

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        public void setAppPath(String appPath) {
            this.appPath = appPath;
        }

        @Override
        public String getAppPath() {
            return appPath;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        @Override
        public String getAppName() {
            return appName;
        }

        @Override
        public String getConf() {
            return "WorkflowJobImpl_Conf";
        }

        public void setStatus(WorkflowJob.Status status) {
            this.status = status;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public Date getLastModifiedTime() {
            return new Date();
        }

        @Override
        public Date getCreatedTime() {
            return new Date();
        }

        @Override
        public Date getStartTime() {
            return new Date();
        }

        @Override
        public Date getEndTime() {
            return new Date();
        }

        @Override
        public String getUser() {
            return "user";
        }

        @Override
        @Deprecated
        public String getGroup() {
            return "group";
        }

        @Override
        public String getAcl() {
            return "acl";
        }

        @Override
        public int getRun() {
            return 0;
        }

        @Override
        public String getConsoleUrl() {
            return "url";
        }

        @Override
        public String getParentId() {
            return "ParentId";
        }

        @Override
        public List<WorkflowAction> getActions() {
            return actions;
        }

        public void setActions(List<WorkflowAction> actions) {
            this.actions = ImmutableList.copyOf(actions);
        }

        @Override
        public String getExternalId() {
            return "ExternalId";
        }
    }

    static final class WorkflowActionImpl implements WorkflowAction {
        String id;
        String actionName;

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        public void setName(String testWorkflowAction) {
            this.actionName = testWorkflowAction;
        }

        @Override
        public String getName() {
            return actionName;
        }

        @Override
        public String getCred() {
            return "Cred";
        }

        @Override
        public String getType() {
            return "Type";
        }

        @Override
        public String getConf() {
            return "Conf";
        }

        @Override
        public Status getStatus() {
            return Status.RUNNING;
        }

        @Override
        public int getRetries() {
            return 0;
        }

        @Override
        public int getUserRetryCount() {
            return 0;
        }

        @Override
        public int getUserRetryMax() {
            return 0;
        }

        @Override
        public int getUserRetryInterval() {
            return 0;
        }

        @Override
        public Date getStartTime() {
            return new Date();
        }

        @Override
        public Date getEndTime() {
            return new Date();
        }

        @Override
        public String getTransition() {
            return "Transition";
        }

        @Override
        public String getData() {
            return "Data";
        }

        @Override
        public String getStats() {
            return "";
        }

        @Override
        public String getExternalChildIDs() {
            return "ExternalChildIDs";
        }

        @Override
        public String getExternalId() {
            return "ExternalId";
        }

        @Override
        public String getExternalStatus() {
            return "ExternalStatus";
        }

        @Override
        public String getTrackerUri() {
            return "TrackerUri";
        }

        @Override
        public String getConsoleUrl() {
            return "ConsoleUrl";
        }

        @Override
        public String getErrorCode() {
            return "ErrorCode";
        }

        @Override
        public String getErrorMessage() {
            return "ErrorMessage";
        }
    }

    static final class CoordinatorActionImpl implements CoordinatorAction {
        String id;
        String jobId;
        int actionNumber;
        CoordinatorAction.Status status;
        Date nominalTime;
        Date lastModifiedTime;
        Date createdTime;

        public void setActionNumber(int actionNumber) {
            this.actionNumber = actionNumber;
        }

        public void setLastModifiedTime(Date lastModifiedTime) {
            this.lastModifiedTime = lastModifiedTime;
        }

        @Override
        public Date getLastModifiedTime() {
            return lastModifiedTime;
        }

        public void setNominalTime(Date nominalTime) {
            this.nominalTime = nominalTime;
        }

        @Override
        public Date getNominalTime() {
            return nominalTime;
        }

        public void setStatus(
                org.apache.oozie.client.CoordinatorAction.Status status) {
            this.status = status;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public int getActionNumber() {
            return actionNumber;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        @Override
        public String getJobId() {
            return jobId;
        }

        @Override
        public Date getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(Date createdTime) {
            this.createdTime = createdTime;
        }

        @Override
        public String getCreatedConf() {
            return "CreatedConf";
        }

        @Override
        public String getRunConf() {
            return "RunConf";
        }

        @Override
        public String getMissingDependencies() {
            return "MissingDependencies";
        }

        @Override
        public String getPushMissingDependencies() {
            return "PushMissingDependencies";
        }

        @Override
        public String getExternalStatus() {
            return "ExternalStatus";
        }

        @Override
        public String getTrackerUri() {
            return "TrackerUri";
        }

        @Override
        public String getConsoleUrl() {
            return "ConsoleUrl";
        }

        @Override
        public String getErrorCode() {
            return "ErrorCode";
        }

        @Override
        public String getErrorMessage() {
            return "ErrorMessage";
        }

        @Override
        public void setErrorCode(String errorCode) {
        }

        @Override
        public void setErrorMessage(String errorMessage) {
        }

        @Override
        public String getExternalId() {
            return "ExternalId";
        }
    }

    static final class CoordinatorJobImpl implements CoordinatorJob {
        String id;
        String appName;
        Date startTime;
        Date endTime;
        int concurrency;
        CoordinatorJob.Status status;
        String appPath;
        Date createdTime;
        String user;
        String group;
        Date lastModifiedTime;
        ImmutableList<CoordinatorAction> actions;

        public void setConcurrency(int concurrency) {
            this.concurrency = concurrency;
        }

        @Override
        public int getConcurrency() {
            return concurrency;
        }

        public void setUser(String user) {
            this.user = user;
        }

        @Override
        public String getUser() {
            return user;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        @Override
        @Deprecated
        public String getGroup() {
            return group;
        }

        public void setLastModifiedTime(Date lastModifiedTime) {
            this.lastModifiedTime = lastModifiedTime;
        }

        public void setCreatedTime(Date createdTime) {
            this.createdTime = createdTime;
        }

        @Override
        public void setStatus(Status status) {
            this.status = status;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        public void setStartTime(Date startTime) {
            this.startTime = startTime;
        }

        @Override
        public Date getStartTime() {
            return startTime;
        }

        @Override
        public Date getEndTime() {
            return endTime;
        }

        public void setEndTime(Date endTime) {
            this.endTime = endTime;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        @Override
        public List<CoordinatorAction> getActions() {
            return actions;
        }

        public void setActions(List<CoordinatorAction> actions) {
            this.actions = ImmutableList.copyOf(actions);
        }

        @Override
        public String getAppPath() {
            return appPath;
        }

        public void setAppPath(String appPath) {
            this.appPath = appPath;
        }

        @Override
        public void setPending() {
        }

        @Override
        public void resetPending() {
        }

        @Override
        public Date getPauseTime() {
            return new Date();
        }

        @Override
        public String getExternalId() {
            return "1";
        }

        @Override
        public String getConf() {
            return "Conf";
        }

        @Override
        public int getFrequency() {
            return 0;
        }

        @Override
        public Timeunit getTimeUnit() {
            return Timeunit.DAY;
        }

        @Override
        public String getTimeZone() {
            return "TimeZone";
        }

        @Override
        public Execution getExecutionOrder() {
            return Execution.FIFO;
        }

        @Override
        public int getTimeout() {
            return 0;
        }

        @Override
        public Date getLastActionTime() {
            return new Date();
        }

        @Override
        public Date getNextMaterializedTime() {
            return new Date();
        }

        @Override
        public String getAcl() {
            return "acl";
        }

        @Override
        public String getBundleId() {
            return "BundleId";
        }

        @Override
        public String getConsoleUrl() {
            return "ConsoleUrl";
        }
    }
}
