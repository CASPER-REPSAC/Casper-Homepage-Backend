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
            String filename = path.substring(path.lastIndexOf('/') + 1).toLowerCase();
            String fileType = filename.substring(filename.lastIndexOf('.') + 1);
            // check file is inlinable type(for img, iframe tag)
            boolean isInline = false;
            switch (fileType) {
                case "jpg":
                case "jpeg":
                case "jpe":
                case "jif":
                case "jfif":
                case "jfi":
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
                case "dib":
                    httpResponse.setContentType("image/bmp");
                    isInline = true;
                    break;
                case "svg":
                    httpResponse.setContentType("image/svg+xml");
                    isInline = true;
                    break;
                case "svgz":
                    httpResponse.setContentType("image/svg+xml");
                    httpResponse.setHeader("Content-Encoding", "gzip");
                    isInline = true;
                    break;
                case "webp":
                    httpResponse.setContentType("image/webp");
                    isInline = true;
                    break;
                case "avif":
                    httpResponse.setContentType("image/avif");
                    isInline = true;
                    break;
                case "jxl":
                    httpResponse.setContentType("image/jxl");
                    isInline = true;
                    break;
                case "apng":
                    httpResponse.setContentType("image/apng");
                    isInline = true;
                    break;
                case "ico":
                    httpResponse.setContentType("image/x-icon");
                    isInline = true;
                    break;
                case "cur":
                    httpResponse.setContentType("image/vnd.microsoft.icon");
                    isInline = true;
                    break;
                case "tif":
                case "tiff":
                    httpResponse.setContentType("image/tiff");
                    isInline = true;
                    break;
                case "jp2":
                case "j2k":
                case "jpf":
                case "jpx":
                case "jpm":
                case "jpg2":
                case "mj2":
                    httpResponse.setContentType("image/jp2");
                    isInline = true;
                    break;
                case "heic":
                case "heif":
                case "heics":
                case "heifs":
                case "avci":
                case "avcs":
                case "hif":
                    httpResponse.setContentType("image/heic");
                    isInline = true;
                    break;
                case "pdf":
                    httpResponse.setContentType("application/pdf");
                    isInline = true;
                    break;
                case "mp4":
                    httpResponse.setContentType("video/mp4");
                    isInline = true;
                    break;
                case "webm":
                    httpResponse.setContentType("video/webm");
                    isInline = true;
                    break;
                case "mkv":
                    httpResponse.setContentType("video/x-matroska");
                    isInline = true;
                    break;
                case "avi":
                    httpResponse.setContentType("video/x-msvideo");
                    isInline = true;
                    break;
                case "wmv":
                    httpResponse.setContentType("video/x-ms-wmv");
                    isInline = true;
                    break;
                case "flv":
                    httpResponse.setContentType("video/x-flv");
                    isInline = true;
                    break;
                case "mov":
                    httpResponse.setContentType("video/quicktime");
                    isInline = true;
                    break;
                case "ogg":
                    httpResponse.setContentType("video/ogg");
                    isInline = true;
                    break;
                case "mp3":
                    httpResponse.setContentType("audio/mpeg");
                    isInline = true;
                    break;
                case "wav":
                    httpResponse.setContentType("audio/wav");
                    isInline = true;
                    break;
                case "flac":
                    httpResponse.setContentType("audio/flac");
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

