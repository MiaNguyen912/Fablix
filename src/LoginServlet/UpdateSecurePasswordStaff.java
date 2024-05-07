package LoginServlet;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

import java.awt.image.AreaAveragingScaleFilter;
import java.sql.*;
import java.util.ArrayList;

public class UpdateSecurePasswordStaff {

    /*
     *
     * This program updates your existing moviedb employees table to change the
     * plain text passwords to encrypted passwords.
     *
     * You should only run this program **once**, because this program uses the
     * existing passwords as real passwords, then replace them. If you run it more
     * than once, it will treat the encrypted passwords as real passwords and
     * generate wrong values.
     *
     */
    public static void main(String[] args) throws Exception {

        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

        // change the employees table password column from VARCHAR(20) to VARCHAR(128)
        String alterQuery = "ALTER TABLE employees MODIFY COLUMN password VARCHAR(128)";
        PreparedStatement statement = connection.prepareStatement(alterQuery);

        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("altering employees table schema completed, " + alterResult + " rows affected");

        // get the email and password for each employee
        String query = "SELECT email, password from employees";

        ResultSet rs = statement.executeQuery(query);

        // we use the StrongPasswordEncryptor from jasypt library (Java Simplified Encryption)
        //  it internally use SHA-256 algorithm and 10,000 iterations to calculate the encrypted password
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

        ArrayList<String> encryptedPasswords = new ArrayList<>();
        ArrayList<String> emails = new ArrayList<>();

        System.out.println("encrypting password (this might take a while)");
        while (rs.next()) {
            // get the email and plain text password from current table
            String email = rs.getString("email");
            String password = rs.getString("password");

            // encrypt the password using StrongPasswordEncryptor
            String encryptedPassword = passwordEncryptor.encryptPassword(password);

            // store results
            encryptedPasswords.add(encryptedPassword);
            emails.add(email);
        }
        rs.close();

        // execute the update queries to update the password
        System.out.println("updating password");
        int count = 0;
        String updateQuery = "UPDATE employees SET password=? WHERE email=?";
        statement = connection.prepareStatement(updateQuery);
        for (int i = 0; i< encryptedPasswords.size(); i++) {
            statement.setString(1, encryptedPasswords.get(i));
            statement.setString(2, emails.get(i));
            int updateResult = statement.executeUpdate();
            count += updateResult;
        }
        System.out.println("updating password completed, " + count + " rows affected");

        statement.close();
        connection.close();

        System.out.println("finished");

    }

}
