package name.pathfinder.azalea.mvc.struts2.internal;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import name.pathfinder.azalea.mvc.struts2.AzaleaContants;
import name.pathfinder.azalea.mvc.struts2.BundleMappingBuilder;
import name.pathfinder.azalea.mvc.struts2.OsgiClassLoader;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.config.ConfigurationProvider;
import com.opensymphony.xwork2.config.PackageProvider;
import com.opensymphony.xwork2.config.entities.PackageConfig;
import com.opensymphony.xwork2.config.impl.DefaultConfiguration;
import com.opensymphony.xwork2.config.providers.XmlConfigurationProvider;
import com.opensymphony.xwork2.inject.ContainerBuilder;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.finder.ClassLoaderInterface;
import com.opensymphony.xwork2.util.location.LocatableProperties;

public class OsgiBundleConfigurationProvider implements ConfigurationProvider {

	private boolean bundlesChanged = false;

	private Configuration configuration;

	private static final Logger logger = LoggerFactory.getLogger(OsgiBundleConfigurationProvider.class);

	private BundleContext bundleContext;

	private ObjectFactory objectFactory;
	
	private BundleMappingBuilder bundleMappingBuilder;
	
	private String pluginFilename;
	
//	private final Map<String, Bundle> bundleActionMapping = new HashMap<String, Bundle>();
	private final Map<Bundle, Configuration> bundleConfigMapping = new HashMap<Bundle, Configuration>();
	private final Map<Bundle, PackageConfig> bundlePackageConfigMapping = new HashMap<Bundle, PackageConfig>();
	
	private Set<Long> bundleIds = new HashSet<Long>();

	private BundleTracker bundleTracker;

	@Inject
	public void setObjectFactory(ObjectFactory factory) {
		this.objectFactory = factory;
	}
	
	@Inject
	public void setPluginFilename(@Inject(value=AzaleaContants.PLUGIN_FILENAME) String filename) {
		if(null == filename) {
			this.pluginFilename = AzaleaContants.DEBUG_PLUGIN_FILENAME;
		} else
			this.pluginFilename = filename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.xwork2.config.PackageProvider#init(com.opensymphony.
	 * xwork2.config.Configuration)
	 */
	@Override
	public void init(Configuration configuration) throws ConfigurationException {
		this.configuration = configuration;

		Bundle currentBundle = FrameworkUtil.getBundle(getClass());
		bundleContext = currentBundle.getBundleContext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.xwork2.config.PackageProvider#needsReload()
	 */
	@Override
	public boolean needsReload() {
		return bundlesChanged;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.xwork2.config.PackageProvider#loadPackages()
	 */
	@Override
	public synchronized void loadPackages() throws ConfigurationException {
		logger.debug("Loading packages from XML and Convention on startup");

		// init action contect
		ActionContext ctx = ActionContext.getContext();
		if (ctx == null) {
			ctx = new ActionContext(new HashMap());
			ActionContext.setContext(ctx);
		}

//		Set<Long> bundleIds = new HashSet<Long>();
		for (Bundle bundle : bundleContext.getBundles()) {
			if (isPlugin(bundle) && !bundleIds.contains(bundle.getBundleId())) {
				bundleIds.add(bundle.getBundleId());
				loadConfigFromBundle(bundle);
			}
		}
		bundlesChanged = false;
		final ClassLoader runtimeClassLoader = getClass().getClassLoader();
		bundleTracker = new BundleTracker(bundleContext, Bundle.ACTIVE, new PluginBundleTracker(runtimeClassLoader));
		bundleTracker.open();
//		this.bundleContext.addBundleListener(new BundleListener() {
//
//			@Override
//			public void bundleChanged(BundleEvent event) {
//				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//				try {
//					Thread.currentThread().setContextClassLoader(runtimeClassLoader);
//					Bundle bundle = event.getBundle();
//					int eventType = event.getType();
//					switch (eventType) {
//					case BundleEvent.STARTED:
//						logger.info("starting bundle: {}", bundle);
//						if(isPlugin(bundle)) {
//							logger.info("loading config from bundle: {}", bundle);
//							loadConfigFromBundle(bundle);
//						}
//						break;
//					case BundleEvent.STOPPED:
//						logger.info("stoping bundle: {}", bundle);
//						if(isPlugin(bundle)) {
//							Set<String> pkgs = bundleMappingBuilder.getBundlePackageConfigs(bundle);
//							if(null != pkgs) {
//								for(String pkg : pkgs) {
//									configuration.removePackageConfig(pkg);
//								}
//								
//							}
//							bundleMappingBuilder.removePackageMapping(bundle);
//						}
//					}
//				} catch(Exception e) {
//					logger.error(e.getMessage(), e);
//				}finally {
//					Thread.currentThread().setContextClassLoader(classLoader);
//				}
//			}
//		});
	}
	
	protected boolean isPlugin(Bundle bundle) {
		return null != bundle.getResource(pluginFilename);
	}

	/**
	 * 从bundle加载struts configuration。
	 * 
	 * @param bundle
	 */
	protected synchronized void loadConfigFromBundle(Bundle bundle) {
		logger.debug("Loading packages from bundle {}-{}", bundle.getSymbolicName(), bundle.getVersion());
		Map<String, PackageConfig> pkgConfigs = configuration.getPackageConfigs();

		// init action context
		ActionContext ctx = ActionContext.getContext();
		if(null == ctx) {
			ctx = new ActionContext(new HashMap());
			ActionContext.setContext(ctx);
		}
		ClassLoaderInterface classLoader = OsgiClassLoader.getCurrentClassLoaderInterface();
		ctx.put(ClassLoaderInterface.CLASS_LOADER_INTERFACE, OsgiClassLoader.getClassLoaderInterface(bundle));
		ctx.put(AzaleaContants.CURRENT_BUNDLE_KEY, bundle);
		try {
			// load struts.xml from bundle
			Configuration config = new DefaultConfiguration(this.pluginFilename);
			BundleConfigurationProvider prov = new BundleConfigurationProvider(this.pluginFilename, bundle, bundleContext);
			for (PackageConfig pkg : pkgConfigs.values()) {
				config.addPackageConfig(pkg.getName(), pkg);
			}
			prov.setObjectFactory(objectFactory);
			prov.init(config);
			prov.loadPackages();

			List<PackageConfig> list = new ArrayList<PackageConfig>(config.getPackageConfigs().values());
			list.removeAll(pkgConfigs.values());

			// 记录package name 和 bundle 的关系
			// Map<String, Long> packageBundles = new HashMap<String, Long>();
			for (PackageConfig pkg : list) {
				configuration.addPackageConfig(pkg.getName(), pkg);
				bundleMappingBuilder.addPackageMapping(pkg, bundle);
			}
			
			
			// Convention
			// get the existing packages before reloading the provider (so we can figure out what are the new packages)
			Set<String> packagesBeforeLoading = new HashSet<String>(configuration.getPackageConfigNames());

			PackageProvider conventionPackageProvider = configuration.getContainer().getInstance(PackageProvider.class, "convention.packageProvider");
			if (conventionPackageProvider != null) {
				logger.trace("Loading Convention config from bundle {}-{}", bundle.getSymbolicName(), bundle.getVersion());
				conventionPackageProvider.loadPackages();
			}

			Set<String> packagesAfterLoading = new HashSet<String>(configuration.getPackageConfigNames());
			packagesAfterLoading.removeAll(packagesBeforeLoading);
			
			for (String name : packagesAfterLoading) {
				PackageConfig pkg = configuration.getPackageConfig(name);
				bundleMappingBuilder.addPackageMapping(pkg, bundle);
			}

			if (this.configuration.getRuntimeConfiguration() != null) {
				// if there is a runtime config, it means that this method was
				// called froma bundle start event
				// instead of the initial load, in that case, reload the config
				this.configuration.rebuildRuntimeConfiguration();
			}

			bundleConfigMapping.put(bundle, config);
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			ctx.put(ClassLoaderInterface.CLASS_LOADER_INTERFACE, classLoader);
			ctx.put(AzaleaContants.CURRENT_BUNDLE_KEY, null);
		}

	}

	static class BundleConfigurationProvider extends XmlConfigurationProvider {
		private Bundle bundle;
		private BundleContext bundleContext;

		public BundleConfigurationProvider(String filename, Bundle bundle, BundleContext bundleContext) {
			super(filename, false);
			this.bundle = bundle;
			this.bundleContext = bundleContext;
			Map<String, String> dtdMappings = new HashMap<String, String>(getDtdMappings());
			dtdMappings.put("-//Apache Software Foundation//DTD Struts Configuration 2.0//EN",
					"struts-2.0.dtd");
			dtdMappings.put("-//Apache Software Foundation//DTD Struts Configuration 2.1//EN",
					"struts-2.1.dtd");
			dtdMappings.put("-//Apache Software Foundation//DTD Struts Configuration 2.1.7//EN",
					"struts-2.1.7.dtd");
			setDtdMappings(dtdMappings); // close the xml validate.
		}

		public BundleConfigurationProvider(String filename) {
			super(filename);
		}

		@Override
		protected Iterator<URL> getConfigurationUrls(String fileName) throws IOException {
			Enumeration<URL> e = bundle.getResources(fileName);
			return (null != e && e.hasMoreElements()) ? new EnumeratorIterator<URL>(e) : null;
		}
	}

	static class EnumeratorIterator<E> implements Iterator<E> {
		Enumeration<E> e = null;

		public EnumeratorIterator(Enumeration<E> e) {
			this.e = e;
		}

		public boolean hasNext() {
			return e.hasMoreElements();
		}

		public E next() {
			return e.nextElement();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void destroy() {
		if(null != bundleTracker) bundleTracker.close();
//		JSPRuntime jspRuntime = this.configuration.getContainer().getInstance(JSPRuntime.class, "orchid.jspRuntime");
//		jspRuntime.clearCache();
	}

	@Override
	public void register(ContainerBuilder builder, LocatableProperties props) throws ConfigurationException {
	}

	@Inject
	public void setBundleMappingBuilder(BundleMappingBuilder bundleMappingBuilder) {
		this.bundleMappingBuilder = bundleMappingBuilder;
	}

	public BundleMappingBuilder getBundleMappingBuilder() {
		return bundleMappingBuilder;
	}
	
	class PluginBundleTracker implements BundleTrackerCustomizer {
		
		private ClassLoader classLoader;

		public PluginBundleTracker(ClassLoader parentClassLoader) {
			this.classLoader = parentClassLoader;
		}
		@Override
		public Object addingBundle(Bundle bundle, BundleEvent event) {
			if(isPlugin(bundle)) {
				logger.info("adding config from bundle: {}", bundle);
				ClassLoader old = Thread.currentThread().getContextClassLoader();
				try {
					if(!bundleIds.contains(bundle.getBundleId())) {
						Thread.currentThread().setContextClassLoader(classLoader);
						loadConfigFromBundle(bundle);
						return bundle;
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					Thread.currentThread().setContextClassLoader(old);
				}
			}
			return null;
		}

		@Override
		public void modifiedBundle(Bundle bundle, BundleEvent event,
				Object object) {
			logger.info("modified config from bundle: {}", bundle);
		}

		@Override
		public void removedBundle(Bundle bundle, BundleEvent event,
				Object object) {
			logger.info("removed config from bundle: {}", bundle);
			bundleIds.remove(bundle.getBundleId());
			Set<String> pkgs = bundleMappingBuilder.getBundlePackageConfigs(bundle);
			if(null != pkgs) {
				for(String pkg : pkgs) {
					configuration.removePackageConfig(pkg);
				}
				
			}
			bundleMappingBuilder.removePackageMapping(bundle);
		}
		
	}

}