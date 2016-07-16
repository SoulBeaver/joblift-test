package earalov.joblift.test.dao;

import com.couchbase.client.core.time.Delay;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Couchbase connection manager implementation holds single instance of the bucket.
 */
@Service
@ParametersAreNonnullByDefault
public final class CouchbaseConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CouchbaseConnectionManager.class);

    private Bucket bucket;
    private CouchbaseCluster cluster;

    private long operationTimeoutMs;
    private long reconnectIntervalMs;
    private String servers;
    private String bucketName;
    private String bucketPassword;

    @PostConstruct
    public void initialize() {

        final String serversString = servers.replaceAll("\\s+", "");
        final List<String> uriList = Arrays.asList(serversString.split(","));
        final CouchbaseEnvironment environment = DefaultCouchbaseEnvironment.builder()
                .kvTimeout(operationTimeoutMs)
                .reconnectDelay(Delay.fixed(reconnectIntervalMs, TimeUnit.MILLISECONDS))
                .build();
        cluster = CouchbaseCluster.create(environment, uriList);
        LOGGER.info("Connecting to database...");
        bucket = cluster.openBucket(bucketName,
                bucketPassword);
    }

    @PreDestroy
    public void stop() {
        if (bucket != null) {
            bucket.close();
            cluster.disconnect();
        }
    }

    @Nonnull
    public Bucket getBucket() {
        return bucket;
    }

    public void setOperationTimeoutMs(long operationTimeoutMs) {
        this.operationTimeoutMs = operationTimeoutMs;
    }

    public void setReconnectIntervalMs(long reconnectIntervalMs) {
        this.reconnectIntervalMs = reconnectIntervalMs;
    }

    public void setServers(String servers) {
        this.servers = servers;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setBucketPassword(String bucketPassword) {
        this.bucketPassword = bucketPassword;
    }
}
