package co.com.bancolombia.security.apikey;

import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.security.server.apikey.ApiKeyEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryApiKeyRepository
        implements ApiKeyEntityRepository<InMemoryApiKeyEntity> {

    private static final Logger log = LoggerFactory.getLogger(InMemoryApiKeyRepository.class);

    private final ConcurrentHashMap<String, InMemoryApiKeyEntity> cache = new ConcurrentHashMap<>();

    public void put(InMemoryApiKeyEntity entity) {
        log.debug("🔐 Guardando API Key en cache: {}", entity.getId());
        cache.put(entity.getId(), entity);
    }

    public void clear() {
        cache.clear();
    }

    @Override
    public InMemoryApiKeyEntity findByKeyId(String keyId) {
        return cache.get(keyId);
    }
}
