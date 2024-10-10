package main;

/*******
 * <h1>Email Validator</h1>
 * <p> This class is for checking if an email address is valid.
 * <p> It runs some basic checks like making sure the email has an '@' symbol, a domain (something after the '@'), and that everything's in the right place.
 * <p> You can use this class anywhere in the app where you need to confirm someone entered a legit email.
 */
public class EmailValidator {

    /* Constructor - not really doing anything right now */
    public EmailValidator() {
    }

    /*
     * <p> Method to check if the email address is valid.
     * <p> Here's what we check for:
     * <p> The email is not null and not empty
     * <p> It has exactly one '@' symbol
     * <p> There's a '.' (dot) somewhere after the '@'
     * <p> The '@' and '.' aren't at the start or the end of the email
     *
     *
     * @param email    The email address to check
     *
     * @return boolean Returns true if the email looks valid, false otherwise
     */
    public static boolean validateEmail(String email) {

        // First, make sure the email isn't null
        if (email == null) {
            return false;
        }

        // Check if the email isn't empty
        if (email.length() == 0) {
            return false;
        }

        // Does the email have an '@' symbol?
        if (!email.contains("@")) {
            return false;
        }

        // Is there a '.' (dot) somewhere after the '@'?
        if (!email.contains(".")) {
            return false;
        }

        // Make sure the '@' comes before the last '.' (dot)
        if (email.indexOf('@') > email.lastIndexOf('.')) {
            return false;
        }

        // Make sure the '@' isn't the very first character
        if (email.indexOf('@') == 0) {
            return false;
        }

        // The '.' (dot) can't be the very first character either
        if (email.indexOf('.') == 0) {
            return false;
        }

        // The '@' can't be the last character of the email
        if (email.indexOf('@') == email.length() - 1) {
            return false;
        }

        // The '.' (dot) can't be the last character either
        if (email.indexOf('.') == email.length() - 1) {
            return false;
        }

        // If everything checks out, the email is valid!
        return true;
    }
}

