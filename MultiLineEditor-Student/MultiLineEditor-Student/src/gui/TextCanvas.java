package gui;

import editor.Editor;

import javax.swing.JComponent;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Draws the document: line numbers, text, cursor, bookmark marks and the last search match.
 * It only reads the editor through the Editor interface.
 */
class TextCanvas extends JComponent implements Scrollable {

    private static final long serialVersionUID = 1L;

    static final Color BG = new Color(0xFBFBF8);
    static final Color GUTTER_BG = new Color(0xEEF1F5);
    static final Color GUTTER_FG = new Color(0x8A93A0);
    static final Color TEXT = new Color(0x1E2430);
    static final Color CURRENT_LINE = new Color(0xEAF2FF);
    static final Color CARET = new Color(0x1F5FD6);
    static final Color MATCH = new Color(0xFFE08A);
    static final Color MARK = new Color(0xE0533D);
    static final Color ERROR = new Color(0xB3261E);

    private final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 16);
    private final Font small = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    private Editor editor;
    private String paintError;           // message when the editor cannot be drawn
    private int matchLine = -1, matchCol, matchLen;

    TextCanvas() {
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);   // let the editor receive Tab
        setOpaque(true);
    }

    private String noEditorReason = "";

    void setEditor(Editor editor) {
        setEditor(editor, "");
    }

    void setEditor(Editor editor, String reason) {
        this.editor = editor;
        this.noEditorReason = reason;
        clearMatch();
    }

    void setMatch(int line, int col, int len) {
        matchLine = line;
        matchCol = col;
        matchLen = len;
    }

    void clearMatch() {
        matchLine = -1;
    }

    int charWidth() {
        return getFontMetrics(font).charWidth('m');
    }

    int lineHeight() {
        return getFontMetrics(font).getHeight() + 4;
    }

    int gutterWidth() {
        return 16 + getFontMetrics(small).stringWidth("0000") + 10;
    }

    /** Text column for an x coordinate (used for mouse clicks). */
    int columnAt(int x) {
        return Math.max(0, Math.round((x - gutterWidth() - 8) / (float) charWidth()));
    }

    int lineAt(int y) {
        return Math.max(0, (y - 6) / lineHeight());
    }

    /** Recomputes the preferred size from the text and shows the cursor. */
    void textChanged() {
        int lines = 1, longest = 0;
        try {
            lines = editor.lineCount();
            for (int i = 0; i < lines; i++) longest = Math.max(longest, editor.lineLength(i));
        } catch (RuntimeException e) {
            // drawn as an error by paintComponent
        }
        setPreferredSize(new Dimension(gutterWidth() + 40 + longest * charWidth(), 12 + lines * lineHeight()));
        revalidate();
        try {
            int y = 6 + editor.cursorLine() * lineHeight();
            int x = gutterWidth() + 8 + editor.cursorColumn() * charWidth();
            scrollRectToVisible(new Rectangle(x - 40, y - 4, 80, lineHeight() + 8));
        } catch (RuntimeException e) {
            // ignored: cursor methods not ready yet
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Rectangle clip = g.getClipBounds() != null ? g.getClipBounds() : new Rectangle(0, 0, getWidth(), getHeight());
        g.setColor(BG);
        g.fill(clip);
        int gw = gutterWidth(), lh = lineHeight(), cw = charWidth();
        g.setColor(GUTTER_BG);
        g.fillRect(0, clip.y, gw, clip.height);
        if (editor == null) {
            drawError(g, gw, noEditorReason.isEmpty() ? "no editor" : noEditorReason);
            return;
        }
        try {
            int n = editor.lineCount();
            int curLine = editor.cursorLine();
            int curCol = editor.cursorColumn();
            boolean[] marked = bookmarkedLines(n);
            FontMetrics fm = g.getFontMetrics(font);
            int first = Math.max(0, (clip.y - 6) / lh), last = Math.min(n - 1, (clip.y + clip.height) / lh + 1);
            for (int i = first; i <= last; i++) {
                int top = 6 + i * lh;
                if (i == curLine) {
                    g.setColor(CURRENT_LINE);
                    g.fillRect(gw, top, Math.max(getWidth(), clip.x + clip.width) - gw, lh);
                }
                if (i == matchLine) {
                    g.setColor(MATCH);
                    g.fillRect(gw + 8 + matchCol * cw, top + 1, matchLen * cw, lh - 2);
                }
                g.setFont(small);
                g.setColor(GUTTER_FG);
                String num = String.valueOf(i + 1);
                g.drawString(num, gw - 8 - g.getFontMetrics().stringWidth(num), top + lh - 7);
                if (marked[i]) {
                    g.setColor(MARK);
                    g.fillOval(5, top + lh / 2 - 5, 10, 10);
                }
                g.setFont(font);
                g.setColor(TEXT);
                g.drawString(editor.lineText(i), gw + 8, top + lh - 4 - fm.getDescent());
            }
            g.setColor(CARET);
            int cx = gw + 8 + curCol * cw, cy = 6 + curLine * lh;
            g.fillRect(cx - 1, cy + 2, 2, lh - 4);
            paintError = null;
        } catch (RuntimeException e) {
            drawError(g, gw, EditorView.describe(e));
        }
    }

    private boolean[] bookmarkedLines(int n) {
        boolean[] marked = new boolean[n];
        try {
            for (String name : editor.bookmarkNames()) {
                int line = editor.bookmarkLine(name);
                if (line >= 0 && line < n) marked[line] = true;
            }
        } catch (RuntimeException e) {
            // bookmarks not ready yet: draw no marks
        }
        return marked;
    }

    private void drawError(Graphics2D g, int gw, String message) {
        paintError = message;
        g.setFont(small.deriveFont(Font.BOLD, 14f));
        g.setColor(ERROR);
        g.drawString("Cannot draw the text:", gw + 12, 30);
        g.drawString(message, gw + 12, 52);
    }

    String paintError() {
        return paintError;
    }

    // ------------------------------------------------------------ Scrollable

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(760, 460);
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? lineHeight() : charWidth();
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? r.height - lineHeight() : r.width / 2;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getParent() != null && getParent().getWidth() > getPreferredSize().width;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return getParent() != null && getParent().getHeight() > getPreferredSize().height;
    }
}
