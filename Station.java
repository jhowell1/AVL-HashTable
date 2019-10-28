/*
    James Howell
    CIS 233 / Scovil
    Assignment 2
 */

package AVLHashTable;

// I create what I call a Station because since the data structure of
// AVLHashTable is a hybrid data structure between an AVL Tree,
// a Hash Table, and the milestones object, I wanted to differentiate the Nodes
// as stations, to help decrease the confusion one might get due to expectations of
// what a Node might be.
public class Station<T extends Comparable<? super T>>
{
    public T item;
    public Station<T> left;
    public Station<T> right;
    public int hashPosition;
    public int frequency = 1;
    public int height = 0;
    public int balance;
    public boolean isActive;

    public Station (T theItem)
    {
        item = theItem;
        left = null;
        right = null;
        isActive = true;
        height = 0;
    }

    public Station (T theItem, boolean i)
    {
        item = theItem;
        left = null;
        right = null;
        isActive = i;
        height = 0;
    }

    public Station (T theItem, Station<T> lt, Station<T> rt, boolean i)
    {
        item = theItem;
        left = lt;
        right = rt;
        isActive = i;
    }

    public String character ()
    {
        return "Data: " + item + "\t\tHeight: " + height + "\t\t" + "Balance: " + balance;
    }

    public String toString ()
    {
        String leftChild;
        String rightChild;
        if (left != null)
            leftChild = left.character();
        else
            leftChild = null;
        if (right != null)
            rightChild = right.character();
        else
            rightChild = null;

        return "\n" + character() + "\n\t  Left:  " + leftChild + "\n\t  Right: " + rightChild;
    }
}
