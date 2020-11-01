package com.mandric.ratelimiterspringbootstarter.aspect;

import com.mandric.ratelimiterspringbootstarter.config.RateLimiterConfig;
import com.mandric.ratelimiterspringbootstarter.service.RateLimiterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class RateLimiterAspect implements BeanFactoryAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiterAspect.class);

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();
    private static final TemplateParserContext PARSER_CONTEXT = new TemplateParserContext();
    private static final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

    private RateLimiterService rateLimiterService;
    private RateLimiterConfig rateLimiterConfig;

    @Around("@annotation(RateLimiter)")
    public Object rateLimit(final ProceedingJoinPoint joinPoint) throws Throwable {
        final String limitName = generateLimitName(joinPoint);

        if (Objects.isNull(rateLimiterConfig.getLimits())) {
            LOGGER.warn("No limits defined!");
            return joinPoint.proceed();
        }

        final String mappedLimit = rateLimiterConfig.getLimits().entrySet().stream()
                .filter(Objects::nonNull)
                .filter(limit -> limitName.equals(limit.getKey()))
                .map(Map.Entry::getValue).findFirst()
                .orElseGet(() -> rateLimiterConfig.getLimits().get("default"));

        if (Objects.isNull(mappedLimit)) {
            log.warn("No default limit is defined, considered defining one?");
        }

        rateLimiterService.limit(mappedLimit);
        final Object proceed = joinPoint.proceed();
        rateLimiterService.release(mappedLimit);

        return proceed;
    }

    private String generateLimitName(ProceedingJoinPoint joinPoint) {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Method method = signature.getMethod();
        final RateLimiter customAnnotation = method.getAnnotation(RateLimiter.class);

        if (customAnnotation.name().isEmpty()) {
            return String.format("%s.%s", signature.getDeclaringType().getName(), signature.getName());
        }

        return RateLimiterAspect.CustomSpringExpressionLanguageParser.getDynamicValue(customAnnotation.name());
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
    }

    static class CustomSpringExpressionLanguageParser {
        static String getDynamicValue(String key) {
            return PARSER.parseExpression(key, PARSER_CONTEXT)
                    .getValue(evaluationContext, String.class);
        }
    }
}