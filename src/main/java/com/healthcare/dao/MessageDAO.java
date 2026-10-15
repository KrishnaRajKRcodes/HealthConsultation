package com.healthcare.dao;

import com.healthcare.model.Message;
import com.healthcare.util.DAOException;
import com.healthcare.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    public void send(int senderId, int receiverId, String body) throws DAOException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("INSERT INTO messages(sender_id,receiver_id,body) VALUES(?,?,?)")) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, body);
            ps.executeUpdate();
        } catch (SQLException e) { throw new DAOException("Could not send message", e); }
    }

    public List<Message> findInvolving(int userId) throws DAOException {
        String sql = "SELECT m.id,m.sender_id,m.receiver_id,m.body,m.sent_at,s.name AS sname,r.name AS rname FROM messages m "
                + "JOIN users s ON m.sender_id=s.id JOIN users r ON m.receiver_id=r.id "
                + "WHERE m.sender_id=? OR m.receiver_id=? ORDER BY m.sent_at DESC, m.id DESC";
        List<Message> list = new ArrayList<>();
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message m = new Message();
                    m.setId(rs.getInt("id"));
                    m.setSenderId(rs.getInt("sender_id"));
                    m.setReceiverId(rs.getInt("receiver_id"));
                    m.setBody(rs.getString("body"));
                    m.setSenderName(rs.getString("sname"));
                    m.setReceiverName(rs.getString("rname"));
                    Timestamp ts = rs.getTimestamp("sent_at");
                    if (ts != null) m.setSentAt(ts.toLocalDateTime());
                    list.add(m);
                }
            }
        } catch (SQLException e) { throw new DAOException("Could not load messages", e); }
        return list;
    }
}
