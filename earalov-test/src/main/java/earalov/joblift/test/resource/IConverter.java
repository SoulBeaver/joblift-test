package earalov.joblift.test.resource;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNullableByDefault;

/**
 * Converts long to some hash value.
 */
@ParametersAreNullableByDefault
public interface IConverter {

    /**
     * Convert provided value to hash value.
     *
     * @param toBeConverted counter value.
     * @return hash string.
     */
    @Nonnull
    String convert(long toBeConverted);
}
