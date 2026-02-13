package com.burnafter.burnafter.service;

import com.burnafter.burnafter.model.ApiKey;
import com.burnafter.burnafter.repository.ApiKeyRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ApiKeyService {

    private static final String KEY_PREFIX = "ba_live_";
    private static final int PREFIX_LENGTH = 12;

    private final ApiKeyRepository repository;
    private final PasswordEncoder encoder;

    public ApiKeyService(ApiKeyRepository repository,
                         PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public String generateApiKey(int rateLimitPerHour,
                                 String resilienceProfile) {

        String randomPart = generateSecureRandom(32);
        String fullKey = KEY_PREFIX + randomPart;

        String prefix = fullKey.substring(0, PREFIX_LENGTH);

        String hash = encoder.encode(fullKey);

        ApiKey apiKey = new ApiKey(
                hash,
                prefix,
                rateLimitPerHour,
                resilienceProfile
        );

        repository.save(apiKey);

        return fullKey; // Return once only
    }

    private String generateSecureRandom(int length) {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, length);
    }

    public Optional<ApiKey> validate(String incomingKey) {

        if (incomingKey == null || incomingKey.length() < PREFIX_LENGTH) {
            return Optional.empty();
        }

        String prefix = incomingKey.substring(0, PREFIX_LENGTH);

        return repository.findByPrefixAndEnabledTrue(prefix)
                .filter(apiKey -> encoder.matches(incomingKey, apiKey.getKeyHash()));
    }
}

