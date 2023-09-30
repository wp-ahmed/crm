package site.easy.to.build.crm.util;

import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import jakarta.servlet.ServletContext;

@Component
public class TomcatWebAppValidator implements ServletContextAware {

    private ServletContext servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public boolean isValidWebApp(String webAppPath) {
        String realPath = servletContext.getRealPath(webAppPath);
        return realPath != null;
    }
}