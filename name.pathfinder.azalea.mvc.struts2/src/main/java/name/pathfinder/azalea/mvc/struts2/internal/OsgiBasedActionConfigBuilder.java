/**
 * 
 */
package name.pathfinder.azalea.mvc.struts2.internal;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import name.pathfinder.azalea.mvc.struts2.BundleMappingBuilder;

import org.apache.commons.lang.xwork.StringUtils;
import org.apache.struts2.convention.ActionConfigBuilder;
import org.apache.struts2.convention.PackageBasedActionConfigBuilder;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;

/**
 * @author yaowei
 *
 */
public class OsgiBasedActionConfigBuilder extends PackageBasedActionConfigBuilder implements ActionConfigBuilder{

	private BundleMappingBuilder bundleMappingBuilder;
	private String[] actionPackages;
	private String[] packageLocators;
	private boolean disablePackageLocatorsScanning;
	private String actionSuffix = "Action";
	private boolean checkImplementsAction;
	
	private static final Logger logger = LoggerFactory.getLogger(OsgiBundleConfigurationProvider.class);
	
    @Inject
	public OsgiBasedActionConfigBuilder(Configuration configuration, Container container, ObjectFactory objectFactory,
            @Inject("struts.convention.redirect.to.slash") String redirectToSlash,
            @Inject("struts.convention.default.parent.package") String defaultParentPackage) {
		super(configuration, container, objectFactory, redirectToSlash,
				defaultParentPackage);
	}
	
    /**
     * 
     */
	@Override
	protected Set<Class> findActions() {
        Bundle bundle = this.bundleMappingBuilder.getCurrentBundle();
        if(null == bundle) {
        	return super.findActions();
        }
        Set<Class> classes = new HashSet<Class>();
        try {
            if (actionPackages != null || (packageLocators != null && !disablePackageLocatorsScanning)) {

                Enumeration<URL> entries = bundle.findEntries("", "*.class", true);
                
                while(entries.hasMoreElements()) {
                	URL entry = entries.nextElement();
                	String path = entry.getPath();
                	if(path.startsWith("/")) path = path.substring(1);
                	
                	String className = path.substring(0, path.length() - 6).replace('/', '.');
            		try {
						Class clazz = bundle.loadClass(className);
						
            			boolean inPackage = includeClassNameInActionScan(className);
            			boolean nameMatches = className.endsWith(actionSuffix);
						
	                    if( null != clazz && inPackage && (nameMatches || (checkImplementsAction && com.opensymphony.xwork2.Action.class.isAssignableFrom(clazz))) )
	                    	classes.add(clazz);
					} catch (Exception e) {
						logger.error("Unable to load class {}", className);
					}
                		
                }
            }
        } catch (Exception ex) {
                logger.error("Unable to scan named packages", ex);
        }

        return classes;
	}
	
	@Inject
	public void setBundleMappingBuilder(BundleMappingBuilder bundleMappingBuilder) {
		this.bundleMappingBuilder = bundleMappingBuilder;
	}
    /*
     * (non-Javadoc)
     * @see org.apache.struts2.convention.PackageBasedActionConfigBuilder#setActionPackages(java.lang.String)
     */
    @Inject(value = "struts.convention.action.packages", required = false)
    public void setActionPackages(String actionPackages) {
    	super.setActionPackages(actionPackages);
        if (StringUtils.isNotBlank(actionPackages)) {
            this.actionPackages = actionPackages.split("\\s*[,]\\s*");
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.struts2.convention.PackageBasedActionConfigBuilder#setPackageLocators(java.lang.String)
     */
    @Inject(value = "struts.convention.package.locators", required = false)
    public void setPackageLocators(String packageLocators) {
    	super.setPackageLocators(packageLocators);
        this.packageLocators = packageLocators.split("\\s*[,]\\s*");
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.struts2.convention.PackageBasedActionConfigBuilder#setDisablePackageLocatorsScanning(java.lang.String)
     */
    @Inject(value = "struts.convention.package.locators.disable", required = false)
    public void setDisablePackageLocatorsScanning(String disablePackageLocatorsScanning) {
    	super.setDisablePackageLocatorsScanning(disablePackageLocatorsScanning);
        this.disablePackageLocatorsScanning = "true".equals(disablePackageLocatorsScanning);
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.struts2.convention.PackageBasedActionConfigBuilder#setActionSuffix(java.lang.String)
     */
    @Inject(value = "struts.convention.action.suffix", required = false)
    public void setActionSuffix(String actionSuffix) {
    	super.setActionSuffix(actionSuffix);
        if (StringUtils.isNotBlank(actionSuffix)) {
            this.actionSuffix = actionSuffix;
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.struts2.convention.PackageBasedActionConfigBuilder#setCheckImplementsAction(java.lang.String)
     */
    @Inject(value = "struts.convention.action.checkImplementsAction", required = false)
    public void setCheckImplementsAction(String checkImplementsAction) {
    	super.setCheckImplementsAction(checkImplementsAction);
        this.checkImplementsAction = "true".equals(checkImplementsAction);
    }

}
