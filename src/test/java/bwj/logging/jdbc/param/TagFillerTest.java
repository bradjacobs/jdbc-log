package bwj.logging.jdbc.param;

import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class TagFillerTest
{
    // test default case.   Numbers get filled + the string param gets quoted.
    @Test
    public void testDefaultMultiValues() throws Exception
    {
        String sql = "select * from myTable where id = ? AND name = ? AND status > ?";
        Map<Integer, Object> paramMap = new HashMap<>();
        paramMap.put(1, 1);
        paramMap.put(2, "Rufus");
        paramMap.put(3, 200);
        String expectedSql = "select * from myTable where id = 1 AND name = 'Rufus' AND status > 200";

        TagFiller tagFiller = createTestTagFiller();

        String result = tagFiller.replace(sql, paramMap);
        assertNotNull(result);
        assertEquals(result, expectedSql, "mismatch of expected replace tag result");
    }



    /**
     * Can't replace if no data to use to replace with
     */
    @Test
    public void testMissingValueParamMap() throws Exception
    {
        String sql = "select * from myTable where id = ? AND name = ?";
        TagFiller tagFiller = createTestTagFiller();

        String result = tagFiller.replace(sql, null);
        assertNotNull(result);
        assertEquals(result, sql, "mismatch of expected replace tag result");

        String result2 = tagFiller.replace(sql, Collections.emptyMap());
        assertNotNull(result2);
        assertEquals(result2, sql, "mismatch of expected replace tag result");
    }

    /**
     * Replace boolean will set value as either 1 or 0
     */
    @Test
    public void testRenderBoolean() throws Exception
    {
        String sql = "select * from myTable where isOk = ?";
        Map<Integer, Object> paramMap = Collections.singletonMap(1, true);
        String expectedSql = sql.replace("?", "1");

        TagFiller tagFiller = createTestTagFiller();

        String result = tagFiller.replace(sql, paramMap);
        assertNotNull(result);
        assertEquals(result, expectedSql, "mismatch of expected replace tag result");
    }


    /**
     *  if a value is 'missing' then the original ? tag should remain in the final result
     */
    @Test
    public void testMissingParamValue() throws Exception
    {
        String sql = "select * from myTable where id = ? AND name = ? AND status > ?";
        Map<Integer, Object> paramMap = new HashMap<>();
        /////paramMap.put(1, 1);
        paramMap.put(2, "Rufus");
        paramMap.put(3, 200);

        // note first ? is expected to remain
        String expectedSql = "select * from myTable where id = ? AND name = 'Rufus' AND status > 200";

        TagFiller tagFiller = createTestTagFiller();

        String result = tagFiller.replace(sql, paramMap);
        assertNotNull(result);
        assertEquals(result, expectedSql, "mismatch of expected replace tag result");
    }




    /**
     * test w/ one of the params literally is getting set to null
     */
    @Test
    public void testWithNull() throws Exception
    {
        String sql = "update table set field1 = ?";
        Map<Integer,Object> paramMap = Collections.singletonMap(1, null);
        String expectedSql = "update table set field1 = null";

        TagFiller tagFiller = createTestTagFiller();
        String result = tagFiller.replace(sql, paramMap);
        assertNotNull(result);
        assertEquals(result, expectedSql, "mismatch of expected replace tag result");
    }


    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = "Must provide a tag parameter.")
    public void testMissingRenSelector() throws Exception {
        TagFiller tagFiller = new TagFiller(null, null);
    }
    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = "Must provide a tag parameter.")
    public void testMissingTag() throws Exception {
        TagFiller tagFiller = new TagFiller("", null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = "Must provide a rendererSelector.")
    public void testMissingRenSeletor() throws Exception {
        TagFiller tagFiller = new TagFiller(null);
    }



    /**
     * Pass in null sql, get back null sql
     */
    @Test
    public void testMissingSqlSource() throws Exception
    {
        String sql = null;
        TagFiller tagFiller = createTestTagFiller();
        String result = tagFiller.replace(sql, Collections.singletonMap(1, 10));
        assertNull(result);
    }

    /**
     *  And extra tags are basiclaly the same as missing param
     *    the ?'s will remain
     */
    @Test
    public void testExtraTags() throws Exception
    {
        String sql = "select * from myTable where id = ? AND name = ? AND status > ? AND val = ?";
        Map<Integer, Object> paramMap = new HashMap<>();
        paramMap.put(1, 1);
        paramMap.put(2, "Rufus");
        String expectedSql = "select * from myTable where id = 1 AND name = 'Rufus' AND status > ? AND val = ?";

        TagFiller tagFiller = createTestTagFiller();

        String result = tagFiller.replace(sql, paramMap);
        assertNotNull(result);
        assertEquals(result, expectedSql, "mismatch of expected replace tag result");
    }


    /**
     * Replace boolean will set value as either 1 or 0
     */
    @Test
    public void testRenderTimestamp() throws Exception
    {
        long timeLong = 1432260431000L;
        String expectedTimeStringGmt = "'2015-05-22 02:07:11'";
        Timestamp timestamp = new Timestamp(timeLong);
        Map<Integer, Object> paramMap = Collections.singletonMap(1, timestamp);

        String sql = "select * from myTable where updated = ?";
        String expectedSql = sql.replace("?", expectedTimeStringGmt);

        TagFiller tagFiller = createTestTagFiller();

        String result = tagFiller.replace(sql, paramMap);
        assertNotNull(result);
        assertEquals(result, expectedSql, "mismatch of expected replace tag result");
    }

    @Test
    public void testRenderDate() throws Exception
    {
        // NOTE: this value is: Thursday, September 27, 2018 2:07:11 AM  __GMT__
        //     since default render is gmt, make sure get a date string of
        //     '2018-09-27'  (and not '2018-09-26')
        long timeLong = 1538014031000L;

        String expectedTimeStringGmt = "'2018-09-27'";
        java.sql.Date sqlDate = new java.sql.Date(timeLong);

        Map<Integer, Object> paramMap = Collections.singletonMap(1, sqlDate);

        String sql = "select * from myTable where date = ?";
        String expectedSql = sql.replace("?", expectedTimeStringGmt);

        TagFiller tagFiller = createTestTagFiller();

        String result = tagFiller.replace(sql, paramMap);
        assertNotNull(result);
        assertEquals(result, expectedSql, "mismatch of expected replace tag result");
    }


    @Test
    public void testRenderTime() throws Exception
    {
        // NOTE: this value is: Thursday, September 27, 2018 2:07:11 AM  __GMT__
        long timeLong = 1538014031000L;

        String expectedTimeStringGmt = "'02:07:11'"; // gmt time
        java.sql.Time sqlTime = new java.sql.Time(timeLong);

        Map<Integer, Object> paramMap = Collections.singletonMap(1, sqlTime);

        String sql = "select * from myTable where time = ?";
        String expectedSql = sql.replace("?", expectedTimeStringGmt);

        TagFiller tagFiller = createTestTagFiller();

        String result = tagFiller.replace(sql, paramMap);
        assertNotNull(result);
        assertEquals(result, expectedSql, "mismatch of expected replace tag result");
    }



    // Test to see if can configure such that a java.sql.Date will render like a timestamp
    @Test
    public void testOverrideDateRender() throws Exception
    {
        // NOTE: this value is: Thursday, September 27, 2018 2:07:11 AM  __GMT__
        long timeLong = 1538014031000L;

        RendererDefinitions rendereDefinitions = RendererDefinitionsFactory.createDefaultDefinitions("");
        rendereDefinitions.setDateRenderer( rendereDefinitions.getTimestampRenderer() );

        RendererSelector rendererSelector = new RendererSelector(rendereDefinitions);
        TagFiller tagFiller = new TagFiller(rendererSelector);


        String expectedTimeStringGmt = "'2018-09-27 02:07:11'";
        java.sql.Date sqlDate = new java.sql.Date(timeLong);

        Map<Integer, Object> paramMap = Collections.singletonMap(1, sqlDate);

        String sql = "select * from myTable where date = ?";
        String expectedSql = sql.replace("?", expectedTimeStringGmt);

        String result = tagFiller.replace(sql, paramMap);
        assertNotNull(result);
        assertEquals(result, expectedSql, "mismatch of expected replace tag result");
    }



    // Check String renderer when the string value contains a literal '  within
    @Test
    public void testDefaultStringEscaped() throws Exception
    {
        String sql = "select * from myTable where name = ?";
        Map<Integer, Object> paramMap = new HashMap<>();
        paramMap.put(1, "Scott's");
        String expectedSql = "select * from myTable where name = 'Scott''s'";

        TagFiller tagFiller = createTestTagFiller();

        String result = tagFiller.replace(sql, paramMap);
        assertNotNull(result);
        assertEquals(result, expectedSql, "mismatch of expected replace tag result");
    }



    /////////////////////////////////////////////////////////


    private TagFiller createTestTagFiller()
    {
        RendererDefinitions rendereDefinitions = RendererDefinitionsFactory.createDefaultDefinitions("");
        RendererSelector rendererSelector = new RendererSelector(rendereDefinitions);
        return new TagFiller(rendererSelector);
    }

}
