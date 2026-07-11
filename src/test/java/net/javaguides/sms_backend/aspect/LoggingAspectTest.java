package net.javaguides.sms_backend.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoggingAspectTest {

    private final LoggingAspect loggingAspect = new LoggingAspect();

    @Test
    void serviceMethodsPointcutMethodIsCallable() {
        assertThatCode(loggingAspect::serviceMethods).doesNotThrowAnyException();
    }

    @Test
    void logBeforeLogsJoinPointArguments() {
        JoinPoint joinPoint = joinPoint("StudentService.getAll()");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg"});

        assertThatCode(() -> loggingAspect.logBefore(joinPoint)).doesNotThrowAnyException();
    }

    @Test
    void logAfterReturningLogsResult() {
        JoinPoint joinPoint = joinPoint("StudentService.getAll()");

        assertThatCode(() -> loggingAspect.logAfterReturning(joinPoint, "result")).doesNotThrowAnyException();
    }

    @Test
    void logAfterThrowingLogsException() {
        JoinPoint joinPoint = joinPoint("StudentService.getAll()");

        assertThatCode(() -> loggingAspect.logAfterThrowing(joinPoint, new RuntimeException("failed"))).doesNotThrowAnyException();
    }

    private static JoinPoint joinPoint(String shortString) {
        Signature signature = mock(Signature.class);
        when(signature.toShortString()).thenReturn(shortString);

        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        return joinPoint;
    }
}
