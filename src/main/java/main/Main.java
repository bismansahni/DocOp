package main;


import java.sql.SQLException;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/*******
 *  <p>Title: Main Class.</p>
 *
 *  <p>Description: A JavaFX application: This controller class describes the user interface for the Help System. </p>
 *
 *  <p>Copyright: CSE 360 Group : Sri Ujjwal Reddy, Bisman, Sreechand, Ansh Dani, Krishna Balaji, Kyle Knowwles @ 2024</p>
 *
 * @author Sri Ujjwal Reddy, Bisman, Sreechand, Ansh Dani, Krishna Balaji, Kyle Knowwles
 *
 * @version 1.0.0  1 is Phase 1 and 0 is the iteration of the phase 1 and 0 is the task completed in the iteration 0
 *
 */

/*  The Main class is the entry point for the JavaFX application. It creates the initial layout for the application*/
public class Main extends Application {

    /**
     * Default constructor for the Main class.
     */
    public Main() {
    }

    //Create a DatabaseHelper object to interact with the database
    private DatabaseHelper dbHelper = new DatabaseHelper();

    // Create LoginManager and SessionManager objects to handle login and session management
    private LoginManager loginManager;

    // Create a SessionManager object to manage the user session
    private SessionManager sessionManager;

    @Override
    public void start(Stage primaryStage) {
        try {

            //Connect to the database
            dbHelper.connectToDatabase();

            //Create a LoginManager object to handle login
            loginManager = new LoginManager(dbHelper);

            //Create a SessionManager object to manage the user session
            this.sessionManager = new SessionManager();

            // If no users exist in the database, redirect to Admin creation
            if (dbHelper.isDatabaseEmpty()) {

                //Create a new Admin
                GridPane adminCreationLayout = createAdminCreationLayout(primaryStage);

                //Create a new scene for the Admin
                Scene adminScene = new Scene(adminCreationLayout, 800, 450);

                //Add the css file to the scene
//                adminScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

                //Set the title of the stage
                primaryStage.setTitle("Create Admin");

                //Set the scene for the stage
                primaryStage.setScene(adminScene);

            } else {

                //Create a new login layout
                GridPane loginLayout = createLoginLayout(primaryStage);

                //Create a new scene for the login
                Scene loginScene = new Scene(loginLayout, 800, 450);

                //Add the css file to the scene
                loginScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

                //Set the title of the stage
                primaryStage.setTitle("Login");

                //Set the scene for the stage
                primaryStage.setScene(loginScene);
            }

            primaryStage.show();

        } catch (Exception e) {

            //Print the stack
            e.printStackTrace();
        }
    }

    // Admin creation page layout that allows creating the first admin user
    private GridPane createAdminCreationLayout(Stage stage) {

        //Create a new GridPane layout
        GridPane layout = new GridPane();
        layout.setHgap(10); // Horizontal gap between elements
        layout.setVgap(10); // Vertical gap between elements
        layout.setPadding(new Insets(20, 20, 20, 20)); // Padding around the grid

        //Create a new Label for the username of the admin
        Label usernameLabel = new Label("Create Admin - Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter admin username");

        //Create a new Label for the password of the admin
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");

        //Create a new Label for the confirm password of the admin
        Label confirmPasswordLabel = new Label("Confirm Password:");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm password");

        //Create a new Label for the message
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        //Create a new button to create the admin
        Button createAdminButton = new Button("Create Admin");

        //Create a new button to go back
        createAdminButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (!password.equals(confirmPassword)) {

                //Print the message if the passwords do not match
                messageLabel.setText("Passwords do not match!");
                return;
            }

            // Validate the password with PasswordEvaluator
            String passwordError = PasswordEvaluator.evaluatePassword(password);

            //If the password is not empty
            if (!passwordError.isEmpty()) {

                //Print the message if the password is not empty
                messageLabel.setText(passwordError);
                return;
            }

            // Create Admin in the database
            try {

                //Register the admin
                loginManager.register(username, password, "Admin");
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Admin created successfully!");

                // Redirect to login after creation
                GridPane loginLayout = createLoginLayout(stage);
                stage.getScene().setRoot(loginLayout);

            } catch (Exception ex) {

                //if the exception occurs, print the stack trace
                ex.printStackTrace();
                messageLabel.setText("Database error");
            }
        });

        //Create a new button to go back
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {

            //Create a new layout for the login
            GridPane loginLayout = createLoginLayout(stage);
            stage.getScene().setRoot(loginLayout);
        });

        //setting grid positions for the elements
        GridPane.setConstraints(usernameLabel, 0, 0);
        GridPane.setConstraints(usernameField, 1, 0);
        GridPane.setConstraints(passwordLabel, 0, 1);
        GridPane.setConstraints(passwordField, 1, 1);
        GridPane.setConstraints(confirmPasswordLabel, 0, 2);
        GridPane.setConstraints(confirmPasswordField, 1, 2);
        GridPane.setConstraints(messageLabel, 1, 3);
        GridPane.setConstraints(createAdminButton, 1, 4);
        GridPane.setConstraints(backButton, 1, 5);

        //Add the elements to the layout
        layout.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField,
                confirmPasswordLabel, confirmPasswordField, messageLabel, createAdminButton, backButton);

        return layout;
    }


    /* Method to create the login layout */
    private GridPane createLoginLayout(Stage stage) {

        //Create a new GridPane layout
        GridPane layout = new GridPane();
        layout.setHgap(10); // Horizontal gap between elements
        layout.setVgap(10); // Vertical gap between elements
        layout.setPadding(new Insets(20, 20, 20, 20)); // Padding around the grid

        //Create a new Label for the role
        Label roleLabel = new Label("Sign in as:");
        ChoiceBox<String> roleBox = new ChoiceBox<>();
        roleBox.getItems().addAll("Admin", "Student", "Instructor");
        roleBox.setValue("Student");

        //Create a new Label for the username
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");

        //Create a new Label for the password
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");

        //Create a new Label for the message
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        //Create a new button for the
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        // Login button action
        loginButton.setOnAction(e -> {

            //Get the username, password and role
            String username = usernameField.getText();
            String password = passwordField.getText();
            String role = roleBox.getValue();
            String currentDateTime = getCurrentDateTime(); // Get current date and time

            sessionManager.setRoles(new String[]{role}); // Store the role in session
            sessionManager.setUsername(username); // Store the username in session

            try {

                // Call the login method from the LoginManager class
                String loginResult = loginManager.login(username, password, role, currentDateTime);

                // Handle the result based on the login method's output
                if (loginResult.equals("Login successful")) {

                    // If login is successful, redirect to the user dashboard
                    GridPane userDashboardLayout = createUserDashboard(stage, role);
                    stage.getScene().setRoot(userDashboardLayout);

                } else if (loginResult.equals("OTP login successful, please reset your password.")) {

                    // If OTP login is successful, prompt the user to reset their password
                    GridPane passwordResetLayout = createPasswordResetLayout(stage, username);
                    stage.getScene().setRoot(passwordResetLayout);

                } else if (loginResult.equals("Account setup incomplete")) {

                    //if the account setup is incomplete, redirect to the setup layout
                    GridPane setupLayout = createSetupLayout(stage);
                    stage.getScene().setRoot(setupLayout);

                } else {

                    // If login fails, show the error message
                    messageLabel.setText(loginResult);
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                messageLabel.setText("Database error.");
            }
        });

        // Register button action
        registerButton.setOnAction(e -> {
            GridPane registerLayout = createRegisterLayout(stage);
            stage.getScene().setRoot(registerLayout);
        });


        // Set grid positions for elements in createLoginLayout
        GridPane.setConstraints(roleLabel, 0, 0);
        GridPane.setConstraints(roleBox, 1, 0);
        GridPane.setConstraints(usernameLabel, 0, 1);
        GridPane.setConstraints(usernameField, 1, 1);
        GridPane.setConstraints(passwordLabel, 0, 2);
        GridPane.setConstraints(passwordField, 1, 2);
        GridPane.setConstraints(loginButton, 1, 3);
        GridPane.setConstraints(registerButton, 1, 4);
        GridPane.setConstraints(messageLabel, 1, 5);

        // Add elements to the layout
        layout.getChildren().addAll(roleLabel, roleBox, usernameLabel, usernameField, passwordLabel, passwordField, loginButton, registerButton, messageLabel);

        return layout;
    }


    // Method to prompt the user to reset their password after OTP login
    private GridPane createPasswordResetLayout(Stage stage, String username) {

        //Create a new GridPane layout
        GridPane layout = new GridPane();
        layout.setHgap(10); // Horizontal gap between elements
        layout.setVgap(10); // Vertical gap between elements
        layout.setPadding(new Insets(20, 20, 20, 20)); // Padding around the grid

        //Create a new Label for the new password
        Label newPasswordLabel = new Label("New Password:");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password");

        //Create a new Label for the confirm new password
        Label confirmNewPasswordLabel = new Label("Confirm New Password:");
        PasswordField confirmNewPasswordField = new PasswordField();
        confirmNewPasswordField.setPromptText("Confirm new password");

        //Create a new Label for the message
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        //Create a new button to reset the password
        Button resetPasswordButton = new Button("Reset Password");

        // Reset password button action
        resetPasswordButton.setOnAction(e -> {

            String newPassword = newPasswordField.getText();					//Get the new password
            String confirmNewPassword = confirmNewPasswordField.getText();		//Get the confirm new password

            // Check if the new password and confirm password
            if (!newPassword.equals(confirmNewPassword)) {

                //Print the message if the passwords do not match
                System.out.println("Password reset failed: Passwords do not match");
                messageLabel.setText("Passwords do not match!");
                return;
            }

            // Validate the password with PasswordEvaluator
            String resetMessage = loginManager.resetPassword(username, newPassword);
            System.out.println("Password reset result for user " + username + ": " + resetMessage);
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText(resetMessage);

            // Redirect to login page after password reset
            GridPane loginLayout = createLoginLayout(stage);
            stage.getScene().setRoot(loginLayout);
        });

        // Set grid positions for elements in createPasswordResetLayout
        GridPane.setConstraints(newPasswordLabel, 0, 0);
        GridPane.setConstraints(newPasswordField, 1, 0);
        GridPane.setConstraints(confirmNewPasswordLabel, 0, 1);
        GridPane.setConstraints(confirmNewPasswordField, 1, 1);
        GridPane.setConstraints(resetPasswordButton, 1, 2);
        GridPane.setConstraints(messageLabel, 1, 3);

        // Add elements to the layout
        layout.getChildren().addAll(newPasswordLabel, newPasswordField, confirmNewPasswordLabel, confirmNewPasswordField, resetPasswordButton, messageLabel);
        return layout;
    }

    // Helper method to get the current date and time as a string
    private String getCurrentDateTime() {

        // Implement the logic to return current date and time in the required format (e.g., yyyy-MM-dd HH:mm:ss)
        return java.time.LocalDateTime.now().toString();
    }

    // Method to generate a random invite code
    private GridPane createRegisterLayout(Stage stage) {

        //Create a new GridPane layout
        GridPane layout = new GridPane();
        layout.setHgap(10); 								// Horizontal gap between elements
        layout.setVgap(10); 								// Vertical gap between elements
        layout.setPadding(new Insets(20, 20, 20, 20)); 		// Padding around the grid

        //Create a new Label for the invite code
        Label inviteCodeLabel = new Label("Invite Code:");
        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter invite code");

        //Create a new Label for the username
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");

        //Create a new Label for the password
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");

        //Create a new Label for the confirm password
        Label confirmPasswordLabel = new Label("Confirm Password:");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm password");

        //Create a new Label for the message
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        //Create a new button to register
        Button registerButton = new Button("Register");

        // Register button action
        registerButton.setOnAction(e -> {
            String inviteCode = inviteCodeField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // Check if the passwords match
            if (!password.equals(confirmPassword)) {
                messageLabel.setText("Passwords do not match!");
                return;
            }

            // Validate the password with PasswordEvaluator
            String passwordError = PasswordEvaluator.evaluatePassword(password);
            if (!passwordError.isEmpty()) {
                messageLabel.setText(passwordError);
                return;
            }

            // Register user with invite code
            try {

                //Register the invite code
                String message = loginManager.registerInvite(inviteCode, username, password);
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText(message);

                // Redirect to login after registration
                GridPane loginLayout = createLoginLayout(stage);
                stage.getScene().setRoot(loginLayout);

            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Database error");
            }
        });

        // Create a back button to return to the login page
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            GridPane loginLayout = createLoginLayout(stage);
            stage.getScene().setRoot(loginLayout);
        });

        // Set grid positions for elements in createRegisterLayout
        GridPane.setConstraints(inviteCodeLabel, 0, 0);
        GridPane.setConstraints(inviteCodeField, 1, 0);
        GridPane.setConstraints(usernameLabel, 0, 1);
        GridPane.setConstraints(usernameField, 1, 1);
        GridPane.setConstraints(passwordLabel, 0, 2);
        GridPane.setConstraints(passwordField, 1, 2);
        GridPane.setConstraints(confirmPasswordLabel, 0, 3);
        GridPane.setConstraints(confirmPasswordField, 1, 3);
        GridPane.setConstraints(registerButton, 1, 4);
        GridPane.setConstraints(messageLabel, 1, 5);
        GridPane.setConstraints(backButton, 1, 6);

        // Add elements to the layout
        layout.getChildren().addAll(inviteCodeLabel, inviteCodeField, usernameLabel, usernameField, passwordLabel,
                passwordField, confirmPasswordLabel, confirmPasswordField, registerButton, messageLabel, backButton);

        return layout;
    }


    // Method to create the setup layout
    private GridPane createSetupLayout(Stage stage) {

        //Create a new GridPane layout
        GridPane layout = new GridPane();
        layout.setHgap(10); // Horizontal gap between elements
        layout.setVgap(10); // Vertical gap between elements
        layout.setPadding(new Insets(20, 20, 20, 20)); // Padding around the grid

        // Fetch the current username from the session
        String currentUsername = sessionManager.getUsername();
        System.out.println("Current Username during setup: " + currentUsername); 					// Debugging statement

        // Make sure this prints the username correctly
        if (currentUsername == null) {
            System.out.println("Username is null, something went wrong with login/session");		// Debugging statement
        }

        // Proceed with setup
        String[] currentRoles = sessionManager.getRoles();

        //create a new Label for the first name
        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Enter first name");

        //create a new Label for the middle name
        Label middleNameLabel = new Label("Middle Name:");
        TextField middleNameField = new TextField();
        middleNameField.setPromptText("Enter middle name");

        //create a new Label for the last name
        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Enter last name");

        //create a new Label for the preferred name
        Label preferredNameLabel = new Label("Preferred Name:");
        TextField preferredNameField = new TextField();
        preferredNameField.setPromptText("Enter preferred name");

        //create a new Label for the email
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter email");

        //create a new Label for the
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        //create a new button to setup
        Button setupButton = new Button("Setup");

        // Setup button action
        setupButton.setOnAction(e -> {

            //Get the first name, middle name, last name, preferred name and email
            String firstName = firstNameField.getText();
            String middleName = middleNameField.getText();
            String lastName = lastNameField.getText();
            String preferredName = preferredNameField.getText();
            String email = emailField.getText();

            // Validate the email address
            if (!EmailValidator.validateEmail(email)) {

                //Print the message if the email is invalid
                messageLabel.setText("Invalid email address!");
                return;
            }

            try {

                //Setup the user details
                dbHelper.setupUserDetails(currentUsername, firstName, middleName, lastName, preferredName, email);
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Account setup complete!");

                // Redirect to login after setup
                GridPane loginLayout = createLoginLayout(stage);
                stage.getScene().setRoot(loginLayout);

            } catch (Exception ex) {

                //Print the stack trace if the exception occurs
                ex.printStackTrace();
                messageLabel.setText("Database error");
            }
        });

        // Create a back button to return to the login page
        Button backButton = new Button("Back");

        // Back button action
        backButton.setOnAction(e -> {

            //Create a new layout for the login
            GridPane loginLayout = createLoginLayout(stage);
            stage.getScene().setRoot(loginLayout);
        });

        // Set grid positions for elements in createSetupLayout
        GridPane.setConstraints(firstNameLabel, 0, 0);
        GridPane.setConstraints(firstNameField, 1, 0);
        GridPane.setConstraints(middleNameLabel, 0, 1);
        GridPane.setConstraints(middleNameField, 1, 1);
        GridPane.setConstraints(lastNameLabel, 0, 2);
        GridPane.setConstraints(lastNameField, 1, 2);
        GridPane.setConstraints(preferredNameLabel, 0, 3);
        GridPane.setConstraints(preferredNameField, 1, 3);
        GridPane.setConstraints(emailLabel, 0, 4);
        GridPane.setConstraints(emailField, 1, 4);
        GridPane.setConstraints(setupButton, 1, 5);
        GridPane.setConstraints(messageLabel, 1, 6);
        GridPane.setConstraints(backButton, 1, 7);

        // Add elements to the layout
        layout.getChildren().addAll(firstNameLabel, firstNameField, middleNameLabel, middleNameField, lastNameLabel,
                lastNameField, preferredNameLabel, preferredNameField, emailLabel, emailField, setupButton, messageLabel, backButton);

        return layout;
    }

    // Method to create the user dashboard
    private GridPane createUserDashboard(Stage stage, String currentRole) throws SQLException {

        // Create a larger scene size for the dashboard
        Scene dashboardScene = new Scene(new GridPane(), 900, 600);

        // Create a new GridPane layout for the dashboard
        GridPane layout = new GridPane();
        layout.setHgap(20); 													// Increased horizontal gap between elements
        layout.setVgap(20);														// Increased vertical gap between elements
        layout.setPadding(new Insets(30, 30, 30, 30)); 							// More padding for better spacing

        // Fetch the current username from the session
        String currentUsername = sessionManager.getUsername();
        Label userDetailsLabel = new Label("Logged-in User: " + currentUsername);

        // Fetch user details and roles from the database
        String[] userDetails = dbHelper.getUserDetails(currentUsername);
        String[] userRoles = dbHelper.getRolesForUser(currentUsername);

        // To avoid null pointer exception
        String rolesString = (userRoles != null) ? String.join(", ", userRoles) : "";

        // Create labels for user details with User Full Name, Email, and Roles
        Label firstNameLabel = new Label("First Name: " + userDetails[0]);
        Label middleNameLabel = new Label("Middle Name: " + userDetails[1]);
        Label lastNameLabel = new Label("Last Name: " + userDetails[2]);
        Label emailLabel = new Label("Email: " + userDetails[4]);
        Label rolesLabel = new Label("Roles: " + rolesString);

        // Create a ChoiceBox for changing roles
        ChoiceBox<String> roleBox = new ChoiceBox<>();
        roleBox.getItems().addAll(userRoles);
        roleBox.setValue(currentRole);

        // Handle role change in the ChoiceBox
        roleBox.setOnAction(e -> {

            //Get the selected role
            String selectedRole = roleBox.getValue();
            try {

                //create a new scene for the dashboard
                GridPane newDashboardLayout = createUserDashboard(stage, selectedRole);
                stage.getScene().setRoot(newDashboardLayout);

            } catch (SQLException ex) {

                //Print the stack trace if the exception occurs
                ex.printStackTrace();
            }
        });

        // Set grid positions for elements in createUserDashboard (Admin)
        GridPane.setConstraints(userDetailsLabel, 0, 0);
        GridPane.setConstraints(firstNameLabel, 0, 1);
        GridPane.setConstraints(middleNameLabel, 0, 2);
        GridPane.setConstraints(lastNameLabel, 0, 3);
        GridPane.setConstraints(emailLabel, 0, 4);
        GridPane.setConstraints(rolesLabel, 0, 5);
        GridPane.setConstraints(roleBox, 1, 5);

        // Add elements to the layout
        layout.getChildren().addAll(userDetailsLabel, firstNameLabel, middleNameLabel, lastNameLabel, emailLabel, rolesLabel, roleBox);

        if (currentRole.equals("Admin")) {

            // Table for all users
            TableView<String[]> userTable = createUserTable();
            layout.getChildren().add(userTable);

            // Label to show selected user
            Label selectedUserLabel = new Label("Selected User: None");

            // Handle user selection in table
            userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {

                //Get the selected user
                if (newSelection != null) {
                    String selectedUsername = newSelection[0];
                    selectedUserLabel.setText("Selected User: " + selectedUsername);
                }

            });

            // Role ChoiceBox for adding/removing roles
            ChoiceBox<String> roleChoiceBox = new ChoiceBox<>();
            roleChoiceBox.getItems().addAll("Admin", "Student", "Instructor"); 		// Valid roles for adding/removing
            roleChoiceBox.setValue("Student"); 										// Default role

            // Add action buttons
            Button addRoleButton = new Button("Add Role");
            Button removeRoleButton = new Button("Remove Role");
            Button resetUserButton = new Button("Reset User");
            Button deleteUserButton = new Button("Delete User");
            Button addUserButton = new Button("Add User");
            Button logoutButton = new Button("Logout");

            // Apply button CSS style for consistency
            String buttonStyle = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-size: 14px;";
            addRoleButton.setStyle(buttonStyle);
            removeRoleButton.setStyle(buttonStyle);
            resetUserButton.setStyle(buttonStyle);
            deleteUserButton.setStyle(buttonStyle);
            addUserButton.setStyle(buttonStyle);
            logoutButton.setStyle(buttonStyle);

            //message
            Label messageLabel = new Label();
            messageLabel.setStyle("-fx-text-fill: red;");

            // Add role action
            addRoleButton.setOnAction(e -> {

                //get the selected role
                String selectedRole = roleChoiceBox.getValue();
                String[] selectedUser = userTable.getSelectionModel().getSelectedItem();

                //if a user is selected
                if (selectedUser != null) {

                    //get the username of the user
                    String username = selectedUser[0];
                    try {

                        //call the addRoleToUser function to add a role to the user if not already exists
                        dbHelper.addRoleToUser(username, selectedRole);
                        messageLabel.setText("Role added successfully!");
                        messageLabel.setStyle("-fx-text-fill: green;");
                        userTable.setItems(FXCollections.observableArrayList(dbHelper.getAllUserAccounts()));

                    } catch (SQLException ex) {

                        ex.printStackTrace();
                        messageLabel.setText("Error adding role.");

                    }

                } else {
                    messageLabel.setText("No user selected!");
                }
            });

            // Remove role action
            removeRoleButton.setOnAction(e -> {

                //get the selected role of from the choice box input
                String selectedRole = roleChoiceBox.getValue();
                String[] selectedUser = userTable.getSelectionModel().getSelectedItem();

                //if the user os selected
                if (selectedUser != null) {

                    String username = selectedUser[0];

                    try {

                        //call the removeRoleFrom user function to remove a role to the function if doesn't exist already
                        dbHelper.removeRoleFromUser(username, selectedRole);
                        messageLabel.setText("Role removed successfully!");
                        messageLabel.setStyle("-fx-text-fill: green;");
                        userTable.setItems(FXCollections.observableArrayList(dbHelper.getAllUserAccounts()));

                    } catch (SQLException ex) {

                        //print the error stack
                        ex.printStackTrace();
                        messageLabel.setText("Error removing role.");

                    }

                } else {

                    //message
                    messageLabel.setText("No user selected!");

                }
            });

            // Reset user action
            resetUserButton.setOnAction(e -> {

                //get the selected user
                String[] selectedUser = userTable.getSelectionModel().getSelectedItem();

                //if a user is selected
                if (selectedUser != null) {

                    //Get the username and create the reset user page to reset the user
                    String username = selectedUser[0];
                    GridPane resetUserLayout = createResetUserLayout(stage, username);
                    stage.getScene().setRoot(resetUserLayout);

                } else {

                    //appropriate message if the user is not selected
                    messageLabel.setText("No user selected!");
                }
            });

            // Delete user action
            deleteUserButton.setOnAction(e -> {

                //get the current user selected
                String[] selectedUser = userTable.getSelectionModel().getSelectedItem();

                //if a user is selected
                if (selectedUser != null) {

                    //get the user and go to the delete confirmation page to ask for confirmation from the admin
                    String username = selectedUser[0];
                    GridPane deleteConfirmationLayout = createDeleteConfirmationLayout(stage, username, userDetails);
                    stage.getScene().setRoot(deleteConfirmationLayout);

                } else {

                    //appropriate message if the user is not selected
                    messageLabel.setText("No user selected!");
                }
            });

            // Add user action
            addUserButton.setOnAction(e -> {
                GridPane addUserLayout = createAddUserLayout(stage);
                stage.getScene().setRoot(addUserLayout);
            });

            // Logout action
            logoutButton.setOnAction(e -> {
                sessionManager.logout();
                GridPane loginLayout = createLoginLayout(stage);
                stage.getScene().setRoot(loginLayout);
            });

            // Initialize the action buttons layout (use HBox for horizontal layout)
            HBox actionButtonsLayout = new HBox(15); // Increased spacing between buttons for better layout
            actionButtonsLayout.getChildren().addAll(roleChoiceBox, addRoleButton, removeRoleButton, resetUserButton, deleteUserButton, addUserButton);

            // Set grid constraints for Admin elements
            GridPane.setConstraints(userTable, 0, 6, 2, 1);
            GridPane.setConstraints(selectedUserLabel, 0, 7);
            GridPane.setConstraints(actionButtonsLayout, 0, 8, 2, 1);
            GridPane.setConstraints(logoutButton, 1, 9);

            // Add Admin-specific elements to layout
            layout.getChildren().addAll(selectedUserLabel, actionButtonsLayout, logoutButton);

        } else if (currentRole.equals("Student") || currentRole.equals("Instructor")) {

            // Create a new Label for the personal details
            Label personalDetailsLabel = new Label("Personal Details");
            layout.getChildren().add(personalDetailsLabel);

            // Set grid constraints for Student/Instructor elements
            Button logoutButton = new Button("Logout");

            logoutButton.setOnAction(e -> {

                //logout the user
                sessionManager.logout();
                GridPane loginLayout = createLoginLayout(stage);
                stage.getScene().setRoot(loginLayout);

            });

            GridPane.setConstraints(logoutButton, 6, 0);

            layout.getChildren().add(logoutButton);
        }

        // Set the new scene size specifically for the dashboard for better visibility
        stage.setScene(new Scene(layout, 900, 600));

        return layout;
    }



    // Method to display all users in a TableView
    private TableView<String[]> createUserTable() throws SQLException {
        TableView<String[]> tableView = new TableView<>();

        // Define columns for Username, Name, and Roles
        TableColumn<String[], String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));

        // Combine first name and last name into a single column
        TableColumn<String[], String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1] + " " + data.getValue()[2]));

        // Display roles as a comma-separated list
        TableColumn<String[], String> rolesCol = new TableColumn<>("Roles");
        rolesCol.setCellValueFactory(data -> new SimpleStringProperty(String.join(", ", data.getValue()[4].split(","))));

        // Add columns to the TableView
        tableView.getColumns().add(usernameCol);
        tableView.getColumns().add(nameCol);
        tableView.getColumns().add(rolesCol);

        // Get user data from the database using the helper method getAllUserAccounts and convert to ObservableList
        String[][] allUsers = dbHelper.getAllUserAccounts();
        ObservableList<String[]> usersData = FXCollections.observableArrayList(allUsers);

        // Set data to the table
        tableView.setItems(usersData);

        return tableView;
    }


    // Method to create the delete confirmation layout
    private GridPane createAddUserLayout(Stage stage) {

        //Create a new GridPane layout
        GridPane layout = new GridPane();
        layout.setHgap(10); // Horizontal gap between elements
        layout.setVgap(10); // Vertical gap between elements
        layout.setPadding(new Insets(20, 20, 20, 20)); // Padding around the grid

        //Create a new Label for the invite code
        Label inviteCodeLabel = new Label("Invite Code:");
        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Generated Invite Code");

        //Create a new Label for the role
        Label roleLabel = new Label("Role:");
        ChoiceBox<String> roleBox = new ChoiceBox<>();
        roleBox.getItems().addAll("Admin", "Student", "Instructor");
        roleBox.setValue("Student");

        //Create a new Label for the message
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        //Create a new button to generate invite code
        Button generateInviteCodeButton = new Button("Generate Invite Code");

        generateInviteCodeButton.setOnAction(e -> {

            //Generate a random invite code using the generateInviteCode function
            String generatedCode = generateInviteCode(10);
            inviteCodeField.setText(generatedCode);

        });

        //Create a new button to add
        Button addUserButton = new Button("Add User");

        // Add user button action
        addUserButton.setOnAction(e -> {

            //Get the invite code and role
            String inviteCode = inviteCodeField.getText();
            String role = roleBox.getValue();

            try {

                //call the addUser function to add the user with the invite code and role
                String message = dbHelper.addUser(inviteCode, role);
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText(message);

            } catch (Exception ex) {

                //print the stack trace if the exception occurs
                ex.printStackTrace();
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Database error");
            }

        });

        // Create a back button to return to the user dashboard
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {

            try {

                //create a new user dashboard for the admin
                stage.getScene().setRoot(createUserDashboard(stage, "Admin"));

            } catch (SQLException e1) {

                e1.printStackTrace();
            }
        });

        // Set grid positions for elements in createAddUserLayout
        GridPane.setConstraints(inviteCodeLabel, 0, 0);
        GridPane.setConstraints(inviteCodeField, 1, 0);
        GridPane.setConstraints(roleLabel, 0, 1);
        GridPane.setConstraints(roleBox, 1, 1);
        GridPane.setConstraints(generateInviteCodeButton, 1, 2);
        GridPane.setConstraints(addUserButton, 1, 3);
        GridPane.setConstraints(messageLabel, 1, 4);
        GridPane.setConstraints(backButton, 1, 5);

        // Add elements to the layout
        layout.getChildren().addAll(inviteCodeLabel, inviteCodeField, roleLabel, roleBox, generateInviteCodeButton, addUserButton, messageLabel, backButton);

        return layout;
    }

    // Helper method to generate invite code
    private String generateInviteCode(int i) {

        //Define the characters to be used in the invite code
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        //Create a new StringBuilder object
        StringBuilder code = new StringBuilder();

        //Generate a random invite code
        while (i > 0) {

            // Get a random index from the characters
            int index = (int) (Math.random() * characters.length());
            code.append(characters.charAt(index));
            i--;
        }

        //Return the generated invite code
        return code.toString();
    }

    // Method to create the reset user
    private GridPane createResetUserLayout(Stage stage, String username) {

        //Create a new GridPane layout
        GridPane layout = new GridPane();
        layout.setHgap(10);
        layout.setVgap(10);
        layout.setPadding(new Insets(20, 20, 20, 20));

        //Create a new Label for the username
        Label usernameLabel = new Label("Username: " + username);

        //Create a new Label for the one time
        Label oneTimePasswordLabel = new Label("One Time Password:");
        TextField oneTimePasswordField = new TextField();
        oneTimePasswordField.setPromptText("Enter one-time password");

        //Create a new Label for the expiration date
        Label expirationDateTimeLabel = new Label("Expiration Date and Time:");
        TextField expirationDateTimeField = new TextField();
        expirationDateTimeField.setPromptText("Enter expiration date (MM/DD/YYYY)");

        //Create a new Label for the
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        //Create a new button to reset the user
        Button resetUserButton = new Button("Reset User");

        resetUserButton.setOnAction(e -> {

            //Get the one time password and expiration
            String oneTimePassword = oneTimePasswordField.getText();
            String expirationDateTime = expirationDateTimeField.getText();

            // Use DateRecognizer to validate the expiration date
            String dateValidationMessage = DateRecognizer.checkForValidDate(expirationDateTime);

            if (!dateValidationMessage.isEmpty()) {

                // If date is invalid, show the error message
                messageLabel.setText("Invalid expiration date: " + dateValidationMessage);
                return;
            }

            //if the date is valid, reset the user
            try {

                //call the resetUser function to reset the user
                String message = dbHelper.resetUser(username, oneTimePassword, expirationDateTime);

                //if the message is password reset successfully
                if (message.equals("Password reset successfully")) {

                    messageLabel.setStyle("-fx-text-fill: green;");

                } else {

                    //if the message is not password reset successfully
                    messageLabel.setStyle("-fx-text-fill: red;");
                }

                messageLabel.setText(message);

            } catch (Exception ex) {

                //print the stack trace if the exception occurs
                ex.printStackTrace();
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Database error");
            }
        });

        // Create a back button to return to the user dashboard
        Button backButton = new Button("Back");

        // Back button action
        backButton.setOnAction(e -> {

            //create a new user dashboard for the admin
            try {

                //create a new user dashboard for the admin
                stage.getScene().setRoot(createUserDashboard(stage, "Admin"));

            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });

        // Set grid positions for elements in createResetUserLayout
        GridPane.setConstraints(usernameLabel, 0, 0);
        GridPane.setConstraints(oneTimePasswordLabel, 0, 1);
        GridPane.setConstraints(oneTimePasswordField, 1, 1);
        GridPane.setConstraints(expirationDateTimeLabel, 0, 2);
        GridPane.setConstraints(expirationDateTimeField, 1, 2);
        GridPane.setConstraints(resetUserButton, 1, 3);
        GridPane.setConstraints(messageLabel, 1, 4);
        GridPane.setConstraints(backButton, 1, 5);

        // Add elements to the layout
        layout.getChildren().addAll(usernameLabel, oneTimePasswordLabel, oneTimePasswordField, expirationDateTimeLabel, expirationDateTimeField, resetUserButton, messageLabel, backButton);

        return layout;
    }

    // Method to create the delete confirmation
    private GridPane createDeleteConfirmationLayout(Stage stage, String username, String[] userDetails) {

        GridPane layout = new GridPane();
        layout.setHgap(10);
        layout.setVgap(10);
        layout.setPadding(new Insets(20, 20, 20, 20));

        //Create a new Label for the confirmation
        Label confirmationLabel = new Label("Are you sure you want to delete the user?");
        Label usernameLabel = new Label("Username: " + username);
        Label rolesLabel = new Label("Roles: " + userDetails[4]);

        //Create a new Label for the message
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        //Create a new button to confirm the deletion
        Button yesButton = new Button("Yes");

        // Yes button action
        yesButton.setOnAction(e -> {

            //if the user is confirmed to be deleted
            try {

                //call the deleteUser function to delete the user
                dbHelper.deleteUser(username);
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("User deleted successfully!");

                // Redirect to user dashboard after deletion
                GridPane userDashboardLayout = createUserDashboard(stage, "Admin");
                stage.getScene().setRoot(userDashboardLayout);

            } catch (SQLException ex) {

                ex.printStackTrace();
                messageLabel.setText("Error deleting user.");
            }

        });

        //Create a new button to cancel the deletion
        Button noButton = new Button("No");

        // No button action
        noButton.setOnAction(e -> {
            try {

                //create a new user dashboard for the admin
                GridPane userDashboardLayout = createUserDashboard(stage, "Admin");
                stage.getScene().setRoot(userDashboardLayout);

            } catch (SQLException ex) {

                ex.printStackTrace();
            }
        });

        // Create a back button to return to the user dashboard
        Button backButton = new Button("Back");

        // Back button action
        backButton.setOnAction(e -> {

            //create a new user dashboard for the  admin
            try {

                GridPane userDashboardLayout = createUserDashboard(stage, "Admin");
                stage.getScene().setRoot(userDashboardLayout);

            } catch (SQLException ex) {

                ex.printStackTrace();
            }
        });

        // Set grid positions for elements in createDeleteConfirmationLayout
        GridPane.setConstraints(confirmationLabel, 0, 0, 2, 1);  // Span two columns
        GridPane.setConstraints(usernameLabel, 0, 1);
        GridPane.setConstraints(rolesLabel, 0, 2);
        GridPane.setConstraints(yesButton, 0, 3);
        GridPane.setConstraints(noButton, 1, 3);
        GridPane.setConstraints(messageLabel, 0, 4);
        GridPane.setConstraints(backButton, 0, 5);

        // Add elements to the layout
        layout.getChildren().addAll(confirmationLabel, usernameLabel, rolesLabel, yesButton, noButton, messageLabel, backButton);

        return layout;
    }

    /**
     * The main entry point for the application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /*
     *  The start method is the entry point for JavaFX applications.
     */
    @Override
    public void stop() {

        //close the database connection
        dbHelper.closeConnection();
    }
}