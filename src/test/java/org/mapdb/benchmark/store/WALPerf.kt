package org.mapdb.benchmark.store

import org.junit.Test
import org.mapdb.DataIO
import org.mapdb.WriteAheadLog
import org.mapdb.benchmark.Bench
import org.mapdb.volume.FileChannelVol
import org.mapdb.volume.MappedFileVol
import org.mapdb.volume.RandomAccessFileVol
import org.mapdb.volume.VolumeFactory
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

/**
 * Tests performance of sync
 */

class WALPerf{

    val max = 1000000;

    fun wal(name:String, volFab: VolumeFactory){
        Bench.bench("WALPerf-$name-preal") {
            val f = Bench.tempFile()
            val wal = WriteAheadLog(f.path, volFab, 0, false)
            val ret = Bench.stopwatch {
                for(i in 0 until max){
                    wal.walPutPreallocate(111L)
                    wal.sync()
                }
            }
            wal.close()
            f.delete()
            ret
        }

        Bench.bench("WALPerf-$name-multi") {
            val f = Bench.tempDir()

            val ret = Bench.stopwatch {
                for(i in 0 until max){
                    val vol = volFab.makeVolume(f.path+"/"+i, false)
                    val max = 8L*4;
                    vol.ensureAvailable(max)
                    for(j in 0L until max step 8)
                        vol.putLong(j, 11L)
                    vol.sync()
                    vol.close()
                }
            }
            Bench.tempDelete(f)
            ret
        }
    }

    @Test fun mmap(){
        wal("mmapVol",MappedFileVol.FACTORY)
    }

    @Test fun raf(){
        wal("rafVol", RandomAccessFileVol.FACTORY)
    }

    @Test fun fileChannelVol(){
        wal("fileChannelVol", FileChannelVol.FACTORY)
    }

    @Test fun outputStreams(){
        Bench.bench("WALPerf-outputStream-multi") {
            val f = Bench.tempDir()
            val b = ByteArray(32)

            val ret = Bench.stopwatch {
                for(i in 0 until max){
                    val out = FileOutputStream(f.path+"/"+i)
                    out.write(b)
                    out.flush()
                    out.close()
                }
            }
            Bench.tempDelete(f)
            ret
        }
    }

    @Test fun outputStreams10(){
        Bench.bench("WALPerf-outputStream-multi10") {
            val f = Bench.tempDir()
            val b = ByteArray(32)

            val ret = Bench.stopwatch {
                for(i in 0 until max/10){
                    val out = FileOutputStream(f.path+"/"+i)
                    for(j in 0 until 10) {
                        out.write(b)
                        //out.flush()
                        out.fd.sync()

                    }
                    out.close()
                }
            }
            Bench.tempDelete(f)
            ret
        }
    }

    @Test fun outputStreamsSingle(){
        Bench.bench("WALPerf-outputStream-preal") {
            val f = Bench.tempDir()
            val b = ByteArray(32)

            val ret = Bench.stopwatch {
                val out = FileOutputStream(f.path+"/qw")
                for(i in 0 until max){
                    out.write(b)
                    out.flush()
                    out.fd.sync()
                }
                out.close()
            }
            Bench.tempDelete(f)
            ret
        }
    }



    @Test fun fileChannel(){
        Bench.bench("WALPerf-fileChannel-multi") {
            val f = Bench.tempDir()
            val b = ByteBuffer.allocate(32)

            val ret = Bench.stopwatch {
                for(i in 0 until max){
                    val out = FileChannel.open(File(f.path+"/"+i).toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
                    b.rewind();
                    DataIO.writeFully(out, b)
                    out.force(false)
                    out.close()
                }
            }
            Bench.tempDelete(f)
            ret
        }
    }

    @Test fun fileChannel10(){
        Bench.bench("WALPerf-fileChannel-multi10") {
            val f = Bench.tempDir()
            val b = ByteBuffer.allocate(32)

            val ret = Bench.stopwatch {
                for(i in 0 until max/10){
                    val out = FileChannel.open(File(f.path+"/"+i).toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
                    for(j in 0 until 10) {
                        b.rewind();
                        DataIO.writeFully(out, b)
                        out.force(false)
                    }
                    out.close()
                }
            }
            Bench.tempDelete(f)
            ret
        }
    }

    @Test fun fileChannelSingle(){
        Bench.bench("WALPerf-fileChannel-preal") {
            val f = Bench.tempDir()
            val b = ByteBuffer.allocate(32)

            val ret = Bench.stopwatch {
                val out = FileChannel.open(File(f.path+"/aa").toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
                for(i in 0 until max){
                    b.rewind();
                    DataIO.writeFully(out, b)
                    out.force(false)
                }
                out.close()
            }
            Bench.tempDelete(f)
            ret
        }
    }


}
