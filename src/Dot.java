import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;

/**
 * A dot representing a location in the Cartesian coordinate system, specified
 * in integer partition.
 */
public class Dot extends Point
{
   // Constants
   private static final long serialVersionUID = -1L;
   private static final int BORDER_THICKNESS = 2;
   
   // Instance variables
   private int radius;
   private Color color;
   
   /**
    * Constructs a dot with the specified radius and color at the specified
    * (<i>x</i>, <i>y</i>) location in the coordinate system.
    * 
    * @param x        the <i>x</i>-coordinate
    * @param y        the <i>y</i>-coordinate
    * @param radius   the radius
    * @param color    the color
    */
   public Dot(int x, int y, int radius, Color color)
   {
      super(x, y);
      this.radius = radius;
      this.color = color;
   }
   
   /**
    * Returns the color of this dot.
    * 
    * @return the color
    */
   public Color getColor() { return color; }
   
   /**
    * Paints with dot with the specified two-dimensional graphics.
    * 
    * @param g   the two-dimensional graphics
    */
   public void paint(Graphics2D g)
   {
      Ellipse2D.Double circle = new Ellipse2D.Double(x - radius, y - radius,
         radius * 2, radius * 2);
      g.setColor(color);
      g.fill(circle);
      g.setColor(Color.BLACK);
      g.setStroke(new BasicStroke(BORDER_THICKNESS));
      g.draw(circle);
   }
   
   /**
    * Sets the color of this dot to the specified color.
    * 
    * @param color   the new color
    */
   public void setColor(Color color) { this.color = color; }
}