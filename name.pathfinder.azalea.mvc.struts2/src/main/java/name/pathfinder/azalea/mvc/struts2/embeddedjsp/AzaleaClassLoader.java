/**
 * 
 */
package name.pathfinder.azalea.mvc.struts2.embeddedjsp;

import org.apache.struts2.compiler.MemoryJavaFileObject;

/**
 * @author yaowei
 *
 */
public class AzaleaClassLoader extends ClassLoader {
	
	private MemoryJavaFileObject memoryJavaFileObject;
	
	public AzaleaClassLoader() {
        //without this, the tests will not run, because the tests are loaded by a custom classloader
        //so the classes referenced from the compiled code will not be found by the System Class Loader because
        //the target dir is not part of the classpath used when calling the jvm to execute the tests
        super(Thread.currentThread().getContextClassLoader());
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		if (memoryJavaFileObject != null) {
            byte[] bytes = memoryJavaFileObject.toByteArray();
            return defineClass(name, bytes, 0, bytes.length);
        }
        return super.findClass(name);
	}

	public void setMemoryJavaFileObject(MemoryJavaFileObject memoryJavaFileObject) {
		this.memoryJavaFileObject = memoryJavaFileObject;
	}

}
