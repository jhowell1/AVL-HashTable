/*
    James Howell
    CIS 233 / Scovil
    Assignment 2
 */

package AVLHashTable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class AVLHashTable<T extends Comparable<? super T>>
{
    public Station<T> root;
    private Station<T> [] hashTable;
    private int adjSize = 1001;
    private int theSize = 0;
    // milestones contains helpful information: mode, maxVal, minVal
    private Milestones<T> milestones;

    public AVLHashTable ()
    {
        root = null;
        hashTable = new Station [adjSize];
    }

    // Enters the first item into AVL Tree as its root,
    // enters item into hash table
    public AVLHashTable (T item)
    {
        theSize++;
        root = new Station<>(item);
        hashTable = new Station [adjSize];
        int hashPosition = hashPos(item);
        hashTable[hashPosition] = root;
        root.hashPosition = hashPosition;
        milestones = new Milestones(this.root);
    }

    // Enters the first station into AVL Tree as its root,
    // enters station into hash table
    public AVLHashTable (Station<T> station)
    {
        theSize++;
        root = station;
        hashTable = new Station [adjSize];
        int hashPosition = hashPos(root.item);
        hashTable[hashPosition] = root;
        root.hashPosition = hashPosition;
        milestones = new Milestones(this.root);
    }

    // Checks if a root exists
    public boolean isEmpty( )
    {
        return this.root == null;
    }

    // Erases root station, thereby erasing all references to other stations
    // Garbage collection: Thanos snaps his fingers
    public void makeEmpty ()
    {
        root = null;
        theSize = 0;
    }

    // returns height of a station
    private int height (Station<T> station)
    {
        return station == null ? -1 : station.height;
    }

    private int max (int lhs, int rhs)
    {
        return lhs > rhs ? lhs : rhs;
    }

    public Station<T> getRoot () { return this.root; }

    public int getSize () { return this.theSize; }

    public int getAdjSize () { return this.adjSize; }

    // Returns a hash value converted as a non-negative 31-bit integer
    private int hashFunction (T item)
    {
        if (item != null)
            return (item.hashCode() & 0x7fffffff) % adjSize;
        return -1;
    }

    // Returns the final destination of an item in the hash table,
    // with a modified version of quadratic probing
    private int hashPos (T newItem)
    {
        int hashPosition = hashFunction(newItem);
        int offset = 1;

        while (hashTable[hashPosition] != null)
        {
            hashPosition += offset;     //offset increment;
            offset += 2;
            if (offset >= adjSize*1.5)
                offset = 1;
            if (hashPosition >= adjSize)
                hashPosition -= adjSize;
        }

        return hashPosition;
    }

    // Finds specified item by deducting its hash val, uses quadratic probing
    // but stops when it has covered every place in memory,
    // b/c therefore it doesn't exist
    public int findPos (T item) {      //A2233JHowStation<T>
        if (item != null)
        {
            int itrs = 0;
            int hashVal = hashFunction(item);
            int offset = 1;
            while (itrs < theSize)
            {
                if (hashTable[hashVal] != null)
                {
                    if (item.compareTo(hashTable[hashVal].item) == 0)
                        return hashVal;

                    itrs++;
                    hashVal += offset;
                    offset += 2;

                    if (offset >= adjSize*1.5)
                        offset = 1;
                    if (hashVal >= adjSize)
                        hashVal -= adjSize;
                }
                else return -1;
            }
        }
        return -1;
    }

    // Uses find to generate existence boolean
    private boolean contains (T item)
    {
        if (findPos(item) != -1) return true;
        return false;
    }

    // Uses nextPrime method to change hash table size, copy items to new hashTable
    private void rehash ()
    {
        Station<T> [] old = hashTable;
        adjSize = nextPrime(4 * adjSize);
        hashTable = new Station [adjSize];
        for (int i = 0; i < theSize; i++)
            hashTable[i] = old[i];
    }

    // Finds next prime number
    private static int nextPrime (int n)
    {
        if (n % 2 == 0)
            n++;
        for (; !isPrime(n); n += 2)
            ;

        return n;
    }

    // Checks if number is a prime number
    private static boolean isPrime (int n)
    {
        if (n == 2 || n == 3)
            return true;

        if (n == 1 || n % 2 == 0)
            return false;

        for (int i = 3; i * i <= n; i += 2)
            if (n % i == 0)
                return false;

        return true;
    }

    // Insert method based on user entering an item only
    // accounts for present state of AVL Tree
    public void insert (T item)
    {
        if (!isEmpty() && item != null)
        {
            theSize++;
            root = insert(item, root);
        }
        else    //No root established
        {
            theSize++;
            root = new Station<>(item);
            int hashPosition = hashPos(item);
            hashTable[hashPosition] = root;
            root.hashPosition = hashPosition;
            milestones = new Milestones<T>(this.root);
        }
    }

    // The "chuncky" insert method
    // Places item into proper AVL Tree location
    private Station<T> insert (T item, Station<T> station)
    {
        if (theSize > adjSize / 2)
            rehash();

        if (station == null)
        {
            station = new Station<>(item);
            int hashPosition = hashPos(item);
            hashTable[hashPosition] = station;
            station.hashPosition = hashPosition;
        }
        else if (item.compareTo(station.item) < 0)
        {
            station.left = insert(item, station.left);
            station.balance = height(station.right) - height(station.left);
            if (height(station.left) - height(station.right) == 2)  // rotation req'd
                if (item.compareTo(station.left.item) < 0)          // Case 1
                    station = rotateWithLeftChild(station);
                else                                                // Case 2
                    station = doubleWithLeftChild(station);
        }
        else if (item.compareTo(station.item) > 0)
        {
            station.right = insert(item, station.right);
            station.balance = height(station.right) - height(station.left);
            if (height(station.right) - height(station.left) == 2)  // rotation req'd
                if (item.compareTo(station.right.item) > 0)         // Case 3
                    station = rotateWithRightChild(station);
                else                                                // Case 4
                    station = doubleWithRightChild(station);
        }
        else    // Item already exists in AVL Tree
        {
            // Item will still be inserted as a new item into hash table
            // Increments duplicates of the preexisting item, checks if its the new mode
            theSize++;
            if (++station.frequency > milestones.mode.frequency)
                milestones.mode = station;

            Station<T> newStation = new Station<>(item, false);
            int hashPosition = hashPos(item);
            hashTable[hashPosition] = newStation;
            newStation.hashPosition = hashPosition;
        }
        // Item is properly inserted
        station.height = max(height(station.left), height(station.right)) + 1;
        return station;
    }

    private Station<T> rotateWithLeftChild (Station<T> s2)
    {
        Station<T> s1 = s2.left;
        s2.left = s1.right;
        s1.right = s2;
        s2.balance = height(s2.right) - height(s2.left);
        s2.height = max(height(s2.left), height(s2.right)) + 1;
        s1.balance = height(s1.right) - height(s1.left);
        s1.height = max(height(s1.left), s2.height) + 1;
        return s1;
    }

    private Station<T> rotateWithRightChild (Station<T> s1)
    {
        Station s2 = s1.right;
        s1.right = s2.left;
        s2.left = s1;
        s1.height = max(height(s1.left), height(s1.right)) + 1;
        s1.balance = height(s2.right) - height(s2.left);
        s2.height = max(height(s2.right), s1.height) + 1;
        s2.balance = height(s1.right) - height(s1.left);
        return s2;
    }

    private Station<T> doubleWithLeftChild (Station<T> s3)
    {
        s3.left = rotateWithRightChild(s3.left);
        return rotateWithLeftChild(s3);
    }

    private Station<T> doubleWithRightChild (Station<T> s1)
    {
        s1.right = rotateWithLeftChild(s1.right);
        return rotateWithRightChild(s1);
    }

    // Remove method when station is not provided, must find station
    public void remove (T item)
    {
        Station<T> s = hashTable[findPos(item)];
        remove(s);
    }

    // Lazy Deletion: Turns off station
    public void remove (Station<T> station)
    {
        station.isActive = false;
    }

    // printTree starting at root (whole tree)
    // only prints active stations
    public void printTree ()
    {
        printTree(root);
    }

    // printTree starting at specified station
    // only prints active stations
    private void printTree (Station<T> station)
    {
        if (isEmpty())
            System.out.println("Tree is currently empty.");
        if (station != null)
        {
            printTree(station.left);
            if (station.isActive)
                System.out.println(station.toString());
            printTree(station.right);
        }
    }

    // printBalTree starting at root (whole tree)
    public void printBalTree ()
    {
        printBalTree(root, true);
    }

    // printBalTree starting at root (whole tree) in specified order
    public void printBalTree (boolean ascOrder)
    {
        printBalTree(root, ascOrder);
    }

    // printBalTree starting at specified station in specified order
    private void printBalTree (Station<T> station, boolean ascOrder)
    {
        if (isEmpty())
            System.out.println("Tree is currently empty.");
        if (station != null)
        {
            if (ascOrder)
            {
                printBalTree(station.left, true);
                System.out.println(station.toString());
                printBalTree(station.right, true);
            }
            else
            {
                printBalTree(station.right, false);
                System.out.println(station.toString());
                printBalTree(station.left, false);
            }
        }
    }

    public void writeBalTree () throws IOException
    {
        writeBalTree(root, true);
    }

    // writes to a new file
    // writeBalTree starting at root (whole tree)
    public void writeBalTree (boolean ascOrder) throws IOException
    {
        writeBalTree(root, ascOrder);
    }

    // writes to a new file
    // writeBalTree starting at specified station
    public void writeBalTree (Station<T> station, boolean ascOrder) throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter("AVLHashTableout.txt"));
        if (station != null)
            printBalTree(station, writer, ascOrder);
        else
            writer.write("\nTree is currently empty.");
        writer.close();
    }

    // Assists writeBalTree method, writes each station to file instead of display
    private void printBalTree (Station<T> station, BufferedWriter w, boolean ascOrder) throws IOException
    {
        if (station != null)
        {
            printBalTree(station.left, w, ascOrder);
            w.write(station.toString());
            printBalTree(station.right, w, ascOrder);
        }
    }

    // Returns the left most station held by milestones
    public T findMin ()
    {
        milestones.updateMinVal(root);
        return milestones.getMinVal();
    }

    // Returns the right most station held by milestones
    public T findMax ()
    {
        milestones.updateMaxVal(root);
        return milestones.getMaxVal();
    }

    // Returns new Result instance with values held by milestones
    public AVLHashTableResult<T> findMode ()
    {
        return new AVLHashTableResult(milestones.mode, milestones.mode.frequency);
    }

    // Nested Result class
    private class AVLHashTableResult<T> implements Result
    {
        private T mode;
        private int count;

        public AVLHashTableResult(T m, int frequency)
        {
            mode =  (T) m;
            count = frequency;
        }

        public T mode() { return mode; }

        public int count() { return count; }

        public String toString()
        {
            return "\nMode: \t" + mode();
        }
    }

    public class Milestones<T extends Comparable<? super T>>
    {
        public Station<T> mode;
        public T maxVal;
        public T minVal;

        public Milestones (Station<T> r)
        {
            mode = r;
            maxVal = r.item;
            minVal = r.item;
        }

        public void updateMaxVal (Station<T> station)
        {
            while (station.right.isActive)
                station = station.right;
            maxVal = station.item;
        }

        public void updateMinVal (Station<T> station)
        {
            while (station.left.isActive)
                station = station.left;
            minVal = station.item;
        }

        public T getMinVal() { return minVal; }

        public T getMaxVal() { return maxVal; }
    }

    public String author () { return "James West Howell"; }
}
