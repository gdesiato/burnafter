package com.burnafter.burnafter.resilience;

public interface ResilienceStrategyResolver {

    ResilienceStrategy resolve(String profile);

}
