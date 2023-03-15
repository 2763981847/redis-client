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
}
