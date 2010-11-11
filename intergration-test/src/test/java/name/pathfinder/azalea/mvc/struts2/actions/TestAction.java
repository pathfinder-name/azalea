/**
 * 
 */
package name.pathfinder.azalea.mvc.struts2.actions;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.dispatcher.DefaultActionSupport;

/**
 * @author yaowei
 *
 */
@Namespace("/test")
public class TestAction extends DefaultActionSupport {
	
	@Action(value="index")
	public String index() {
		return SUCCESS;
	}

}
