package org.mapdb.benchmark

import org.junit.Test
import org.mapdb.*
import org.mapdb.volume.ByteArrayVol
import org.mapdb.volume.Volume

class SortedTableMapBenchmark{

    val size = MapBenchmark.size

    @Test fun get(){
        val consumer = SortedTableMap.create(
                keySerializer = Serializer.INTEGER,
                valueSerializer = Serializer.INTEGER,
                volume = ByteArrayVol.FACTORY.makeVolume(null, false)
            ).consumer()
        for(i in 0 until size ){
            consumer.take(Pair(i,i))
        }
        val map = consumer.finish()

        Bench.bench("SortedTableMapBenchmark_get") {
            Bench.stopwatch {
                for (i in 0 until size) {
                    map[i]
                }
            }
        }


    }
}
