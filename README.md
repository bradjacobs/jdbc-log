# JDBC-Logger

JDBC-Logger is a alternative module for logging JDBC SQL statements in an application.  Will print out 'real-looking' SQL statements.

## Background
This project was created for one reason: ***I can't stand looking at silly Question Marks (?'s) in SQL log statements and want to see the actual values!.***

## Comparisons

1. **Existing Hibernate logging (DEBUG)**
```sql
insert into employee (active, first_name, hire_date, last_name, level, notes, salary, id) 
values (?, ?, ?, ?, ?, ?, ?, ?)
```
2. **Existing Hibernate logging (TRACE)**
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
3 **JDBC-Logger (this probject)**
```sql
insert into employee (active, first_name, hire_date, last_name, level, notes, salary, id) 
values (1, 'George', '2021-11-07 02:23:07', 'Washington', 9, null, 55123.43, 1)
```
