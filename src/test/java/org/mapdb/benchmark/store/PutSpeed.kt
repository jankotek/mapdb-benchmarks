package org.mapdb.benchmark.store

import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList
import org.junit.Test
import org.mapdb.*
import org.mapdb.benchmark.Bench
import org.mapdb.benchmark.MapBenchmark

/**
 * Tests insertion speed
 */
class PutSpeed{

    val max = MapBenchmark.size
    fun b() = ByteArray(10)
    fun b2() = ByteArray(11)

    @Test fun storeOnHeap(){
        run("PutSpeed-StoreOnHeap", StoreOnHeap())
    }

    @Test fun storeDirect(){
        val store = StoreDirect.make()
        val name = "PutSpeed-StoreDirect";
        run(name, store)
        println(name+" size after finished: "+store.getTotalSize())
    }

    @Test fun storeTrivial(){
        run("PutSpeed-StoreTrivial", StoreTrivial())
    }


    @Test fun store20(){
        val store = org.mapdb20.DBMaker.memoryDB().transactionDisable().makeEngine() as org.mapdb20.StoreDirect

        val name = "PutSpeed-MapDB2"

        val recids = LongArrayList();
        Bench.bench(name+"-put"){

            Bench.stopwatch {
                for(i in 0 .. max){
                    val recid = store.put(b(), org.mapdb20.Serializer.BYTE_ARRAY_NOSIZE)
                    recids.add(recid)
                }
            }
        }

        Bench.bench(name+"-get"){
            Bench.stopwatch {
                recids.forEach {
                    store.get(it, org.mapdb20.Serializer.BYTE_ARRAY_NOSIZE)
                }
            }
        }

        Bench.bench(name+"-update"){
            Bench.stopwatch {
                recids.forEach {
                    store.update(it, b2(), org.mapdb20.Serializer.BYTE_ARRAY_NOSIZE)
                }
            }
        }

        Bench.bench(name+"-cas"){
            Bench.stopwatch {
                recids.forEach {
                    store.compareAndSwap(it, b2(), b(), org.mapdb20.Serializer.BYTE_ARRAY_NOSIZE)
                }
            }
        }
        println(name+" size after finished: "+store.currSize)
    }


    fun run(name:String,  store: Store){
        val recids = LongArrayList();
        Bench.bench(name+"-put"){

            Bench.stopwatch {
                for(i in 0 .. max){
                    val recid = store.put(b(), Serializer.BYTE_ARRAY_NOSIZE)
                    recids.add(recid)
                }
            }
        }

        Bench.bench(name+"-get"){
            Bench.stopwatch {
                recids.forEach {
                    store.get(it, Serializer.BYTE_ARRAY_NOSIZE)
                }
            }
        }

        Bench.bench(name+"-update"){
            Bench.stopwatch {
                recids.forEach {
                    store.update(it, b2(), Serializer.BYTE_ARRAY_NOSIZE)
                }
            }
        }

        Bench.bench(name+"-cas"){
            Bench.stopwatch {
                recids.forEach {
                    store.compareAndSwap(it, b2(), b(), Serializer.BYTE_ARRAY_NOSIZE)
                }
            }
        }
    }
}