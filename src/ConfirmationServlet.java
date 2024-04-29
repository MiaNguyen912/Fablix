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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@WebServlet(name = "ConfirmationServlet", urlPatterns = "/authenticated/api/confirmation")
public class ConfirmationServlet extends HttpServlet {
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
        PrintWriter out = response.getWriter(); // Output stream to STDOUT

        User currentUser = (User) request.getSession().getAttribute("user");
        ArrayList<Integer> sales_id_list= currentUser.getSales();

        response.setContentType("application/json"); // Response mime type

        String query = "";

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {



            query = "SELECT s.id as sale_id, title, quantity FROM movies m " +
                    "JOIN sales s ON m.id = s.movieid " +
                    " WHERE s.id IN (";
            for (int id : sales_id_list){
                query += id + ",";
            }
            query = query.substring(0, query.length() - 1);
            query += ")";

            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery(query);
            JsonArray jsonArray = new JsonArray();

            JsonObject responseJson = new JsonObject();

            while (resultSet.next()) { // if card information exists, insert data to the sales table and return success message
                int sale_id = resultSet.getInt("sale_id");
                String title = resultSet.getString("title");
                int quantity = resultSet.getInt("quantity");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("sale_id", sale_id);
                jsonObject.addProperty("movie_title", title);
                jsonObject.addProperty("quantity", quantity);
                jsonArray.add(jsonObject);
            }
            resultSet.close();
            statement.close();
            out.write(jsonArray.toString());
            response.setStatus(200);
        }
        catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            jsonObject.addProperty("query", query);
            out.write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }


    }
}
