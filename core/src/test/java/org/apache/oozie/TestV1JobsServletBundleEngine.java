package org.apache.oozie;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletResponse;

import org.apache.oozie.BundleJobBean;
import org.apache.oozie.client.Job;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.rest.BulkResponseImpl;
import org.apache.oozie.client.rest.RestConstants;
import org.apache.oozie.service.Services;
import org.apache.oozie.service.UUIDService;
import org.apache.oozie.servlet.DagServletTestCase;
import org.apache.oozie.servlet.MockDagEngineService;
import org.apache.oozie.servlet.V1JobsServlet;
import org.apache.oozie.test.XDataTestCase;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Ignore;

public class TestV1JobsServletBundleEngine extends DagServletTestCase {
    static {
        new V1JobsServlet();
    }

    private static final boolean IS_SECURITY_ENABLED = false;

    @Ignore
    private static class XDataTestCase1 extends XDataTestCase {}
    private final XDataTestCase1 xDataTestCase = new XDataTestCase1();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        xDataTestCase.setName(getName());
        xDataTestCase.setUpPub();
        
        new Services().init();
        Services services = Services.get();
        services.setService(UUIDService.class);
    }

    @Override
    protected void tearDown() throws Exception {
        xDataTestCase.tearDownPub();
        super.tearDown();
    }

    /**
     * Tests method {@link BundleEngine#getBundleJobs(String, int, int)}.
     * Also tests positive cases of the filter parsing by {@link BundleEngine#parseFilter(String)}.
     */
    public void testGetBundleJobs() throws Exception {
        final BundleJobBean bundleJobBean = xDataTestCase.addRecordToBundleJobTable(Job.Status.PREP, false);
        
        runTest("/v1/jobs", V1JobsServlet.class, IS_SECURITY_ENABLED, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MockDagEngineService.reset();

                Map<String, String> params = new HashMap<String, String>();
                params.put(RestConstants.JOBTYPE_PARAM, "bundle");
                params.put(RestConstants.JOBS_FILTER_PARAM, 
                         OozieClient.FILTER_STATUS+"=PREP;"
                       + OozieClient.FILTER_NAME+"=BUNDLE-TEST;"
                       + OozieClient.FILTER_USER+"="+getTestUser());
                
                URL url = createURL("", params);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                assertEquals(HttpServletResponse.SC_OK, conn.getResponseCode());
                assertTrue(conn.getHeaderField("content-type").startsWith(RestConstants.JSON_CONTENT_TYPE));
                
                JSONObject json = (JSONObject) JSONValue.parse(new InputStreamReader(conn.getInputStream()));
                
                //System.out.println("["+json+"]");
                assertEquals(Long.valueOf(1L), json.get("total"));
                JSONArray array = (JSONArray)json.get("bundlejobs");
                JSONObject jo = (JSONObject)array.get(0);
                assertEquals(bundleJobBean.getId(), jo.get("bundleJobId"));

                return null;
            }
        });
    }
    
    /**
     * Test negative cases of the filter parsing by {@link BundleEngine#parseFilter(String)}.
     */
    public void testParseFilterNegative() {
        BundleEngine be = new BundleEngine();
        // no eq sign in token:
        try {
            be.parseFilter("vinnypooh");
            assertTrue("BundleEngineException expected.", false);
        } 
        catch (BundleEngineException bee) {
            assertTrue(ErrorCode.E0420 == bee.getErrorCode());
        }
        // incorrect key=value pair syntax:
        try {
            be.parseFilter("xx=yy=zz");
            assertTrue("BundleEngineException expected.", false);
        } 
        catch (BundleEngineException bee) {
            assertTrue(ErrorCode.E0420 == bee.getErrorCode());
        }
        // unknown key in key=value pair:
        try {
            be.parseFilter("foo=moo");
            assertTrue("BundleEngineException expected.", false);
        } 
        catch (BundleEngineException bee) {
            assertTrue(ErrorCode.E0420 == bee.getErrorCode());
        }
        // incorrect "status" key value:
        try {
            be.parseFilter("status=foo");
            assertTrue("BundleEngineException expected.", false);
        } 
        catch (BundleEngineException bee) {
            assertTrue(ErrorCode.E0420 == bee.getErrorCode());
        }
    }

    /**
     * Test negative cases of method {@link BundleEngine#parseBulkFilter(String)}
     */
    public void testParseBulkFilterNegative() {
        // incorrect key=value pair syntax:
        try {
            BundleEngine.parseBulkFilter("xx=yy=zz");
            assertTrue("BundleEngineException expected.", false);
        } 
        catch (BundleEngineException bee) {
            assertTrue(bee.toString(), ErrorCode.E0420 == bee.getErrorCode());
        }
        // no eq sign in token:
        try {
            BundleEngine.parseBulkFilter("vinnypooh");
            assertTrue("BundleEngineException expected.", false);
        } 
        catch (BundleEngineException bee) {
            assertTrue(bee.toString(), ErrorCode.E0420 == bee.getErrorCode());
        }
        // one of the values is a whitespace:
        try {
            BundleEngine.parseBulkFilter(BulkResponseImpl.BULK_FILTER_BUNDLE_NAME+"=aaa, ,bbb");
            assertTrue("BundleEngineException expected.", false);
        } 
        catch (BundleEngineException bee) {
            assertTrue(bee.toString(), ErrorCode.E0420 == bee.getErrorCode());
        }
        // unparseable time value:
        try { 
            BundleEngine.parseBulkFilter(BulkResponseImpl.BULK_FILTER_START_CREATED_EPOCH + "=blah-blah");
            assertTrue("BundleEngineException expected.", false);
        } 
        catch (BundleEngineException bee) {
            assertTrue(bee.toString(), ErrorCode.E0420 == bee.getErrorCode());
        }
        // incorrect status:
        try {
            BundleEngine.parseBulkFilter(BulkResponseImpl.BULK_FILTER_STATUS+"=foo");
            assertTrue("BundleEngineException expected.", false);
        } 
        catch (BundleEngineException bee) {
            assertTrue(bee.toString(), ErrorCode.E0420 == bee.getErrorCode());
        }
        // filter does not contain "BulkResponseImpl.BULK_FILTER_BUNDLE_NAME" field:
        try {
            BundleEngine.parseBulkFilter(BulkResponseImpl.BULK_FILTER_LEVEL + "=foo");
            assertTrue("BundleEngineException expected.", false);
        } 
        catch (BundleEngineException bee) {
            assertTrue(bee.toString(), ErrorCode.E0305 == bee.getErrorCode());
        }
    }
    
}
