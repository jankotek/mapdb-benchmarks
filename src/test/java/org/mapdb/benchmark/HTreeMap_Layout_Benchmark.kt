package org.mapdb.benchmark

import org.junit.Test
import org.mapdb.*

/**
 * Effect of Index Tree List Layout on performance
 */
class HTreeMap_Layout_Benchmark {

    @Test fun run() {
        for(concShift in 0 .. 5) {
            for (dirShift in 1..8) {
                for (levels in 2..8) {
                    try {
                        val map = DBMaker.memoryDB().make()
                                .hashMap("list", Serializer.INTEGER, Serializer.INTEGER)
                                .layout(1.shl(concShift), 1.shl(dirShift), levels)
                                .create()

                        val name = HTreeMap_Layout_Benchmark::class.java.simpleName +
                                "_${concShift}_${dirShift}_${levels}_"

                        val max = 1e4.toInt()

                        Bench.bench(name + "ADD") {
                            Bench.stopwatch {
                                for (i in 0 until max) {
                                    map.put(i, i)
                                }
                            }
                        }

                        Bench.bench(name + "GET") {
                            Bench.stopwatch {
                                for (i in 0 until max) {
                                    map[i]
                                }
                            }
                        }

                        Bench.bench(name + "UPDATE") {
                            Bench.stopwatch {
                                for (i in 0 until max) {
                                    map[i] = i + 1
                                }
                            }
                        }


                    } catch(e: Throwable) {
                        //e.printStackTrace()
                    }
                }
            }
        }
    }


}
