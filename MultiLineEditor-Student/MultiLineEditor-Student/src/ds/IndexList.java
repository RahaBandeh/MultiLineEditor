package ds;

/**
 * Index-based list ADT (a small version of java.util.List).
 * Indices start at 0. An invalid index throws IndexOutOfBoundsException.
 */
public interface IndexList<E> extends Iterable<E> {

    /** Number of elements in the list. */
    int size();

    /** True if the list has no elements. */
    boolean isEmpty();

    /** Returns the element at index i (0 <= i < size()). */
    E get(int i);

    /** Replaces the element at index i (0 <= i < size()) and returns the old element. */
    E set(int i, E e);

    /** Inserts e at index i (0 <= i <= size()); later elements move one place right. */
    void add(int i, E e);

    /** Adds e at the end of the list (same as add(size(), e)). */
    void addLast(E e);

    /** Removes and returns the element at index i (0 <= i < size()); later ones move left. */
    E remove(int i);
}
