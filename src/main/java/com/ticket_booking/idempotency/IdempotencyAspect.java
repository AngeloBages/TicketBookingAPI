package com.ticket_booking.idempotency;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.ticket_booking.common.exceptions.DomainException;
import com.ticket_booking.common.exceptions.IntegrationException;
import com.ticket_booking.common.exceptions.enums.OperationOutcome;
import com.ticket_booking.domain.models.IdempotencyRecord;

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE)
@Component
public class IdempotencyAspect {

    private final IdempotencyService idempotencyService;
    private final IdempotencyFingerprintService fingerprintService;
    private final IdempotencyResponseSerializer responseSerializer;
    private final ExpressionParser expressionParser;
    private final ParameterNameDiscoverer parameterNameDiscoverer;

    public IdempotencyAspect(
            IdempotencyService idempotencyService,
            IdempotencyFingerprintService fingerprintService,
            IdempotencyResponseSerializer responseSerializer
    ) {
        this.idempotencyService = idempotencyService;
        this.fingerprintService = fingerprintService;
        this.responseSerializer = responseSerializer;

        this.expressionParser =
                new SpelExpressionParser();

        this.parameterNameDiscoverer =
                new DefaultParameterNameDiscoverer();
    }

    @Around("@annotation(idempotent)")
    public Object execute(
            ProceedingJoinPoint joinPoint,
            Idempotent idempotent
    ) throws Throwable {

        StandardEvaluationContext context =
                createEvaluationContext(joinPoint);

        String idempotencyKey =
                evaluate(
                        idempotent.key(),
                        context,
                        String.class
                );

        String identityId =
                evaluate(
                        idempotent.identityId(),
                        context,
                        String.class
                );

        Object fingerprint =
                evaluate(
                        idempotent.fingerprint(),
                        context,
                        Object.class
                );

        String requestHash =
                fingerprintService.hash(fingerprint);

        IdempotencyRecord record =
                idempotencyService.claim(
                        idempotent.operation(),
                        identityId,
                        idempotencyKey,
                        requestHash
                );

        if (record.isCompleted()) {

            return responseSerializer.deserialize(
                    record.getResponseBody(),
                    resolveReturnType(joinPoint)
            );
        }

        try {

            Object result = joinPoint.proceed();

            registerSuccessfulCompletion(
                    record,
                    result
            );

            return result;

        } catch (Throwable ex) {

            handleFailure(
                    record,
                    ex
            );

            throw ex;
        }
    }

    private void registerSuccessfulCompletion(
            IdempotencyRecord record,
            Object result
    ) {

        String responseBody =
                responseSerializer.serialize(result);

        if (TransactionSynchronizationManager
                .isSynchronizationActive()) {

            TransactionSynchronizationManager
                    .registerSynchronization(
                            new TransactionSynchronization() {

                                @Override
                                public void afterCommit() {

                                    idempotencyService.complete(
                                            record.getId(),
                                            responseBody
                                    );
                                }

                                @Override
                                public void afterCompletion(
                                        int status
                                ) {

                                    if (status ==
                                            TransactionSynchronization
                                                    .STATUS_ROLLED_BACK) {

                                        idempotencyService.release(
                                                record.getId()
                                        );
                                    }
                                }
                            }
                    );

        } else {

            idempotencyService.complete(
                    record.getId(),
                    responseBody
            );
        }
    }

    private void handleFailure(
            IdempotencyRecord record,
            Throwable exception
    ) {

    	if (exception instanceof DomainException) {

            idempotencyService.release(
                    record.getId()
            );

            return;
        }

        if (exception instanceof IntegrationException integration) {

            if (integration.getOutcome()
                    == OperationOutcome.FAILED) {

                idempotencyService.release(
                        record.getId()
                );

                return;
            }

            /*
             * UNKNOWN
 			 * cannot safely determine whether the external operation happened.
             *
             * Keep IN_PROGRESS.
             */
            return;
        }

        // Default behavior for unexpected exceptions.
        idempotencyService.release(
                record.getId()
        );
    }

    private StandardEvaluationContext
    createEvaluationContext(
            ProceedingJoinPoint joinPoint
    ) {

        MethodSignature signature =
                (MethodSignature) joinPoint.getSignature();

        Method method =
                signature.getMethod();

        StandardEvaluationContext context =
                new StandardEvaluationContext();

        String[] parameterNames =
                parameterNameDiscoverer
                        .getParameterNames(method);

        Object[] arguments =
                joinPoint.getArgs();

        if (parameterNames != null) {

            for (int i = 0;
                 i < parameterNames.length;
                 i++) {

                context.setVariable(
                        parameterNames[i],
                        arguments[i]
                );
            }
        }

        return context;
    }

    private <T> T evaluate(
            String expression,
            EvaluationContext context,
            Class<T> type
    ) {

        return expressionParser
                .parseExpression(expression)
                .getValue(context, type);
    }

    private Class<?> resolveReturnType(
            ProceedingJoinPoint joinPoint
    ) {

        MethodSignature signature =
                (MethodSignature) joinPoint.getSignature();

        return signature.getReturnType();
    }
}