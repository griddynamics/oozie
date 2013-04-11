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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.oozie.client.CoordinatorAction;
import org.apache.oozie.client.CoordinatorJob;
import org.apache.oozie.client.WorkflowAction;
import org.apache.oozie.client.WorkflowJob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class TestOozieCLIMethods extends TestCase {           
    
    static final String jobIdPattern = "Job ID[\\s|:]+";
    static final String jobNamePattern = "Job Name[\\s|:]+";
    static final String appPathPattern = "App Path[\\s|:]+";
    static final String statusPattern = "Status[\\s|:]+RUNNING";
    static final String actionIdPattern = "ID[\\s|:]+";
    static final String actionJobIdPattern = "Job ID[\\s|:]+";
    
    final ImmutableSet<String> actionIds = ImmutableSet.of("1", "2");

    static class DataObject {
        String deamonName;
        String appName;
        String appPath;        
    }
    
    public void testValidatePrintCoordJobMethod() throws IOException {        
        final DataObject dtObject = 
                new DataObject() {{
                    this.deamonName = "test-coord-job";
                    this.appName = "testCoordinatorJobApp";
                    this.appPath = "testCoordinatorJobAppPath";
                }};

        CoordinatorJob coordJob = createCoordinator(dtObject);

        assertPrintCoordJobOutputHeader(readCoordJobOutput(coordJob, true), dtObject);
        assertPrintCoordJobOutputHeader(readCoordJobOutput(coordJob, false), dtObject);
    }

    public void testValidateReadPrintCoordAction() throws IOException {
        final DataObject dtObject = 
                new DataObject() {{
                    this.deamonName = "testCoordinatorAction";
                    this.appName = "testCoordinatorJobApp";
                    this.appPath = "testCoordinatorJobAppPath";
                }};
        
        CoordinatorAction coordinatorAction = createCoordinatorAction(dtObject);
        assertPrintCoordActionOutput(readPrintCoordAction(coordinatorAction), dtObject);
    }

    public void testValidatePrintJob() throws IOException {
        final DataObject dtObject = 
                new DataObject() {{
                    this.deamonName = "testCoordinatorAction";
                    this.appName = "testCoordinatorJobApp";
                    this.appPath = "testCoordinatorJobAppPath";
                }};
                
        WorkflowJob workflowJob = createWorkflowJob(dtObject);
        assertPrintWorkflowJobOutput(readWorkflowJobOutput(workflowJob, true), dtObject);
    }

    private void assertPrintWorkflowJobOutput(String readWorkflowJobOutput, DataObject dtObject) {
        //TO DO : needed assertion
        assertTrue(true);
    }

    private String readWorkflowJobOutput(WorkflowJob workflowJob, boolean b) throws IOException {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PipedOutputStream pipeOut = new PipedOutputStream();
        PipedInputStream pipeIn = new PipedInputStream(pipeOut, 1024 * 50);
        System.setOut(new PrintStream(pipeOut));
        new OozieCLI().printJob(workflowJob, null, true);
        pipeOut.close();
        copyByteStream(pipeIn, outBytes);
        pipeIn.close();
        return new String(outBytes.toByteArray());        
    }

    private void assertPrintCoordActionOutput(String output, DataObject dtObject) {
        assertTrue("assertPrintCoordActionOutput Job ID error ",
                Pattern.compile(actionIdPattern + dtObject.deamonName).matcher(output).find());

        assertTrue("assertPrintCoordActionOutput ID error ",
                Pattern.compile(actionJobIdPattern + dtObject.appName).matcher(output).find());
        
    }

    private String readPrintCoordAction(CoordinatorAction coordinatorAction) throws IOException {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PipedOutputStream pipeOut = new PipedOutputStream();
        PipedInputStream pipeIn = new PipedInputStream(pipeOut, 1024 * 50);
        System.setOut(new PrintStream(pipeOut));
        new OozieCLI().printCoordAction(coordinatorAction, null);
        pipeOut.close();
        copyByteStream(pipeIn, outBytes);
        pipeIn.close();
        return new String(outBytes.toByteArray());
    }

    private void assertPrintCoordJobOutputHeader(String line, DataObject dtObject) {
        assertTrue("testValidatePrintCoordJobMethod Job ID error ",        
                Pattern.compile(jobIdPattern + dtObject.deamonName).matcher(line).find());
        assertTrue("testValidatePrintCoordJobMethod Job Name error ",
                Pattern.compile(jobNamePattern + dtObject.appName).matcher(line).find());
        assertTrue("testValidatePrintCoordJobMethod App Path error ",
                Pattern.compile(appPathPattern +  dtObject.appPath).matcher(line).find());
        assertTrue("testValidatePrintCoordJobMethod Status error",
                Pattern.compile(statusPattern).matcher(line).find());
    }

    private String readCoordJobOutput(CoordinatorJob coordJob, boolean verbose) throws IOException {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PipedOutputStream pipeOut = new PipedOutputStream();
        PipedInputStream pipeIn = new PipedInputStream(pipeOut, 1024 * 50);
        System.setOut(new PrintStream(pipeOut));
        new OozieCLI().printCoordJob(coordJob, null, verbose);
        pipeOut.close();
        copyByteStream(pipeIn, outBytes);
        pipeIn.close();
        return new String(outBytes.toByteArray());
    }

    private static void copyByteStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024 * 4];
        int read;
        while ((read = in.read(buffer)) > -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.close();
    }

    private CoordinatorJob createCoordinator(DataObject dtObject) {
        CoordinatorJobImpl coordJob = new CoordinatorJobImpl();
        coordJob.setId(dtObject.deamonName);
        coordJob.setAppName(dtObject.appName);
        coordJob.setConcurrency(15);
        coordJob.setAppPath(dtObject.appPath);
        coordJob.setStatus(CoordinatorJob.Status.RUNNING);
        coordJob.setCreatedTime(new Date());
        coordJob.setLastModifiedTime(new Date());
        coordJob.setUser("test");
        coordJob.setGroup("test-group");
        coordJob.setStartTime(new Date());
        coordJob.setEndTime(new Date());

        ImmutableList.Builder<CoordinatorAction> builder = ImmutableList
                    .builder();
        
        for (final String id : actionIds)
            builder.add(createCoordinatorAction(new DataObject() {{
                this.deamonName = id;
                this.appName = "testCoordinatorAction";
            }}));

        coordJob.setActions(builder.build());
        return coordJob;
    }

    private WorkflowJob createWorkflowJob(DataObject dtObject) {
        WorkflowJobImpl workflowJob = new WorkflowJobImpl();
        workflowJob.setId(dtObject.deamonName);
        workflowJob.setAppName(dtObject.appName);
        workflowJob.setAppPath(dtObject.appPath);
        workflowJob.setStatus(WorkflowJob.Status.RUNNING);
        //workflowJob.setActions(ImmutableList.of(createAction("1"), createAction("2")));
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
