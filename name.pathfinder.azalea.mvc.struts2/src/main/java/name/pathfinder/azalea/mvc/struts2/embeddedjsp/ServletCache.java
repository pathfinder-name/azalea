/**
 * 
 */
package name.pathfinder.azalea.mvc.struts2.embeddedjsp;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.util.finder.ClassLoaderInterface;

/**
 * @author yaowei
 *
 */
public class ServletCache implements Runnable {
	protected final ConcurrentMap<String, Future<JspServletWrapper>> cache = new ConcurrentHashMap<String, Future<JspServletWrapper>>();
	private ServletContext servletContext;
	

	public ServletCache(ServletContext servletContext) {
		this.servletContext = servletContext;

		executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.scheduleAtFixedRate(this, 0, 4, TimeUnit.SECONDS);
		
	}

	public void clear() {
		executorService.shutdown();
		cache.clear();
	}
	
	private static final Logger logger = LoggerFactory.getLogger(ServletCache.class);
	private ScheduledExecutorService executorService;

	public JspServletWrapper get(final String location, final ClassLoaderInterface classLoaderInterface) throws InterruptedException {
		final ServletCache servletCache = this;
		while (true) {
			Future<JspServletWrapper> future = cache.get(location);
			if (future == null) {
				Callable<JspServletWrapper> loadJSPCallable = new Callable<JspServletWrapper>() {
					public JspServletWrapper call() throws Exception {
						JspServletWrapper wrapper = new JspServletWrapper(servletContext, location, classLoaderInterface, servletCache);
						wrapper.compile();
						return wrapper;
					}
				};
				FutureTask<JspServletWrapper> futureTask = new FutureTask<JspServletWrapper>(loadJSPCallable);
				future = cache.putIfAbsent(location, futureTask);
				if (future == null) {
					future = futureTask;
					futureTask.run();
				}
			}
			try {
				return future.get();
			} catch (CancellationException e) {
				cache.remove(location, future);
			} catch (ExecutionException e) {
				cache.remove(location, future);
				throw launderThrowable(e.getCause());
			}
		}
	}
	
	public void remove(String jspUri) {
		this.cache.remove(jspUri);
	}

	public static RuntimeException launderThrowable(Throwable t) {
		if (t instanceof RuntimeException)
			return (RuntimeException) t;
		else if (t instanceof Error)
			throw (Error) t;
		else
			throw new IllegalStateException(t);
	}

	@Override
	public void run() {
		Iterator<Future<JspServletWrapper>> iter = cache.values().iterator();
		while(iter.hasNext()) {
			Future<JspServletWrapper> future = iter.next();
			JspServletWrapper wrapper = null;
			try {
				wrapper = future.get();
				wrapper.compile();
			} catch (InterruptedException e) {
			}
			catch (ExecutionException e) {
				if(null != wrapper) cache.remove(wrapper.getJspUri(), future);
				throw launderThrowable(e.getCause());
			}
		}
	}

}
