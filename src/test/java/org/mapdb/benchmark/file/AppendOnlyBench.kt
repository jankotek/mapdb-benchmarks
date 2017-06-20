package org.mapdb.benchmark.file

import org.junit.Test
import org.mapdb.util.DataIO
import org.mapdb.benchmark.Bench
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

/**
 * Benchmarks various options to populate append-only file
 */
class AppendOnlyBench{

    val count = 1e6.toInt()

    val data1 = ByteArray(32)
    val data2 = ByteArray(8)

    @Test fun RAF(){
        Bench.bench{
            val f =Bench.tempFile()
            val raf = RandomAccessFile(f, "rw")
            Bench.stopwatch {
                for(i in 0..count){
                    raf.write(data1)
                    raf.writeLong(0)
                }
                raf.fd.sync()
                raf.close()
            }
        }
    }


    @Test fun fileChannel(){
        Bench.bench{
            val f =Bench.tempFile()
            val c = FileChannel.open(f.toPath(),StandardOpenOption.APPEND, StandardOpenOption.CREATE_NEW)
            val b1 = ByteBuffer.wrap(data1)
            val b2 = ByteBuffer.wrap(data2)
            Bench.stopwatch {
                for(i in 0..count){
                    b1.clear()
                    DataIO.writeFully(c, b1)
                    b2.clear()
                    DataIO.writeFully(c, b2)
                }
                c.force(true)
                c.close()
            }
        }
    }


    fun stream(stream: OutputStream) {
        for (i in 0..count) {
            stream.write(data1)
            stream.write(data2)
        }
    }


    @Test fun stream(){
        Bench.bench{
            val f =Bench.tempFile()
            val fs = FileOutputStream(f)
            Bench.stopwatch {
                stream(fs)
                fs.flush()
                fs.fd.sync()
                fs.close()
            }
        }
    }


    @Test fun bufferedStream(){
        Bench.bench{
            val f =Bench.tempFile()
            val fs = FileOutputStream(f)
            val bufs = BufferedOutputStream(fs)
            Bench.stopwatch {
                stream(bufs)
                bufs.flush()
                fs.fd.sync()
                bufs.close()
                fs.close()
            }
        }
    }

}