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
        return lines.size();
    }

    @Override //shows the number of lines we can have
    public int lineCapacity() {
        return lines.capacity();
    }

    @Override
    public String lineText(int i) {
        StringBuilder currentLine = new  StringBuilder();
        for (Character c : lines.get(i)) { //
            currentLine.append(c);
        }
        return currentLine.toString();
    }

    @Override
    public int lineLength(int i) {
        return lines.get(i).size();
    }

    @Override
    public int cursorLine() {
        return curLine;
    }

    @Override
    public int cursorColumn() {
        if (left == null)
            return 0;

        int count = 0;
        Position<Character> current = line().first();

        while (current != left) {
            count++;
            current = line().after(current);
        }

        return count + 1;

    }


    // ------------------------------------------------------------ moving the cursor

    @Override
    public void moveLeft() {
        if (left == null) {
            if (curLine > 0) {
                curLine--;
                left = lines.get(curLine).last();
            }
        } 
        else {
            left = line().before(left);
        }
    }

    @Override
    public void moveRight() {
        if (left == null) {
            if (!line().isEmpty()) {
                left = line().first();
            } 
            else if (curLine < lines.size() - 1) {
                curLine++;
                left = null;
            }
        } 
        else {
            Position<Character> next = line().after(left);
            if (next != null) {
                left = next;
            } 
            else if (curLine < lines.size() - 1) {
                curLine++;
                left = null;
            }
        }
    }

    @Override
    public void moveUp() {
        if (curLine > 0) {
            int column = cursorColumn();
            curLine--;
            placeAtColumn(column);
        }
    }

    @Override
    public void moveDown() { 
    if (curLine < lines.size() - 1) {
        int column = cursorColumn();
        curLine++;
        placeAtColumn(column);
    }
}

    @Override
    public void moveHome() {
        left = null;
    }

    @Override
    public void moveEnd() {
        left = lines.get(curLine).last();
    }

    @Override
    public void moveTo(int line, int column) {
        if (line < 0) {
            curLine = 0;
        } else if (line >= lines.size()) {
            curLine = lines.size() - 1;
        } else {
            curLine = line;
        }

        placeAtColumn(column);
    }

    /** Puts the cursor at column col of this line (at its end if the line is shorter). */
    private void placeAtColumn(int col) { //col is the number of characters before the cursor
        if (col <= 0) {
            left = null;
            return;
        } 
        if (col > line().size()) {
            left = line().last();
            return;
        }
        Position<Character> current = line().first();
        int count = 0;

        while (count < col) {
            current = line().after(current);
            count++;
        }

        left = current;
        
    }

    // ------------------------------------------------------------ editing

    @Override
    public void insertChar(char c) {
        if (c == '\n') {
            newLine();
        } 
        else {
            if (left == null) {
                left = line().addFirst(c);
            } 
            else {
                left = line().addAfter(left, c);
            }
        }
    }

    @Override
    public void newLine() {
        PositionalList<Character> newLine = line().splitAfter(left);

        lines.add(curLine + 1, newLine);
        curLine++;
        left = null;
    }

    @Override
    public void backspace() {
        if(left == null) {
            if(curLine == 0)
                return;

            Position<Character> willBeLeft = line().last();
            lines.get(curLine - 1).appendAll(line());
            lines.remove(curLine);
            curLine--;
            left = willBeLeft;
        }
        else {
            Position<Character> deleted = left;
            left = line().before(left);
            line().remove(deleted);
        }
    }

    @Override
    public void delete() {
        Position<Character> right =
                    (left == null) ? line().first() : line().after(left);

        if(right == null) { //end of line
            if (curLine == lines.size() - 1)
                return;

                line().appendAll(lines.get(curLine+1));
                lines.remove(curLine+1);
            }
        
        else {
            line().remove(right);
        }
    }

    // ------------------------------------------------------------ bookmarks

    /** Index of the bookmark with this name in the bookmarks array, or -1. */
    private int findBookmark(String name) {
        for (int i = 0; i < bookmarks.size(); i++) {
            if (bookmarks.get(i).name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /** Index of the line that holds position p, or -1 if p is in no line (deleted). */
    private int lineOf(Position<Character> p) {
        for (int i = 0; i < lines.size(); i++) {
            PositionalList<Character> line = lines.get(i);

            Position<Character> current = line.first();

            while (current != null) {
                if (current == p)
                    return i;

                current = line.after(current);
            }
        }
        return -1;
    }

    @Override
    public boolean addBookmark(String name) {
        name = name.trim();

        if (name.isEmpty() || line().isEmpty())
            return false;

        Position<Character> target;

        if (left == null)
            target = line().first();
        else if (line().after(left) == null)
            target = left;
        else
            target = line().after(left);

        int index = findBookmark(name);

        if (index != -1)
            bookmarks.get(index).pos = target;
        else
            bookmarks.addLast(new Bookmark(name, target));

        return true;
    }

    @Override
    public boolean removeBookmark(String name) {
        int index = findBookmark(name);
        if (index != -1) {
            bookmarks.remove(index);
            return true;
        }
        return false;
    }

    @Override
    public boolean goToBookmark(String name) {
        int index = findBookmark(name);

        if (index != -1) {
            Bookmark bookmark = bookmarks.get(index);
            int lineIndex = lineOf(bookmark.pos);

            if (lineIndex != -1) {
                curLine = lineIndex;
                left = line().before(bookmark.pos);
                return true;
            } 
            else {
                bookmarks.remove(index);
            }
        }

        return false;
    }

    @Override
    public String[] bookmarkNames() {
        String[] names = new String[bookmarks.size()];
        for (int i = 0; i < bookmarks.size(); i++) {
            names[i] = bookmarks.get(i).name;
        }
        return names;
    }

    @Override
    public int bookmarkLine(String name) {
        int index = findBookmark(name);
        if (index != -1) {
            Bookmark bookmark = bookmarks.get(index);
            return lineOf(bookmark.pos);
        }
        return -1;
    }

    // ------------------------------------------------------------ search and statistics

    @Override
    public boolean find(String pattern) {
        if (pattern.isEmpty())
            return false;

        int startLine = curLine;
        Position<Character> startPos = left;

        int searchLine = curLine;
        Position<Character> searchStart =
                (left == null) ? line().first() : line().after(left);

        boolean first = true;

        do {
            PositionalList<Character> currentLine = lines.get(searchLine);

            while (searchStart != null) {
                if (matchesAt(currentLine, searchStart, pattern)) {
                    curLine = searchLine;
                    left = currentLine.before(searchStart);
                    return true;
                }

                searchStart = currentLine.after(searchStart);
            }

            searchLine = (searchLine + 1) % lines.size();
            searchStart = lines.get(searchLine).first();

        } while (searchLine != startLine || first);

        return false;
    }

    /** True if pattern appears in line starting at position start. */
    private boolean matchesAt(PositionalList<Character> line, Position<Character> start,
                              String pattern) {
        Position<Character> current = start;
        for (int i = 0; i < pattern.length(); i++) {
            if (current == null || current.getElement() != pattern.charAt(i)) {
                return false;
            }
            current = line.after(current);
        }
        return true;
    }

    @Override
    public int charCount() {
        int count = 0;
        for (int i = 0; i < lines.size(); i++) {
            count += lines.get(i).size();
        }
        return count;
    }

    @Override
    public int wordCount() {
        int count = 0;
        for (int i = 0; i < lines.size(); i++) {
            PositionalList<Character> line = lines.get(i);
            boolean inWord = false;
            for (Character c : line) {
                if (Character.isWhitespace(c)) {
                    inWord = false;
                } else {
                    if (!inWord) {
                        count++;
                        inWord = true;
                    }
                }
            }
        }
        return count;
    }

    // ------------------------------------------------------------ whole text

    @Override
    public String getText() {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < lines.size(); i++) {
            PositionalList<Character> line = lines.get(i);

            for (Character c : line) {
                text.append(c);
            }

            if (i < lines.size() - 1) {
                text.append('\n');
            }
        }

        return text.toString();
    }

    @Override
    public void setText(String text) {
        while (!lines.isEmpty()) {
            lines.remove(lines.size() - 1);
        }

        curLine = 0;
        left = null;

        while (!bookmarks.isEmpty()) {
            bookmarks.remove(bookmarks.size() - 1);
        }

        String[] splitLines = text.split("\n", -1);

        for (String line : splitLines) {
            PositionalList<Character> newLine = new LinkedPositionalList<>();

            for (char c : line.toCharArray()) {
                newLine.addLast(c);
            }

            lines.addLast(newLine);
        }
    }

    @Override
    public Iterator<Character> iterator() {
        return new TextIterator();
    }

    /** Walks line by line; returns '\n' when it passes from one line to the next. */
    private class TextIterator implements Iterator<Character> {
        private int lineIndex = 0;
        private Iterator<Character> inLine = lines.get(0).iterator();

        @Override
        public boolean hasNext() {
            return inLine.hasNext() || lineIndex < lines.size() - 1;
        }

        @Override
        public Character next() {
            if (inLine.hasNext()) {
                return inLine.next();
            }

            if (lineIndex < lines.size() - 1) {
                lineIndex++;
                inLine = lines.get(lineIndex).iterator();
                return '\n';
            }

            throw new NoSuchElementException();
        }
    }
}
