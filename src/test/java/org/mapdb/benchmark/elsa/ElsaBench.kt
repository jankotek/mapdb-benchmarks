package org.mapdb.benchmark.elsa

import org.junit.Test
import org.mapdb.benchmark.Bench
import org.mapdb.benchmark.MapBenchmark
import org.mapdb.elsa.ElsaMaker
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*

class ElsaBench{

    val size = MapBenchmark.size/1000


    fun ser(name:String, e:Any?){
        val elsa = ElsaMaker().make()
        val out1 = ByteArrayOutputStream()
        val out2 = DataOutputStream(out1)

        Bench.bench("elsa-$name") {
            Bench.stopwatch {
                for (i in 0 until size) {
                    elsa.serialize(out2, e)
                    out1.reset()
                }
            }
        }

        val out3 = ObjectOutputStream(out1)

        Bench.bench("obj-$name") {
            Bench.stopwatch {
                for (i in 0 until size) {
                    out3.writeObject(e)
                    out3.reset()
                    out1.reset()
                }
            }
        }
    }

    @Test fun sernull() = ser("null", null)


    @Test fun long() = ser("long", 1L)

    @Test fun string_empty() = ser("string_empty", "")

    @Test fun string() = ser("string", "3d29d023009329d23--032d23d3meoie")

    @Test fun pojo() = ser("pojo", ElsaBenchPojo(1,2))

    @Test fun map() {
        val m = HashMap<Int,Int>()
        for(i in 0 until 10000)
            m[i] = i
        ser("map", m)
    }


}

data class ElsaBenchPojo(val a:Long, val b:Int): Serializable {

}