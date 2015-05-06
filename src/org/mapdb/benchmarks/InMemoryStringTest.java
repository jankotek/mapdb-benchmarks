package org.mapdb.benchmarks;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.Serializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * In-memory concurrent benchmark of MapDB versus ConcurrentSkipListMap
 */
public class InMemoryStringTest {

    static long time;
    static long size;
    final static int stringSize = 32;
    final static Random r = new Random();


    public static void main(String[] args) {
        time = Long.parseLong(args[0])*1000;
        size = Long.parseLong(args[1])*1000000;

        int type  = Integer.parseInt(args[2]);
        int threads = Integer.parseInt(args[3]);

        Map m;
        String title;

        if(type ==0) {
            m = new ConcurrentHashMap();
        }else if(type ==1){
            m = new ConcurrentSkipListMap();
        }else if(type ==2){
            m = org.mapdb10.DBMaker.newMemoryDB().transactionDisable().make()
                    .createHashMap("test")
                    .keySerializer(org.mapdb10.Serializer.STRING)
                    .valueSerializer(org.mapdb10.Serializer.UUID)
                    .make();
        }else if(type ==3){
            m = org.mapdb10.DBMaker.newMemoryDB().transactionDisable().make()
                    .createTreeMap("test")
                    .keySerializer(org.mapdb10.BTreeKeySerializer.STRING)

                    .valueSerializer(org.mapdb10.Serializer.UUID)
                    .make();
        }else if(type ==4){
            m =
//                    DBMaker.newMemoryDB().transactionDisable().make()
//                    .createHashMap("test")
                    DBMaker.hashMapSegmentedMemory()
                    .keySerializer(Serializer.STRING)
                    .valueSerializer(Serializer.UUID)
                    .make();
        }else if(type ==5){
            m = DBMaker.newMemoryDB().transactionDisable().make()
                    .createTreeMap("test")
                    .keySerializer(BTreeKeySerializer.STRING)

                    .valueSerializer(Serializer.UUID)
                    .make();
        }else{
            throw new IllegalArgumentException("Unknown map type: "+type);
        }


        if(m.isEmpty()) {
            for (Long key = 0L; key < size; key++) {
                m.put(BU.randomString(stringSize), new UUID(r.nextLong(),r.nextLong()));
            }
        }


        runBench(m.getClass().getName(), threads,m);
    }


    private static void runBench(String title, int threadNum, final Map m) {

        {

            BU.printStart(title,"reads",threadNum);
            ExecutorService e = Executors.newCachedThreadPool();

            for (int i = 0; i < threadNum; i++) {
                e.execute(new Runnable() {
                    @Override
                    public void run() {
                        Random r = new Random();
                        while (BU.printIncrementTime(time)) {
                            String key = BU.randomString(stringSize);
                            Object value = m.get(key);
//                            if (value == null)
//                                throw new InternalError();
                        }
                    }
                });
            }

            BU.shutdown(e);
            BU.printEnd();
        }

        {
            BU.printStart(title, "updates", threadNum);
            ExecutorService e = Executors.newCachedThreadPool();

            for (int i = 0; i < threadNum; i++) {
                e.execute(new Runnable() {
                    @Override
                    public void run() {
                        Random r = new Random();
                        while (BU.printIncrementTime(time)) {
                            String key = BU.randomString(stringSize);
                            UUID uuid = new UUID(r.nextLong(),r.nextLong());
                            Object value = m.put(key,uuid);
//                            if (value == null)
//                                throw new InternalError();
                        }
                    }
                });
            }

            BU.shutdown(e);
            BU.printEnd();
        }

        {
            BU.printStart(title,"combined",threadNum);
            ExecutorService e = Executors.newCachedThreadPool();

            for (int i = 0; i < threadNum; i++) {
                e.execute(new Runnable() {
                    @Override
                    public void run() {
                        Random r = new Random();
                        while (BU.printIncrementTime(time) && BU.printIncrementTime(time) && BU.printIncrementTime(time)) {

                            String key = BU.randomString(stringSize);
                            Object value = m.get(key);
//                            if (value == null)
//                                throw new InternalError();

                            key = BU.randomString(stringSize);
                            value = m.get(key);
//                            if (value == null)
//                                throw new InternalError();


                            key = BU.randomString(stringSize);
                            UUID uuid = new UUID(r.nextLong(), r.nextLong());
                            value = m.put(key,uuid);
//                            if (value == null)
//                                throw new InternalError();
                        }
                    }
                });
            }

            BU.shutdown(e);
            BU.printEnd();
        }


    }
}
