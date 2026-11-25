package gui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Starts the editor window. Run > "Editor (GUI)". */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // keep the default look
            }
            JFrame frame = new JFrame("Multi-line Text Editor - Data Structures");
            EditorView view = new EditorView();
            frame.setContentPane(view);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1040, 640);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            view.canvas().requestFocusInWindow();
        });
    }
}
