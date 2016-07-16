package earalov.joblift.test.resource;

import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

/**
 * Based on: https://gist.github.com/jdcrensh/4670128
 */
@Service
public class Base62Converter implements IConverter {

    public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static final int BASE = ALPHABET.length();

    @Nonnull
    @Override
    public String convert(long toBeConverted) {
        StringBuilder sb = new StringBuilder("");
        if (toBeConverted == 0) {
            return "a";
        }
        while (toBeConverted > 0) {
            toBeConverted = fromBase10(toBeConverted, sb);
        }
        return sb.reverse().toString();
    }

    private static long fromBase10(long i, final StringBuilder sb) {
        int rem = (int)(i % BASE);
        sb.append(ALPHABET.charAt(rem));
        return i / BASE;
    }
}
