package org.mapdb.benchmarks;


import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.TxMaker;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tests concurrent transactions
 */
public class TxMakerBench {

    public static void main(String[] args) throws IOException {

        final long size = (long) 1e6;

        OptionParser parser = new OptionParser();
        OptionSpec<Integer> cliThreads =
                parser.accepts( "threads" ).withOptionalArg().ofType( Integer.class )
                        .defaultsTo( 4 );
        OptionSpec<Integer> cliDur =
                parser.accepts( "duration" ).withOptionalArg().ofType( Integer.class )
                        .defaultsTo(10);

        OptionSpec<File> cliDir =
                parser.accepts("dir").withOptionalArg().ofType(File.class)
                        .defaultsTo(new File(System.getProperty("java.io.tmpdir")));

        OptionSet options = parser.parse(args);

        int threads = cliThreads.value(options);
        File f = File.createTempFile("mapdb", "bench", cliDir.value(options));

        //fill with data so there are not that many conflicts
        DB tx0 = DBMaker.fileDB(f)
                .mmapFileEnable()
                .transactionDisable()
                .make();

        Map m = tx0.hashMapCreate("aa")
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.LONG)
                .make();
        for(long i = 0;i<size;i++){
            m.put(i,i*10);
        }
        tx0.commit();
        tx0.close();

        //reopen
        final TxMaker txmaker = DBMaker.fileDB(f)
                .mmapFileEnable()
                .makeTxMaker();


        final long finish = cliDur.value(options)*1000 + System.currentTimeMillis();

        final AtomicLong result = new AtomicLong();
        BU.execNTimes(threads, new Callable() {
            @Override
            public Object call() throws Exception {
                while (System.currentTimeMillis() < finish) try{
                    DB db = txmaker.makeTx();

                    Map map = db.hashMap("aa");
                    long key = (long) (Math.random()*size);
                    long val = (long) (Math.random()*size);

                    map.put(key,val);

                    db.commit();
                    db.close();
                    result.incrementAndGet();
                }catch(Exception e){
                    e.printStackTrace();;
                }

                return null;
            }
        });

        txmaker.close();

        System.out.println(result);
     }
}
