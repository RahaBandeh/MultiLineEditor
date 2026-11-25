package ds;

/**
 * A place inside a PositionalList. A position stays valid, and keeps pointing to
 * the same element, while other elements are added or removed around it.
 * It becomes invalid only when its own element is removed.
 */
public interface Position<E> {

    /**
     * Returns the element stored at this position.
     * @throws IllegalStateException if the position is no longer valid (it was removed)
     */
    E getElement() throws IllegalStateException;
}
