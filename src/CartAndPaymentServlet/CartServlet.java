package CartAndPaymentServlet;

import com.google.gson.JsonArray;
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
import java.sql.*;
import java.util.Collections;
import java.util.HashMap;


// Declaring a WebServlet called MoviesServlet, which maps to url "/api/20movies"
@WebServlet(name = "CartAndPaymentServlet.CartServlet", urlPatterns = "/authenticated/api/cart")
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbReadOnly");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        String query = "";

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            //Statement statement = conn.createStatement();

            // String of movieIds separated by ","
            String movieIdsString = request.getParameter("movieIds");
            String[] movieIdsArray = movieIdsString.split(","); // Split the movie IDs string into an array of individual IDs

            String placeholder = "";
            for (int i = 0; i < movieIdsArray.length; i++) {
                placeholder += "?";
                if (i < movieIdsArray.length - 1) {
                    placeholder += ",";
                }
            }
            query = "SELECT * FROM movies WHERE id IN (" + placeholder + ")";

            PreparedStatement statement = conn.prepareStatement(query);
            for (int i = 0; i < movieIdsArray.length; i++) {
                statement.setString(i + 1, movieIdsArray[i]);
            }

            ResultSet rs = statement.executeQuery();


            // Perform the query
            //ResultSet rs = statement.executeQuery(queryBuilder.toString());

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String title = rs.getString("title");
                int price = rs.getInt("price");
                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", title);
                jsonObject.addProperty("movie_price", price);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

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

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
