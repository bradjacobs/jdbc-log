package com.github.bradjacobs.logging.jdbc.param;

import com.github.bradjacobs.logging.jdbc.AbstractLoggingBuilder;
import com.github.bradjacobs.logging.jdbc.DatabaseType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SqlTagFillerTest {
    private static final String GENERIC_SQL_NO_PARAMS = "SELECT * FROM table";
    private static final String GENERIC_SQL_ONE_PARAM = "SELECT * FROM table WHERE field = ?";

    // todo - move this to common home so it's not redundant with other tests
    private static final long TEST_DATE_LONG = 1538014031000L;
    private static final Date TEST_DATE = new Date(TEST_DATE_LONG);
    private static final String EXPECTED_DATETIME_UTC = "'2018-09-27 02:07:11'";
    private static final String EXPECTED_DATETIME_PACIFIC = "'2018-09-26 19:07:11'";
    private static final String EXPECTED_DATE_UTC = "'2018-09-27'";
    private static final String EXPECTED_DATE_PACIFIC = "'2018-09-26'";
    private static final String EXPECTED_TIME_UTC = "'02:07:11'";
    private static final String EXPECTED_TIME_PACIFIC = "'19:07:11'";


    @ParameterizedTest
    @MethodSource("singleParamProvider")
    public void testSingleParamReplacement(Object input, String expected) {
        SqlTagFiller sqlTagFiller = createDefaultSqlTagFiller();
        String sql = sqlTagFiller.replace(GENERIC_SQL_ONE_PARAM, Collections.singletonMap(1, input));

        String bassSql = GENERIC_SQL_ONE_PARAM.replace("?", "");
        String resultParam = sql.replace(bassSql, "");
        assertEquals(expected, resultParam);
    }

    @Test
    public void testNullSource() {
        // pass in null sql, get back null sql
        SqlTagFiller sqlTagFiller = createDefaultSqlTagFiller();
        String sql = sqlTagFiller.replace(null, Collections.singletonMap(1, "abc"));
        assertNull(sql);
    }

    @Test
    public void testNoParamsSqlString() {
        // sql string doesn't have any ?'s, so nothing to substitute
        SqlTagFiller sqlTagFiller = createDefaultSqlTagFiller();
        String sql = sqlTagFiller.replace(GENERIC_SQL_NO_PARAMS, Collections.singletonMap(1, "abc"));
        assertEquals(GENERIC_SQL_NO_PARAMS, sql);
    }

    @Test
    public void testNullParmasMap() {
        // with no param map, the sql is unaltered
        SqlTagFiller sqlTagFiller = createDefaultSqlTagFiller();
        String sql = sqlTagFiller.replace(GENERIC_SQL_ONE_PARAM, null);
        assertEquals(GENERIC_SQL_ONE_PARAM, sql);
    }

    @Test
    public void testEmptyParmasMap() {
        // with no param map, the sql is unaltered
        SqlTagFiller sqlTagFiller = createDefaultSqlTagFiller();
        String sql = sqlTagFiller.replace(GENERIC_SQL_ONE_PARAM, new HashMap<>());
        assertEquals(GENERIC_SQL_ONE_PARAM, sql);
    }

    @Test
    public void testMultipeParameters() {
        Map<Integer,Object> paramMap = new LinkedHashMap<Integer,Object>(){{
            put(1, "Cat");
            put(2, true);
            put(3, 34);
        }};

        String sqlTemplate = "SELECT * FROM table WHERE field1 = ? AND field2 = ? and field3 = ?";
        String expectedSql = "SELECT * FROM table WHERE field1 = 'Cat' AND field2 = 1 and field3 = 34";

        SqlTagFiller sqlTagFiller = createDefaultSqlTagFiller();
        String sql = sqlTagFiller.replace(sqlTemplate, paramMap);
        assertEquals(expectedSql, sql);
    }

    @Test
    public void testMultipeWithMissingParameters() {
        Map<Integer,Object> paramMap = new LinkedHashMap<Integer,Object>(){{
            put(1, "Cat");
            //purposely leave out param 2
            put(3, 34);
        }};

        String sqlTemplate = "SELECT * FROM table WHERE field1 = ? AND field2 = ? and field3 = ?";
        String expectedSql = "SELECT * FROM table WHERE field1 = 'Cat' AND field2 = ? and field3 = 34";

        SqlTagFiller sqlTagFiller = createDefaultSqlTagFiller();
        String sql = sqlTagFiller.replace(sqlTemplate, paramMap);
        assertEquals(expectedSql, sql);
    }

    @Test
    public void testExtraParametersIgnored() {
        Map<Integer,Object> paramMap = new LinkedHashMap<Integer,Object>(){{
            put(1, "Cat");
            put(2, true);
            put(3, 34);
        }};

        String sqlTemplate = "SELECT * FROM table WHERE field1 = ?";
        String expectedSql = "SELECT * FROM table WHERE field1 = 'Cat'";

        SqlTagFiller sqlTagFiller = createDefaultSqlTagFiller();
        String sql = sqlTagFiller.replace(sqlTemplate, paramMap);
        assertEquals(expectedSql, sql);
    }

    @Test
    public void testNotEnoughParameters() {
        Map<Integer,Object> paramMap = new LinkedHashMap<Integer,Object>(){{
            put(1, "Cat");
        }};

        String sqlTemplate = "SELECT * FROM table WHERE field1 = ? AND field2 = ? and field3 = ?";
        String expectedSql = "SELECT * FROM table WHERE field1 = 'Cat' AND field2 = ? and field3 = ?";

        SqlTagFiller sqlTagFiller = createDefaultSqlTagFiller();
        String sql = sqlTagFiller.replace(sqlTemplate, paramMap);
        assertEquals(expectedSql, sql);
    }

    static Stream<Arguments> singleParamProvider() {
        return Stream.of(
                Arguments.of("foobar", "'foobar'"),
                Arguments.of("don't worry", "'don''t worry'"),
                Arguments.of(6, "6"),
                Arguments.of(-123, "-123"),
                Arguments.of(6.3, "6.3"),
                Arguments.of(0.0000002d, "0.0000002"),
                Arguments.of(null, "null"),
                Arguments.of(true, "1"),
                Arguments.of(false, "0"),
                Arguments.of(new Date(TEST_DATE_LONG), EXPECTED_DATETIME_UTC),
                Arguments.of(new java.sql.Timestamp(TEST_DATE_LONG), EXPECTED_DATETIME_UTC),
                Arguments.of(new java.sql.Date(TEST_DATE_LONG), EXPECTED_DATE_UTC),
                Arguments.of(new java.sql.Time(TEST_DATE_LONG), EXPECTED_TIME_UTC)
        );
    }

    private SqlTagFiller createDefaultSqlTagFiller() {
        return new SqlTagFiller(DatabaseType.DEFAULT, AbstractLoggingBuilder.DEFAULT_ZONE);
    }
}
