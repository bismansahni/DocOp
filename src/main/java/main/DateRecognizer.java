package main;

/**
 * The DateRecognizer class is responsible for checking whether a given date is valid
 * according to the MM/DD/YYYY format using a finite state machine (FSM).
 */
public class DateRecognizer {

    /**
     * Default constructor
     */
    public DateRecognizer() {}

    // Attributes for error message and input tracking
    /** Error message generated when a date format error occurs. */
    public static String dateRecognizerErrorMessage = "";

    /** Input string to be checked for validity. */
    public static String dateRecognizerInput = "";

    /** The index of the character where an error occurred in the input. */
    public static int dateRecognizerIndexOfError = -1;

    /** Current state of the FSM. */
    private static int state = 0;

    /** Next state of the FSM. */
    private static int nextState = 0;

    /** Indicates whether the FSM has reached a valid final state. */
    private static boolean finalState = false;

    /** The input string being checked. */
    private static String inputLine = "";

    /** Current character being evaluated by the FSM. */
    private static char currentChar;

    /** Index of the current character in the input string. */
    private static int currentCharIndex;

    /** Boolean flag to indicate if the FSM is still running. */
    private static boolean running;

    /** Counter for the length of the date value being processed. */
    private static int dateValueSize = 0;

    /**
     * Displays the input string up to the character where the error occurred,
     * appending a '?' to show where the error is located.
     *
     * @param input The input string to be displayed.
     * @param currentCharIndex The index where the error occurred.
     * @return A string representation of the input showing where the error occurred.
     */
    private static String displayInput(String input, int currentCharIndex) {
        return input.substring(0, currentCharIndex) + "?\n";
    }

    /**
     * Displays debugging information including the current state, whether the FSM is
     * in a final state, and the current character being evaluated.
     */
    private static void displayDebuggingInfo() {
        if (currentCharIndex >= inputLine.length())
            System.out.println("State: " + state + " Final State: " + finalState + " Current Char: None");
        else
            System.out.println("State: " + state + " Final State: " + finalState + " Current Char: " + currentChar);
    }

    /**
     * Moves to the next character in the input string and updates the current character.
     * If the end of the string is reached, the FSM stops running.
     */
    private static void moveToNextCharacter() {
        currentCharIndex++;
        if (currentCharIndex < inputLine.length())
            currentChar = inputLine.charAt(currentCharIndex);
        else {
            currentChar = ' ';
            running = false;
        }
    }

    /**
     * The main method for validating if a given date string is in the correct MM/DD/YYYY format.
     * This method uses a finite state machine (FSM) to process the input.
     *
     * @param input The input date string to be checked.
     * @return An error message if the input date is invalid, or an empty string if the date is valid.
     */
    public static String checkForValidDate(String input) {
        if (input.length() <= 0) return "";

        // Initialize FSM variables
        state = 0;
        inputLine = input;
        currentCharIndex = 0;
        currentChar = input.charAt(0);
        dateRecognizerInput = input;
        running = true;
        nextState = -1;
        dateValueSize = 0;

        System.out.println("\nState transitions: ");

        // FSM loop
        while (running) {
            switch (state) {
                case 0:
                    dateValueSize = 0;

                    // State 0: Expect first digit of the month
                    if (currentChar == '0' || currentChar == '1') {
                        nextState = 1;
                        dateValueSize++;
                    } else if (currentChar >= '2' && currentChar <= '9') {
                        nextState = 4;
                        dateValueSize++;
                    } else {
                        running = false;
                    }
                    break;

                case 1:
                    // State 1: Expect second digit of the month (0-9)
                    if (currentChar >= '0' && currentChar <= '9') {
                        nextState = 2;
                        dateValueSize++;
                    } else {
                        running = false;
                    }
                    break;

                case 2:
                    // State 2: Expect '/' after the month
                    if (currentChar == '/') {
                        nextState = 3;
                        dateValueSize++;
                    } else {
                        running = false;
                    }
                    break;

                case 3:
                    // State 3: Expect first digit of the day (0-3)
                    if (currentChar >= '0' && currentChar <= '3') {
                        nextState = 4;
                        dateValueSize++;
                    } else {
                        running = false;
                    }
                    break;

                case 4:
                    // State 4: Expect second digit of the day (0-9)
                    if (currentChar >= '0' && currentChar <= '9') {
                        nextState = 5;
                        dateValueSize++;
                    } else {
                        running = false;
                    }
                    break;

                case 5:
                    // State 5: Expect '/' after the day
                    if (currentChar == '/') {
                        nextState = 6;
                        dateValueSize++;
                    } else {
                        running = false;
                    }
                    break;

                case 6:
                    // State 6: Expect first digit of the year (1 or 2)
                    if (currentChar == '1' || currentChar == '2') {
                        nextState = 7;
                        dateValueSize++;
                    } else {
                        running = false;
                    }
                    break;

                case 7:
                    // State 7: Expect second digit of the year (0-9)
                    if (currentChar >= '0' && currentChar <= '9') {
                        nextState = 8;
                        dateValueSize++;
                    } else {
                        running = false;
                    }
                    break;

                case 8:
                    // State 8: Expect third digit of the year (0-9)
                    if (currentChar >= '0' && currentChar <= '9') {
                        nextState = 9;
                        dateValueSize++;
                    } else {
                        running = false;
                    }
                    break;

                case 9:
                    // State 9: Expect fourth digit of the year (0-9)
                    if (currentChar >= '0' && currentChar <= '9') {
                        nextState = 10;
                        dateValueSize++;
                        finalState = true;
                        running = false;
                    } else {
                        running = false;
                    }
                    break;

                default:
                    running = false;
                    break;
            }

            // Move to the next character if FSM is still running
            if (running) {
                moveToNextCharacter();
                state = nextState;
            }

            // Check for a valid final state
            if (dateValueSize > 10) {
                dateRecognizerErrorMessage = "A valid date must be no longer than 10 characters.\n";
                return dateRecognizerErrorMessage + displayInput(input, 10);
            }
        }

        // If FSM stops at a valid final state
        if (finalState) {
            return "";  // No error, valid date
        } else {
            dateRecognizerIndexOfError = currentCharIndex;
            dateRecognizerErrorMessage = "Invalid date format.\n";
            return dateRecognizerErrorMessage + displayInput(input, currentCharIndex);
        }
    }
}
