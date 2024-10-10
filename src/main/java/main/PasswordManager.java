package main;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * <p>PasswordManager class handles the hashing and verification of passwords using the SHA-256 algorithm.</p>
 * <p>This class provides methods to hash a password and verify a given password against a stored hash.</p>
 */
public class PasswordManager {

    /**
     * Default constructor
     */
    public PasswordManager() {}

    /**
     * Hashes a given password using the SHA-256 algorithm.
     *
     * @param password The plaintext password to be hashed.
     * @return A byte array representing the hashed password.
     * @throws NoSuchAlgorithmException If the SHA-256 algorithm is not available in the environment.
     */
    public static byte[] hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedPassword = md.digest(password.getBytes());
        System.out.println("Hashed password: " + Arrays.toString(hashedPassword));
        return hashedPassword;  // Return hashed password as byte array
    }

    /**
     * Verifies whether a given password matches the stored hashed password.
     *
     * @param enteredPassword The plaintext password entered by the user.
     * @param storedHash The stored hash from the database to compare against.
     * @return True if the entered password matches the stored hash, false otherwise.
     * @throws NoSuchAlgorithmException If the SHA-256 algorithm is not available in the environment.
     */
    public static boolean verifyPassword(String enteredPassword, byte[] storedHash) throws NoSuchAlgorithmException {
        byte[] hashedEnteredPassword = hashPassword(enteredPassword);  // Hash the entered password
        System.out.println("Entered password hash: " + Arrays.toString(hashedEnteredPassword));
        System.out.println("Stored hash to compare: " + Arrays.toString(storedHash));
        boolean isEqual = MessageDigest.isEqual(hashedEnteredPassword, storedHash);  // Compare the hashes
        System.out.println("Password match result: " + isEqual);
        return isEqual;
    }
}
