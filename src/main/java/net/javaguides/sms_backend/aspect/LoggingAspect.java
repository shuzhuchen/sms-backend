package net.javaguides.sms_backend.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    // Matches every method in the service package and its subpackages
    @Pointcut("execution(* net.javaguides.sms_backend.service..*(..))")
    public void serviceMethods() {
    }

    // runs before every matched service method starts - logs method name and arguments
    @Before("serviceMethods()")
    public void logBefore(JoinPoint jp) {
        log.info("[BEFORE] {} args={}", jp.getSignature().toShortString(), Arrays.toString(jp.getArgs()));
    }

    // runs after the matched service method returns successfully.
    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logAfterReturning(JoinPoint jp, Object result) {
        log.info("[AFTER] {} completed result={}", jp.getSignature().toShortString(), result);
    }

    // runs after the matched service method throws an exception.
    @AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint jp, Throwable ex) {
        log.warn("[AFTER] {} failed: {}", jp.getSignature().toShortString(), ex.getMessage());
    }

    // Around wraps the matched methods - measures and logs execution time.
    @Around("serviceMethods()")
    public Object measureTime(ProceedingJoinPoint jp) throws Throwable {
        long start = System.currentTimeMillis();

        // proceed() runs the original service method.
        Object result = jp.proceed();
        long duration = System.currentTimeMillis() - start;
        log.info("[AROUND] {} took {} ms", jp.getSignature().toShortString(), duration);
        return result;
    }
}
