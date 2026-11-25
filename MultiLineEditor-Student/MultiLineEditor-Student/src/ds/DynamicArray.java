package ds;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Index-based list on a dynamic array.
 *
 * Rules (checked by SelfCheck):
 *  - a new list has capacity MIN_CAPACITY (4);
 *  - when an element is added to a full array, the capacity doubles;
 *  - after a removal, if size <= capacity / 4 and capacity > MIN_CAPACITY,
 *    the capacity is halved (it never goes below MIN_CAPACITY).
 */
public class DynamicArray<E> implements IndexList<E> {

    public static final int MIN_CAPACITY = 4;

    private E[] data;   // data[0 .. size-1] hold the elements
    private int size;

    @SuppressWarnings("unchecked")
    public DynamicArray() {
        data = (E[]) new Object[MIN_CAPACITY];
        size = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /** Length of the internal array (shown in the editor's status bar). */
    public int capacity() {
        return data.length;
    }

    @Override
    public E get(int i) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public E set(int i, E e) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void add(int i, E e) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void addLast(E e) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public E remove(int i) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    /** Moves the elements to a new array of the given length. */
    @SuppressWarnings("unchecked")
    private void resize(int newCapacity) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    /** Throws IndexOutOfBoundsException unless 0 <= i < n. */
    private static void checkIndex(int i, int n) {
        if (i < 0 || i >= n) {
            throw new IndexOutOfBoundsException("index " + i + " outside [0, " + n + ")");
        }
    }

    /** Iterates from index 0 to size()-1. */
    @Override
    public Iterator<E> iterator() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    /** remove() deletes the element returned by the last call to next(). */
    private class ArrayIterator implements Iterator<E> {
        private int next = 0;              // index of the element next() will return
        private boolean canRemove = false; // true right after next()

        @Override
        public boolean hasNext() {
            // TODO
            throw new UnsupportedOperationException("not implemented yet");
        }

        @Override
        public E next() {
            // TODO
            throw new UnsupportedOperationException("not implemented yet");
        }

        @Override
        public void remove() {
            // TODO
            throw new UnsupportedOperationException("not implemented yet");
        }
    }
}
