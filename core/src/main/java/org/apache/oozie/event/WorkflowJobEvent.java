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

package org.apache.oozie.event;

import java.util.Date;

import org.apache.oozie.client.WorkflowJob;
import org.apache.oozie.client.event.JobEvent;
import org.apache.oozie.service.EventHandlerService;
import org.apache.oozie.util.XLog;

/**
 * Class implementing JobEvent for events generated by Workflow Jobs
 */
public class WorkflowJobEvent extends JobEvent {

    private WorkflowJob.Status status;
    private String errorCode;
    private String errorMessage;
    // TODO more attributes - run, coordName, bundleId
    // for some advanced processing and linking using events

    public WorkflowJobEvent(String id, String parentId, WorkflowJob.Status status, String user, String appName,
            Date startTime, Date endTime) {
        super(AppType.WORKFLOW_JOB, id, parentId, user, appName);
        setStatus(status);
        setStartTime(startTime);
        setEndTime(endTime);
        XLog.getLog(EventHandlerService.class).debug("Event generated - " + this.toString());
    }

    public String getCoordJobId() {
        return null; // TODO extract prefix from coordActionId before '@'
    }

    public WorkflowJob.Status getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String code) {
        errorCode = code;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String msg) {
        errorMessage = msg;
    }

    public void setStatus(WorkflowJob.Status wfstatus) {
        status = wfstatus;
        // set high-level status for event based on low-level actual job status
        // this is to ease filtering on the consumer side
        switch (status) {
            case SUCCEEDED:
                setEventStatus(EventStatus.SUCCESS);
                break;
            case RUNNING:
                setEventStatus(EventStatus.STARTED);
                break;
            case SUSPENDED:
                setEventStatus(EventStatus.SUSPEND);
                break;
            case KILLED:
            case FAILED:
                setEventStatus(EventStatus.FAILURE);
        }
    }

}
