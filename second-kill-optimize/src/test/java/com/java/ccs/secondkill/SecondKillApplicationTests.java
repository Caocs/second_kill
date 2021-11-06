package com.java.ccs.secondkill;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class SecondKillApplicationTests {
    private static final String REDIS_LOCK_KEY = "redis_lock_key";
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DefaultRedisScript<Boolean> redisLockDeleteScript;

    @Test
    void testRedisLock01() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 占位，如果key不存在才可以设置成功
        boolean isLock = valueOperations.setIfAbsent(REDIS_LOCK_KEY, true);
        if (isLock) {
            valueOperations.set("name", "ccs");
            String name = (String) valueOperations.get("name");
            System.out.println(name);
            // 删除锁
            redisTemplate.delete(REDIS_LOCK_KEY);
        } else {
            System.out.println("有线程在使用");
        }
    }

    /**
     * 程序异常等情况无法删除锁，后续访问都无法获取锁
     * ->
     * 加锁时，设置超时时间。
     */
    @Test
    void testRedisLock02() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 占位，如果key不存在才可以设置成功
        boolean isLock = valueOperations.setIfAbsent(REDIS_LOCK_KEY, true, 1, TimeUnit.MINUTES);
        if (isLock) {
            valueOperations.set("name", "ccs");
            String name = (String) valueOperations.get("name");
            System.out.println(name);
            // 删除锁
            redisTemplate.delete(REDIS_LOCK_KEY);
        } else {
            System.out.println("有线程在使用");
        }
    }

    /**
     * 如果某个加锁后的线程A执行时间过长，此时锁已经超过超时时间而自动释放。
     * 其他线程B就会加锁继续执行，此时A执行结束删除锁。
     * 但是A删除的锁时B加的锁。
     * ->
     * 给锁的value一个随机值，线程只能释放自己加的锁。
     * 但是：先查询锁的value，比较是否相同，删除锁 -> 不是原子操作
     */
    @Test
    void testRedisLock03() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String uuid = UUID.randomUUID().toString();
        boolean isLock = valueOperations.setIfAbsent(REDIS_LOCK_KEY, uuid, 1, TimeUnit.MINUTES);
        if (isLock) {
            valueOperations.set("name", "ccs");
            String name = (String) valueOperations.get("name");
            System.out.println(name);
            // 删除锁(先查询锁的value，比较是否相同，删除锁) -> 不是原子操作
            String lockValue = (String) valueOperations.get(REDIS_LOCK_KEY);
            if (uuid.equals(lockValue)) {
                redisTemplate.delete(REDIS_LOCK_KEY);
            }
        } else {
            System.out.println("有线程在使用");
        }
    }

    /**
     * 删除锁的过程：先查询锁的value，比较是否相同，删除锁 -> 不是原子操作
     * ->
     * Lua脚本
     */
    @Test
    void testRedisLock04() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String uuid = UUID.randomUUID().toString();
        boolean isLock = valueOperations.setIfAbsent(REDIS_LOCK_KEY, uuid, 1, TimeUnit.MINUTES);
        if (isLock) {
            valueOperations.set("name", "ccs");
            String name = (String) valueOperations.get("name");
            System.out.println(name);
            String lockValue = (String) valueOperations.get(REDIS_LOCK_KEY);
            System.out.println(lockValue);
            // 删除锁(使用lua脚本)
            Boolean result = (Boolean) redisTemplate.execute(redisLockDeleteScript, Collections.singletonList(REDIS_LOCK_KEY), uuid);
            System.out.println(result);
        } else {
            System.out.println("有线程在使用");
        }
    }
}
