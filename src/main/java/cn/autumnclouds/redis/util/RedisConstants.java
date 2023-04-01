package cn.autumnclouds.redis.util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


/**
 * @author autumnclouds
 */
@Configuration
public class RedisConstants {
    public static Long CACHE_NULL_TTL = 2L;
    public static String LOCK_KEY = "lock:";
    public static Long LOCK_TTL = 10L;

    @Value("${autumnclouds.redis.cache-null-ttl:10}")
    private void setCacheNullTtl(Long cacheNullTTL) {
        RedisConstants.CACHE_NULL_TTL= cacheNullTTL;
    }

    @Value("${autumnclouds.redis.lock-key:lock:}")
    private void setLockKey(String lockKey) {
        RedisConstants.LOCK_KEY = lockKey;
    }

    @Value("${autumnclouds.redis.lock-ttl:10}")
    private void setLockTtl(Long lockTTL) {
        RedisConstants.LOCK_TTL = lockTTL;
    }
}
