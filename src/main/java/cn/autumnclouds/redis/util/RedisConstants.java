package cn.autumnclouds.redis.util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * @author autumnclouds
 */
@Component
public class RedisConstants {
    public static Long CACHE_NULL_TTL = 2L;
    public static String LOCK_KEY = "lock:";
    public static Long LOCK_TTL = 10L;

    @Value("${autumnclouds.redis.cache-null-ttl:10}")
    public static void setCacheNullTtl(Long cacheNullTTL) {
        CACHE_NULL_TTL = cacheNullTTL;
    }

    @Value("${autumnclouds.redis.lock-key:lock:}")
    public static void setLockKey(String lockKey) {
        LOCK_KEY = lockKey;
    }

    @Value("${autumnclouds.redis.lock-ttl:10}")
    public static void setLockTtl(Long lockTTL) {
        LOCK_TTL = lockTTL;
    }
}
