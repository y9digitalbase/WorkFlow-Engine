package net.risesoft.web.filter;


import javax.servlet.*;
import java.io.IOException;

/**
 * @author lizihwen
 * @date 2023-08-02
 */
public class ChinesePathFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
