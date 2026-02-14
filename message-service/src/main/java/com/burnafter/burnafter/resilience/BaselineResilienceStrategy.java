package com.burnafter.burnafter.resilience;

import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component("baselineStrategy")
public class BaselineResilienceStrategy implements ResilienceStrategy {

    @Override
    public <T> T execute(Supplier<T> operation) {
        return operation.get();
    }
}
