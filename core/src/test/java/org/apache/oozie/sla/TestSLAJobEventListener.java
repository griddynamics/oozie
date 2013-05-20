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

import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.oozie.AppType;
import org.apache.oozie.client.CoordinatorAction;
import org.apache.oozie.client.CoordinatorJob;
import org.apache.oozie.client.WorkflowAction;
import org.apache.oozie.client.WorkflowJob;
import org.apache.oozie.client.event.SLAEvent.EventStatus;
import org.apache.oozie.event.CoordinatorActionEvent;
import org.apache.oozie.event.CoordinatorJobEvent;
import org.apache.oozie.event.WorkflowActionEvent;
import org.apache.oozie.event.WorkflowJobEvent;
import org.apache.oozie.executor.jpa.sla.SLACalculatorGetJPAExecutor;
import org.apache.oozie.executor.jpa.sla.SLASummaryGetJPAExecutor;
import org.apache.oozie.service.EventHandlerService;
import org.apache.oozie.service.JPAService;
import org.apache.oozie.service.Services;
import org.apache.oozie.sla.listener.SLAJobEventListener;
import org.apache.oozie.sla.service.SLAService;
import org.apache.oozie.test.XTestCase;
import org.apache.oozie.util.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestSLAJobEventListener extends XTestCase {
    Services services;
    static StringBuilder output = new StringBuilder();

    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        services = new Services();
        Configuration conf = services.getConf();
        conf.set(Services.CONF_SERVICE_EXT_CLASSES, "org.apache.oozie.service.EventHandlerService,"
                + "org.apache.oozie.sla.service.SLAService");
        services.init();
    }

    @Override
    @After
    protected void tearDown() throws Exception {
        services.destroy();
        super.tearDown();
    }

    @Test
    public void testListenerConfigured() throws Exception {
        EventHandlerService ehs = services.get(EventHandlerService.class);
        assertNotNull(ehs);
        assertTrue(SLAService.isEnabled());
        assertTrue(ehs.listEventListeners().contains(SLAJobEventListener.class.getCanonicalName()));
    }

    @Test
    public void testOnJobEvent() throws Exception {
        SLAService slas = services.get(SLAService.class);
        SLAJobEventListener listener = new SLAJobEventListener();
        listener.init(services.getConf());
        // add dummy registration events to the SLAService map
        SLARegistrationBean job = _createSLARegBean("wf1", AppType.WORKFLOW_JOB);
        job.setExpectedStart(DateUtils.parseDateUTC("2012-07-22T00:00Z"));
        slas.addRegistrationEvent(job);
        assertEquals(1, slas.getSLACalculator().size());
        Date actualStart = DateUtils.parseDateUTC("2012-07-22T01:00Z");
        WorkflowJobEvent wfe = new WorkflowJobEvent("wf1", "caId1", WorkflowJob.Status.RUNNING, "user1",
                "wf-app-name1", actualStart, null);
        listener.onWorkflowJobEvent(wfe);
        SLACalcStatus serviceObj = slas.getSLACalculator().get("wf1");
        // check that start sla has been calculated
        assertTrue(serviceObj.isStartProcessed());
        assertEquals(EventStatus.START_MISS, serviceObj.getEventStatus());

        job = _createSLARegBean("wa1", AppType.WORKFLOW_ACTION);
        slas.addRegistrationEvent(job);
        assertEquals(2, slas.getSLACalculator().size());
        job.setExpectedStart(DateUtils.parseDateUTC("2012-07-22T00:00Z"));
        WorkflowActionEvent wae = new WorkflowActionEvent("wa1", "wfId1", WorkflowAction.Status.RUNNING, "user1",
                "wf-app-name1", actualStart, null);
        listener.onWorkflowActionEvent(wae);
        serviceObj = slas.getSLACalculator().get("wa1");
        // check that start sla has been calculated
        assertTrue(serviceObj.isStartProcessed());
        assertEquals(EventStatus.START_MISS, serviceObj.getEventStatus());

        job = _createSLARegBean("cj1", AppType.COORDINATOR_JOB);
        job.setExpectedEnd(DateUtils.parseDateUTC("2012-07-22T01:00Z"));
        slas.addRegistrationEvent(job);
        assertEquals(3, slas.getSLACalculator().size());
        Date actualEnd = DateUtils.parseDateUTC("2012-07-22T00:00Z");
        CoordinatorJobEvent cje = new CoordinatorJobEvent("cj1", "bj1", CoordinatorJob.Status.SUCCEEDED, "user1",
                "coord-app-name1", actualStart, actualEnd);
        listener.onCoordinatorJobEvent(cje);

        // Since serviceObj is removed from memory after END stage
        SLACalculatorBean calc = Services.get().get(JPAService.class).execute(new SLACalculatorGetJPAExecutor("cj1"));
        SLASummaryBean summary = Services.get().get(JPAService.class).execute(new SLASummaryGetJPAExecutor("cj1"));
        // check that end sla has been calculated
        assertTrue(calc.isEndProcessed());
        assertEquals(EventStatus.END_MET, summary.getEventStatus());

        job = _createSLARegBean("ca1", AppType.COORDINATOR_ACTION);
        actualEnd = DateUtils.parseDateUTC("2012-07-22T02:00Z");
        slas.addRegistrationEvent(job);
        assertEquals(3, slas.getSLACalculator().size());
        CoordinatorActionEvent cae = new CoordinatorActionEvent("ca1", "cj1", CoordinatorAction.Status.KILLED, "user1",
                "coord-app-name1", null, actualEnd, null);
        listener.onCoordinatorActionEvent(cae);
        calc = Services.get().get(JPAService.class).execute(new SLACalculatorGetJPAExecutor("ca1"));
        summary = Services.get().get(JPAService.class).execute(new SLASummaryGetJPAExecutor("ca1"));
        // check that start sla has been calculated
        assertTrue(calc.isEndProcessed());
        assertEquals(EventStatus.END_MISS, summary.getEventStatus());

    }

    private SLARegistrationBean _createSLARegBean(String jobId, AppType appType) {
        SLARegistrationBean reg = new SLARegistrationBean();
        reg.setJobId(jobId);
        reg.setAppType(appType);
        return reg;
    }
}
