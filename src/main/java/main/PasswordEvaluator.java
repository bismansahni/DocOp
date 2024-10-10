package main;

/*******
 * <p>Password Evaluator</p>
 * <p> This class is used to check if a password meets some basic security requirements.
 * <p> It checks for things like length, uppercase and lowercase letters, numbers, and special characters.
 * <p> This class is to help make sure passwords are validated properly.
 */
public class PasswordEvaluator {

    /*
     * <p> Method to evaluate the strength of a password.
     * <p> Conditions:
     * <p> At least 8 characters long
     * <p> Has at least one uppercase letter
     * <p> Has at least one lowercase letter
     * <p> Contains at least one number
     * <p> Has at least one special character (!, @, #, $, %, ^, &, *)
     *
     * @param input    The password entered by the user
     *
     * @return String  Returns an empty string if the password is valid, or a message explaining what's wrong
     */

    /**
     * Default constructor
     */
    public PasswordEvaluator() {}

    /*
     * Method to evaluate the strength of a password.
     *
     * @param input The password entered by the user
     *
     * @return String Returns an empty string if the password is valid, or a message explaining what's wrong
     */
    public static String evaluatePassword(String input) {

        // Check if the password is at least 8 characters long
        if (input.length() < 8)
            return "Password must be at least 8 characters long.";

        // Check if the password contains at least one uppercase letter
        if (!input.matches(".*[A-Z].*"))
            return "Password must contain at least one uppercase letter.";

        // Check if the password contains at least one lowercase letter
        if (!input.matches(".*[a-z].*"))
            return "Password must contain at least one lowercase letter.";

        // Check if the password has at least one number
        if (!input.matches(".*\\d.*"))
            return "Password must contain at least one numeric digit.";

        // Check if the password contains at least one special character
        if (!input.matches(".*[!@#\\$%\\^&\\*].*"))
            return "Password must contain at least one special character.";

        // If all checks pass, return an empty string (which means the password is good)
        return "";
    }
}

