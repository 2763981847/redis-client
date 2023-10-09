package cn.autumnclouds.redis.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * @author autumnclouds
 */
@Data
@ConfigurationProperties("autumnclouds.redis")
public class RedisClientProperties {

    private Long cacheNullTtl = 2L;
    private String lockKey = "lock:";
    private Long lockTtl = 10L;

}
