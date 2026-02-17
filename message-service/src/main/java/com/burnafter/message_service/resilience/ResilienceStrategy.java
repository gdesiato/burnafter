package com.burnafter.message_service.resilience;

import java.util.function.Supplier;

public interface ResilienceStrategy {

    <T> T execute(Supplier<T> supplier);

}

