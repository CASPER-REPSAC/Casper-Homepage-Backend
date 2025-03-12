package com.example.newsper.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.io.IOException;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<FileDownloadFilter> fileDownloadFilter() {
        FilterRegistrationBean<FileDownloadFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new FileDownloadFilter());
        // Apply filter to all upload files
        registrationBean.addUrlPatterns(
                "/profile/*",
                "/article/*",
                "/assignment/*",
                "/submit/*"
        );
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }

    public static class FileDownloadFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String path = httpRequest.getRequestURI();
            String filename = path.substring(path.lastIndexOf('/') + 1);
            String fileType = filename.substring(filename.lastIndexOf('.') + 1);
            // check file is inlinable type(for img, iframe tag)
            boolean isInline = false;
            switch (fileType) {
                case "jpg":
                case "jpeg":
                    httpResponse.setContentType("image/jpeg");
                    isInline = true;
                    break;
                case "png":
                    httpResponse.setContentType("image/png");
                    isInline = true;
                    break;
                case "gif":
                    httpResponse.setContentType("image/gif");
                    isInline = true;
                    break;
                case "bmp":
                    httpResponse.setContentType("image/bmp");
                    isInline = true;
                    break;
                case "pdf":
                    httpResponse.setContentType("application/pdf");
                    isInline = true;
                    break;
                default:
                    httpResponse.setContentType("application/octet-stream");
                    break;
            }
            if (isInline) {
                httpResponse.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
            } else {
                httpResponse.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            }
            chain.doFilter(request, response);
        }
    }
}

