package editor;

/**
 * What the window (package gui) needs from the editor core. Do not change this file.
 *
 * Text model: the document has at least one line. Lines and columns start at 0.
 * The cursor sits BETWEEN characters: column c means "before the c-th character of the line";
 * column == lineLength(line) means "at the end of the line".
 *
 * Iteration (for-each over an Editor) returns every character of the document in order,
 * with '\n' between two lines (no '\n' after the last line).
 */
public interface Editor extends Iterable<Character> {

    // ------------------------------------------------------------ reading the text

    /** Number of lines (always at least 1). */
    int lineCount();

    /** Capacity of the dynamic array that holds the lines (shown in the status bar). */
    int lineCapacity();

    /** Text of line i, without '\n'. */
    String lineText(int i);

    /** Number of characters in line i. */
    int lineLength(int i);

    /** Line of the cursor. */
    int cursorLine();

    /** Column of the cursor (0 .. lineLength(cursorLine())). */
    int cursorColumn();

    // ------------------------------------------------------------ moving the cursor

    /** One character left; at the start of a line, go to the end of the previous line. */
    void moveLeft();

    /** One character right; at the end of a line, go to the start of the next line. */
    void moveRight();

    /** One line up, same column, or the end of that line if it is shorter. No-op on line 0. */
    void moveUp();

    /** One line down, same column, or the end of that line if it is shorter. No-op on the last line. */
    void moveDown();

    /** Start of the current line. */
    void moveHome();

    /** End of the current line. */
    void moveEnd();

    /** Moves to (line, column). Out-of-range values are clamped to the nearest valid place. */
    void moveTo(int line, int column);

    // ------------------------------------------------------------ editing

    /** Inserts c before the cursor; the cursor ends up after c. '\n' acts like newLine(). */
    void insertChar(char c);

    /** Enter: the text after the cursor moves to a new line below; the cursor goes to its start. */
    void newLine();

    /** Removes the character before the cursor; at the start of a line, joins it to the previous line. */
    void backspace();

    /** Removes the character after the cursor; at the end of a line, joins the next line to this one. */
    void delete();

    // ------------------------------------------------------------ bookmarks

    /**
     * Puts a bookmark with this name on the character just after the cursor
     * (at the end of a line: on the character just before the cursor).
     * If the name exists, that bookmark moves here. Names are trimmed.
     * Returns false if the name is empty or the line is empty.
     */
    boolean addBookmark(String name);

    /** Removes the bookmark; false if there is no such bookmark. */
    boolean removeBookmark(String name);

    /**
     * Moves the cursor just before the bookmarked character.
     * Returns false if there is no such bookmark, or its character was deleted (bookmark lost).
     */
    boolean goToBookmark(String name);

    /** Names of all bookmarks, in the order they were first added. */
    String[] bookmarkNames();

    /** Current line of the bookmarked character, or -1 if the bookmark does not exist or is lost. */
    int bookmarkLine(String name);

    // ------------------------------------------------------------ search and statistics

    /**
     * Finds the next occurrence of pattern that starts after the cursor position,
     * continuing from the top of the document if needed (wrap-around).
     * A match never crosses a line break. On success the cursor moves to the start of
     * the match and true is returned; otherwise nothing changes and false is returned.
     */
    boolean find(String pattern);

    /** Number of characters, not counting line breaks. */
    int charCount();

    /** Number of words: maximal runs of non-whitespace characters. */
    int wordCount();

    // ------------------------------------------------------------ whole text

    /** The whole document as one string, lines separated by '\n'. */
    String getText();

    /** Replaces the document with text ('\r' is ignored). Bookmarks are cleared; cursor at (0, 0). */
    void setText(String text);
}
