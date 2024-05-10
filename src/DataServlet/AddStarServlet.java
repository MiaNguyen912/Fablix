package DataServlet;

import Utility.User;
import com.google.gson.*;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@WebServlet(name = "DataServlet.AddStarServlet", urlPatterns = "/_dashboard/loggedin/api/add-star")
public class AddStarServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     * Adds a star into the database, taking in a star name and an optional birth year
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        String query = "";


        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Query should be inserting into stars with name and birth year
            // Doesn't matter if there is duplicate, treat as different with different ids
            String name = request.getParameter("name");
            String birth_year = request.getParameter("birth_year");

            // table for reference
//            CREATE TABLE IF NOT EXISTS stars (
//                   id VARCHAR(10) PRIMARY KEY,
//                   name VARCHAR(100) NOT NULL DEFAULT '',
//                   birthYear INTEGER
//            );


            // make a sql insert query for the prepare statement to database to insert
            String insert_query = "INSERT INTO stars (name, birthYear) VALUES (?, ?)";
            PreparedStatement statement = conn.prepareStatement(insert_query, PreparedStatement.RETURN_GENERATED_KEYS);



            statement.setString(1, name);

            if (birth_year != null && !birth_year.isEmpty()) {
                statement.setInt(2, Integer.parseInt(birth_year));
            } else {
                statement.setNull(2, Types.INTEGER);
            }

            int rowsAffected = statement.executeUpdate();

            JsonObject responseJson = new JsonObject();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        responseJson.addProperty("new_star_id", generatedKeys.getString(1));
                    }
                }
            }

            responseJson.addProperty("status", "success");

            out.println(responseJson.toString());

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            jsonObject.addProperty("query", query);

            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
