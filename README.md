
# Quick-Start Guide

The artifact consists of two main components:

1. SQLancer, the tool which we created and extended, and in which we implemented Pivoted Query Synthesis (PQS), to find all bugs reported in the paper.
2. A SQLite database with a list of bugs that we reported and additional meta information.

Both components are expected to be usable with minimal effort. We first provide instructions to check whether the artifact is usable in principle.

To help getting started with the artifact, we recorded a video to explain and demonstrate its main functionality. Please consider watching it (see [tutorial.mp4](tutorial.mp4)).

## SQLancer

SQLancer is a [Java](https://www.java.com/) application built using [Maven](https://maven.apache.org/). Since we believe Java and Maven to be widely-used software, and since Maven takes care of dependency management, we do not provide a VM image or Docker container.

For an up-to-date installation description and source code of SQLancer, please consult the [SQLancer repository](https://github.com/sqlancer/sqlancer/).

As of now, SQLancer requires the following software to be installed:

* [Java 8 or above](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
* [Maven](https://maven.apache.org/install.html)

To download SQLancer, build it, and run PQS using SQLite, run the following (or use the SQLancer version contained in the compressed archive):

```
git clone https://github.com/sqlancer/sqlancer
cd sqlancer
mvn package -DskipTests
cd target
java -jar SQLancer-0.0.1-SNAPSHOT.jar --random-string-generation ALPHANUMERIC_SPECIALCHAR --num-threads 1 sqlite3 --oracle pqs --test-fts false --test-rtree false --test-in-operator false
```

It is expected that progress information, similar to the following, is printed:
```
[2020/08/27 17:21:08] Executed 15949 queries (3189 queries/s; 0.40/s dbs, successful statements: 52%). Threads shut down: 0.
[2020/08/27 17:21:13] Executed 38803 queries (4571 queries/s; 0.00/s dbs, successful statements: 52%). Threads shut down: 0.
[2020/08/27 17:21:18] Executed 60870 queries (4413 queries/s; 0.00/s dbs, successful statements: 52%). Threads shut down: 0.
```

Besides printing the number of queries and databases that are generated on average each second, SQLancer also prints the percentage of SQL statements that are executed successfully. While SQLancer generates syntactically correct statements and queries, not all of them can be successfully executed by the DBMS. For example, an `INSERT` statement can fail when a constraint on a table or column is violated. As another example, a query can fail when division-by-zero error occurs.

The shortcut CTRL+C can be used to terminate SQLancer manually. If SQLancer does not find any bugs, it executes **infinitely**. The option `--num-tries` controls after how many bugs SQLancer terminates. Alternatively, the option `--timeout-seconds` can be used to specify the maximum duration that SQLancer is allowed to run.

Note that general options that are supported by all DBMS-testing implementations (e.g., `--num-threads`) need to precede the name of DBMS to be tested (e.g., `sqlite3`). Options that are supported only for specific DBMS (e.g., `--test-rtree` for SQLite3), or options for which each testing implementation provides different values (e.g. `--oracle PQS`) need to go after the DBMS name.

Using SQLite to evaluate the artifact is most convenient, since it is an embedded DBMS, meaning that the DBMS are included directly within SQLancer using a [JDBC driver](https://docs.oracle.com/javase/tutorial/jdbc/basics/index.html). Note that it uses the latest JDBC driver, and not the latest development version.

Besides for SQLite, PQS is supported also for the following DBMS:
* [MySQL](https://github.com/mysql/mysql-server)
* [PostgreSQL](https://github.com/postgres/postgres/)

If you decide to test these, please download and install their latest version, using the instructions on the respective pages.

## SQLite Database

To view the database's content, we recommend using a GUI like [DB Browser for SQLite](https://sqlitebrowser.org/). Alternatively, you can install [SQLite](https://www.sqlite.org/download.html) and use its CLI:

```
sqlite3 bugs.db
sqlite> SELECT COUNT(*) FROM DBMS_BUGS_TRUE_POSITIVES; -- 96
```

If the output of the query is 96, using the database seems to work.


# Claims

We claim that ...
* all the bugs that we reported can be validated via a database that we distribute as part of the artifact.
* the key statistics reported in the paper can be validated by using the described SQL commands on the database.
* SQLancer's PQS implementation is exercisable and corresponds to what is described in the paper (see # Context and Caveats for details).
* that SQLancer's implementation is clean (i.e., we use multiple quality-assurance tools) and exercisable (we run integration tests for each pull request).

# Context and Caveats

PQS is the first in a series of approaches to find logic bugs in DBMS that we proposed. The follow-up approaches are [NoREC](https://arxiv.org/abs/2007.08292) and [TLP](https://www.manuelrigger.at/preprints/TLP.pdf), which we also implemented in SQLancer. When starting with implementing NoREC, which was more than one year ago, we no longer maintained the PQS implementation, which thus quickly became outdated. However, at the same time, SQLancer has gained significant additional functionality, such as support for user options, additional support for statements and expressions, as well as a cleaner and better readable codebase. For the artifact, we thus either had the choice to submit our old prototype, or fix and refactor the PQS implementation based on the current SQLancer implementation.

We eventually decided to fix and refactor the PQS implementation based on the current SQLancer version, since we expect that this will benefit the research community in the longer term. In terms of benefits, besides the improvements to SQLancer itself, we intend to actively maintain SQLancer and its PQS implementation [on GitHub](https://github.com/sqlancer/sqlancer/), such that the PQS implementation will also benefit from future improvements of SQLancer. To this end, we have invested significant additional effort and [opened more than 30 pull requests](https://github.com/sqlancer/sqlancer/issues/161). However, this also means that the SQLancer version in which we conducted the experiments described in the paper differs from the one contained in the artifact. While we believe that the description of the implementation in the paper is up-to-date and correct (except the size of the implementation), it could be that inadvertent changes have caused changes in the PQS implementation. Furthermore, since we have significantly changed the code base within a week's time, the PQS implementation is less mature than the other parts of SQLancer's code base, and might still contain bugs.

Since SQLancer has evolved, running SQLancer with the default options does not work when using PQS. For example, consider the arguments for SQLite above. The `--test-fts false` and `--test-rtree false` options disable support for testing SQLite extensions (i.e., the full-text search and R*-Tree extensions), which are not supported for PQS. The `--random-string-generation ALPHANUMERIC_SPECIALCHAR` extension controls which random strings are generated, and the range of supported semantics differs for each of the implementations. The option `--test-in-operator false` disables testing the IN operator, since we recently detected a regression bug using PQS that is triggered when using this option (see # Reproducing Bugs using SQLancer).

# Validating the Bug Database

To validate the bugs that we reported, you can inspect the database distributed with the artifact. For most bug reports (excluding those where we reported a potential security vulnerability), you can find either an `URL_EMAIL` or `URL_BUGTRACKER` entry. If the bug was fixed, you can also find an entry in `URL_FIX`. For SQLite, the bugtracker is accessible only for the developers, which is why we initially reported our bugs on the mailing list; however, subsequently, the developers also gave us access to the bugtracker, which is why continued reporting bugs there. Note that the mailing list was shut down; all old bug reports were transferred to a forum (see https://sqlite.org/forum/forummain). Since PostgreSQL lacks a bugtracker, we reported all bugs on the mailing list.

As an example, consider the bug report with an entry `https://www.sqlite.org/src/tktview/3182d3879020ef3b2e6db56be2470a0266d3c773` in `URL_BUGTRACKER`. On the mailing list, we reported the following test case (note that the bug tracker contains a different test case):

```sql
CREATE TABLE test (c1 TEXT PRIMARY KEY) WITHOUT ROWID;
CREATE INDEX index_0 ON test(c1 COLLATE NOCASE);
INSERT INTO test(c1) VALUES ('A');
INSERT INTO test(c1) VALUES ('a');
SELECT * FROM test; -- only one row is fetched
```

The bug was fixed in commit `1b1dd4d48cd79a58`. We can reproduce the bug by [following the instructions to obtain and build the development version of SQLite](https://sqlite.org/src/doc/trunk/README.md).

For example, on Ubuntu, we can use the following commands to install fossil and clone the SQLite repository:

```
sudo apt-get install fossil
fossil clone https://www.sqlite.org/src sqlite.fossil
fossil open sqlite.fossil
```

Next, we can obtain metainformation about the commit that addressed the bug that we reported:

```
fossil info 1b1dd4d48cd79a58
uuid:         1b1dd4d48cd79a585e1fa7ee79128e9f2a9ee984 2019-04-29 13:48:45 UTC
parent:       92facbc73a940d2844ac88fafd2d2dadb10886fb 2019-04-29 11:41:46 UTC
merged-from:  740d5ff6cc9bf7b151dfb8b27409e5923cfb2789 2019-04-29 13:30:16 UTC
child:        a27b0b880d76c6838c0365f66bcd69b1b49b7594 2019-04-29 16:44:11 UTC
tags:         trunk
comment:      Do not de-duplicate columns index columns associated with a WITHOUT ROWID table if the columns have different collating sequences. This is the fix for ticket [3182d3879020ef3b2]. There is one test case added, but most of the tests are done in TH3. (user: drh)
```

Based on this, we can check-out the parent commit, on which we found the bug:

```
fossil checkout 92facbc73a940d2844ac88fafd2d2dadb10886fb
./configure
make
```

We can reproduce the bug by passing the test case to SQLite:

```
$ ./sqlite3 
SQLite version 3.29.0 2019-04-29 11:41:46
Enter ".help" for usage hints.
Connected to a transient in-memory database.
Use ".open FILENAME" to reopen on a persistent database.
sqlite> CREATE TABLE test (c1 TEXT PRIMARY KEY) WITHOUT ROWID;
sqlite> CREATE INDEX index_0 ON test(c1 COLLATE NOCASE);
sqlite> INSERT INTO test(c1) VALUES ('A');
sqlite> INSERT INTO test(c1) VALUES ('a');
sqlite> SELECT * FROM test; -- only one row is fetched
a
sqlite> 
```
As reported, only a single row has been fetched. Next, we build the version of SQLite, in which the bug was addressed:

```
fossil checkout 1b1dd4d48cd79a58
./configure
make
```

As expected, the bug is no longer reproducible:

```
./sqlite3
SQLite version 3.29.0 2019-04-29 13:48:45
Enter ".help" for usage hints.
Connected to a transient in-memory database.
Use ".open FILENAME" to reopen on a persistent database.
sqlite> CREATE TABLE test (c1 TEXT PRIMARY KEY) WITHOUT ROWID;
sqlite> CREATE INDEX index_0 ON test(c1 COLLATE NOCASE);
sqlite> INSERT INTO test(c1) VALUES ('A');
sqlite> INSERT INTO test(c1) VALUES ('a');
sqlite> SELECT * FROM test; -- only one row is fetched
A
a
```

When looking at the query `SELECT * FROM test`, it is not immediately clear how PQS relates to the test case. As described in the paper, we automatically and manually reduced all PQS test cases before reporting them. We claim that it is clear for all reported test cases that PQS could have detected them. For example, in this test case, we could have selected 'A' as the pivot row, and then generated a `TRUE` constant to be used in the `WHERE` clause, which would be guaranteed to fetch the pivot row.

# Validating the Statistics in the Paper Based on the Bug Database

Based on the bug database provided by the artifact, it is possible to validate the key statistics mentioned in the paper (such as the number of found bugs).

## Abstract

> In total, we reported 121 bugs in these DBMS, 96 of which have been fixed or verified, demonstrating that the approach is highly effective and general.

```sql
SELECT COUNT(*) FROM DBMS_BUGS; -- 121
SELECT COUNT(*) FROM DBMS_BUGS_TRUE_POSITIVES; -- 96
```
## Introduction

> In total, we found 96 bugs, namely 64 bugs in SQLite, 24 bugs in MySQL, and 8 in PostgreSQL, demonstrating that the approach is highly effective and general. 61 of these were logic bugs found by the containment oracle. In addition, we found 32 bugs by causing DBMS-internal errors, such as database corruptions, and for 3 bugs we caused DBMS crashes (i.e., SEGFAULTs).

```sql
SELECT COUNT(*) FROM DBMS_BUGS_TRUE_POSITIVES; -- 96
SELECT database, COUNT(database) FROM DBMS_BUGS_TRUE_POSITIVES GROUP BY database ORDER BY COUNT(database) DESC; {sqlite|64, mysql|24, postgres|8}
SELECT ORACLE, sum(count) FROM ORACLES_AGGREGATED GROUP BY ORACLE; -- {contains|61, error|32, segfault|3}
```

## Evaluation

> We considered 96 bugs as true bugs, as they resulted in code fixes (78 reports), documentation fixes (8 reports), or were confirmed by the developers (10 reports). We opened 25 bug reports that we classified as false bugs, because behavior exhibited in the bug reports was considered to work as intended (13 reports) or because bugs that we reported were considered to be duplicates (12 reports, e.g., because a bug had already been fixed on the latest non-release version).

```sql
SELECT COUNT(*) FROM DBMS_BUGS_TRUE_POSITIVES; -- 96
SELECT STATUS, SUM(count) FROM DBMS_BUGS_STATUS GROUP BY STATUS ORDER BY SUM(count) DESC; -- {fixed|78, fixed (in documentation)|8, verified|10, closed (not a bug)|13, closed (duplicate)|12}
```

> 14 bugs were classified as Critical, 8 bugs as Severe, and 16 as Important. For 13 bugs, we reported them on the mailing list and no entry in the bug tracker was created.

```sql 
SELECT severity, COUNT(*) FROM DBMS_BUGS_TRUE_POSITIVES WHERE SEVERITY IN ("Critical", "Severe", "Important", "None") AND DATABASE='sqlite' GROUP BY severity -- {14|Critical, 8|Severe, 16|Important, 13|None}
```

> Table 2: Total number of reported bugs and their status.

```sql
SELECT DATABASE, STATUS, count FROM DBMS_BUGS_STATUS UNION SELECT DATABASE, 'fixedInDocsOrCode' as STATUS, SUM(count) FROM DBMS_BUGS_STATUS WHERE STATUS IN ('fixed', 'fixed (in documentation)') GROUP BY database;
```

> Table 3: The oracles and how many bugs they found.

```sql
SELECT DATABASE, ORACLE, count FROM ORACLES_AGGREGATED
UNION 
SELECT 'sum' as DATABASE, ORACLE, sum(count) FROM ORACLES_AGGREGATED GROUP BY ORACLE;
```

> Our automatically and manually reduced test cases typically comprised only a few SQL statements (3.71 LOC on average).

```sql
SELECT ROUND(AVG(count), 2) FROM (SELECT COUNT(*) as count FROM BUG_TEST_CASES WHERE id IN (SELECT id FROM DBMS_BUGS_TRUE_POSITIVES) GROUP BY id); -- 3.71
```

> For 13 test cases, a single line was sufficient.

```sql
SELECT COUNT(*) FROM (SELECT COUNT(*) FROM BUG_TEST_CASES_NO_FP s  JOIN DBMS_BUGS r WHERE s.id = r.id GROUP BY r.id HAVING COUNT(*) = 1); -- 13
```

> The maximum number of statements required to reproduce a bug was 8.

```sql
SELECT MAX(count) FROM (SELECT COUNT(*) as count FROM BUG_TEST_CASES WHERE id IN (SELECT id FROM DBMS_BUGS_TRUE_POSITIVES) GROUP BY id); -- 8
```

> The most common constraint was UNIQUE (appearing in 21.9% of the test cases).

```sql
SELECT ROUND(COUNT(DISTINCT id) * 100.0 / (SELECT COUNT(*) FROM DBMS_BUGS_TRUE_POSITIVES), 1) FROM BUG_TEST_CASES WHERE STATEMENT LIKE "%UNIQUE%" AND STATEMENT NOT LIKE "%UNIQUE INDEX%" AND id IN (SELECT id FROM DBMS_BUGS_TRUE_POSITIVES) -- 21.9
```

> Also PRIMARY KEY columns were frequent (16.7%). 

```sql
SELECT ROUND(COUNT(DISTINCT id) * 100.0 / (SELECT COUNT(*) FROM DBMS_BUGS_TRUE_POSITIVES), 1) FROM BUG_TEST_CASES WHERE STATEMENT LIKE "%PRIMARY KEY%" AND id IN (SELECT id FROM DBMS_BUGS_TRUE_POSITIVES); -- 16.7
```

> explicit indexes, created by CREATE INDEX were more common, however (27.1%).

```sql
SELECT ROUND(COUNT(DISTINCT id) * 100.0 / (SELECT COUNT(*) FROM DBMS_BUGS_TRUE_POSITIVES), 1) FROM BUG_TEST_CASES WHERE STATEMENT LIKE "%INDEX%" AND STATEMENT NOT LIKE "%REINDEX%" AND STATEMENT NOT LIKE "%functional index%" AND id IN (SELECT id FROM DBMS_BUGS_TRUE_POSITIVES) -- 27.1
```

> Other constraints were uncommon, for example, FOREIGN KEYs appeared only in 1.0% of the bug reports.

```sql
SELECT ROUND(COUNT(DISTINCT id) * 100.0 / (SELECT COUNT(*) FROM DBMS_BUGS_TRUE_POSITIVES), 1) FROM BUG_TEST_CASES WHERE STATEMENT LIKE "%FOREIGN KEY%" AND id IN (SELECT id FROM DBMS_BUGS_TRUE_POSITIVES) -- 1.0
```

> Figure 2: The distribution of the SQL statements used in the bug reports to reproduce the bug. 

```sql
SELECT STATEMENT FROM BUG_TEST_CASES_NO_FP;
```


# Reproducing Bugs using SQLancer

The artifact allows both reproducing a specific recent bug, and bugs on older versions.

## Reproducing a Recent Bug

While refactoring the PQS implementation for the OSDI artifact evaluation, we detected a [SQLite regression bug that was incorrectly applied to the IN operator](https://www.sqlite.org/src/tktview?name=f3ff147288). This bug is expected to be quickly and reliably reproducible based on the SQLite JDBC driver that is distributed with the artifact; since SQLite is an embedded DBMS, the JDBC driver that we are using to test it contains also SQLite3 itself.

In order to reproduce the bug, you can remove the `--test-in-operator false` part of the command:

```
java -jar SQLancer-0.0.1-SNAPSHOT.jar --random-string-generation ALPHANUMERIC_SPECIALCHAR --num-threads 1 sqlite3 --oracle pqs --test-fts false --test-rtree false
```

Since SQLancer's test generation approach is random, we cannot give any guarantees on how long SQLancer needs to reproduce the bug. However, we speculate that at least one test case to trigger the bug should be detected within minutes. The statements to reproduce the bug are printed on the command line, and saved as a log file (see # Debugging SQLancer below). After the statements, additional information to debug and validate the bug is printed, such as the values for the pivot row:

```sql
-- we expect the following expression to be contained in the result set: '416549746', x'dc'
SELECT '416549746', x'dc' INTERSECT SELECT * FROM (SELECT t0.c1, x'dc' FROM t0 WHERE ((NOT ((((t0.c1)>(t0.c1)) IN (((0.8823315147298789) IS TRUE)))))) ORDER BY NULL  NULLS LAST);
-- pivot row values:
-- t0
--	t0.c1: TEXT=(TEXT) 416549746
--	t0.c0: INT=(NULL) NULL
--
-- rectified predicates and their expected values:
--	(NOT ((((t0.c1)>(t0.c1)) IN (((0.8823315147298789) IS TRUE))))) -- (INT) 1 explicit collate: null implicit collate: null
-- 		(((t0.c1)>(t0.c1)) IN (((0.8823315147298789) IS TRUE))) -- (INT) 0 explicit collate: null implicit collate: null
-- 			((t0.c1)>(t0.c1)) -- (INT) 0 explicit collate: null implicit collate: null
-- 				t0.c1 -- (TEXT) 416549746 explicit collate: null implicit collate: BINARY
-- 				t0.c1 -- (TEXT) 416549746 explicit collate: null implicit collate: BINARY
-- 			((0.8823315147298789) IS TRUE) -- (INT) 1 explicit collate: null implicit collate: null
-- 				0.8823315147298789 -- (REAL) 0.8823315147298789 explicit collate: null implicit collate: null
-- 
-- pivot row expressions and their expected values:
--	t0.c1 -- (TEXT) 416549746 explicit collate: null implicit collate: BINARY
--
--	x'dc' -- (BINARY) x'dc' explicit collate: null implicit collate: null
--

```



## Testing Old Versions of the DBMS

Our artifact *does not provide a straightforward way* to reproduce *specific* bugs. This would require an infrastructure to check out a specific version of the DBMS and SQLancer, build both components, start and configure the DBMS, and then provide an efficient way to bisect bug-inducing test cases in order to determine which bug was detected. Furthermore, our test case generation is random, meaning that it is not guaranteed how long SQLancer must run to detect a specific bug. However, it is possible to reproduce bugs in principle, by replacing the current JDBC driver with an older version. To achieve this, edit the `pom.xml` file and locate the following entry:

```
    <dependency>
      <groupId>org.xerial</groupId>
      <artifactId>sqlite-jdbc</artifactId>
      <version>3.32.3.2</version>
    </dependency>
```

You can replace the content of the `version` field using an [appropriate version](https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc), for example, by using the 3.28.0 version (see below).

```
    <dependency>
      <groupId>org.xerial</groupId>
      <artifactId>sqlite-jdbc</artifactId>
      <version>3.30.1</version>
    </dependency>
```

Note that versions 3.28.0 and older lack features such as they `NULLS FIRST` and `NULLS LAST` keywords in `ORDER BY` clauses, which are now supported by SQLancer, causing older versions to fail due to syntax errors. To prevent this, after building (i.e., executing `mvn package -DskipTests`), an additional option `--test-nulls-first-last false` needs to be passed to SQLancer:

```
java -jar SQLancer-0.0.1-SNAPSHOT.jar --random-string-generation ALPHANUMERIC_SPECIALCHAR --num-threads 1 sqlite3 --oracle pqs --test-fts false --test-rtree false --test-in-operator false --test-nulls-first-last false
```

When executing on an older version, it is expected that bugs can quickly be found. [Note that the JVM might also crash when triggering memory errors in SQLite itself](https://github.com/sqlancer/sqlancer/issues/1). Finding the root cause for found bugs requires domain-knowledge (i.e., consulting the [SQLite docs](https://sqlite.org/docs.html)).

## Testing other DBMS

We run our regression tests for every Pull Request (PR) using [Travis CI](https://travis-ci.org/). The [sqlancer/.travis.yml](sqlancer/.travis.yml) specifies a reproducible Ubuntu environment to install and set up the DBMS, as well as run the tests. Based on these instructions, also the other DBMS could be evaluated. Note that we also run the PQS implementation of SQLite, MySQL, and Postgres in this environment.

To run the PostgreSQL implementation, you can use the following command:

```
java -jar SQLancer-0.0.1-SNAPSHOT.jar --random-string-generation ALPHANUMERIC_SPECIALCHAR  postgres --oracle pqs
```

To run the MySQL implementation, you can use the following command:

```
java -jar SQLancer-0.0.1-SNAPSHOT.jar --random-string-generation ALPHANUMERIC --num-queries 10000  mysql --oracle PQS
```

Note that the MySQL PQS implementation does not support all special characters, which is why the string-generation strategy is `ALPHANUMERIC` (rather than `ALPHANUMERIC_SPECIALCHAR`).

## Debugging SQLancer

As stated in the [main documentation of SQLancer](sqlancer/README.md), the SQLancer stores logs in the `target/logs` subdirectory. By default, the option `--log-each-select` is enabled, which results in every SQL statement that is sent to the DBMS being logged. The corresponding file names are postfixed with `-cur.log`. In addition, if SQLancer detects a logic bug, it creates a file with the extension `.log`, in which the statements to reproduce the bug are logged. For PQS, additional information is printed, such as the expected values of each (sub-)expression and the pivot row that was used to identify the bug. We believe that this is the most convenient way to observe the behavior of SQLancer.

## Navigating Through the Source Code

The [sqlancer/CONTRIBUTING.md](sqlancer/CONTRIBUTING.md) file includes information on how SQLancer can be imported in an IDEA such as Eclipse, and provides useful information on how to work with the source code of SQLancer. Furthermore, the [video tutorial](tutorial.mp4) demonstrates the code parts relevant for the PQS implementations. The most important classes are the following:

* [PivotedQuerySynthesisBase](sqlancer/src/sqlancer/common/oracle/PivotedQuerySynthesisBase.java) is the base class for all PQS test oracles.
* [SQLite3PivotedQuerySynthesisOracle](sqlancer/src/sqlancer/sqlite3/oracle/SQLite3PivotedQuerySynthesisOracle.java) is the SQLite PQS implementation and implements steps 2-7 of the paper. The SQLite implementation is the most complete implementation. Note that the PostgreSQL and MySQL implementations follow the same folder structure.
* [SQLite3Provider](sqlancer/src/sqlancer/sqlite3/SQLite3Provider.java) is the main entry point to the SQLite testing implementation, and controls the overall testing process. It also creates the database state, corresponding to step 1 of the paper. Note that equivalent classes exist for the PostgreSQL and MySQL implementations.

# Additional Resources

* PingCAP, the company developing TiDB, has adopted PQS to test their DBMS. They have implemented PQS in a tool called [go-sqlancer](https://github.com/chaos-mesh/go-sqlancer), which is written in Go, based only on a preprint of the PQS paper (SQLancer itself was not yet available at this time).
* A PQS talk held for the [Workshop on Dependable and Secure Software Systems 2019](https://www.sri.inf.ethz.ch/workshop2019) is available on [YouTube](https://www.youtube.com/watch?v=yzENTaWe7qg).
* The [website on our bug-finding efforts](http://manuelrigger.at/dbms-bugs) provides additional information on PQS, and the other approaches that we have developed.

# Related Artifacts: NoREC and TLP

For our previous NoREC work, we also released an artifact, which is available at https://doi.org/10.5281/zenodo.3947858. An artifact for TLP is currently under review. Both related artifacts include a(n earlier) version of SQLancer, with the outdated PQS implementation, and we re-used some general descriptions in this artifact.
