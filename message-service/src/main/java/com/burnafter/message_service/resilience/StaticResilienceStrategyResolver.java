package com.burnafter.message_service.resilience;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class StaticResilienceStrategyResolver
        implements ResilienceStrategyResolver {

    private final ResilienceStrategy baselineStrategy;
    private final ResilienceStrategy chaosStrategy;

    public StaticResilienceStrategyResolver(

            @Qualifier("baselineStrategy")
            ResilienceStrategy baselineStrategy,

            @Qualifier("chaosStrategy")
            ResilienceStrategy chaosStrategy) {

        this.baselineStrategy = baselineStrategy;
        this.chaosStrategy = chaosStrategy;
    }

    @Override
    public ResilienceStrategy resolve(String profile) {

        if ("chaos".equals(profile)) {
            return chaosStrategy;
        }

        return baselineStrategy;
    }
}
