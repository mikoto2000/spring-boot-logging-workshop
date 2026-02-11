package dev.mikoto2000.springboot.logging.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

  private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

  @Around(
    "within(dev.mikoto2000.springboot.logging.controller..*)"
  )
  public Object logMethod(ProceedingJoinPoint pjp) throws Throwable {

    // メソッド情報取得
    String className = pjp.getTarget().getClass().getSimpleName();
    String methodName = pjp.getSignature().getName();

    log.info("START {}#{}", className, methodName);

    // 時間計測開始
    long startTime = System.currentTimeMillis();
    try {

      Object result = pjp.proceed();

      // 時間計測終了
      long endTime = System.currentTimeMillis();

      log.info("END   {}#{}, time={}ms", className, methodName, endTime - startTime);

      return result;

    } catch (Throwable e) {

      // 時間計測終了
      long endTime = System.currentTimeMillis();

      log.error("ERROR {}#{}, time={}", className, methodName, endTime - startTime, e);

      throw e;
    }
  }
}

