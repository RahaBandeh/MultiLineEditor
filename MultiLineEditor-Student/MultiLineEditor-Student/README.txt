Multi-line Text Editor - Data Structures assignment
====================================================

Open this folder in IntelliJ IDEA (File > Open, choose the folder).
If IntelliJ asks for a JDK: File > Project Structure > Project > SDK, choose any JDK 11 or newer.

Two run configurations are ready (top-right of the IntelliJ window):
  Editor (GUI)   the editor window (class gui.Main)
  Self Check     the automatic tests (class check.SelfCheck)

Packages
  ds       DynamicArray, LinkedPositionalList (and their interfaces)
  editor   Editor (interface, do not change) and TextEditor (the editor core)
  gui      the window - already written, do not change
  check    SelfCheck - already written, do not change

Your task: replace every 'throw new UnsupportedOperationException("not implemented yet")'
in ds/DynamicArray.java, ds/LinkedPositionalList.java and editor/TextEditor.java.
Do not use java.util collections (ArrayList, LinkedList, ...) in packages ds and editor.
Until a method is written, the window shows "not implemented yet: ..." in its status bar.
