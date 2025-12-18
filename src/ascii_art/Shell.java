package ascii_art;

import utils.MathUtils;
import ascii_output.AsciiOutput;
import ascii_output.ConsoleAsciiOutput;
import ascii_output.HtmlAsciiOutput;
import exceptions.CustomShellException;
import image.Image;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * The Shell class is responsible for the user interface of the ASCII Art algorithm.
 * It is a command line interface that allows the user to control the algorithm's parameters.
 * <p>Valid commands:</p>
 * <ul>
 *      <li>exit - Exit the shell.</li>
 *      <li>chars - View the current character set.</li>
 *      <li>add - Add characters to the current character set.</li>
 *      <li>remove - Remove characters to the current character set.</li>
 *      <li>res - Control the picture's resolution.</li>
 *      <li>round - Change rounding method when matching an ASCII character.</li>
 *      <li>output - Choose output format: .html file or console.</li>
 *      <li>asciiArt - Run the algorithm with the current parameters.</li>
 * </ul>
 */
public class Shell {

    // Welcome message constants
    private static final String SHELL_WELCOME_MSG_PATH = "src/utils/Welcome";
    private static final String READ_ERROR_MSG = "Error reading welcome message: %s.\n" +
            "Please open an issue on GitHub to report this error.";

    // Constants for user input
    private static final String NO_IMAGE_ERROR = "No image path provided. " +
            "Please provide a valid image path as an argument.";
    private static final String WAIT_FOR_USER_INPUT = ">>> ";
    private static final String EXIT_INPUT = "exit";
    private static final String PRINT_CHARS_LIST_INPUT = "chars";
    private static final String ADD_CHARS_TO_LIST = "add";
    private static final String REMOVE_CHARS_FROM_LIST = "remove";
    private static final String CHANGE_RESOLUTION = "res";
    private static final String ROUND_METHOD = "round";
    private static final String OUTPUT_FORMAT = "output";
    private static final String RUN_ALGORITHM = "asciiArt";
    private static final String INSUFFICIENT_CHARACTER_SET_SIZE = "Did not execute. Charset is too small." +
            " Minimum size is 2 characters.";

    // Default values constants
    private static final String HTML_OUTPUT_FONT = "Courier New";
    private static final int DEFAULT_RESOLUTION_VALUE = 2;
    private static final Character[] DEFAULT_CHARACTER_SET = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };
    private static final int TWO_ARGUMENTS = 2;

    // ASCII values
    private static final char SPACE_ASCII_CODE = 32;
    private static final char FIRST_ASCII_CHARACTER = 32;
    private static final char LAST_ASCII_CHARACTER = 126;

    // "add"/"remove" shell command constants
    private static final String SPACE_ADDITION_REQUEST = "space";
    private static final String ADD_ALL_ASCII_REQUEST = "all";
    private static final String REMOVE_ALL_ASCII_REQUEST = ADD_ALL_ASCII_REQUEST;
    private static final int VALID_RANGE_STRING_LENGTH = 3;
    private static final int ADD_REMOVE_RANGE_LENGTH = 3;
    private static final int FROM_CHAR_INDEX = 0;
    private static final int TO_CHAR_INDEX = 2;

    // "res" shell command constants
    private static final String REQUESTED_RESOLUTION_CHANGE = "change resolution";
    private static final String OUT_OF_BOUNDS = "exceeding boundaries";
    private static final int RESOLUTION_CHANGE_FACTOR = 2;
    private static final String INCREASE_RES_REQUEST = "up";
    private static final String DECREASE_RES_REQUEST = "down";
    private static final String RESOLUTION_SET_MESSAGE = "Resolution set to %d.";

    // "output" shell command constants
    private static final String CHANGE_OUTPUT_METHOD = "change output method";

    // "round" shell command constants
    static final String ROUND_UP_FORMAT = "up";
    static final String ROUND_DOWN_FORMAT = "down";
    static final String ROUND_ABS_VALUE_FORMAT = "abs";
    private static final String ROUND_ERROR = "change rounding method";

    // incorrect format or command requests
    private static final String INCORRECT_FORMAT = "incorrect format";
    private static final String EXECUTE_REQUEST = "execute";
    private static final String INCORRECT_COMMAND = "incorrect command";
    private static final int SUFFICIENT_CHAR_SET_SIZE = 2;

    // Enum constants
    private static final String CONSOLE_FORMAT = "console";
    private static final String HTML_FORMAT = "html";

    // Separators
    private static final String HYPHEN_SEPARATOR = "-";


    // private fields
    private final HashSet<Character> characterSet;
    private int resolution;
    private AsciiOutput userOutput;
    private RoundMethod roundMethod;
    private int minCharsInRow;
    private int imageWidth;
    private String imageName;

    /**
     * An enum to represent the output method of the algorithm.
     */
    private enum OutputMethod {

        CONSOLE(CONSOLE_FORMAT),
        HTML(HTML_FORMAT);

        private final String value;

        /**
         * Constructor
         * @param value The value of the enum.
         */
        OutputMethod(String value) {
            this.value = value;
        }

        /**
         * Getter method to retrieve the value.
         * @return The value of the enum.
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * <p>Constructor for the Shell class.</p>
     * <p>Initializes the character set with the default values.</p>
     * <p>Initializes the resolution with the default value.</p>
     * <p>Initializes the rounding method with the default value.</p>
     * <p>Initializes the output method with the default value.</p>
     */
    private Shell() {
        // set up default values for the algorithm
        this.characterSet = new HashSet<>(Arrays.asList(DEFAULT_CHARACTER_SET));
        this.resolution = DEFAULT_RESOLUTION_VALUE;
        this.roundMethod = RoundMethod.ABSOLUTE;
        this.userOutput = new ConsoleAsciiOutput();
    }

    /**
     * Prints the current character list to the standard output in increasing ASCII values,
     * separated by commas and spaces.
     */
    private void printCharList() {
        // turn the character set to a tree set to efficiently sort it
        TreeSet<Character> sortedCharacterSet = new TreeSet<>(this.characterSet);
        System.out.println(sortedCharacterSet); // print the characters with ", " separator
    }

    /**
     * Adds characters to the character list.
     * Valid argument options:
     * <ul>
     *      <li>single character - A single character to add to the list.</li>
     *      <li>all - Adds all ASCII characters from 32 to 126 to the list.</li>
     *      <li>space - Adds the character space ' ' to the list.</li>
     *      <li>character range - Adds characters from start to finish of the given range.
     *          <ul>
     *              <li>Examples: a-g or g-a</li>
     *          </ul>
     *      </li>
     * </ul>
     *
     * @param args Argument array (first argument is the command, second is the character to add).
     * @throws CustomShellException In case of invalid input
     *
     * @see Shell#removeCharsFromList(String[])
     */
    private void addCharsToList(String[] args) throws CustomShellException {
        // Create a new exception to throw in case of an invalid input.
        CustomShellException characterSetException = new CustomShellException(
                ADD_CHARS_TO_LIST, INCORRECT_FORMAT
        );
        if (args.length >= TWO_ARGUMENTS) {
            String operation = args[1];
            if (operation.equals(SPACE_ADDITION_REQUEST)) { // Add space to the character set
                this.characterSet.add(SPACE_ASCII_CODE);
            } else if (operation.equals(ADD_ALL_ASCII_REQUEST)) { // Add all ASCII characters to the set
                operateOnAsciiCharactersInRange(FIRST_ASCII_CHARACTER, LAST_ASCII_CHARACTER,
                        ADD_CHARS_TO_LIST);
            } else if (operation.length() == 1) { // Add a single character to the set
                char characterValue = operation.toCharArray()[0];
                // Add the character iff it is in the ASCII table range.
                if (isInAsciiTable(characterValue)) {
                    this.characterSet.add(characterValue);
                } else { // Character is not in the ASCII table, throw exception.
                    throw characterSetException;
                }
            } else if (operation.contains(HYPHEN_SEPARATOR) && args[1].length() == ADD_REMOVE_RANGE_LENGTH) {
                // Given range of characters to add to the set.
                commandCharactersInRange(operation.split(""), ADD_CHARS_TO_LIST);
            } else {
                throw characterSetException;
            }
        } else { // User did not specify what to add to the set
            throw characterSetException;
        }
    }

    /**
     * Removes characters to the character list.
     * Valid argument options:
     * <ul>
     *      <li>single character - A single character to remove from the list.</li>
     *      <li>all - Removes all ASCII characters from 32 to 126 from the list.</li>
     *      <li>space - Removes the character space ' ' from the list.</li>
     *      <li>character range - Removes characters from start to finish of the given range.
     *      <p>Examples: a-g or g-a</p></li>
     * </ul>
     *
     * @param args Argument array (first argument is the command, second is the character to add).
     * @throws CustomShellException In case of invalid input
     *
     * @see Shell#addCharsToList(String[])
     */
    private void removeCharsFromList(String[] args) throws CustomShellException {
        // Create a new exception to throw in case of an invalid input.
        CustomShellException characterSetException = new CustomShellException(
                REMOVE_CHARS_FROM_LIST, INCORRECT_FORMAT
        );
        if (args.length >= TWO_ARGUMENTS) {
            String operation = args[1];
            if (operation.equals(SPACE_ADDITION_REQUEST)) { // Remove space from the character set
                this.characterSet.remove(SPACE_ASCII_CODE);
            } else if (operation.equals(REMOVE_ALL_ASCII_REQUEST)) {
                // Remove all ASCII characters from the set
                operateOnAsciiCharactersInRange(
                        FIRST_ASCII_CHARACTER, LAST_ASCII_CHARACTER, REMOVE_CHARS_FROM_LIST
                );
            } else if (operation.length() == 1) { // Remove a single character from the set
                this.characterSet.remove(operation.toCharArray()[0]);
            } else if (operation.contains(HYPHEN_SEPARATOR) && args[1].length() == ADD_REMOVE_RANGE_LENGTH) {
                // Given range of characters to add to the set.
                commandCharactersInRange(operation.split(""), REMOVE_CHARS_FROM_LIST);
            } else {
                throw characterSetException;
            }
        } else { // User did not specify what to remove from the set
            throw characterSetException;
        }
    }

    /**
     * Implements the "add" or "remove" command on a range of characters.
     * @param stringArray The array of strings to operate on.
     * @param command The command to operate on the set.
     * @throws CustomShellException In case of invalid input.
     */
    private void commandCharactersInRange(String[] stringArray, String command) throws CustomShellException {
        CustomShellException characterSetException = new CustomShellException(command, INCORRECT_FORMAT);
        if (stringArray.length != VALID_RANGE_STRING_LENGTH) {
            throw characterSetException;
        } else {
            char param1 = stringArray[FROM_CHAR_INDEX].charAt(0);
            char param2 = stringArray[TO_CHAR_INDEX].charAt(0);
            if (!isInAsciiTable(param1) || !isInAsciiTable(param2)) {
                throw characterSetException;
            }
            char fromChar = (char) Math.min(param1, param2);
            char toChar = (char) Math.max(param1, param2);
            //
            operateOnAsciiCharactersInRange(fromChar, toChar, command);
        }
    }

    /**
     * Checks if a given character is in the range of the ASCII table.
     *
     * @param c The value of the character to check.
     * @return <code>true</code> if the character is in ASCII range, <code>false</code> otherwise.
     */
    private static boolean isInAsciiTable(char c) {
        return c >= FIRST_ASCII_CHARACTER && c <= LAST_ASCII_CHARACTER;
    }

    /**
     * Implements the given command on all ASCII characters in range (fromChar)-(toChar) to the
     * characterSet (inclusive).
     * @param fromChar <code>char</code> to command chars from.
     * @param toChar <code>char</code> to command chars up to.
     * @param command "add" or "remove" command to operate on the set. Assumes valid command.
     */
    private void operateOnAsciiCharactersInRange(char fromChar, char toChar, String command) {
        // Since all characters are treated as integers, iterate from space = 32, to '~' = 126
        for (char c = fromChar; c <= toChar; c++) {
            if (command.equals(ADD_CHARS_TO_LIST)) {
                this.characterSet.add(c); // Since this is a HashSet, will not add an existing character.
            } else {
                this.characterSet.remove(c); // Will not remove a character that is not in the set.
            }
        }
    }

    /**
     * Changes the resolution of the output picture.
     * <p>Has the following set of commands:</p>
     * <ul>
     *      <li>res up: Multiply the current resolution by 2.</li>
     *      <li>res down: Divide the current resolution by 2.</li>
     * </ul>
     * <pre>The default resolution is set to 2, cannot exceed certain boundaries.</pre>
     * @param args The arguments given by the user. The second argument is the operation to perform.
     * @throws CustomShellException The requested resolution change exceeds upper/lower bounds.
     */
    private void changeOutputResolution(String[] args) throws CustomShellException {
        if (args.length >= TWO_ARGUMENTS) {
            CustomShellException resolutionBoundException = new CustomShellException(
                    REQUESTED_RESOLUTION_CHANGE, OUT_OF_BOUNDS
            );
            String operation = args[1];
            if (operation.equals(INCREASE_RES_REQUEST)) {
                if (this.resolution * RESOLUTION_CHANGE_FACTOR <= this.imageWidth) {
                    this.resolution *= RESOLUTION_CHANGE_FACTOR;
                    System.out.printf((RESOLUTION_SET_MESSAGE) + "%n", this.resolution);
                } else {
                    throw resolutionBoundException;
                }
            } else if (operation.equals(DECREASE_RES_REQUEST)) {
                if (this.resolution / RESOLUTION_CHANGE_FACTOR >= this.minCharsInRow) {
                    this.resolution /= RESOLUTION_CHANGE_FACTOR;
                    System.out.printf((RESOLUTION_SET_MESSAGE) + "%n", this.resolution);
                } else {
                    throw resolutionBoundException;
                }
            } else {
                throw new CustomShellException(REQUESTED_RESOLUTION_CHANGE, INCORRECT_FORMAT);
            }
        } else { // User wants to print the current resolution.
            System.out.printf((RESOLUTION_SET_MESSAGE) + "%n", this.resolution);
        }
    }

    /**
     * Changes the ASCII-Art output format.
     * <p>Has the following commands:</p>
     * <ul>
     *      <li>console - Prints the ASCII-Art to the standard output.</li>
     *      <li>HTML - Creates an HTML file with the ASCII-Art.</li>
     * </ul>
     * @param args The arguments given by the user. The second argument is the output format.
     * @throws CustomShellException In case of invalid output format.
     */
    private void changeOutputFormat(String[] args) throws CustomShellException{
        CustomShellException formatException = new CustomShellException(CHANGE_OUTPUT_METHOD,
                INCORRECT_FORMAT);
        if (args.length >= TWO_ARGUMENTS) { // User added another request after command type, check it
            String outputFormat = args[1];
            if (outputFormat.equals(OutputMethod.CONSOLE.getValue())) {
                this.userOutput = new ConsoleAsciiOutput();
            } else if (outputFormat.equals(OutputMethod.HTML.getValue())) {
                this.userOutput = new HtmlAsciiOutput(this.imageName + "." + HTML_FORMAT, HTML_OUTPUT_FONT);
            } else {
                throw formatException;
            }
        } else {
            throw formatException;
        }
    }

    /**
     * Runs the ASCII Art algorithm on the given image.
     * @param imageName The path to the image to run the algorithm on.
     * @throws IOException In case of invalid image path.
     * @throws CustomShellException In case of invalid input.
     */
    private void runAsciiArtAlgorithm(String imageName) throws IOException, CustomShellException {
        if (this.characterSet.size() >= SUFFICIENT_CHAR_SET_SIZE){
            AsciiArtAlgorithm asciiArtAlgorithm = new AsciiArtAlgorithm(imageName, this.characterSet,
                    this.resolution, this.roundMethod);
            char[][] output = asciiArtAlgorithm.run(); // Run the algorithm.
            this.userOutput.out(output); //  Display output according to current format.
        } else {
            throw new CustomShellException(INSUFFICIENT_CHARACTER_SET_SIZE);
        }
    }

    /**
     * Changes the rounding method when matching an ASCII character.
     * <p>Has the following commands:</p>
     * <ul>
     *      <li>up - Round up to the nearest character.</li>
     *      <li>down - Round down to the nearest character.</li>
     *      <li>abs - Round to the nearest character by absolute value.</li>
     * </ul>
     * @param args The arguments given by the user. The second argument is the rounding method.
     * @throws CustomShellException In case of invalid rounding method.
     */
    private void changeRoundMethod(String[] args) throws CustomShellException {
        CustomShellException roundMethodException = new CustomShellException(ROUND_ERROR, INCORRECT_FORMAT);
        if (args.length >= TWO_ARGUMENTS) {
            switch (args[1]) {
                case ROUND_UP_FORMAT -> this.roundMethod = RoundMethod.UP;
                case ROUND_DOWN_FORMAT -> this.roundMethod = RoundMethod.DOWN;
                case ROUND_ABS_VALUE_FORMAT -> this.roundMethod = RoundMethod.ABSOLUTE;
                default -> throw roundMethodException;
            }
        } else {
            throw roundMethodException;
        }
    }

    /**
     * Handles the user's input and executes the requested commands.
     * <p>Valid commands:</p>
     * <ul>
     *      <li>chars - View the current character set.</li>
     *      <li>add - Add characters to the current character set.</li>
     *      <li>remove - Remove characters to the current character set.</li>
     *      <li>res - Control the picture's resolution.</li>
     *      <li>round - Change rounding method when matching an ASCII character.</li>
     *      <li>output - Choose output format: .html file or console.</li>
     *      <li>asciiArt - Run the algorithm with the current parameters.</li>
     * </ul>
     * @param args The arguments given by the user.
     * @param imageName The path to the image to run the algorithm on.
     * @throws CustomShellException In case of invalid input.
     * @throws IOException In case of invalid image path.
     */
    private void inputSwitcher(String[] args, String imageName) throws CustomShellException, IOException {
        switch (args[0]) { // Switch user's command
            case PRINT_CHARS_LIST_INPUT:
                printCharList();
                break;
            case ADD_CHARS_TO_LIST:
                addCharsToList(args);
                break;
            case REMOVE_CHARS_FROM_LIST:
                removeCharsFromList(args);
                break;
            case CHANGE_RESOLUTION:
                changeOutputResolution(args);
                break;
            case ROUND_METHOD:
                changeRoundMethod(args);
                break;
            case OUTPUT_FORMAT:
                changeOutputFormat(args);
                break;
            case RUN_ALGORITHM:
                runAsciiArtAlgorithm(imageName);
                break;
            case EXIT_INPUT:
                break;
            default:
                throw new CustomShellException(EXECUTE_REQUEST, INCORRECT_COMMAND);
        }
    }

    /**
     * Responsible for translating the commands given from the user and execute them.
     * <p>List of valid commands:</p>
     * <ul>
     *      <li>exit - Exit the shell.</li>
     *      <li>chars - View the current character set.</li>
     *      <li>add - Add characters to the current character set.</li>
     *      <li>remove - Remove characters to the current character set.</li>
     *      <li>res - Control the picture's resolution.</li>
     *      <li>round - Change rounding method when matching an ASCII character.</li>
     *      <li>output - Choose output format: .html file or console.</li>
     *      <li>asciiArt - Run the algorithm with the current parameters.</li>
     * </ul>
     *
     * @param imagePath Path to the image to activate the algorithm on.
     */
    private void run(String imagePath) {
        try {
            this.imageName = imagePath.substring(imagePath.lastIndexOf("\\") + 1).split("\\.")[0];
            Image image = new Image(imagePath);
            // Save image sizes after padding
            this.imageWidth = MathUtils.closestPowerOfTwo(image.getWidth());
            this.minCharsInRow = Math.max(
                    1, this.imageWidth / MathUtils.closestPowerOfTwo(image.getHeight())
            );
            String input = "";
            while (!input.equals(EXIT_INPUT)) {
                try {
                    System.out.print(WAIT_FOR_USER_INPUT);
                    input = KeyboardInput.readLine();

                    String[] args = input.split(" "); // parse the command from the user

                    inputSwitcher(args, imagePath); // handle the user's request according to the command
                } catch (CustomShellException e) { // Catch exceptions that occurred due to invalid commands
                    System.out.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            // Invalid image path, will end the run() and return to main to end the session.
            System.out.println(e.getMessage());
        }
    }

    /**
     * Prints the welcome message to the user.
     */
    private static void printWelcomeMessage() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SHELL_WELCOME_MSG_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println(String.format(READ_ERROR_MSG, e.getMessage()));
        }
    }

    /**
     * Main method to run the shell.
     * @param args Command line arguments.
     *
     * @see Shell#run(String)
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println(NO_IMAGE_ERROR);
            return; // Exit if no arguments are provided
        }
        printWelcomeMessage();
        Shell newShellSession = new Shell();
        newShellSession.run(args[0]);
    }

}
