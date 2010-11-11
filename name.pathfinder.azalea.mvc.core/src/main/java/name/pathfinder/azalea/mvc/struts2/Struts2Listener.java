/**
 * 
 */
package name.pathfinder.azalea.mvc.struts2;

import java.util.HashMap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.finder.ClassLoaderInterface;
import com.opensymphony.xwork2.util.finder.ClassLoaderInterfaceDelegate;

/**
 * @author yaowei
 *
 */
public class Struts2Listener implements ServletContextListener {
	
	private static final Logger logger = LoggerFactory.getLogger(Struts2Listener.class);

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		Bundle currentBundle = FrameworkUtil.getBundle(getClass());
		
		ActionContext context = ActionContext.getContext();
		if(null == context) {
			context = new ActionContext(new HashMap<String, Object>());
			ActionContext.setContext(context);
		}
		ClassLoaderInterface parent = (ClassLoaderInterface) context.get(ClassLoaderInterface.CLASS_LOADER_INTERFACE);
		context.put(ClassLoaderInterface.CLASS_LOADER_INTERFACE, new OsgiClassLoader(currentBundle, null == parent ? new ClassLoaderInterfaceDelegate(getClass().getClassLoader()) : parent));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub

	}

}
