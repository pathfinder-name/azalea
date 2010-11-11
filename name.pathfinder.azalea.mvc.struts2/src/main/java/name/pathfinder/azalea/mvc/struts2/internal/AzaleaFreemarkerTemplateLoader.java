package name.pathfinder.azalea.mvc.struts2.internal;

import java.net.URL;

import name.pathfinder.azalea.mvc.struts2.OsgiClassLoader;

import com.opensymphony.xwork2.util.finder.ClassLoaderInterface;

import freemarker.cache.URLTemplateLoader;

public class AzaleaFreemarkerTemplateLoader extends URLTemplateLoader {

	@Override
	protected URL getURL(String name) {
		URL resource = null;
		ClassLoaderInterface classLoaderInterface = OsgiClassLoader.getCurrentClassLoaderInterface();
		if(null != classLoaderInterface) {
			resource = classLoaderInterface.getResource(name);
		}
		return resource;
	}

}
