package net.risesoft.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.jodconverter.core.util.OSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import net.risesoft.config.ConfigConstants;
import net.risesoft.utils.WebUtils;

import io.mola.galimatias.GalimatiasParseException;

public class TrustDirFilter implements Filter {

    private String notTrustDirView;
    private final Logger logger = LoggerFactory.getLogger(TrustDirFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        ClassPathResource classPathResource = new ClassPathResource("templates/notTrustDir.html");
        try {
            classPathResource.getInputStream();
            byte[] bytes = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            this.notTrustDirView = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        String url = WebUtils.getSourceUrl(request);
        if (!allowPreview(url)) {
            response.getWriter().write(this.notTrustDirView);
            response.getWriter().close();
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }

    private boolean allowPreview(String urlPath) {
        // 判断URL是否合法
        if (!StringUtils.hasText(urlPath) || !WebUtils.isValidUrl(urlPath)) {
            return false;
        }
        try {
            URL url = WebUtils.normalizedURL(urlPath);
            if ("file".equals(url.getProtocol().toLowerCase(Locale.ROOT))) {
                String filePath = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8.name());
                if (OSUtils.IS_OS_WINDOWS) {
                    filePath = filePath.replaceAll("/", "\\\\");
                }
                return filePath.startsWith(ConfigConstants.getFileDir())
                    || filePath.startsWith(ConfigConstants.getLocalPreviewDir());
            }
            return true;
        } catch (IOException | GalimatiasParseException e) {
            logger.error("解析URL异常，url：{}", urlPath, e);
            return false;
        }
    }
}
