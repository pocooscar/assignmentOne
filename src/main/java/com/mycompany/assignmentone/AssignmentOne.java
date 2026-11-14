package com.mycompany.assignmentone;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author OSCAR THE SOFTWARE G
 */
public class AssignmentOne {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
                // keep Swing's default look and feel
            }
            new ChatAppGUI().setVisible(true);
        });
    }
}
