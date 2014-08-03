package xmltransfo;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author jbr
 */
public class MainTest {

  @Test
  public void testToRectBound() {
    RectBound rectBound;
    rectBound = Main.toRectBound("m 80.7407,1352.8296 -75,0 0,33.6475 75,0 0,-33.6475 z");
    Assert.assertTrue("x1", new BigDecimal("5.7407").compareTo(rectBound.getX1()) == 0);
    Assert.assertTrue("x2", new BigDecimal("80.7407").compareTo(rectBound.getX2()) == 0);
    Assert.assertTrue("width", new BigDecimal("75").compareTo(rectBound.getWidth()) == 0);
    Assert.assertTrue("y1", new BigDecimal("1352.8296").compareTo(rectBound.getY1()) == 0);
    Assert.assertTrue("y2", new BigDecimal("1386.4771").compareTo(rectBound.getY2()) == 0);
    Assert.assertTrue("height", new BigDecimal("33.6475").compareTo(rectBound.getHeight()) == 0);

    rectBound = Main.toRectBound("m 5.6099,1352.8296 -75,0 0,33.6475 75,0 0,-33.6475 z");
    Assert.assertTrue("x1", new BigDecimal("-69.3901").compareTo(rectBound.getX1()) == 0);
    Assert.assertTrue("x2", new BigDecimal("5.6099").compareTo(rectBound.getX2()) == 0);
    Assert.assertTrue("width", new BigDecimal("75").compareTo(rectBound.getWidth()) == 0);
    Assert.assertTrue("y1", new BigDecimal("1352.8296").compareTo(rectBound.getY1()) == 0);
    Assert.assertTrue("y2", new BigDecimal("1386.4771").compareTo(rectBound.getY2()) == 0);
    Assert.assertTrue("height", new BigDecimal("33.6475").compareTo(rectBound.getHeight()) == 0);

    rectBound = Main.toRectBound("m 680.5693,-354 -75,0 0,33.6475 75,0 0,-33.6475");
    Assert.assertTrue("x1", new BigDecimal("605.5693").compareTo(rectBound.getX1()) == 0);
    Assert.assertTrue("x2", new BigDecimal("680.5693").compareTo(rectBound.getX2()) == 0);
    Assert.assertTrue("width", new BigDecimal("75").compareTo(rectBound.getWidth()) == 0);
    Assert.assertTrue("y1", new BigDecimal("-354").compareTo(rectBound.getY1()) == 0);
    Assert.assertTrue("y2", new BigDecimal("-320.3525").compareTo(rectBound.getY2()) == 0);
    Assert.assertTrue("height", new BigDecimal("33.6475").compareTo(rectBound.getHeight()) == 0);

    rectBound = Main.toRectBound("m 50,50 c 0,0 10,20 50,50 l 12,40 43,10 z");
    Assert.assertTrue("x1", new BigDecimal("50").compareTo(rectBound.getX1()) == 0);
    Assert.assertTrue("x2", new BigDecimal("155").compareTo(rectBound.getX2()) == 0);
    Assert.assertTrue("y1", new BigDecimal("50").compareTo(rectBound.getY1()) == 0);
    Assert.assertTrue("y2", new BigDecimal("150").compareTo(rectBound.getY2()) == 0);

    rectBound = Main.toRectBound("m 50,50 c 0,0 10,20 50,50 20,10 70,80 200,150 z");
    Assert.assertTrue("x1", new BigDecimal("50").compareTo(rectBound.getX1()) == 0);
    Assert.assertTrue("x2", new BigDecimal("300").compareTo(rectBound.getX2()) == 0);
    Assert.assertTrue("y1", new BigDecimal("50").compareTo(rectBound.getY1()) == 0);
    Assert.assertTrue("y2", new BigDecimal("250").compareTo(rectBound.getY2()) == 0);
  }

  @Test
  public void testContainsPoint() throws Exception {
    RectBound rectBound;
    rectBound = RectBound.newBoundUsingCoordinates(new BigDecimal(10), new BigDecimal(20), new BigDecimal(100), new BigDecimal(200));
    Assert.assertTrue("{15, 105}", Main.containsPoint(rectBound, new BigDecimal(15), new BigDecimal(105)));
    Assert.assertTrue("{10, 100}", Main.containsPoint(rectBound, new BigDecimal(10), new BigDecimal(100)));
    Assert.assertTrue("{10, 200}", Main.containsPoint(rectBound, new BigDecimal(10), new BigDecimal(200)));
    Assert.assertTrue("{20, 100}", Main.containsPoint(rectBound, new BigDecimal(20), new BigDecimal(100)));
    Assert.assertTrue("{20, 200}", Main.containsPoint(rectBound, new BigDecimal(20), new BigDecimal(200)));

    Assert.assertFalse("{5, 105}", Main.containsPoint(rectBound, new BigDecimal(5), new BigDecimal(105)));
    Assert.assertFalse("{25, 105}", Main.containsPoint(rectBound, new BigDecimal(25), new BigDecimal(105)));
    Assert.assertFalse("{15, 95}", Main.containsPoint(rectBound, new BigDecimal(15), new BigDecimal(95)));
    Assert.assertFalse("{15, 295}", Main.containsPoint(rectBound, new BigDecimal(15), new BigDecimal(295)));
  }

  @Test
  public void testContainsRect() throws Exception {
    RectBound root = RectBound.newBoundUsingCoordinates(new BigDecimal(10), new BigDecimal(20), new BigDecimal(100), new BigDecimal(200));
    Assert.assertTrue(Main.containsRect(root, root));

    Assert.assertTrue(Main.containsRect(root, RectBound.newBoundUsingCoordinates(new BigDecimal(12), new BigDecimal(18), new BigDecimal(120), new BigDecimal(180))));
    Assert.assertFalse(Main.containsRect(root, RectBound.newBoundUsingCoordinates(new BigDecimal(2), new BigDecimal(18), new BigDecimal(120), new BigDecimal(180))));
    Assert.assertFalse(Main.containsRect(root, RectBound.newBoundUsingCoordinates(new BigDecimal(12), new BigDecimal(22), new BigDecimal(120), new BigDecimal(180))));
    Assert.assertFalse(Main.containsRect(root, RectBound.newBoundUsingCoordinates(new BigDecimal(12), new BigDecimal(18), new BigDecimal(80), new BigDecimal(180))));
    Assert.assertFalse(Main.containsRect(root, RectBound.newBoundUsingCoordinates(new BigDecimal(12), new BigDecimal(18), new BigDecimal(120), new BigDecimal(280))));

    Assert.assertFalse(Main.containsRect(root, RectBound.newBoundUsingCoordinates(new BigDecimal(2), new BigDecimal(8), new BigDecimal(120), new BigDecimal(180))));
    Assert.assertFalse(Main.containsRect(root, RectBound.newBoundUsingCoordinates(new BigDecimal(12), new BigDecimal(18), new BigDecimal(10), new BigDecimal(20))));
  }

  @Test
  public void testAccess() throws Exception {
    // isPathInRect
    RectBound rect = RectBound.newBoundUsingCoordinates(new BigDecimal("-69.3901"), new BigDecimal("5.6099"), new BigDecimal("1386.4829"), new BigDecimal("1452.4829"));
    RectBound r;
    r = Main.toRectBound("m -10.832,1425.1289 c -1.9571,-2.7207 -1.9551,-2.7559 0.455,-4.9844 -2.4414,2.1836 -2.4736,2.1836 -4.4746,-0.5 1.96,2.7207 1.9571,2.7539 -0.4511,4.9825 2.4414,-2.1797 2.4726,-2.1797 4.4707,0.5019 z");
    Assert.assertTrue(Main.containsRect(rect, r));
    r = Main.toRectBound("m -8.8857,1425.498 -37.8565,0 c -1.0283,3.584 -3.5693,5.0215 -6.5449,5.0215 -3.8858,0 -7.0391,-3.2168 -7.0391,-8.5468 0,-5.3262 3.1533,-8.543 7.0391,-8.543 2.9785,0 5.5215,1.4433 6.5478,5.0312 5.7061,0 19.8252,0 20.5616,0 0.9248,0 1.1103,-1.1093 1.1103,-1.1093 l 0,-5.3614 c 0,-1.0234 1.4072,-1.8496 3.1436,-1.8496 1.7373,0 3.1445,0.8262 3.1445,1.8496 l 0.0928,5.9161 c 0,0.6132 0.2519,1.1113 1.2939,1.1113 1.042,0 1.2022,-0.4981 1.2022,-1.1113 l 0.0927,-5.9161 c 0,-1.0234 1.4073,-1.8496 3.1436,-1.8496 1.7373,0 3.1445,0.8262 3.1445,1.8496 0,0 0,3.9746 0,5.3614 0,1.3886 0.9239,1.0976 0.9239,1.0976 1.9462,0 2.6826,1.5801 2.6826,3.5254 0,1.9473 -0.7364,3.5234 -2.6826,3.5234 m -44.4014,-8.2402 c -1.7178,0 -3.1104,2.1152 -3.1104,4.7266 0,2.6113 1.3926,4.7285 3.1104,4.7285 1.7158,0 3.1094,-2.1172 3.1094,-4.7285 0,-2.6114 -1.3936,-4.7266 -3.1094,-4.7266 z");
    Assert.assertTrue(Main.containsRect(rect, r));
  }

  @Test
  public void textCalcTx() throws Exception {
    Assert.assertEquals("86", Main.calcTx(new BigDecimal(-69.3901))); //or 87
    Assert.assertEquals("-7", Main.calcTx(new BigDecimal(5.7407)));
    Assert.assertEquals("-100", Main.calcTx(new BigDecimal(80.8711)));
  }

  @Test
  public void textCalcTy() throws Exception {
    Assert.assertEquals("1816", Main.calcTy(new BigDecimal(1386.4829))); //or 1817
    Assert.assertEquals("1684", Main.calcTy(new BigDecimal(1280.4829)));
  }

}
