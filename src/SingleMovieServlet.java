
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie?id=..."
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT * " +
                    "FROM movies m " +
                    "JOIN genres_in_movies gm ON m.id = gm.movieid " +
                    "JOIN genres g ON g.id = gm.genreid " +
                    "JOIN stars_in_movies sm USING(movieid) " +
                    "JOIN stars s ON s.id = sm.starid " +
                    "WHERE m.id = ?";

            /*
            SELECT *
            FROM movies m
            JOIN genres_in_movies gm ON m.id = gm.movieid
            JOIN genres g ON g.id = gm.genreid
            JOIN stars_in_movies sm USING(movieid)
            JOIN stars s ON s.id = sm.starid
            WHERE m.id = 'tt0395642';
             */




            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            if (rs.next()) {

                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");

                HashMap<String, String> movieGenres = new HashMap<>();
                String genre_ID = "" + rs.getInt("genreid"); //cast int to string
                String genre_name = rs.getString("g.name");
                movieGenres.put(genre_ID, genre_name);

                HashMap<String, String> movieStars = new HashMap<>();
                String star_ID = rs.getString("starid");
                String star_name = rs.getString("s.name");
                movieStars.put(star_ID, star_name);

                while(rs.next()){
                    genre_ID = "" + rs.getInt("genreid"); //cast int to string
                    genre_name = rs.getString("g.name");
                    movieGenres.put(genre_ID, genre_name);

                    star_ID = rs.getString("starid");
                    star_name = rs.getString("s.name");
                    movieStars.put(star_ID, star_name);
                }

                // Convert movieStars to JsonObject
                JsonObject starsJson = new JsonObject();
                for (String key : movieStars.keySet()) {
                    starsJson.addProperty(key, movieStars.get(key));
                }

                // Convert movieGenres to JsonObject
                JsonObject genresJson = new JsonObject();
                for (String key : movieGenres.keySet()) {
                    genresJson.addProperty(key, movieGenres.get(key));
                }

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.add("stars", starsJson);
                jsonObject.add("genres", genresJson);
                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
