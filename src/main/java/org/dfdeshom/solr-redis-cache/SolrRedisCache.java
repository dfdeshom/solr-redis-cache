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
import org.apache.solr.search.SolrCache;
import org.apache.solr.search.SolrCacheBase;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.CacheRegenerator;

import redis.clients.jedis.Jedis;
import net.spy.memcached.MemcachedClient;

/**
 * 
 *
 */

public class SolrRedisCache<K,V> extends SolrCacheBase implements SolrCache<K,V>
{
    public static Logger log = Logger.getLogger(SolrRedisCache.class.getName());
	
    /* An instance of this class will be shared across multiple instances
     * of an LRUCache at the same time.  Make sure everything is thread safe.
     */
    private static class CumulativeStats {
        AtomicLong lookups = new AtomicLong();
        AtomicLong hits = new AtomicLong();
        AtomicLong inserts = new AtomicLong();
        AtomicLong evictions = new AtomicLong();
    }

    private CumulativeStats stats;

    // per instance stats.  The synchronization used for the map will also be
    // used for updating these statistics (and hence they are not AtomicLongs
    private long lookups;
    private long hits;
    private long inserts;
    private long evictions;

    private long warmupTime = 0;

    private MemcachedClient memcachedClient;
    private RedisCache cache;
	
    private String name;
    private int port;
    private String defaultString = "localhost";
    private String keyPrefix = "";
    
    private State state;
    private String description="Redis Cache";
    
    public Object init(Map args, Object persistence, CacheRegenerator regenerator) {
        state=State.CREATED;
        name = (String)args.get("name");

        String prefix = (String)args.get("keyPrefix");
        if(prefix != null) {
            keyPrefix = prefix;
        }
		
        String host = (String)args.get("host");
        if(host == null) {
            host = "localhost";
        }

        String portStr = (String)args.get("port");
        if(portStr == null) {
            port = 6379;
        }
        else{
            port = Integer.parseInt(portStr);
        }

        description = "RedisCache("+host+", Port:"+port+", keyPrefix:"+keyPrefix+")";

        try{
            cache = new RedisCache(host+":"+ Integer.toString(port));
        }
        catch(java.io.UnsupportedEncodingException e){

        }

        if (persistence==null) {
            // must be the first time a cache of this type is being created
            persistence = new CumulativeStats();
        }

        stats = (CumulativeStats)persistence;
        
        return persistence;
    }

    public String name() {
        return name;
    }

    public int size() {
        return (int)cache.size();
    }

    public Object put(Object key, Object value) {
        if (state == State.LIVE) {
            stats.inserts.incrementAndGet();
        }
        
        try{
            cache.put(toKeyString(key), value);
        }
        catch(Exception e){
        
        }
        return value;
    
    }

    private String toKeyString(Object key) {
        return keyPrefix+":"+key.hashCode();
    }
    
    public Object get(Object key) {
        Object val = new Object();
        try{
            val = cache.get(toKeyString(key));
        }
        catch(java.io.UnsupportedEncodingException e) {
            
        }
        if (state == State.LIVE) {
            // only increment lookups and hits if we are live.
            lookups++;
            stats.lookups.incrementAndGet();
            if (val!=null) {
                hits++;
                stats.hits.incrementAndGet();
            }
        }
        return val;
        
	}

    public void clear(){
        try{
            cache.clear();
        }
        catch(java.io.UnsupportedEncodingException e) {
            return;
        }
    }

    public void warm(SolrIndexSearcher searcher, SolrCache old)  {
        
    }
	
    public void close() {
        
    }


    //////////////////////// SolrInfoMBeans methods //////////////////////


    public String getName() {
        return SolrRedisCache.class.getName();
    }
    
    public String getVersion() {
        return SolrCore.version;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return Category.CACHE;
    }

    public String getSourceId() {
        return "$Id: SolrRedisCache.java $";
    }
    
    public String getSource() {
        return "";
    }
    
    public URL[] getDocs() {
        return null;
    }
    
    
    public NamedList getStatistics() {
        NamedList lst = new SimpleOrderedMap();
        //synchronized (map) {
        lst.add("lookups", lookups);
        lst.add("hits", hits);
        lst.add("hitratio", calcHitRatio(lookups,hits));
        lst.add("inserts", inserts);
        lst.add("evictions", evictions);
        lst.add("size", size());
        // }
        
        lst.add("warmupTime", warmupTime);
        
        long clookups = stats.lookups.get();
        long chits = stats.hits.get();
        lst.add("cumulative_lookups", clookups);
        lst.add("cumulative_hits", chits);
        lst.add("cumulative_hitratio", calcHitRatio(clookups,chits));
        lst.add("cumulative_inserts", stats.inserts.get());
        lst.add("cumulative_evictions", stats.evictions.get());
        
        //lst.add("indexVersion", indexVersion);
        return lst;
    }
    
}
