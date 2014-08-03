package xmltransfo;

import java.math.BigDecimal;

/**
 * @author jbr
 */
public class RectBound {
  private final BigDecimal x1;
  private final BigDecimal x2;
  private final BigDecimal y1;
  private final BigDecimal y2;

  /**
   * @param x1
   * @param x2
   * @param y1
   * @param y2
   */
  public static RectBound newBoundUsingCoordinates(BigDecimal x1, BigDecimal x2, BigDecimal y1, BigDecimal y2) {
    return new RectBound(x1, x2, y1, y2);
  }

  public BigDecimal getX1() {
    return x1;
  }

  public BigDecimal getX2() {
    return x2;
  }

  public BigDecimal getWidth() {
    return x1.subtract(x2).abs();
  }

  public BigDecimal getY1() {
    return y1;
  }

  public BigDecimal getY2() {
    return y2;
  }

  public BigDecimal getHeight() {
    return y1.subtract(y2).abs();
  }

  @Override
  public String toString() {
    return "RectBound [x1=" + x1 + ", x2=" + x2 + ", y1=" + y1 + ", y2=" + y2 + "]";
  }

  private RectBound(BigDecimal x1, BigDecimal x2, BigDecimal y1, BigDecimal y2) {
    super();
    this.x1 = x1;
    this.x2 = x2;
    this.y1 = y1;
    this.y2 = y2;
  }

}
