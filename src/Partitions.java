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
      private static final int MAX_DOT_RADIUS = 20;
      private final int[] SENTINELS = { 14, 15, 16, 17, 18, 19, 21, 22, 24, 27,
         29, 33, 37, 42, 49, 59, 74 };
      
      // Colors
      private final Color UCLA_BLUE = new Color(50, 132, 191);
      private final Color UCLA_GOLD = new Color(255, 232, 0);
      private final Color TRANSLUCENT_UCLA_BLUE = new Color(50, 132, 191, 128);
      private final Color TRANSLUCENT_UCLA_GOLD = new Color(255, 232, 0, 128);
      private final Color TRANSLUCENT_WHITE = new Color(255, 255, 255, 128);
      
      // Instance variables
      private int rows;
      private int columns;
      private int dotRadius;
      
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
            dotRadius = MAX_DOT_RADIUS;
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
               JOptionPane.ERROR_MESSAGE);
         }
         catch (RuntimeException re)
         {
            String message = re.getMessage();
            
            // The user tries to generate an even partition with an odd weight.
            if (message.startsWith("Illegal"))
               message = "An even partition cannot have an odd weight.";
            
            JOptionPane.showMessageDialog(null, message, "",
               JOptionPane.ERROR_MESSAGE);
         }
      }
      
      @Override
      public void run()
      {
         try
         {
            String bijectionType = (String) bijectionComboBox.getSelectedItem();
            
            // Animates the bijection.
            if (bijectionType == STRIKE_SLIP)
            {
               createFerrersDiagram(rows + 1, columns);
               cut(1, 0, UCLA_BLUE, UCLA_GOLD);
               animateStrikeSlipBijection();
            }
            else if (bijectionType == SHRED_STRETCH)
            {
               int side = Math.max(rows * 2, columns / 2);
               createFerrersDiagram(side, side);
               shred();
               animateShredStretchBijection();
            }
            else if (bijectionType == CUT_STRETCH)
            {
               createFerrersDiagram(rows, columns * 2 - 1);
               cut(1, 1, TRANSLUCENT_UCLA_BLUE, TRANSLUCENT_UCLA_GOLD);
               animateCutStretchBijection();
            }
            else if (bijectionType == GLAISHER)
            {
               int height = rows;
               int width = Math.max(columns, (columns - 1) / 2 + rows);
               createFerrersDiagram(height, width);
               cut(2, 0, TRANSLUCENT_UCLA_BLUE, TRANSLUCENT_UCLA_GOLD);
               animateGlaisherBijection();
            }
            
            okayButton.setEnabled(true);
         }
         catch (InterruptedException e) {}
      }
      
      /**
       * Animates the cut-stretch bijection.
       * 
       * @throws InterruptedException if the bijection is interrupted
       */
      private void animateCutStretchBijection() throws InterruptedException
      {
         normalizeUpperPartLeft(1, 1);
         normalizeLowerPartUp();
         transposeLowerPart(1, 1);
         stretchBothPartsRight();
      }
      
      /**
       * Animates the Glaisher bijection.
       * 
       * @throws InterruptedException if the bijection is interrupted
       */
      private void animateGlaisherBijection() throws InterruptedException
      {
         // Upper part
         normalizeUpperPartLeft(2, 0);
         colorUpperOddVerticalStrips(TRANSLUCENT_WHITE);
         compressAllUpperVerticalStripsLeft();
         stretchAllUpperVerticalStripsDown();
         colorUpperOddVerticalStrips(TRANSLUCENT_UCLA_BLUE);
         
         // Lower part
         colorLowerOddVerticalStrips(TRANSLUCENT_WHITE);
         compressAllLowerVerticalStripsDown();
         normalizeAllLowerVerticalStrips();
         transposeLowerPart(2, 0);
         stretchAllLowerVerticalStripsDown();
         colorLowerOddVerticalStrips(TRANSLUCENT_UCLA_GOLD);
         
         appendBothParts();
      }
      
      /**
       * Animates the shred-stretch bijection.
       * 
       * @throws InterruptedException if the bijection is interrupted
       */
      private void animateShredStretchBijection() throws InterruptedException
      {
         compressAllVerticalStripsLeft();
         stretchAllVerticalStripsDown();
         transpose();
      }
      
      /**
       * Animates the strike-slip bijection.
       * 
       * @throws InterruptedException if the bijection is interrupted
       */
      private void animateStrikeSlipBijection() throws InterruptedException
      {
         Vector<Thread> v = new Vector<Thread>();
         
         for (Dot dot : ferrers)
         {
            // Upper part
            if (dot != null && dot.getColor() == UCLA_BLUE)
               v.add(new Thread(new AnimationRunnable(dot, -1, 0)));
            
            // Lower part
            if (dot != null && dot.getColor() == UCLA_GOLD)
               v.add(new Thread(new AnimationRunnable(dot, 0, 1)));
         }
         
         // Starts the animation.
         for (Thread t : v) t.start();
         for (Thread t : v) t.join();
      }
      
      /**
       * Appends both parts of the Ferrers diagram to each other side by side.
       * 
       * @throws InterruptedException if the animation is interrupted
       */
      private void appendBothParts() throws InterruptedException
      {
         Vector<Thread> v = new Vector<Thread>();
         
         int dotDiameter = dotRadius * 2;
         int latticeUnit = dotRadius + dotDiameter;
         
         for (int i = 0, jo = 1, je = 2; i < rows; i++, jo += 2, je += 2)
         {
            int dxo = 0;
            int dxe = 0;
            
            // Determines the horizontal shift of...
            for (Dot dot : ferrers)
            {
               // ...the "odd" rows of the upper part.
               if (dot != null && dot.getColor() == TRANSLUCENT_UCLA_GOLD
                  && jo < λ.part(i) && jo == dot.y / latticeUnit + 1) { dxo++; }
               
               // ...the "even" rows of the upper part.
               if (dot != null && dot.getColor() == TRANSLUCENT_UCLA_GOLD
                  && je < λ.part(i) && je == dot.y / latticeUnit + 1) { dxe++; }
            }
            
            // Shifts the "odd" rows of the upper part to the right.
            for (int j = jo; j < λ.part(i); j += 2)
            {
               int k = i * columns + j;
               if (j < λ.part(i) && ferrers[k] != null)
                  v.add(new Thread(new AnimationRunnable(ferrers[k], dxo, 0)));
            }
            
            // Shifts the "even" rows of the upper part to the right.
            for (int j = je; j < λ.part(i); j += 2)
            {
               int k = i * columns + j;
               if (j < λ.part(i) && ferrers[k] != null)
                  v.add(new Thread(new AnimationRunnable(ferrers[k], dxe, 0)));
            }
         }
         
         // Starts the animation.
         for (Thread t : v) t.start();
         for (Thread t : v) t.join();
      }
      
      /**
       * Colors the "odd" vertical strips of the lower part of the Ferrers
       * diagram.
       * 
       * @param color   the color
       * 
       * @throws InterruptedException if the animation is interrupted
       */
      private void colorLowerOddVerticalStrips(Color color)
         throws InterruptedException
      {
         Thread.sleep(HOLD);
         
         for (int i = 0; i < rows; i++)
         {
            for (int j = 1; j <= 2 * i; j += 2)
            {
               int k = i * columns + j;
               if (j < λ.part(i) && ferrers[k] != null)
                  ferrers[k].setColor(color);
            }
         }
         
         pause(HOLD);
      }
      
      /**
       * Colors the "odd" vertical strips of the upper part of the Ferrers
       * diagram.
       * 
       * @param color   the color
       * 
       * @throws InterruptedException if the animation is interrupted
       */
      private void colorUpperOddVerticalStrips(Color color)
         throws InterruptedException
      {
         Thread.sleep(HOLD);
         
         for (int i = 0; i < rows; i++)
         {
            for (int j = 2 * i + 2; j < λ.part(i); j += 2)
               ferrers[i * columns + j].setColor(color);
         }
         
         pause(HOLD);
      }
      
      /**
       * Compress all of the vertical strips of the lower part of the Ferrers
       * diagram to the left.
       * 
       * @throws InterruptedException if the animation is interrupted
       */
      private void compressAllLowerVerticalStripsDown()
         throws InterruptedException
      {
         Vector<Thread> v = new Vector<Thread>();
         
         for (int i = 0; i < rows; i++)
         {
            // "Even" vertical strips of the lower part
            for (int j = 0; j <= 2 * i; j += 2)
            {
               int k = i * columns + j;
               int dx = -j / 2;
               if (j < λ.part(i) && ferrers[k] != null)
                  v.add(new Thread(new AnimationRunnable(ferrers[k], dx, 0)));
            }
            
            // "Odd" vertical strips of the lower part
            for (int j = 1; j <= 2 * i; j += 2)
            {
               int k = i * columns + j;
               int dx = -(j + 1) / 2;
               if (j < λ.part(i) && ferrers[k] != null)
                  v.add(new Thread(new AnimationRunnable(ferrers[k], dx, 0)));
            }
         }
         
         // Starts the animation.
         for (Thread t : v) t.start();
         for (Thread t : v) t.join();
      }
      
      /**
       * Compresses all of the vertical strips of the upper part of the Ferrers
       * diagram to the left.
       * 
       * @throws InterruptedException if the animation is interrupted
       */
      private void compressAllUpperVerticalStripsLeft()
         throws InterruptedException
      {
         Vector<Thread> v = new Vector<Thread>();
         
         for (int i = 0; i < rows; i++)
         {
            for (int j = 2 * i + 1; j < λ.part(i); j++)
            {
               int k = i * columns + j;
               int dx = (i * 2 - j) / 2;
               v.add(new Thread(new AnimationRunnable(ferrers[k], dx, 0)));
            }
         }
         
         // Starts the animation.
         for (Thread t : v) t.start();
         for (Thread t : v) t.join();
      }
      
      /**
       * Compresses all of the vertical strips of the Ferrers diagram to the
       * left.
       * 
       * @throws InterruptedException if the animation is interrupted
       */
      private void compressAllVerticalStripsLeft() throws InterruptedException
      {
         Vector<Thread> v = new Vector<Thread>();
         
         for (int i = 0; i < rows; i++)
         {
            // "Even" vertical strips
            for (int j = 0; j < λ.part(i); j += 2)
            {
               int k = i * columns + j;
               int dx = -j / 2;
               v.add(new Thread(new AnimationRunnable(ferrers[k], dx, 0)));
            }
            
            // "Odd" vertical strips
            for (int j = 1; j < λ.part(i); j += 2)
            {
               int k = i * columns + j;
               int dx = -(j + 1) / 2;
               v.add(new Thread(new AnimationRunnable(ferrers[k], dx, 0)));
            }
         }
         
         // Starts the animation.
         for (Thread t : v) t.start();
         for (Thread t : v) t.join();
      }
      
      /**
       * Creates a Ferrers diagram.
       * 
       * @param rows      the maximum number of rows in the Ferrers diagram
       *                  during the bijection
       * @param columns   the maximum number of columns in the Ferrers diagram
       *                  during the bijection
       */
      private void createFerrersDiagram(int rows, int columns)
      {
         // Shrinks the Ferrers diagram in order to fit inside the board.
         for (int sentinel : SENTINELS)
         {
            if (rows > sentinel || columns > sentinel) dotRadius--;
            else break;
         }
         
         int dotDiameter = dotRadius * 2;
         int latticeUnit = dotRadius + dotDiameter;
         
         // Creates the Ferrers diagram.
         for (int i = 0; i < this.rows; i++)
         {
            for (int j = 0; j < λ.part(i); j++)
            {
               int x = j * latticeUnit + dotDiameter;
               int y = i * latticeUnit + dotDiameter;
               ferrers[i * this.columns + j] = new Dot(x, y, dotRadius, null);
            }
         }
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
       * Cuts the Ferrers diagram along the line
       * <i>ai</i> − <i>j</i> = <i>c</i>. The upper part is in one specified
       * color, and the lower part is in another specified color.
       * 
       * @param a       the slope of the diagonal cut
       * @param c       the downward shift in the diagonal cut
       * @param upper   the color of the upper part
       * @param lower   the color of the lower part
       * 
       * @throws InterruptedException if the animation is interrupted
       */
      private void cut(int a, int c, Color upper, Color lower)
         throws InterruptedException
      {
         for (int i = 0; i < rows; i++)
         {
            // Upper part
            for (int j = a * i - c + 1; j < λ.part(i); j++)
               ferrers[i * columns + j].setColor(upper);
            
            // Lower part
            for (int j = 0; j <= a * i - c; j++)
            {
               int k = i * columns + j;
               if (j < λ.part(i) && ferrers[k] != null)
                  ferrers[k].setColor(lower);
            }
         }
         
         pause(HOLD);
      }
      
      /**
       * Normalize all of the vertical strips of lower part of the Ferrers
       * diagram to the left.
       * 
       * @throws InterruptedException if the animation is interrupted
       */
      private void normalizeAllLowerVerticalStrips() throws InterruptedException
      {
         Vector<Thread> v = new Vector<Thread>();
         
         for (int i = 0; i < rows; i++)
         {
            // "Even" vertical strips of the lower part
            for (int j = 0; j <= 2 * i; j += 2)
            {
               int k = i * columns + j;
               int dy = -j / 2;
               if (j < λ.part(i) && ferrers[k] != null)
                  v.add(new Thread(new AnimationRunnable(ferrers[k], 0, dy)));
            }
            
            // "Odd" vertical strips of the lower part
            for (int j = 1; j <= 2 * i; j += 2)
            {
               int k = i * columns + j;
               int dy = -(j / 2 + 1);
               if (j < λ.part(i) && ferrers[k] != null)
                  v.add(new Thread(new AnimationRunnable(ferrers[k], 0, dy)));
            }
         }
         
         // Starts the animation.
         for (Thread t : v) t.start();
         for (Thread t : v) t.join();
      }
      
      /**
       * Normalizes the lower part of the Ferrers diagram up.
       * 
       * @throws InterruptedException if the animation is interrupted
       */
      private void normalizeLowerPartUp() throws InterruptedException
      {
         Vector<Thread> v = new Vector<Thread>();
         
         for (int i = 0; i < rows; i++)
         {
            for (int j = 0; j <= i - 1; j++)
            {
               int k = i * columns + j;
               int dy = -(j + 1);
               
               if (j < λ.part(i) && ferrers[k] != null)
                  v.add(new Thread(new AnimationRunnable(ferrers[k], 0, dy)));
            }
         }
         
         // Starts the animation.
         for (Thread t : v) t.start();
         for (Thread t : v) t.join();
      }
      
      /**
       * Normalizes the upper part of the Ferrers diagram to the left.
       * 
       * @param a   the slope of the diagonal cut
       * @param c   the downward shift in the diagonal cut
       * 
       * @throws InterruptedException if the animation is interrupted
       */
      private void normalizeUpperPartLeft(int a, int c)
         throws InterruptedException
      {
         Vector<Thread> v = new Vector<Thread>();
         
         for (int i = 0; i < rows; i++)
         {
            for (int j = a * i - c + 1; j < λ.part(i); j++)
            {
               int k = i * columns + j;
               int dx = -a * i + c - 1;
               v.add(new Thread(new AnimationRunnable(ferrers[k], dx, 0)));
            }
         }
         
         // Starts the animation.
         for (Thread t : v) t.start();
         for (Thread t : v) t.join();
      }
      
      /**
       * Pauses the animation for the specified number of milliseconds.
       * 
       * @param duration   the number of milliseconds to pause
       * 
       * @throws InterruptedException if the animation is interrupted
       */
      private void pause(int duration) throws InterruptedException
      {
         board.repaint();
         Thread.sleep(duration);
      }
      
      /**
       * Shreds the Ferrers diagram such that the "even" vertical strips are in
       * one color, and the "odd" vertical strips are in another color.
       * 
       * @throws InterruptedException if the animation is interrupted
       */
      private void shred() throws InterruptedException
      {
         for (int i = 0; i < rows; i++)
         {
            // "Even" vertical strips
            for (int j = 0; j < λ.part(i); j += 2)
               ferrers[i * columns + j].setColor(TRANSLUCENT_UCLA_GOLD);
            
            // "Odd" vertical strips
            for (int j = 1; j < λ.part(i); j += 2)
               ferrers[i * columns + j].setColor(TRANSLUCENT_UCLA_BLUE);
         }
         
         pause(HOLD);
      }
      
      /**
       * Stretches all of the vertical strips of the lower part of the Ferrers
       * diagram down by a factor of 2.
       * 
       * @throws InterruptedException if the animation is interrupted
       */
      private void stretchAllLowerVerticalStripsDown()
         throws InterruptedException
      {
         Vector<Thread> v = new Vector<Thread>();
         
         for (int i = 0; i < rows; i++)
         {
            // "Even" vertical strips of the lower part
            for (int j = 0; j <= 2 * i; j += 2)
            {
               int k = i * columns + j;
               int dy = j / 2;
               if (j < λ.part(i) && ferrers[k] != null)
                  v.add(new Thread(new AnimationRunnable(ferrers[k], 0, dy)));
            }
            
            // "Odd" vertical strips of the lower part
            for (int j = 1; j <= 2 * i; j += 2)
            {
               int k = i * columns + j;
               int dy = j / 2 + 1;
               if (j < λ.part(i) && ferrers[k] != null)
                  v.add(new Thread(new AnimationRunnable(ferrers[k], 0, dy)));
            }
         }
         
         // Starts the animation.
         for (Thread t : v) t.start();
         for (Thread t : v) t.join();
      }
      
      /**
       * Stretches all of the vertical strips of the upper part of the Ferrers
       * diagram down by a factor of 2.
       * 
       * @throws InterruptedException if the animation is interrupted
       */
      private void stretchAllUpperVerticalStripsDown()
         throws InterruptedException
      {
         Vector<Thread> v = new Vector<Thread>();
         
         for (int i = 0; i < rows; i++)
         {
            // "Even" vertical strips of the upper component
            for (int j = 2 * i + 1; j < λ.part(i); j += 2)
            {
               int k = i * columns + j;
               int dy = i;
               v.add(new Thread(new AnimationRunnable(ferrers[k], 0, dy)));
            }
            
            // "Odd" vertical strips of the upper component
            for (int j = 2 * i + 2; j < λ.part(i); j += 2)
            {
               int k = i * columns + j;
               int dy = i + 1;
               v.add(new Thread(new AnimationRunnable(ferrers[k], 0, dy)));
            }
         }
         
         // Starts the animation.
         for (Thread t : v) t.start();
         for (Thread t : v) t.join();
      }
      
      /**
       * Stretches all of the vertical strips of the Ferrers diagram down by a
       * factor of 2.
       * 
       * @throws InterruptedException if the animation is interrupted
       */
      private void stretchAllVerticalStripsDown() throws InterruptedException
      {
         Vector<Thread> v = new Vector<Thread>();
         
         for (int i = 0; i < rows; i++)
         {
            // "Even" vertical strips
            for (int j = 0; j < λ.part(i); j += 2)
            {
               int k = i * columns + j;
               int dy = i + 1;
               v.add(new Thread(new AnimationRunnable(ferrers[k], 0, dy)));
            }
            
            // "Odd" vertical strips
            for (int j = 1; j < λ.part(i); j += 2)
            {
               int k = i * columns + j;
               int dy = i;
               v.add(new Thread(new AnimationRunnable(ferrers[k], 0, dy)));
            }
         }
         
         // Starts the animation.
         for (Thread t : v) t.start();
         for (Thread t : v) t.join();
      }
      
      /**
       * Stretches both the upper and lower parts of the Ferrers diagram to the
       * right by a factor of 2.
       * 
       * @throws InterruptedException if the animation is interrupted
       */
      private void stretchBothPartsRight() throws InterruptedException
      {
         Vector<Thread> v = new Vector<Thread>();
         
         for (int i = 0; i < rows; i++)
         {
            // Upper part
            for (int j = i; j < λ.part(i); j++)
            {
               int k = i * columns + j;
               int dx = j - i;
               v.add(new Thread(new AnimationRunnable(ferrers[k], dx, 0)));
            }
            
            // Lower part
            for (int j = 0; j <= i - 1; j++)
            {
               int k = i * columns + j;
               int dx = i - j;
               if (j < λ.part(i) && ferrers[k] != null)
                  v.add(new Thread(new AnimationRunnable(ferrers[k], dx, 0)));
            }
         }
         
         // Starts the animation.
         for (Thread t : v) t.start();
         for (Thread t : v) t.join();
      }
      
      /**
       * Transposes the Ferrers diagram.
       * 
       * @throws InterruptedException if the animation is interrupted
       */
      private void transpose() throws InterruptedException
      {
         Thread.sleep(HOLD);
         for (Dot dot : ferrers) if (dot != null) dot.move(dot.y, dot.x);
         board.repaint();
      }
      
      /**
       * Transposes the lower part of the Ferrers diagram.
       * 
       * @param a   the slope of the diagonal cut
       * @param c   the downward shift in the diagonal cut
       * 
       * @throws InterruptedException if the animation is interrupted
       */
      private void transposeLowerPart(int a, int c) throws InterruptedException
      {
         Thread.sleep(HOLD);
         
         for (int i = 0; i < rows; i++)
         {
            for (int j = 0; j <= a * i - c; j++)
            {
               int k = i * columns + j;
               if (j < λ.part(i) && ferrers[k] != null)
                  ferrers[k].move(ferrers[k].y, ferrers[k].x);
            }
         }
         
         pause(HOLD);
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
               int x = dot.x + 3 * dotRadius * dx;
               int y = dot.y + 3 * dotRadius * dy;
               
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
         
         /**
          * Animates this runnable.
          * 
          * @throws InterruptedException if the animation is interrupted
          */
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