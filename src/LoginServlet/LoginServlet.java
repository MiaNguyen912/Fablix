package LoginServlet;
import Utility.User;

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login") // LoginServlet.LoginServlet handles POST request sent to /api/login
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    // specify database when init class object
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
        // Retrieve parameters username/password from the POST request.
        String username = request.getParameter("username");
        String password = request.getParameter("password");



        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        // Verify reCAPTCHA
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "fail");
            request.getServletContext().log("Login failed"); // Log to localhost log
            // responseJsonObject.addProperty("message", e.getMessage());
            responseJsonObject.addProperty("message", "Recaptcha verification failed");
            response.setStatus(200); // Set response status to 200 (OK)
            response.getWriter().write(responseJsonObject.toString()); // write out response object
            return;
        }

        // Verifying username and password
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT * FROM customers WHERE email= ?";
            PreparedStatement statement = conn.prepareStatement(query); // Declare statement
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery(); // Perform the query
            JsonObject responseJsonObject = new JsonObject();

            if (rs.next()) { // rs having a row means the username or email exists
                String id = rs.getString("id");

                // verifying password using encrypted password
                VerifyPassword verifier = new VerifyPassword();
                if (verifier.verifyCredentialsCustomers(username, password)){
                    // Login success, set this user into the session
                    request.getSession().setAttribute("user", new User(username, id)); // initialize a Utility.User object
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                }
                else {
                    // Login fail because of wrong password
                    responseJsonObject.addProperty("status", "fail");
                    request.getServletContext().log("Login failed"); // Log to localhost log
                    responseJsonObject.addProperty("message", "incorrect password");
                }



//                // verifying password using plain text password
//                String resulting_password = rs.getString("password");
//                if (resulting_password.equals(password)){
//                    // Login success, set this user into the session
//                    request.getSession().setAttribute("user", new Utility.User(username, id)); // initialize a Utility.User object
//                    responseJsonObject.addProperty("status", "success");
//                    responseJsonObject.addProperty("message", "success");
//                } else {
//                    // Login fail because of wrong password
//                    responseJsonObject.addProperty("status", "fail");
//                    request.getServletContext().log("Login failed"); // Log to localhost log
//                    responseJsonObject.addProperty("message", "incorrect password");
//                }
            }
            else {
                // Login fail because of wrong username/email
                responseJsonObject.addProperty("status", "fail");
                request.getServletContext().log("Login failed"); // Log to localhost log
                responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
            }

            rs.close();
            statement.close();
            response.setStatus(200); // Set response status to 200 (OK)
            response.getWriter().write(responseJsonObject.toString()); // write out response object
        }
        catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            request.getServletContext().log("Error:", e);  // Log error to localhost log
            response.setStatus(500);  // Set response status to 500 (Internal Server Error)
        }
    }
}
