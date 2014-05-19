This library offers [Unsafe](http://www.docjar.com/docs/api/sun/misc/Unsafe.html) off-heap storage for
MapDB. It brings about 10% over default off-heap storage based on DirectByteBuffers. 

Unsafe does not check range boundaries, so it may crash your JVM. It also does not work on some JVMs such as Dalvik.
To use add this maven dependency:

```xml
    <dependencies>
        <dependency>
            <groupId>org.mapdb</groupId>
            <artifactId>mapdb-snappy</artifactId>
            <version>0.9</version>
        </dependency>
    </dependencies>
```

And than use `DBMakerUnsafe.newMemoryDirectDB()` to create DB:

```java

    import org.mapdb.unsafe.*;

    DB db = DBMakerUnsafe
        .newMemoryDirectDB()
        .make();

    //do not forget to close db to release memory
    db.close()

```


0.9 is prototype release to prove feasibility. It requires some changes in MapDB to improve its performance. 