/**
 * 
 */
package name.pathfinder.azalea.mvc.struts2;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
public class OsgiClassLoader implements ClassLoaderInterface {
	
	private Bundle currentBundle;
	private static final Logger logger = LoggerFactory.getLogger(OsgiClassLoader.class);

	private ClassLoaderInterface parentClassLoaderInterface;
	
	private static ConcurrentMap<Long, OsgiClassLoader> osgiClassLoaderMapping = new ConcurrentHashMap<Long, OsgiClassLoader>();

	// private Map<Long, ClassLoaderInterface> classLoaderInterfaces = new
	// ConcurrentHashMap<Long, ClassLoaderInterface>();

	public OsgiClassLoader(Bundle currentBundle,
			ClassLoaderInterface parentClassLoaderInterface) {
		this.currentBundle = currentBundle;
		this.parentClassLoaderInterface = parentClassLoaderInterface;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.opensymphony.xwork2.util.finder.ClassLoaderInterface#loadClass(java
	 * .lang.String)
	 */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		logger.trace("loadClass: {}", name);
		Class<?> clazz = currentBundle.loadClass(name);
		if (null == clazz)
			clazz = this.parentClassLoaderInterface.loadClass(name);
		return clazz;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.opensymphony.xwork2.util.finder.ClassLoaderInterface#getResource(
	 * java.lang.String)
	 */
	@Override
	public URL getResource(String name) {
		logger.trace("getResource: {}", name);
		URL resource = currentBundle.getResource(name);
		if (null == resource)
			resource = this.parentClassLoaderInterface.getResource(name);
		return resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.opensymphony.xwork2.util.finder.ClassLoaderInterface#getResources
	 * (java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		logger.trace("getResources: {}", name);
		if (null == name)
			throw new IllegalArgumentException("name can not be null.");
		// Vector<URL> resources = new Vector<URL>();
		Set<URL> resources = new HashSet<URL>();
		Enumeration<URL> result = currentBundle.getResources(name);
		if (null != result)
			resources.addAll(Collections.list(result));
		result = this.parentClassLoaderInterface.getResources(name);
		if (null != result)
			resources.addAll(Collections.list(result));
		result = Thread.currentThread().getContextClassLoader()
				.getResources(name);
		if (null != result)
			resources.addAll(Collections.list(result));
		return Collections.enumeration(resources);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.opensymphony.xwork2.util.finder.ClassLoaderInterface#getResourceAsStream
	 * (java.lang.String)
	 */
	@Override
	public InputStream getResourceAsStream(String name) throws IOException {
		logger.trace("getResourceAsStream: {}", name);
		URL resource = getResource(name);
		if (null != resource)
			return resource.openStream();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.xwork2.util.finder.ClassLoaderInterface#getParent()
	 */
	@Override
	public ClassLoaderInterface getParent() {
		return this.parentClassLoaderInterface;
	}

	public static ClassLoaderInterface getClassLoaderInterfaceFromContext() {
		return getClassLoaderInterface(OsgiClassLoader.class);
	}

	/**
	 * 根据指定的class获取
	 * 
	 * @param clazz
	 * @return
	 */
	public static ClassLoaderInterface getClassLoaderInterface(Class<?> clazz) {
		Bundle bundle = FrameworkUtil.getBundle(clazz);
		return getClassLoaderInterface(bundle);
	}

	/**
	 * 根据当前 bundle 创建一个 OsgiClassLoader 对象。如果当前 ActionContext 中存在 ClassLoaderInterface ，则parentClassLoader 为它，否则为Thread.currentThread().getContextClassLoader()
	 * 
	 * @param bundle
	 * @return
	 */
	public static ClassLoaderInterface getClassLoaderInterface(Bundle bundle) {
		OsgiClassLoader classLoader = osgiClassLoaderMapping.get(bundle.getBundleId());
		if(null != classLoader) return classLoader;
		
		ClassLoaderInterface parentClassLoader = getCurrentClassLoaderInterface();
		if (null == parentClassLoader) {
			classLoader = new OsgiClassLoader(bundle, new ClassLoaderInterfaceDelegate(Thread.currentThread().getContextClassLoader()));
		} else
			classLoader = new OsgiClassLoader(bundle, parentClassLoader);
		OsgiClassLoader pre = osgiClassLoaderMapping.putIfAbsent(bundle.getBundleId(), classLoader);
		return null != pre ? pre : classLoader;
	}

	/**
	 * 获取当前 ActionContext 中的 ClassLoaderInterface
	 * 
	 * @return 如果当前 ActionContext 不为null，则返回 key 为 ClassLoaderInterface.CLASS_LOADER_INTERFACE 的对象;否则返回null。
	 */
	public static ClassLoaderInterface getCurrentClassLoaderInterface() {
		ActionContext ctxt = ActionContext.getContext();
		if (null != ctxt) {
			return (ClassLoaderInterface) ctxt
					.get(ClassLoaderInterface.CLASS_LOADER_INTERFACE);
		}
		return null;
	}
}
