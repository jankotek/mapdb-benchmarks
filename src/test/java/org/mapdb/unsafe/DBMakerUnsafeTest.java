package org.mapdb.unsafe;

import org.junit.Test;
import org.mapdb.DB;

import java.util.Map;

import static org.junit.Assert.*;

public class DBMakerUnsafeTest {

    @Test
    public void test(){
        DB db = DBMakerUnsafe.newMemoryDirectDB().cacheDisable().transactionDisable().make();
        Map m = db.getHashMap("test");
        m.put("aa","bb");
        assertEquals("bb",m.get("aa"));
        db.close();
    }

}