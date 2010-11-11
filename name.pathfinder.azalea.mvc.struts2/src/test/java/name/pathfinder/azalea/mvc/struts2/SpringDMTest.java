package name.pathfinder.azalea.mvc.struts2;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;
import org.springframework.osgi.test.platform.Platforms;
import org.springframework.osgi.util.OsgiStringUtils;

public class SpringDMTest extends AbstractConfigurableBundleCreatorTests {

//	@Override
//	protected String[] getTestBundlesNames() {
//		return new String[] {
//				"org.apache.felix,org.apache.felix.fileinstall,3.0.2",
//				"org.apache.aries.blueprint,org.apache.aries.blueprint,0.2-incubating",
//
//				// spring dependencies
//				"org.aopalliance,com.springsource.org.aopalliance,1.0.0",
//				"org.springframework,org.springframework.aop,3.0.3.RELEASE" };
//	}

	public void testOsgiPlatformStarts() throws Exception {
		System.out.println(bundleContext
				.getProperty(Constants.FRAMEWORK_VENDOR));
		System.out.println(bundleContext
				.getProperty(Constants.FRAMEWORK_VERSION));
		System.out.println(bundleContext
				.getProperty(Constants.FRAMEWORK_EXECUTIONENVIRONMENT));
	}

	public void testOsgiEnvironment() throws Exception {
		Bundle[] bundles = bundleContext.getBundles();
		for (int i = 0; i < bundles.length; i++) {
			System.out.print(OsgiStringUtils.nullSafeName(bundles[i]));
			System.out.print(", ");
		}
		System.out.println();
	}

//	protected String getPlatformName() {
//		return Platforms.FELIX;
//	}
}
