/**
 * 
 */
package name.pathfinder.azalea.mvc.struts2.embeddedjsp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.struts2.compiler.MemoryJavaFileObject;

/**
 * @author yaowei
 *
 */
public class AzaleaJspClassLoader extends ClassLoader {
    private Map<String, MemoryJavaFileObject> cachedObjects = new ConcurrentHashMap<String, MemoryJavaFileObject>();

    public AzaleaJspClassLoader() {
        //without this, the tests will not run, because the tests are loaded by a custom classloader
        //so the classes referenced from the compiled code will not be found by the System Class Loader because
        //the target dir is not part of the classpath used when calling the jvm to execute the tests
        super(Thread.currentThread().getContextClassLoader());
    }

    @Override
    protected Class<?> findClass(String name) throws
            ClassNotFoundException {
        MemoryJavaFileObject fileObject = cachedObjects.get(name);
        if (fileObject != null) {
            byte[] bytes = fileObject.toByteArray();
            return defineClass(name, bytes, 0, bytes.length);
        }
        return null;
    }

    public void addMemoryJavaFileObject(String jsp, MemoryJavaFileObject memoryJavaFileObject) {
        cachedObjects.put(jsp, memoryJavaFileObject);
    }
    
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
    	Class<?> clazz = findClass(name);
    	if(null != clazz) return clazz;
    	return super.loadClass(name);
    }
}
