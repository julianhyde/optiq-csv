[![Build Status](https://travis-ci.org/julianhyde/optiq-csv.png)](https://travis-ci.org/julianhyde/optiq-csv)

optiq-csv
============

Calcite adapter that reads
<a href="http://en.wikipedia.org/wiki/Comma-separated_values">CSV</a> files.

Optiq-csv is a nice simple example of how to connect
<a href="https://github.com/apache/incubator-calcite">Apache Calcite</a>
to your own data source and quickly get a full SQL/JDBC interface.

Download and build
==================

You need Java (1.6 or higher; 1.7 preferred), git and maven (3.0.4 or later).

```bash
$ git clone git://github.com/julianhyde/optiq-csv.git
$ cd optiq-csv
$ mvn clean install
```

Kick the tires
==============

Let's take a quick look at optiq-csv's (and Calcite's) features.
We'll use <code>sqlline</code>, a SQL shell that connects to
any JDBC data source and is included with optiq-csv.

Connect to Calcite and try out some queries:

```SQL
$ ./sqlline
sqlline> !connect jdbc:calcite:model=target/test-classes/model.json admin admin
sqlline> !tables
sqlline> !describe emps
sqlline> SELECT * FROM emps;
sqlline> EXPLAIN PLAN FOR SELECT * FROM emps;
sqlline> !connect jdbc:calcite:model=target/test-classes/smart.json admin admin
sqlline> EXPLAIN PLAN FOR SELECT * FROM emps;
sqlline> SELECT depts.name, count(*)
. . . .> FROM emps JOIN depts USING (deptno)
. . . .> GROUP BY depts.name;
sqlline> VALUES char_length('hello, ' || 'world!');
sqlline> !quit
```

(On Windows, the command is `sqlline.bat`.)

As you can see, Calcite has a full SQL implementation that can efficiently
query any data source.

For a more leisurely walk through what Calcite can do and how it does it,
try <a href="TUTORIAL.md">the Tutorial</a>.

More information
================

* License: Apache License, Version 2.0.
* Author: Julian Hyde
* Blog: http://julianhyde.blogspot.com
* Project page: http://www.hydromatic.net/optiq-csv
* Source code: http://github.com/julianhyde/optiq-csv
* Developers list: http://mail-archives.apache.org/mod_mbox/incubator-calcite-dev/
* <a href="TUTORIAL.md">Tutorial</a>
* <a href="HISTORY.md">Release notes and history</a>
