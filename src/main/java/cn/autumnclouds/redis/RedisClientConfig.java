package cn.autumnclouds.redis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Fu Qiujie
 * @since 2023/3/11
 */
@Configuration
@ComponentScan
@ConfigurationProperties("autumnclouds.redis")
@Data
public class RedisClientConfig {
    public  Long cache_ull_ttl = 2L;
    public String lock_key = "lock:";
    public  Long lock_ttl = 10L;
}
