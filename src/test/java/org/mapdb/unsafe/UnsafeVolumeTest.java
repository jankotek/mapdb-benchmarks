package org.mapdb.unsafe;

import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.*;

public class UnsafeVolumeTest {

    @Test
    public void largeA() throws IOException {

        byte[] b = new byte[10000];
        new Random().nextBytes(b);

        UnsafeVolume vol = new UnsafeVolume();
        vol.ensureAvailable((long) 1e7);

        int offset = 3000000;
        vol.putData(offset,b,0,b.length);

        byte[] b2 = new byte[b.length];
        vol.getDataInput(offset,b.length).read(b2);

        assertArrayEquals(b, b2);


        vol.putLong(offset,111L);
        assertEquals(111L,vol.getLong(offset));
        assertEquals(111L,vol.getDataInput(offset,b.length).readLong());

        vol.close();


    }

}