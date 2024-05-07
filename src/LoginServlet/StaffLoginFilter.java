package LoginServlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginServlet.LoginFilter
 */
@WebFilter(filterName = "LoginServlet.LoginServlet.StaffLoginFilter", urlPatterns = "/fablix/_dashboard/*")
public class StaffLoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginServlet.LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        // Redirect to login page if the "user" attribute doesn't exist in session
        if (httpRequest.getSession().getAttribute("staff") == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/fablix/_dashboard/login.html");

        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {

        if (requestURI.contains("../assets/")) {
            return true;
        }

        // allow all files/url specified in allowedURIs
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("/fablix/_dashboard/login.html"); // login page
        allowedURIs.add("/fablix/_dashboard/login.js");
        allowedURIs.add("/fablix/_dashboard/api/staff-login");
//        allowedURIs.add("/login.html"); // login page
//        allowedURIs.add("/login.js");
//        allowedURIs.add("/api/staff-login");

    }

    public void destroy() {
        // ignored.
    }

}
