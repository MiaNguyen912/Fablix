
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star/id=..."
@WebServlet(name = "SingleStarServlet", urlPatterns = "/authenticated/api/single-star")
public class SingleStarServlet extends HttpServlet {
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
            String query = "SELECT * FROM stars s JOIN stars_in_movies sm ON s.id = sm.starid JOIN movies m ON sm.movieid = m.id WHERE s.id = ? ORDER BY m.year DESC, m.title ASC";

            /*
            SELECT *
            FROM stars s
            JOIN stars_in_movies sm ON s.id = sm.starid
            JOIN movies m ON sm.movieid = m.id
            ORDER BY s.id;
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
                String starId = rs.getString("starId");
                String starName = rs.getString("name");
                String starYob = rs.getString("birthYear");

                LinkedHashMap<String, ArrayList<String>> movies_of_this_star = new LinkedHashMap<>(); // use LinkedHashMap to keep data in the same order as they're added
                String movieId = rs.getString("movieId");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");

                ArrayList<String> movieDetail = new ArrayList<>();
                movieDetail.add(movieTitle);
                movieDetail.add(movieYear);
                movieDetail.add(movieDirector);
                movies_of_this_star.put(movieId, movieDetail);


                while(rs.next()){
                    movieId = rs.getString("movieId");
                    movieTitle = rs.getString("title");
                    movieYear = rs.getString("year");
                    movieDirector = rs.getString("director");
                    ArrayList<String> thisMovieDetail = new ArrayList<>();

                    thisMovieDetail.add(movieTitle);
                    thisMovieDetail.add(movieYear);
                    thisMovieDetail.add(movieDirector);

                    movies_of_this_star.put(movieId, thisMovieDetail);
                }

                // Convert movieDetail ArrayList<String> to JsonObject
//                JsonObject movieDetailJson = new JsonObject();
//                movieDetailJson.addProperty("title", movieDetail.get(0));
//                movieDetailJson.addProperty("year", movieDetail.get(1));
//                movieDetailJson.addProperty("director", movieDetail.get(2));


                // Convert movies_of_this_star to JsonObject
                JsonObject moviesJson = new JsonObject();
                for (String key : movies_of_this_star.keySet()) {

                    // Convert movieDetail ArrayList<String> to JsonObject
                    JsonObject movieDetailJson = new JsonObject();
                    ArrayList<String> detail = movies_of_this_star.get(key);
                    movieDetailJson.addProperty("title", detail.get(0));
                    movieDetailJson.addProperty("year", detail.get(1));
                    movieDetailJson.addProperty("director", detail.get(2));

                    moviesJson.add(key, movieDetailJson);

//                    moviesJson.addProperty(key, movies_of_this_star.get(key));
                }


                // Create a JsonObject based on the data we retrieve from rs

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("star_id", starId);
                jsonObject.addProperty("star_name", starName);
                jsonObject.addProperty("star_yob", starYob);
                jsonObject.add("movies", moviesJson);


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
