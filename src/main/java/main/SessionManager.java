package main;

/**
 * <p>SessionManager class handles the management of the current user session, including storing and retrieving
 * the user's username, roles, and other session-related functions.</p>
 * <p>This class is used to manage session information for users including roles (e.g., admin, student, instructor) and username.</p>
 */
public class SessionManager {

    /** The username of the current user in the session. */
    private String username;

    /** The roles of the current user (admin, student, instructor). */
    private String[] roles;

    /** The current role of the user. */
    private String role;

    /**
     * Default constructor initializes the session with no user or roles.
     */
    public SessionManager() {
        this.username = null;
        this.roles = null;
    }

    /**
     * Retrieves the current user's username.
     *
     * @return The username of the current user, or null if not set.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the current user's username.
     *
     * @param username The username of the current user.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retrieves the roles assigned to the current user.
     *
     * @return An array of roles assigned to the current user, or null if not set.
     */
    public String[] getRoles() {
        return roles;
    }

    /**
     * Sets the roles for the current user.
     *
     * @param roles An array of roles to be assigned to the current user.
     */
    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    /**
     * Sets the current role for the user.
     *
     * @param role The role to assign to the current user.
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Logs out the current user by clearing the username, role, and roles.
     */
    public void logout() {
        this.username = null;
        this.role = null;
        this.roles = null;
    }
}
