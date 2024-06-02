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
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbReadWrite");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     * Adds a star into the database, taking in a star name and an optional birth year
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Starting doPost");
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        String query = "";

        System.out.println("Connecting to data");
        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            System.out.println("Getting params");
            // Query should be inserting into stars with name and birth year
            // Doesn't matter if there is duplicate, treat as different with different ids
            String name = request.getParameter("star_name");
            String birth_year = request.getParameter("birth_year");

            // table for reference
//            CREATE TABLE IF NOT EXISTS stars (
//                   id VARCHAR(10) PRIMARY KEY,
//                   name VARCHAR(100) NOT NULL DEFAULT '',
//                   birthYear INTEGER
//            );

            // Call the stored procedure
            String call = "{CALL add_star(?, ?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(call)) {
                stmt.setString(1, name);

                // Check if birth_year is provided and parse it to an integer if it is
                if (birth_year != null && !birth_year.isEmpty()) {
                    stmt.setInt(2, Integer.parseInt(birth_year));
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }

                // Register the third parameter as an OUT parameter
                stmt.registerOutParameter(3, Types.VARCHAR);

                // Execute the stored procedure
                stmt.execute();

                // Retrieve the new star ID from the OUT parameter
                String newStarId = stmt.getString(3);

                // Create a JSON object to send as a response
                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("status", "success");
                responseJson.addProperty("new_star_id", newStarId);

                out.println(responseJson.toString());
            }

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            jsonObject.addProperty("query", query);

            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(200);
        } finally {
            out.close();
        }
    }
}
