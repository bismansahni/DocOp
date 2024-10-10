package main;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

/*******
 *  <p>Database Helper</p>
 *  <p>The DatabaseHelper class is responsible for handling all database operations for the application.</p>
 *  <p>This includes establishing a connection to the database, creating tables, and executing queries.</p>
 *  <p>The class also contains methods for user registration, login, and password management.</p>
 *
 *  <p>The class is designed to work with the H2 Database Engine, an in-memory database system.</p>
 */
public class DatabaseHelper {

    /* Default Contructor*/
    public DatabaseHelper() {
    }

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:C:\\Users\\sriuj\\git\\DocOp/cse360database";

    // Database credentials (use defaults for H2)
    static final String USER = "sa";
    static final String PASS = "";

    // Connection and statement objects
    private Connection connection = null;
    private Statement statement = null;

    /* ConnectToDatabase Function to connect to the database*/
    public void connectToDatabase() throws SQLException {
        try {
            // Load the JDBC driver
            Class.forName(JDBC_DRIVER);  // This line loads the H2 driver
            System.out.println("Connecting to database...");

            connection = DriverManager.getConnection(DB_URL, USER, PASS);

            // Ensure that the connection is successful
            if (connection != null) {

                System.out.println("Database connected successfully!");
                statement = connection.createStatement(); // Initialize statement

                // Ensure that the statement is initialized
                if (statement != null) {

                    //create tables if they don't exist
                    System.out.println("Statement initialized successfully!");
                    createTables();

                } else {

                    System.out.println("Failed to initialize statement.");
                }

            } else {

                System.out.println("Failed to establish database connection.");
            }

        } catch (ClassNotFoundException e) {

            // Catch errors related to the JDBC driver
            System.err.println("JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();

        } catch (SQLException e) {

            System.err.println("SQL Exception: " + e.getMessage());
            throw e;  // Rethrow to catch issues with SQL execution
        }
    }

    /*
     *  Database Structure
     *  Table: users
     *  Columns: user_id (INT), username (VARCHAR), email (VARCHAR), password_hash (BINARY), one_time_password (BOOLEAN), password_expiration (DATETIME), role (VARCHAR), first_name (VARCHAR), middle_name (VARCHAR), last_name (VARCHAR), preferred_name (VARCHAR), account_setup_complete (BOOLEAN), invite_code (VARCHAR), invite_role (ENUM), invite_expires_at (DATETIME), created_by_admin_id (INT), created_at (TIMESTAMP), updated_at (TIMESTAMP)
     *
     *  Table: password_reset_tokens
     *  Columns: token_id (INT), user_id (INT), token (VARCHAR), expires_at (DATETIME), used (BOOLEAN)
     */
    private void createTables() throws SQLException {

        // SQL for creating the users table
        String createUserTable = "CREATE TABLE IF NOT EXISTS users ("
                + "user_id INT PRIMARY KEY AUTO_INCREMENT, "  									// Primary Key
                + "username VARCHAR(255) UNIQUE, "   											// Username must be unique
                + "email VARCHAR(255) UNIQUE, "      											// Email must be unique
                + "password_hash BINARY(32), "     												// Hashed password
                + "one_time_password BOOLEAN DEFAULT FALSE, " 									// Flag for one-time password
                + "password_expiration DATETIME, "            									// Expiration date for the one-time password
                + "role VARCHAR(255), "															// Rples assigned to the user
                + "first_name VARCHAR(255), "        											// First name
                + "middle_name VARCHAR(255), "               									// Middle name (optional)
                + "last_name VARCHAR(255), "         											// Last name
                + "preferred_name VARCHAR(255), "             									// Preferred name (optional)
                + "account_setup_complete BOOLEAN DEFAULT FALSE, " 								// Account setup completion flag
                + "invite_code VARCHAR(255), "                									// Invite code (if used)
                + "invite_role ENUM('Student', 'Instructor', 'Admin'), " 						// Role assigned by the invite code
                + "invite_expires_at DATETIME, "              									// Expiration date for the invite code
                + "created_by_admin_id INT, "                 									// Admin who created the invite (optional, references another user)
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " 							// Timestamp for user creation (OPTIONAL)
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" 	// Auto-updating timestamp for changes (OPTIONAL)
                + ")";

        // SQL for creating the password_reset_tokens table OPTIONAL FOR FURTHER OPTIMIZATION
        String createPasswordResetTokensTable = "CREATE TABLE IF NOT EXISTS password_reset_tokens ("
                + "token_id INT PRIMARY KEY AUTO_INCREMENT, "  // Primary Key
                + "user_id INT NOT NULL, "                    // Foreign Key referencing users
                + "token VARCHAR(255) NOT NULL UNIQUE, "      // Unique password reset token
                + "expires_at DATETIME NOT NULL, "            // Expiration date and time for the token
                + "used BOOLEAN DEFAULT FALSE, "              // Flag indicating whether the token has been used
                + "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" // Foreign Key constraint
                + ")";

        // Execute the SQL to create the users table
        statement.execute(createUserTable);

        // Execute the SQL to create the password_reset_tokens table
        statement.execute(createPasswordResetTokensTable);
    }

    /*login method that checks if the user exists in the database and if the user exists then checks if the password and role are correct if correct then return true else return false
     *
     * @param username	The username of the user
     * @param password	The password of the user
     * @param role		The role of the user
     *
     * @return boolean	Returns true if the user exists and the password and role are correct, otherwise returns false
     */
    public boolean login(String username, String password, String role) throws SQLException {

        // SQL query to check if the user exists in the database
        String query = "SELECT * FROM users WHERE username = ? AND password_hash = ? AND role = ?";
        System.out.println("Executing login query for username: " + username);

        // Prepare the statement and set the parameters
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {

            // Set the parameters for the query
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);

            // Execute the query and check if the result set has any rows
            ResultSet resultSet = pstmt.executeQuery();

            // Return true if the result set has any rows, indicating a successful login
            boolean result = resultSet.next();
            System.out.println("Login query result for user " + username + ": " + result);
            return result;
        }
    }


    /*
     *  Register a new user with the given username, password hash, and role in the database.
     *
     * @param username		The username of the user
     * @param passwordHash	The hashed password of the user
     * @param role			The role of the user
     *
     * @throws SQLException	Throws an SQLException if there is an error during user registration
     */
    public void register(String username, byte[] passwordHash, String role) throws SQLException {

        // SQL query to insert the user into the database
        System.out.println("Registering user with username: " + username);
        System.out.println("Hashed password during registration: " + Arrays.toString(passwordHash));
        String insertUser = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";

        // Prepare the statement and set the parameters
        try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {

            pstmt.setString(1, username);
            pstmt.setBytes(2, passwordHash);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
            System.out.println("User registered successfully with username: " + username);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error during user registration: " + e.getMessage());
        }
    }


    /*
     *  Method to check if the user exists in the database
     *
     * @param username	The username of the user
     *
     * @return boolean	Returns true if the user exists, otherwise returns
     */
    public boolean doesUserExist(String username) throws SQLException {

        // SQL query to check if the user exists in the database
        String query = "SELECT * FROM users WHERE username = ?";
        System.out.println("Executing query to check if user exists for username: " + username);

        // Prepare the statement and set the parameters
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {

            // Set the parameters for the query
            pstmt.setString(1, username);
            ResultSet resultSet = pstmt.executeQuery();
            boolean exists = resultSet.next();
            System.out.println("Does user exist result: " + exists);
            return exists;
        }
    }


    /*
     *  Method to check if the invite code exists in the database
     *
     *  @param inviteCode	The invite code to check for existence of an invitation that is made by the admin in the database
     *
     *  @return boolean	Returns true if the invite code exists, otherwise returns false
     *
     */
    public boolean doesInviteExist(String inviteCode) throws SQLException {

        // SQL query to check if the invite code exists in the database
        String query = "SELECT * FROM users WHERE LOWER(TRIM(invite_code)) = LOWER(TRIM(?))";

        // Prepare the statement and set the parameters
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, inviteCode);
            ResultSet resultSet = pstmt.executeQuery();
            return resultSet.next();
        }
    }


    /*
     *  Method to get the role from the invite code in the database
     *
     * @param inviteCode	The invite code to get the role from the database
     *
     * @return String	Returns the role associated with the invite code, or an empty string if no role is found
     */
    public String getRoleFromInvite(String inviteCode) throws SQLException {

        // SQL query to get the role from the invite code in the database
        String query = "SELECT invite_role FROM users WHERE invite_code = ?";
        System.out.println("Fetching role from invite code: " + inviteCode);

        // Prepare the statement and set the parameters
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, inviteCode);
            ResultSet resultSet = pstmt.executeQuery();

            // Check if the result set has any rows
            if (resultSet.next()) {

                // Get the role from the result set
                String role = resultSet.getString("invite_role");
                System.out.println("Role fetched for invite code: " + inviteCode + " is: " + role);
                return role;

            } else {

                // Return an empty string if no role is found
                System.out.println("No role found for invite code: " + inviteCode);
                return "";
            }
        }
    }

    /*
     *  Method to get the username from the invite code in the database to add the role to the user
     *
     * @param username     The username of the user
     * @param newRole      The role to add to the user
     *
     * @throws SQLException	Throws an SQLException if there is an error during adding the role
     */
    public void addRoleToUser(String username, String newRole) throws SQLException {

        // SQL query to get the role from the invite code in the database
        String query = "SELECT role FROM users WHERE username = ?";

        // Prepare the statement and set the parameters
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {

            //Set the parameters for the query
            pstmt.setString(1, username);
            ResultSet resultSet = pstmt.executeQuery();

            // Check if the result set has any rows
            if (resultSet.next()) {
                String currentRoles = resultSet.getString("role");

                // Check if the new role is already assigned
                if (currentRoles == null || currentRoles.isEmpty()) {

                    currentRoles = newRole;

                } else if (!currentRoles.contains(newRole)) {

                    currentRoles += "," + newRole;

                } else {

                    System.out.println("User already has the role " + newRole);
                    return;  // Role is already assigned, so we can return early
                }

                // Update the user's roles in the database
                String updateQuery = "UPDATE users SET role = ? WHERE username = ?";
                try (PreparedStatement updatePstmt = connection.prepareStatement(updateQuery)) {
                    updatePstmt.setString(1, currentRoles);
                    updatePstmt.setString(2, username);
                    updatePstmt.executeUpdate();
                    System.out.println("Role " + newRole + " successfully added to user " + username);
                }
            } else {
                System.out.println("User " + username + " not found.");
            }
        }
    }



    /*
     *  Method to remove the role from the user in the database
     *
     * @param username		The username of the user
     * @param roleToRemove	The role to remove from the user
     *
     * @throws SQLException	Throws an SQLException if there is an error during removing the role
     */
    public void removeRoleFromUser(String username, String roleToRemove) throws SQLException {

        // SQL query to remove the role from the user in the database
        String query = "SELECT role FROM users WHERE username = ?";

        // Prepare the statement and set the parameters
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {

            // Set the parameters for the
            pstmt.setString(1, username);
            ResultSet resultSet = pstmt.executeQuery();

            // Check if the result set has any rows
            if (resultSet.next()) {
                String currentRoles = resultSet.getString("role");

                // Check if the role to remove is assigned to the user
                if (currentRoles != null && currentRoles.contains(roleToRemove)) {
                    String[] rolesArray = currentRoles.split(",");
                    StringBuilder updatedRoles = new StringBuilder();

                    // Loop through the roles and add all roles except the one to remove
                    for (String role : rolesArray) {

                        if (!role.equals(roleToRemove)) {

                            if (updatedRoles.length() > 0) {
                                updatedRoles.append(",");

                            }

                            updatedRoles.append(role);
                        }
                    }

                    // Update the user's roles in the database (may be empty after removal)
                    String updateQuery = "UPDATE users SET role = ? WHERE username = ?";

                    // Prepare the statement and set the parameters
                    try (PreparedStatement updatePstmt = connection.prepareStatement(updateQuery)) {

                        // Set the parameters for the query
                        updatePstmt.setString(1, updatedRoles.toString());
                        updatePstmt.setString(2, username);
                        updatePstmt.executeUpdate();
                        System.out.println("Role " + roleToRemove + " successfully removed from user " + username);
                    }

                } else {

                    System.out.println("Role " + roleToRemove + " is not assigned to user " + username);

                }
            }
        }
    }


    /*
     *  Method to get the roles for the user in the database
     *
     * @param username	The username of the user
     *
     * @return  String [] Returns an array of roles assigned to the user, or an empty array if no roles are assigned to the user yet
     */
    public String[] getRolesForUser(String username) throws SQLException {

        // SQL query to get the roles for the user in the database
        String query = "SELECT role FROM users WHERE username = ?";

        // Prepare the statement and set the parameters
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {

            // Set the parameters for the
            pstmt.setString(1, username);
            ResultSet resultSet = pstmt.executeQuery();

            // Check if the result set has any rows
            if (resultSet.next()) {

                // Get the roles from the result set
                String roles = resultSet.getString("role");
                return (roles != null && !roles.isEmpty()) ? roles.split(",") : new String[] {}; // Split roles by commas
            }
        }

        return new String[] {}; // Return empty array if no roles found
    }




    /*
     * Method to check if the database is empty
     *
     * @return boolean Returns true if the database is empty, otherwise returns false
     *
     */
    public boolean isDatabaseEmpty() throws SQLException {

        // SQL query to check if the database is empty
        if (statement == null) {

            // Check if the statement is initialized
            System.out.println("Statement is not initialized, database connection might have failed.");
            return true;
        }

        // Execute the query to count the number of users in the database
        String query = "SELECT COUNT(*) AS count FROM users";
        ResultSet resultSet = statement.executeQuery(query);

        if (resultSet.next()) {

            return resultSet.getInt("count") == 0;
        }
        return true;
    }

    /*
     * Method to check if the password given is correct or not
     *
     * @param username	The username of the user
     * @param password	The password of the user
     *
     * @return boolean	Returns true if the password is correct, otherwise returns false
     */
    public boolean isPasswordCorrect(String username, String password) throws SQLException, NoSuchAlgorithmException {

        // SQL query to get the password hash for the user
        System.out.println("Validating password for username: " + username);
        byte[] storedHash = getPasswordHash(username);

        // Check if the password hash is found
        if (storedHash != null) {
            boolean correct = PasswordManager.verifyPassword(password, storedHash);
            System.out.println("Password validation result for username " + username + ": " + correct);
            return correct;
        }

        //print error message if password hash not found and return false
        System.out.println("Password hash not found for username: " + username);
        return false;
    }

    /*
     *  Method to check if the role is correct or not for the user
     *
     * @param username	The username of the user
     * @param role		The role of the user
     *
     * @return boolean	Returns true if the role is correct, otherwise returns falseS
     *
     */
    public boolean isRoleCorrect(String username, String role) throws SQLException {

        // SQL query to check if the role is correct for the user
        String query = "SELECT * FROM users WHERE username = ? AND role = ?";
        System.out.println("Validating role for username: " + username);

        // Prepare the statement and set the parameters
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {

            // Set the parameters for the query
            pstmt.setString(1, username);
            pstmt.setString(2, role);

            // Execute the query and check if the result
            ResultSet resultSet = pstmt.executeQuery();
            boolean correct = resultSet.next();
            System.out.println("Role is correct: " + correct);
            return correct;
        }
    }


    /*
     *  Method to check if the user has completed the account setup process using the username and the account_setup_complete flag
     *
     * @param username	The username of the user
     *
     * @return boolean	Returns true if the account setup is complete, otherwise returns false
     */
    public boolean isAccountSetupComplete(String username) throws SQLException {

        // SQL query to check if the account setup is complete for the user
        String query = "SELECT account_setup_complete FROM users WHERE username = ?";
        System.out.println("Checking if account setup is complete for username: " + username);

        // Prepare the statement and set the parameters
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {

            // Set the parameters for the
            pstmt.setString(1, username);
            ResultSet resultSet = pstmt.executeQuery();

            // Check if the result set has any rows
            if (resultSet.next()) {

                // Get the account setup completion flag from the result set
                boolean complete = resultSet.getBoolean("account_setup_complete");
                System.out.println("Account setup complete: " + complete);
                return complete;
            }

            return false;
        }
    }

    /*
     *  Method to set the account setup completion flag for the user using the username and the other user details
     *
     * @param username		The username of the user
     * @param firstName		The first name of the user
     * @param middleName	The middle name of the user
     * @param lastName		The last name of the user
     * @param preferredName	The preferred name of the user
     * @param email			The email of the user
     *
     * @throws SQLException	Throws an SQLException if there is an error during user setup
     */
    public void setupUserDetails(String username, String firstName, String middleName, String lastName, String preferredName, String email) throws SQLException {

        // Debugging statement to display user details
        System.out.println("Setting up user details for username: " + username);
        System.out.println("First Name: " + firstName + ", Middle Name: " + middleName + ", Last Name: " + lastName + ", Preferred Name: " + preferredName + ", Email: " + email);

        // SQL query to update the user details in the database
        String query = "UPDATE users SET first_name = ?, middle_name = ?, last_name = ?, preferred_name = ?, email = ?, account_setup_complete = TRUE WHERE username = ?";

        // Prepare the statement and set the parameters
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, middleName);
            pstmt.setString(3, lastName);
            pstmt.setString(4, preferredName);
            pstmt.setString(5, email);
            pstmt.setString(6, username);

            // Execute the query and check the number of rows affected
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected by update: " + rowsAffected);

            // Print success message if the user details are updated successfully
            if (rowsAffected > 0) {

                System.out.println("User details updated successfully for username: " + username);

            } else {

                System.out.println("Failed to update user details for username: " + username);
            }

        } catch (SQLException e) {

            System.err.println("SQL Exception during user setup: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /*
     *  Method to reset the user password using the username and the new password and the expiration date
     *
     * @param username	The username of the user
     * @param password	The password of the user
     * @param expirationDate	The expiration date of the password
     *
     * @return returns a String with a message of confirmation
     *
     */
    public String resetUser(String username, String password, String expirationDate) throws SQLException {
        try {
            // Debugging statement to display input expiration date
            System.out.println("Original expiration date: " + expirationDate);

            // Convert the expirationDate from MM/DD/YYYY to YYYY-MM-DD
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Parse the expiration date and format it to the desired output
            LocalDate date = LocalDate.parse(expirationDate, inputFormatter);
            String formattedDate = date.format(outputFormatter);

            // Debugging statement to display formatted expiration date
            System.out.println("Formatted expiration date: " + formattedDate);

            // Hash the password before storing it using the PasswordManager class method hashPassword
            byte[] hashedPassword = PasswordManager.hashPassword(password);

            // SQL query to update the user password and set the one-time password flag and expiration
            String query = "UPDATE users SET password_hash = ?, one_time_password = TRUE, password_expiration = ? WHERE username = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {

                pstmt.setBytes(1, hashedPassword);
                pstmt.setString(2, formattedDate);
                pstmt.setString(3, username);
                pstmt.executeUpdate();
                return "Password reset successfully";
            }

        } catch (DateTimeParseException e) {

            System.err.println("Error parsing expiration date: " + e.getMessage());
            return "Invalid date format. Please use MM/DD/YYYY.";

        } catch (NoSuchAlgorithmException e) {

            System.err.println("Error hashing password: " + e.getMessage());
            return "Error resetting password.";

        }
    }

    /*
     *  Method to close Connection to the database
     */
    public void closeConnection() {
        try {
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
     *  Method to clear the database
     *
     * @throws SQLException Throws an SQLException if there is an error during
     * clearing the database
     */
    public void clearDatabase() throws SQLException {
        String query = "DELETE FROM users";
        statement.execute(query);
    }

    /*
     *  Method to add a user using the invite code and role for the admin
     *
     * @param inviteCode	The invite code to add the user
     * @param role			The role to add to the user
     *
     * @return String	Returns a message with the status of the user addition
     */
    public String addUser(String inviteCode, String role) {

        // Check if the invite code exists
        try {

            // Check if the invite code exists
            if (doesInviteExist(inviteCode)) {
                return "Invite code already exists";
            }

            // Get the role from the invite code
            createInvite(inviteCode, role);
            return "Invite created successfully";

        } catch (SQLException e) {

            e.printStackTrace();
            return "Database error";
        }
    }

    /*
     *  Method to create an invite using the invite code and role for the admin
     *
     * @param inviteCode	The invite code to create
     * @param inviteRole	The role to assign to the invite code
     *
     * @throws SQLException	Throws an SQLException if there is an error during invite creation
     */
    public void createInvite(String inviteCode, String inviteRole) throws SQLException {

        // First, check if the invite already exists
        String checkInvite = "SELECT invite_role FROM users WHERE invite_code = ?";

        // Prepare the statement and set the parameters
        try (PreparedStatement pstmt = connection.prepareStatement(checkInvite)) {

            // Set the parameters for the
            pstmt.setString(1, inviteCode);
            ResultSet resultSet = pstmt.executeQuery();

            // Check if the result set has any rows
            if (resultSet.next()) {

                String currentRoles = resultSet.getString("invite_role");

                // If the role already exists, return without making changes
                if (currentRoles != null && currentRoles.contains(inviteRole)) {
                    System.out.println("Role " + inviteRole + " already exists for invite code: " + inviteCode); // Debugging statement
                    return;
                }

                // If the role does not exist, append the new role to the existing ones
                String updatedRoles = currentRoles == null ? inviteRole : currentRoles + "," + inviteRole;

                // Update the invite with the new roles
                String updateInvite = "UPDATE users SET invite_role = ?, role = ? WHERE invite_code = ?";

                try (PreparedStatement updatePstmt = connection.prepareStatement(updateInvite)) {

                    // Set the parameters for the query
                    updatePstmt.setString(1, updatedRoles);
                    updatePstmt.setString(2, updatedRoles); // Ensure that the role column is also updated
                    updatePstmt.setString(3, inviteCode);
                    updatePstmt.executeUpdate();
                    System.out.println("Updated roles for invite code: " + inviteCode + " with role: " + inviteRole); // Debugging statement

                }

            } else {

                // If no invite exists, create a new one with invite_role and role populated
                String insertInvite = "INSERT INTO users (invite_code, invite_role, role) VALUES (?, ?, ?)";

                // Prepare the statement and set the parameters
                try (PreparedStatement insertPstmt = connection.prepareStatement(insertInvite)) {
                    insertPstmt.setString(1, inviteCode);
                    insertPstmt.setString(2, inviteRole);
                    insertPstmt.setString(3, inviteRole); // Ensure that the role is also inserted
                    insertPstmt.executeUpdate();
                    System.out.println("Invite created successfully with invite code: " + inviteCode + " and role: " + inviteRole); // Debugging statement
                }
            }
        }
    }


    /*
     *  Method to get the user details using the current username
     *
     * @param currentUsername	The username of the user
     *
     * @return String []	Returns an array of user details
     */
    public String[] getUserDetails(String currentUsername) {

        //get user details method to get user details using the current username
        String[] userDetails = new String[5];
        String query = "SELECT first_name, middle_name, last_name, preferred_name, email FROM users WHERE username = ?";

        // Prepare the statement and set the parameters
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {

            // Set the parameters for the query
            pstmt.setString(1, currentUsername);
            ResultSet resultSet = pstmt.executeQuery();

            // Check if the result set has any
            if (resultSet.next()) {
                userDetails[0] = resultSet.getString("first_name");
                userDetails[1] = resultSet.getString("middle_name");
                userDetails[2] = resultSet.getString("last_name");
                userDetails[3] = resultSet.getString("preferred_name");
                userDetails[4] = resultSet.getString("email");
            }


        } catch (SQLException e) {

            // Print error message if there is an error during user details retrieval
            e.printStackTrace();
        }

        return userDetails;
    }

    /*
     *  Method to update the user details using the current username and the new
     * details
     *
     * @param currentUsername The username of the user
     *
     * @param firstName The first name of the user
     *
     * @param middleName The middle name of the user
     *
     * @param lastName The last name of the user
     *
     * @param preferredName The preferred name of the user
     *
     * @param email The email of the user
     *
     */
    public void updateUserDetails(String currentUsername, String firstName, String middleName, String lastName,
                                  String preferredName, String email) {

        // update user details method to update user details using the current username
        String query = "UPDATE users SET first_name = ?, middle_name = ?, last_name = ?, preferred_name = ?, email = ? WHERE username = ?";

        // Prepare the statement and set the
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {

            // Set the parameters for the query
            pstmt.setString(1, firstName);
            pstmt.setString(2, middleName);
            pstmt.setString(3, lastName);
            pstmt.setString(4, preferredName);
            pstmt.setString(5, email);
            pstmt.setString(6, currentUsername);
            pstmt.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    /*
     *  Method to get all the user accounts in the database
     *
     * @return String [][]	Returns a 2D array of user accounts
     */
    public String[][] getAllUserAccounts() throws SQLException {

        //get all user accounts method to get all user accounts
        String query = "SELECT username, first_name, middle_name, last_name, role FROM users";

        // Prepare the statement and set
        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet resultSet = stmt.executeQuery(query)) {

            // Move to the last row to get the count of rows
            resultSet.last();
            int numRows = resultSet.getRow();
            resultSet.beforeFirst();

            // Create a 2D array to store the user accounts
            String[][] userAccounts = new String[numRows][5];
            int i = 0;

            // Iterate through the result set and store the user accounts
            while (resultSet.next()) {

                userAccounts[i][0] = resultSet.getString("username");
                userAccounts[i][1] = resultSet.getString("first_name");
                userAccounts[i][2] = resultSet.getString("middle_name");
                userAccounts[i][3] = resultSet.getString("last_name");
                userAccounts[i][4] = resultSet.getString("role");
                i++;

            }
            return userAccounts;
        }
    }


    /*
     *  Method to delete a user account using the username
     *
     * @param username The username of the user
     *
     */
    public void deleteUser(String username) {
        String query = "DELETE FROM users WHERE username = ?";
        System.out.println("Attempting to delete user with username: " + username); // Debugging statement

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            int rowsAffected = pstmt.executeUpdate(); // Capture how many rows were affected
            if (rowsAffected > 0) {
                System.out.println("User deleted successfully: " + username); // Debugging statement
            } else {
                System.out.println("No user found with username: " + username); // Debugging statement
            }
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage()); // Debugging statement for errors
            e.printStackTrace();
        }
    }


    /*
     *  Method to update the user account using the invitecode username password hash and role to update the user account
     *
     * @param inviteCode	The invite code to update the user account
     * @param username		The username of the user
     * @param passwordHash	The hashed password of the user
     * @param role			The role of the user
     *
     * @return boolean	Returns true if the user account is updated successfully, otherwise returns false
     */
    public boolean updateUserFromInviteCode(String inviteCode, String username, byte[] passwordHash, String role) throws SQLException {

        // SQL query to update the user account using the invite code
        String query = "UPDATE users SET username = ?, password_hash = ?, role = ? WHERE invite_code = ?";

        // Prepare the statement and set the parameters
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {

            // Set the parameters for the query
            pstmt.setString(1, username);
            pstmt.setBytes(2, passwordHash);
            pstmt.setString(3, role);
            pstmt.setString(4, inviteCode);

            // Execute the query and check if the user account is updated successfully
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }


    /*
     *  Method to get the password hash for the user using the username
     *
     * @param username	The username of the user
     *
     * @return byte []	Returns the password hash for the user
     */
    public byte[] getPasswordHash(String username) throws SQLException {

        // SQL query to get the password hash for the user
        System.out.println("Fetching password hash for username: " + username);
        String query = "SELECT password_hash FROM users WHERE username = ?";

        // Prepare the statement and set the parameters
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {

            // Set the parameters for the query
            pstmt.setString(1, username);
            ResultSet resultSet = pstmt.executeQuery();

            // Check if the result set has any rows
            if (resultSet.next()) {

                // Get the password hash from the result set
                byte[] passwordHash = resultSet.getBytes("password_hash");
                System.out.println("Retrieved password hash for user " + username + ": " + Arrays.toString(passwordHash));
                return passwordHash;  // Get the binary password hash

            } else {

                System.out.println("User " + username + " not found.");
            }
        }
        return null;
    }

    /*
     *  Method to set the one-time password for the user using the username, one-time password hash, and expiration date
     *
     * @param username	The username of the user
     * @param oneTimePasswordHash	The one-time password hash for the user
     * @param expirationDateTime	The expiration date and time of the one-time password
     *
     * @throws SQLException	Throws an SQLException if there is an error during setting the one-time password
     */
    public void setOneTimePassword(String username, byte[] oneTimePasswordHash, String expirationDateTime) throws SQLException {

        try {
            // Debugging statement to display input expiration date
            System.out.println("Original expiration date: " + expirationDateTime);

            // Convert the expirationDateTime from MM/DD/YYYY to YYYY-MM-DD
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Parse the expiration date and format it to the desired output
            LocalDate date = LocalDate.parse(expirationDateTime, inputFormatter);
            String formattedDate = date.format(outputFormatter);

            // Debugging statement to display formatted expiration date
            System.out.println("Formatted expiration date: " + formattedDate);

            // SQL query to update the user password and set the one-time password flag and expiration
            String query = "UPDATE users SET password_hash = ?, one_time_password = TRUE, password_expiration = ? WHERE username = ?";

            // Prepare the statement and set the parameters
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {

                // Set the parameters for the query
                pstmt.setBytes(1, oneTimePasswordHash);
                pstmt.setString(2, formattedDate); // Use formatted date
                pstmt.setString(3, username);
                int rowsAffected = pstmt.executeUpdate();
                System.out.println("Rows affected during OTP setup: " + rowsAffected); // Debugging statement
            }

        } catch (DateTimeParseException e) {

            System.err.println("Error parsing expiration date: " + e.getMessage());
        }
    }


    /*
     *  Method to check if the one-time password is valid for the user using the username and the current date and time
     *
     * @param username	The username of the user
     * @param currentDateTime	The current date and time
     *
     * @return boolean	Returns true if the one-time password is valid, otherwise returns false
     *
     */
    public boolean isOneTimePasswordValid(String username, String currentDateTime) throws SQLException {

        // SQL query to check if the one-time
        String query = "SELECT one_time_password, password_expiration FROM users WHERE username = ?";

        // Prepare the statement and set the parameters
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {

            // Set the parameters for the query
            pstmt.setString(1, username);
            ResultSet resultSet = pstmt.executeQuery();

            // Check if the result set has any rows
            if (resultSet.next()) {

                // Get the one-time password flag and the expiration date from the result set
                boolean isOneTimePassword = resultSet.getBoolean("one_time_password");
                String expirationDateTime = resultSet.getString("password_expiration");

                System.out.println("One-time password set: " + isOneTimePassword + ", Expiration date: " + expirationDateTime);
                System.out.println("Current date and time: " + currentDateTime);

                // Compare currentDateTime with expirationDateTime
                if (isOneTimePassword && currentDateTime.compareTo(expirationDateTime) <= 0) {

                    System.out.println("One-time password is valid and within expiration date.");
                    return true;

                } else {

                    System.out.println("One-time password is invalid or expired.");
                    return false;
                }
            }
        }
        return false;
    }


    /*
     *  Method to update the password after OTP validation using the username and
     * the new password hash
     *
     * @param username The username of the user
     *
     * @param newPasswordHash The new password hash for the user
     *
     * @throws SQLException Throws an SQLException if there is an error during
     * updating the password
     */
    public void updatePasswordAfterOtp(String username, byte[] newPasswordHash) throws SQLException {

        // SQL query to update the password after OTP validation
        String query = "UPDATE users SET password_hash = ?, one_time_password = FALSE, password_expiration = NULL WHERE username = ?";
        System.out.println("Updating password after OTP validation for user: " + username); // Debugging statement

        // Prepare the statement and set the parameters
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {

            // Set the parameters for the query
            pstmt.setBytes(1, newPasswordHash);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Password updated, rows affected: " + rowsAffected); // Debugging statement
        }
    }

    /*
     *  Method to check if the one-time password is set for the user using the
     * username
     *
     * @param username The username of the user
     *
     * @return boolean Returns true if the one-time password is set, otherwise
     * returns false
     */
    public boolean isOneTimePasswordSet(String username) throws SQLException {

        // SQL query to check if the one-time password is set for
        String query = "SELECT one_time_password FROM users WHERE username = ?";

        // Prepare the statement and set the parameters
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {

            // Set the parameters for the query
            pstmt.setString(1, username);
            ResultSet resultSet = pstmt.executeQuery();

            // Check if the result set has any rows
            if (resultSet.next()) {
                boolean isOneTimePassword = resultSet.getBoolean("one_time_password");
                System.out.println("Is one-time password set for user " + username + ": " + isOneTimePassword);
                return isOneTimePassword;
            }
        }
        return false;
    }



}
