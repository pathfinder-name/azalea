/**
 * 
 */
package name.pathfinder.azalea.mvc.struts2.internal;

import java.util.Map;

import name.pathfinder.azalea.mvc.struts2.BundleMappingBuilder;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;

/**
 * @author yaowei
 * 
 */
public class OsgiSpringObjectFactory extends ObjectFactory {

	private static final long serialVersionUID = 1091647757983637267L;

	private ObjectFactory delegateObjectFactory;
	private BundleContext bundleContext;
	private BundleMappingBuilder bundleMappingBuilder;

	private static final Logger logger = LoggerFactory.getLogger(OsgiSpringObjectFactory.class);

	private Bundle parentBundle;

	public OsgiSpringObjectFactory() {
		parentBundle = FrameworkUtil.getBundle(OsgiSpringObjectFactory.class);
		bundleContext = parentBundle.getBundleContext();
		
	}

	@Override
	public Object buildBean(String className, Map<String, Object> extraContext,
			boolean injectInternal) throws Exception {
		Object o = null;
		Class clazz = getClassInstance(className);
		try {
			o = buildBean(clazz, extraContext);
		} catch (NoSuchBeanDefinitionException e) {
			o = delegateObjectFactory.buildBean(clazz, extraContext);
		}
		if (injectInternal) {
			injectInternalBeans(o);
		}
		return o;
	}

	/**
	 * 实现从
	 */
	@Override
	public Object buildBean(Class clazz, Map<String, Object> extraContext)
			throws Exception {
//		Bundle bundle = bundleMappingBuilder.getCurrentBundle();
		Bundle bundle = FrameworkUtil.getBundle(clazz);
		if(null != bundle) {
			ApplicationContext appContext = getApplicationContext(bundle);
			if (null != appContext) {
				try {
					Object bean = appContext.getBean(clazz);
					if (null != bean)
						return bean;
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return delegateObjectFactory.buildBean(clazz, extraContext);
	}

	protected ApplicationContext getApplicationContext(Bundle bundle)
			throws InvalidSyntaxException {
		ServiceReference[] registeredServices = bundle.getRegisteredServices();
		for(ServiceReference ref : registeredServices) {
			if(ref.isAssignableTo(bundle, ApplicationContext.class.getName())) 
				return (ApplicationContext) bundleContext.getService(ref);
		}
		return null;
	}

	@Override
	public Class getClassInstance(String className)
			throws ClassNotFoundException {
		Class clazz = null;
		Bundle bundle = bundleMappingBuilder.getCurrentBundle();
		if(null != bundle) clazz = bundle.loadClass(className);
		if(null != clazz) return clazz;
		return delegateObjectFactory.getClassInstance(className);
	}
	

	@Inject
	public void setDelegateObjectFactory(@Inject Container container,
			@Inject("azalea.objectFactory.delegate") String delegate) {
		if (delegate == null) {
			delegate = "struts";
		}
		delegateObjectFactory = container.getInstance(ObjectFactory.class,
				delegate);
	}

	@Inject
	public void setBundleMappingBuilder(
			BundleMappingBuilder bundleMappingBuilder) {
		this.bundleMappingBuilder = bundleMappingBuilder;
	}
}
