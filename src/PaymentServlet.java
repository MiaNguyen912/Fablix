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
import java.sql.Date;
import java.text.SimpleDateFormat;


// Declaring a WebServlet called MoviesServlet, which maps to url "/api/20movies"
@WebServlet(name = "PaymentServlet", urlPatterns = "/authenticated/api/payment")
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

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Get the credit card information into variables
            String cardNumber = request.getParameter("card_number");
            String firstName = request.getParameter("first_name");
            String lastName = request.getParameter("last_name");
            String expirationDate = request.getParameter("expiration_date"); // getParameter() return String only


//            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy"); // Adjust the format based on how the date is sent from the client
//            Date expirationDate = (Date) dateFormat.parse(expirationDateString);


            // Make a sql query to the database to see if the information exists in the table creditcards
                //Id varchar(20) primary key,
                //Firstname varchar(50) NOT NULL DEFAULT '',
                //Lastname varchar(50) NOT NULL DEFAULT '',
                //Expiration date not null
            String query = "SELECT * FROM creditcards " +
                    " WHERE Id = ? " +
                    " AND Firstname = ?" +
                    " AND Lastname = ?" +
                    " AND Expiration = '" + expirationDate + "'";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, cardNumber);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
//            statement.setDate(4, new java.sql.Date(expirationDate));

            ResultSet resultSet = statement.executeQuery(query);

            JsonObject responseJson = new JsonObject();

            if (resultSet.next()) {
                // Card information exists, return success message

                // If query matches, then using cart_data, make a new insert into the sales table
//            Id integer primary key auto_increment,
//            Customerid integer not null references customers(id),
//            Moviewid varchar(10) NOT NULL DEFAULT '' references movies(id),
//            Saledate Date not null
//            Quantity INT NOT NULL DEFAULT 0
                // Like
// INSERT INTO INSERT INTO sales (Customerid, Moviewid, Saledate, Quantity) VALUES(786,490001,'tt0339507', '2005/01/08', moviequantity);


                responseJson.addProperty("success", true);
            } else {
                // Card information does not exist, return error message
                responseJson.addProperty("success", false);
            }

            out.println(responseJson.toString());

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
    }
}
