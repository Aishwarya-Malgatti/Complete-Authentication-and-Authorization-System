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
import javax.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

public class LoginServlet extends HttpServlet {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/mydata";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "vijay";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");

        // Retrieve login credentials from the request
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try (PrintWriter out = response.getWriter()) {
            if (validateUser(username, password)) {
                // Fetch user details after successful validation
                User user = getUserDetails(username);

                if (user != null) {
                    // Create a session for the logged-in user
                    HttpSession session = request.getSession();
                    session.setAttribute("username", user.getUsername());
                    session.setAttribute("user", user);

                    // Display user profile
                    out.println("<html>");
                    out.println("<head><title>User Profile</title>");
                    out.println("<style>");
                    out.println("body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-image: url('path/to/your/background-image.jpg'); background-size: cover; background-position: center; height: 100vh; }");
                    out.println(".profile-container { max-width: 800px; margin: 50px auto; padding: 20px; background-color: rgba(255, 255, 255, 0.8); border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); }");
                    out.println("h1 { text-align: center; color: #333; }");
                    out.println(".profile-info { margin: 20px 0; }");
                    out.println(".profile-info p { font-size: 18px; line-height: 1.6; margin: 10px 0; }");
                    out.println(".profile-info strong { color: #444; }");
                    out.println(".profile-info span { color: #555; font-weight: normal; }");
                    out.println(".logout-btn { text-align: center; margin-top: 30px; }");
                    out.println(".logout-btn .btn { background-color: #f44336; color: #fff; padding: 10px 20px; text-decoration: none; font-size: 18px; border-radius: 5px; transition: background-color 0.3s; }");
                    out.println(".logout-btn .btn:hover { background-color: #e53935; }");
                    out.println(".profile-image { text-align: center; margin-bottom: 20px; }");
                    out.println(".profile-image img { width: 150px; height: 150px; border-radius: 50%; object-fit: cover; }");
                    out.println("</style>");
                    out.println("</head>");
                    out.println("<body style=\"background-image: url('image/kle2.jpg'); background-size: cover; background-position: center;\">");
                    out.println("<div class='profile-container'>");
                    out.println("<div class='profile-image'>");
                    out.println("<img src='image/user.jpg' alt='User Profile Image' />");
                    out.println("</div>");
                    out.println("<h1>User Profile</h1>");
                    out.println("<div class='profile-info'>");
                    out.println("<h2>Login Successful! Welcome, " + user.getUsername() + "!</h2>");
                    out.println("<p><strong>Last Name:</strong> " + user.getLastName() + "</p>");
                    out.println("<p><strong>Phone Number:</strong> " + user.getPhoneno() + "</p>");
                    out.println("<p><strong>USN:</strong> " + user.getUSN() + "</p>");
                    out.println("<p><strong>Email:</strong> " + user.getEmail() + "</p>");
                    out.println("<p><strong>Address:</strong> " + user.getAddress() + "</p>");
                    out.println("</div>");
                    out.println("<div class='logout-btn'>");
                    out.println("<a href='index.html' class='btn'>Logout</a>");
                    out.println("</div>");
                    out.println("</div>");
                    out.println("</body>");
                    out.println("</html>");
                    
                    
                } else {
                    out.println("<h2>Error retrieving user details. Please try again.</h2>");
                }
            } else {
                // If login fails, display an error message
                out.println("<html>");
                out.println("<head><title>Login Failed</title></head>");
                out.println("<body>");
                out.println("<h2>Invalid username or password!</h2>");
                out.println("<a href='index.html'>Go back to Login</a>");
                out.println("</body>");
                out.println("</html>");
            }
        }
    }

    // Validate the username and password against the database
    private boolean validateUser(String username, String password) {
        boolean isValidUser = false;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
                String query = "SELECT password FROM users WHERE username = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, username);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        String hashedPassword = resultSet.getString("password");
                        isValidUser = BCrypt.checkpw(password, hashedPassword);
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return isValidUser;
    }

    // Fetch user details from the database using the username
    private User getUserDetails(String username) {
        User user = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
                String query = "SELECT username, lastName, phoneno,usn, email, address FROM users WHERE username = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, username);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        user = new User(
                            resultSet.getString("username"),
                            resultSet.getString("lastName"),
                            resultSet.getString("phoneno"),
                            resultSet.getString("usn"),
                            resultSet.getString("email"),
                            resultSet.getString("address")
                        );
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    // Inner User class to represent a user's details
    public class User {
        private String username;
        private String lastName;
        private String phoneno;
        private String usn;
        private String email;
        private String address;

        public User(String username, String lastName, String phoneno,String usn, String email, String address) {
            this.username = username;
            this.lastName = lastName;
            this.phoneno = phoneno;
            this.usn=usn;
            this.email = email;
            this.address = address;
        }

        // Getters for the User attributes
        public String getUsername() {
            return username;
        }

        public String getLastName() {
            return lastName;
        }

        public String getPhoneno() {
            return phoneno;
        }
        public String getUSN() {
            return usn;
        }

        public String getEmail() {
            return email;
        }

        public String getAddress() {
            return address;
        }
    }
}
