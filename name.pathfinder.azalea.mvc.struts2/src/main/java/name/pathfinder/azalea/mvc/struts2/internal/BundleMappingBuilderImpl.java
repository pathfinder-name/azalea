/**
 * 
 */
package name.pathfinder.azalea.mvc.struts2.internal;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

import name.pathfinder.azalea.mvc.struts2.AzaleaContants;
import name.pathfinder.azalea.mvc.struts2.BundleMappingBuilder;

import org.osgi.framework.Bundle;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.config.entities.PackageConfig;


/**
 * @author yaowei
 *
 */
public class BundleMappingBuilderImpl implements BundleMappingBuilder {
	
	private ConcurrentMap<String, Bundle> packageMapping = new ConcurrentHashMap<String, Bundle>();
	private ConcurrentMap<Bundle, Set<String>> bundlePackageMapping = new ConcurrentHashMap<Bundle, Set<String>>();
	private ConcurrentMap<String, Bundle> actionMapping = new ConcurrentHashMap<String, Bundle>();
	private ConcurrentMap<Bundle, Set<String>> bundleActionMapping = new ConcurrentHashMap<Bundle, Set<String>>();
//	private 
	/*
	 * (non-Javadoc)
	 * @see name.pathfinder.azalea.mvc.struts2.internal.BundleMappingBuilder#addPackageMapping(com.opensymphony.xwork2.config.entities.PackageConfig, org.osgi.framework.Bundle)
	 */
	@Override
	public void addPackageMapping(PackageConfig pkg, Bundle bundle) {
		this.packageMapping.putIfAbsent(pkg.getName(), bundle);
		Set<String> bundlePackages = bundlePackageMapping.get(bundle);
		if(null == bundlePackages) {
			bundlePackages = new ConcurrentSkipListSet<String>();
			Set<String> old = bundlePackageMapping.putIfAbsent(bundle, bundlePackages);
			if(null != old) bundlePackages = old;
		}
		bundlePackages.add(pkg.getName());
		
		Set<String> bundleActions = bundleActionMapping.get(bundle);
		if(null == bundleActions) {
			bundleActions = new ConcurrentSkipListSet<String>();
			Set<String> old = bundleActionMapping.putIfAbsent(bundle, bundleActions);
			if(null != old) bundleActions = old;
		}
		for(String actionConfigName : pkg.getActionConfigs().keySet()) {
			actionMapping.putIfAbsent(actionConfigName, bundle);
			bundleActions.add(actionConfigName);
		}
	}

	@Override
	public Bundle getBundleByActionName(String actionName) {
		return actionMapping.get(actionName);
	}

	@Override
	public void removePackageMapping(Bundle bundle) {
		Set<String> packages = bundlePackageMapping.remove(bundle);
		if(null != packages) {
			for(String pkg : packages) {
				packageMapping.remove(pkg);
			}
			packages.clear();
		}
		
		Set<String> actions = bundleActionMapping.remove(bundle);
		if(null != actions) {
			for(String actionConfigName : actions ) {
				actionMapping.remove(actionConfigName);
			}
			actions.clear();
		}
	}

	@Override
	public Bundle getCurrentBundle() {
		ActionContext ctx = ActionContext.getContext();
		if (null == ctx)
			return null;
		Bundle bundle = (Bundle) ctx.get(AzaleaContants.CURRENT_BUNDLE_KEY);
		if (bundle == null) {
			ActionInvocation inv = ctx.getActionInvocation();
			if (null == inv)
				return null;
			ActionProxy proxy = inv.getProxy();
			bundle = getBundleByActionName(proxy.getActionName());
		}
		return bundle;
	}

	@Override
	public Set<String> getBundlePackageConfigs(Bundle bundle) {
		return bundlePackageMapping.get(bundle);
	}

	@Override
	public Set<Bundle> getPluginBundles() {
		return bundlePackageMapping.keySet();
	}
	
}
