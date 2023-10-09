package cn.autumnclouds.redis.client;


import cn.autumnclouds.redis.properties.RedisClientProperties;
import cn.autumnclouds.redis.model.RedisData;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author Fu Qiujie
 * @since 2023/3/11
 */
@AllArgsConstructor
public class RedisClient {
    private final StringRedisTemplate stringRedisTemplate;

    private final RedisClientProperties redisClientProperties;


    private static final ExecutorService CACHE_REBUILD_EXECUTOR
            = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(), runnable -> new Thread(runnable, "缓存重建线程"));


    /**
     * 将任意对象缓存至redis
     *
     * @param key        key
     * @param value      value
     * @param expireTime 过期时间
     * @param timeUnit   timeUnit
     */
    public void set(String key, Object value, long expireTime, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), expireTime, timeUnit);
    }

    /**
     * 将任意对象缓存至redis(逻辑过期)
     *
     * @param key        key
     * @param value      value
     * @param expireTime 逻辑过期时间
     * @param timeUnit   timeUnit
     */
    public void setWithLogicExpire(String key, Object value, long expireTime, TimeUnit timeUnit) {
        LocalDateTime logicExpireTime = LocalDateTime.now().plusSeconds(timeUnit.toSeconds(expireTime));
        RedisData redisData = new RedisData(value, logicExpireTime);
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 从redis或数据库中拿到数据（带缓存穿透解决）
     *
     * @param key        redis中的key
     * @param type       要查询的数据类型
     * @param expireTime 缓存过期时间
     * @param timeUnit   timeUnit
     * @param dbCallback 数据库回调操作（redis中未查到数据后进行的数据库查询操作）
     * @param <T>        查询到的数据类型
     * @return 查询到的数据
     */
    public <T> T getWithPassThrough(String key, Class<T> type, long expireTime, TimeUnit timeUnit, Supplier<T> dbCallback) {
        //先尝试从redis中拿到数据
        String jsonString = stringRedisTemplate.opsForValue().get(key);
        //如果成功拿到
        if (jsonString != null) {
            //刷新过期时间
            stringRedisTemplate.expire(key, expireTime, timeUnit);
            //判断是否为空对象
            if (jsonString.isEmpty()) {
                //是空对象则返回null
                return null;
            }
            //不为空则返回数据
            return JSONUtil.toBean(jsonString, type);
        }
        //如果redis中没有数据则查询数据库
        T data = dbCallback.get();
        if (data == null) {
            //未查到数据则缓存空对象
            stringRedisTemplate.opsForValue().set(key, "", redisClientProperties.getCacheNullTtl(), TimeUnit.MINUTES);
            //返回空
            return null;
        }
        //查到数据则缓存至redis并返回
        this.set(key, data, expireTime, timeUnit);
        return data;
    }

    /**
     * 从redis或数据库中拿到数据（带缓存击穿解决）
     *
     * @param key        redis中的key
     * @param type       要查询的数据类型
     * @param expireTime 缓存逻辑过期时间
     * @param timeUnit   timeUnit
     * @param dbCallback 数据库回调操作（redis中未查到数据后进行的数据库查询操作）
     * @param <T>        查询到的数据类型
     * @return 查询到的数据
     */
    public <T> T getWithLogicExpire(String key, Class<T> type, long expireTime, TimeUnit timeUnit, Supplier<T> dbCallback) {
        //先尝试从redis中拿到数据
        String jsonString = stringRedisTemplate.opsForValue().get(key);
        //缓存未命中则返回空
        if (StrUtil.isBlank(jsonString)) {
            return null;
        }
        //缓存命中则判断缓存是否已过期
        RedisData redisData = JSONUtil.toBean(jsonString, RedisData.class);
        T data = Convert.convert(type, redisData.getData());
        if (LocalDateTime.now().isBefore(redisData.getExpireTime())) {
            //未过期则刷新缓存并返回
            this.setWithLogicExpire(key, data, expireTime, timeUnit);
            return data;
        }
        //已过期则尝试重建缓存
        tryReBuildData(key, expireTime, timeUnit, dbCallback);
        return data;
    }

    /**
     * 尝试重建缓存
     *
     * @param key        key
     * @param expireTime 缓存逻辑过期时间
     * @param timeUnit   timeUnit
     * @param dbCallback 数据库查询函数
     * @param <T>        数据库返回数据类型
     */
    private <T> void tryReBuildData(String key, long expireTime, TimeUnit timeUnit, Supplier<T> dbCallback) {
        //尝试获取互斥锁进行缓存重建
        boolean tryLock = tryLock(key);
        if (tryLock) {
            //获取到锁调用用其他线程开始进行缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    //查询数据库
                    T t = dbCallback.get();
                    //写入redis
                    this.setWithLogicExpire(key, t, expireTime, timeUnit);
                } finally {
                    //释放锁
                    unlock(key);
                }
            });
        }
    }

    /**
     * 尝试获得redis锁
     *
     * @param key 锁的key
     * @return 是否获取成功
     */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(redisClientProperties.getLockKey() + key, "lock", redisClientProperties.getLockTtl(), TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }


    /**
     * 解除redis锁
     *
     * @param key 锁的key
     */
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}