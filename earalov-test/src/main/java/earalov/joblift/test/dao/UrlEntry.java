package earalov.joblift.test.dao;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * DTO to store in DAO.
 */
@ParametersAreNonnullByDefault
public class UrlEntry {
    private long counter; //for debugging only
    private String longURL;

    public UrlEntry(){
        //dummy constructor
    }

    public UrlEntry(long counter, String longURL) {
        this.counter = counter;
        this.longURL = longURL;
    }

    @Nonnull
    public String getLongUrl() {
        return longURL;
    }

    @Nonnull
    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    public void setLongUrl(String longURL) {
        this.longURL = longURL;
    }
}
