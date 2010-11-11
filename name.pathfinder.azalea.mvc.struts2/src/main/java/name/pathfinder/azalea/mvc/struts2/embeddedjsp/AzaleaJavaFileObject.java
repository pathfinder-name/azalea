/**
 * 
 */
package name.pathfinder.azalea.mvc.struts2.embeddedjsp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

/**
 * @author yaowei
 *
 */
public class AzaleaJavaFileObject extends SimpleJavaFileObject {
	
	private String filename;
	
	public AzaleaJavaFileObject(String name, JavaFileObject.Kind kind) {
        super(toURI(name), kind);
        String output = System.getProperty("azalea.jsp.output");
        if(null == output) output = System.getProperty("java.io.tmpdir")+ "/azalea/jspout";
        File file = new File(output);
        if(!file.exists()) file.mkdirs();
        this.filename = output + "/" + name;
    }
	
    private static URI toURI(String name) {
        try {
            return new URI(name);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see javax.tools.SimpleJavaFileObject#openOutputStream()
     */
    @Override
    public OutputStream openOutputStream() throws IOException {
    	return new FileOutputStream(filename);
    }
    
    /*
     * (non-Javadoc)
     * @see javax.tools.SimpleJavaFileObject#openInputStream()
     */
    @Override
    public InputStream openInputStream() throws IOException {
    	return new FileInputStream(filename);
    }

}
