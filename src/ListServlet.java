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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.TreeMap;

/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedb,
 * generates output as a html <table>
 */

// Declaring a WebServlet called FormServlet, which maps to url "/api/search"
@WebServlet(name = "ListServlet", urlPatterns = "/authenticated/api/list")
public class ListServlet extends HttpServlet {
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
        String query = "";

        try {
            Connection conn = dataSource.getConnection(); // Create a new connection to database


            // ----------- Retrieve request type (Whether it is by genre, title, or search parameters)
            String type = request.getParameter("type");


            // ----------- Retrieve general parameters from the http request
            String sort_style = request.getParameter("sort-style");
            int limit = Integer.parseInt(request.getParameter("limit"));
            int page = Integer.parseInt(request.getParameter("page"));


            // ----------- Retrieve type specific parameters from the http request
            String genre = "", firstLetter = "", like = "", titleParam = "", yearParam = "", directorParam = "", starParam = "";
            if (type.equals("genre")){
                genre = request.getParameter("name");
            }
            else if (type.equals("title")){
                firstLetter = request.getParameter("start");
                if (firstLetter.equals("*")){
                    like = " RLIKE '^[^A-Za-z0-9]' ";
                } else {
                    like = " LIKE '" + firstLetter + "%' ";
                }
            } else if (type.equals("search")) {
                titleParam = (request.getParameter("title") != null)? request.getParameter("title"): "";
                yearParam = (request.getParameter("year") != null)? request.getParameter("year"): "";
                directorParam = (request.getParameter("director") != null)? request.getParameter("director"): "";
                starParam = (request.getParameter("star") != null)? request.getParameter("star"): "";
            }


            // ------------ Generate a SQL query
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
            query = "";

            PreparedStatement statement = null;
            if (type.equals("genre")){
                /*
                    SELECT *
                    FROM (
                        SELECT DISTINCT m.id AS movieid, title, year, director, rating, numvotes, genreid, name as genrename
                        FROM ratings r
                        JOIN movies m ON r.movieid = m.id
                        JOIN genres_in_movies gm ON gm.movieid = r.movieid
                        JOIN genres g ON g.id = gm.genreid
                        WHERE g.name = 'action'
                        ORDER BY title DESC, rating DESC
                        LIMIT 10 OFFSET 5
                    ) AS distinct_movies
                    JOIN stars_in_movies sm ON sm.movieid = distinct_movies.movieid
                    JOIN stars s ON s.id = sm.starid
                    JOIN (
                        SELECT starid, COUNT(movieid) AS movie_count
                        FROM stars_in_movies
                        GROUP BY starid
                    ) sp ON s.id = sp.starid
                    ORDER BY title DESC, rating DESC, movie_count DESC, s.name ASC;

                */
                query += "SELECT *\n" +
                        "                FROM (\n" +
                        "                    SELECT DISTINCT m.id AS movieid, title, year, director, rating, numvotes, genreid, name as genrename\n" +
                        "                    FROM ratings r\n" +
                        "                    JOIN movies m ON r.movieid = m.id\n" +
                        "                    JOIN genres_in_movies gm ON gm.movieid = r.movieid\n" +
                        "                    JOIN genres g ON g.id = gm.genreid\n" +
                        "                    WHERE g.name = ?" +
                        "                    ORDER BY " + order_by +
                        "                    LIMIT ? OFFSET ?" +
                        "                ) AS distinct_movies\n" +
                        "                JOIN stars_in_movies sm ON sm.movieid = distinct_movies.movieid\n" +
                        "                JOIN stars s ON s.id = sm.starid\n" +
                        "                JOIN (\n" +
                        "                    SELECT starid, COUNT(movieid) AS movie_count\n" +
                        "                    FROM stars_in_movies\n" +
                        "                    GROUP BY starid\n" +
                        "                ) sp ON s.id = sp.starid " +
                        "                ORDER BY " + order_by + ", movie_count DESC, s.name ASC";

                statement = conn.prepareStatement(query);
                statement.setString(1, genre);
                statement.setInt(2, limit);
                statement.setInt(3,limit*(page-1));
            }
            else if (type.equals("title")){
                /*
                    SELECT distinct_movies.movieid, rating, numvotes, title, year, director, genreid, g.name as genrename, sm.starid, s.name, birthYear, movie_count
                    FROM (
                        SELECT *
                        FROM ratings r
                        JOIN movies m ON r.movieid = m.id
                        WHERE title LIKE 'b%'
                        ORDER BY title ASC, rating ASC
                        LIMIT 25 OFFSET 0
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
                query = "SELECT distinct_movies.movieid, rating, numvotes, title, year, director, genreid, g.name as genrename, sm.starid, s.name, birthYear, movie_count\n" +
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
                        "                ORDER BY " + order_by + ", movie_count DESC, s.name ASC";


                statement = conn.prepareStatement(query);
                statement.setInt(1, limit);
                statement.setInt(2,limit*(page-1));
            }
            else if (type.equals("search")){
                   /*
                    SELECT distinct_movies.movieid, rating, numvotes, title, year, director, genreid, g.name as genrename, sm.starid, s.name, birthYear, movie_count
                    FROM (
                        SELECT *
                        FROM ratings r
                        JOIN movies m ON r.movieid = m.id
                        JOIN (SELECT sm.movieid as movie_of_chosen_star
                            FROM stars_in_movies sm
                            JOIN stars s ON s.id = sm.starid
                            WHERE s.name LIKE '%ste%'
                        ) as movies_of_chosen_star ON movies_of_chosen_star.movie_of_chosen_star = m.id
                        WHERE (title LIKE 'term%' OR title LIKE '% term%')
                            AND year = 2004
                            AND director LIKE '%%'
                        ORDER BY title ASC, rating ASC
                        LIMIT 10 OFFSET 0
                    ) AS distinct_movies
                    JOIN genres_in_movies gm ON gm.movieid = distinct_movies.movieid
                    JOIN genres g ON g.id = gm.genreid
                    JOIN stars_in_movies sm ON sm.movieid = distinct_movies.movieid
                    JOIN stars s ON s.id = sm.starid
                    JOIN ( SELECT starid, COUNT(movieid) AS movie_count
                        FROM stars_in_movies
                        GROUP BY starid
                    ) sp ON s.id = sp.starid
                    ORDER BY title ASC, rating ASC, movie_count DESC, s.name ASC;
                */

                query = " SELECT distinct_movies.movieid, rating, numvotes, title, year, director, genreid, g.name as genrename, sm.starid, s.name, birthYear, movie_count " +
                        " FROM (" +
                        "     SELECT *" +
                        "     FROM ratings r" +
                        "     JOIN movies m ON r.movieid = m.id";
                if (!starParam.isEmpty()) {
                    query += " JOIN (SELECT sm.movieid as movie_of_chosen_star\n" +
                            "       FROM stars_in_movies sm\n" +
                            "       JOIN stars s ON s.id = sm.starid\n" +
                            "       WHERE s.name LIKE '%" + starParam + "%'\n" +
                            " ) as movies_of_chosen_star ON movies_of_chosen_star.movie_of_chosen_star = m.id\n";
                }

                if (!titleParam.isEmpty() || !yearParam.isEmpty() || !directorParam.isEmpty())
                    query += " WHERE ";
                if (!titleParam.isEmpty())
                    query += "(title LIKE '" + titleParam + "%' OR title LIKE '% " + titleParam + "%') AND ";
                if (!yearParam.isEmpty())
                    query += "m.year = " + yearParam + " AND ";
                if (!directorParam.isEmpty())
                    query += "m.director LIKE '%" + directorParam + "%' AND ";
                if (query.endsWith(" AND "))
                    query = query.substring(0, query.length() - 5); // Remove the last " AND " if necessary

                query += "     ORDER BY " + order_by +
                        "      LIMIT ? OFFSET ?" +
                        " ) AS distinct_movies\n" +
                        " JOIN genres_in_movies gm ON gm.movieid = distinct_movies.movieid\n" +
                        " JOIN genres g ON g.id = gm.genreid\n" +
                        " JOIN stars_in_movies sm ON sm.movieid = distinct_movies.movieid\n" +
                        " JOIN stars s ON s.id = sm.starid\n" +
                        " JOIN ( SELECT starid, COUNT(movieid) AS movie_count\n" +
                        "    FROM stars_in_movies\n" +
                        "    GROUP BY starid\n" +
                        " ) sp ON s.id = sp.starid\n" +
                        " ORDER BY " + order_by + ", movie_count DESC, s.name ASC";

                statement = conn.prepareStatement(query);
                statement.setInt(1, limit);
                statement.setInt(2,limit*(page-1));
            }



            // --------------- execute query
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();


            // ---------------- process each row of rs
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

            // ---------------- Close all structures
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
            jsonObject.addProperty("query", query);

            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();
    }
}
