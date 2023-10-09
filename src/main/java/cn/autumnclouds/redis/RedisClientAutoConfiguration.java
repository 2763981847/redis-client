package cn.autumnclouds.redis;

import cn.autumnclouds.redis.client.RedisClient;
import cn.autumnclouds.redis.properties.RedisClientProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author Fu Qiujie
 * @since 2023/3/11
 */
@AutoConfiguration(after = RedisAutoConfiguration.class)
@EnableConfigurationProperties(RedisClientProperties.class)
@ConditionalOnBean(StringRedisTemplate.class)
public class RedisClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RedisClient.class)
    public RedisClient redisClient(StringRedisTemplate stringRedisTemplate, RedisClientProperties redisClientProperties) {
        return new RedisClient(stringRedisTemplate, redisClientProperties);
    }
}
