package name.pathfinder.azalea.mvc.struts2.internal;

import java.util.HashMap;

import name.pathfinder.azalea.mvc.struts2.AzaleaContants;
import name.pathfinder.azalea.mvc.struts2.OsgiClassLoader;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.util.finder.ClassLoaderInterface;

public class OsgiInterceptor extends AbstractInterceptor{

	private static final long serialVersionUID = -6005535986346630523L;

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		
		Object action = invocation.getAction();
		ActionContext ctxt = ActionContext.getContext();
		if(null == ctxt) {
			ctxt = new ActionContext(new HashMap<String, Object>());
		}
		Bundle oldBundle = (Bundle) ctxt.get(AzaleaContants.CURRENT_BUNDLE_KEY);
		Bundle currentBundle = FrameworkUtil.getBundle(action.getClass());
		ctxt.put(AzaleaContants.CURRENT_BUNDLE_KEY, currentBundle);
		
		ClassLoaderInterface classLoader = OsgiClassLoader.getClassLoaderInterface(currentBundle);
		ClassLoaderInterface old = (ClassLoaderInterface) ctxt.get(ClassLoaderInterface.CLASS_LOADER_INTERFACE);
		ctxt.put(ClassLoaderInterface.CLASS_LOADER_INTERFACE, classLoader);
		try {
			return invocation.invoke();
		} finally {
			ctxt.put(ClassLoaderInterface.CLASS_LOADER_INTERFACE, old);
			ctxt.put(AzaleaContants.CURRENT_BUNDLE_KEY, oldBundle);
		}
	}

}
