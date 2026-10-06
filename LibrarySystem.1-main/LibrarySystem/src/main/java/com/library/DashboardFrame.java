package com.library;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.bson.Document;

public class DashboardFrame extends JFrame {
    private final BookService bookService = new BookService();
    private final LibraryService libraryService = new LibraryService();
    private String userEmail;
    private String userName;
    private String userRole;

    public DashboardFrame(String email, String name, String role) {
        this.userEmail = email;
        this.userName = name;
        this.userRole = role;
        initializeUI();
    }

    public DashboardFrame() {
        this.userEmail = "admin@library.com";
        this.userName = "Admin";
        this.userRole = "Admin";
        initializeUI();
    }

    private void initializeUI() {
        setTitle("JEREZA LIBRARY MANAGEMENT SYSTEM");
        setSize(650, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Header Title
        JLabel headerLabel = new JLabel("JEREZA LIBRARY SYSTEM - " + userName.toUpperCase() + " (" + userRole.toUpperCase() + ")", SwingConstants.CENTER);
        headerLabel.setOpaque(true);
        headerLabel.setBackground(new Color(40, 50, 60));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setPreferredSize(new Dimension(650, 50));
        add(headerLabel, BorderLayout.NORTH);

        // Buttons Panel (2 Columns, 3 Rows Grid)
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton btnAddBook = createStyledButton("Add New Book");
        JButton btnRegisterUser = createStyledButton("View Users");
        JButton btnViewBooks = createStyledButton("View All Books");
        JButton btnBorrowBook = createStyledButton("Borrow Book");
        JButton btnReturnBook = createStyledButton("Return / History");
        JButton btnExit = createStyledButton("Exit System");

        // --- 1. ADD NEW BOOK ---
        btnAddBook.addActionListener(e -> {
            JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));

            JTextField txtTitle = new JTextField();
            JTextField txtAuthor = new JTextField();
            JTextField txtIsbn = new JTextField();
            JTextField txtCategory = new JTextField();
            JTextField txtCopies = new JTextField();

            formPanel.add(new JLabel("Book Title:"));
            formPanel.add(txtTitle);
            formPanel.add(new JLabel("Author:"));
            formPanel.add(txtAuthor);
            formPanel.add(new JLabel("ISBN:"));
            formPanel.add(txtIsbn);
            formPanel.add(new JLabel("Category:"));
            formPanel.add(txtCategory);
            formPanel.add(new JLabel("Total Copies:"));
            formPanel.add(txtCopies);

            int result = JOptionPane.showConfirmDialog(
                    this, 
                    formPanel, 
                    "Add New Book", 
                    JOptionPane.OK_CANCEL_OPTION, 
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                String title = txtTitle.getText().trim();
                String author = txtAuthor.getText().trim();
                String isbn = txtIsbn.getText().trim();
                String category = txtCategory.getText().trim();
                String copiesStr = txtCopies.getText().trim();

                if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || category.isEmpty() || copiesStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill in all fields!", "Input Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    int copies = Integer.parseInt(copiesStr);
                    bookService.addBook(title, author, isbn, category, copies);
                    JOptionPane.showMessageDialog(this, "Book successfully added!");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Copies must be a valid number!", "Input Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error adding book: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // --- 2. VIEW & DELETE BOOKS ---
        btnViewBooks.addActionListener(e -> showBooksDialog());

        // --- 3. VIEW & DELETE USERS ---
        btnRegisterUser.addActionListener(e -> showUsersDialog());

        // --- 4. BORROW BOOK ---
        btnBorrowBook.addActionListener(e -> {
            JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));

            JTextField txtUserEmail = new JTextField(this.userEmail);
            JTextField txtIsbn = new JTextField();

            formPanel.add(new JLabel("Borrower Email:"));
            formPanel.add(txtUserEmail);
            formPanel.add(new JLabel("Book ISBN:"));
            formPanel.add(txtIsbn);

            int result = JOptionPane.showConfirmDialog(
                    this, 
                    formPanel, 
                    "Borrow Book", 
                    JOptionPane.OK_CANCEL_OPTION, 
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                String borrowerEmail = txtUserEmail.getText().trim();
                String isbn = txtIsbn.getText().trim();

                if (borrowerEmail.isEmpty() || isbn.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill in both fields!", "Input Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    boolean success = libraryService.borrowBook(borrowerEmail, isbn);
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Book borrowed successfully for " + borrowerEmail + "!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to borrow book. Check if ISBN is correct or copies are available.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error borrowing book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // --- 5. RETURN / HISTORY ---
        btnReturnBook.addActionListener(e -> {
            int choice = JOptionPane.showOptionDialog(this, 
                "Choose an action:", 
                "Borrow & Return Management", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                new String[]{"Return a Book", "View Borrow History"}, 
                "Return a Book");

            if (choice == 0) { // Return a Book
                JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
                JTextField txtUserEmail = new JTextField(this.userEmail);
                JTextField txtIsbn = new JTextField();

                formPanel.add(new JLabel("Borrower Email:"));
                formPanel.add(txtUserEmail);
                formPanel.add(new JLabel("Book ISBN:"));
                formPanel.add(txtIsbn);

                int result = JOptionPane.showConfirmDialog(this, formPanel, "Return Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String borrowerEmail = txtUserEmail.getText().trim();
                    String isbn = txtIsbn.getText().trim();

                    if (!borrowerEmail.isEmpty() && !isbn.isEmpty()) {
                        try {
                            boolean success = libraryService.returnBook(borrowerEmail, isbn);
                            if (success) {
                                JOptionPane.showMessageDialog(this, "Book returned successfully for " + borrowerEmail + "!");
                            } else {
                                JOptionPane.showMessageDialog(this, "No active borrow record found.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Error returning book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Please fill in all fields!", "Input Error", JOptionPane.WARNING_MESSAGE);
                    }
                }
            } else if (choice == 1) { // View History
                try {
                    List<Document> records = libraryService.getAllBorrowRecords();
                    if (records == null || records.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "No borrow history found.");
                        return;
                    }
                    String[] columns = {"User Email", "ISBN", "Borrow Date", "Status"};
                    DefaultTableModel model = new DefaultTableModel(columns, 0);
                    for (Document doc : records) {
                        model.addRow(new Object[]{
                            doc.getString("userEmail"),
                            doc.getString("isbn"),
                            doc.get("borrowDate") != null ? doc.get("borrowDate").toString() : "N/A",
                            doc.getString("status")
                        });
                    }
                    showTableDialog("Borrow History Logs", model);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error fetching history: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // --- 6. EXIT ---
        btnExit.addActionListener(e -> System.exit(0));

        buttonPanel.add(btnAddBook);
        buttonPanel.add(btnRegisterUser);
        buttonPanel.add(btnViewBooks);
        buttonPanel.add(btnBorrowBook);
        buttonPanel.add(btnReturnBook);
        buttonPanel.add(btnExit);

        add(buttonPanel, BorderLayout.CENTER);
    }

    // DIALOG FOR VIEWING & DELETING BOOKS
    private void showBooksDialog() {
        try {
            List<Document> books = bookService.getAllBooks();
            if (books == null || books.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No books found in database.");
                return;
            }

            String[] columns = {"ISBN", "Title", "Author", "Category", "Available", "Total"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (Document doc : books) {
                model.addRow(new Object[]{
                    doc.getString("isbn"),
                    doc.getString("title"),
                    doc.getString("author"),
                    doc.getString("category"),
                    doc.getInteger("availableCopies", 0),
                    doc.getInteger("totalCopies", 0)
                });
            }

            JTable table = new JTable(model);
            table.setFont(new Font("Arial", Font.PLAIN, 13));
            table.setRowHeight(25);
            JScrollPane scrollPane = new JScrollPane(table);

            JDialog dialog = new JDialog(this, "All Registered Books", true);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.setSize(700, 400);
            dialog.setLocationRelativeTo(this);

            dialog.add(scrollPane, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel();
            JButton btnDelete = new JButton("Delete Selected Book");
            btnDelete.setBackground(new Color(192, 57, 43));
            btnDelete.setForeground(Color.WHITE);
            btnDelete.setFont(new Font("Arial", Font.BOLD, 12));

            btnDelete.addActionListener(ev -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(dialog, "Please select a book from the table to delete!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String isbnToDelete = (String) model.getValueAt(selectedRow, 0);
                String bookTitle = (String) model.getValueAt(selectedRow, 1);

                int confirm = JOptionPane.showConfirmDialog(
                        dialog, 
                        "Are you sure you want to delete book: " + bookTitle + " (ISBN: " + isbnToDelete + ")?", 
                        "Confirm Delete", 
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean deleted = libraryService.deleteBook(isbnToDelete);
                    if (deleted) {
                        model.removeRow(selectedRow);
                        JOptionPane.showMessageDialog(dialog, "Book deleted successfully!");
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to delete book.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            bottomPanel.add(btnDelete);
            dialog.add(bottomPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching books: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // DIALOG FOR VIEWING & DELETING USERS
    private void showUsersDialog() {
        try {
            List<Document> users = libraryService.getAllUsers();
            if (users == null || users.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No users found in database.");
                return;
            }

            String[] columns = {"Name", "Email", "Role"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (Document doc : users) {
                model.addRow(new Object[]{
                    doc.getString("name"),
                    doc.getString("email"),
                    doc.getString("role")
                });
            }

            JTable table = new JTable(model);
            table.setFont(new Font("Arial", Font.PLAIN, 13));
            table.setRowHeight(25);
            JScrollPane scrollPane = new JScrollPane(table);

            JDialog dialog = new JDialog(this, "All Registered Users", true);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.setSize(600, 400);
            dialog.setLocationRelativeTo(this);

            dialog.add(scrollPane, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel();
            JButton btnDelete = new JButton("Delete Selected User");
            btnDelete.setBackground(new Color(192, 57, 43));
            btnDelete.setForeground(Color.WHITE);
            btnDelete.setFont(new Font("Arial", Font.BOLD, 12));

            btnDelete.addActionListener(ev -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(dialog, "Please select a user from the table to delete!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String emailToDelete = (String) model.getValueAt(selectedRow, 1);
                int confirm = JOptionPane.showConfirmDialog(
                        dialog, 
                        "Are you sure you want to delete user: " + emailToDelete + "?", 
                        "Confirm Delete", 
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean deleted = libraryService.deleteUser(emailToDelete);
                    if (deleted) {
                        model.removeRow(selectedRow);
                        JOptionPane.showMessageDialog(dialog, "User deleted successfully!");
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to delete user.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            bottomPanel.add(btnDelete);
            dialog.add(bottomPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching users: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTableDialog(String title, DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(700, 350));
        JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.PLAIN_MESSAGE);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(41, 128, 185));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        return button;
    }
}