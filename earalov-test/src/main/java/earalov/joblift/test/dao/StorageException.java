package earalov.joblift.test.dao;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Thrown if storage is unavailable
 */
@ParametersAreNonnullByDefault
public class StorageException extends Exception {

  /**
   * Constructs {@link StorageException}
   *
   * @param message description of error
   * @param cause   cause of the exception
   */
  public StorageException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs {@link StorageException}
   *
   * @param cause cause of the exception
   */
  public StorageException(final Throwable cause) {
    super(cause);
  }

  /**
   * Constructs {@link StorageException}
   *
   * @param message description of error
   */
  public StorageException(final String message) {
    super(message);
  }
}
