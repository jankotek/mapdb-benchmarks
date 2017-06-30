Benchmarks for MapDB database engine
----------------------------------------

Source code for benchmarks at :
[http://www.mapdb.org/benchmarks.html](http://www.mapdb.org/benchmarks.html)

Benchmarks are executed as  part of unit tests:

```
mvn clean test
```

If it uses development version of MapDB, it may fail with `4.0.0-SNAPSHOT not found`. In that case checkout MapDB from git and install it into your local maven repository:

```
cd mapdb
mvn install
```

You should have at least 32GB RAM,  4 CPU cores and 100GB free in temp directory.
Runtime is a few hours. Results are generated in `res` subfolder.
Benchmarks are executed in forked JVMs with various memory settings.

By default every benchmark is ran only once, and that  may produce incorrect results. This will run every bechmark 100 times and calculate average value:


```
mvn clean test -DtestLong=1
```

Benchmark puts its files into temporary folder (`/tmp`). You can override it with property:

```
mvn clean test -Djava.io.tmpdir=/other/tmp/
```
