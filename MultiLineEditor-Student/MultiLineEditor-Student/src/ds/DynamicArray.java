package ds;

import java.util.Iterator;

/**
 * Index-based list on a dynamic array.
 *
 * Rules (checked by SelfCheck):
 * - a new list has capacity MIN_CAPACITY (4);
 * - when an element is added to a full array, the capacity doubles;
 * - after a removal, if size <= capacity / 4 and capacity > MIN_CAPACITY,
 * the capacity is halved (it never goes below MIN_CAPACITY).
 */
public class DynamicArray<E> implements IndexList<E> {

    public static final int MIN_CAPACITY = 4;

    private E[] data; // data[0 .. size-1] hold the elements
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
        checkIndex(i, size);
        return data[i];
    }

    @Override
    public E set(int i, E e) {
        checkIndex(i, size);
        E cellValue = data[i];
        data[i] = e;
        return cellValue;
    }

    @Override
    public void add(int index, E e) {
        checkIndex(index, size + 1);

        if (size == data.length) {
            resize(data.length * 2);

        }
        for (int i = size - 1; i >= index; i--) {
            data[i + 1] = data[i];
        }
        data[index] = e;
        size++;
    }

    @Override
    public void addLast(E e) {
        if (size == data.length) {
            resize(data.length * 2);
        }
        data[size] = e;
        size++;
    }

    @Override
    public E remove(int index) {
        checkIndex(index, size);
        E cellData = data[index];

        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
        }
        data[size - 1] = null;
        size--;

        if (size <= data.length / 4 && data.length > 7) {
            resize(data.length / 2);
        }
        return cellData;
    }

    /** Moves the elements to a new array of the given length. */
    @SuppressWarnings("unchecked")
    private void resize(int newCapacity) {
        E[] newData = (E[]) new Object[newCapacity];

        for (int i = 0; i < size; i++) {
            newData[i] = data[i];
        }
        data = newData;
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
        return new ArrayIterator();
    }

    /** remove() deletes the element returned by the last call to next(). */
    private class ArrayIterator implements Iterator<E> {

        private int next = 0;
        private int lastReturned = -1;

        @Override
        public boolean hasNext() {
            return next < size;
        }

        @Override
        public E next() {
            if (!hasNext())
                throw new NoSuchElementException();

            lastReturned = next;
            return data[next++];
        }

        @Override
        public void remove() {
            if (!canRemove)
                return;
            next--;
            DynamicArray.this.remove(next);
        }
    }
}
