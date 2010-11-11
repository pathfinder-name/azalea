/**
 * 
 */
package name.pathfinder.azalea.mvc.struts2.embeddedjsp;

import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * @author yaowei
 *
 */
public class JSPServletConfig implements ServletConfig {
    private final Enumeration EMPTY_ENUMERATION = Collections.enumeration(Collections.EMPTY_LIST);

    private ServletContext servletContext;

    public JSPServletConfig(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public String getInitParameter(String name) {
        return null;
    }

    public Enumeration getInitParameterNames() {
        return EMPTY_ENUMERATION;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public String getServletName() {
        return null;
    }
}