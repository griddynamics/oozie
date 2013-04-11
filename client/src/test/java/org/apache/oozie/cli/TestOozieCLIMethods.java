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

import junit.framework.TestCase;

import org.apache.oozie.client.CoordinatorAction;
import org.apache.oozie.client.CoordinatorJob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class TestOozieCLIMethods extends TestCase {

    final ImmutableSet<String> actionIds = ImmutableSet.of("1", "2");
    final String coordinatorName = "coord-job-C";
    final String appName = "testApp";
    final String appPath = "testAppPath";
    
    public void testValidatePrintCoordJobMethod() throws IOException {
        CoordinatorJob coordJob = createCoordinator();
        
        ByteArrayOutputStream output = readCoordJobOutput(coordJob, true);
        String line = new String(output.toByteArray());
        assertPrintCoordJobOutputHeader(line);
        
        output = readCoordJobOutput(coordJob, false);
        line = new String(output.toByteArray());
        assertPrintCoordJobOutputHeader(line);
    }

    private void assertPrintCoordJobOutputHeader(String line) {
        assertTrue("testValidatePrintCoordJobMethod Job ID error ",
                line.contains("Job ID : " + coordinatorName));
        assertTrue("testValidatePrintCoordJobMethod Job Name error ",
                line.contains("Job Name    : "+ appName));
        assertTrue("testValidatePrintCoordJobMethod App Path error ",
                line.contains("App Path    : " +  appPath));
        assertTrue("testValidatePrintCoordJobMethod Status error",
                line.contains("Status      : RUNNING"));
    }

    private ByteArrayOutputStream readCoordJobOutput(CoordinatorJob coordJob, boolean verbose) throws IOException {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PipedOutputStream pipeOut = new PipedOutputStream();
        PipedInputStream pipeIn = new PipedInputStream(pipeOut, 1024 * 50);
        System.setOut(new PrintStream(pipeOut));
        new OozieCLI().printCoordJob(coordJob, null, verbose);
        pipeOut.close();
        copyByteStream(pipeIn, outBytes);
        pipeIn.close();
        return outBytes;
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

    private CoordinatorJob createCoordinator() {
        CoordinatorJobImpl coordJob = new CoordinatorJobImpl();
        coordJob.setId(coordinatorName);
        coordJob.setAppName(appName);
        coordJob.setConcurrency(1);
        coordJob.setAppPath(appPath);
        coordJob.setStatus(CoordinatorJob.Status.RUNNING);
        coordJob.setCreatedTime(new Date());
        coordJob.setLastModifiedTime(new Date());
        coordJob.setUser("test");
        coordJob.setGroup("test-group");
        coordJob.setStartTime(new Date());
        coordJob.setEndTime(new Date());

        ImmutableList.Builder<CoordinatorAction> builder = ImmutableList
                .builder();
        for (String id : actionIds)
            builder.add(createAction(id));

        coordJob.setActions(builder.build());
        return coordJob;
    }

    private static CoordinatorAction createAction(String actionId) {
        CoordinatorActionImpl action = new CoordinatorActionImpl();
        action.setId(actionId);
        action.setJobId("jobId");
        action.setActionNumber(11);
        action.setStatus(CoordinatorAction.Status.SUBMITTED);
        action.setNominalTime(new Date());
        action.setLastModifiedTime(new Date());
        action.setCreatedTime(new Date());
        return action;
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
