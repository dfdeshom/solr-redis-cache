package org.dfdeshom.solr_redis_cache;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrEventListener;

import redis.clients.jedis.BinaryJedis;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.SerializingTranscoder;
import net.spy.memcached.CachedData;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Hello world!
 *
 */
public class App 
{
    private BinaryJedis jedis;
    SerializingTranscoder encoder;
    private final String dataField = "data";
    private final String flagField = "flag";

    public App(){
        jedis = new BinaryJedis("localhost");
        encoder = new SerializingTranscoder();
    }

    public static void main( String[] args )
    {
        BinaryJedis jedis = new BinaryJedis("localhost");
        String i = "23err"; 
        LinkedList list = new LinkedList();
        list.add("1");
        SerializingTranscoder encoder = new SerializingTranscoder();
        CachedData cd = encoder.encode(list);
        byte[] b = cd.getData();
        System.out.print(Arrays.toString(b));
        
        try{
            jedis.set(i.getBytes("UTF-8"),b);
            byte[] value = jedis.get(i.getBytes("UTF-8"));
            //TOD: need to save flag also to retrieve object
            CachedData cd2 = new CachedData(1, value,CachedData.MAX_SIZE);
            Object res = encoder.decode(cd2);
            System.out.print(res.toString());
        }
        catch(Exception e){
            return;
        }
        System.out.print("Success!");
        
    }
}
