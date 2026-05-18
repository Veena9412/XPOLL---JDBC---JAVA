package com.crio.xpoll.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.crio.xpoll.model.Response;
import com.crio.xpoll.util.DatabaseConnection;
import com.mysql.cj.protocol.Resultset;

/**
 * Data Access Object (DAO) for managing responses in the XPoll application.
 * Provides methods for creating responses to polls.
 */
public class ResponseDAO {
    private final DatabaseConnection databaseConnection;

    /**
     * Constructs a ResponseDAO with the specified DatabaseConnection.
     *
     * @param databaseConnection The DatabaseConnection to be used for database operations.
     */
    public ResponseDAO(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    /**
     * Creates a new response for a specified poll, choice, and user.
     *
     * @param pollId   The ID of the poll to which the response is made.
     * @param choiceId The ID of the choice selected by the user.
     * @param userId   The ID of the user making the response.
     * @return A Response object representing the created response.
     * @throws SQLException If a database error occurs during response creation.
     */
    public Response createResponse(int pollId, int choiceId, int userId) throws SQLException {
        Connection con = databaseConnection.getConnection();
        Response response =null;
        String sql = "INSERT INTO responses(poll_id, choice_id, user_id, created_at) VALUES (?,?,?, CURRENT_TIMESTAMP)";
        try(PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            stmt.setInt(1,pollId);
            stmt.setInt(2,choiceId);
            stmt.setInt(3,userId);
            stmt.executeUpdate();
            
            
        }
        response = new Response(pollId, choiceId, userId);
        return response;
    }
}