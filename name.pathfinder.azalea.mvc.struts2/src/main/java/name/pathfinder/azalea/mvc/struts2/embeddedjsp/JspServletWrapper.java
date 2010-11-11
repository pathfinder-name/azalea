/**
 * 
 */
package name.pathfinder.azalea.mvc.struts2.embeddedjsp;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.jsp.JspPage;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.xwork.ObjectUtils;
import org.apache.commons.lang.xwork.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsException;
import org.apache.struts2.compiler.MemoryJavaFileObject;
import org.apache.struts2.jasper.JasperException;
import org.apache.struts2.jasper.JspC;
import org.apache.struts2.jasper.servlet.JspServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.util.URLUtil;
import com.opensymphony.xwork2.util.finder.ClassLoaderInterface;

/**
 * @author yaowei
 *
 */
public class JspServletWrapper {

	private static final String DEFAULT_PACKAGE = "org.apache.struts2.jsp";

	private static final Pattern PACKAGE_PATTERN = Pattern.compile("package (.*?);");
	private static final Pattern CLASS_PATTERN = Pattern.compile("public final class (.*?) ");

	private AzaleaClassLoader classLoader;
	private String jspUri;
	
	private Servlet servlet;
	private String className;
//    private boolean firstTime = true;
    private boolean reload = true;
//    private boolean isTagFile;
//    private int tripCount;
    private long jspLastModifiedTime;
    private long lastModificationTest = 0L;
    private ServletCache servletCache;
    
	private ClassLoaderInterface classLoaderInterface;

	private ServletContext servletContext;
    
    private static final Logger logger = LoggerFactory.getLogger(JspServletWrapper.class);
    
    public JspServletWrapper(ServletContext servletContext, String jspUri, ClassLoaderInterface classLoaderInterface, ServletCache servletCache) {
    	this.servletContext = servletContext;
    	this.jspUri = jspUri;
    	this.classLoaderInterface = classLoaderInterface;
    	this.servletCache = servletCache;
    }
    
    public void compile() {
		// use Jasper to compile the JSP into java code
		try {
			if(isOutDated()) {
				this.classLoader = null;
				JspC jspC = compileJSP(this.jspUri);
				String source = jspC.getSourceCode();

				this.className = extractClassName(source);
				
				// use Java Compiler API to compile the java code into a class
				// the tlds that were discovered are added (their jars) to the classpath
				compileJava(this.className, source, jspC);
				reload = true;
			}


		} catch (JasperException e) {
			logger.error(e.getMessage(), e);
			removeFromCache();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			removeFromCache();
		}
    }
    
    public void removeFromCache() {
    	this.servletCache.remove(this.jspUri);
    }
    
    public Servlet getServlet() throws ClassNotFoundException, IllegalAccessException, InstantiationException, ServletException {
    	if (reload) {
    		synchronized (this) {
    			if(reload) {
    				destroy();
    				
    				Servlet servlet = null;
    				
    				Class<?> clazz = Class.forName(className, false, classLoader);
    				servlet = createServlet(clazz);
    				
    				this.servlet = servlet;
    				this.reload = false;
    			}
    		}
    	}
		return this.servlet;
    }
    
    public ClassLoader getJspLoader() {
        if( classLoader == null ) {
        	classLoader = new AzaleaClassLoader();
        }
        return classLoader;
    }
    
    public void destroy() {
        if (servlet != null) {
        	servlet.destroy();
        	Object annotationProcessor = this.servletContext.getAttribute("org.apache.AnnotationProcessor");
            if (annotationProcessor != null) {
                try {
                	Method method = annotationProcessor.getClass().getMethod("preDestroy", new Class[]{javax.servlet.Servlet.class});
                	if(null != method) method.invoke(annotationProcessor, servlet);
                } catch (Exception e) {
                    // Log any exception, since it can't be passed along
                    logger.error("jsp.error.file.not.found" + e.getMessage(), e);
                    removeFromCache();
                }
            }
        }
    }

	/**
	 * Creates and inits a servlet
	 */
	private Servlet createServlet(Class<?> clazz) throws IllegalAccessException, InstantiationException,
			ServletException {
		JSPServletConfig config = new JSPServletConfig(ServletActionContext.getServletContext());

		Servlet servlet = (Servlet) clazz.newInstance();
		servlet.init(config);

		/*
		 * there is no need to call JspPage.init explicitly because Jasper's JSP base classe
		 * HttpJspBase.init(ServletConfig) calls: jspInit(); _jspInit();
		 */

		return servlet;
	}
    
	private void compileJava(String className, final String source, JspC jspC) throws IOException {
		logger.trace("Compiling {}, source: {}", className, source);
		getJspLoader();
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		
		

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		// the generated bytecode is fed to the class loader
		JavaFileManager jfm = new ForwardingJavaFileManager<StandardJavaFileManager>(
				compiler.getStandardFileManager(diagnostics, null, null)) {

			@Override
			public JavaFileObject getJavaFileForOutput(Location location, String name,
					JavaFileObject.Kind kind, FileObject sibling) throws IOException {
				MemoryJavaFileObject fileObject = new MemoryJavaFileObject(name, kind);
				classLoader.setMemoryJavaFileObject(fileObject);
				return fileObject;
			}
		};

		// read java source code from memory
		String fileName = className.replace('.', '/') + ".java";
		SimpleJavaFileObject sourceCodeObject = new SimpleJavaFileObject(toURI(fileName),
				JavaFileObject.Kind.SOURCE) {
			@Override
			public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException,
					IllegalStateException, UnsupportedOperationException {
				return source;
			}

		};

		// build classpath
		// some entries will be added multiple times, hence the set
		List<String> optionList = new ArrayList<String>();
		Set<String> classPath = new HashSet<String>();

		// find available jars
		List<URL> urls = getUrls(jspC);
		for (URL url : urls) {
			URL normalizedUrl = URLUtil.normalizeToFileProtocol(url);
			File file = FileUtils.toFile((URL) ObjectUtils.defaultIfNull(normalizedUrl, url));
			if (file.exists())
				classPath.add(file.getAbsolutePath());
		}


		// these should be in the list already, but I am feeling paranoid
		// this jar
		classPath.add(getJarUrl(EmbeddedJSPResult.class));
		// servlet api
		classPath.add(getJarUrl(Servlet.class));
		// jsp api
		classPath.add(getJarUrl(JspPage.class));
		// struts2 embeddedjsp plugin
		classPath.add(getJarUrl(JspServlet.class));

		try {
			Class<?> annotationsProcessor = Class.forName("org.apache.AnnotationProcessor");
			classPath.add(getJarUrl(annotationsProcessor));
		} catch (ClassNotFoundException e) {
			// ok ignore
		}

		// add extra classpath entries (jars where tlds were found will be here)
		Set<String> extraClassPath = jspC.getTldAbsolutePaths();
		for (Iterator<String> iterator = extraClassPath.iterator(); iterator.hasNext();) {
			String entry = iterator.next();
			classPath.add(entry);
		}

		String classPathString = StringUtils.join(classPath, File.pathSeparator);
		logger.debug("Compiling {} with classpath {}", className, classPathString);

		optionList.addAll(Arrays.asList("-classpath", classPathString));

		// compile
		JavaCompiler.CompilationTask task = compiler.getTask(null, jfm, diagnostics, optionList, null,
				Arrays.asList(sourceCodeObject));

		if (!task.call()) {
			StringBuilder message = new StringBuilder("Compilation failed:");
			for(Diagnostic<?> d : diagnostics.getDiagnostics()) {
				message.append(d.toString());
			}
			throw new StrutsException(message.toString());
		}
	}

	private String extractClassName(String source) {
		Matcher matcher = PACKAGE_PATTERN.matcher(source);
		matcher.find();
		String packageName = matcher.group(1);

		matcher = CLASS_PATTERN.matcher(source);
		matcher.find();
		String className = matcher.group(1);

		return packageName + "." + className;
	}

	private JspC compileJSP(String location) throws JasperException {
		JspC jspC = new JspC();
		jspC.setClassLoaderInterface(this.classLoaderInterface);
		jspC.setCompile(false);
		jspC.setJspFiles(location);
		jspC.setPackage(DEFAULT_PACKAGE);
		jspC.execute();
		return jspC;
	}
	
	public String getJspUri() {
		return jspUri;
	}
	
	protected String getJarUrl(Class<?> clazz) {
		ProtectionDomain protectionDomain = clazz.getProtectionDomain();
		CodeSource codeSource = protectionDomain.getCodeSource();
		URL loc = codeSource.getLocation();
		File file = FileUtils.toFile(loc);
		return file.getAbsolutePath();
	}
	

	private static URI toURI(String name) {
		try {
			return new URI(name);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
    /**
     * Determine if a compilation is necessary by checking the time stamp of the
     * JSP page with that of the corresponding .class or .java file. If the page
     * has dependencies, the check is also extended to its dependeants, and so
     * on. This method can by overidden by a subclasses of Compiler.
     * 
     * @param checkClass
     *            If true, check against .class file, if false, check against
     *            .java file.
     */
    public boolean isOutDated() {

        String jsp = this.jspUri;

        if (getModificationTestInterval() > 0) {

            if (getLastModificationTest()
                    + (getModificationTestInterval() * 1000) > System
                    .currentTimeMillis()) {
                return false;
            } else {
                setLastModificationTest(System.currentTimeMillis());
            }
        }

        long jspRealLastModified = 0;
        try {
            URL jspUrl = this.classLoaderInterface.getResource(jsp);
            if (jspUrl == null) {
//                ctxt.incrementRemoved();
//            	remove the wrapper from cache?
            	removeFromCache();
                return true;
            }
            URLConnection uc = jspUrl.openConnection();
            if (uc instanceof JarURLConnection) {
                jspRealLastModified =
                    ((JarURLConnection) uc).getJarEntry().getTime();
            } else {
                jspRealLastModified = uc.getLastModified();
            }
            uc.getInputStream().close();
        } catch (Exception e) {
            return true;
        }
        
        if(jspLastModifiedTime < jspRealLastModified){
            if (logger.isDebugEnabled()) {
                logger.debug("Compiler: outdated: {} {}", this.jspUri, jspRealLastModified);
            }
            jspLastModifiedTime = jspRealLastModified;
            return true;
        }

        return false;

    }
    
    private int modificationTestInterval = 4;
    
    /**
     * Modification test interval.
     */
    public int getModificationTestInterval(){
    	return this.modificationTestInterval;
    }
    
    public long getLastModificationTest() {
		return lastModificationTest;
	}
    
    public void setLastModificationTest(long lastModificationTest) {
		this.lastModificationTest = lastModificationTest;
	}
    
    protected List<URL> getUrls(JspC jspC) throws IOException {
        List<URL> list = new ArrayList<URL>();

        //find jars
        ArrayList<URL> urls = Collections.list(this.classLoaderInterface.getResources("META-INF"));

        for (URL url : urls) {
            url = extractURL(url);
            if(null != url)
            	list.add(url);
        }
        
		// get the tld files from JspC cache.
		// 由于无法在其他地方获取当前编译jsp所使用的tld地址，因此不得不采用这种方式。
		Set tlds = jspC.getCache().keySet();
		for(Iterator iter = tlds.iterator(); iter.hasNext(); ) {
			String tld = (String) iter.next();
			URL url = this.classLoaderInterface.getResource(tld);
            url = extractURL(url);
            if(null != url)
            	list.add(url);
		}

        //usually the "classes" dir
        list.addAll(Collections.list(this.classLoaderInterface.getResources("")));
        return list;
    }

	private URL extractURL(URL url) throws MalformedURLException {
		URL resutl = null;
		if ("jar".equalsIgnoreCase(url.getProtocol()) || "file".equalsIgnoreCase(url.getProtocol())) {
		    String externalForm = url.toExternalForm();
		    //build a URL pointing to the jar, instead of the META-INF dir
		    resutl = new URL(StringUtils.substringBefore(externalForm, "META-INF"));
		} else 
		    logger.debug("Ignoring URL {} because it is not a jar", url.toExternalForm());
		return resutl;
	}
}
