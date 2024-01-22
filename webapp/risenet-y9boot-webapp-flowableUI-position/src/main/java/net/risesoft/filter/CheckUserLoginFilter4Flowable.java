package net.risesoft.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.risesoft.api.org.PersonApi;
import net.risesoft.api.org.PositionApi;
import net.risesoft.model.platform.Position;
import net.risesoft.model.user.UserInfo;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;

public class CheckUserLoginFilter4Flowable implements Filter {

    protected final Logger log = LoggerFactory.getLogger(CheckUserLoginFilter4Flowable.class);

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpSession session = request.getSession();
        try {
            UserInfo loginPerson = Y9LoginUserHolder.getUserInfo();
            if (loginPerson != null) {
                String positionId = request.getHeader("positionId");
                if (StringUtils.isNotBlank(positionId)) {
                    session.setAttribute("positionId", positionId);
                    Y9LoginUserHolder.setPositionId(positionId);
                    PositionApi positionApi = Y9Context.getBean(PositionApi.class);
                    Position position = positionApi.getPosition(loginPerson.getTenantId(), positionId).getData();
                    if (position != null) {
                        Y9LoginUserHolder.setPosition(position);
                    }
                } else {
                    PersonApi personApi = Y9Context.getBean(PersonApi.class);
                    List<Position> list = personApi.listPositions(loginPerson.getTenantId(), loginPerson.getPersonId()).getData();
                    if (list.size() > 0) {
                        Y9LoginUserHolder.setPosition(list.get(0));
                    }
                }
            }
            chain.doFilter(servletRequest, servletResponse);
        } finally {
            Y9LoginUserHolder.clear();
        }
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        log.debug("......................................init Y9SkipSSOFilter ...");
    }
}
