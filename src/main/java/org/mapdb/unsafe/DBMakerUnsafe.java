package org.mapdb.unsafe;

import org.mapdb.CC;
import org.mapdb.DBMaker;
import org.mapdb.Volume;

/**
 * Provides {@link org.mapdb.DBMaker} with Unsafe extension
 */
public class DBMakerUnsafe {

    public static org.mapdb.DBMaker newMemoryDirectDB() {
        return new DBMaker(){
            @Override
            protected Volume.Factory extendStoreVolumeFactory() {

                final long sizeLimit = propsGetLong(Keys.sizeLimit,0);

                return new Volume.Factory(){

                    @Override
                    public Volume createIndexVolume() {
                        return new UnsafeVolume(sizeLimit,CC.VOLUME_CHUNK_SHIFT);
                    }

                    @Override
                    public Volume createPhysVolume() {
                        return new UnsafeVolume(sizeLimit, CC.VOLUME_CHUNK_SHIFT);
                    }

                    @Override
                    public Volume createTransLogVolume() {
                        return new UnsafeVolume(sizeLimit,CC.VOLUME_CHUNK_SHIFT);
                    }
                };
            }
        };
    }
}
