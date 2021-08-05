package nl.haarlem.translations.zdstozgw.debug;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.webcontainer.WebContainerServlet;
import nl.nn.testtool.echo2.Echo2Application;

/**
 * @author Jaco de Groot
 */
public class DebugServlet extends WebContainerServlet {
	private static final long serialVersionUID = 1L;
	private WebApplicationContext webApplicationContext;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletConfig.getServletContext());
	}

	@Override
	public ApplicationInstance newApplicationInstance() {
		return (Echo2Application)webApplicationContext.getBean("echo2Application");
	}

}
