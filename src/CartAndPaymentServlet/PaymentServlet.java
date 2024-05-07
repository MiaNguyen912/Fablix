package CartAndPaymentServlet;

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


@WebServlet(name = "CartAndPaymentServlet", urlPatterns = "/authenticated/api/payment")
public class PaymentServlet extends HttpServlet {
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        String query = "";


        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Get the credit card information into variables
            String cardNumber = request.getParameter("card_number");
            String firstName = request.getParameter("first_name");
            String lastName = request.getParameter("last_name");
            String expirationDate = request.getParameter("expiration_date"); // getParameter() return String only
            String cart_data_string = request.getParameter("cart_data"); // format: '{"tt0395642":2,"tt0424773":1}'
//            System.out.println(cart_data_string);


            // Parse JSON string into a Map
            Map<String, Integer> cart_data = new HashMap<>();

            // Parse JSON string using Gson's JsonParser
            JsonElement jsonElement = JsonParser.parseString(cart_data_string);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Iterate over entries in the JsonObject
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                int value = entry.getValue().getAsInt();
                cart_data.put(key, value);
            }

//            System.out.println(cart_data); // format: {tt0424773=1, tt0395642=1}




            // Make a sql query to the database to see if the information exists in the table creditcards
                //Id varchar(20) primary key,
                //Firstname varchar(50) NOT NULL DEFAULT '',
                //Lastname varchar(50) NOT NULL DEFAULT '',
                //Expiration date not null
            query = "SELECT * FROM creditcards " +
                    " WHERE Id = '" + cardNumber + "'" +
                    " AND Firstname = '" + firstName + "'" +
                    " AND Lastname = '" + lastName + "'" +
                    " AND Expiration = '" + expirationDate + "'";

            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery(query);
            JsonObject responseJson = new JsonObject();

            if (resultSet.next()) { // if card information exists, insert data to the sales table and return success message
                User currentUser = (User) request.getSession().getAttribute("user");
                int userID = Integer.parseInt(currentUser.getUserID());



                ArrayList<Integer> sale_id_list = new ArrayList<>();
                for (Map.Entry<String, Integer> item_data : cart_data.entrySet()) {
                    String movie_id = item_data.getKey();
                    Integer quantity = item_data.getValue();

                    String insert_query = "INSERT INTO sales (Customerid, Movieid, Saledate, Quantity) VALUES (?, ?, ?, ?)";
                    statement = conn.prepareStatement(insert_query, PreparedStatement.RETURN_GENERATED_KEYS);

                    statement.setInt(1, userID);
                    statement.setString(2, movie_id);
                    statement.setDate(3, Date.valueOf(LocalDate.now())); // Assuming Saledate is a Date type in the database
                    statement.setInt(4, quantity);


                    int rowsAffected = statement.executeUpdate();

                    // Check if any rows were affected
                    if (rowsAffected > 0) {
                        resultSet = statement.getGeneratedKeys(); // Retrieve the generated keys (in this case, the auto-generated sale ID)
                        while (resultSet.next()) {
                            int generatedId = resultSet.getInt(1); // Assuming the auto-increment column is the first column
                            sale_id_list.add(generatedId);
                        }
                    }

                }
                currentUser.setSales(sale_id_list);
                System.out.println("Generated ID: " + sale_id_list);
                responseJson.addProperty("status", "success");
            } else {
                // Card information does not exist, return error message
                responseJson.addProperty("status", "fail");
                responseJson.addProperty("message", "Your payment could not be processed. Please use another card or check your provided information");
            }

            out.println(responseJson.toString());

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
    }
}
