package name.pathfinder.azalea.mvc.struts2;

import java.util.Set;

import org.osgi.framework.Bundle;

import com.opensymphony.xwork2.config.entities.PackageConfig;

public interface BundleMappingBuilder {
	
	void addPackageMapping(PackageConfig pkg, Bundle bundle);
	
	Bundle getBundleByActionName(String actionName);
	
	void removePackageMapping(Bundle bundle);
	
	Set<String> getBundlePackageConfigs(Bundle bundle);
	
	Set<Bundle> getPluginBundles();
	
	/**
	 * 从 ActionContext 获取当前Bundle。
	 * 查找顺序：
	 * <ul>
	 * 	<li>如果 ActionContext 中存在 @AzaleaContants.CURRENT_BUNDLE_KEY 的对象，则直接返回</li>
	 *  <li>从当前 ActionInvocation</li>
	 * </ul>
	 * 
	 * @return
	 */
	Bundle getCurrentBundle();
	
}
