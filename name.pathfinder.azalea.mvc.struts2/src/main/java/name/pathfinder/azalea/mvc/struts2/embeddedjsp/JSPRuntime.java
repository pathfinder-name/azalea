package name.pathfinder.azalea.mvc.struts2.embeddedjsp;

import com.opensymphony.xwork2.util.finder.ClassLoaderInterface;

public interface JSPRuntime {

	public void clearCache() ;
	public void handle(String location) throws Exception;
	
	public void handle(String location, ClassLoaderInterface classLoaderInterface) throws Exception ;

	public void handle(String location, boolean flush, ClassLoaderInterface classLoaderInterface) throws Exception;
}
