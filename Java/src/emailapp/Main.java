package emailapp;

import emailapp.ui.EmailAdminUI;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(EmailAdminUI::new);
    }
}
