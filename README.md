# IMPORTANT DISCLAIMER
This project was created as a _<u>personal database logging experiment</u>_.<br>
There are much better pre-existing solutions that can do the same (or similar)

## Useful Links to some better alternatives
* <a href="https://p6spy.readthedocs.io/en/latest/index.html">P6SPY</a><br>
* <a href="https://github.com/gavlyukovskiy/spring-boot-data-source-decorator">Spring Boot Data Source Decorator</a><br>
* <a href="https://github.com/ttddyy/datasource-proxy">DataSource Proxy</a> (and <a href="https://github.com/ttddyy/datasource-proxy-examples">examples</a>)<br>

-----
-----


## JDBC-Logger
JDBC-Logger is a alternative module for logging JDBC SQL statements in an application.  Will print out 'real-looking' SQL statements.

Motivated by the fact that: ***SQL Statement logs with silly Question Marks (?'s) drive me bonkers!.***

## Output Comparisons
1 **JDBC-Logger (THIS PROJECT)**
```sql
insert into employee (active, first_name, hire_date, last_name, level, notes, salary, id) 
values (1, 'George', '2021-11-07 02:22:04', 'Washington', 9, null, 55123.43, 1)
```
2 **Existing Hibernate logging (DEBUG)**
```sql
insert into employee (active, first_name, hire_date, last_name, level, notes, salary, id) 
values (?, ?, ?, ?, ?, ?, ?, ?)
```
3 **Existing Hibernate logging (TRACE)**
```sql
insert into employee (active, first_name, hire_date, last_name, level, notes, salary, id) 
values (?, ?, ?, ?, ?, ?, ?, ?)
binding parameter [1] as [BOOLEAN] - [true]
binding parameter [2] as [VARCHAR] - [George]
binding parameter [3] as [TIMESTAMP] - [Sat Nov 06 19:22:04 PDT 2021]
binding parameter [4] as [VARCHAR] - [Washington]
binding parameter [5] as [INTEGER] - [9]
binding parameter [6] as [VARCHAR] - [null]
binding parameter [7] as [DOUBLE] - [55123.43]
binding parameter [8] as [BIGINT] - [1]
```
# Usage Examples

## Example #1 - Simplest Logging DataSource
```
DataSource dataSource = new LoggingDataSource(__ORIGINAL_DATA_SOURCE__, __YOUR_LOGGER_HERE__);
```

## Example #2 - Inject Into SpringBoot
```java
@Configuration
public class AppConfig {
    @Autowired
    private DataSourceProperties dataSourceProperties;

    @Primary
    @Bean
    @ConfigurationProperties(prefix = "datasource")
    public DataSource dataSource()
    {
        DataSource innerDataSource = dataSourceProperties.initializeDataSourceBuilder().build();
        return new LoggingDataSource(innerDataSource, __YOUR_LOGGER_HERE__);
    }
}
```
## Example 3 - Simplest Logging Connection
```
Connection innerConn = DriverManager.getConnection("jdbc:hsqldb:mem:sampleDB", "SA", "");
Connection loggingConn = new LoggingConnection(innerConn, __YOUR_LOGGER_HERE__)
```
## Example 4 - Custom Data Source Via Builder
```
DbLoggingBuilder.builder()
        // include 2 logging listeners
        .setLoggingListeners(new Slf4jLoggingListener(logger), new CustomLoggingListener())
        // attempt to log 'actual' string value for any CLOB objects
        .setClobParamLogging(true)
        // date string based on PST timezone (instead of default UTC)
        .setZone("PST")
        // create LoggingDataSource (wrapping the original dataSource)
        .createFrom(innerDataSource);
... 

public class CustomLoggingListener implements LoggingListener {
    @Override
    public void log(String sql) {
        // special logger handling here
    }
}
```
# Additional Notes
## Known Issues
1. The SQL statement is logged immediately **BEFORE** the SQL is actually executed (so it gets logged even if there was a SQL Exception)
2. All dates are logged using the UTC timezone by default.
3. Project still needs javadocs and Readme updates.
4. Still making code tweaks sporadically.

## NOT Implemented
1. CallableStatement SQL param logging when setting parameter by _NAME_
2. PreparedStatement `setArray` method

## Testing
### What WAS Tested
1. Most 'happy path' cases,
2. Testing with "HSQLDB" driver
### What WAS NOT Tested
1. Non-HsqlDb drivers
2. No Load/Performance testing was conducted. 
3. This does NOT check for any Sql-Injection vulnerabilities (i.e. Log4J)
## Other
1. Take a look at the nested Demo project and unittests for other usages