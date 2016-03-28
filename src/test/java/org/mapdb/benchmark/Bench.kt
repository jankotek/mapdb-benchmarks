package org.mapdb.benchmark

import java.io.File
import java.io.IOError
import java.io.IOException
import java.util.*

/**
 * Benchmark utilities
 */
object Bench{

    val propFile = File("target/benchmark.properties")

    fun bench(benchName:String=callerName(), body:()->Long){
        val many = testScale()>0;
        val metric =
                if(!many) {
                    body()
                }else{
                    //run many times and do average
                    var t=0L;
                    for(i in 0 until 100)
                        t += body()
                    t/100
                }

        var formatedMetrics = String.format("%12s", String.format("%,d", metric))
        println("BENCH: $formatedMetrics - $benchName  ")

        //load, update and save property file with results
        val props = object : Properties(){
            //save keys in sorted order
            override fun keys(): Enumeration<Any>? {
                 return Collections.enumeration(TreeSet(keys));
            }
        }
        if(propFile.exists())
            props.load(propFile.inputStream().buffered())
        props.put(benchName, metric.toString())
        val out = propFile.outputStream()
        val out2 = out.buffered()
        props.store(out2,"mapdb benchmark")
        out2.flush()
        out.close()
    }

    fun stopwatch(body:()->Unit):Long{
        val start = System.currentTimeMillis()
        body()
        return System.currentTimeMillis() - start
    }

    /** returns class name and method name of caller from previous stack trace frame */
    inline fun callerName():String{
        val t = Thread.currentThread().stackTrace
        val t0 = t[2]
        return t0.className+"."+t0.methodName
    }

    /** how many hours should unit tests run? Controlled by:
     * `mvn test -Dmdbtest=2`
     * @return test scale
     */
    @JvmStatic fun testScale(): Int {
        val prop = System.getProperty("mdbtest")?:"0";
        try {
            return Integer.valueOf(prop);
        } catch(e:NumberFormatException) {
            return 0;
        }
    }

    private val tempDir = System.getProperty("java.io.tmpdir");

    /*
     * Create temporary file in temp folder. All associated db files will be deleted on JVM exit.
     */
    @JvmStatic fun tempFile(): File {
        try {
            val stackTrace = Thread.currentThread().stackTrace;
            val elem = stackTrace[2];
            val prefix = "mapdbTest_"+elem.className+"#"+elem.methodName+":"+elem.lineNumber+"_"
            while(true){
                val file = File(tempDir+"/"+prefix+System.currentTimeMillis()+"_"+Math.random());
                if(file.exists().not()) {
                    file.deleteOnExit()
                    return file
                }
            }
        } catch (e: IOException) {
            throw IOError(e)
        }

    }

    @JvmStatic fun tempDir(): File {
        val ret = tempFile()
        ret.mkdir()
        return ret
    }

    @JvmStatic fun tempDelete(file: File){
        val name = file.getName()
        for (f2 in file.getParentFile().listFiles()!!) {
            if (f2.name.startsWith(name))
                tempDeleteRecur(f2)
        }
        tempDeleteRecur(file)
    }

    @JvmStatic fun tempDeleteRecur(file: File) {
        if(file.isDirectory){
            for(child in file.listFiles())
                tempDeleteRecur(child)
        }
        file.delete()
    }

}
