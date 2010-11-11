/**
 * 
 */
package name.pathfinder.azalea.mvc.struts2.internal;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;

import org.apache.struts2.views.freemarker.FreemarkerManager;
import org.apache.struts2.views.freemarker.StrutsClassTemplateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;

/**
 * @author yaowei
 *
 */
public class AzaleaFreemarkerManager extends FreemarkerManager {
    private static final Logger logger = LoggerFactory.getLogger(AzaleaFreemarkerManager.class);

	@Override
	protected TemplateLoader createTemplateLoader(ServletContext servletContext, String templatePath) {
		TemplateLoader templatePathLoader = null;

		try {
			if (templatePath.startsWith("class://")) {
				// substring(7) is intentional as we "reuse" the last slash
				templatePathLoader = new ClassTemplateLoader(getClass(), templatePath.substring(7));
			} else if (templatePath.startsWith("file://")) {
				templatePathLoader = new FileTemplateLoader(new File(templatePath));
			}
		} catch (IOException e) {
			logger.error("Invalid template path specified: " + e.getMessage(), e);
		}

		// presume that most apps will require the class and webapp template loader
		// if people wish to
		return templatePathLoader != null ? new MultiTemplateLoader(new TemplateLoader[] {
				templatePathLoader, new WebappTemplateLoader(servletContext),
				new StrutsClassTemplateLoader(), new AzaleaFreemarkerTemplateLoader() })
				: new MultiTemplateLoader(new TemplateLoader[] { new WebappTemplateLoader(servletContext),
						new StrutsClassTemplateLoader(), new AzaleaFreemarkerTemplateLoader() });
	}
}
