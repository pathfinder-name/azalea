/**
 * 
 */
package name.pathfinder.azalea.mvc.struts2.embeddedjsp;

import name.pathfinder.azalea.mvc.struts2.OsgiClassLoader;

import org.apache.commons.lang.xwork.StringUtils;
import org.apache.struts2.dispatcher.StrutsResultSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.finder.ClassLoaderInterface;

/**
 * 
 * 
 * @author yaowei
 *
 */
public class EmbeddedJSPResult extends StrutsResultSupport {
	private static final long serialVersionUID = -3309899407185708298L;
	
	private JSPRuntime jspRuntime;
	
	private static final Logger logger = LoggerFactory.getLogger(EmbeddedJSPResult.class);

	protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
		Object action = invocation.getAction();
		ClassLoaderInterface classLoaderInterface = OsgiClassLoader.getCurrentClassLoaderInterface();
//		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
//			Thread.currentThread().setContextClassLoader(action.getClass().getClassLoader());
			getJspRuntime().handle(StringUtils.removeStart(finalLocation, "/"), false, classLoaderInterface);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
//			Thread.currentThread().setContextClassLoader(classLoader);
		}
    }

	@Inject("azalea.jspRuntime")
	public void setJspRuntime(JSPRuntime jspRuntime) {
		this.jspRuntime = jspRuntime;
	}

	public JSPRuntime getJspRuntime() {
		return jspRuntime;
	}
}