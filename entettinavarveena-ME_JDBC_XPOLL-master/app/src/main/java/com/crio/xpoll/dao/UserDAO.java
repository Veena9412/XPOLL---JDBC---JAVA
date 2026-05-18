package com.crio.xpoll.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.crio.xpoll.model.User;
import com.crio.xpoll.util.DatabaseConnection;

/**
 * Data Access Object (DAO) for managing users in the XPoll application.
 * Provides methods for creating and retrieving user information.
 */
public class UserDAO {

    private final DatabaseConnection databaseConnection;

    /**
     * Constructs a UserDAO with the specified DatabaseConnection.
     *
     * @param databaseConnection The DatabaseConnection to be used for database operations.
     */
    public UserDAO(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    /**
     * Creates a new user with the specified username and password.
     *
     * @param username The username for the new user.
     * @param password The password for the new user.
     * @return A User object representing the created user.
     * @throws SQLException If a database error occurs during user creation.
     */
    public User createUser(String username, String password) throws SQLException {
        Connection con = databaseConnection.getConnection();
        User user=null;
        String sql = "INSERT INTO users (username, password) VALUES (?,?)";
        try(PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if(rs.next()){
                int userId = rs.getInt(1);
                user = new User(userId, username, password);
            }
        }

           
        
        return user;
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user to retrieve.
     * @return A User object representing the user with the specified ID, or null if no user is found.
     * @throws SQLException If a database error occurs during the retrieval.
     */
    public User getUserById(int userId) throws SQLException {
        Connection con = databaseConnection.getConnection();
        User user=null;
        String sql = "select * from users where id =?";
        try(PreparedStatement stmt = con.prepareStatement(sql)){
            stmt.setInt(1, userId);
            ResultSet rs=stmt.executeQuery();
            if(rs.next()){
                user = new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
            }
        }
        
        return user;
    }
}