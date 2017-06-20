package org.mapdb.benchmark

import org.junit.Test
import org.mapdb.tree.*
import org.mapdb.*
import org.mapdb.store.*

/**
 * Benchmarks HTreeMap expiration
 */
class ExpiryBench{

    @Test fun createTTL(){
        Bench.bench {
            //fill data
            val map = HTreeMap.make<Int,Int>(
                    keySerializer = Serializer.INTEGER,
                    valueSerializer = Serializer.INTEGER,
                    expireCreateTTL = 1,
                    concShift = 0,
                    stores = arrayOf(StoreDirect.make()),
                    hashSeed = 1
            )
            val size = MapBenchmark.size
            Bench.stopwatch {
                for(i in 0 until size) {
                    map.put(i, i)
                }
                while(!map.isEmpty()){
                    map.expireEvict()
                }
            }
        }
    }
}
