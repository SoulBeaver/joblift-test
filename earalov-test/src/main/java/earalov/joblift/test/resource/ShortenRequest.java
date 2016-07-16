package earalov.joblift.test.resource;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents shouten request.
 */
@ParametersAreNonnullByDefault
public class ShortenRequest {

    private String url;
    private String preferredKey;

    public ShortenRequest(){
        //dummy constructor
    }

    /**
     * @return URL to shorten.
     */
    @Nullable
    public String getUrl() {
        return url;
    }

    /**
     * @param url URL to shorten.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return user's preferred shortened url. Fox example: myparrots for http://imgur.com/gallery/awtt8NH
     */
    @Nullable
    public String getPreferredKey() { //TODO(earalov): implement preferred key logic in resource
        return preferredKey;
    }

    /**
     * @param preferredKey user's preferred shortened url.
     */
    public void setPreferredKey(String preferredKey) {
        this.preferredKey = preferredKey;
    }
}
