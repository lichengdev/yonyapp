package nc.uap.portal.login.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.RequestFacade;
import org.apache.commons.lang.StringUtils;

import nc.bs.logging.Logger;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.ModelServerConfig;
import nc.uap.lfw.core.WebContext;
import nc.uap.portal.log.PortalLogger;
import pers.bc.utils.constant.IPubCons;
import pers.bc.utils.pub.PubEnvUtil;
import pers.bc.utils.yonyou.YonLogUtil;
import pers.bc.utils.yonyou.YonYouUtilbc;
import uap.lfw.core.ml.LfwResBundle;

public abstract class AbstractLfwLoginFilter implements Filter
{
    private static final String ESCAPE_URL = "ESCAPE_URL";
    protected String LOGIN_PATH;
    protected String RETURN_PARAM = "returnUrl";
    protected String ABOUT_PATH = "/lfw/core/login/about.jsp";
    private static final String PORTAL_LOGINPATH = "/app/mockapp/login.jsp";
    protected String SSO_SERVER = "127.0.0.1";
    
    public void init(FilterConfig config) throws ServletException
    {
        String ctx = config.getServletContext().getContextPath();
        ModelServerConfig modelConfig = new ModelServerConfig(ctx);
        if (modelConfig != null)
        {
            String login_path = modelConfig.getConfigValue("login_path");
            if (login_path != null)
            {
                this.LOGIN_PATH = login_path;
            }
        }
        
        if (config.getInitParameter("login_path") != null)
        {
            this.LOGIN_PATH = config.getInitParameter("login_path");
        }
        
        if (config.getInitParameter("about_path") != null)
        {
            this.ABOUT_PATH = config.getInitParameter("abount_path");
        }
        
        if (config.getInitParameter("sso_server") != null)
        {
            this.SSO_SERVER = config.getInitParameter("sso_server");
        }
        
    }
    
    protected String getLoginJspName()
    {
        return this.LOGIN_PATH == null ? LfwRuntimeEnvironment.getRootPath() + "/app/mockapp/login.jsp" : this.LOGIN_PATH;
    }
    
    private String getAboutJspName()
    {
        return this.ABOUT_PATH;
    }
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        PortalLogger.debug(LfwResBundle.getInstance().getStrByID("ds", "AbstractLfwLoginFilter-000000"));
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        WebContext webCtx = LfwRuntimeEnvironment.getWebContext();
        String currentUrl = req.getRequestURI();
        String loginURL = this.getLoginJspName();
        if (webCtx != null)
        {
            LfwRuntimeEnvironment.getWebContext().setResponse(res);
        }
        
        if (this.isToLogin(req, res))
        {
            chain.doFilter(request, response);
        }
        else if (this.isEscapeUrl(req, res))
        {
            chain.doFilter(request, response);
        }
        else if (this.isUserLogin(req, res))
        {
            if (currentUrl.startsWith(loginURL))
            {
                res.sendRedirect("/portal");
                return;
            }
            
            chain.doFilter(request, response);
        }
        else
        {
            if (currentUrl.startsWith(LfwRuntimeEnvironment.getRootPath() + "/app/mockapp/login.jsp")
                && StringUtils.isNotBlank(this.LOGIN_PATH))
            {
                res.sendRedirect(this.LOGIN_PATH);
                return;
            }
            
            if (req.getQueryString() != null)
            {
                currentUrl = currentUrl + "?" + req.getQueryString();
            }
            if (currentUrl.startsWith("/portal/pt/home/index"))
            {
                req.getSession().setAttribute("CURRENT_URL", currentUrl);
                chain.doFilter(request, response);
            }
            else
            {
                if (!this.isNotInControlUrl(currentUrl, req))
                {
                    if (req.getParameter("isAjax") != null)
                    {
                        res.setStatus(306);
                        return;
                    }
                    
                    req.getSession().setAttribute("CURRENT_URL", currentUrl);
                    String lrid = "lrid=1";
                    if (loginURL.indexOf("?") != -1)
                    {
                        loginURL = loginURL + "&" + lrid;
                    }
                    else
                    {
                        loginURL = loginURL + "?" + lrid;
                    }
                    
                    res.sendRedirect(loginURL);
                }
                else
                {
                    AAMRedirectFilter(request, response, chain);
                    // chain.doFilter(request, response);
                }
            }
        }
        
    }
    
    /**
     * *********************************************************** <br>
     * *说明： B/S ncportal 单点登录，校验统一认证平台 <br>
     *
     * @param request
     * @param response
     * @param chain
     * @throws Exception <br>
     * @void <br>
     * @methods pers.bc.utils.yonyou.BackgroundWorkTaskPlugin#runing <br>
     * @author LiBencheng <br>
     * @date Created on 2024年1月22日 <br>
     * @time 13:48:39 <br>
     * @version 1.0 <br>
     *          ************************************************************ <br>
     * @see <br>
     */
    private void AAMRedirectFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    {
        Logger.info("统一认证拦截开始");
        YonLogUtil.debug("ssoFilter", "统一认证拦截开始");
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        StringBuffer url = req.getRequestURL();
        
        try
        {
            Map<String, String> configInfo = YonYouUtilbc.NCLocator(nc.web.sso.itf.NC_Itf_SsoServer.class).getConfigInfo();
            String enableSSO = configInfo.get("enableSSO").toLowerCase();
            String ssicIP = configInfo.get("ssicIP");
            String siteCode = configInfo.get("siteCode");
            String ssicLoginUri = configInfo.get("ssicLoginUri");
            String ssicVersion = configInfo.get("ssicVersion");
            String fbipIp = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            if (PubEnvUtil.isNotEmptyObj(configInfo.get("portalurl")))//
                fbipIp = configInfo.get("portalurl");
            
            boolean enableUser = checkUser((HttpServletRequest) request, (HttpServletResponse) response, configInfo.get("user.enable"));
            boolean enableToken = checkToken((HttpServletRequest) request, (HttpServletResponse) response);
            if (enableUser || enableToken || PubEnvUtil.equals("yonyou", req.getParameter("logintype"))
                || PubEnvUtil.equals("y", req.getParameter("loginssokey"))
                || !(PubEnvUtil.equals("y", enableSSO) || PubEnvUtil.equals("true", enableSSO)))
            {
                chain.doFilter(request, response);
                return;
            }
            Logger.info("统一认证拦截：拦截地址：" + url);
            String login_url = fbipIp + "/portal/PortalSsoKeyServlet";
            String rez_url = ssicIP + ssicLoginUri + "?SERVICENAME=" + siteCode + "&SERVICEURL=" + login_url + "&VERSION=" + ssicVersion
                + "&OP=signIn&OPMode=0&STYPE=0&lang=";
            
            res.sendRedirect(rez_url);
        }
        catch (Exception e)
        {
            Logger.info(e.getMessage());
            Logger.debug("loginfilter报错 ；msg:" + e.getMessage());
            res.setHeader("REDIRECT", "REDIRECT");
            res.setHeader("REDIRECTSTATUS", "401");
            res.setStatus(401);
            
            YonLogUtil.debug("ssoFilter", "统一认证拦截异常：" + e.getMessage());
        }
        YonLogUtil.debug("ssoFilter", "统一认证拦截结束" + url);
        Logger.info("统一认证拦截结束");
        
    }
    
    /**
     * *********************************************************** <br>
     * *说明： B/S checkUser <br>
     *
     * @param request
     * @param response
     * @param userCode
     * @throws Exception <br>
     * @void <br>
     * @methods checkUser <br>
     * @author LiBencheng <br>
     * @date Created on 2024年1月22日 <br>
     * @time 13:48:39 <br>
     * @version 1.0 <br>
     *          ************************************************************ <br>
     * @see <br>
     */
    private Boolean checkUser(HttpServletRequest request, HttpServletResponse response, String userCode)
    {
        
        Cookie[] cookies = ((RequestFacade) request).getCookies();
        boolean enableUser = Boolean.FALSE;
        for (int i = 0; i < PubEnvUtil.getSize(cookies); i++)
        {
            Cookie cookie = cookies[i];
            if (PubEnvUtil.equals(cookie.getName(), "usercode"))
            {
                if (PubEnvUtil.isNotEmptyObj(userCode))
                {
                    String[] users = userCode.split(IPubCons.COMMA);
                    // enableUser = !PubEnvUtil.containStrTrim(cookie.getValue(), users);
                    enableUser = PubEnvUtil.containAllStr(cookie.getValue(), users);
                }
            }
        }
        
        return enableUser;
    }
    
    /**
     * *********************************************************** <br>
     * *说明： B/S checkToken <br>
     *
     * @param request
     * @param response
     * @throws Exception <br>
     * @void <br>
     * @methods checkUser <br>
     * @author LiBencheng <br>
     * @date Created on 2024年1月22日 <br>
     * @time 13:48:39 <br>
     * @version 1.0 <br>
     *          ************************************************************ <br>
     * @see <br>
     */
    private boolean checkToken(HttpServletRequest request, HttpServletResponse response)
    {
        
        String token = "";
        Cookie[] cookies = ((RequestFacade) request).getCookies();
        for (int i = 0; i < PubEnvUtil.getSize(cookies); i++)
        {
            Cookie cookie = cookies[i];
            if (PubEnvUtil.equals(cookie.getName(), "token")) token = cookie.getValue();
        }
        
        return PubEnvUtil.isNotEmptyObj(token);
    }
    
    protected abstract String getSysType();
    
    private boolean isToLogin(HttpServletRequest req, HttpServletResponse res)
    {
        String pageId = req.getParameter("pageId");
        return pageId != null && (pageId.equals("login") || pageId.equals("passwordmng"));
    }
    
    protected boolean isEscapeUrl(HttpServletRequest req, HttpServletResponse res)
    {
        ModelServerConfig modelConfig = LfwRuntimeEnvironment.getModelServerConfig();
        if (modelConfig == null)
        {
            return false;
        }
        else
        {
            String exp = modelConfig.getConfigValue("ESCAPE_URL");
            String url = req.getServletPath() + StringUtils.defaultIfEmpty(req.getPathInfo(), "")
                + StringUtils.defaultIfEmpty(req.getQueryString(), "");
            if (exp != null && exp.length() > 0)
            {
                String[] ex = exp.split(",");
                
                for (String e : ex)
                {
                    if (url.indexOf(e) != -1)
                    {
                        return true;
                    }
                }
            }
            
            return false;
        }
    }
    
    protected boolean isNotInControlUrl(String currentUrl, HttpServletRequest req)
    {
        WebContext wc = LfwRuntimeEnvironment.getWebContext();
        if (wc == null)
        {
            return true;
        }
        else
        {
            String pageId = wc.getPageId();
            if (pageId != null && pageId.equals("reference"))
            {
                return true;
            }
            else
            {
                String JSON_PATH = LfwRuntimeEnvironment.getCorePath() + "/json";
                return currentUrl.indexOf(this.getLoginJspName()) != -1 || currentUrl.indexOf(JSON_PATH) != -1
                    || currentUrl.equals(this.getAboutJspName()) || currentUrl.indexOf("/reference/") != -1;
            }
        }
    }
    
    protected boolean isUserLogin(HttpServletRequest request, HttpServletResponse response)
    {
        return LfwRuntimeEnvironment.getLfwSessionBean() != null;
    }
    
    public void destroy()
    {
    }
}
