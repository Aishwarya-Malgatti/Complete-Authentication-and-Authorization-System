package com.kletech;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

public class RegistrationServlet extends HttpServlet {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/mydata";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "vijay";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        String username = request.getParameter("username");
        String lastName = request.getParameter("lastName");
        String phoneno = request.getParameter("phoneno");
        String usn = request.getParameter("usn");
        String email = request.getParameter("email");
        String address = request.getParameter("address");
        String password = request.getParameter("password");

        // Hash the password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try (PrintWriter out = response.getWriter()) {
            if (isUsernameTaken(username)) {
                out.println("<h2>Registration failed! Username is already taken.</h2>");
            } else if (registerUser(username, lastName, phoneno, usn, email, address, hashedPassword)) {
                out.println("<h2>Registration successful! Please log in.</h2>");
            } else {
                out.println("<h2>Registration failed! An error occurred. Please try again.</h2>");
            }
        }
    }

    private boolean isUsernameTaken(String username) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
                String query = "SELECT COUNT(*) FROM users WHERE username = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, username);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            return resultSet.getInt(1) > 0; // If count > 0, username is taken
                        }
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return false; // In case of error or no result, assume username is not taken
    }

    private boolean registerUser(String username, String lastName, String phoneno, String usn, String email, String address, String hashedPassword) {
        boolean isRegistered = false;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
                String query = "INSERT INTO users (username, lastName, phoneno, usn, email, address, password) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, username);
                    preparedStatement.setString(2, lastName);
                    preparedStatement.setString(3, phoneno);
                    preparedStatement.setString(4, usn);
                    preparedStatement.setString(5, email);
                    preparedStatement.setString(6, address);
                    preparedStatement.setString(7, hashedPassword);
                    int rowsAffected = preparedStatement.executeUpdate();
                    isRegistered = rowsAffected > 0; // If rows were affected, registration was successful
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace(); // Log this properly in production
        }

        return isRegistered;
    }
}
