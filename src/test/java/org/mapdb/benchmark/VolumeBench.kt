//package org.mapdb.benchmark
//
//import org.junit.Ignore
//import org.junit.Test
//import org.mapdb.volume.FileChannelVol
//import org.mapdb.volume.MappedFileVol
//import org.mapdb.volume.RandomAccessFileVol
//import org.mapdb.volume.Volume
//import java.io.File
//import java.io.FileInputStream
//import java.io.FileOutputStream
//import java.util.*
//
//class VolumeBench{
//    val file = File("/media/jan/fc160542-579e-4d88-99f4-3f8ef8c08a43//file")
//    val buf = ByteArray(1024 * 1024)
//    @Test
//    @Ignore
//    fun aa() {
//        Random().nextBytes(buf)
//
//        val out = FileOutputStream(file);
//
//        while(file.freeSpace>10* 1e9){
//            out.write(buf)
//        }
//        out.flush()
//        out.close()
//        System.out.println("Wrote ${file.length()/1e9}")
//    }
//
//    fun readRandom(vol:Volume){
//        val len = vol.length()/buf.size
//        val r = Random();
//        for(i in 1..1e5.toInt()){
//            val pos = r.nextInt(len.toInt()).toLong()*buf.size
//            vol.getData(pos, buf, 0, buf.size)
//        }
//    }
//
//
//    @Test fun readSeqInputStream(){
//        val input = FileInputStream(file)
//        var size = file.length()
//        while(size>0){
//            size-=buf.size
//            input.read(buf)
//        }
//    }
//
//    @Test fun readSeqRAF(){
//        val input = RandomAccessFileVol.FACTORY.makeVolume(file.toString(),true);
//        val size = file.length()
//        for(pos in 0 until size step buf.size.toLong()){
//            input.getData(pos, buf, 0, buf.size)
//        }
//        input.close()
//    }
//
//    @Test fun readSeqFileChannel(){
//        val input = FileChannelVol.FACTORY.makeVolume(file.toString(),true);
//        val size = file.length()
//        for(pos in 0 until size step buf.size.toLong()){
//            input.getData(pos, buf, 0, buf.size)
//        }
//        input.close()
//    }
//
//
//    @Test fun readSeqMmapFile(){
//        val input = MappedFileVol.FACTORY.makeVolume(file.toString(),true);
//        val size = file.length()
//        for(pos in 0 until size step buf.size.toLong()){
//            input.getData(pos, buf, 0, buf.size)
//        }
//        input.close()
//    }
//
//
//    @Test fun readRAF(){
//        val input = RandomAccessFileVol.FACTORY.makeVolume(file.toString(),true);
//        readRandom(input)
//        input.close()
//    }
//
//    @Test fun readFileChannel(){
//        val input = FileChannelVol.FACTORY.makeVolume(file.toString(),true);
//        readRandom(input)
//        input.close()
//    }
//
//
//    @Test fun readMmapFile(){
//        val input = MappedFileVol.FACTORY.makeVolume(file.toString(),true);
//        readRandom(input)
//        input.close()
//    }
//}