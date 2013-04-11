package org.apache.oozie;

import org.apache.hadoop.conf.Configuration;
import org.apache.oozie.client.CoordinatorJob;
import org.apache.oozie.client.WorkflowJob;
import org.apache.oozie.test.XTestCase;

/**
 * Test non-argument constructor and methods of {@link BundleEngine} that 
 * either throw exceptions or return null.
 * {@link BundleEngineException} covered as well.
 */
public class TestBundleEngineSimple extends XTestCase {

    public void testGetCoordJob1() {
        BundleEngine be = new BundleEngine();
        try {
            CoordinatorJob cj = be.getCoordJob("foo");
            assertTrue("Expected BundleEngineException was not thrown.", false);
        } 
        catch (BundleEngineException bee) {
            assertTrue(bee.getErrorCode() == ErrorCode.E0301);
        }
    }
    
    public void testGetCoordJob4() {
        BundleEngine be = new BundleEngine();
        try {
            CoordinatorJob cj = be.getCoordJob("foo", "filter", 0, 1);
            assertTrue("Expected BundleEngineException was not thrown.", false);
        } 
        catch (BundleEngineException bee) {
            assertTrue(bee.getErrorCode() == ErrorCode.E0301);
        }
    }
    
    public void testGetJob1() {
        BundleEngine be = new BundleEngine();
        try {
            WorkflowJob wj = be.getJob("foo");
            assertTrue("Expected BundleEngineException was not thrown.", false);
        } 
        catch (BundleEngineException bee) {
            assertTrue(bee.getErrorCode() == ErrorCode.E0301);
        }
    }
    
    public void testGetJob3() {
        BundleEngine be = new BundleEngine();
        try {
            WorkflowJob wj = be.getJob("foo", 0, 1);
            assertTrue("Expected BundleEngineException was not thrown.", false);
        } 
        catch (BundleEngineException bee) {
            assertTrue(bee.getErrorCode() == ErrorCode.E0301);
        }
    }

    @SuppressWarnings("deprecation")
    public void testReRun2() {
        BundleEngine be = new BundleEngine();
        try {
            Configuration c = new Configuration();
            be.reRun("foo", c);
            assertTrue("Expected BundleEngineException was not thrown.", false);
        } 
        catch (BundleEngineException bee) {
            assertTrue(bee.getErrorCode() == ErrorCode.E0301);
        }
    }
    
    public void testGetJobForExternalId() throws BundleEngineException {
        BundleEngine be = new BundleEngine();
        String job = be.getJobIdForExternalId("externalFoo");
        assertTrue(job == null);
    }
}
