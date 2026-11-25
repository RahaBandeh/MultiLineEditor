package check;

import ds.DynamicArray;
import ds.LinkedPositionalList;
import ds.Position;
import ds.PositionalList;
import editor.Editor;
import editor.TextEditor;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Self-check for the assignment. Run it (Run > "Self Check") and read the report.
 *   [PASS]  the test passed
 *   [FAIL]  the test failed; the reason is printed
 *   [TODO]  a method used by the test still throws UnsupportedOperationException
 * Part E is optional: its failures are reported as [ -- ] and do not count.
 */
public class SelfCheck {

    private interface Test {
        void run() throws Exception;
    }

    private static final String[] PARTS = {"A", "B", "C", "E"};
    private static final String[] TITLES = {
        "Part A  DynamicArray", "Part B  LinkedPositionalList", "Part C  TextEditor", "Part E  fail-fast (optional)"};
    private static final int[] passed = new int[4];
    private static final int[] total = new int[4];

    public static void main(String[] args) {
        partA();
        partB();
        partC();
        partE();
        System.out.println();
        System.out.println("================ summary ================");
        for (int k = 0; k < 4; k++) {
            System.out.printf("%-32s %2d / %2d%n", TITLES[k], passed[k], total[k]);
        }
    }

    // ================================================================ Part A

    private static void partA() {
        header(0);
        test("A", "addLast and get keep the order", () -> {
            DynamicArray<Integer> a = new DynamicArray<>();
            for (int i = 0; i < 10; i++) a.addLast(i);
            eq(10, a.size(), "size");
            for (int i = 0; i < 10; i++) eq(i, a.get(i), "get(" + i + ")");
        });
        test("A", "add(i, e) shifts later elements right", () -> {
            DynamicArray<String> a = list("a", "b", "c");
            a.add(1, "x");
            a.add(0, "first");
            a.add(a.size(), "last");
            eq("[first, a, x, b, c, last]", show(a), "list");
        });
        test("A", "set returns the old element", () -> {
            DynamicArray<String> a = list("a", "b");
            eq("b", a.set(1, "B"), "set(1) return value");
            eq("[a, B]", show(a), "list");
        });
        test("A", "remove(i) returns the element and shifts left", () -> {
            DynamicArray<String> a = list("a", "b", "c", "d");
            eq("b", a.remove(1), "remove(1)");
            eq("a", a.remove(0), "remove(0)");
            eq("[c, d]", show(a), "list");
            eq(2, a.size(), "size");
        });
        test("A", "capacity starts at 4 and doubles when full", () -> {
            DynamicArray<Integer> a = new DynamicArray<>();
            eq(4, a.capacity(), "new capacity");
            for (int i = 0; i < 4; i++) a.addLast(i);
            eq(4, a.capacity(), "capacity with 4 elements");
            a.addLast(4);
            eq(8, a.capacity(), "capacity with 5 elements");
            for (int i = 5; i < 9; i++) a.addLast(i);
            eq(16, a.capacity(), "capacity with 9 elements");
            for (int i = 0; i < 9; i++) eq(i, a.get(i), "element " + i + " after growing");
        });
        test("A", "capacity halves at one quarter, never below 4", () -> {
            DynamicArray<Integer> a = new DynamicArray<>();
            for (int i = 0; i < 16; i++) a.addLast(i);
            while (a.size() > 5) a.remove(a.size() - 1);
            eq(16, a.capacity(), "capacity with 5 elements");
            a.remove(4);
            eq(8, a.capacity(), "capacity with 4 elements");
            a.remove(3);
            a.remove(2);
            eq(4, a.capacity(), "capacity with 2 elements");
            a.remove(1);
            a.remove(0);
            eq(4, a.capacity(), "capacity with 0 elements");
        });
        test("A", "no shrink right after growing (no thrashing)", () -> {
            DynamicArray<Integer> a = new DynamicArray<>();
            for (int i = 0; i < 8; i++) a.addLast(i);
            a.addLast(8);
            eq(16, a.capacity(), "capacity after growing");
            a.remove(8);
            eq(16, a.capacity(), "capacity after one removal");
        });
        test("A", "bad index throws IndexOutOfBoundsException", () -> {
            DynamicArray<String> a = list("a", "b");
            throwsEx(IndexOutOfBoundsException.class, () -> a.get(-1), "get(-1)");
            throwsEx(IndexOutOfBoundsException.class, () -> a.get(2), "get(size)");
            throwsEx(IndexOutOfBoundsException.class, () -> a.set(5, "x"), "set(5)");
            throwsEx(IndexOutOfBoundsException.class, () -> a.add(3, "x"), "add(size+1)");
            throwsEx(IndexOutOfBoundsException.class, () -> a.remove(2), "remove(size)");
        });
        test("A", "iterator visits all elements in order", () -> {
            DynamicArray<String> a = list("x", "y", "z");
            StringBuilder sb = new StringBuilder();
            for (String s : a) sb.append(s);
            eq("xyz", sb.toString(), "for-each");
            Iterator<String> it = a.iterator();
            it.next(); it.next(); it.next();
            check(!it.hasNext(), "hasNext() must be false at the end");
            throwsEx(NoSuchElementException.class, it::next, "next() at the end");
        });
        test("A", "iterator.remove deletes the last returned element", () -> {
            DynamicArray<Integer> a = new DynamicArray<>();
            for (int i = 0; i < 10; i++) a.addLast(i);
            Iterator<Integer> it = a.iterator();
            while (it.hasNext()) if (it.next() % 2 == 1) it.remove();
            eq("[0, 2, 4, 6, 8]", show(a), "list after removing odd numbers");
        });
    }

    // ================================================================ Part B

    private static void partB() {
        header(1);
        test("B", "empty list: first() and last() are null", () -> {
            PositionalList<Character> l = new LinkedPositionalList<>();
            check(l.first() == null && l.last() == null, "first()/last() of an empty list must be null");
            check(l.isEmpty(), "isEmpty()");
        });
        test("B", "addFirst/addLast/first/last/before/after", () -> {
            PositionalList<Character> l = new LinkedPositionalList<>();
            Position<Character> b = l.addLast('b');
            Position<Character> a = l.addFirst('a');
            Position<Character> c = l.addLast('c');
            eq("abc", text(l), "list");
            check(l.first() == a && l.last() == c, "first()/last()");
            check(l.after(a) == b && l.before(c) == b, "after()/before()");
            check(l.before(a) == null && l.after(c) == null, "before(first)/after(last) must be null");
            eq(3, l.size(), "size");
        });
        test("B", "addBefore/addAfter", () -> {
            PositionalList<Character> l = new LinkedPositionalList<>();
            Position<Character> m = l.addLast('m');
            l.addBefore(m, 'k');
            l.addAfter(m, 'p');
            l.addAfter(l.last(), 'z');
            eq("kmpz", text(l), "list");
        });
        test("B", "set returns the old element", () -> {
            PositionalList<Character> l = chars("abc");
            eq('b', l.set(l.after(l.first()), 'B'), "set return value");
            eq("aBc", text(l), "list");
        });
        test("B", "remove makes the position invalid", () -> {
            PositionalList<Character> l = chars("abc");
            Position<Character> b = l.after(l.first());
            eq('b', l.remove(b), "remove return value");
            eq("ac", text(l), "list");
            eq(2, l.size(), "size");
            check(!l.contains(b), "contains(removed) must be false");
            throwsEx(IllegalStateException.class, b::getElement, "getElement() of a removed position");
            throwsEx(IllegalArgumentException.class, () -> l.after(b), "after(removed)");
            throwsEx(IllegalArgumentException.class, () -> l.remove(b), "remove(removed) again");
        });
        test("B", "position of another list is rejected", () -> {
            PositionalList<Character> l1 = chars("ab");
            PositionalList<Character> l2 = chars("cd");
            check(!l1.contains(l2.first()), "contains(position of another list) must be false");
            check(!l1.contains(null), "contains(null) must be false");
            throwsEx(IllegalArgumentException.class, () -> l1.addAfter(l2.first(), 'x'), "addAfter(other list)");
            throwsEx(IllegalArgumentException.class, () -> l1.before(null), "before(null)");
        });
        test("B", "positions() walks all positions in order", () -> {
            PositionalList<Character> l = chars("hey");
            StringBuilder sb = new StringBuilder();
            for (Position<Character> p : l.positions()) sb.append(p.getElement());
            eq("hey", sb.toString(), "positions()");
        });
        test("B", "iterator.remove removes the last returned element", () -> {
            PositionalList<Character> l = chars("data structures");
            Iterator<Character> it = l.iterator();
            while (it.hasNext()) if ("aeiou".indexOf(it.next()) >= 0) it.remove();
            eq("dt strctrs", text(l), "list without vowels");
            eq(10, l.size(), "size");
        });
        test("B", "splitAfter(p) moves the tail to a new list", () -> {
            PositionalList<Character> l = chars("hello world");
            Position<Character> o = l.first();
            for (int k = 0; k < 4; k++) o = l.after(o);            // the 'o' of hello
            Position<Character> w = l.after(l.after(o));            // the 'w'
            PositionalList<Character> rest = l.splitAfter(o);
            eq("hello", text(l), "first part");
            eq(" world", text(rest), "second part");
            eq(5, l.size(), "size of first part");
            eq(6, rest.size(), "size of second part");
            check(rest.contains(w) && !l.contains(w), "a moved position must belong to the new list");
            eq('w', w.getElement(), "moved position keeps its element");
            check(l.last() == o, "last() of the first part");
        });
        test("B", "splitAfter(null) and splitAfter(last)", () -> {
            PositionalList<Character> l = chars("abc");
            PositionalList<Character> empty = l.splitAfter(l.last());
            check(empty.isEmpty() && empty.first() == null, "splitAfter(last) must return an empty list");
            eq("abc", text(l), "list after splitAfter(last)");
            PositionalList<Character> all = l.splitAfter(null);
            eq("abc", text(all), "splitAfter(null) moves everything");
            check(l.isEmpty() && l.first() == null && l.last() == null, "the old list must be empty");
            l.addLast('x');
            eq("x", text(l), "the emptied list still works");
        });
        test("B", "appendAll moves all elements and keeps positions", () -> {
            PositionalList<Character> l1 = chars("ab");
            PositionalList<Character> l2 = chars("cd");
            Position<Character> c = l2.first();
            l1.appendAll(l2);
            eq("abcd", text(l1), "joined list");
            check(l2.isEmpty() && l2.first() == null, "the other list must be empty");
            eq(4, l1.size(), "size");
            check(l1.contains(c) && !l2.contains(c), "a moved position must belong to this list");
            check(l1.before(c) == l1.after(l1.first()), "links around the joint");
            l1.appendAll(new LinkedPositionalList<>());
            eq("abcd", text(l1), "appending an empty list");
            l2.addLast('e');
            l1.appendAll(l2);
            eq("abcde", text(l1), "appending again");
        });
    }

    // ================================================================ Part C

    private static void partC() {
        header(2);
        test("C", "a new editor has one empty line", () -> {
            Editor e = new TextEditor();
            eq(1, e.lineCount(), "lineCount");
            eq("", e.lineText(0), "lineText(0)");
            eq("0,0", cursor(e), "cursor");
            eq("", e.getText(), "getText");
        });
        test("C", "typing inserts before the cursor", () -> {
            Editor e = new TextEditor();
            type(e, "hello");
            eq("hello", e.lineText(0), "line 0");
            eq("0,5", cursor(e), "cursor");
            e.moveLeft();
            e.moveLeft();
            type(e, "XY");
            eq("helXYlo", e.lineText(0), "line 0 after typing in the middle");
            eq("0,5", cursor(e), "cursor");
        });
        test("C", "setText / getText round trip", () -> {
            Editor e = new TextEditor();
            e.setText("one\r\ntwo\n\nfour");
            eq(4, e.lineCount(), "lineCount");
            eq("", e.lineText(2), "empty line 2");
            eq(4, e.lineLength(3), "lineLength(3)");
            eq("one\ntwo\n\nfour", e.getText(), "getText");
            eq("0,0", cursor(e), "cursor after setText");
        });
        test("C", "Enter splits the line at the cursor", () -> {
            Editor e = editor("hello world");
            e.moveTo(0, 5);
            e.newLine();
            eq("hello| world", lines(e), "lines");
            eq("1,0", cursor(e), "cursor");
            e.moveEnd();
            e.newLine();
            eq("hello| world|", lines(e), "Enter at the end of a line");
            e.moveTo(0, 0);
            e.newLine();
            eq("|hello| world|", lines(e), "Enter at the start of a line");
            eq("1,0", cursor(e), "cursor");
        });
        test("C", "Backspace inside a line and at a line start", () -> {
            Editor e = editor("abc\ndef");
            e.moveTo(0, 2);
            e.backspace();
            eq("ac|def", lines(e), "lines after Backspace inside line 0");
            eq("0,1", cursor(e), "cursor");
            e.moveTo(1, 0);
            e.backspace();
            eq("acdef", lines(e), "lines after joining");
            eq("0,2", cursor(e), "cursor at the joint");
        });
        test("C", "Delete inside a line and at a line end", () -> {
            Editor e = editor("abc\ndef");
            e.moveTo(0, 1);
            e.delete();
            eq("ac|def", lines(e), "lines after Delete inside line 0");
            e.moveEnd();
            e.delete();
            eq("acdef", lines(e), "lines after joining");
            eq("0,2", cursor(e), "cursor");
        });
        test("C", "Backspace at (0,0) and Delete at the very end do nothing", () -> {
            Editor e = editor("ab\ncd");
            e.backspace();
            eq("ab|cd", lines(e), "Backspace at (0,0)");
            e.moveTo(1, 2);
            e.delete();
            eq("ab|cd", lines(e), "Delete at the end of the last line");
            eq("1,2", cursor(e), "cursor");
        });
        test("C", "Left/Right cross line boundaries", () -> {
            Editor e = editor("ab\ncd");
            e.moveTo(1, 0);
            e.moveLeft();
            eq("0,2", cursor(e), "Left at the start of line 1");
            e.moveRight();
            eq("1,0", cursor(e), "Right at the end of line 0");
            e.moveTo(0, 0);
            e.moveLeft();
            eq("0,0", cursor(e), "Left at (0,0)");
            e.moveTo(1, 2);
            e.moveRight();
            eq("1,2", cursor(e), "Right at the very end");
        });
        test("C", "Up/Down keep the column or go to a shorter line's end", () -> {
            Editor e = editor("long line\nab\nanother line");
            e.moveTo(0, 6);
            e.moveDown();
            eq("1,2", cursor(e), "Down to a shorter line");
            e.moveDown();
            eq("2,2", cursor(e), "Down again");
            e.moveTo(2, 7);
            e.moveUp();
            eq("1,2", cursor(e), "Up to a shorter line");
            e.moveUp();
            eq("0,2", cursor(e), "Up again");
            e.moveUp();
            eq("0,2", cursor(e), "Up on line 0");
            e.moveTo(2, 3);
            e.moveDown();
            eq("2,3", cursor(e), "Down on the last line");
        });
        test("C", "Home/End and moveTo clamping", () -> {
            Editor e = editor("abc\nde");
            e.moveTo(0, 2);
            e.moveHome();
            eq("0,0", cursor(e), "Home");
            e.moveEnd();
            eq("0,3", cursor(e), "End");
            e.moveTo(99, 99);
            eq("1,2", cursor(e), "moveTo(99, 99)");
            e.moveTo(-5, -5);
            eq("0,0", cursor(e), "moveTo(-5, -5)");
        });
        test("C", "a bookmark follows its line when lines are added above", () -> {
            Editor e = editor("a\nb\nc");
            e.moveTo(2, 0);
            check(e.addBookmark("x"), "addBookmark must succeed");
            eq(2, e.bookmarkLine("x"), "bookmark line");
            e.moveTo(0, 0);
            e.newLine();
            e.newLine();
            eq(4, e.bookmarkLine("x"), "bookmark line after two new lines above");
            check(e.goToBookmark("x"), "goToBookmark must succeed");
            eq("4,0", cursor(e), "cursor after goToBookmark");
        });
        test("C", "a bookmark survives splitting and joining its line", () -> {
            Editor e = editor("hello world");
            e.moveTo(0, 6);
            check(e.addBookmark("w"), "addBookmark must succeed");
            e.moveTo(0, 3);
            e.newLine();
            eq(1, e.bookmarkLine("w"), "bookmark line after the split");
            check(e.goToBookmark("w"), "goToBookmark");
            eq("1,3", cursor(e), "cursor after goToBookmark");
            e.moveTo(1, 0);
            e.backspace();
            eq(0, e.bookmarkLine("w"), "bookmark line after joining");
            check(e.goToBookmark("w"), "goToBookmark");
            eq("0,6", cursor(e), "cursor after goToBookmark");
        });
        test("C", "bookmark rules: lost, end of line, empty line, rename, remove", () -> {
            Editor e = editor("abc\n\nxy");
            e.moveTo(0, 1);
            e.addBookmark("b");
            e.moveTo(0, 2);
            e.backspace();                                       // deletes 'b'
            eq(-1, e.bookmarkLine("b"), "bookmarkLine of a lost bookmark");
            check(!e.goToBookmark("b"), "goToBookmark of a lost bookmark must be false");
            check(!e.goToBookmark("nothing"), "goToBookmark of an unknown name must be false");
            e.moveTo(1, 0);
            check(!e.addBookmark("empty"), "addBookmark on an empty line must be false");
            check(!e.addBookmark("   "), "addBookmark with a blank name must be false");
            e.moveTo(2, 2);
            check(e.addBookmark(" end "), "addBookmark at the end of a line");
            check(e.goToBookmark("end"), "names are trimmed");
            eq("2,1", cursor(e), "bookmark at a line end is on the last character");
            e.moveTo(0, 0);
            e.addBookmark("end");                                // same name: the bookmark moves
            eq(0, e.bookmarkLine("end"), "an existing name moves the bookmark");
            eq("[b, end]", java.util.Arrays.toString(e.bookmarkNames()), "bookmarkNames");
            check(e.removeBookmark("b"), "removeBookmark");
            check(!e.removeBookmark("b"), "removeBookmark twice");
            eq("[end]", java.util.Arrays.toString(e.bookmarkNames()), "bookmarkNames after removing");
        });
        test("C", "find moves to the next match, with wrap-around", () -> {
            Editor e = editor("cat dog\nthe cat\ncatalog");
            check(e.find("cat"), "find from (0,0)");
            eq("1,4", cursor(e), "first find skips the match at the cursor");
            check(e.find("cat"), "find again");
            eq("2,0", cursor(e), "second find");
            check(e.find("cat"), "find again (wrap-around)");
            eq("0,0", cursor(e), "third find wraps to the top");
            check(!e.find("bird"), "find of a missing word");
            eq("0,0", cursor(e), "cursor does not move when nothing is found");
            check(!e.find("dogthe"), "a match must not cross a line break");
            check(!e.find(""), "find of an empty pattern");
            e.moveTo(2, 6);
            check(e.find("log"), "find on the cursor's line after wrapping");
            eq("2,4", cursor(e), "match before the cursor on the same line");
        });
        test("C", "charCount and wordCount", () -> {
            Editor e = editor("  two words\n\nthree  ");
            eq(18, e.charCount(), "charCount");
            eq(3, e.wordCount(), "wordCount");
            e.setText("");
            eq(0, e.wordCount(), "wordCount of an empty text");
        });
        test("C", "iterating over the editor", () -> {
            Editor e = editor("ab\n\nc");
            StringBuilder sb = new StringBuilder();
            for (char c : e) sb.append(c == '\n' ? '/' : c);
            eq("ab//c", sb.toString(), "characters with '\\n' between lines");
            Iterator<Character> it = editor("x").iterator();
            it.next();
            check(!it.hasNext(), "hasNext() at the end");
            throwsEx(NoSuchElementException.class, it::next, "next() at the end");
        });
        test("C", "lines live in the dynamic array (capacity grows and shrinks)", () -> {
            Editor e = new TextEditor();
            eq(4, e.lineCapacity(), "capacity of a new editor");
            for (int i = 0; i < 20; i++) e.newLine();
            eq(21, e.lineCount(), "lineCount");
            eq(32, e.lineCapacity(), "capacity with 21 lines");
            for (int i = 0; i < 18; i++) e.backspace();
            eq(3, e.lineCount(), "lineCount after 18 Backspaces");
            eq(8, e.lineCapacity(), "capacity with 3 lines");
        });
    }

    // ================================================================ Part E

    private static void partE() {
        header(3);
        test("E", "changing the list during iteration throws", () -> {
            PositionalList<Character> l = chars("abc");
            Iterator<Character> it = l.iterator();
            it.next();
            l.addLast('d');
            throwsEx(ConcurrentModificationException.class, it::next, "next() after addLast");
        });
        test("E", "iterator.remove does not break the iteration", () -> {
            PositionalList<Character> l = chars("abcd");
            Iterator<Character> it = l.iterator();
            it.next();
            it.remove();
            eq('b', it.next(), "next() after remove()");
        });
    }

    // ================================================================ helpers

    private static int currentPart;

    private static void header(int part) {
        currentPart = part;
        System.out.println();
        System.out.println("---- " + TITLES[part] + " ----");
    }

    private static void test(String part, String name, Test t) {
        total[currentPart]++;
        boolean optional = part.equals("E");
        try {
            t.run();
            passed[currentPart]++;
            System.out.println("[PASS] " + name);
        } catch (UnsupportedOperationException e) {
            System.out.println("[TODO] " + name + "  -> not implemented: " + where(e));
        } catch (AssertionError e) {
            System.out.println((optional ? "[ -- ] " : "[FAIL] ") + name + "  -> " + e.getMessage());
        } catch (Throwable e) {
            System.out.println((optional ? "[ -- ] " : "[FAIL] ") + name + "  -> "
                + e.getClass().getSimpleName() + ": " + e.getMessage() + "  at " + where(e));
        }
    }

    /** The first stack frame inside the student's packages. */
    private static String where(Throwable e) {
        for (StackTraceElement s : e.getStackTrace()) {
            String c = s.getClassName();
            if (c.startsWith("ds.") || c.startsWith("editor.")) {
                String simple = c.substring(c.lastIndexOf('.') + 1);
                return simple + "." + s.getMethodName() + "() line " + s.getLineNumber();
            }
        }
        return "?";
    }

    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    private static void eq(Object expected, Object actual, String what) {
        boolean same = (expected == null) ? actual == null : expected.equals(actual);
        if (!same) throw new AssertionError(what + ": expected <" + expected + "> but got <" + actual + ">");
    }

    private static void throwsEx(Class<? extends Throwable> type, Runnable r, String what) {
        try {
            r.run();
        } catch (UnsupportedOperationException e) {
            throw e;                                         // not implemented: report as TODO
        } catch (Throwable e) {
            if (type.isInstance(e)) return;
            throw new AssertionError(what + ": expected " + type.getSimpleName()
                + " but got " + e.getClass().getSimpleName());
        }
        throw new AssertionError(what + ": expected " + type.getSimpleName() + " but nothing was thrown");
    }

    @SafeVarargs
    private static <T> DynamicArray<T> list(T... items) {
        DynamicArray<T> a = new DynamicArray<>();
        for (T x : items) a.addLast(x);
        return a;
    }

    private static <T> String show(DynamicArray<T> a) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < a.size(); i++) sb.append(i == 0 ? "" : ", ").append(a.get(i));
        return sb.append("]").toString();
    }

    private static PositionalList<Character> chars(String s) {
        PositionalList<Character> l = new LinkedPositionalList<>();
        for (char c : s.toCharArray()) l.addLast(c);
        return l;
    }

    /** Text of a positional list, walking with first()/after() only. */
    private static String text(PositionalList<Character> l) {
        StringBuilder sb = new StringBuilder();
        for (Position<Character> p = l.first(); p != null; p = l.after(p)) sb.append(p.getElement());
        return sb.toString();
    }

    private static Editor editor(String s) {
        Editor e = new TextEditor();
        e.setText(s);
        return e;
    }

    private static void type(Editor e, String s) {
        for (char c : s.toCharArray()) e.insertChar(c);
    }

    private static String cursor(Editor e) {
        return e.cursorLine() + "," + e.cursorColumn();
    }

    private static String lines(Editor e) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < e.lineCount(); i++) sb.append(i == 0 ? "" : "|").append(e.lineText(i));
        return sb.toString();
    }
}
