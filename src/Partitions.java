import edu.ucla.math.Partition;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * An applet which animates partition bijections.
 * 
 * @version 1.1
 * 
 * @author Kris Torres
 */
public class Partitions extends JApplet
{
   // Applet constants
   private static final long serialVersionUID = 0L;
   private static final int APPLET_WIDTH = 900;
   private static final int APPLET_HEIGHT = 1000;
   private static final int BIJECTION_PANEL_WIDTH = 400;
   private static final int BIJECTION_PANEL_HEIGHT = 83;
   private static final int PANEL_BORDER_THICKNESS = 2;
   private static final int TITLE_FONT_SIZE = 15;
   private static final int SIZE_FIELD_WIDTH = 3;
   
   // Partition bijection constants
   private static final String STRIKE_SLIP = "Strike-slip";
   private static final String SHRED_STRETCH = "Shred-and-stretch";
   private static final String CUT_STRETCH = "Cut-and-stretch";
   private static final String GLAISHER = "Glaisher";
   
   // Panels
   private JPanel sizePanel;
   private JPanel bijectionPanel;
   private Board board;
   
   // Components
   private JTextField sizeField;
   private JRadioButton atLeastButton;
   private JRadioButton exactlyButton;
   private JComboBox<String> bijectionComboBox;
   private JLabel descriptionLabel;
   private JButton okayButton;
   
   // Other instance variables
   private Partition λ;
   private Dot[] ferrers;
   
   @Override
   public void init()
   {
      createSizePanel();
      createBijectionPanel();
      
      okayButton = new JButton("OK");
      okayButton.addActionListener(new BijectionAnimationListener());
      
      board = new Board();
      board.setBorder(BorderFactory.createLineBorder(Color.BLACK,
         PANEL_BORDER_THICKNESS));
      
      JPanel controlPanel = new JPanel(new FlowLayout());
      controlPanel.add(sizePanel);
      controlPanel.add(bijectionPanel);
      controlPanel.add(okayButton);
      
      setSize(APPLET_WIDTH, APPLET_HEIGHT);
      setLayout(new BorderLayout());
      add(controlPanel, BorderLayout.NORTH);
      add(board, BorderLayout.CENTER);
   }
   
   /** Creates a panel which controls the bijection of the random partition. */
   private void createBijectionPanel()
   {
      String[] bijectionType = { "Please select one…", STRIKE_SLIP,
         SHRED_STRETCH, CUT_STRETCH, GLAISHER };
      bijectionComboBox = new JComboBox<String>(bijectionType);
      bijectionComboBox.addActionListener(new BijectionChangeListener());
      
      descriptionLabel = new JLabel(" ");
      
      bijectionPanel = new JPanel(new BorderLayout());
      bijectionPanel.setPreferredSize(new Dimension(BIJECTION_PANEL_WIDTH,
         BIJECTION_PANEL_HEIGHT));
      bijectionPanel.setBorder(panelBorder("Bijection"));
      bijectionPanel.add(bijectionComboBox, BorderLayout.WEST);
      bijectionPanel.add(descriptionLabel, BorderLayout.SOUTH);
   }
   
   /** Creates a panel which controls the size of the random partition. */
   private void createSizePanel()
   {
      sizeField = new JTextField(SIZE_FIELD_WIDTH);
      sizeField.setHorizontalAlignment(JTextField.RIGHT);
      
      atLeastButton = new JRadioButton("At least");
      exactlyButton = new JRadioButton("Exactly");
      
      ButtonGroup sampleGroup = new ButtonGroup();
      sampleGroup.add(atLeastButton);
      sampleGroup.add(exactlyButton);
      
      JPanel samplePanel = new JPanel(new BorderLayout());
      samplePanel.add(atLeastButton, BorderLayout.NORTH);
      samplePanel.add(exactlyButton, BorderLayout.SOUTH);
      
      sizePanel = new JPanel(new FlowLayout());
      sizePanel.setBorder(panelBorder("Partition Size"));
      sizePanel.add(new JLabel("n ="));
      sizePanel.add(sizeField);
      sizePanel.add(samplePanel);
   }
   
   /**
    * Returns a panel border with the specified title.
    * 
    * @param title   the title
    * 
    * @return the panel border
    */
   private TitledBorder panelBorder(String title)
   {
      Border border = BorderFactory.createLineBorder(Color.BLACK,
         PANEL_BORDER_THICKNESS);
      Font font = new Font(Font.SANS_SERIF, Font.BOLD, TITLE_FONT_SIZE);
      
      return BorderFactory.createTitledBorder(border, title,
         TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, font);
   }
   
   /**
    * A listener that animates the bijection of the random partition on the
    * board.
    */
   private class BijectionAnimationListener implements ActionListener, Runnable
   {
      // Constants
      private static final int HOLD = 500;
      private static final int DELAY = 20;
      private static final int MIN_PARTITION_SIZE = 1;
      private static final int MAX_PARTITION_SIZE = 100;
      private static final int DOT_RADIUS = 5;
      
      // Colors
      private final Color UCLA_BLUE = new Color(50, 132, 191);
      private final Color UCLA_GOLD = new Color(255, 232, 0);
      private final Color PHILIPPINE_BLUE = new Color(0, 56, 168);
      private final Color PHILIPPINE_RED = new Color(206, 17, 38);
      private final Color PHILIPPINE_GOLD = new Color(252, 209, 22);
      
      // Instance variables
      private int rows;
      private int columns;
      
      @Override
      public void actionPerformed(ActionEvent e)
      {
         try
         {
            int n = Integer.parseInt(sizeField.getText());
            verifyPartitionSize(n);
            createRandomPartition(n);
            
            okayButton.setEnabled(false);
            
            rows = λ.numberOfParts();
            columns = λ.largestPart();
            ferrers = new Dot[rows * columns];
            
            Thread bijection = new Thread(this);
            bijection.start();
         }
         catch (NumberFormatException nfe)
         {
            String message = "Please enter a partition size between "
               + MIN_PARTITION_SIZE + "-" + MAX_PARTITION_SIZE + ".";
            
            // Tests if the size field is left empty.
            if (!sizeField.getText().isEmpty())
               message = "The size field text cannot be parsed as an integer.";
            
            JOptionPane.showMessageDialog(null, message, "",
               JOptionPane.PLAIN_MESSAGE);
         }
         catch (RuntimeException re)
         {
            String message = re.getMessage();
            
            // The user tries to generate an even partition with an odd weight.
            if (message.startsWith("Illegal"))
               message = "An even partition cannot have an odd weight.";
            
            JOptionPane.showMessageDialog(null, message, "",
               JOptionPane.PLAIN_MESSAGE);
         }
      }
      
      @Override
      public void run()
      {
         try
         {
            String bijectionType = (String) bijectionComboBox.getSelectedItem();
            createFerrersDiagram();
            
            if (bijectionType == STRIKE_SLIP)
               animateStrikeSlipBijection();
            else if (bijectionType == SHRED_STRETCH)
               animateShredStretchBijection();
            else if (bijectionType == CUT_STRETCH)
               animateCutStretchBijection();
            else if (bijectionType == GLAISHER)
               animateGlaisherBijection();
            
            okayButton.setEnabled(true);
         }
         catch (InterruptedException e) {}
      }
      
      /**
       * Appends the lower component to the right side of the upper component.
       * 
       * @param upper   the color of the upper component
       * @param lower   the color of the lower component
       */
      private void add(Color upper, Color lower) throws InterruptedException
      {
         Vector<Thread> v = new Vector<Thread>();
         Set<Integer> upperY = new TreeSet<Integer>();
         
         // Inserts the y-coordinates in the upper component to the set.
         for (Dot dot : ferrers)
         {
            if (dot != null && dot.getColor() == upper)
               upperY.add(dot.y / (DOT_RADIUS * 3));
         }
         
         Iterator<Integer> pos = upperY.iterator();
         
         while (pos.hasNext())
         {
            int y = pos.next();
            int dx = 0;
            
            // Determines the shift of the rows in the lower component.
            for (Dot dot : ferrers)
            {
               if (dot != null && dot.getColor() == upper
                  && y == dot.y / (DOT_RADIUS * 3)) { dx++; }
            }
            
            // Shifts the rows in the lower component to the right.
            for (Dot dot : ferrers)
            {
               if (dot != null && dot.getColor() == lower
                  && y == dot.y / (DOT_RADIUS * 3) - rows)
               {
                  v.add(new Thread(new AnimationRunnable(dot, dx, 0)));
               }
            }
         }
         
         for (Thread t : v) t.start();
         for (Thread t : v) t.join();
         move(lower, 0, -rows);
      }
      
      /** Animates the cut-and-stretch bijection. */
      private void animateCutStretchBijection() throws InterruptedException
      {
         class UpperComponentRunnable implements Runnable
         {
            @Override
            public void run()
            {
               try
               {
                  move(UCLA_BLUE, 1, 1);
                  shift(UCLA_BLUE, 1, -1, 0, 1);
                  move(UCLA_BLUE, 0, -1);
                  stretch(UCLA_BLUE, 2, 1);
               }
               catch (InterruptedException e) {}
            }
         }
         
         class LowerComponentRunnable implements Runnable
         {
            @Override
            public void run()
            {
               try
               {
                  move(UCLA_GOLD, 0, rows);
                  shift(UCLA_GOLD, 1, 0, -1, 1);
                  move(UCLA_GOLD, 0, -1);
                  transpose(UCLA_GOLD, rows);
                  stretch(UCLA_GOLD, 2, 1);
                  move(UCLA_GOLD, 1, -rows);
               }
               catch (InterruptedException e) {}
            }
         }
         
         cut(-1, 1, 1, UCLA_BLUE, UCLA_GOLD);
         
         Thread t1 = new Thread(new UpperComponentRunnable());
         Thread t2 = new Thread(new LowerComponentRunnable());
         t1.start();
         t2.start();
         t1.join();
         t2.join();
      }
      
      /** Animates Glaisher's bijection. */
      private void animateGlaisherBijection() throws InterruptedException
      {
         class UpperComponentRunnable implements Runnable
         {
            @Override
            public void run()
            {
               try
               {
                  class BlueComponentRunnable implements Runnable
                  {
                     @Override
                     public void run()
                     {
                        try { stretch(PHILIPPINE_BLUE, 0.5, 0.5); }
                        catch (InterruptedException e) {}
                     }
                  }
                  
                  class GoldComponentRunnable implements Runnable
                  {
                     @Override
                     public void run()
                     {
                        try { stretch(PHILIPPINE_GOLD, 0.5, 0.5); }
                        catch (InterruptedException e) {}
                     }
                  }
                  
                  move(PHILIPPINE_BLUE, 1, 1);
                  shift(PHILIPPINE_BLUE, 1, -2, 0, 1);
                  move(PHILIPPINE_BLUE, 0, -1);
                  shred(PHILIPPINE_BLUE, PHILIPPINE_BLUE, PHILIPPINE_GOLD);
                  move(PHILIPPINE_BLUE, 1, 0);
                  
                  Thread t1 = new Thread(new BlueComponentRunnable());
                  Thread t2 = new Thread(new GoldComponentRunnable());
                  t1.start();
                  t2.start();
                  t1.join();
                  t2.join();
                  
                  move(PHILIPPINE_GOLD, 0, 1);
                  fill(PHILIPPINE_GOLD, PHILIPPINE_BLUE);
               }
               catch (InterruptedException e) {}
            }
         }
         
         class LowerComponentRunnable implements Runnable
         {
            @Override
            public void run()
            {
               try
               {
                  class RedComponentRunnable implements Runnable
                  {
                     @Override
                     public void run()
                     {
                        try
                        {
                           stretch(PHILIPPINE_RED, 0.5, 1);
                           shift(PHILIPPINE_RED, 1, 0, -1, 1);
                           transpose(PHILIPPINE_RED, rows);
                           stretch(PHILIPPINE_RED, 1, 0.5);
                           move(PHILIPPINE_RED, 0, -rows);
                        }
                        catch (InterruptedException e) {}
                     }
                  }
                  
                  class WhiteComponentRunnable implements Runnable
                  {
                     @Override
                     public void run()
                     {
                        try
                        {
                           stretch(Color.WHITE, 0.5, 1);
                           shift(Color.WHITE, 1, 0, -1, 1);
                           transpose(Color.WHITE, rows + 1);
                           stretch(Color.WHITE, 1, 0.5);
                           move(Color.WHITE, 0, -rows - 1);
                        }
                        catch (InterruptedException e) {}
                     }
                  }
                  
                  move(PHILIPPINE_RED, 0, rows);
                  shred(PHILIPPINE_RED, PHILIPPINE_RED, Color.WHITE);
                  move(PHILIPPINE_RED, 1, 0);
                  
                  Thread t1 = new Thread(new RedComponentRunnable());
                  Thread t2 = new Thread(new WhiteComponentRunnable());
                  t1.start();
                  t2.start();
                  t1.join();
                  t2.join();
                  
                  fill(Color.WHITE, PHILIPPINE_RED);
               }
               catch (InterruptedException e) {}
            }
         }
         
         cut(-1, 2, 0, PHILIPPINE_BLUE, PHILIPPINE_RED);
         
         Thread t1 = new Thread(new UpperComponentRunnable());
         Thread t2 = new Thread(new LowerComponentRunnable());
         t1.start();
         t2.start();
         t1.join();
         t2.join();
         
         add(PHILIPPINE_BLUE, PHILIPPINE_RED);
      }
      
      /** Animates the shred-and-stretch bijection. */
      private void animateShredStretchBijection() throws InterruptedException
      {
         class UpperComponentRunnable implements Runnable
         {
            @Override
            public void run()
            {
               try { stretch(UCLA_BLUE, 0.5, 0.5); }
               catch (InterruptedException e) {}
            }
         }
         
         class LowerComponentRunnable implements Runnable
         {
            @Override
            public void run()
            {
               try { stretch(UCLA_GOLD, 0.5, 0.5); }
               catch (InterruptedException e) {}
            }
         }
         
         shred(Color.BLACK, UCLA_BLUE, UCLA_GOLD);
         move(UCLA_BLUE, 1, 0);
         
         Thread t1 = new Thread(new UpperComponentRunnable());
         Thread t2 = new Thread(new LowerComponentRunnable());
         t1.start();
         t2.start();
         t1.join();
         t2.join();
         
         transpose();
         move(UCLA_GOLD, 1, 0);
      }
      
      /** Animates the strike-slip bijection. */
      private void animateStrikeSlipBijection() throws InterruptedException
      {
         class UpperComponentRunnable implements Runnable
         {
            @Override
            public void run()
            {
               try { move(UCLA_BLUE, -1, 0); }
               catch (InterruptedException e) {}
            }
         }
         
         class LowerComponentRunnable implements Runnable
         {
            @Override
            public void run()
            {
               try { move(UCLA_GOLD, 0, 1); }
               catch (InterruptedException e) {}
            }
         }
         
         cut(-1, 1, 0, UCLA_BLUE, UCLA_GOLD);
         
         Thread t1 = new Thread(new UpperComponentRunnable());
         Thread t2 = new Thread(new LowerComponentRunnable());
         t1.start();
         t2.start();
         t1.join();
         t2.join();
      }
      
      /** Creates the Ferrers diagram. */
      private void createFerrersDiagram() throws InterruptedException
      {
         int dotDiameter = DOT_RADIUS * 2;
         int latticeUnit = DOT_RADIUS + dotDiameter;
         
         for (int i = 0; i < rows; i++)
         {
            for (int j = 0; j < λ.part(i); j++)
            {
               int x = j * latticeUnit + dotDiameter;
               int y = i * latticeUnit + dotDiameter;
               ferrers[i * columns + j] = new Dot(x, y, DOT_RADIUS,
                  Color.BLACK);
            }
         }
         
         pause(HOLD);
      }
      
      /**
       * Generates a random partition of the specified expected partition size.
       * 
       * @param n   the expected partition size
       * 
       * @throws RuntimeException if no sampling method is selected, or if no
       *         bijection is selected
       * @throws IllegalArgumentException if <i>n</i> is odd and the
       *         shred-and-stretch bijection is selected
       */
      private void createRandomPartition(int n)
      {
         String bijectionType = (String) bijectionComboBox.getSelectedItem();
         
         if (exactlyButton.isSelected())
         {
            if (bijectionType == STRIKE_SLIP)
               λ = Partition.randomExactly(n);
            else if (bijectionType == SHRED_STRETCH)
               λ = Partition.evenRandomExactly(n);
            else if (bijectionType == CUT_STRETCH)
               createRandomSelfConjugatePartition(n, true);
            else if (bijectionType == GLAISHER)
               λ = Partition.oddRandomExactly(n);
            else
               throw new RuntimeException("Please select a bijection.");
         }
         else if (atLeastButton.isSelected())
         {
            if (bijectionType == STRIKE_SLIP)
               λ = Partition.random(n);
            else if (bijectionType == SHRED_STRETCH)
               λ = Partition.evenRandom(n);
            else if (bijectionType == CUT_STRETCH)
               createRandomSelfConjugatePartition(n, false);
            else if (bijectionType == GLAISHER)
               λ = Partition.oddRandom(n);
            else
               throw new RuntimeException("Please select a bijection.");
         }
         else
         {
            throw new RuntimeException("Please select a sampling method.");
         }
      }
      
      /**
       * Generates a random self-conjugate partition of the specified expected
       * partition size by the specified sampling method.
       * 
       * @param n       the expected partition size
       * @param exact   the sampling method (<code>true</code> for the partition
       *                size to be exactly <i>n</i>, or <code>false</code> for
       *                the partition size to be at least <i>n</i>)
       */
      private void createRandomSelfConjugatePartition(int n, boolean exact)
      {
         // Generates a random odd partition with distinct parts.
         if (exact) λ = Partition.distinctOddRandomExactly(n);
         else λ = Partition.distinctOddRandom(n);
         
         Integer[] parts = λ.toArray();
         Partition λ1 = new Partition();
         Partition λ2 = new Partition();
         
         // Splits the partition into roughly two halves.
         for (int part : parts) λ1.insert(part / 2 + 1);
         for (int part : parts) if (part > 1) λ2.insert(part / 2);
         
         // Creates the self-conjugate partition.
         for (int i = 0; i < λ2.numberOfParts(); i++)
         {
            for (int j = 0; j < λ2.part(i); j++)
            {
               if (j < λ1.numberOfParts() - i - 1)
               {
                  int part = λ1.part(i + j + 1);
                  λ1.erase(part);
                  λ1.insert(part + 1);
               }
               else
               {
                  λ1.insert(1);
               }
            }
         }
         
         λ = λ1;
      }
      
      /**
       * Projects the dots (<i>i</i>, <i>j</i>) onto the upper component if
       * <i>ai</i> + <i>bj</i> < <i>c</i>, and onto the lower component
       * otherwise, where <i>a</i>, <i>b</i>, <i>c</i> ∈ ℤ.
       * 
       * @param a       the rise of the slope of the diagonal cut
       * @param b       the run of the slope of the diagonal cut
       * @param c       the downward shift in the diagonal cut
       * @param upper   the color of the upper part
       * @param lower   the color of the lower part
       */
      private void cut(int a, int b, int c, Color upper, Color lower)
         throws InterruptedException
      {
         for (Dot dot : ferrers)
         {
            if (dot != null)
            {
               int x = dot.x / (DOT_RADIUS * 3);
               int y = dot.y / (DOT_RADIUS * 3);
               if (a * x + b * y < c) dot.setColor(upper);
               else dot.setColor(lower);
            }
         }
         
         pause(HOLD);
      }
      
      /**
       * Changes the color of the specified dots.
       * 
       * @param from   the color of the dots to change
       * @param to     the new color of the dots
       */
      private void fill(Color from, Color to) throws InterruptedException
      {
         for (Dot dot : ferrers)
            if (dot != null && dot.getColor() == from) dot.setColor(to);
         
         pause(HOLD);
      }
      
      /**
       * Translates the dots (<i>x</i>, <i>y</i>) of the specified color by the
       * vector (<i>dx</i>, <i>dy</i>), where <i>dx</i>, <i>dy</i> ∈ ℤ.
       * 
       * @param color   the color of the dots to translate
       * @param dx      the change in the dot's horizontal position
       * @param dy      the change in the dot's vertical position
       */
      private void move(Color color, int dx, int dy) throws InterruptedException
      {
         Vector<Thread> v = new Vector<Thread>();
         
         for (Dot dot : ferrers)
         {
            if (dot != null && dot.getColor() == color)
               v.add(new Thread(new AnimationRunnable(dot, dx, dy)));
         }
         
         for (Thread t : v) t.start();
         for (Thread t : v) t.join();
      }
      
      /**
       * Pauses the animation for the specified number of milliseconds.
       * 
       * @param duration   the number of milliseconds to pause
       */
      private void pause(int duration) throws InterruptedException
      {
         board.repaint();
         Thread.sleep(duration);
      }
      
      /**
       * Translates the dots (<i>x</i>, <i>y</i>) of the specified color to
       * <i>ax</i> + <i>by</i>, <i>cx</i> + <i>dy</i>), where
       * <i>a</i>, <i>b</i>, <i>c</i>, <i>d</i> ∈ ℤ.
       * 
       * @param color   the color of the dots to translate
       * @param a       the first coefficient
       * @param b       the second coefficient
       * @param c       the third coefficient
       * @param d       the fourth coefficient
       */
      private void shift(Color color, int a, int b, int c, int d)
         throws InterruptedException
      {
         Vector<Thread> v = new Vector<Thread>();
         
         for (Dot dot : ferrers)
         {
            if (dot != null && dot.getColor() == color)
            {
               int xi = dot.x / (DOT_RADIUS * 3);
               int yi = dot.y / (DOT_RADIUS * 3);
               int xf = a * xi + b * yi;
               int yf = c * xi + d * yi;
               v.add(new Thread(new AnimationRunnable(dot, xf - xi, yf - yi)));
            }
         }
         
         for (Thread t : v) t.start();
         for (Thread t : v) t.join();
      }
      
      /**
       * Projects the dots (<i>i</i>, <i>j</i>) onto the component corresponding
       * to the coset Γ<sub><i>k</i></sub>, where
       * <i>k</i> = <i>i</i> <code>%</code> <code>strips.length</code>.
       * 
       * @param color    the color of the component to shred
       * @param strips   the colors of the vertical strips
       */
      private void shred(Color color, Color... strips)
         throws InterruptedException
      {
         for (Dot dot : ferrers)
         {
            if (dot != null && dot.getColor() == color)
            {
               int x = dot.x / (DOT_RADIUS * 3);
               dot.setColor(strips[x % strips.length]);
            }
         }
         
         pause(HOLD);
      }
      
      /**
       * Translates the dots (<i>x</i>, <i>y</i>) of the specified color to
       * (<i>ki</i>, <sup><i>j</i></sup>/<sub>ℓ</sub>), where
       * <i>k</i>, <i>ℓ</i> ∈ ℤ.
       * 
       * @param color   the color of the dots to translate
       * @param k       the stretch factor in the dot's horizontal position
       * @param l       the compression factor in the dot's vertical position
       */
      private void stretch(Color color, double k, double l)
         throws InterruptedException
      {
         Vector<Thread> v = new Vector<Thread>();
         
         for (Dot dot : ferrers)
         {
            if (dot != null && dot.getColor() == color)
            {
               int x = dot.x / (DOT_RADIUS * 3);
               int y = dot.y / (DOT_RADIUS * 3);
               int dx = (int) (x * k) - x;
               int dy = (int) (y / l) - y;
               v.add(new Thread(new AnimationRunnable(dot, dx, dy)));
            }
         }
         
         for (Thread t : v) t.start();
         for (Thread t : v) t.join();
      }
      
      /** Transposes the Ferrers diagram. */
      private void transpose() throws InterruptedException
      {
         Vector<Thread> v = new Vector<Thread>();
         
         for (Dot dot : ferrers)
         {
            if (dot != null)
            {
               int dx = (dot.y - dot.x) / (DOT_RADIUS * 3);
               int dy = (dot.x - dot.y) / (DOT_RADIUS * 3);
               v.add(new Thread(new AnimationRunnable(dot, dx, dy)));
            }
         }
         
         for (Thread t : v) t.start();
         for (Thread t : v) t.join();
      }
      
      /**
       * Translates the dots (<i>x</i>, <i>y</i>) of the specified color to
       * (<i>y</i> - <i>k</i>, <i>y</i> + <i>k</i>), where
       * <i>dx</i>, <i>dy</i> ∈ ℤ.
       * 
       * @param color   the color of the dots to translate
       * @param k       the downward shift in the transposition
       */
      private void transpose(Color color, int k) throws InterruptedException
      {
         Vector<Thread> v = new Vector<Thread>();
         
         for (Dot dot : ferrers)
         {
            if (dot != null && dot.getColor() == color)
            {
               int dx = (dot.y - dot.x) / (DOT_RADIUS * 3) - k;
               int dy = (dot.x - dot.y) / (DOT_RADIUS * 3) + k;
               v.add(new Thread(new AnimationRunnable(dot, dx, dy)));
            }
         }
         
         for (Thread t : v) t.start();
         for (Thread t : v) t.join();
      }
      
      /**
       * Tests if the specified size of the random partition is valid.
       * 
       * @param n   the partition size
       * 
       * @throws IllegalArgumentException if either
       *         <i>n</i> < <code>MIN_PARTITION_SIZE</code> or
       *         <i>n</i> > <code>MAX_PARTITION_SIZE</code>
       */
      private void verifyPartitionSize(int n)
      {
         if (n < MIN_PARTITION_SIZE)
         {
            String message = "The partition size is less than the minimum: "
               + MIN_PARTITION_SIZE + ".";
            throw new IllegalArgumentException(message);
         }
         else if (n > MAX_PARTITION_SIZE)
         {
            String message = "The partition size is greater than the maximum: "
               + MAX_PARTITION_SIZE + ".";
            throw new IllegalArgumentException(message);
         }
      }
      
      /** A runnable with animation. */
      private class AnimationRunnable implements Runnable
      {
         // Instance variables
         private Dot dot;
         private int dx;
         private int dy;
         
         /**
          * Constructs a runnable with the specified dot and changes in the
          * dot's <i>x</i>-coordinate and <i>y</i>-coordinate.
          * 
          * @param dot   the dot
          * @param dx    the change in the dot's <i>x</i>-coordinate
          * @param dy    the change in the dot's <i>y</i>-coordinate
          */
         public AnimationRunnable(Dot dot, int dx, int dy)
         {
            this.dot = dot;
            this.dx = dx;
            this.dy = dy;
         }
         
         @Override
         public void run()
         {
            try
            {
               int x = dot.x + 3 * DOT_RADIUS * dx;
               int y = dot.y + 3 * DOT_RADIUS * dy;
               
               // Moves the dot.
               if (dx >= 0)
               {
                  if (dy >= 0) while (dot.x < x || dot.y < y) animate();
                  else while (dot.x < x || dot.y > y) animate();
               }
               else
               {
                  if (dy >= 0) while (dot.x > x || dot.y < y) animate();
                  else while (dot.x > x || dot.y > y) animate();
               }
            }
            catch (InterruptedException e) {}
         }
         
         /** Animates this runnable. */
         private void animate() throws InterruptedException
         {
            dot.translate(dx, dy);
            pause(DELAY);
         }
      }
   }
   
   /** A listener that changes the description of the bijection. */
   private class BijectionChangeListener implements ActionListener
   {
      @Override
      public void actionPerformed(ActionEvent e)
      {
         String bijectionType = (String) bijectionComboBox.getSelectedItem();
         
         if (bijectionType == STRIKE_SLIP)
         {
            descriptionLabel.setText(" Works on most partitions.");
         }
         else if (bijectionType == SHRED_STRETCH)
         {
            descriptionLabel.setText(" Even partition ↦ Even partition");
         }
         else if (bijectionType == CUT_STRETCH)
         {
            String domain = "Self-conjugate partition";
            String range = "Partition with distinct odd parts";
            descriptionLabel.setText(" " + domain + " ↦ " + range);
         }
         else if (bijectionType == GLAISHER)
         {
            String domain = "Odd partition";
            String range = "Partition with distinct parts";
            descriptionLabel.setText(" " + domain + " ↦ " + range);
         }
         else
         {
            descriptionLabel.setText(" ");
         }
      }
   }
   
   /** A component to animate the partition bijection on. */
   private class Board extends JComponent
   {
      private static final long serialVersionUID = 1L;
      
      @Override
      public void paintComponent(Graphics g)
      {
         // Paints all of the dots in the Ferrers diagram on this board.
         if (ferrers != null)
            for (Dot dot : ferrers) if (dot != null) dot.paint((Graphics2D) g);
      }
   }
}