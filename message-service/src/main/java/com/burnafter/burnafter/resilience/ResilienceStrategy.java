package com.burnafter.burnafter.resilience;

import java.util.function.Supplier;

public interface ResilienceStrategy {

    <T> T execute(Supplier<T> supplier);

}

