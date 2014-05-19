package org.mapdb.unsafe;

import org.mapdb.CC;
import org.mapdb.DataInput2;
import org.mapdb.Volume;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

public class UnsafeVolume extends Volume {

    protected static final Unsafe unsafe = getUnsafe();

    @SuppressWarnings("restriction")
    private static Unsafe getUnsafe() {
        try {

            Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
            singleoneInstanceField.setAccessible(true);
            return (Unsafe) singleoneInstanceField.get(null);

        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate Unsafe", e);
        }
    }


    // Cached array base offset
    private static final long arrayBaseOffset = (long)unsafe.arrayBaseOffset(byte[].class);


    // This number limits the number of bytes to copy per call to Unsafe's
    // copyMemory method. A limit is imposed to allow for safepoint polling
    // during a large copy
    static final long UNSAFE_COPY_THRESHOLD = 1024L * 1024L;


    /**
     * Copy from given source array to destination address.
     *
     * @param   src
     *          source array
     * @param   srcBaseOffset
     *          offset of first element of storage in source array
     * @param   srcPos
     *          offset within source array of the first element to read
     * @param   dstAddr
     *          destination address
     * @param   length
     *          number of bytes to copy
     */
    static void copyFromArray(Object src, long srcBaseOffset, long srcPos,
                              long dstAddr, long length)
    {
        //*LOG*/ System.err.printf("copyFromArray srcBaseOffset:%d, srcPos:%d, srcPos:%d, dstAddr:%d, length:%d\n",srcBaseOffset, srcBaseOffset, srcPos, dstAddr, length);
        //*LOG*/ System.err.flush();
        long offset = srcBaseOffset + srcPos;
        while (length > 0) {
            long size = (length > UNSAFE_COPY_THRESHOLD) ? UNSAFE_COPY_THRESHOLD : length;
            unsafe.copyMemory(src, offset, null, dstAddr, size);
            length -= size;
            offset += size;
            dstAddr += size;
        }
    }


    /**
     * Copy from source address into given destination array.
     *
     * @param   srcAddr
     *          source address
     * @param   dst
     *          destination array
     * @param   dstBaseOffset
     *          offset of first element of storage in destination array
     * @param   dstPos
     *          offset within destination array of the first element to write
     * @param   length
     *          number of bytes to copy
     */
    static void copyToArray(long srcAddr, Object dst, long dstBaseOffset, long dstPos,
                            long length)
    {

        //*LOG*/ System.err.printf("copyToArray srcAddr:%d, dstBaseOffset:%d, dstPos:%d, lenght:%d\n",srcAddr, dstBaseOffset, dstPos, length);
        //*LOG*/ System.err.flush();
        long offset = dstBaseOffset + dstPos;
        while (length > 0) {
            long size = (length > UNSAFE_COPY_THRESHOLD) ? UNSAFE_COPY_THRESHOLD : length;
            unsafe.copyMemory(null, srcAddr, dst, offset, size);
            length -= size;
            srcAddr += size;
            offset += size;
        }
    }



    protected volatile long[] addresses= new long[0];

    protected final long sizeLimit;
    protected final boolean hasLimit;
    protected final int chunkShift;
    protected final int chunkSizeModMask;
    protected final int chunkSize;

    protected final ReentrantLock growLock = new ReentrantLock(CC.FAIR_LOCKS);


    public UnsafeVolume() {
        this(0, CC.VOLUME_CHUNK_SHIFT);
    }

    public UnsafeVolume(long sizeLimit, int chunkShift) {
        this.sizeLimit = (long) 1e9;///sizeLimit;
        this.hasLimit = sizeLimit>0;
        this.chunkShift = chunkShift;
        this.chunkSize = 1<< chunkShift;
        this.chunkSizeModMask = chunkSize -1;

    }


    @Override
    public boolean tryAvailable(long offset) {
        //*LOG*/ System.err.printf("tryAvailabl: offset:%d\n",offset);
        //*LOG*/ System.err.flush();
        if(hasLimit && offset>sizeLimit)
            return false;

        int chunkPos = (int) (offset >>> chunkShift);

        //check for most common case, this is already mapped
        if (chunkPos < addresses.length){
            return true;
        }

        growLock.lock();
        try{
            //check second time
            if(chunkPos< addresses.length)
                return true;

            int oldSize = addresses.length;
            long[] addresses2 = addresses;

            addresses2 = Arrays.copyOf(addresses2, Math.max(chunkPos + 1, addresses2.length * 2));

            for(int pos=oldSize;pos<addresses2.length;pos++) {
                long address = unsafe.allocateMemory(chunkSize);
                //TODO is this necessary?
                //TODO speedup  by copying an array
                for(int i=0;i<chunkSize;i+=8) {
                    unsafe.putLong(address + i, 0L);
                }

                addresses2[pos]=address;
            }

            addresses = addresses2;
        }finally{
            growLock.unlock();
        }
        return true;
    }

    @Override
    public void truncate(long size) {

    }

    @Override
    public void putLong(long offset, long value) {
        //*LOG*/ System.err.printf("putLong: offset:%d, value:%d\n",offset,value);
        //*LOG*/ System.err.flush();
        value = Long.reverseBytes(value);
        final long address = addresses[((int) (offset >>> chunkShift))];
        offset = offset & chunkSizeModMask;;
        unsafe.putLong(address +offset,value);
    }

    @Override
    public void putInt(long offset, int value) {
        //*LOG*/ System.err.printf("putInt: offset:%d, value:%d\n",offset,value);
        //*LOG*/ System.err.flush();
        value = Integer.reverseBytes(value);
        final long address = addresses[((int) (offset >>> chunkShift))];
        offset = offset & chunkSizeModMask;;
        unsafe.putInt(address +offset,value);
    }

    @Override
    public void putByte(long offset, byte value) {
        //*LOG*/ System.err.printf("putByte: offset:%d, value:%d\n",offset,value);
        //*LOG*/ System.err.flush();
        final long address = addresses[((int) (offset >>> chunkShift))];
        offset = offset & chunkSizeModMask;;
        unsafe.putByte(address +offset,value);
    }

    @Override
    public void putData(long offset, byte[] src, int srcPos, int srcSize) {
//        for(int pos=srcPos;pos<srcPos+srcSize;pos++){
//            unsafe.putByte(address+offset+pos,src[pos]);
//        }
        //*LOG*/ System.err.printf("putData: offset:%d, srcLen:%d, srcPos:%d, srcSize:%d\n",offset, src.length, srcPos, srcSize);
        //*LOG*/ System.err.flush();
        final long address = addresses[((int) (offset >>> chunkShift))];
        offset = offset & chunkSizeModMask;;


        copyFromArray(src, arrayBaseOffset, srcPos,
                address+offset, srcSize);

    }

    @Override
    public void putData(long offset, ByteBuffer buf) {
        //*LOG*/ System.err.printf("putData: offset:%d, bufPos:%d, bufLimit:%d:\n",offset,buf.position(), buf.limit());
        //*LOG*/ System.err.flush();
        final long address = addresses[((int) (offset >>> chunkShift))];
        offset = offset & chunkSizeModMask;;

        for(int pos=buf.position();pos<buf.limit();pos++){
            unsafe.putByte(address +offset+pos,buf.get(pos));
        }

    }

    @Override
    public long getLong(long offset) {
        //*LOG*/ System.err.printf("getLong: offset:%d \n",offset);
        //*LOG*/ System.err.flush();
        final long address = addresses[((int) (offset >>> chunkShift))];
        offset = offset & chunkSizeModMask;;
        long l =  unsafe.getLong(address +offset);
        return Long.reverseBytes(l);
    }

    @Override
    public int getInt(long offset) {
        //*LOG*/ System.err.printf("getInt: offset:%d\n",offset);
        //*LOG*/ System.err.flush();
        final long address = addresses[((int) (offset >>> chunkShift))];
        offset = offset & chunkSizeModMask;;
        int i =  unsafe.getInt(address +offset);
        return Integer.reverseBytes(i);
    }

    @Override
    public byte getByte(long offset) {
        //*LOG*/ System.err.printf("getByte: offset:%d\n",offset);
        //*LOG*/ System.err.flush();
        final long address = addresses[((int) (offset >>> chunkShift))];
        offset = offset & chunkSizeModMask;;

        return unsafe.getByte(address +offset);
    }

    @Override
    public DataInput2 getDataInput(long offset, int size) {
        //*LOG*/ System.err.printf("getDataInput: offset:%d, size:%d\n",offset,size);
        //*LOG*/ System.err.flush();
        byte[] dst = new byte[size];
//        for(int pos=0;pos<size;pos++){
//            dst[pos] = unsafe.getByte(address +offset+pos);
//        }

        final long address = addresses[((int) (offset >>> chunkShift))];
        offset = offset & chunkSizeModMask;;

        copyToArray(address+offset, dst, arrayBaseOffset,
                0,
                size);

        return new DataInput2(dst);
    }

    @Override
    public void close() {
        //*LOG*/ System.err.printf("close\n");
        //*LOG*/ System.err.flush();
        for(long address:addresses){
            if(address!=0)
                unsafe.freeMemory(address);
        }
    }

    @Override
    public void sync() {
    }

    @Override
    public boolean isEmpty() {
        return addresses.length==0;
    }

    @Override
    public void deleteFile() {
    }

    @Override
    public boolean isSliced() {
        return false;
    }

    @Override
    public File getFile() {
        return null;
    }
}
