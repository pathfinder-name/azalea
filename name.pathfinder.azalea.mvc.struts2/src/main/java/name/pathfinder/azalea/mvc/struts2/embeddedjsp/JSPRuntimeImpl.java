package name.pathfinder.azalea.mvc.struts2.embeddedjsp;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.views.util.UrlHelper;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.finder.ClassLoaderInterface;

public class JSPRuntimeImpl implements JSPRuntime {
	// maps from jsp path -> pagelet
	protected ServletCache servletCache = null;
	
	private ServletContext servletContext;
	
	@Inject
	public JSPRuntimeImpl(ServletContext servletContext) {
		this.servletContext = servletContext;
		this.servletCache = new ServletCache(this.servletContext);
	}

	/* (non-Javadoc)
	 * @see com.supcon.orchid.container.runtime.struts2.embeddedjsp.JSPRuntime#clearCache()
	 */
	@Override
	public void clearCache() {
		servletCache.clear();
	}

	/* (non-Javadoc)
	 * @see com.supcon.orchid.container.runtime.struts2.embeddedjsp.JSPRuntime#handle(java.lang.String)
	 */
	@Override
	public void handle(String location) throws Exception {
		handle(location, false, null);
	}
	
	/* (non-Javadoc)
	 * @see com.supcon.orchid.container.runtime.struts2.embeddedjsp.JSPRuntime#handle(java.lang.String, com.opensymphony.xwork2.util.finder.ClassLoaderInterface)
	 */
	@Override
	public void handle(String location, ClassLoaderInterface classLoaderInterface) throws Exception {
		handle(location, false, classLoaderInterface);
	}

	/* (non-Javadoc)
	 * @see com.supcon.orchid.container.runtime.struts2.embeddedjsp.JSPRuntime#handle(java.lang.String, boolean, com.opensymphony.xwork2.util.finder.ClassLoaderInterface)
	 */
	@Override
	public void handle(String location, boolean flush, ClassLoaderInterface classLoaderInterface) throws Exception {
		final HttpServletResponse response = ServletActionContext.getResponse();
		final HttpServletRequest request = ServletActionContext.getRequest();

		int i = location.indexOf("?");
		if (i > 0) {
			// extract params from the url and add them to the request
			Map parameters = ActionContext.getContext().getParameters();
			String query = location.substring(i + 1);
			Map queryParams = UrlHelper.parseQueryString(query, true);
			if (queryParams != null && !queryParams.isEmpty())
				parameters.putAll(queryParams);
			location = location.substring(0, i);
		}

		JspServletWrapper wrapper = servletCache.get(location, classLoaderInterface);
		if(null == wrapper) throw new ServletException("Can not compile jsp for " + location);
		HttpJspPage page = (HttpJspPage) wrapper.getServlet();

		page._jspService(request, response);
		if (flush)
			response.flushBuffer();
	}
}
