package cn.autumnclouds.redis;

import cn.autumnclouds.redis.util.RedisConstants;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
@AutoConfiguration
public class RedisClientConfig {
    private Long cacheNullTtl =2L;
    private String lockKey = "lock:";
    private Long lockTtl = 10L;
}
