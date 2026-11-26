package ds;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Positional list on a doubly linked list with two sentinel nodes (header and
 * trailer).
 * Every node knows the list it belongs to (owner). A removed node has owner ==
 * null.
 */
public class LinkedPositionalList<E> implements PositionalList<E> {

    /** One node of the list. The node itself is the Position given to the user. */
    private static class Node<E> implements Position<E> {
        private E element;
        private Node<E> prev;
        private Node<E> next;
        private LinkedPositionalList<E> owner; // null after the node is removed

        Node(E element, Node<E> prev, Node<E> next, LinkedPositionalList<E> owner) {
            this.element = element;
            this.prev = prev;
            this.next = next;
            this.owner = owner;
        }

        @Override
        public E getElement() {
            if (owner == null) {
                throw new IllegalStateException("this position is no longer valid");
            }
            return element;
        }
    }

    private final Node<E> header; // sentinel before the first element
    private final Node<E> trailer; // sentinel after the last element
    private int size;
    private int modCount; // part E: +1 on every add, remove, split and append

    public LinkedPositionalList() {
        header = new Node<>(null, null, null, this);
        trailer = new Node<>(null, header, null, this);
        header.next = trailer;
        size = 0;
    }

    // ------------------------------------------------------------ helpers

    /**
     * Returns the node of p if p is a valid position of this list.
     * 
     * @throws IllegalArgumentException if p was removed, is a sentinel, is null
     *                                  or belongs to another list
     */
    private Node<E> validate(Position<E> p) {
        if (!(p instanceof Node))
            throw new IllegalArgumentException("this position is invalid!");

        Node<E> node = (Node<E>) p;

        if (node.owner == null)
            throw new IllegalArgumentException("this position has been removed!");

        if (node.owner != this)
            throw new IllegalArgumentException("this position doesn't belong this list!");

        if (node == header || node == trailer)
            throw new IllegalArgumentException("this position belongs to sentinels!");

        return node;
    }

    /** Returns the node as a Position, or null if it is a sentinel. */
    private Position<E> position(Node<E> node) {
        return (node == header || node == trailer) ? null : node;
    }

    /** Creates a node for e between pred and succ and returns it. */
    private Position<E> addBetween(E e, Node<E> pred, Node<E> succ) {

        Node<E> newNode = new Node<E>(e, pred, succ, this);
        pred.next = newNode;
        succ.prev = newNode;

        size++;
        modCount++;

        return position(newNode);
    }

    // ------------------------------------------------------------ queries

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Position<E> first() {

        return position(header.next);
    }

    @Override
    public Position<E> last() {

        return position(trailer.prev);
    }

    @Override
    public Position<E> before(Position<E> p) {
        Node<E> current = validate(p);

        return position(current.prev);

    }

    @Override
    public Position<E> after(Position<E> p) {
        Node<E> current = validate(p);

        return position(current.next);
    }

    @Override
    public boolean contains(Position<E> p) {
        try {
            validate(p);
            return true;
        } catch (IllegalArgumentException e) {

            return false;
        }
    }

    // ------------------------------------------------------------ updates

    @Override
    public Position<E> addFirst(E e) {

        return addBetween(e, header, header.next);

    }

    @Override
    public Position<E> addLast(E e) {

        return addBetween(e, trailer.prev, trailer);

    }

    @Override
    public Position<E> addBefore(Position<E> p, E e) {

        Node<E> current = validate(p);
        return addBetween(e, current.prev, current);

    }

    @Override
    public Position<E> addAfter(Position<E> p, E e) {

        Node<E> current = validate(p);
        return addBetween(e, current, current.next);

    }

    @Override
    public E set(Position<E> p, E e) {
        Node<E> current = validate(p);

        E oldValue = current.element;
        current.element = e;

        return oldValue;
    }

    @Override
    public E remove(Position<E> p) {
        Node<E> current = validate(p);

        current.prev.next = current.next;
        current.next.prev = current.prev;
        current.owner = null;
        current.prev = null;
        current.next = null;

        size--;
        modCount++;

        return current.element;

    }

    @Override
    public PositionalList<E> splitAfter(Position<E> p) {
        Node<E> before = (p == null) ? header : validate(p);
        LinkedPositionalList<E> newList = new LinkedPositionalList<>();

        Node<E> first = before.next;
        if (first == trailer) {
            return newList;
        }
        Node<E> last = trailer.prev;

        int count = 0;
        for (Node<E> n = first; n != trailer; n = n.next) {
            n.owner = newList;
            count++;
        }

        before.next = trailer;
        trailer.prev = before;

        newList.header.next = first;
        first.prev = newList.header;
        last.next = newList.trailer;
        newList.trailer.prev = last;

        this.size -= count;
        newList.size = count;
        this.modCount++;
        newList.modCount++;

        return newList;
    }

    @Override
    public void appendAll(PositionalList<E> other) {
        if (!(other instanceof LinkedPositionalList<?>))
            throw new IllegalArgumentException("other must be a LinkedPositionalList");

        LinkedPositionalList<E> list = (LinkedPositionalList<E>) other;

        if (list == this || list.size == 0)
            return;

        Node<E> last = trailer.prev;
        Node<E> firstOther = list.header.next;
        Node<E> lastOther = list.trailer.prev;
        int count = list.size;

        for (Node<E> n = firstOther; n != list.trailer; n = n.next)
            n.owner = this;

        last.next = firstOther;
        firstOther.prev = last;
        lastOther.next = trailer;
        trailer.prev = lastOther;

        list.header.next = list.trailer;
        list.trailer.prev = list.header;

        size += count;
        list.size = 0;
        modCount++;
        list.modCount++;
    }

    // ------------------------------------------------------------ iteration

    @Override
    public Iterator<E> iterator() {
        return new ElementIterator();
    }

    @Override
    public Iterable<Position<E>> positions() {
        return new Iterable<Position<E>>() {
            @Override
            public Iterator<Position<E>> iterator() {
                return new PositionIterator();
            }
        };
    }

    /**
     * Walks the positions from first to last; remove() removes the last one
     * returned.
     */
    private class PositionIterator implements Iterator<Position<E>> {
        private Node<E> cursor = header.next; // node that next() will return
        private Node<E> recent = null; // node returned by the last next()
        private int expectedModCount = modCount;

        /** Part E (optional): fail-fast check. */
        private void checkForChanges() {
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException("the list was changed during iteration");
        }

        @Override
        public boolean hasNext() {
            return cursor != trailer;
        }

        @Override
        public Position<E> next() {
            checkForChanges();
            if (!hasNext())
                throw new NoSuchElementException("no more positions");

            recent = cursor;
            cursor = cursor.next;
            return recent;
        }

        @Override
        public void remove() {
            checkForChanges();
            if (recent == null)
                throw new IllegalStateException("next() has not been called, or remove() was already called");

            LinkedPositionalList.this.remove(recent);
            recent = null;
            expectedModCount = modCount;
        }

    }

    /** Same walk as PositionIterator, but returns the elements. */
    private class ElementIterator implements Iterator<E> {
        private final Iterator<Position<E>> walk = new PositionIterator();

        @Override
        public boolean hasNext() {
            return walk.hasNext();
        }

        @Override
        public E next() {
            return walk.next().getElement();
        }

        @Override
        public void remove() {
            walk.remove();
        }
    }
}
