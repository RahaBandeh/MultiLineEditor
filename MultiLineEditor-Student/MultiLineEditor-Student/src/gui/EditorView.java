package gui;

import editor.Editor;
import editor.TextEditor;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * The whole window content: toolbar, text, bookmark list and status bar.
 * Every call into the editor goes through run(...), so a method that is not written yet
 * shows a message in the status bar instead of closing the program.
 */
public class EditorView extends JPanel {

    private static final long serialVersionUID = 1L;

    static final String SAMPLE =
        "Data Structures - lecture notes\n"
        + "\n"
        + "1. Dynamic array: get(i) is fast, insert in the middle is slow.\n"
        + "2. Positional list: insert next to a position is fast.\n"
        + "3. An iterator walks a structure without showing how it is built.\n"
        + "\n"
        + "TODO: compare a dynamic array and a linked list for a text editor.\n"
        + "Each line of this editor is a positional list of characters,\n"
        + "and the lines are kept in a dynamic array.";

    private Editor editor;
    private final TextCanvas canvas = new TextCanvas();
    private final JLabel position = new JLabel();
    private final JLabel stats = new JLabel();
    private final JLabel message = new JLabel(" ");
    private final DefaultListModel<String> bookmarkModel = new DefaultListModel<>();
    private final JList<String> bookmarkList = new JList<>(bookmarkModel);
    private String[] bookmarkNames = new String[0];
    private String lastPattern = "";
    private String noEditorReason = "";     // why the TextEditor constructor failed

    public EditorView() {
        super(new BorderLayout());
        add(toolbar(), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(canvas);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, new Color(0xD5DAE1)));
        add(scroll, BorderLayout.CENTER);
        add(bookmarkPanel(), BorderLayout.EAST);
        add(statusBar(), BorderLayout.SOUTH);
        installKeys();
        installMouse();
        newDocument();
    }

    /** Text canvas, for tests that send key events. */
    public TextCanvas canvas() {
        return canvas;
    }

    // ------------------------------------------------------------ layout

    private JToolBar toolbar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.add(button("New", "New empty document (Ctrl+N)", this::newDocument));
        bar.add(button("Open...", "Open a text file (Ctrl+O)", this::openFile));
        bar.add(button("Save...", "Save with getText() (Ctrl+S)", this::saveFile));
        bar.add(button("Sample", "Load a sample text with setText()", () -> loadText(SAMPLE)));
        bar.addSeparator();
        bar.add(button("Bookmark...", "Bookmark the character at the cursor (Ctrl+B)", this::askBookmark));
        bar.add(button("Find...", "Find text (Ctrl+F)", this::askFind));
        bar.add(button("Find next", "Find the same text again (F3)", this::findNext));
        return bar;
    }

    private JButton button(String text, String tip, Runnable action) {
        JButton b = new JButton(text);
        b.setToolTipText(tip);
        b.setFocusable(false);
        b.addActionListener(e -> {
            action.run();
            canvas.requestFocusInWindow();
        });
        return b;
    }

    private JPanel bookmarkPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.setPreferredSize(new Dimension(210, 100));
        JLabel title = new JLabel("Bookmarks");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        p.add(title, BorderLayout.NORTH);
        bookmarkList.setToolTipText("Double-click to jump to a bookmark");
        bookmarkList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) jumpToSelected();
            }
        });
        p.add(new JScrollPane(bookmarkList), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new GridLayout(1, 2, 6, 0));
        buttons.add(button("Go to", "Move the cursor to the selected bookmark", this::jumpToSelected));
        buttons.add(button("Remove", "Remove the selected bookmark", this::removeSelected));
        p.add(buttons, BorderLayout.SOUTH);
        return p;
    }

    private JPanel statusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JPanel left = new JPanel(new GridLayout(1, 2, 16, 0));
        left.add(position);
        left.add(stats);
        p.add(left, BorderLayout.WEST);
        message.setHorizontalAlignment(JLabel.RIGHT);
        p.add(message, BorderLayout.CENTER);
        return p;
    }

    // ------------------------------------------------------------ input

    private void installKeys() {
        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                boolean ctrl = e.isControlDown() || e.isMetaDown();
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:       run("Left", () -> editor.moveLeft()); break;
                    case KeyEvent.VK_RIGHT:      run("Right", () -> editor.moveRight()); break;
                    case KeyEvent.VK_UP:         run("Up", () -> editor.moveUp()); break;
                    case KeyEvent.VK_DOWN:       run("Down", () -> editor.moveDown()); break;
                    case KeyEvent.VK_HOME:       run("Home", () -> editor.moveHome()); break;
                    case KeyEvent.VK_END:        run("End", () -> editor.moveEnd()); break;
                    case KeyEvent.VK_ENTER:      edit("Enter", () -> editor.newLine()); break;
                    case KeyEvent.VK_BACK_SPACE: edit("Backspace", () -> editor.backspace()); break;
                    case KeyEvent.VK_DELETE:     edit("Delete", () -> editor.delete()); break;
                    case KeyEvent.VK_TAB:        edit("Tab", () -> typeText("    ")); break;
                    case KeyEvent.VK_F3:         findNext(); break;
                    default:
                        if (ctrl) shortcut(e.getKeyCode());
                        return;
                }
                e.consume();
            }

            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (c >= 32 && c != 127 && !e.isControlDown() && !e.isMetaDown()) {
                    edit("Typing", () -> editor.insertChar(c));
                }
            }
        });
    }

    private void shortcut(int key) {
        switch (key) {
            case KeyEvent.VK_N: newDocument(); break;
            case KeyEvent.VK_O: openFile(); break;
            case KeyEvent.VK_S: saveFile(); break;
            case KeyEvent.VK_B: askBookmark(); break;
            case KeyEvent.VK_F: askFind(); break;
            default: break;
        }
    }

    private void installMouse() {
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                canvas.requestFocusInWindow();
                int line = canvas.lineAt(e.getY()), col = canvas.columnAt(e.getX());
                run("Mouse click", () -> editor.moveTo(line, col));
            }
        });
    }

    // ------------------------------------------------------------ actions

    private void newDocument() {
        try {
            editor = new TextEditor();
            canvas.setEditor(editor);
            lastPattern = "";
            refresh("New document");
        } catch (RuntimeException e) {
            editor = null;
            noEditorReason = "new TextEditor() failed: " + describe(e);
            canvas.setEditor(null, noEditorReason);
            showError(noEditorReason);
        }
    }

    private void loadText(String text) {
        run("setText", () -> editor.setText(text.replace("\t", "    ")));
    }

    private void typeText(String s) {
        for (char c : s.toCharArray()) editor.insertChar(c);
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser(new File("."));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            String text = new String(Files.readAllBytes(chooser.getSelectedFile().toPath()), StandardCharsets.UTF_8);
            loadText(text);
        } catch (Exception e) {
            showError("Cannot read the file: " + e.getMessage());
        }
    }

    private void saveFile() {
        if (editor == null) return;
        JFileChooser chooser = new JFileChooser(new File("."));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            String text = editor.getText();
            Files.write(chooser.getSelectedFile().toPath(), text.getBytes(StandardCharsets.UTF_8));
            refresh("Saved " + chooser.getSelectedFile().getName());
        } catch (RuntimeException e) {
            showError(describe(e));
        } catch (Exception e) {
            showError("Cannot write the file: " + e.getMessage());
        }
    }

    private void askBookmark() {
        if (editor == null) return;
        String name = JOptionPane.showInputDialog(this, "Bookmark name:", "Add bookmark", JOptionPane.PLAIN_MESSAGE);
        if (name == null) return;
        addBookmark(name);
    }

    /** Adds a bookmark at the cursor (also used by tests). */
    public void addBookmark(String name) {
        run("Bookmark", () -> {
            if (!editor.addBookmark(name)) {
                throw new IllegalStateException("addBookmark returned false (empty name or empty line)");
            }
        });
    }

    private void askFind() {
        if (editor == null) return;
        String p = (String) JOptionPane.showInputDialog(this, "Find:", "Find", JOptionPane.PLAIN_MESSAGE,
            null, null, lastPattern);
        if (p == null || p.isEmpty()) return;
        find(p);
    }

    private void findNext() {
        if (lastPattern.isEmpty()) askFind();
        else find(lastPattern);
    }

    /** Finds pattern and highlights the match (also used by tests). */
    public void find(String pattern) {
        lastPattern = pattern;
        run("Find", () -> {
            if (editor.find(pattern)) {
                canvas.setMatch(editor.cursorLine(), editor.cursorColumn(), pattern.length());
            } else {
                throw new IllegalStateException("\"" + pattern + "\" not found");
            }
        });
    }

    private void jumpToSelected() {
        int k = bookmarkList.getSelectedIndex();
        if (k < 0 || k >= bookmarkNames.length) return;
        String name = bookmarkNames[k];
        run("Go to bookmark", () -> {
            if (!editor.goToBookmark(name)) {
                throw new IllegalStateException("bookmark \"" + name + "\" is lost (its character was deleted)");
            }
        });
    }

    private void removeSelected() {
        int k = bookmarkList.getSelectedIndex();
        if (k < 0 || k >= bookmarkNames.length) return;
        String name = bookmarkNames[k];
        run("Remove bookmark", () -> editor.removeBookmark(name));
    }

    // ------------------------------------------------------------ calling the editor safely

    /** A change of the text: the search highlight is cleared. */
    private void edit(String what, Runnable action) {
        canvas.clearMatch();
        run(what, action);
    }

    private void run(String what, Runnable action) {
        if (editor == null) {
            showError(noEditorReason + "  (press New to try again)");
            return;
        }
        try {
            action.run();
            refresh(what);
        } catch (IllegalStateException e) {
            refresh(" ");
            showError(e.getMessage());
        } catch (RuntimeException e) {
            refresh(" ");
            showError(what + ": " + describe(e));
        }
    }

    /** Short description of an exception, pointing at the student's code. */
    static String describe(RuntimeException e) {
        String where = "";
        for (StackTraceElement s : e.getStackTrace()) {
            String c = s.getClassName();
            if (c.startsWith("ds.") || c.startsWith("editor.")) {
                where = c.substring(c.lastIndexOf('.') + 1) + "." + s.getMethodName() + "()";
                break;
            }
        }
        if (e instanceof UnsupportedOperationException) {
            return "not implemented yet: " + where;
        }
        return e.getClass().getSimpleName() + (e.getMessage() == null ? "" : " (" + e.getMessage() + ")")
            + (where.isEmpty() ? "" : " in " + where);
    }

    private void refresh(String what) {
        canvas.textChanged();
        updateStatus();
        updateBookmarks();
        message.setForeground(new Color(0x3C4655));
        message.setText(what);
    }

    private void showError(String text) {
        message.setForeground(TextCanvas.ERROR);
        message.setText(text);
    }

    private void updateStatus() {
        try {
            position.setText("Ln " + (editor.cursorLine() + 1) + ", Col " + (editor.cursorColumn() + 1));
        } catch (RuntimeException e) {
            position.setText("Ln ?, Col ?");
        }
        String lines, chars, words;
        try {
            lines = editor.lineCount() + " lines (array capacity " + editor.lineCapacity() + ")";
        } catch (RuntimeException e) {
            lines = "? lines";
        }
        try {
            chars = editor.charCount() + " chars";
        } catch (RuntimeException e) {
            chars = "? chars";
        }
        try {
            words = editor.wordCount() + " words";
        } catch (RuntimeException e) {
            words = "? words";
        }
        stats.setText(lines + "   " + chars + "   " + words);
    }

    private void updateBookmarks() {
        bookmarkModel.clear();
        try {
            bookmarkNames = editor.bookmarkNames();
            for (String name : bookmarkNames) {
                int line = editor.bookmarkLine(name);
                bookmarkModel.addElement(line >= 0 ? name + "   (line " + (line + 1) + ")" : name + "   (lost)");
            }
        } catch (RuntimeException e) {
            bookmarkNames = new String[0];
        }
    }

    /** Status message (for tests). */
    public String statusMessage() {
        return message.getText();
    }

    /** Position and statistics labels (for tests). */
    public String statusText() {
        return position.getText() + " | " + stats.getText();
    }
}
