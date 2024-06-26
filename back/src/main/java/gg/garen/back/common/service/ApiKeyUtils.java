package gg.garen.back.common.service;

import gg.garen.back.common.domain.mysql.ApiKey;
import gg.garen.back.common.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ApiKeyUtils {

    private final ApiKeyRepository apiKeyRepository;

    HashMap<Long, ApiKey> apiKeys;
    Long index = 0L;

    public ApiKey getOneApiKey() {
        if (apiKeys == null) {
            apiKeys = new HashMap<>();

            List<ApiKey> findedApiKeys = apiKeyRepository.findAll();
            for (ApiKey apiKey : findedApiKeys) {
                apiKeys.put(apiKey.getId(), apiKey);
            }
        }

        index = index%5000+1;

        return apiKeys.get(index);
    }
}
