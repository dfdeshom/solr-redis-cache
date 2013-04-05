Solr Redis Cache
----------------

``solr_redis_cache`` is a generic cache for custom solr plugins. It is written for Solr4 and higher. Please see http://wiki.apache.org/solr/SolrCaching to learn more about them

Building
---------
You will need maven: ``mvn package`` . The reslting jar should be in your ``target/`` folder.

Configuration
--------------
In ``solrconfig.xml``, add the jar to your lib:
       <lib path="/path/to/solr_redis_cache-1.0-SNAPSHOT.jar" />
      
then configure your custom cache:
     <!-- Example of a generic cache.  These caches may be accessed by name
         through SolrIndexSearcher.getCache(),cacheLookup(), and cacheInsert().
         The purpose is to enable easy caching of user/application level data.
         The regenerator argument should be specified as an implementation
         of solr.search.CacheRegenerator if autowarming is desired.
      -->
     <!--
            <cache name="myUserCache"
              class="org.dfdeshom.solr_redis_cache.SolrRedisCache"
              size="4096"
              initialSize="1024"
              autowarmCount="1024"
              />


