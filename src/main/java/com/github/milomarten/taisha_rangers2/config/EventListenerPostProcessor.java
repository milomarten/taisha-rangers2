package com.github.milomarten.taisha_rangers2.config;

import com.github.milomarten.taisha_rangers2.command.DiscordEventListener;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventListenerPostProcessor implements BeanPostProcessor {
    private final GatewayDiscordClient gateway;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithMethods(
                bean.getClass(),
                new ReflectionUtils.MethodCallback() {
                    @Override
                    public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                        if (method.getParameterCount() != 1) {
                            throw new IllegalArgumentException("Expect at least one parameter");
                        }
                        var parameter = method.getParameterTypes()[0];
                        if (Event.class.isAssignableFrom(parameter)) {
                            gateway.on((Class<? extends Event>) parameter, e -> {
                                try {
                                    var res = method.invoke(bean, e);
                                    if (res instanceof Publisher<?> p) {
                                        return Mono.from(p).then();
                                    } else {
                                        return Mono.empty();
                                    }
                                } catch (Exception ex) {
                                    log.error("Error calling listener {}", getMethodName(method), ex);
                                    return Mono.empty();
                                }
                            })
                            .subscribe();
                            log.info("Registered listener {}", getMethodName(method));
                        } else {
                            throw new IllegalArgumentException("Expect at least one parameter of a valid Discord event");
                        }
                    }
                },
                method -> method.isAnnotationPresent(DiscordEventListener.class)
        );
        return bean;
    }

    private static String getMethodName(Method method) {
        var enclosedClass = method.getDeclaringClass().getName();
        var methodName = method.getName();
        var paramType = method.getParameterTypes()[0].getSimpleName();
        return enclosedClass + "." + methodName + "(" + paramType + ")";
    }
}
