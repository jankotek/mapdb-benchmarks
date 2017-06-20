package org.mapdb.benchmark

import org.junit.Test
import org.mapdb.*
import org.mapdb.tree.*
import org.mapdb.store.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap

class MapBenchmark {

    companion object {
        val size = 1e5.toInt()
    }

    @Test fun hashMap() {
        run(ConcurrentHashMap(size), "ConcurrentHashMap", size)
    }

    @Test fun skipListMap() {
        run(ConcurrentSkipListMap(), "ConcurrentSkipListMap", size)
    }


    @Test fun MapDB20_BTreeMap() {
        run(org.mapdb20.DBMaker.memoryDB()
                .transactionDisable()
                .allocateStartSize(1024 * 1024 * 512)
                .make()
                .treeMap("map", org.mapdb20.Serializer.LONG, org.mapdb20.Serializer.UUID),
                "MapDB2_BTreeMap", size)
    }

    @Test fun MapDB20_HTreeMap() {
        run(org.mapdb20.DBMaker.memoryDB()
                .transactionDisable()
                .allocateStartSize(1024 * 1024 * 512)
                .make()
                .hashMap("map", org.mapdb20.Serializer.LONG, org.mapdb20.Serializer.UUID),
                "MapDB2_HTreeMap", size)
    }


    @Test fun btreemap_heap(){
        val map = BTreeMap.make(
                keySerializer = Serializer.LONG,
                valueSerializer = Serializer.UUID,
                store = StoreOnHeap())
        run(map, "BTreeMap_heap", size)
    }


    @Test fun htreemap_heap(){
        var maker =
                DBMaker
                        .heapDB()
                        .make()
                        .hashMap("map")
                        .keySerializer(Serializer.LONG)
                        .valueSerializer(Serializer.UUID)
                        .valueInline()
        //                            .layout(0,6,4)

        val map = maker.create()
        run(map, "HTreeMap_heap", size)
    }

    @Test fun btreemap(){
        val map = BTreeMap.make(
                keySerializer = Serializer.LONG,
                valueSerializer = Serializer.UUID,
                store = StoreDirect.make())
        run(map, "BTreeMap", size)
    }


    @Test fun htreemap(){
        var maker =
                DBMaker
                        .memoryDB()
                        .allocateStartSize(1024*1024*512)
                        .make()
                        .hashMap("map")
                        .keySerializer(Serializer.LONG)
                        .valueSerializer(Serializer.UUID)
                        .valueInline()
        //                            .layout(0,6,4)

        val map = maker.create()
        run(map, "HTreeMap", size)
    }


    fun run(map:MutableMap<Long?, UUID?>, name:String, size:Int){

        Bench.bench(name+"_insert") {
            Bench.stopwatch {
                for (i in 0L until size) {
                    map.put(i, UUID(i,i))
                }
            }
        }

        Bench.bench(name+"_get") {
            Bench.stopwatch {
                for (i in 0L until size) {
                    map[i]
                }
            }
        }

        Bench.bench(name+"_update") {
            Bench.stopwatch {
                for (i in 0L until size) {
                    map.put(i, UUID(i*10,i*10))
                }
            }
        }

    }
}