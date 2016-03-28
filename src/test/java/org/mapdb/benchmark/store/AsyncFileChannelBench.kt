package org.mapdb.benchmark.store

import org.mapdb.benchmark.Bench
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.StandardOpenOption

/**
 * Tests AsynchronousFileChannel performance
 */

fun main(args: Array<String>) {
    val f = Bench.tempFile()
    val c = AsynchronousFileChannel.open(f.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE)
    val b = ByteBuffer.allocate(100)
    while(true){
        b.rewind()
        val fut = c.write(b,0)
        while(fut.isDone.not()) {
        }
    }

    f.delete()
}
