package name.pathfinder.azalea.mvc.struts2.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import name.pathfinder.azalea.mvc.struts2.BundleMappingBuilder;

import org.apache.struts2.dispatcher.DefaultStaticContentLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.inject.Inject;

/**
 * <code>/static/251/css/styles.css</code>
 * 
 * @author yaowei
 *
 */
public class AzaleaStaticRContentLoader  extends DefaultStaticContentLoader{

	private static final Logger logger = LoggerFactory.getLogger(AzaleaStaticRContentLoader.class);
	
	private BundleMappingBuilder bundleMappingBuilder;
	
	private BundleContext bundleContext;
	
	public AzaleaStaticRContentLoader() {
		bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
	}
	
	
	@Override
	public void findStaticResource(String path, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
        String name = cleanupPath(path);
        String bundleId = null;
        Bundle bundle = null;
        try {
        	if(name.startsWith("/"))
        		bundleId = name.substring(1, name.indexOf("/", 1));
        	else
        		bundleId = name.substring(0, name.indexOf("/"));
        	bundle = bundleContext.getBundle(Long.parseLong(bundleId));
		} catch (NumberFormatException e) {
			logger.debug(e.getMessage(), e);
		}
		
		if(null != bundle) name = name.substring(name.indexOf("/") + 1 + bundleId.length());
		
        for (String pathPrefix : pathPrefixes) {
            URL resourceUrl = null;
            String buildPath = buildPath(name, pathPrefix);
			if(null != bundle) resourceUrl = bundle.getResource(buildPath);
            else resourceUrl = findResource(buildPath);
            if (resourceUrl != null) {
                InputStream is = null;
                try {
                    //check that the resource path is under the pathPrefix path
                    if (resourceUrl.getFile().endsWith(buildPath))
                        is = resourceUrl.openStream();
                } catch (IOException ex) {
                    // just ignore it
                    continue;
                }

                //not inside the try block, as this could throw IOExceptions also
                if (is != null) {
                    process(is, path, request, response);
                    return;
                }
            }
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
	
	

	@Inject
	public void setBundleMappingBuilder(BundleMappingBuilder bundleMappingBuilder) {
		this.bundleMappingBuilder = bundleMappingBuilder;
	}

	public BundleMappingBuilder getBundleMappingBuilder() {
		return bundleMappingBuilder;
	}
}
