package com.crio.xpoll.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.crio.xpoll.model.Choice;
import com.crio.xpoll.model.Poll;
import com.crio.xpoll.model.PollSummary;
import com.crio.xpoll.util.DatabaseConnection;

/**
 * Data Access Object (DAO) for managing polls in the XPoll application.
 * Provides methods for creating, retrieving, closing polls, and fetching poll summaries.
 */
public class PollDAO {

    private final DatabaseConnection databaseConnection;

    /**
     * Constructs a PollDAO with the specified DatabaseConnection.
     *
     * @param databaseConnection The DatabaseConnection to be used for database operations.
     */
    public PollDAO(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    /**
     * Creates a new poll with the specified question and choices.
     *
     * @param userId   The ID of the user creating the poll.
     * @param question The question for the poll.
     * @param choices  A list of choices for the poll.
     * @return The created Poll object with its associated choices.
     * @throws SQLException If a database error occurs during poll creation.
     */
    public Poll createPoll(int userId, String question, List<String> choices) throws SQLException {
        Connection con = databaseConnection.getConnection();
        Poll poll=null;
        //int pollId=null;
        String pollsql = "insert into polls(user_id, question, is_closed,  created_at) values (?,?,?, CURRENT_TIMESTAMP) ";
        String choicesql = "insert into choices (poll_id, choice_text) values (?, ?) ";
        try( PreparedStatement pollstmt = con.prepareStatement(pollsql, Statement.RETURN_GENERATED_KEYS)){
            pollstmt.setInt(1, userId);
            pollstmt.setString(2, question);
            pollstmt.setBoolean(3, false);
            //pollstmt.setBoolean(4, true);
            pollstmt.executeUpdate();
            ResultSet rs = pollstmt.getGeneratedKeys();
            if (rs.next()) {
                int pollId = rs.getInt(1);
                List<Choice> choiceslist = new ArrayList<>();

                try (PreparedStatement choiceStmt = con.prepareStatement(choicesql,Statement.RETURN_GENERATED_KEYS )) {
                    for (String choice : choices) {
                        choiceStmt.setInt(1, pollId);
                        choiceStmt.setString(2, choice);
                        choiceStmt.executeUpdate();

                        ResultSet choiceRs = choiceStmt.getGeneratedKeys();
                        int choiceId = 0;
                        if (choiceRs.next()) {
                            choiceId = choiceRs.getInt(1);
                        }

                        choiceslist.add(new Choice(choiceId, pollId, choice));
                    }
                }
                poll = new Poll(pollId, userId, question, choiceslist, false);


                
            }
            
        }
        return poll;
    }
        
    

    /**
     * Retrieves a poll by its ID.
     *
     * @param pollId The ID of the poll to retrieve.
     * @return The Poll object with its associated choices.
     * @throws SQLException If a database error occurs or the poll is not found.
     */
    public Poll getPoll(int pollId) throws SQLException {
        Connection con = databaseConnection.getConnection();
        Poll poll=null;
        String pollsql = "select id, user_id, question, is_closed, created_at from polls where id =?";
        String choicesql = "select id, poll_id, choice_text from choices where poll_id=?";
        try (PreparedStatement pollStmt = con.prepareStatement(pollsql)) {
            pollStmt.setInt(1, pollId);
            ResultSet pollrs = pollStmt.executeQuery();
            if (pollrs.next()) {
                int id = pollrs.getInt("id");
                int userId = pollrs.getInt("user_id");
                String question = pollrs.getString("question");
                boolean isClosed = pollrs.getBoolean("is_closed");

                List<Choice> choiceList = new ArrayList<>();

                try (PreparedStatement choiceStmt = con.prepareStatement(choicesql)) {
                    choiceStmt.setInt(1, pollId);
                    ResultSet choiceRs = choiceStmt.executeQuery();

                    while (choiceRs.next()) {
                        int choiceId = choiceRs.getInt("id");
                        int pollIdFk = choiceRs.getInt("poll_id");
                        String choiceText = choiceRs.getString("choice_text");

                        choiceList.add(new Choice(choiceId, pollIdFk, choiceText));
                    }
                }
                poll = new Poll(id, userId, question, choiceList, isClosed);

            }
        }

    
    
        
        
        return poll;
    }

    /**
     * Closes a poll by updating its status in the database.
     *
     * @param pollId The ID of the poll to close.
     * @throws SQLException If a database error occurs during the update.
     */
    public void closePoll(int pollId) throws SQLException {
        Connection con = databaseConnection.getConnection();

        String sql = "UPDATE polls SET is_closed=true WHERE id = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, pollId);
            stmt.executeUpdate();
        }
        return;
        
    }

    /**
     * Retrieves a list of poll summaries for the specified poll.
     *
     * @param pollId The ID of the poll for which to retrieve summaries.
     * @return A list of PollSummary objects containing the poll question, choice text, and response count.
     * @throws SQLException If a database error occurs during the query.
     */
    public List<PollSummary> getPollSummaries(int pollId) throws SQLException {
        Connection con = databaseConnection.getConnection();
        List<PollSummary> summaries = new ArrayList<>();

        String pollsql = "SELECT question FROM polls WHERE id = ?";

        String choicesql = "SELECT c.choice_text, COUNT(r.user_id) AS response_count " +
                       "FROM choices c LEFT JOIN responses r ON c.id = r.choice_id AND c.poll_id = r.poll_id " +
                       "WHERE c.poll_id = ? GROUP BY c.choice_text";

        String question = null;
        try (PreparedStatement pollStmt = con.prepareStatement(pollsql)) {
            pollStmt.setInt(1, pollId);
            ResultSet pollRs = pollStmt.executeQuery();
            if (pollRs.next()) {
                question = pollRs.getString("question");
            }
        }
        try (PreparedStatement choiceStmt = con.prepareStatement(choicesql)) {
            choiceStmt.setInt(1, pollId);
            ResultSet choiceRs = choiceStmt.executeQuery();
    
            while (choiceRs.next()) {
                String choiceText = choiceRs.getString("choice_text");
                int responseCount = choiceRs.getInt("response_count");
    
                summaries.add(new PollSummary(question, choiceText, responseCount));
            }
        }
    


        return summaries;
    }
}