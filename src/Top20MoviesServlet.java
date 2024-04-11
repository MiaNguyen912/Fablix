
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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.ArrayList;



// Declaring a WebServlet called MoviesServlet, which maps to url "/api/20movies"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/20movies")
public class Top20MoviesServlet extends HttpServlet {
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
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();

            String query = "SELECT *\n" +
                    "       FROM (\n" +
                    "             SELECT movieid, rating, DENSE_RANK() OVER (ORDER BY rating DESC, movieid) AS ranking\n" +
                    "             FROM ratings\n" +
                    "       ) ranked_ratings\n" +
                    "       JOIN movies m ON ranked_ratings.movieid = m.id\n" +
                    "       JOIN genres_in_movies gm USING(movieid)\n" +
                    "       JOIN genres g ON g.id = gm.genreid\n" +
                    "       JOIN stars_in_movies sm USING(movieid)\n" +
                    "       JOIN stars s ON s.id = sm.starid\n" +
                    "       WHERE ranking <= 20\n" +
                    "       ORDER BY ranked_ratings.ranking;";

            /*SELECT *
            FROM (
                 SELECT movieid, rating, DENSE_RANK() OVER (ORDER BY rating DESC, movieid) AS ranking
                 FROM ratings
            ) ranked_ratings
            JOIN movies m ON ranked_ratings.movieid = m.id
            JOIN genres_in_movies gm USING(movieid)
            JOIN genres g ON g.id = gm.genreid
            JOIN stars_in_movies sm USING(movieid)
            JOIN stars s ON s.id = sm.starid
            WHERE ranking <= 20
            ORDER BY ranked_ratings.ranking ;
            */



            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            String current_movie_ID = "";

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("movieid");
                if (movie_id.equals(current_movie_ID))
                    continue; // go to next row
                else
                    current_movie_ID = movie_id; // update current_movie_id
                String title = rs.getString("title");
                String director = rs.getString("director");
                float rating = rs.getFloat("rating");
                HashMap<String, String> genres = new HashMap<>();
                HashMap<String, String> stars = new HashMap<>();

                // record 3 stars, 3 genres

                while(genres.size()<3 || stars.size()<3){
                    String this_movie_id = rs.getString("movieid");

                    // if not the same movie, skip recording stars/genres and go to next row
                    if(!this_movie_id.equals(current_movie_ID))
                        break;

                    if (genres.size()<3){
                        String genre_ID = "" + rs.getInt("genreid"); //cast int to string
                        String genre_name = rs.getString("g.name");
                        genres.put(genre_ID, genre_name);
                    }

                    if (stars.size()<3){
                        String star_ID = rs.getString("starid");
                        String star_name = rs.getString("s.name");
                        stars.put(star_ID, star_name);
                    }
                    if (!rs.next()) // go to next row
                        break;
                }


                // Convert stars HashMap<String, String> to JsonObject
                JsonObject starsJson = new JsonObject();
                for (String key : stars.keySet()) {
                    starsJson.addProperty(key, stars.get(key));
                }

                // Convert genres HashMap<String, String> to JsonObject
                JsonObject genresJson = new JsonObject();
                for (String key : genres.keySet()) {
                    genresJson.addProperty(key, genres.get(key));
                }


                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", title);
                jsonObject.addProperty("movie_director", director);
                jsonObject.addProperty("movie_rating", rating);
                jsonObject.add("stars", starsJson);
                jsonObject.add("genres", genresJson);


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
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
