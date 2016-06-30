package org.mapdb.benchmark

import org.junit.Test
import org.mapdb.*

/**
 * Effect of Index Tree List Layout on performance
 */
class IndexTreeList_Layout_Benchmark {

    @Test fun run() {
        for(dirShift in 1 .. 8){
            for(levels in 2..8){
                try{
                    val list = DBMaker.memoryDB().make()
                            .indexTreeList("list", Serializer.INTEGER)
                            .layout(1.shl(dirShift), levels)
                            .create()

                    val name = IndexTreeList_Layout_Benchmark::class.java.simpleName +
                            "_${dirShift}_${levels}_"

                    val max = 1e6.toInt()

                    Bench.bench(name+"ADD") {
                        Bench.stopwatch {
                            for (i in 0 until max) {
                                list+=i
                            }
                        }
                    }

                    Bench.bench(name+"GET") {
                        Bench.stopwatch {
                            for (i in 0 until max) {
                                list[i]
                            }
                        }
                    }

                    Bench.bench(name+"UPDATE") {
                        Bench.stopwatch {
                            for (i in 0 until max) {
                                list[i] = i+1
                            }
                        }
                    }


                }catch(e:Throwable){
                    //e.printStackTrace()
                }
            }
        }
    }


}
