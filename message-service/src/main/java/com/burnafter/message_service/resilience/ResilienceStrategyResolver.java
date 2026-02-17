package com.burnafter.message_service.resilience;

public interface ResilienceStrategyResolver {

    ResilienceStrategy resolve(String profile);

}
