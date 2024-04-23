import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedb,
 * generates output as a html <table>
 */

// Declaring a WebServlet called FormServlet, which maps to url "/api/search"
@WebServlet(name = "MoviesByTitleServlet", urlPatterns = "/authenticated/api/title")
public class MoviesByTitleServlet extends HttpServlet {
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // Use http GET
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/html");    // Response mime type
        PrintWriter out = response.getWriter(); // Output stream to STDOUT

        try {
            Connection conn = dataSource.getConnection(); // Create a new connection to database

            // ----------- Retrieve parameters from the http request
            String firstLetter = request.getParameter("start");
            String sort_style = request.getParameter("sort-style");
            int limit = Integer.parseInt(request.getParameter("limit"));
            int page = Integer.parseInt(request.getParameter("page"));

            // ------------ Generate a SQL query
            /*
                SELECT distinct_movies.movieid, rating, numvotes, title, year, director, genreid, g.name as genrename, sm.starid, s.name, birthYear, movie_count
                FROM (
                    SELECT *
                    FROM ratings r
                    JOIN movies m ON r.movieid = m.id
                    WHERE title LIKE 'b%'
                    ORDER BY title ASC, rating ASC
                    LIMIT 10 OFFSET 0
                ) AS distinct_movies
                JOIN genres_in_movies gm ON gm.movieid = distinct_movies.movieid
                JOIN genres g ON g.id = gm.genreid
                JOIN stars_in_movies sm ON sm.movieid = distinct_movies.movieid
                JOIN stars s ON s.id = sm.starid
                JOIN (
                    SELECT starid, COUNT(movieid) AS movie_count
                    FROM stars_in_movies
                    GROUP BY starid
                ) sp ON s.id = sp.starid
                ORDER BY title ASC, rating ASC, movie_count DESC, s.name ASC;

            */
            String order_by = "";
            if (sort_style.equals("title_asc")){
                order_by = "title ASC, rating ASC";
            } else if (sort_style.equals("title_desc")){
                order_by = "title DESC, rating DESC";
            } else if (sort_style.equals("rating_asc")){
                order_by = "rating ASC, title ASC";
            } else {
                order_by = "rating DESC, title DESC";
            }

            String like = "";
            if (firstLetter.equals("*")){
                like = " RLIKE '^[^A-Za-z0-9]' ";
            } else {
                like = " LIKE '" + firstLetter + "%' ";
            }

            String query = "SELECT distinct_movies.movieid, rating, numvotes, title, year, director, genreid, g.name as genrename, sm.starid, s.name, birthYear, movie_count\n" +
                    "                FROM (\n" +
                    "                    SELECT *\n" +
                    "                    FROM ratings r\n" +
                    "                    JOIN movies m ON r.movieid = m.id\n" +
                    "                    WHERE title " + like +
                    "                    ORDER BY " + order_by +
                    "                    LIMIT ? OFFSET ?" +
                    "                ) AS distinct_movies\n" +
                    "                JOIN genres_in_movies gm ON gm.movieid = distinct_movies.movieid\n" +
                    "                JOIN genres g ON g.id = gm.genreid\n" +
                    "                JOIN stars_in_movies sm ON sm.movieid = distinct_movies.movieid\n" +
                    "                JOIN stars s ON s.id = sm.starid\n" +
                    "                JOIN (\n" +
                    "                    SELECT starid, COUNT(movieid) AS movie_count\n" +
                    "                    FROM stars_in_movies\n" +
                    "                    GROUP BY starid\n" +
                    "                ) sp ON s.id = sp.starid\n" +
                    "                ORDER BY " + order_by + ", movie_count DESC, s.name ASC;";


            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, limit);
            statement.setInt(2,limit*(page-1));

            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            String current_movie_ID = "";
            boolean hasNextRow = rs.next();
            while (hasNextRow) {
                String movie_id = rs.getString("movieid");
                current_movie_ID = movie_id; // update current_movie_id

                String title = rs.getString("title");
                String year = rs.getString("year");
                String director = rs.getString("director");
                float rating = rs.getFloat("rating");
                TreeMap<String, String> genres = new TreeMap<>();
                LinkedHashMap<String, String> stars = new LinkedHashMap<>();

                String genre_ID = "" + rs.getInt("genreid"); //cast int to string
                String genre_name = rs.getString("genrename");
                genres.put(genre_ID, genre_name);

                String star_ID = rs.getString("starid");
                String star_name = rs.getString("s.name");
                stars.put(star_ID, star_name);

                hasNextRow = rs.next();
                while(hasNextRow){
                    String this_movie_id = rs.getString("movieid");

                    // if not the same movie, skip recording stars/genres and go to next row
                    if(!this_movie_id.equals(current_movie_ID)){
                        break;
                    }


                    genre_ID = "" + rs.getInt("genreid"); //cast int to string
                    genre_name = rs.getString("genrename");
                    genres.put(genre_ID, genre_name);

                    star_ID = rs.getString("starid");
                    star_name = rs.getString("s.name");
                    stars.put(star_ID, star_name);
                    hasNextRow = rs.next();
                }


                // Convert stars to JsonObject
                JsonObject starsJson = new JsonObject();
                for (String key : stars.keySet()) {
                    starsJson.addProperty(key, stars.get(key));
                }

                // Convert genres to JsonObject
                JsonObject genresJson = new JsonObject();
                for (String key : genres.keySet()) {
                    genresJson.addProperty(key, genres.get(key));
                }


                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", title);
                jsonObject.addProperty("movie_year", year);
                jsonObject.addProperty("movie_director", director);
                jsonObject.addProperty("movie_rating", rating);
                jsonObject.add("stars", starsJson);
                jsonObject.add("genres", genresJson);

                jsonArray.add(jsonObject);
            }

            // Close all structures
            rs.close();
            statement.close();
            conn.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");
            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            /*
             * After you deploy the WAR file through tomcat manager webpage,
             *   there's no console to see the print messages.
             * Tomcat append all the print messages to the file: tomcat_directory/logs/catalina.out
             *
             * To view the last n lines (for example, 100 lines) of messages you can use:
             *   tail -100 catalina.out
             * This can help you debug your program after deploying it on AWS.
             */


            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();
    }
}
