package org.mapdb.benchmarks;

import org.mapdb.*;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * In-memory concurrent benchmark of MapDB versus ConcurrentSkipListMap
 */
public class InMemoryUUIDTest {

    final static long time = 100 * 1000;
    final static long size = (long) 1e8;

    //create map and fill it with data
    static Fun.Function1 newUUID = new Fun.Function1() {

        Random r = new Random();

        @Override
        public Object run(Object o) {
            return new UUID(r.nextLong(), r.nextLong());
        }
    };


    public static void main(String[] args) {


        boolean mapdb = Boolean.parseBoolean(args[0]);
        int threads = Integer.parseInt(args[1]);

        Map m;
        String title;

        if(mapdb) {
            DB db = DBMaker.newMemoryDB()
                    .transactionDisable()
                    .make();



            m = db.createTreeMap("map")
                    .pumpSource(BU.reverseLongIterator(0,size),newUUID)
                    .keySerializer(BTreeKeySerializer.ZERO_OR_POSITIVE_LONG)
                    .valueSerializer(Serializer.UUID)
                    .make();

            title = "BTreeMap";
        }else {

            //run NavigableSkipListMap

            m = new ConcurrentSkipListMap();

            for (Long key = 0L; key < size; key++) {
                m.put(key, newUUID.run(key));
            }

            title = "SkipListMap";
        }

        runBench(title, threads,m);
    }


    private static void runBench(String title, int threadNum, final Map m) {

        {

            BU.printStart(title+" #"+threadNum+" - reads");
            ExecutorService e = Executors.newCachedThreadPool();

            for (int i = 0; i < threadNum; i++) {
                e.execute(new Runnable() {
                    @Override
                    public void run() {
                        Random r = new Random();
                        while (BU.printIncrementTime(time)) {
                            Long key = Math.abs(r.nextLong() % size);
                            Object value = m.get(key);
                            if (value == null)
                                throw new InternalError();
                        }
                    }
                });
            }

            BU.shutdown(e);
            BU.printEnd();
        }

        {
            BU.printStart(title+" #"+threadNum+" - updates");
            ExecutorService e = Executors.newCachedThreadPool();

            for (int i = 0; i < threadNum; i++) {
                e.execute(new Runnable() {
                    @Override
                    public void run() {
                        Random r = new Random();
                        while (BU.printIncrementTime(time)) {
                            Long key = Math.abs(r.nextLong() % size);
                            UUID uuid = new UUID(r.nextLong(),r.nextLong());
                            Object value = m.put(key,uuid);
                            if (value == null)
                                throw new InternalError();
                        }
                    }
                });
            }

            BU.shutdown(e);
            BU.printEnd();
        }

        {
            BU.printStart(title+" #"+threadNum+" - combined");
            ExecutorService e = Executors.newCachedThreadPool();

            for (int i = 0; i < threadNum; i++) {
                e.execute(new Runnable() {
                    @Override
                    public void run() {
                        Random r = new Random();
                        while (BU.printIncrementTime(time) && BU.printIncrementTime(time) && BU.printIncrementTime(time)) {

                            Long key = Math.abs(r.nextLong() % size);
                            Object value = m.get(key);
                            if (value == null)
                                throw new InternalError();

                            key = Math.abs(r.nextLong() % size);
                            value = m.get(key);
                            if (value == null)
                                throw new InternalError();


                            key = Math.abs(r.nextLong() % size);
                            UUID uuid = new UUID(r.nextLong(), r.nextLong());
                            value = m.put(key,uuid);
                            if (value == null)
                                throw new InternalError();
                        }
                    }
                });
            }

            BU.shutdown(e);
            BU.printEnd();
        }


    }
}
