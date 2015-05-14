package edu.ucla.math;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Vector;

/**
 * In number theory and combinatorics, a <b>partition</b> of a positive integer
 * <i>n</i> is a way of writing <i>n</i> as a sum of positive integers, which
 * are in decreasing order. The notation λ ⊢ <i>n</i> means that λ is a
 * partition of <i>n</i>. A summand in a partition is also called a <b>part</b>.
 * Partitions can be graphically visualized with Young diagrams or Ferrers
 * diagrams.
 * 
 * @author Kris Torres
 */
public final class Partition
{
   /**
    * The value of the square root of the Riemann zeta function ζ(<i>z</i>)
    * evaluated at <i>z</i> = 2.
    */
   private static final double SQRT_ZETA_2 = Math.PI / Math.sqrt(6.0);
   
   /** The vector buffer into which the parts in this partition are stored. */
   private Vector<Integer> buffer = new Vector<Integer>();
   
   /** Constructs an "empty" partition. */
   public Partition() {}
   
   /**
    * Constructs a partition containing the specified parts.
    * 
    * @param parts   the parts
    * 
    * @throws IllegalArgumentException if any of the parts is not positive
    */
   public Partition(int... parts)
   {
      // Tests if all of the parts are positive.
      for (int i = 0; i < parts.length; i++)
      {
         if (parts[i] < 1)
         {
            String error = "Part " + i + " is not positive: " + parts[i];
            throw new IllegalArgumentException(error);
         }
         
         buffer.add(parts[i]);
      }
      
      Collections.sort(buffer);
      Collections.reverse(buffer);
   }
   
   /**
    * Constructs a partition containing the parts in the specified array.
    * 
    * @param a   the array of parts
    * 
    * @throws NullPointerException if the array is <code>null</code>, or if any
    *         of the parts in the array is <code>null</code>
    * @throws IllegalArgumentException if any of the parts in the array is not
    *         positive
    */
   public Partition(Integer[] a)
   {
      // Tests if the given array is not null.
      if (a == null) throw new NullPointerException("Array is null.");
      
      // Tests if all of the parts in the given array are not null and positive.
      for (int i = 0; i < a.length; i++)
      {
         if (a[i] == null)
            throw new NullPointerException("Part " + i + " in array is null");
         
         if (a[i] < 1)
         {
            String error = "Part " + i + " in array is not positive: " + a[i];
            throw new IllegalArgumentException(error);
         }
         
         buffer.add(a[i]);
      }
      
      Collections.sort(buffer);
      Collections.reverse(buffer);
   }
   
   /**
    * Constructs a partition containing the parts in the specified partition.
    * 
    * @param p   the partition to be copied
    * 
    * @throws NullPointerException if the partition is <code>null</code>
    */
   public Partition(Partition p)
   {
      // Tests if the given partition is not null.
      if (p == null) throw new NullPointerException("Partition is null.");
      
      buffer = new Vector<Integer>(p.buffer);
   }
   
   /** Removes all of the parts from this partition. */
   public void clear() { buffer.clear(); }
   
   /**
    * Tests if this partition contains the specified part.
    * 
    * @param part   the part to be searched
    * 
    * @return <code>true</code> if this partition contains the part, or
    *         <code>false</code> otherwise
    */
   public boolean contains(int part) { return buffer.contains(part); }
   
   /**
    * Returns the <b>crank</b> of this partition. For a partition λ, let
    * <i>b</i>(λ) denote the largest part in λ, ω(λ) denote the multiplicity of
    * 1 in λ, and μ(λ) denote the number of parts in λ larger than ω(λ). If
    * ω(λ) = 0, the crank <i>c</i>(λ) = <i>b</i>(λ). Otherwise,
    * <i>c</i>(λ) = μ(λ) − ω(λ).
    * 
    * @return the crank
    * 
    * @throws RuntimeException if this partition contains no parts
    */
   public int crank()
   {
      // The crank of an "empty" partition is undefined!
      if (isEmpty()) throw new RuntimeException("No such crank exists.");
      
      int ω = multiplicity(1);
      
      if (ω == 0)
      {
         return largestPart();
      }
      else
      {
         int μ = 0;
         for (int element : buffer) if (element > ω) μ++;
         return μ - ω;
      }
   }
   
   /**
    * Returns the <b>Durfee rank</b> of this partition. The Durfee rank of a
    * partition λ is the largest number <i>k</i> such that λ contains at least
    * <i>k</i> parts ≥ <i>k</i>.
    * 
    * @return the Durfee rank
    * 
    * @throws RuntimeException if this partition contains no parts
    */
   public int durfeeRank()
   {
      // The Durfee rank of an "empty" partition is undefined!
      if (isEmpty()) throw new RuntimeException("No such Durfee rank exists.");
      
      int s = 0;
      
      for (int i = 1; i <= buffer.firstElement(); i++)
      {
         int count = 0;
         for (int element : buffer) if (element >= i) count++;
         if (count >= i) s = i;
      }
      
      return s;
   }
   
   /**
    * Compares the specified partition with this partition for equality.
    * 
    * @param p   the partition to be compared for equality
    * 
    * @return <code>true</code> if the two partitions are equal, or
    *         <code>false</code> otherwise
    */
   public boolean equals(Partition p)
   {
      return this == null ? p == null : buffer.equals(p.buffer);
   }
   
   /**
    * Removes the first (lowest-indexed) occurrence of the specified part from
    * this partition. If the partition does not contain the part, then it is
    * unchanged.
    * 
    * @param part   the part to be erased
    */
   public void erase(int part) { buffer.remove((Integer) part); }
   
   /**
    * Inserts the specified part into this partition.
    * 
    * @param part   the part to be inserted
    * 
    * @throws IllegalArgumentException if the part is not positive
    */
   public void insert(int part)
   {
      // Tests if the part to be inserted is positive.
      if (part < 1)
         throw new IllegalArgumentException("Illegal part to insert: " + part);
      
      buffer.add(part);
      Collections.sort(buffer);
      Collections.reverse(buffer);
   }
   
   /**
    * Tests if this partition contains distinct parts.
    * 
    * @return <code>true</code> if this partition contains distinct parts, or
    * <code>false</code> otherwise
    */
   public boolean isDistinct()
   {
      // Tests if all of the parts in ths partition are distinct.
      for (int i = 0; i < numberOfParts() - 1; i++)
         if (part(i) == part(i + 1)) return false;
      
      return true;
   }
   
   /**
    * Tests if this partition is "empty."
    * 
    * @return <code>true</code> if this partition contains no parts, or
    *         <code>false</code> otherwise
    */
   public boolean isEmpty() { return buffer.isEmpty(); }
   
   /**
    * Tests if this partition is an even partition, that is, if all of the parts
    * in the partition are even.
    * 
    * @return <code>true</code> if this partition is an even partition, or
    *         <code>false</code> otherwise
    */
   public boolean isEven()
   {
      // Tests if all of the parts in this partition are even.
      for (int element : buffer) if (element % 2 != 0) return false;
      
      return true;
   }
   
   /**
    * Tests if this partition is an odd partition, that is, if all of the parts
    * in the partition are odd.
    * 
    * @return <code>true</code> if this partition is an odd partition, or
    *         <code>false</code> otherwise
    */
   public boolean isOdd()
   {
      // Tests if all of the parts in this partition are odd.
      for (int element : buffer) if (element % 2 == 0) return false;
      
      return true;
   }
   
   /**
    * Returns the <b>largest part</b> (the part at index <code>0</code>) in this
    * partition.
    * 
    * @return the largest part
    * 
    * @throws NoSuchElementException if this partition contains no parts
    */
   public int largestPart()
   {
      // Tests if this partition is "nonempty."
      if (isEmpty())
         throw new NoSuchElementException("No such largest part exists.");
      
      return buffer.firstElement();
   }
   
   /**
    * Returns the <b>multiplicity</b> of the specified part in this partition.
    * 
    * @param part   the part to be counted
    * 
    * @return the multiplicity of the part
    */
   public int multiplicity(int part)
   {
      int count = 0;
      for (int element : buffer) if (element == part) count++;
      return count;
   }
   
   /**
    * Returns the <b>number of parts</b> in this partition.
    * 
    * @return the number of parts
    */
   public int numberOfParts() { return buffer.size(); }
   
   /**
    * Returns the <i>k</i>-th part in this partition.
    * 
    * @param k   the index of the part
    * 
    * @return the <i>k</i>-th part
    * 
    * @throws IndexOutOfBoundsException if
    *         <code>k < 0 || k >= numberOfParts()</code>
    */
   public int part(int k)
   {
      // Tests if k is valid.
      if (k < 0 || k >= numberOfParts())
         throw new IndexOutOfBoundsException("Illegal part index: " + k);
      
      return buffer.get(k);
   }
   
   /**
    * Outputs the Ferrers diagram of this partition in English notation using
    * the character <code>*</code> for each cell.
    */
   public void printFerrersDiagram() { printFerrersDiagram('*'); }
   
   /**
    * Outputs the Ferrers diagram of this partition in English notation using
    * the specified character for each cell.
    * 
    * @param cell   the character to represent a cell
    */
   public void printFerrersDiagram(char cell)
   {
      // Prints the parts as rows of cells from largest to smallest.
      for (int element : buffer)
      {
         for (int i = 0; i < element; i++) System.out.print(cell);
         System.out.println();
      }
   }
   
   /**
    * Outputs the Ferrers diagram of this partition in the specified notation
    * using the specified character for each cell.
    * 
    * @param cell      the character to represent a cell
    * @param english   the notation (<code>true</code> for English, or
    *                  <code>false</code> for French)
    */
   public void printFerrersDiagram(char cell, boolean english)
   {
      if (english)
      {
         printFerrersDiagram(cell);
      }
      else
      {
         for (int i = numberOfParts() - 1; i >= 0; i--)
         {
            for (int j = 0; j < part(i); j++) System.out.print(cell);
            System.out.println();
         }
      }
   }
   
   /**
    * Returns the <b>rank</b> of this partition. The rank of a partition λ is
    * the number obtained by subtracting the number of parts in λ from the
    * largest part in λ.
    * 
    * @return the rank
    * 
    * @throws RuntimeException if this partition contains no parts
    */
   public int rank()
   {
      // The rank of an "empty" partition is undefined!
      if (isEmpty()) throw new RuntimeException("No such rank exists.");
      
      return largestPart() - numberOfParts();
   }
   
   /**
    * Returns the <b>smallest part</b> (the part at index
    * <code>numberOfParts() - 1</code>) in this partition.
    * 
    * @return the smallest part
    * 
    * @throws NoSuchElementException if this partition contains no parts
    */
   public int smallestPart()
   {
      // Tests if this partition is "nonempty."
      if (isEmpty())
         throw new NoSuchElementException("No such smallest part exists.");
      
      return buffer.lastElement();
   }
   
   /**
    * Returns an array containing all of the parts in this partition in
    * decreasing order.
    * 
    * @return the array of parts
    */
   public Integer[] toArray()
   {
      Integer[] a = new Integer[numberOfParts()];
      
      // Inserts the parts from this partition into the array.
      for (int i = 0; i < numberOfParts(); i++) a[i] = part(i);
      
      return a;
   }
   
   /**
    * Returns a string representation of the tuple of parts in this partition.
    * 
    * @return the tuple of parts as a string
    */
   @Override
   public String toString() { return buffer.toString(); }
   
   /**
    * Returns the <b>weight</b> <i>n</i> of this partition, that is, the sum of
    * the parts in the partition.
    * 
    * @return the weight, or 0 if this partition contains no parts
    */
   public int weight()
   {
      int n = 0;
      for (int element : buffer) n += element;
      return n;
   }
   
   /**
    * Returns the <b>conjugate</b> of the specified partition.
    * 
    * @param p   the partition
    * 
    * @return the conjugate
    * 
    * @throws NullPointerException if the partition is <code>null</code>
    */
   public static Partition conjugate(Partition p)
   {
      // Tests if the given partition is not null.
      if (p == null)
         throw new NullPointerException("Partition is null.");
      
      Partition q = new Partition();
      
      // Inserts the rows in the Ferrers diagram as columns into the result.
      for (int i = 0; i < p.numberOfParts(); i++)
      {
         for (int j = 0; j < p.part(i); j++)
         {
            q.erase(i);
            q.insert(i + 1);
         }
      }
      
      return q;
   }
   
   /**
    * Returns a random even partition with distinct parts such that the sum of
    * its distinct parts is at least the specified positive integer.
    * 
    * @param n   the positive integer
    * 
    * @return a random even partition with distinct parts
    * 
    * @throws IllegalArgumentException if <i>n</i> < 1
    */
   public static Partition distinctEvenRandom(int n)
   {
      // Tests if n is positive.
      if (n < 1) throw new IllegalArgumentException("Illegal weight: " + n);
      
      Partition p;
      
      // Keeps on randomizing until we get lucky!
      do { p = evenRandom(n); }
      while (!p.isDistinct());
      
      return p;
   }
   
   /**
    * Returns a random even partition with distinct parts such that the sum of
    * its distinct parts is exactly the specified positive even integer.
    * 
    * @param n   the positive even integer
    * 
    * @return a random even partition with distinct parts
    * 
    * @throws IllegalArgumentException if either <i>n</i> < 1 or <i>n</i> is odd
    */
   public static Partition distinctEvenRandomExactly(int n)
   {
      // Tests if n is positive and even.
      if (n < 1 || n % 2 != 0)
         throw new IllegalArgumentException("Illegal even weight: " + n);
      
      Partition p;
      
      // Keeps on randomizing until we get lucky!
      do { p = distinctEvenRandom(n); }
      while (p.weight() != n);
      
      return p;
   }
   
   /**
    * Returns a random odd partition with distinct parts such that the sum of
    * its distinct parts is at least the specified positive integer.
    * 
    * @param n   the positive integer
    * 
    * @return a random odd partition with distinct parts
    * 
    * @throws IllegalArgumentException if <i>n</i> < 1
    */
   public static Partition distinctOddRandom(int n)
   {
      // Tests if n is positive.
      if (n < 1) throw new IllegalArgumentException("Illegal weight: " + n);
      
      Partition p;
      
      // Keeps on randomizing until we get lucky!
      do { p = oddRandom(n); }
      while (!p.isDistinct());
      
      return p;
   }
   
   /**
    * Returns a random odd partition with distinct parts such that the sum of
    * its parts is exactly the specified positive integer.
    * 
    * @param n   the positive integer
    * 
    * @return a random odd partition with distinct parts
    * 
    * @throws IllegalArgumentException if <i>n</i> < 1
    */
   public static Partition distinctOddRandomExactly(int n)
   {
      // Tests if n is positive.
      if (n < 1) throw new IllegalArgumentException("Illegal weight: " + n);
      
      Partition p;
      
      // Keeps on randomizing until we get lucky!
      do { p = distinctOddRandom(n); }
      while (p.weight() != n);
      
      return p;
   }
   
   /**
    * Returns a random even partition such that the sum of its parts is at least
    * the specified positive integer.
    * 
    * @param n   the positive integer
    * 
    * @return a random even partition
    * 
    * @throws IllegalArgumentException if <i>n</i> < 1
    */
   public static Partition evenRandom(int n)
   {
      // Tests if n is positive.
      if (n < 1) throw new IllegalArgumentException("Illegal weight: " + n);
      
      Partition p = new Partition();
      Random shuffle = new Random();
      
      // Generates a random even partition.
      for (int i = 2; p.weight() < n; i += 2)
      {
         double u = shuffle.nextDouble();
         
         // log u is undefined!
         if (u > 0.0)
         {
            double x = Math.exp(-SQRT_ZETA_2 / Math.sqrt(n));
            double λ = 1 - Math.pow(x, i);
            int count = (int) Math.floor(-Math.log(u) / λ);
            
            // Inserts the parts into the partition.
            for (int j = 0; j < count; j++) p.insert(i);
         }
      }
      
      return p;
   }
   
   /**
    * Returns a random even partition such that the sum of its parts is exactly
    * the specified positive even integer.
    * 
    * @param n   the positive even integer
    * 
    * @return a random even partition
    * 
    * @throws IllegalArgumentException if either <i>n</i> < 1 or <i>n</i> is odd
    */
   public static Partition evenRandomExactly(int n)
   {
      // Tests if n is positive and even.
      if (n < 1 || n % 2 != 0)
         throw new IllegalArgumentException("Illegal even weight: " + n);
      
      Partition p;
      
      // Keeps on randomizing until we get lucky!
      do { p = evenRandom(n); }
      while (p.weight() != n);
      
      return p;
   }
   
   /**
    * Returns a random odd partition such that the sum of its parts is at least
    * the specified positive integer.
    * 
    * @param n   the positive integer
    * 
    * @return a random odd partition
    * 
    * @throws IllegalArgumentException if <i>n</i> < 1
    */
   public static Partition oddRandom(int n)
   {
      // Tests if n is positive.
      if (n < 1) throw new IllegalArgumentException("Illegal weight: " + n);
      
      Partition p = new Partition();
      Random shuffle = new Random();
      
      // Generates a random even partition.
      for (int i = 1; p.weight() < n; i += 2)
      {
         double u = shuffle.nextDouble();
         
         // log u is undefined!
         if (u > 0.0)
         {
            double x = Math.exp(-SQRT_ZETA_2 / Math.sqrt(n));
            double λ = 1 - Math.pow(x, i);
            int count = (int) Math.floor(-Math.log(u) / λ);
            
            // Inserts the parts into the partition.
            for (int j = 0; j < count; j++) p.insert(i);
         }
      }
      
      return p;
   }
   
   /**
    * Returns a random odd partition such that the sum of its parts is exactly
    * the specified positive integer.
    * 
    * @param n   the positive integer
    * 
    * @return  a random odd partition
    * 
    * @throws IllegalArgumentException if <i>n</i> < 1
    */
   public static Partition oddRandomExactly(int n)
   {
      // Tests if n is positive.
      if (n < 1) throw new IllegalArgumentException("Illegal weight: " + n);
      
      Partition p;
      
      // Keeps on randomizing until we get lucky!
      do { p = oddRandom(n); }
      while (p.weight() != n);
      
      return p;
   }
   
   /**
    * Returns a random partition such that the sum of its parts is at least the
    * specified positive integer.
    * 
    * @param n   the positive integer
    * 
    * @return a random partition
    * 
    * @throws IllegalArgumentException if <i>n</i> < 1
    */
   public static Partition random(int n)
   {
      // Tests if n is positive.
      if (n < 1) throw new IllegalArgumentException("Illegal weight: " + n);
      
      Partition p = new Partition();
      Random shuffle = new Random();
      
      // Generates a random partition.
      for (int i = 1; p.weight() < n; i++)
      {
         double u = shuffle.nextDouble();
         
         // log u is undefined!
         if (u > 0.0)
         {
            double x = Math.exp(-SQRT_ZETA_2 / Math.sqrt(n));
            double λ = 1 - Math.pow(x, i);
            int count = (int) Math.floor(-Math.log(u) / λ);
            
            // Inserts the parts into the partition.
            for (int j = 0; j < count; j++) p.insert(i);
         }
      }
      
      return p;
   }
   
   /**
    * Returns a random partition such that the sum of its parts is exactly the
    * specified positive integer.
    * 
    * @param n   the positive integer
    * 
    * @return a random partition
    * 
    * @throws IllegalArgumentException if <i>n</i> < 1
    */
   public static Partition randomExactly(int n)
   {
      // Tests if n is positive.
      if (n < 1) throw new IllegalArgumentException("Illegal weight: " + n);
      
      Partition p = new Partition();
      
      // Keeps on randomizing until we get lucky!
      while (p.weight() != n) p = random(n);
      
      return p;
   }
   
   /**
    * Returns the <b>sum</b> of two specified partitions, as defined on page 9
    * in <i>The Nature of Partitions Bijections II: Asymptotic Stability</i> by
    * Igor Pak
    * (<font color=blue><u>www.math.ucla.edu/~pak/papers/stab5.pdf</u></font>).
    * 
    * @param lhs   the first partition addend
    * @param rhs   the second partition addend
    * 
    * @return the sum
    * 
    * @throws NullPointerException if the first partition is <code>null</code>,
    *         or the second partition is <code>null</code>
    */
   public static Partition sum(Partition lhs, Partition rhs)
   {
      // Tests if the first given partition is not null.
      if (lhs == null)
      {
         String error = "Left partition addend is null.";
         throw new NullPointerException(error);
      }
      
      // Tests if the second given partition is not null.
      if (rhs == null)
      {
         String error = "Right partition addend is null.";
         throw new NullPointerException(error);
      }
      
      Partition p = new Partition();
      
      // Inserts the sums of the pairs of corresponding parts.
      for (int i = 0; i < Math.max(lhs.buffer.size(), rhs.buffer.size()); i++)
         p.insert(lhs.part(i) + rhs.part(i));
      
      return p;
   }
   
   /**
    * Returns the <b>union</b> of two specified partitions, as defined on page 9
    * in <i>The Nature of Partitions Bijections II: Asymptotic Stability</i> by
    * Igor Pak
    * (<font color=blue><u>www.math.ucla.edu/~pak/papers/stab5.pdf</u></font>).
    * 
    * @param lhs   the first partition
    * @param rhs   the second partition
    * 
    * @return the union
    * 
    * @throws NullPointerException if the first partition is <code>null</code>,
    *         or the second partition is <code>null</code>
    */
   public static Partition union(Partition lhs, Partition rhs)
   {
      // Tests if the first given partition is not null.
      if (lhs == null)
      {
         String error = "Left partition is null.";
         throw new NullPointerException(error);
      }
      
      // Tests if the second given partition is not null.
      if (rhs == null)
      {
         String error = "Right partition is null.";
         throw new NullPointerException(error);
      }
      
      Partition p = new Partition(lhs);
      
      // Inserts the parts from the second partition into the first partition.
      for (int i = 0; i < rhs.numberOfParts(); i++) p.insert(rhs.part(i));
      
      return p;
   }
}