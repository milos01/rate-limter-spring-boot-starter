package com.mandric.ratelimiterspringbootstarter.aspect;

import com.mandric.ratelimiterspringbootstarter.config.RateLimiterConfig;
import com.mandric.ratelimiterspringbootstarter.service.RateLimiterService;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@DisplayName("When running RateLimiterAspect")
@ExtendWith(SpringExtension.class)
class RateLimiterAspectTest {

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private RateLimiterConfig rateLimiterConfig;

    @InjectMocks
    private RateLimiterAspect rateLimiterAspect;

    private static MockedStatic<RateLimiterAspect.CustomSpringExpressionLanguageParser> customSpringExpressionLanguageParserMockedStatic;

    @BeforeAll
    static void init() {
        customSpringExpressionLanguageParserMockedStatic = mockStatic(RateLimiterAspect.CustomSpringExpressionLanguageParser.class);
    }

    @AfterAll
    static void close() {
        customSpringExpressionLanguageParserMockedStatic.close();
    }

    @Test
    @DisplayName("RateLimiter method for bucket name not found")
    @SneakyThrows
    void testRateLimiterWithBucketNameFound() {
        MethodSignature signature = mock(MethodSignature.class);
        when(rateLimiterService.limit(anyString())).thenReturn(true);
        when(proceedingJoinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(testSignatureMethod());
        when(rateLimiterConfig.getLimits()).thenReturn(null);
        rateLimiterAspect.rateLimit(proceedingJoinPoint);
        verify(proceedingJoinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("RateLimiter method for bucket name empty")
    @SneakyThrows
    void testRateLimiterWithBucketNameEmpty() {
        MethodSignature signature = mock(MethodSignature.class);
        when(rateLimiterService.limit(anyString())).thenReturn(true);
        when(proceedingJoinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(testSignatureMethodWithoutName());
        when(signature.getDeclaringType()).thenReturn(testSignatureMethod().getDeclaringClass());
        when(signature.getName()).thenReturn("test");
        when(rateLimiterConfig.getLimits()).thenReturn(null);
        rateLimiterAspect.rateLimit(proceedingJoinPoint);
        verify(proceedingJoinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("RateLimiter method for no limits defined")
    @SneakyThrows
    void testRateLimiterWithNoLimitsDefined() {
        MethodSignature signature = mock(MethodSignature.class);
        when(proceedingJoinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(testSignatureMethodWithoutName());
        when(signature.getDeclaringType()).thenReturn(testSignatureMethod().getDeclaringClass());
        when(signature.getName()).thenReturn("testMethod");
        when(rateLimiterConfig.getLimits()).thenReturn(null);
        rateLimiterAspect.rateLimit(proceedingJoinPoint);

        verify(proceedingJoinPoint, times(1)).proceed();
        verify(rateLimiterService, times(0)).limit(anyString());
        verify(rateLimiterService, times(0)).release(anyString());
    }

    @Test
    @DisplayName("RateLimiter method for empty limits and no default limit")
    @SneakyThrows
    void testRateLimiterWithEmptyLimitsDefinedAndWithNoDefaultLimit() {
        MethodSignature signature = mock(MethodSignature.class);
        when(rateLimiterService.limit(anyString())).thenReturn(true);
        when(proceedingJoinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(testSignatureMethodWithoutName());
        when(signature.getDeclaringType()).thenReturn(testSignatureMethod().getDeclaringClass());
        when(signature.getName()).thenReturn("testMethod");
        when(rateLimiterConfig.getLimits()).thenReturn(generateLimitsMap());
        rateLimiterAspect.rateLimit(proceedingJoinPoint);

        verify(proceedingJoinPoint, times(1)).proceed();
        verify(rateLimiterService, times(1)).limit(null);
        verify(rateLimiterService, times(1)).release(null);
    }

    @Test
    @DisplayName("RateLimiter method for not empty limits and with default limit")
    @SneakyThrows
    void testRateLimiterWithEmptyLimitsDefinedAndWithDefaultLimit() {
        MethodSignature signature = mock(MethodSignature.class);
        when(rateLimiterService.limit(anyString())).thenReturn(true);
        when(proceedingJoinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(testSignatureMethodWithoutName());
        when(signature.getDeclaringType()).thenReturn(testSignatureMethod().getDeclaringClass());
        when(signature.getName()).thenReturn("testMethod");
        when(rateLimiterConfig.getLimits()).thenReturn(generateLimitsMapWithDefaultLimit());
        rateLimiterAspect.rateLimit(proceedingJoinPoint);

        verify(proceedingJoinPoint, times(1)).proceed();
        verify(rateLimiterService, times(1)).limit(eq("someDefaultLimitName"));
        verify(rateLimiterService, times(1)).release(eq("someDefaultLimitName"));
    }

    @Test
    @DisplayName("RateLimiter method for valid limits and with default limit")
    @SneakyThrows
    void testRateLimiterWithLimitsDefinedAndWithDefaultLimit() {
        MethodSignature signature = mock(MethodSignature.class);
        when(rateLimiterService.limit(anyString())).thenReturn(true);
        when(proceedingJoinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(testSignatureMethodWithoutName());
        when(signature.getDeclaringType()).thenReturn(testSignatureMethod().getDeclaringClass());
        when(signature.getName()).thenReturn("testMethod");
        when(rateLimiterConfig.getLimits()).thenReturn(generateLimitsMapWithDefaultLimitAndValidLimit());
        rateLimiterAspect.rateLimit(proceedingJoinPoint);

        verify(proceedingJoinPoint, times(1)).proceed();
        verify(rateLimiterService, times(1)).limit(eq("someLimitName"));
        verify(rateLimiterService, times(1)).release(eq("someLimitName"));
    }

    private Map<String, String> generateLimitsMap() {
        Map<String, String> limits = new HashMap<>();
        limits.put("testNotDefinedMethodName", "someLimitName");
        return limits;
    }

    private Map<String, String> generateLimitsMapWithDefaultLimit() {
        Map<String, String> limits = new HashMap<>();
        limits.put("testNotDefinedMethodName", "someLimitName");
        limits.put("default", "someDefaultLimitName");
        return limits;
    }

    private Map<String, String> generateLimitsMapWithDefaultLimitAndValidLimit() {
        Map<String, String> limits = new HashMap<>();
        limits.put(String.format("%s.%s", RateLimiterAspectTest.class.getName(), "testMethod"), "someLimitName");
        limits.put("default", "someDefaultLimitName");
        return limits;
    }

    private Method testSignatureMethod() throws NoSuchMethodException {
        return getClass().getDeclaredMethod("someMethod");
    }

    @RateLimiter(name = "testBucketName")
    private void someMethod() {
    }

    private Method testSignatureMethodWithoutName() throws NoSuchMethodException {
        return getClass().getDeclaredMethod("someMethodWithoutName");
    }

    @RateLimiter
    private void someMethodWithoutName() {
    }
}