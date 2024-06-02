package LoginServlet;
import Utility.Staff;
import Utility.User;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

@WebServlet(name = "LoginServlet.LoginServlet.StaffLoginServlet", urlPatterns = "/_dashboard/api/staff-login")
public class StaffLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    // specify database when init class object
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbReadOnly");
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

        System.out.println("About to verify staff recaptcha 1");
        // Verify reCAPTCHA
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
            System.out.println("Recaptcha response verified");
        } catch (Exception e) {
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "fail");
            request.getServletContext().log("Login failed"); // Log to localhost log
            responseJsonObject.addProperty("message", e.getMessage());
            //responseJsonObject.addProperty("message", "Recaptcha verification failed");
            response.setStatus(200); // Set response status to 200 (OK)
            response.getWriter().write(responseJsonObject.toString()); // write out response object
            return;
        }


        // Verifying username and password
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT * FROM employees WHERE email= ?";
            PreparedStatement statement = conn.prepareStatement(query); // Declare statement
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery(); // Perform the query

            JsonObject responseJsonObject = new JsonObject();


            if (rs.next()) { // rs having a row means the username or email exists -> check password
                String fullName = rs.getString("fullName");

                VerifyPassword verifier = new VerifyPassword();
                if (verifier.verifyCredentialsStaff(username, password)){
                    System.out.println("login success");
                    // Login success, set this user into the session
                    request.getSession().setAttribute("staff", new Staff(username, fullName)); // username is email
                    request.getSession().setAttribute("user", new User(username, "000")); // create a Utility.User object with id=000 so that staff can access user's api used for browsing/searching

                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                } else {
                    System.out.println("login fail");

                    // Login fail because of wrong password
                    responseJsonObject.addProperty("status", "fail");
                    request.getServletContext().log("Login failed"); // Log to localhost log
                    responseJsonObject.addProperty("message", "Incorrect password");
                }





//                String resulting_password = rs.getString("password");
//                String fullName = rs.getString("fullName");
//
//                if (resulting_password.equals(password)){
//                    // Login success, set this user into the session
//                    request.getSession().setAttribute("staff", new Utility.Staff(username, fullName)); // username is email
//                    request.getSession().setAttribute("user", new Utility.User(username, "000")); // create a Utility.User object with id=000 so that staff can access user's api used for browsing/searching
//
//                    responseJsonObject.addProperty("status", "success");
//                    responseJsonObject.addProperty("message", "success");
//                } else {
//                    // Login fail because of wrong password
//                    responseJsonObject.addProperty("status", "fail");
//                    request.getServletContext().log("Login failed"); // Log to localhost log
//                    responseJsonObject.addProperty("message", "Incorrect password");
//                }
            }
            else {
                // Login fail because of wrong username/email
                responseJsonObject.addProperty("status", "fail");
                request.getServletContext().log("Login failed"); // Log to localhost log
                responseJsonObject.addProperty("message", "Employee's username " + username + " doesn't exist");
            }

            rs.close();
            statement.close();
            response.setStatus(200); // Set response status to 200 (OK)
            response.getWriter().write(responseJsonObject.toString()); // write out response object


        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            request.getServletContext().log("Error:", e);  // Log error to localhost log
            response.setStatus(500);  // Set response status to 500 (Internal Server Error)
        }
    }
}
