import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class TaskServlet extends HttpServlet {
    private static final String URL = "jdbc:postgresql://localhost:5432/task_manager";
    private static final String USER = "postgres"; // Replace with your PostgreSQL username
    private static final String PASSWORD ="123"; // Replace with your PostgreSQL password

    // doGet method to fetch tasks in JSON format
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONArray tasks = new JSONArray();
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "SELECT * FROM tasks";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                JSONObject task = new JSONObject();
                task.put("id", rs.getInt("id"));
                task.put("title", rs.getString("title"));
                task.put("description", rs.getString("description"));
                task.put("is_completed", rs.getBoolean("is_completed"));
                tasks.put(task);
            }
            out.print(tasks.toString());
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            out.print("{\"error\":\"Database error\"}");
        }
    }

    // doPost method to add or delete tasks based on the JSON input
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        StringBuilder jb = new StringBuilder();
        String line;

        // Read the JSON body from the request
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) jb.append(line);
        }
        
        // Parse the incoming JSON request
        JSONObject json = new JSONObject(jb.toString());
        String action = json.getString("action");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            if ("add".equals(action)) {
                String title = json.getString("title");
                String description = json.optString("description", "");
                String sql = "INSERT INTO tasks (title, description) VALUES (?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, title);
                stmt.setString(2, description);
                stmt.executeUpdate();
                out.print("{\"success\":true}");
            } else if ("delete".equals(action)) {
                int id = json.getInt("id");
                String sql = "DELETE FROM tasks WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, id);
                stmt.executeUpdate();
                out.print("{\"success\":true}");
            } else {
                out.print("{\"error\":\"Invalid action\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"success\":false,\"error\":\"Database error\"}");
        }
    }
}
