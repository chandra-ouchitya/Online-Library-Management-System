import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class LibraryManagementSystem {
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== ONLINE LIBRARY MANAGEMENT SYSTEM ===");
            System.out.println("1. View books");
            System.out.println("2. Add book");
            System.out.println("3. Add member");
            System.out.println("4. Issue book");
            System.out.println("5. Return book");
            System.out.println("6. View issued books");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine();

            try {
                switch (choice) {
                    case "1": viewBooks(); break;
                    case "2": addBook(); break;
                    case "3": addMember(); break;
                    case "4": issueBook(); break;
                    case "5": returnBook(); break;
                    case "6": viewIssuedBooks(); break;
                    case "0": System.out.println("Goodbye!"); return;
                    default: System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    static void viewBooks() throws SQLException {
        String sql = "SELECT * FROM books";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\nID | TITLE | AUTHOR | AVAILABLE");
            while (rs.next()) {
                System.out.printf("%d | %s | %s | %s%n",
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getBoolean("available") ? "Yes" : "No");
            }
        }
    }

    static void addBook() throws SQLException {
        System.out.print("Book title: ");
        String title = sc.nextLine();
        System.out.print("Author: ");
        String author = sc.nextLine();

        String sql = "INSERT INTO books(title, author) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.executeUpdate();
            System.out.println("Book added successfully.");
        }
    }

    static void addMember() throws SQLException {
        System.out.print("Member name: ");
        String name = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();

        String sql = "INSERT INTO members(name, email) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.executeUpdate();
            System.out.println("Member added successfully.");
        }
    }

    static void issueBook() throws SQLException {
        System.out.print("Book ID: ");
        int bookId = Integer.parseInt(sc.nextLine());
        System.out.print("Member ID: ");
        int memberId = Integer.parseInt(sc.nextLine());

        String sql = "INSERT INTO issued_books(book_id, member_id, issue_date) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             PreparedStatement update = con.prepareStatement(
                 "UPDATE books SET available = FALSE WHERE book_id = ? AND available = TRUE")) {

            update.setInt(1, bookId);
            if (update.executeUpdate() == 0) {
                System.out.println("Book is unavailable or does not exist.");
                return;
            }

            ps.setInt(1, bookId);
            ps.setInt(2, memberId);
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            ps.executeUpdate();
            System.out.println("Book issued successfully.");
        }
    }

    static void returnBook() throws SQLException {
        System.out.print("Book ID: ");
        int bookId = Integer.parseInt(sc.nextLine());

        String sql = "UPDATE issued_books SET return_date = ? " +
                     "WHERE book_id = ? AND return_date IS NULL";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             PreparedStatement update = con.prepareStatement(
                 "UPDATE books SET available = TRUE WHERE book_id = ?")) {

            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.setInt(2, bookId);

            if (ps.executeUpdate() == 0) {
                System.out.println("No active issue found.");
                return;
            }

            update.setInt(1, bookId);
            update.executeUpdate();
            System.out.println("Book returned successfully.");
        }
    }

    static void viewIssuedBooks() throws SQLException {
        String sql = "SELECT i.issue_id, b.title, m.name, i.issue_date, i.return_date " +
                     "FROM issued_books i JOIN books b ON i.book_id=b.book_id " +
                     "JOIN members m ON i.member_id=m.member_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\nISSUE ID | BOOK | MEMBER | ISSUE DATE | RETURN DATE");
            while (rs.next()) {
                System.out.printf("%d | %s | %s | %s | %s%n",
                    rs.getInt("issue_id"), rs.getString("title"),
                    rs.getString("name"), rs.getDate("issue_date"),
                    rs.getDate("return_date"));
            }
        }
    }
}
