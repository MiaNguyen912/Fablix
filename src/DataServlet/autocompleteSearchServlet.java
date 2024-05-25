package DataServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet(name = "DataServlet.autocompleteSearchServlet", urlPatterns = "/authenticated/api/autocomplete")
public class autocompleteSearchServlet extends HttpServlet {
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");    // Response mime type
        PrintWriter out = response.getWriter(); // Output stream to STDOUT
        String query = "";
        String titleQuery = "";
        String[] words = null;
        PreparedStatement statement = null;
        JsonArray jsonArray = new JsonArray();

        try {
            Connection conn = dataSource.getConnection(); // Create a new connection to database
            titleQuery = request.getParameter("titleQuery");

            // return the empty json array if query is null or empty
            if (titleQuery == null || titleQuery.trim().isEmpty()) {
                response.getWriter().write(jsonArray.toString());
                return;
            }
            // define search query
            words = titleQuery.trim().split(" ");
            String placeholder = "";
            for (String word : words) {
                placeholder += "+" + word + "* "; // Append placeholder for each word
            }
            int fuzzySearchThreshold = (int) Math.floor(Math.sqrt(titleQuery.length())) + words.length - 1; //the ed function distinguish capital and normal letter, so we add  words.length to compensate the first letter of every word
            System.out.println("fuzzySearchThreshold: " + fuzzySearchThreshold);
            query = "SELECT * FROM movies WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE) OR ed(title, ?) <= ? OR title LIKE ? OR title LIKE ? LIMIT 10;";
            statement = conn.prepareStatement(query);
            statement.setString(1, placeholder);
            statement.setString(2, titleQuery);
            statement.setInt(3, fuzzySearchThreshold);
            statement.setString(4, titleQuery + "%");
            statement.setString(5, "% " + titleQuery + '%');
            ResultSet rs = statement.executeQuery();

            while (rs.next()){
                String movieID = rs.getString("id");
                String movieTitle = rs.getString("title");
                int year = rs.getInt("year");

                JsonObject jsonSubObject = new JsonObject();
                jsonSubObject.addProperty("id", movieID);
                jsonSubObject.addProperty("title", movieTitle);
                jsonSubObject.addProperty("year", year);

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("value", movieTitle + " (" + year + ")");
                jsonObject.add("data", jsonSubObject);
                jsonArray.add(jsonObject);

                /* required format for autocomplete library: (the data in "value" will appear in suggestion box)
                * [{value: "Tom and Viv (1996)", data: {id: "BGt10", title: "Tom and Viv", year: 1996}},…]
                 */
            }

            // ---------------- Close all structures
            rs.close();
            statement.close();
            conn.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);
        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            jsonObject.addProperty("query", query);
            jsonObject.addProperty("title", titleQuery);
            String words_string = "";
            for (String word : words){
                words_string += word + ", ";
            }
            jsonObject.addProperty("title array", words_string);


            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();
    }


}
