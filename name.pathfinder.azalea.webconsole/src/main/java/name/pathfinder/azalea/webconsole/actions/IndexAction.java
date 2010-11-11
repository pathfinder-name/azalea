/**
 * 
 */
package name.pathfinder.azalea.webconsole.actions;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsConstants;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.dispatcher.DefaultActionSupport;
import org.apache.struts2.interceptor.RequestAware;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.stereotype.Controller;

import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.inject.Inject;

/**
 * 
 * 
 * @author yaowei
 * 
 */
@Controller
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class IndexAction extends DefaultActionSupport implements BundleContextAware, RequestAware {

	private static final long serialVersionUID = 2117751061905442815L;
	
	private BundleContext bundleContext;
	private Configuration configuration;
	
	private Map<String, Object> requestMap;

    private Set<String> actionNames;
    private Set<Properties> bundleNames;
    private String bundleName;
    private String namespace = "";
    private Set<String> namespaces;
    private String actionExtension;

	private String pluginFilename;

	@Action(value = "index", results = { @Result(name = "success", location = "index.ftl", type = "freemarker") })
	public String index() {
		this.bundleNames = getBundleNames();
		
		
		
		return SUCCESS;
	}
	
	public Set<Properties> getBundleNames() {
		Set<Properties> names = new HashSet<Properties>();
		for(Bundle bundle : bundleContext.getBundles()) {
			if(null != bundle.getResource(pluginFilename)) {
				Properties prop = new Properties();
				prop.put("symbolicName", bundle.getSymbolicName());
				prop.put("version", bundle.getVersion().getQualifier());
				prop.put("id", bundle.getBundleId());
				names.add(prop);
			}
		}
		return names;
	}
	
	public String getContextPath() {
		HttpServletRequest request = ServletActionContext.getRequest();
		return request.getContextPath();
	}
	
	public String getStaticPath() {
		HttpServletRequest request = ServletActionContext.getRequest();
		
		StringBuilder builder = new StringBuilder(request.getContextPath());
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		builder.append("/static/").append(bundle.getBundleId());
		return builder.toString();
		
	}
	
	@Inject
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
    @Inject(StrutsConstants.STRUTS_ACTION_EXTENSION)
    public void setActionExtension(String extension) {
        this.actionExtension = extension;
    }
    
    @Inject("azalea.plugin.filename")
    public void setPluginFilename(String filename) {
    	this.pluginFilename = filename;
    }

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
    
	public BundleContext getBundleContext() {
		if(null == bundleContext) {
			Bundle bundle = FrameworkUtil.getBundle(getClass());
			if(null != bundle) bundleContext = bundle.getBundleContext();
		}
		return this.bundleContext;
	}

	@Override
	public void setRequest(Map<String, Object> request) {
		this.requestMap = request;
	}

}
