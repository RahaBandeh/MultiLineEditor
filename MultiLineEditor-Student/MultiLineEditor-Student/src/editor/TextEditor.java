package editor;

import ds.DynamicArray;
import ds.LinkedPositionalList;
import ds.Position;
import ds.PositionalList;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Editor core.
 *  - lines:   a DynamicArray, one entry per line (fast jump to line i);
 *  - a line:  a PositionalList of characters (fast insert/delete at the cursor);
 *  - cursor:  the line index plus the position of the character just BEFORE the cursor
 *             (null when the cursor is at the start of the line);
 *  - bookmark: the position of one character, so it follows the text when the text changes.
 */
public class TextEditor implements Editor {

    /** A named bookmark on one character. */
    private static class Bookmark {
        final String name;
        Position<Character> pos;

        Bookmark(String name, Position<Character> pos) {
            this.name = name;
            this.pos = pos;
        }
    }

    private DynamicArray<PositionalList<Character>> lines;
    private int curLine;                    // index of the cursor's line
    private Position<Character> left;       // char before the cursor; null = line start
    private DynamicArray<Bookmark> bookmarks;

    public TextEditor() {
        lines = new DynamicArray<>();
        lines.addLast(new LinkedPositionalList<>());
        curLine = 0;
        left = null;
        bookmarks = new DynamicArray<>();
    }

    /** The cursor's line. */
    private PositionalList<Character> line() {
        return lines.get(curLine);
    }

    // ------------------------------------------------------------ reading the text

    @Override
    public int lineCount() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public int lineCapacity() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public String lineText(int i) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public int lineLength(int i) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public int cursorLine() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public int cursorColumn() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    // ------------------------------------------------------------ moving the cursor

    @Override
    public void moveLeft() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void moveRight() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void moveUp() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void moveDown() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void moveHome() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void moveEnd() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void moveTo(int line, int column) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    /** Puts the cursor at column col of this line (at its end if the line is shorter). */
    private void placeAtColumn(int col) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    // ------------------------------------------------------------ editing

    @Override
    public void insertChar(char c) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void newLine() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void backspace() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void delete() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    // ------------------------------------------------------------ bookmarks

    /** Index of the bookmark with this name in the bookmarks array, or -1. */
    private int findBookmark(String name) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    /** Index of the line that holds position p, or -1 if p is in no line (deleted). */
    private int lineOf(Position<Character> p) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public boolean addBookmark(String name) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public boolean removeBookmark(String name) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public boolean goToBookmark(String name) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public String[] bookmarkNames() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public int bookmarkLine(String name) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    // ------------------------------------------------------------ search and statistics

    @Override
    public boolean find(String pattern) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    /** True if pattern appears in line starting at position start. */
    private boolean matchesAt(PositionalList<Character> line, Position<Character> start,
                              String pattern) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public int charCount() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public int wordCount() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    // ------------------------------------------------------------ whole text

    @Override
    public String getText() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void setText(String text) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public Iterator<Character> iterator() {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
    }

    /** Walks line by line; returns '\n' when it passes from one line to the next. */
    private class TextIterator implements Iterator<Character> {
        private int lineIndex = 0;                                  // line being read
        private Iterator<Character> inLine = lines.get(0).iterator(); // walk in that line

        @Override
        public boolean hasNext() {
            // TODO
            throw new UnsupportedOperationException("not implemented yet");
        }

        @Override
        public Character next() {
            // TODO
            throw new UnsupportedOperationException("not implemented yet");
        }
    }
}
