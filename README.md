# JDBC-Logger
JDBC-Logger is a alternative module for logging JDBC SQL statements in an application.  Will print out 'real-looking' SQL statements.

## TLDR / Background
This project was created for one reason: ***SQL Statement logs with silly Question Marks (?'s) drive me bonkers!.***

## Output Comparisons
1 **JDBC-Logger (THIS PROJECT)**
```sql
insert into employee (active, first_name, hire_date, last_name, level, notes, salary, id) 
values (1, 'George', '2021-11-07 02:23:07', 'Washington', 9, null, 55123.43, 1)
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

## Example #1 - Inject Into SpringBoot
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
## Example 2 - Build with Raw Connection
```
Connection innerConn = DriverManager.getConnection("jdbc:hsqldb:mem:sampleDB", "SA", "");

LoggingConnectionCreator loggingConnectionCreator =
        LoggingConnectionCreator.builder()
                .withLogger(__YOUR_LOGGER_HERE__)
                .build();

Connection dbConnection = loggingConnectionCreator.create(innerConn);
```
## Example 3 - Custom Data Source
```
public DataSource getLoggingDataSource(DataSource originalDataSource) {
    LoggingConnectionCreator loggingConnectionCreator = LoggingConnectionCreator.builder()
            .withLogListener(new CustomLoggingListener())  // custome logging listener
            .setClobReaderLogging(true)  // will attempt to log string values of CLOBs  (slow)
            .build();

    return new LoggingDataSource(originalDataSource, loggingConnectionCreator);
}

public class CustomLoggingListener implements LoggingListener {
    @Override
    public void log(String sql) {
        // special logger handling here
    }
}
```
# Additional Notes
## Testing
### What WAS Tested
1. Most 'happy path' cases,
2. Most testing was only done with "hsqldb"
### What WAS NOT Tested
1. Many of the JDBC drivers (they 'should' all acts the same)  Famous last words
2. No Load/Performance testing was conducted. 
3. This does NOT check for any Sql-Injection vulnerabilities (i.e. Log4J)
4. Minimal testing with custom datatime formatting.
## Other
1. Take a look at the nested Demo project and unittests for other usages