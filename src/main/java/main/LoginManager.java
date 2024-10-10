package main;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * <p>LoginManager class handles the logic for user authentication and account management.</p>
 * <p>This class includes methods for login, password reset, user registration, and invite-based registration.</p>
 * <p>It interacts with the {@link DatabaseHelper} class to execute database operations.</p>
 */
public class LoginManager {

    /** The DatabaseHelper object to handle database operations. */
    private DatabaseHelper dbHelper;

    /**
     * Constructor for LoginManager.
     *
     * @param dbHelper The DatabaseHelper instance used for database operations.
     */
    public LoginManager(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Handles user login, checking for one-time password (OTP) validation and standard login flow.
     *
     * @param username The username of the user attempting to log in.
     * @param password The password or OTP provided by the user.
     * @param role The role the user is attempting to log in with.
     * @param currentDateTime The current date and time for OTP validation.
     * @return A message indicating the login result: success, failure, or specific errors.
     */
    public String login(String username, String password, String role, String currentDateTime) {
        try {
            System.out.println("Login attempt for username: " + username + " with role: " + role);

            // Check if the user exists
            if (!dbHelper.doesUserExist(username)) {
                return "User does not exist";
            }

            // Fetch stored hash for password validation
            byte[] storedHash = dbHelper.getPasswordHash(username);
            if (storedHash == null) {
                return "User does not exist or no password found";
            }

            // Check if account setup is incomplete
            if (!dbHelper.isAccountSetupComplete(username)) {
                return "Account setup incomplete";
            }

            // Check if the one-time password (OTP) is set
            boolean isOtpSet = dbHelper.isOneTimePasswordSet(username);
            if (isOtpSet) {
                System.out.println("One-time password is set for user: " + username);

                // Validate OTP expiration
                if (dbHelper.isOneTimePasswordValid(username, currentDateTime)) {
                    System.out.println("One-time password is valid for user: " + username);

                    // Verify the password (OTP)
                    if (!PasswordManager.verifyPassword(password, storedHash)) {
                        System.out.println("Incorrect one-time password for user: " + username);
                        return "Incorrect one-time password";
                    }

                    // OTP login successful, allow password reset
                    System.out.println("OTP login successful, redirecting user to reset password page.");
                    return "OTP login successful, please reset your password.";
                } else {
                    // If the OTP has expired, deny login
                    System.out.println("One-time password expired for user: " + username);
                    return "One-time password has expired, please contact the admin.";
                }
            }

            // Standard password verification if OTP is not set
            if (!PasswordManager.verifyPassword(password, storedHash)) {
                System.out.println("Incorrect password for user: " + username);
                return "Incorrect password";
            }

            // Fetch roles and check if the role is assigned to the user
            String[] roles = dbHelper.getRolesForUser(username);
            if (!Arrays.asList(roles).contains(role)) {
                return "You do not have the role '" + role + "' assigned.";
            }

            System.out.println("Login successful for user: " + username);
            return "Login successful";

        } catch (Exception e) {
            e.printStackTrace();
            return "Database error";
        }
    }

    /**
     * Resets the user's password after successful OTP validation.
     *
     * @param username The username of the user resetting the password.
     * @param newPassword The new password provided by the user.
     * @return A message indicating the result of the password reset.
     */
    public String resetPassword(String username, String newPassword) {
        try {
            System.out.println("Resetting password for username: " + username);
            byte[] hashedPassword = PasswordManager.hashPassword(newPassword); // Hash the new password
            dbHelper.updatePasswordAfterOtp(username, hashedPassword); // Update the password and clear the OTP flag
            System.out.println("Password reset successful for user: " + username);
            return "Password reset successful, please log in again.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Database error";
        }
    }

    /**
     * Registers a new user by creating an account with a hashed password and role.
     *
     * @param username The username for the new user.
     * @param password The plaintext password for the new user.
     * @param role The role to be assigned to the new user.
     * @return A message indicating the result of the registration.
     */
    public String register(String username, String password, String role) {
        try {
            if (dbHelper.doesUserExist(username)) {
                System.out.println("User already exists for username: " + username);
                return "User already exists";
            }
            byte[] hashedPassword = PasswordManager.hashPassword(password);  // Hash password to byte[]
            System.out.println("Hashed password for user " + username + ": " + Arrays.toString(hashedPassword));
            dbHelper.register(username, hashedPassword, role);
            return "Registration successful";
        } catch (Exception e) {
            e.printStackTrace();
            return "Database error";
        }
    }

    /**
     * Registers a user based on an invite code, updating the user details accordingly.
     *
     * @param inviteCode The invite code used for registration.
     * @param username The username for the new user.
     * @param password The plaintext password for the new user.
     * @return A message indicating the result of the invite-based registration.
     */
    public String registerInvite(String inviteCode, String username, String password) {
        try {
            if (!dbHelper.doesInviteExist(inviteCode)) {
                return "Invalid invite code";
            }

            byte[] hashedPassword = PasswordManager.hashPassword(password);  // Hash password to byte[]
            String role = dbHelper.getRoleFromInvite(inviteCode);
            dbHelper.updateUserFromInviteCode(inviteCode, username, hashedPassword, role);
            return "Registration successful";
        } catch (Exception e) {
            e.printStackTrace();
            return "Database error";
        }
    }

    /**
     * Resets the user's password using a one-time password (OTP).
     *
     * @param username The username of the user resetting their password.
     * @param oneTimePassword The one-time password provided by the user.
     * @param expirationDateTime The expiration date and time for the OTP.
     * @return A message indicating the result of the OTP setup.
     * @throws NoSuchAlgorithmException If there is an issue hashing the password.
     */
    public String resetUserWithOtp(String username, String oneTimePassword, String expirationDateTime) throws NoSuchAlgorithmException {
        try {
            byte[] otpHash = PasswordManager.hashPassword(oneTimePassword); // Hash the OTP
            dbHelper.setOneTimePassword(username, otpHash, expirationDateTime); // Set OTP and expiration
            System.out.println("One-time password set for user: " + username); // Debugging statement
            return "One-time password set successfully";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }
}
