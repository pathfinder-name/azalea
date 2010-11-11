/**
 * 
 */
package name.pathfinder.azalea.mvc.struts2;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;
import static org.ops4j.pax.swissbox.tinybundles.core.TinyBundles.*;

import java.io.File;
import java.io.InputStream;

import name.pathfinder.azalea.mvc.struts2.actions.TestAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Customizer;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

/**
 * @author yaowei
 * 
 */
@RunWith(JUnit4TestRunner.class)
public class OsgiBundleConfigurationProviderPaxExamTest {

	@Inject
	private BundleContext bundleContext = null;

	// you get that because you "installed" a log profile in configuration.
	public Log logger = LogFactory
			.getLog(OsgiBundleConfigurationProviderPaxExamTest.class);

	/*
	 * You can configure all kinds of stuff. You will learn about most of it on
	 * the project wiki. Here's a typical example: - add a log service to your
	 * runtime - add custom bundles via the mvn handler - add additional, non
	 * bundlized dependencies. (wrapping on the fly)
	 */
	@Configuration
	public static Option[] configure() {
		return options(
				when( Boolean.getBoolean( "isDebugEnabled" ) ).useOptions(
			            vmOption( "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005" ),
			            // this is necessary to let junit runner not timout the remote process before attaching debugger
			            // setting timeout to 0 means wait as long as the remote service comes available.
			            // starting with version 0.5.0 of PAx Exam this is no longer required as by default the framework tests
			            // will not be triggered till the framework is not started
			            waitForFrameworkStartup()
			        ),
			        
				felix().version("3.0.2"),
		// install log service using pax runners profile abstraction (there are
		// more profiles, like DS)
				logProfile(),
				// this is how you set the default log level when using pax
				// logging (logProfile)
				systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level")
						.value("INFO"),
						
				// karaf dependencies
				mavenBundle("org.apache.felix", "org.apache.felix.fileinstall", "3.0.2"),
				//mavenBundle("org.osgi", "org.osgi.enterprise", "4.2.0"),
				
				mavenBundle("org.apache.aries.blueprint","org.apache.aries.blueprint","0.2-incubating"),

				// spring dependencies
				bundle("mvn:org.aopalliance/com.springsource.org.aopalliance/1.0.0"),
				bundle("mvn:org.springframework/org.springframework.aop/3.0.3.RELEASE"),
				bundle("mvn:org.springframework/org.springframework.asm/3.0.3.RELEASE"),
				bundle("mvn:org.springframework/org.springframework.beans/3.0.3.RELEASE"),
				bundle("mvn:org.springframework/org.springframework.context/3.0.3.RELEASE"),
				bundle("mvn:org.springframework/org.springframework.context.support/3.0.3.RELEASE"),
				bundle("mvn:org.springframework/org.springframework.core/3.0.3.RELEASE"),
				bundle("mvn:org.springframework/org.springframework.expression/3.0.3.RELEASE"),
				bundle("mvn:org.springframework/org.springframework.jdbc/3.0.3.RELEASE"),
				bundle("mvn:javax.jms/com.springsource.javax.jms/1.1.0"),
				bundle("mvn:org.springframework/org.springframework.jms/3.0.3.RELEASE"),
				bundle("mvn:org.springframework/org.springframework.orm/3.0.3.RELEASE"),
				bundle("mvn:org.springframework/org.springframework.oxm/3.0.3.RELEASE"),
				bundle("mvn:javax.transaction/com.springsource.javax.transaction/1.1.0"),
				bundle("mvn:org.springframework/org.springframework.transaction/3.0.3.RELEASE"),
				bundle("mvn:javax.portlet/com.springsource.javax.portlet/2.0.0"),
				bundle("mvn:javax.servlet/com.springsource.javax.servlet/2.5.0"),
				bundle("mvn:org.springframework/org.springframework.web/3.0.3.RELEASE"),
				bundle("mvn:org.springframework/org.springframework.web.portlet/3.0.3.RELEASE"),
				bundle("mvn:org.springframework/org.springframework.web.servlet/3.0.3.RELEASE"),
						
				// spring-dm
				bundle("mvn:net.sourceforge.cglib/com.springsource.net.sf.cglib/2.2.0"),
				bundle("mvn:org.springframework.osgi/spring-osgi-io/1.2.1"),
				bundle("mvn:org.springframework.osgi/spring-osgi-core/1.2.1"),
				bundle("mvn:org.springframework.osgi/spring-osgi-extender/1.2.1"),
				bundle("mvn:org.springframework.osgi/spring-osgi-annotation/1.2.1"),
				bundle("mvn:org.apache.karaf.deployer/org.apache.karaf.deployer.spring/2.1.0"),

						
				//azalea-war
				bundle("mvn:javax.el/com.springsource.javax.el/1.0.0"),
				bundle("mvn:javax.servlet/com.springsource.javax.servlet/2.5.0"),
				bundle("mvn:javax.servlet/com.springsource.javax.servlet.jsp/2.1.0"),
				bundle("mvn:org.ops4j.pax.web/pax-web-jetty-bundle/0.8.0"),
				bundle("mvn:org.ops4j.pax.web/pax-web-jsp/0.8.0"),
				bundle("mvn:org.ops4j.pax.web/pax-web-extender-war/0.8.0"),
				bundle("mvn:org.ops4j.pax.web/pax-web-extender-whiteboard/0.8.0"),
				bundle("mvn:org.ops4j.pax.url/pax-url-war/1.2.0"),
				bundle("mvn:org.apache.karaf.deployer/org.apache.karaf.deployer.war/2.1.0"),
//				mavenBundle("javax.el", "com.springsource.javax.el","1.0.0"),
						
				//azalea-mvc-base
				mavenBundle("org.jboss.javassist", "com.springsource.javassist","3.9.0.GA"),
				mavenBundle("org.apache.commons", "com.springsource.org.apache.commons.fileupload","1.2.1"),
				mavenBundle("org.apache.struts", "name.pathfinder.azalea.struts2","2.2.1"),
				mavenBundle("org.apache.struts", "name.pathfinder.azalea.struts2-spring-plugin","2.2.1"),
				mavenBundle("org.apache.struts", "name.pathfinder.azalea.struts2-convention-plugin","2.2.1"),
				mavenBundle("org.apache.struts", "name.pathfinder.azalea.struts2-embeddedjsp-plugin","2.2.1"),
				mavenBundle("org.ognl", "name.pathfinder.azalea.ognl","3.0"),
				mavenBundle("org.freemarker", "name.pathfinder.azalea.freemarker","2.3.16"),
				

				mavenBundle("name.pathfinder.azalea", "name.pathfinder.azalea.mvc.struts2","0.1.0.SNAPSHOT").type("war"),
				
				
				bundle("mvn:org.ops4j.base/ops4j-base-lang/1.2.2"),
				bundle("mvn:org.ops4j.base/ops4j-base-monitors/1.2.2"),
				bundle("mvn:org.ops4j.base/ops4j-base-io/1.2.2"),
//				wrappedBundle("mvn:org.ops4j.pax.swissbox/pax")
				bundle("mvn:org.ops4j.pax.swissbox/pax-swissbox-tinybundles/1.3.0"),
				mavenBundle("name.pathfinder.azalea", "name.pathfinder.azalea.webconsole","0.1.0.SNAPSHOT"),
				
//				provision(newBundle()
//						.add(TestAction.class)
//						.add("azalea-plugin.xml",
//								OsgiBundleConfigurationProviderPaxExamTest.class .getResource("actions/azalea-plugin.xml"))
//						.set(Constants.BUNDLE_SYMBOLICNAME, "azalea.mvc.struts2.test")
//						.set(Constants.BUNDLE_VERSION, "0.1")
//						.set(Constants.EXPORT_PACKAGE, "name.pathfinder.azalea.mvc.struts2.actions")
//						.set(Constants.IMPORT_PACKAGE, "org.apache.struts2.convention.annotation;version=\"[2.2,2.3)\",org.apache.struts2.dispatcher;version=\"[2.2,2.3)\",org.slf4j;version=\"1.5\"")
//						.set(Constants.DYNAMICIMPORT_PACKAGE, "*")
//						.build()),
				
				new Customizer() {
					@Override
					public void customizeEnvironment(File workingFolder) {
					}
				});
	}
//
//	/**
//	 * You will get a list of bundles installed by default plus your testcase,
//	 * wrapped into a bundle called pax-exam-probe
//	 * @throws BundleException 
//	 */
//	@Test
//	public void listBundles() throws BundleException {
//		for (Bundle b : bundleContext.getBundles()) {
//			System.out.println("Bundle " + b.getBundleId() + " : "
//					+ b.getSymbolicName());
//		}
//
//	}
	
	@Test
	public void testDynaLoadBundle() throws BundleException {
		for (Bundle b : bundleContext.getBundles()) {
			System.out.println("Bundle " + b.getBundleId() + " : "
					+ b.getSymbolicName());
		}
//		InputStream is = newBundle()
//				.add(TestAction.class)
//				.add("azalea-plugin.xml",OsgiBundleConfigurationProviderPaxExamTest.class.getResource("actions/azalea-plugin.xml"))
//				.set(Constants.BUNDLE_SYMBOLICNAME, "azalea.mvc.struts2.test")
//				.set(Constants.BUNDLE_VERSION, "0.1")
//				.set(Constants.EXPORT_PACKAGE, "name.pathfinder.azalea.mvc.struts2.actions")
//				.set(Constants.IMPORT_PACKAGE, "org.apache.struts2.convention.annotation;version=\"[2.2,2.3)\",org.apache.struts2.dispatcher;version=\"[2.2,2.3)\",org.slf4j;version=\"1.5\",org.ops4j.pax.swissbox.tinybundles.core;version=\"1.3\"")
//				.set(Constants.DYNAMICIMPORT_PACKAGE, "*")
//				.build();
//		bundleContext.installBundle("azalea.mvc.struts2.test", is);

	}

}
