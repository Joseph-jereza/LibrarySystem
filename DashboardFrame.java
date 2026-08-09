package com.library;

import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {
    private final BookService bookService = new BookService();

    public DashboardFrame() {
        setTitle("JEREZA LIBRARY MANAGEMENT SYSTEM");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Header Title ---
        JLabel headerLabel = new JLabel("JEREZA LIBRARY SYSTEM", SwingConstants.CENTER);
        headerLabel.setOpaque(true);
        headerLabel.setBackground(new Color(40, 50, 60)); // Dark Gray / Navy
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setPreferredSize(new Dimension(600, 50));
        add(headerLabel, BorderLayout.NORTH);

        // --- Buttons Panel (2 Columns, 3 Rows Grid) ---
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create Styled Buttons
        JButton btnAddBook = createStyledButton("Add New Book");
        JButton btnRegisterUser = createStyledButton("Register User");
        JButton btnViewBooks = createStyledButton("View All Books");
        JButton btnBorrowBook = createStyledButton("Borrow Book");
        JButton btnReturnBook = createStyledButton("Return Book");
        JButton btnExit = createStyledButton("Exit System");

        // --- Button Actions ---
        btnAddBook.addActionListener(e -> {
            String isbn = JOptionPane.showInputDialog(this, "Enter Book ISBN:");
            String title = JOptionPane.showInputDialog(this, "Enter Book Title:");
            String author = JOptionPane.showInputDialog(this, "Enter Book Author:");
            String category = JOptionPane.showInputDialog(this, "Enter Category (e.g. Fiction, Tech):");
            String copiesStr = JOptionPane.showInputDialog(this, "Enter Number of Copies:");
            
            if (isbn != null && title != null && author != null && category != null && copiesStr != null) {
                try {
                    int copies = Integer.parseInt(copiesStr.trim());
                    bookService.addBook(isbn, title, author, category, copies);
                    JOptionPane.showMessageDialog(this, "Book added successfully!");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid number for copies!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnViewBooks.addActionListener(e -> {
            String isbn = JOptionPane.showInputDialog(this, "Search Book by ISBN:");
            if (isbn != null && !isbn.trim().isEmpty()) {
                var book = bookService.findBookByIsbn(isbn);
                if (book != null) {
                    JOptionPane.showMessageDialog(this, "Title: " + book.get("title") + "\nAuthor: " + book.get("author"));
                } else {
                    JOptionPane.showMessageDialog(this, "Book not found!");
                }
            }
        });

        btnRegisterUser.addActionListener(e -> JOptionPane.showMessageDialog(this, "Register User feature clicked!"));
        btnBorrowBook.addActionListener(e -> JOptionPane.showMessageDialog(this, "Borrow Book feature clicked!"));
        btnReturnBook.addActionListener(e -> JOptionPane.showMessageDialog(this, "Return Book feature clicked!"));
        btnExit.addActionListener(e -> System.exit(0));

        // Add buttons to panel
        buttonPanel.add(btnAddBook);
        buttonPanel.add(btnRegisterUser);
        buttonPanel.add(btnViewBooks);
        buttonPanel.add(btnBorrowBook);
        buttonPanel.add(btnReturnBook);
        buttonPanel.add(btnExit);

        add(buttonPanel, BorderLayout.CENTER);
    }

    // Custom Blue Button Style
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(41, 128, 185)); // Nice Blue Color
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        return button;
    }
}