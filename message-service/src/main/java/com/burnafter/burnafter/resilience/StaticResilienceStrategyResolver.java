package com.burnafter.burnafter.resilience;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class StaticResilienceStrategyResolver
        implements ResilienceStrategyResolver {

    private final ResilienceStrategy baselineStrategy;

    public StaticResilienceStrategyResolver(
            @Qualifier("baselineStrategy")
            ResilienceStrategy baselineStrategy) {

        this.baselineStrategy = baselineStrategy;
    }

    @Override
    public ResilienceStrategy resolve(String profile) {
        return baselineStrategy;
    }
}

