package com.oreki.redis.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 27639
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisData {
    private Object data;
    private LocalDateTime expireTime;
}
