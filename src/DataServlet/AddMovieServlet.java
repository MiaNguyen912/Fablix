package DataServlet;

import com.google.gson.JsonObject;
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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;


@WebServlet(name = "DataServlet.AddMovieServlet", urlPatterns = "/_dashboard/loggedin/api/add-movie")
public class AddMovieServlet extends HttpServlet {
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
        System.out.println("Starting doPost for AddMovieServlet");
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
            String title = request.getParameter("title");
            String director = request.getParameter("director");
            String year = request.getParameter("year");
            String genre = request.getParameter("genre");
            String star_name = request.getParameter("star_name");

            // table for reference
//            CREATE TABLE IF NOT EXISTS stars (
//                   id VARCHAR(10) PRIMARY KEY,
//                   name VARCHAR(100) NOT NULL DEFAULT '',
//                   birthYear INTEGER
//            );

            // Call the stored procedure
            String call = "{CALL add_movie(?, ?, ?, ?, ?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(call)) {
                stmt.setString(1, title);
                stmt.setString(2, director);
                stmt.setInt(3, Integer.parseInt(year));
                stmt.setString(4, genre);
                stmt.setString(5, star_name);

                // Register the sixth parameter as an OUT parameter
                stmt.registerOutParameter(6, Types.VARCHAR);

                // Execute the stored procedure
                stmt.execute();

                // Retrieve the new movie ID from the OUT parameter
                String new_movie_id = stmt.getString(6);

                // Create a JSON object to send as a response
                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("status", "success");
                responseJson.addProperty("new", new_movie_id);

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
