package earalov.joblift.test.dao;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.validation.constraints.Null;
import java.util.Map;

/**
 * DAO that stores counters and full URLs.
 */
@ParametersAreNonnullByDefault
public interface IShortenerDao {

    /**
     * Save shortened URL.
     *
     * @param key shortened URL key.
     * @param entry full URL
     * @throws StorageException if impossible to save URL to DAO
     */
    void SaveURL(String key, UrlEntry entry) throws StorageException;


    /**
     * Get long URL.
     *
     * @param key shortened URL key.
     * @return  full URL or {@code null} if full URL wasn't present for given key.
     * @throws StorageException if impossible to save URL to DAO
     */
    @Nullable
    UrlEntry GetUrl(String key) throws StorageException;

    /**
     * Atomically increments counter and returns incremented counter
     *
     * @return incremented counter
     * @throws StorageException if impossible to save URL to DAO
     */
    long IncrementCounter() throws StorageException;

    /**
     * Increment top level domain statistics by 1
     *
     * @param topLevelDomainName top level domain name. For example, de.
     */
    void IncrementTld(String topLevelDomainName);

    /**
     * Get top level domain statistics.
     *
     * @return statistics in form of domain name - number of urls shortened.
     * -1 if impossible to read counter from storage
     */
    Map<String, Long> GetTldStats();
}
