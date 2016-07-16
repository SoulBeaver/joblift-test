package earalov.joblift.test.dao;

import com.couchbase.client.java.document.RawJsonDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by user on 7/16/2016.
 */
@Component
@ParametersAreNonnullByDefault
public class CouchbaseShortenerDao implements IShortenerDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CouchbaseShortenerDao.class);
    private static final String[] TOP_LEVEL_DOMAINS = new String[]{
            "com", "org", "ru", "de", "ua" //TODO(earalov): add more TLD
    };

    private final CouchbaseConnectionManager connectionManager;
    private String counterKey;

    @Inject
    public CouchbaseShortenerDao(CouchbaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void SaveURL(String key, UrlEntry entry) throws StorageException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(entry);
            connectionManager.getBucket().upsert(RawJsonDocument.create(key, json));
        } catch (final RuntimeException | IOException e) {
            LOGGER.error(String.format("Cannot upsert document by key=%s", key), e);
            throw new StorageException(e);
        }
    }

    @Override
    @Nullable
    public UrlEntry GetUrl(String key) throws StorageException {
        try {
            final RawJsonDocument document = connectionManager.getBucket().get(RawJsonDocument.create(key));
            if (document == null || document.content() == null) {
                LOGGER.error("Key={} not found", key);
                return null;
            }
            final String content = document.content();
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(content, UrlEntry.class);
        } catch (final RuntimeException | IOException e) {
            LOGGER.error(String.format("Cannot retrieve document by key=%s", key), e);
            throw new StorageException(e);
        }
    }

    @Override
    public long IncrementCounter() throws StorageException {
        try {
            return connectionManager.getBucket().counter(counterKey, 1, 1).content();
        } catch (final RuntimeException e) {
            LOGGER.error(String.format("Cannot increment counter for key=%s", counterKey), e);
            throw new StorageException(e);
        }
    }

    @Override
    public void IncrementTld(String topLevelDomainName) {
        LOGGER.debug("tld="+topLevelDomainName);
        if(!Arrays.asList(TOP_LEVEL_DOMAINS).contains(topLevelDomainName)){
            return;
        }
        try {
            connectionManager.getBucket().counter(GetTldStatsKey(topLevelDomainName), 1, 1).content();
        } catch (final RuntimeException e) {
            LOGGER.error(String.format("Cannot increment counter for tld=%s", topLevelDomainName), e);
        }
    }

    @Override
    public Map<String, Long> GetTldStats() {
        return Arrays.stream(TOP_LEVEL_DOMAINS).collect(Collectors.toMap(tld -> tld, tld -> {
            final String key = GetTldStatsKey(tld);
            try {
                return getCounterValue(key);
            } catch (StorageException e) {
                LOGGER.error(String.format("Exception happened on getting tld key=%s", key), e);
                return new Long(-1);
            }
        }));
    }

    public void setCounterKey(String counterKey) {
        this.counterKey = counterKey;
    }

    @Nonnull
    private String GetTldStatsKey(String topLevelDomainName) {
        return "tld-" + topLevelDomainName;
    }

    private long getCounterValue(final String key) throws StorageException {
        try {
            final RawJsonDocument document = connectionManager.getBucket().get(RawJsonDocument.create(key));
            if (document == null) {
                return 0L;
            } else {
                final String counterValue = document.content();
                return (counterValue == null) ? 0L : Long.parseLong(counterValue);
            }
        } catch (final RuntimeException e) {
            LOGGER.error(String.format("Cannot get counter for key=%s", key), e);
            throw new StorageException(e);
        }
    }
}
