package focandlol.common.aop;

import focandlol.common.annotation.RedissonLock;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RedissonLockAspect {

  private final RedissonClient redissonClient;
  private final TransactionAspect transactionAspect;

  @Pointcut("@annotation(focandlol.common.annotation.RedissonLock)")
  private void distributeLock() {
  }

  @Around("distributeLock()")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();

    RedissonLock redissonLock = method.getAnnotation(RedissonLock.class);

    String lockKey = redissonLock.key();
    System.out.println("lockKey : " + lockKey);
    RLock lock = redissonClient.getLock(lockKey);

    boolean acquired = false;
    try {
      acquired = lock.tryLock(redissonLock.waitTime(), redissonLock.leaseTime(), TimeUnit.SECONDS);
      if (!acquired) {
        throw new RuntimeException("Could not acquire lock for key: " + lockKey);
      }

      return transactionAspect.proceed(joinPoint);

    } finally {
      if (acquired && lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }
}

