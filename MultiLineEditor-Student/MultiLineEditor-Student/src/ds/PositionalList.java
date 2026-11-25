package ds;

/**
 * Positional list ADT. Methods that take a Position throw IllegalArgumentException
 * if the position is not a valid position of THIS list (for example it was removed,
 * or it belongs to another list).
 */
public interface PositionalList<E> extends Iterable<E> {

    /** Number of elements. */
    int size();

    /** True if the list is empty. */
    boolean isEmpty();

    /** First position, or null if the list is empty. */
    Position<E> first();

    /** Last position, or null if the list is empty. */
    Position<E> last();

    /** Position just before p, or null if p is the first position. */
    Position<E> before(Position<E> p);

    /** Position just after p, or null if p is the last position. */
    Position<E> after(Position<E> p);

    /** Adds e at the front and returns its new position. */
    Position<E> addFirst(E e);

    /** Adds e at the end and returns its new position. */
    Position<E> addLast(E e);

    /** Adds e just before p and returns its new position. */
    Position<E> addBefore(Position<E> p, E e);

    /** Adds e just after p and returns its new position. */
    Position<E> addAfter(Position<E> p, E e);

    /** Replaces the element at p and returns the old element. */
    E set(Position<E> p, E e);

    /** Removes the element at p and returns it. After this, p is invalid. */
    E remove(Position<E> p);

    /** True if p is a valid position of this list (never throws). */
    boolean contains(Position<E> p);

    /**
     * Moves every element AFTER p into a new list and returns that list.
     * If p is null, all elements move. The moved positions stay valid:
     * the same Position objects now belong to the returned list.
     */
    PositionalList<E> splitAfter(Position<E> p);

    /**
     * Moves all elements of other to the end of this list; other becomes empty.
     * The moved positions stay valid and now belong to this list.
     */
    void appendAll(PositionalList<E> other);

    /** The positions from first to last (for use in a for-each loop). */
    Iterable<Position<E>> positions();
}
