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
package org.apache.oozie.sla;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.oozie.AppType;
import org.apache.oozie.CoordinatorActionBean;
import org.apache.oozie.WorkflowActionBean;
import org.apache.oozie.WorkflowJobBean;
import org.apache.oozie.client.CoordinatorAction;
import org.apache.oozie.client.WorkflowAction;
import org.apache.oozie.client.WorkflowJob;
import org.apache.oozie.client.event.JobEvent;
import org.apache.oozie.client.event.SLAEvent.EventStatus;
import org.apache.oozie.client.event.SLAEvent.SLAStatus;
import org.apache.oozie.client.rest.JsonBean;
import org.apache.oozie.executor.jpa.CoordActionGetForSLAJPAExecutor;
import org.apache.oozie.executor.jpa.JPAExecutorException;
import org.apache.oozie.executor.jpa.WorkflowActionGetForSLAJPAExecutor;
import org.apache.oozie.executor.jpa.WorkflowJobGetForSLAJPAExecutor;

import org.apache.oozie.executor.jpa.sla.SLACalculationInsertUpdateJPAExecutor;
import org.apache.oozie.executor.jpa.sla.SLARegistrationGetOnRestartJPAExecutor;
import org.apache.oozie.executor.jpa.sla.SLASummaryGetJPAExecutor;
import org.apache.oozie.executor.jpa.sla.SLASummaryGetRecordsOnRestartJPAExecutor;
import org.apache.oozie.executor.jpa.sla.SLASummaryUpdateForSLAStatusActualTimesJPAExecutor;
import org.apache.oozie.executor.jpa.sla.SLASummaryUpdateForSLAStatusJPAExecutor;
import org.apache.oozie.service.EventHandlerService;
import org.apache.oozie.service.JPAService;
import org.apache.oozie.service.ServiceException;
import org.apache.oozie.service.Services;
import org.apache.oozie.sla.service.SLAService;
import org.apache.oozie.util.XLog;


/**
 * Implementation class for SLACalculator that calculates SLA related to
 * start/end/duration of jobs using a memory-based map
 */
public class SLACalculatorMemory implements SLACalculator {
    // TODO optimization priority based insertion/processing/bumping up-down
    private static Map<String, SLACalcStatus> slaMap;
    private static Set<String> historySet;
    private static int capacity;
    private static JPAService jpaService;
    private EventHandlerService eventHandler;
    private static int modifiedAfter;
    private static long jobEventLatency;

    @Override
    public void init(Configuration conf) throws ServiceException {
        capacity = conf.getInt(SLAService.CONF_CAPACITY, 5000);
        jobEventLatency = conf.getInt(SLAService.CONF_JOB_EVENT_LATENCY, 90 * 1000);
        slaMap = new ConcurrentHashMap<String, SLACalcStatus>();
        historySet = Collections.synchronizedSet(new HashSet<String>());
        jpaService = Services.get().get(JPAService.class);
        eventHandler = Services.get().get(EventHandlerService.class);
        // load events modified after
        modifiedAfter = conf.getInt(SLAService.CONF_EVENTS_MODIFIED_AFTER, 7);
        loadOnRestart();

    }

    private void loadOnRestart() {
        boolean isJobModified = false;
        try {
            List<SLASummaryBean> summaryBeans = jpaService.execute(new SLASummaryGetRecordsOnRestartJPAExecutor(
                    modifiedAfter));
            for (SLASummaryBean summaryBean : summaryBeans) {
                String jobId = summaryBean.getJobId();
                try {
                    switch (summaryBean.getAppType()) {
                        case COORDINATOR_ACTION:
                            isJobModified = processSummaryBeanForCoordAction(summaryBean, jobId);
                            break;
                        case WORKFLOW_ACTION:
                            isJobModified = processSummaryBeanForWorkflowAction(summaryBean, jobId);
                            break;
                        case WORKFLOW_JOB:
                            isJobModified = processSummaryBeanForWorkflowJob(summaryBean, jobId);
                            break;
                        default:
                            break;
                    }
                    if (isJobModified) {
                        jpaService.execute(new SLASummaryUpdateForSLAStatusActualTimesJPAExecutor(summaryBean));
                    }
                }
                catch (Exception e) {
                    XLog.getLog(SLAService.class).warn("Failed to load records for " + jobId, e);
                }
                try {
                    if (summaryBean.getEventProcessed() == 7) {
                        historySet.add(jobId);
                    }
                    else if (summaryBean.getEventProcessed() <= 7) {
                        SLARegistrationBean slaRegBean = jpaService.execute(new SLARegistrationGetOnRestartJPAExecutor(
                                jobId));
                        SLACalcStatus slaCalcStatus = new SLACalcStatus(summaryBean, slaRegBean);
                        slaMap.put(jobId, slaCalcStatus);
                    }
                }
                catch (Exception e) {
                    XLog.getLog(SLAService.class).warn("Failed to fetch/update records for " + jobId, e);
                }

            }

        }
        catch (Exception e) {
            XLog.getLog(SLAService.class).warn("Failed to retrieve SLASummary records on restart", e);
        }
    }

    private boolean processSummaryBeanForCoordAction(SLASummaryBean summaryBean, String jobId)
            throws JPAExecutorException {
        boolean isJobModified = false;
        CoordinatorActionBean coordAction = null;
        coordAction = jpaService.execute(new CoordActionGetForSLAJPAExecutor(jobId));
        if (!coordAction.getStatusStr().equals(summaryBean.getJobStatus())) {
            XLog.getLog(SLAService.class).trace(
                    "Coordinator action status is " + coordAction.getStatusStr() + " and summary bean status is "
                            + summaryBean.getJobStatus());
            isJobModified = true;
            summaryBean.setJobStatus(coordAction.getStatusStr());
            if (coordAction.isTerminalStatus()) {
                WorkflowJobBean wfJob = jpaService.execute(new WorkflowJobGetForSLAJPAExecutor(coordAction
                        .getExternalId()));
                setSLASummaryBean(summaryBean, wfJob.getStartTime(), coordAction.getLastModifiedTime(),
                        coordAction.getStatusStr());
            }
            else if (coordAction.getStatus() != CoordinatorAction.Status.WAITING) {
                WorkflowJobBean wfJob = jpaService.execute(new WorkflowJobGetForSLAJPAExecutor(coordAction
                        .getExternalId()));
                setSLASummaryBean(summaryBean, summaryBean.getEventProcessed(), wfJob.getStartTime());
            }
        }
        return isJobModified;
    }

    private boolean processSummaryBeanForWorkflowAction(SLASummaryBean summaryBean, String jobId)
            throws JPAExecutorException {
        boolean isJobModified = false;
        WorkflowActionBean wfAction = null;
        wfAction = jpaService.execute(new WorkflowActionGetForSLAJPAExecutor(jobId));
        if (!wfAction.getStatusStr().equals(summaryBean.getJobStatus())) {
            XLog.getLog(SLAService.class).trace(
                    "Workflow action status is " + wfAction.getStatusStr() + "and summary bean status is "
                            + summaryBean.getJobStatus());
            isJobModified = true;
            summaryBean.setJobStatus(wfAction.getStatusStr());
            if (wfAction.inTerminalState()) {
                setSLASummaryBean(summaryBean, wfAction.getStartTime(), wfAction.getEndTime(), wfAction.getStatusStr());
            }
            else if (wfAction.getStatus() != WorkflowAction.Status.PREP) {
                setSLASummaryBean(summaryBean, summaryBean.getEventProcessed(), wfAction.getStartTime());
            }
        }
        return isJobModified;
    }

    private boolean processSummaryBeanForWorkflowJob(SLASummaryBean summaryBean, String jobId)
            throws JPAExecutorException {
        boolean isJobModified = false;
        WorkflowJobBean wfJob = null;
        wfJob = jpaService.execute(new WorkflowJobGetForSLAJPAExecutor(jobId));
        if (!wfJob.getStatusStr().equals(summaryBean.getJobStatus())) {
            XLog.getLog(SLAService.class).trace(
                    "Workflow job status is " + wfJob.getStatusStr() + "and summary bean status is "
                            + summaryBean.getJobStatus());
            isJobModified = true;
            summaryBean.setJobStatus(wfJob.getStatusStr());
            if (wfJob.inTerminalState()) {
                setSLASummaryBean(summaryBean, wfJob.getStartTime(), wfJob.getEndTime(), wfJob.getStatusStr());
            }
            else if (wfJob.getStatus() != WorkflowJob.Status.PREP) {
                setSLASummaryBean(summaryBean, summaryBean.getEventProcessed(), wfJob.getStartTime());
            }
        }
        return isJobModified;
    }

    private void setSLASummaryBean(SLASummaryBean summaryBean, Date startTime, Date endTime, String status) {
        byte eventProc = summaryBean.getEventProcessed();
        summaryBean.setEventProcessed(8);
        summaryBean.setActualStart(startTime);
        summaryBean.setActualEnd(endTime);
        long actualDuration = endTime.getTime() - startTime.getTime();
        summaryBean.setActualDuration(actualDuration);
        if (eventProc < 4) {
            if (status.equals(WorkflowJob.Status.SUCCEEDED.name()) || status.equals(WorkflowAction.Status.OK.name())
                    || status.equals(CoordinatorAction.Status.SUCCEEDED.name())) {
                if (endTime.getTime() <= summaryBean.getExpectedEnd().getTime()) {
                    summaryBean.setSLAStatus(SLAStatus.MET);
                }
                else {
                    summaryBean.setSLAStatus(SLAStatus.MISS);
                }
            }
            else {
                summaryBean.setSLAStatus(SLAStatus.MISS);
            }
        }

    }

    private void setSLASummaryBean(SLASummaryBean summaryBean, byte eventProc, Date startTime) {
        if (((eventProc & 1) == 0)) {
            eventProc += 1;
            summaryBean.setEventProcessed(eventProc);
        }
        if (summaryBean.getSLAStatus().equals(SLAStatus.NOT_STARTED)) {
            summaryBean.setSLAStatus(SLAStatus.IN_PROCESS);
        }
        summaryBean.setActualStart(startTime);
    }

    @Override
    public int size() {
        return slaMap.size();
    }

    @Override
    public SLACalcStatus get(String jobId) {
        return slaMap.get(jobId);
    }

    public Map<String, SLACalcStatus> getMap() {
        return slaMap;
    }

    @Override
    public Iterator<String> iterator() {
        return slaMap.keySet().iterator();
    }

    @Override
    public boolean isEmpty() {
        return slaMap.isEmpty();
    }

    @Override
    public void clear() {
        slaMap.clear();
        historySet.clear();
    }

    /**
     * Invoked via periodic run, update the SLA for registered jobs
     */
    @Override
    public void updateSlaStatus(String jobId) throws JPAExecutorException, ServiceException {
        SLACalcStatus slaCalc = slaMap.get(jobId);
        synchronized (slaCalc) {
            boolean change = false;
            byte eventProc = slaCalc.getEventProcessed();
            SLARegistrationBean reg = slaCalc.getSLARegistrationBean();
            // calculation w.r.t current time and status
            if ((eventProc & 1) == 0) { // first bit (start-processed) unset
                if (reg.getExpectedStart() != null) {
                    if (reg.getExpectedStart().getTime() + jobEventLatency < Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            .getTimeInMillis()) {
                        slaCalc.setEventStatus(EventStatus.START_MISS);
                        change = true;
                        eventHandler.queueEvent(new SLACalcStatus(slaCalc));
                        eventProc++;
                    }
                }
                else {
                    eventProc++; //disable further processing for optional start sla condition
                    change = true;
                }

            }
            if (((eventProc >> 1) & 1) == 0) { // check if second bit (duration-processed) is unset
                if (reg.getExpectedDuration() == -1) {
                    eventProc += 2;
                    change = true;
                }
                else if (slaCalc.getActualStart() != null) {
                    if (reg.getExpectedDuration() + jobEventLatency < Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            .getTimeInMillis() - slaCalc.getActualStart().getTime()) {
                        slaCalc.setEventStatus(EventStatus.DURATION_MISS);
                        change = true;
                        eventHandler.queueEvent(new SLACalcStatus(slaCalc));
                        eventProc += 2;
                    }
                }
            }
            if (eventProc < 4) {
                if (reg.getExpectedEnd().getTime() + jobEventLatency < Calendar
                        .getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis()) {
                    slaCalc.setEventStatus(EventStatus.END_MISS);
                    slaCalc.setSLAStatus(SLAStatus.MISS);
                    change = true;
                    eventHandler.queueEvent(new SLACalcStatus(slaCalc));
                    eventProc += 4;
                }
            }
            if (change) {
                slaCalc.setEventProcessed(eventProc);
                SLASummaryBean slaSummaryBean = new SLASummaryBean();
                slaSummaryBean.setJobId(slaCalc.getId());
                slaSummaryBean.setEventProcessed(eventProc);
                slaSummaryBean.setSLAStatus(slaCalc.getSLAStatus());
                slaSummaryBean.setEventStatus(slaCalc.getEventStatus());
                jpaService.execute(new SLASummaryUpdateForSLAStatusJPAExecutor(slaSummaryBean));
                if (eventProc == 7) {
                    historySet.add(jobId);
                    slaMap.remove(jobId);
                    XLog.getLog(SLAService.class).trace("Removed Job [{0}] from map after End-processed", jobId);
                }

            }
        }
    }

    /**
     * Periodically run by the SLAService worker threads to update SLA status by
     * iterating through all the jobs in the map
     */
    @Override
    public void updateAllSlaStatus() {
        Iterator<String> iterator = slaMap.keySet().iterator();
        while (iterator.hasNext()) {
            String jobId = iterator.next();
            try {
                updateSlaStatus(jobId);
            }
            catch (Exception e) {
                XLog.getLog(SLAService.class).error("Exception in SLA processing for job [{0}]", jobId, e);
            }
        }
    }

    /**
     * Register a new job into the map for SLA tracking
     */
    @Override
    public boolean addRegistration(String jobId, SLARegistrationBean reg) throws JPAExecutorException {
        try {
            if (slaMap.size() < capacity) {
                SLACalcStatus slaCalc = new SLACalcStatus(reg);
                slaCalc.setSLAStatus(SLAStatus.NOT_STARTED);
                slaCalc.setJobStatus(getJobStatus(reg.getAppType()));
                slaMap.put(jobId, slaCalc);
                List<JsonBean> insertList = new ArrayList<JsonBean>();
                insertList.add(reg);
                insertList.add(new SLASummaryBean(slaCalc));
                jpaService.execute(new SLACalculationInsertUpdateJPAExecutor(insertList, null));
                XLog.getLog(SLAService.class).trace("SLA Registration Event - Job:" + jobId);
                return true;
            }
            else {
                XLog.getLog(SLAService.class)
                .error("SLACalculator memory capacity reached. Cannot add or update new SLA Registration entry for job [{0}]",
                reg.getId());
            }
        }
        catch (JPAExecutorException jpa) {
            throw jpa;
        }
        return false;
    }

    private String getJobStatus(AppType appType) {
        String status = null;
        switch (appType) {
            case COORDINATOR_ACTION:
                status = CoordinatorAction.Status.WAITING.name();
                break;
            case WORKFLOW_ACTION:
                status = WorkflowAction.Status.PREP.name();
                break;
            case WORKFLOW_JOB:
                status = WorkflowJob.Status.PREP.name();
                break;
            default:
                break;
        }
        return status;
    }

    /**
     * Update job into the map for SLA tracking
     */
    @Override
    public boolean updateRegistration(String jobId, SLARegistrationBean reg) throws JPAExecutorException {
        try {
            if (slaMap.size() < capacity) {
                SLACalcStatus slaCalc = new SLACalcStatus(reg);
                slaCalc.setSLAStatus(SLAStatus.NOT_STARTED);
                slaCalc.setJobStatus(getJobStatus(reg.getAppType()));
                slaMap.put(jobId, slaCalc);
                List<JsonBean> updateList = new ArrayList<JsonBean>();
                updateList.add(reg);
                updateList.add(new SLASummaryBean(slaCalc));
                jpaService.execute(new SLACalculationInsertUpdateJPAExecutor(null, updateList));
                XLog.getLog(SLAService.class).trace("SLA Registration Event - Job:" + jobId);
                return true;
            }
            else {
                XLog.getLog(SLAService.class)
                .error("SLACalculator memory capacity reached. Cannot add or update new SLA Registration entry for job [{0}]",
                reg.getId());
            }
        }
        catch (JPAExecutorException jpa) {
            throw jpa;
        }
        return false;
    }

    /**
     * Triggered after receiving Job status change event, update SLA status
     * accordingly
     */
    @Override
    public boolean addJobStatus(String jobId, String jobStatus, JobEvent.EventStatus jobEventStatus, Date startTime,
            Date endTime) throws JPAExecutorException, ServiceException {
        SLACalcStatus slaCalc = slaMap.get(jobId);
        SLASummaryBean slaInfo = null;
        boolean hasSla = false;
        if (slaCalc != null) {
            synchronized (slaCalc) {
                slaCalc.setJobStatus(jobStatus);
                switch (jobEventStatus) {
                    case STARTED:
                        slaInfo = processJobStartSLA(slaCalc, startTime);
                        break;
                    case SUCCESS:
                        slaInfo = processJobEndSuccessSLA(slaCalc, startTime, endTime);
                        break;
                    case FAILURE:
                        slaInfo = processJobEndFailureSLA(slaCalc, startTime, endTime);
                        break;
                    default:
                        XLog.getLog(SLAService.class).debug("Unknown Job Status for SLA purpose[{0}]", jobEventStatus);
                        slaInfo = getSLASummaryBean(slaCalc);
                }

                if (slaCalc.getEventProcessed() == 7) {
                    slaInfo.setEventProcessed(8);
                    slaMap.remove(jobId);
                }
                hasSla = true;
            }
            XLog.getLog(SLAService.class).trace("SLA Status Event - Job:" + jobId + " Status:" + slaCalc.getSLAStatus());
        }
        else if (historySet.contains(jobId)) {
            slaInfo = jpaService.execute(new SLASummaryGetJPAExecutor(jobId));
            slaInfo.setJobStatus(jobStatus);
            slaInfo.setActualStart(startTime);
            slaInfo.setActualEnd(endTime);
            if (endTime != null) {
                slaInfo.setActualDuration(endTime.getTime() - startTime.getTime());
            }
            slaInfo.setEventProcessed(8);
            historySet.remove(jobId);
            hasSla = true;
        }

        if (hasSla) {
            jpaService.execute(new SLASummaryUpdateForSLAStatusActualTimesJPAExecutor(slaInfo));
        }
        return hasSla;
    }

    /**
     * Process SLA for jobs that started running. Also update actual-start time
     *
     * @param slaCalc
     * @param actualStart
     * @return SLASummaryBean
     */
    private SLASummaryBean processJobStartSLA(SLACalcStatus slaCalc, Date actualStart) {
        slaCalc.setActualStart(actualStart);
        if (slaCalc.getSLAStatus().equals(SLAStatus.NOT_STARTED)) {
            slaCalc.setSLAStatus(SLAStatus.IN_PROCESS);
        }
        SLARegistrationBean reg = slaCalc.getSLARegistrationBean();
        Date expecStart = reg.getExpectedStart();
        byte eventProc = slaCalc.getEventProcessed();
        // set event proc here
        if (((eventProc & 1) == 0)) {
            if (expecStart != null) {
                if (actualStart.getTime() > expecStart.getTime()) {
                    slaCalc.setEventStatus(EventStatus.START_MISS);
                }
                else {
                    slaCalc.setEventStatus(EventStatus.START_MET);
                }
                eventHandler.queueEvent(new SLACalcStatus(slaCalc));
            }
            eventProc += 1;
            slaCalc.setEventProcessed(eventProc);
        }
        return getSLASummaryBean(slaCalc);
    }

    /**
     * Process SLA for jobs that ended successfully. Also update actual-start
     * and end time
     *
     * @param slaCalc
     * @param actualStart
     * @param actualEnd
     * @return SLASummaryBean
     * @throws JPAExecutorException
     */
    private SLASummaryBean processJobEndSuccessSLA(SLACalcStatus slaCalc, Date actualStart, Date actualEnd) throws JPAExecutorException {
        SLARegistrationBean reg = slaCalc.getSLARegistrationBean();
        slaCalc.setActualStart(actualStart);
        slaCalc.setActualEnd(actualEnd);
        long expectedDuration = reg.getExpectedDuration();
        long actualDuration = actualEnd.getTime() - actualStart.getTime();
        slaCalc.setActualDuration(actualDuration);
        //check event proc
        byte eventProc = slaCalc.getEventProcessed();
        if (((eventProc >> 1) & 1) == 0) {
            if (expectedDuration != -1 && actualDuration > expectedDuration) {
                slaCalc.setEventStatus(EventStatus.DURATION_MISS);
                eventHandler.queueEvent(new SLACalcStatus(slaCalc));
            }
            else if (expectedDuration != -1 && actualDuration <= expectedDuration) {
                slaCalc.setEventStatus(EventStatus.DURATION_MET);
                eventHandler.queueEvent(new SLACalcStatus(slaCalc));
            }
            eventProc += 2;
            slaCalc.setEventProcessed(eventProc);
        }

        if (eventProc < 4) {
            Date expectedEnd = reg.getExpectedEnd();
            if (actualEnd.getTime() > expectedEnd.getTime()) {
                slaCalc.setEventStatus(EventStatus.END_MISS);
                slaCalc.setSLAStatus(SLAStatus.MISS);
            }
            else {
                slaCalc.setEventStatus(EventStatus.END_MET);
                slaCalc.setSLAStatus(SLAStatus.MET);
            }
            eventProc += 4;
            slaCalc.setEventProcessed(eventProc);
            eventHandler.queueEvent(new SLACalcStatus(slaCalc));
        }
        return getSLASummaryBean(slaCalc);
    }

    /**
     * Process SLA for jobs that ended in failure. Also update actual-start and
     * end time
     *
     * @param slaCalc
     * @param actualStart
     * @param actualEnd
     * @return SLASummaryBean
     * @throws JPAExecutorException
     */
    private SLASummaryBean processJobEndFailureSLA(SLACalcStatus slaCalc, Date actualStart, Date actualEnd) throws JPAExecutorException {
        if (actualStart == null) {
            // job failed before starting
            slaCalc.setEventProcessed(7);
            slaCalc.setActualEnd(actualEnd);
            slaCalc.setEventStatus(EventStatus.END_MISS);
            slaCalc.setSLAStatus(SLAStatus.MISS);
            eventHandler.queueEvent(new SLACalcStatus(slaCalc));
            return getSLASummaryBean(slaCalc);
        }
        slaCalc.setActualStart(actualStart);
        slaCalc.setActualEnd(actualEnd);
        SLARegistrationBean reg = slaCalc.getSLARegistrationBean();
        long expectedDuration = reg.getExpectedDuration();
        long actualDuration = actualEnd.getTime() - actualStart.getTime();
        slaCalc.setActualDuration(actualDuration);

        byte eventProc = slaCalc.getEventProcessed();
        if (((eventProc >> 1) & 1) == 0) {
            if (expectedDuration != -1 && actualDuration > expectedDuration) {
                slaCalc.setEventStatus(EventStatus.DURATION_MISS);
                eventHandler.queueEvent(new SLACalcStatus(slaCalc));
            }
            else if (expectedDuration != -1 && actualDuration <= expectedDuration) {
                slaCalc.setEventStatus(EventStatus.DURATION_MET);
                eventHandler.queueEvent(new SLACalcStatus(slaCalc));
            }
            eventProc += 2;
            slaCalc.setEventProcessed(eventProc);
        }

        if (eventProc < 4) {
            slaCalc.setEventStatus(EventStatus.END_MISS);
            slaCalc.setSLAStatus(SLAStatus.MISS);
            eventProc += 4;
            slaCalc.setEventProcessed(eventProc);
            eventHandler.queueEvent(new SLACalcStatus(slaCalc));
        }
        return getSLASummaryBean(slaCalc);
    }

    private SLASummaryBean getSLASummaryBean (SLACalcStatus slaCalc) {
        SLASummaryBean slaSummaryBean = new SLASummaryBean();
        slaSummaryBean.setActualStart(slaCalc.getActualStart());
        slaSummaryBean.setActualEnd(slaCalc.getActualEnd());
        slaSummaryBean.setActualDuration(slaCalc.getActualDuration());
        slaSummaryBean.setSLAStatus(slaCalc.getSLAStatus());
        slaSummaryBean.setEventStatus(slaCalc.getEventStatus());
        slaSummaryBean.setEventProcessed(slaCalc.getEventProcessed());
        slaSummaryBean.setJobId(slaCalc.getId());
        slaSummaryBean.setJobStatus(slaCalc.getJobStatus());
        return slaSummaryBean;
    }

}
