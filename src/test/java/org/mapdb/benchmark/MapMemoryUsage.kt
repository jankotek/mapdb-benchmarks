package org.mapdb.benchmark

import org.junit.Test
import org.mapdb.DBMaker
import org.mapdb.Serializer
import org.mapdb.SortedTableMap
import org.mapdb.volume.ByteArrayVol
import java.io.File
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap

/**
 * Fills map until it runs out of memory
 */
class MapMemoryUsage{

    val jvmLimit =  "-Xmx5G"

    @Test fun htreeMap(){
        run("HTreeMap")
    }

    @Test fun btreeMap(){
        run("BTreeMap")
    }

    @Test fun htreeMap_heap(){
        run("HTreeMap_heap")
    }

    @Test fun btreeMap_heap(){
        run("BTreeMap_heap")
    }

    @Test fun btreeMap_pump(){
        run("BTreeMap_pump")
    }

    @Test fun sortedTableMapx(){
        run("SortedTableMap")
    }


    @Test fun htreeMap2(){
        run("MapDB2_HTreeMap")
    }

    @Test fun btreeMap2(){
        run("MapDB2_BTreeMap")
    }

    @Test fun concurrentSkipListMap(){
        run("ConcurrentSkipListMap")
    }

    @Test fun concurrentHashMap(){
        run("ConcurrentHashMap")
    }


    companion object{
        @JvmStatic fun main(args : Array<String>){
            val map = when(args[0]) {
                "HTreeMap" -> DBMaker.memoryDB().make().hashMap("map", Serializer.LONG, Serializer.UUID).valueInline().createOrOpen()
                "BTreeMap" -> DBMaker.memoryDB().make().treeMap("map", Serializer.LONG_DELTA, Serializer.UUID).createOrOpen()
                "HTreeMap_heap" -> DBMaker.heapDB().make().hashMap("map", Serializer.LONG, Serializer.UUID).valueInline().createOrOpen()
                "BTreeMap_heap" -> DBMaker.heapDB().make().treeMap("map", Serializer.LONG_DELTA, Serializer.UUID).createOrOpen()
                "MapDB2_BTreeMap" -> org.mapdb20.DBMaker.memoryDB().make().treeMap("map", org.mapdb20.Serializer.LONG, org.mapdb20.Serializer.UUID)
                "MapDB2_HTreeMap" -> org.mapdb20.DBMaker.memoryDB().make().hashMap("map", org.mapdb20.Serializer.LONG, org.mapdb20.Serializer.UUID)
                "ConcurrentSkipListMap" -> ConcurrentSkipListMap<Long,UUID>()
                "ConcurrentHashMap" -> ConcurrentHashMap<Long, UUID>()

                "BTreeMap_pump" ->{
                    pump()
                    throw AssertionError()
                }
                "SortedTableMap" ->{
                    sortedTableMap()
                    throw AssertionError()
                }


                else -> throw AssertionError("Wrong map: "+args[0])
            }

            var c = 0L
            while(true){
                map[c++] = UUID(c,c);
                if(c%100000L==0L) {
                    println(c);
                    System.out.flush()
                }
            }
        }

        fun pump(){
            val consumer = DBMaker.memoryDB().make()
                    .treeMap("map", Serializer.LONG_DELTA, Serializer.UUID)
                    .createFromSink()
            var c = 0L
            while(true){
                consumer.put(c++,UUID(c,c))
                if(c%100000L==0L) {
                    println(c);
                    System.out.flush()
                }
            }
        }

        fun sortedTableMap(){
            val consumer = SortedTableMap
                    .create(ByteArrayVol.FACTORY.makeVolume(null, false),
                            Serializer.LONG_DELTA, Serializer.UUID)
                    .createFromSink()
            var c = 0L
            while(true){
                consumer.put(c++,UUID(c,c))
                if(c%100000L==0L) {
                    println(c);
                    System.out.flush()
                }
            }
        }

    }

    internal fun jvmExecutable(): String {
        val exec = if (System.getProperty("os.name").startsWith("Win"))
            "java.exe"
        else
            "java"
        val javaHome = System.getProperty("java.home")
        if (javaHome == null || "" == javaHome)
            return exec
        return javaHome + File.separator + "bin" + File.separator + exec
    }

    fun run(mapName:String){
        Bench.bench(MapMemoryUsage::class.java.simpleName+"#$mapName") {
            val b = ProcessBuilder(
                    jvmExecutable(),
                    jvmLimit,
                    "-classpath",
                    System.getProperty("java.class.path"),
                    MapMemoryUsage::class.java.name,
                    mapName)
            val pr = b.start()
//            while(true) {
//                Thread.sleep(1)
//                pr.inputStream.copyTo(System.out)
//                pr.errorStream.copyTo(System.err)
//            }

            pr.waitFor() //it should kill itself after some time

            Thread.sleep(100)// just in case
            val out = InputStreamReader(pr.inputStream).readLines()
            if(out.size==0){
                pr.errorStream.copyTo(System.err)
                throw AssertionError()
            }
            val last = out.last()
            last.toLong()
        }
    }
}
