package net.javaguides.sms_backend.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
        log.info("[BEFORE] {}", jp.getSignature().toShortString());
    }

    // runs after the matched service method finishes, whether it succeeds or throws an exception.
    @After("serviceMethods()")
    public void logAfter(JoinPoint jp) {
        log.info("[AFTER] {} completed", jp.getSignature().toShortString());
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
