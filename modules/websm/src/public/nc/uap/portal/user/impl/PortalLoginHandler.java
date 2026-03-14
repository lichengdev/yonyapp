package nc.uap.portal.user.impl;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.uap.cpb.org.exception.CpbBusinessException;
import nc.uap.cpb.org.itf.ICpSysinitQry;
import nc.uap.cpb.org.vos.CpUserVO;
import nc.uap.lfw.core.AppInteractionUtil;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.WebContext;
import nc.uap.lfw.core.cache.LfwCacheManager;
import nc.uap.lfw.core.comp.text.ComboBoxComp;
import nc.uap.lfw.core.comp.text.TextComp;
import nc.uap.lfw.core.ctx.AppLifeCycleContext;
import nc.uap.lfw.core.exception.LfwInteractionException;
import nc.uap.lfw.core.exception.LfwRuntimeException;
import nc.uap.lfw.core.page.LfwView;
import nc.uap.lfw.core.util.HttpUtil;
import nc.uap.lfw.login.vo.LfwSessionBean;
import nc.uap.lfw.util.LfwUserShareUtil;
import nc.uap.portal.cache.PortalCacheManager;
import nc.uap.portal.deploy.vo.PtSessionBean;
import nc.uap.portal.exception.BreakPortalLoginException;
import nc.uap.portal.exception.PortalServerRuntimeException;
import nc.uap.portal.exception.PortalServiceException;
import nc.uap.portal.log.PortalLogger;
import nc.uap.portal.login.itf.ILoginHandler;
import nc.uap.portal.login.itf.ILoginSsoService;
import nc.uap.portal.login.itf.IMaskerHandler;
import nc.uap.portal.login.itf.LoginInterruptedException;
import nc.uap.portal.login.vo.AuthenticationUserVO;
import nc.uap.portal.om.Page;
import nc.uap.portal.plugins.PluginManager;
import nc.uap.portal.service.PortalServiceUtil;
import nc.uap.portal.service.itf.IPtPageQryService;
import nc.uap.portal.user.entity.IOrgVO;
import nc.uap.portal.user.entity.IUserVO;
import nc.uap.portal.user.itf.IUserLoginPlugin;
import nc.uap.portal.util.PortalPageDataWrap;
import nc.uap.portal.util.ToolKit;
import nc.uap.portal.vo.PtPageVO;
import nc.vo.bd.format.FormatDocVO;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import org.apache.commons.lang.StringUtils;
import uap.lfw.core.locator.AdapterServiceLocator;
import uap.lfw.core.ml.LfwResBundle;
import uap.lfw.portal.model.BrotherPair;
import uap.lfw.portal.user.ForceLoginTools;
import uap.lfw.portal.user.itf.IUserBill;

/**
 * 破解補丁
 **
 * @qualiFild nc.uap.portal.user.impl.PortalLoginHandler.java<br>
 * @author：LiBencheng<br>
 * @date Created on 2025年8月12日<br>
 * @version 1.0<br>
 */
public class PortalLoginHandler implements ILoginHandler<PtSessionBean>, IMaskerHandler<PtSessionBean>
{
    private static final String INFO = "INFO";
    private static final String ERROR = "ERROR";
    private static final String LEVEL2 = "level";
    private static final String CHALLLID2 = "challlid";
    private static final String DESC = "DESC";
    private static final String CODE = "CODE";
    private static final String AFTER = "after";
    private static final String CA_USER_ID = "p_userId";
    private static final String SIGNDATA = "p_signdata";
    private static final String MAXWIN = "p_maxwin";
    private static final String LANGUAGE = "p_language";
    protected static final String LOGINDATE = "logindate";
    protected static final String FORCE = "force";
    private static final String BEFORE = "before";
    public static final String KEY = "ufida&UAP!102";
    private List<IUserLoginPlugin> plugins = null;
    IUserBill ub = null;
    
    public PtSessionBean doAuthenticate(AuthenticationUserVO userInfo) throws LoginInterruptedException
    {
        try
        {
            Map<String, String> extMap = (Map) userInfo.getExtInfo();
            this.loginPluginExecutor(userInfo, "before");
            String userid = userInfo.getUserID();
            Map rsl = this.getUserBill().userVerify(userid, userInfo.getPassword(), extMap);
            String rslCode = (String) rsl.get("CODE");
            String rslMsg = (String) rsl.get("DESC");
            String level = (String) rsl.get("level");
            if ("0".equals(rslCode))
            {
                if (AppLifeCycleContext.current() != null)
                {
                    if (rslMsg != null)
                    {
                        if ("ERROR".equals(level))
                        {
                            LfwRuntimeEnvironment.getWebContext().getRequest().setAttribute("isResetUserPwdFlag", "Y");
                            this.ensureChangePasswd(userid, rslMsg);
                            throw new LoginInterruptedException(rslMsg);
                        }
                        
                        if ("INFO".equals(level))
                        {
                            AppInteractionUtil.showMessageDialogWithRePost(rslMsg);
                        }
                    }
                }
                else if (rslMsg != null && "ERROR".equals(level))
                {
                    throw new LoginInterruptedException(rslMsg);
                }
            }
            
            if ("1".equals(rslCode))
            {
                this.getUserBill().doLoginErrorLog(userInfo, rslMsg);
                WebContext webContext = LfwRuntimeEnvironment.getWebContext();
                HttpSession session = null;
                if (webContext != null)
                {
                    HttpServletRequest httpServRequest = webContext.getRequest();
                    if (httpServRequest != null)
                    {
                        session = httpServRequest.getSession();
                        if (session != null)
                        {
                            session.setAttribute("fatalexcp:" + userid, "Y");
                        }
                    }
                }
                else
                {
                    String userId = InvocationInfoProxy.getInstance().getUserId();
                    String var28 = InvocationInfoProxy.getInstance().getGroupId();
                }
                
                throw new LoginInterruptedException(rslMsg);
            }
            else if ("2".equals(rslCode))
            {
                WebContext webContext = LfwRuntimeEnvironment.getWebContext();
                HttpSession session = null;
                String challlid = UUID.randomUUID().toString();
                if (webContext != null)
                {
                    HttpServletRequest httpServRequest = webContext.getRequest();
                    if (httpServRequest != null)
                    {
                        session = httpServRequest.getSession();
                        if (session != null)
                        {
                            session.setAttribute("challlid", challlid);
                        }
                    }
                }
                
                AppLifeCycleContext.current().getWindowContext().addExecScript("calogin('" + challlid + "','" + userid + "')");
                return null;
            }
            else
            {
                String langCode = "simpchn";
                if (extMap.get("p_language") != null)
                {
                    langCode = (String) extMap.get("p_language");
                }
                else
                {
                    langCode = LfwRuntimeEnvironment.getLangCode();
                }
                
                IUserVO ptUser = (IUserVO) rsl.get("USER");
                if (!ForceLoginTools.canMultiLogin())
                {
                    String datasource = InvocationInfoProxy.getInstance().getUserDataSource();
                    BrotherPair<String, String> ret = ForceLoginTools.getOriSessionId(ptUser.getUserid(), datasource);
                    if (ret != null)
                    {
                        if (AppLifeCycleContext.current() != null)
                        {
                            boolean forceLoginFlag = AppInteractionUtil.showConfirmDialog(
                                NCLangRes4VoTransl.getNCLangRes().getStrByID("pserver", "exception-0001"),
                                NCLangRes4VoTransl.getNCLangRes().getStrByID("pserver", "PortalLoginHandler-0000"));
                            if (!forceLoginFlag)
                            {
                                return null;
                            }
                            
                            ForceLoginTools.logout(ret);
                        }
                        else
                        {
                            if (!"Y".equals(extMap.get("forcelogin")))
                            {
                                throw new SecurityException(
                                    NCLangRes4VoTransl.getNCLangRes().getStrByID("pserver", "PortalLoginHandler-0000"));
                            }
                            
                            ForceLoginTools.logout(ret);
                        }
                    }
                }
                
                ptUser.setLangcode(langCode);
                PtSessionBean sbean = this.createSessionBean(ptUser);
                String tzOffset = (String) extMap.get("p_tz");
                if (tzOffset != null)
                {
                    int rawOffset = Integer.parseInt(tzOffset) * 60 * -1 * 1000;
                    TimeZone tz = new SimpleTimeZone(rawOffset, "GMT " + rawOffset / 60 / 60 / 1000);
                    sbean.setTimeZone(tz);
                }
                
                return sbean;
            }
        }
        catch (Exception e)
        {
            if (e instanceof LfwInteractionException)
            {
                throw (LfwInteractionException) e;
            }
            else if (e instanceof LoginInterruptedException)
            {
                throw (LoginInterruptedException) e;
            }
            else if (e instanceof SecurityException)
            {
                throw (SecurityException) e;
            }
            else
            {
                PortalLogger.error("Login Error:" + e.getMessage(), e);
                throw new LoginInterruptedException(e.getMessage());
            }
        }
    }
    
    private void ensureChangePasswd(String userid, String rslMsg)
    {
        WebContext webContext = LfwRuntimeEnvironment.getWebContext();
        HttpSession session = null;
        String challlid = UUID.randomUUID().toString();
        if (webContext != null)
        {
            HttpServletRequest httpServRequest = webContext.getRequest();
            if (httpServRequest != null)
            {
                session = httpServRequest.getSession();
                if (session != null)
                {
                    session.setAttribute("USER_SESSION_ID", userid);
                }
            }
        }
        
        StringBuffer urlBuf = new StringBuffer();
        urlBuf.append("/portal/app/mockapp/passwordmng?model=nc.uap.portal.mng.pwdmng.PasswordManagerModel");
        urlBuf.append("&otherPageUniqueId=" + LfwRuntimeEnvironment.getWebContext().getWebSession().getWebSessionId());
        AppLifeCycleContext.current().getApplicationContext().popOuterWindow(urlBuf.toString(), rslMsg, "480", "280", "TYPE_DIALOG");
    }
    
    public void afterLogin(LfwSessionBean userVO)
    {
        HttpServletRequest request = LfwRuntimeEnvironment.getWebContext().getRequest();
        
        try
        {
            LfwRuntimeEnvironment.setLfwSessionBean(userVO);
            LfwRuntimeEnvironment.setClientIP(HttpUtil.getIp());
            LfwRuntimeEnvironment.setDatasource(userVO.getDatasource());
            if (!"annoyuser".equals(userVO.getUser_code()))
            {
                this.changeSessionIdentifier(request);
            }
            
            request.getSession().setAttribute("LOGIN_SESSION_BEAN", userVO);
            this.initUser(userVO);
            this.regOnlineUser(userVO, request);
            ILoginSsoService<PtSessionBean> ssoService = this.getLoginSsoService();
            ssoService.addSsoSign((PtSessionBean) userVO, this.getSysType());
            UFBoolean loginResult = UFBoolean.TRUE;
            this.loginPluginExecutor(userVO, "after");
            this.getUserBill().doLoginLog(userVO, loginResult,
                LfwResBundle.getInstance().getStrByID("pserver", "PortalLoginHandler-000023"));
        }
        catch (BusinessException e)
        {
            PortalLogger.error(e.getMessage(), e);
        }
        
    }
    
    public void initUser(LfwSessionBean sbean) throws PortalServiceException
    {
        IPtPageQryService qry = PortalServiceUtil.getPageQryService();
        PtSessionBean sb = (PtSessionBean) sbean;
        IUserVO user = sb.getUser();
        String origPkorg = user.getPk_org();
        CpUserVO uservo = (CpUserVO) user.getUser();
        if (LfwUserShareUtil.isNeedShareUser && StringUtils.isNotEmpty(sbean.getPk_unit()) && StringUtils.isNotEmpty(user.getPk_group())
            && !sbean.getPk_unit().equals(user.getPk_group()))
        {
            uservo.setPk_org(sbean.getPk_unit());
        }
        
        PtPageVO[] pageVOs = qry.getPagesByUser(user);
        uservo.setPk_org(origPkorg);
        if (pageVOs != null && pageVOs.length != 0)
        {
            pageVOs = PortalPageDataWrap.filterPagesByUserType(pageVOs, sb.getUser_type());
            if (pageVOs != null && pageVOs.length != 0)
            {
                List<Page> pageList = PortalPageDataWrap.praseUserPages(pageVOs);
                if (pageList.isEmpty())
                {
                    throw new PortalServerRuntimeException(LfwResBundle.getInstance().getStrByID("pserver", "PortalLoginHandler-000026"));
                }
                else
                {
                    Map<String, Page> pagesCache = PortalPageDataWrap.praseUserPages((Page[]) pageList.toArray(new Page[0]));
                    PortalCacheManager.getUserPageCache().clear();
                    PortalCacheManager.getUserPageCache().putAll(pagesCache);
                }
            }
            else
            {
                throw new PortalServerRuntimeException(LfwResBundle.getInstance().getStrByID("pserver", "PortalLoginHandler-000025"));
            }
        }
        else
        {
            throw new PortalServerRuntimeException(LfwResBundle.getInstance().getStrByID("pserver", "PortalLoginHandler-000024"));
        }
    }
    
    private List<IUserLoginPlugin> getLoginPlugins()
    {
        if (this.plugins == null)
        {
            this.plugins = PluginManager.newIns().getExtInstances("loginplugin", IUserLoginPlugin.class);
        }
        
        return this.plugins;
    }
    
    private PtSessionBean createSessionBean(IUserVO user)
    {
        IOrgVO org = this.getUserBill().getOrg(user.getPk_group());
        String groupNo;
        String groupName;
        if (org != null)
        {
            groupNo = org.getCode();
            groupName = org.getName();
        }
        else
        {
            groupNo = "0000";
            groupName = "0000";
        }
        
        PtSessionBean sbean = new PtSessionBean();
        sbean.setDatasource(LfwRuntimeEnvironment.getDatasource());
        sbean.setUnitNo(groupNo);
        sbean.setUnitName(groupName);
        sbean.setUserType(user.getUsertype());
        sbean.setUser(user);
        sbean.setTimespan(System.currentTimeMillis());
        String themeId = LfwRuntimeEnvironment.getThemeId();
        sbean.setThemeId(themeId);
        return sbean;
    }
    
    public AuthenticationUserVO getAuthenticateVO() throws LoginInterruptedException
    {
        AuthenticationUserVO userVO = new AuthenticationUserVO();
        Map<String, String> extMap = new HashMap();
        LfwView widget = this.getCurrentWidget();
        TextComp userIdComp = (TextComp) widget.getViewComponents().getComponent("userid");
        TextComp randomImageComp = (TextComp) widget.getViewComponents().getComponent("randimg");
        ICpSysinitQry cpSysinitQry = PortalServiceUtil.getCpSysinitQry();
        boolean enabledRandomImage = false;
        
        try
        {
            String showRanImg = cpSysinitQry.getSysinitValueByCodeAndPkorg("randomimg", (String) null);
            enabledRandomImage = UFBoolean.valueOf(showRanImg).booleanValue();
        }
        catch (CpbBusinessException e)
        {
            PortalLogger.error(e.getMessage(), e);
        }
        
        String userId = null;
        AppLifeCycleContext pctx = AppLifeCycleContext.current();
        HttpSession session = LfwRuntimeEnvironment.getWebContext().getRequest().getSession();
        String signdata = pctx.getParameter("p_signdata");
        String sn = pctx.getParameter("p_sn");
        String tz = pctx.getParameter("p_tz");
        if (userIdComp != null)
        {
            if (enabledRandomImage)
            {
                String rand = null;
                if (session != null)
                {
                    rand = (String) session.getAttribute("rand");
                }
                
                String ricv = randomImageComp.getValue();
                if (!StringUtils.equals(rand, ricv))
                {
                    throw new LoginInterruptedException(LfwResBundle.getInstance().getStrByID("pserver", "PortalLoginHandler-000006"));
                }
            }
            
            userId = userIdComp.getValue();
            if (userId == null || userId.equals(""))
            {
                throw new LoginInterruptedException(LfwResBundle.getInstance().getStrByID("pserver", "PortalLoginHandler-000007"));
            }
        }
        
        TextComp passComp = (TextComp) widget.getViewComponents().getComponent("password");
        String passValue = null;
        if (passComp != null)
        {
            passValue = passComp.getValue();
            if (passValue == null)
            {
                passValue = "";
            }
        }
        
        ComboBoxComp multiLanguageCombo = (ComboBoxComp) widget.getViewComponents().getComponent("multiLanguageCombo");
        String language = multiLanguageCombo.getValue();
        userVO.setUserID(userId);
        userVO.setPassword(passValue);
        extMap.put("p_language", language);
        extMap.put("p_maxwin", "N");
        extMap.put("p_signdata", signdata);
        extMap.put("needverifypasswd", "N");
        extMap.put("p_sn", sn);
        extMap.put("p_tz", tz);
        String challlid = (String) session.getAttribute("challlid");
        extMap.put("challlid", challlid);
        userVO.setExtInfo(extMap);
        return userVO;
    }
    
    public Cookie[] getCookies(AuthenticationUserVO userVO)
    {
        List<Cookie> list = new ArrayList();
        String userId = userVO.getUserID();
        Map<String, String> extMap = (Map) userVO.getExtInfo();
        String sysId = "" + LfwRuntimeEnvironment.getSysId();
        String themeId = LfwRuntimeEnvironment.getLfwSessionBean().getThemeId();
        String language = (String) extMap.get("p_language");
        String maxwin = (String) extMap.get("p_maxwin");
        String useridEncode = null;
        String cookiePath = LfwRuntimeEnvironment.getRootPath();
        
        try
        {
            useridEncode = URLEncoder.encode(userId, "UTF-8");
        }
        catch (Exception e)
        {
            PortalLogger.warn(e.getMessage());
        }
        
        Cookie tc = new Cookie("TH_K" + sysId, themeId);
        tc.setPath("/");
        tc.setMaxAge(604800000);
        list.add(tc);
        Cookie lc = new Cookie("LA_K" + sysId, language);
        lc.setPath("/");
        lc.setMaxAge(604800000);
        list.add(lc);
        Cookie uc = new Cookie("p_userId", useridEncode);
        uc.setPath(cookiePath);
        uc.setMaxAge(604800000);
        list.add(uc);
        Cookie mc = new Cookie("isMaxWindow", maxwin);
        mc.setPath(cookiePath);
        mc.setMaxAge(604800000);
        list.add(mc);
        Cookie p_auth = new Cookie("p_logoutflag", (String) null);
        p_auth.setMaxAge(604800000);
        p_auth.setPath(cookiePath);
        list.add(p_auth);
        return (Cookie[]) list.toArray(new Cookie[0]);
    }
    
    public ILoginSsoService<PtSessionBean> getLoginSsoService()
    {
        return new PortalSSOServiceImpl();
    }
    
    public String getSysType()
    {
        return "pt";
    }
    
    public LfwView getCurrentWidget()
    {
        return AppLifeCycleContext.current().getViewContext().getView();
    }
    
    public FormatDocVO getMaskerInfo(PtSessionBean loginBean)
    {
        return this.getUserBill().getMaskerInfo(loginBean);
    }
    
    private void regOnlineUser(LfwSessionBean sb, HttpServletRequest request) throws BusinessException
    {
        String clientIP = HttpUtil.getIp();
        String sessionid = request.getSession().getId();
        this.getUserBill().regOnlineUser(sb, sessionid, clientIP);
    }
    
    private void loginPluginExecutor(Object userInfo, String cmd)
    {
        if (ToolKit.notNull(this.getLoginPlugins()))
        {
            for (IUserLoginPlugin ex : this.getLoginPlugins())
            {
                boolean isBefore = "before".equals(cmd);
                
                try
                {
                    if (isBefore)
                    {
                        ex.beforeLogin((AuthenticationUserVO) userInfo);
                    }
                    else
                    {
                        ex.afterLogin((PtSessionBean) userInfo);
                    }
                }
                catch (BreakPortalLoginException var7)
                {
                    PortalLogger.error(var7.getMessage(), var7);
                    if (isBefore)
                    {
                        this.getUserBill().doLoginErrorLog((AuthenticationUserVO) userInfo, var7.getHint());
                    }
                    else
                    {
                        this.getUserBill().doLoginLog((LfwSessionBean) userInfo, UFBoolean.FALSE, var7.getHint());
                    }
                    
                    throw new LfwRuntimeException(var7.getHint());
                }
                catch (Throwable a)
                {
                    PortalLogger.error(LfwResBundle.getInstance().getStrByID("pserver", "PortalLoginHandler-000027") + a.getMessage(), a);
                }
            }
        }
        
    }
    
    private IUserBill getUserBill()
    {
        if (this.ub == null)
        {
            this.ub = (IUserBill) AdapterServiceLocator.newIns().get(IUserBill.class);
        }
        
        return this.ub;
    }
    
    private HttpSession changeSessionIdentifier(HttpServletRequest request)
    {
        HttpSession oldSession = request.getSession();
        Map<String, Object> temp = new ConcurrentHashMap();
        Map<String, Object> oldSessionCache = new ConcurrentHashMap();
        oldSessionCache.putAll(LfwCacheManager.getSessionCache());
        Enumeration<String> e = oldSession.getAttributeNames();
        
        while (e != null && e.hasMoreElements())
        {
            String name = (String) e.nextElement();
            Object value = oldSession.getAttribute(name);
            temp.put(name, value);
        }
        
        oldSession.setAttribute("SESSION_SELF_DESTORY", Boolean.TRUE);
        oldSession.invalidate();
        HttpSession newSession = request.getSession(true);
        
        for (Map.Entry<String, Object> stringObjectEntry : temp.entrySet())
        {
            newSession.setAttribute((String) stringObjectEntry.getKey(), stringObjectEntry.getValue());
        }
        
        Map<String, Object> newSessionCache = LfwCacheManager.getSessionCache();
        newSessionCache.putAll(oldSessionCache);
        return newSession;
    }
}
