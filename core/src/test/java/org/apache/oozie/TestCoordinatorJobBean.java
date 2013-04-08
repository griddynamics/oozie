package org.apache.oozie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.oozie.client.CoordinatorJob.Execution;
import org.apache.oozie.client.CoordinatorJob.Timeunit;
import org.apache.oozie.client.Job.Status;
import org.apache.oozie.client.rest.JsonCoordinatorAction;
import org.apache.oozie.test.XTestCase;
import org.apache.oozie.util.DateUtils;

public class TestCoordinatorJobBean extends XTestCase {

    private static final Date ct = new Date(1L); 
    private static final Date et = new Date(2L); 
    private static final Date st = new Date(3L);
    private static final Date lat = new Date(3L);
    private static final Date lmt = new Date(4L);
    private static final Date nmt = new Date(5L);
    private static final Date pt = new Date(6L);
    private static final Date spt = new Date(7L);
    private static final List<? extends JsonCoordinatorAction> actionList  
        = Collections.singletonList(new CoordinatorActionBean());
    private static final Execution execution = Execution.LIFO;

    private void set(CoordinatorJobBean bean) {
      bean.setActions(actionList);
      bean.setAppName("an");
      bean.setAppNamespace("ans");
      bean.setAppPath("ap");
      bean.setAuthToken("at");
      bean.setBundleId("bi");
      bean.setConcurrency(2);
      bean.setConf("c");
      bean.setConsoleUrl("cu");
      bean.setCreatedTime(ct);
      bean.setDoneMaterialization();
      bean.setEndTime(et);
      bean.setEndTimestamp(DateUtils.convertDateToTimestamp(et));
      bean.setExecution(execution);
      bean.setExecutionOrder(execution);
      bean.setExternalId("ei");
      bean.setFrequency(3);
      bean.setGroup("group");
      bean.setId("id");
      bean.setJobXml("jx");
      bean.setLastActionNumber(4);
      bean.setLastActionTime(lat);
      bean.setLastActionTimestamp(DateUtils.convertDateToTimestamp(lat));
      bean.setLastModifiedTime(lmt);
      bean.setLastModifiedTimestamp(DateUtils.convertDateToTimestamp(lmt));
      bean.setMatThrottling(10);
      bean.setNextMaterializedTime(nmt);
      bean.setNextMaterializedTimestamp(DateUtils.convertDateToTimestamp(nmt));
      bean.setOrigJobXml("ojx");
      bean.setPauseTime(pt);
      bean.setPending();
      bean.setSlaXml("sx");
      bean.setStartTime(st);
      bean.setStartTimestamp(DateUtils.convertDateToTimestamp(st));
      bean.setStatus(Status.KILLED);
      bean.setSuspendedTime(spt);
      bean.setSuspendedTimestamp(DateUtils.convertDateToTimestamp(spt));
      bean.setTimeout(11);
      bean.setTimeUnit(Timeunit.MINUTE);
      bean.setTimeZone("GMT");
      bean.setUser("u");
    }
    
    /**
     * Test {@link CoordinatorJobBean} get- and set- methods.
     */
    public void testSetGet() {
      final CoordinatorJobBean bean = new CoordinatorJobBean();
      set(bean);
      testGet(bean, true);
    }
    
    private void testGet(CoordinatorJobBean bean, boolean checkTransient) {
        if (checkTransient) {
            assertEquals(actionList, bean.getActions());
        }
        assertEquals("an", bean.getAppName());
        assertEquals("ans", bean.getAppNamespace());
        assertEquals("ap", bean.getAppPath());
        if (checkTransient) {
            assertEquals("at", bean.getAuthToken());
            assertEquals("bi", bean.getBundleId());
        }
        assertEquals(2, bean.getConcurrency());
        assertEquals("c", bean.getConf());
        if (checkTransient) {
            assertEquals("cu", bean.getConsoleUrl());
            assertEquals(ct, bean.getCreatedTime());
        }
        assertEquals(true, bean.isDoneMaterialization());
        assertEquals(et, bean.getEndTime());
        assertEquals(DateUtils.convertDateToTimestamp(et), bean.getEndTimestamp());
        if (checkTransient) {
            assertEquals(execution.toString(), bean.getExecution());
        }
        assertEquals(execution, bean.getExecutionOrder());
        assertEquals("ei", bean.getExternalId());
        assertEquals(3, bean.getFrequency());
        assertEquals("group", bean.getGroup());
        assertEquals("id", bean.getId());
        if (checkTransient) {
            assertEquals("jx", bean.getJobXml());
            assertEquals(4, bean.getLastActionNumber());
        }
        assertEquals(lat, bean.getLastActionTime());
        assertEquals(DateUtils.convertDateToTimestamp(lat), bean.getLastActionTimestamp());
        if (checkTransient) {
            assertEquals(lmt, bean.getLastModifiedTime());
            assertEquals(DateUtils.convertDateToTimestamp(lmt), bean.getLastModifiedTimestamp());
        }
        assertEquals(10, bean.getMatThrottling());
        assertEquals(nmt, bean.getNextMaterializedTime());
        assertEquals(DateUtils.convertDateToTimestamp(nmt), bean.getNextMaterializedTimestamp());
        if (checkTransient) {
            assertEquals("ojx", bean.getOrigJobXml());
            assertEquals(pt, bean.getPauseTime());
        }
        assertEquals(true, bean.isPending());
        if (checkTransient) {
            assertEquals("sx", bean.getSlaXml());
        }
        assertEquals(st, bean.getStartTime());
        assertEquals(DateUtils.convertDateToTimestamp(st), bean.getStartTimestamp());
        assertEquals(Status.KILLED, bean.getStatus());
        if (checkTransient) {
            assertEquals(spt, bean.getSuspendedTime());
            assertEquals(DateUtils.convertDateToTimestamp(spt), bean.getSuspendedTimestamp());
        }
        assertEquals(11, bean.getTimeout());
        assertEquals(Timeunit.MINUTE, bean.getTimeUnit());
        assertEquals("GMT", bean.getTimeZone());
        assertEquals("u", bean.getUser());
    }
    
    /**
     * Test {@link CoordinatorJobBean} serialization and deserialization.
     */
    public void testSerialization() throws IOException {
        final CoordinatorJobBean bean = new CoordinatorJobBean();
        
        set(bean);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        bean.write(dos);
        dos.close();
        
        final CoordinatorJobBean bean2 = new CoordinatorJobBean();
        bean2.readFields(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
        
        testGet(bean2, false);
    }
}
