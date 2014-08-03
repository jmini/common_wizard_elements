package xmltransfo;

import java.io.Serializable;

/**
 * @author jbr
 */
public class NotFoundException extends Exception implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * @param string
   */
  public NotFoundException(String string) {
    super(string);
  }
}
