package nc.web.sso.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.vo.ApiLogUtil; 
import nc.bs.vo.ClogVO;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.WebContext;
import nc.uap.lfw.core.ctx.AppLifeCycleContext;
import nc.uap.lfw.core.model.util.RunTimeDataProvider;
import nc.uap.lfw.core.page.LfwWindow;
import nc.uap.lfw.core.serializer.IXml2ObjectSerializer;
import nc.uap.lfw.core.serializer.Xml2AppContextSerializer;
import nc.uap.portal.deploy.vo.PtSessionBean;
import nc.uap.portal.login.itf.LoginHelper;
import nc.uap.portal.login.util.LfwLoginFetcher;
import nc.uap.portal.login.vo.AuthenticationUserVO;
import nc.web.sso.itf.NC_Itf_PortalSsoServer;
import nc.web.sso.itf.NC_Itf_SsoServer;
import pers.bc.utils.constant.IPubEvnCons;
import pers.bc.utils.pub.JudgeAssertUtil;
import pers.bc.utils.pub.LoggerUtil;
import pers.bc.utils.pub.PubEnvUtil;
import pers.bc.utils.pub.StringUtil;
import pers.bc.utils.yonyou.NccSsoPartyAccess;
import pers.bc.utils.yonyou.YonLogUtil;
import pers.bc.utils.yonyou.YonNccDB;
import pers.bc.utils.yonyou.YonYouUtilbc;

public class PortalSsoServlet extends NccSsoPartyAccess
{
    String folder = "ssolog";
    ClogVO clogVO = ApiLogUtil.newInitCLog();
    
    public void doAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            if (PubEnvUtil.isNotEmptyObj(request.getParameter("usercode")))
            {
                clogVO.setCode(request.getParameter("usercode"));
                this.exeDoAction(request, response);
            }
            else
            {
                this.exeLocalAccess(request, response);
                if (this.exeThirdPartyAccess(request, response))
                {
                    this.exeDoAction(request, response);
                }
                else
                {
                    Map<String, String> configInfo = YonYouUtilbc.NCLocator(NC_Itf_SsoServer.class).getConfigInfo();
                    String ssicIP = configInfo.get("ssicIP");
                    String siteCode = configInfo.get("siteCode");
                    String ssicLoginUri = configInfo.get("ssicLoginUri");
                    String ssicVersion = configInfo.get("ssicVersion");
                    String fbipIp = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                    if (PubEnvUtil.isNotEmptyObj(configInfo.get("portalurl")))//
                        fbipIp = configInfo.get("portalurl");
                    String loginType = request.getParameter("logintype");
                    String login_url = fbipIp + "/portal/PortalSsoKeyServlet";
                    String rez_url = ssicIP + ssicLoginUri + "?msg=" + clogVO.getMessage() + "&SERVICENAME=" + siteCode + "&SERVICEURL="
                        + login_url + "&VERSION=" + ssicVersion + "&OP=signIn&OPMode=0&STYPE=0&lang=";
                    
                    response.sendRedirect(rez_url);
                }
            }
        }
        catch (Exception e)
        {
            JudgeAssertUtil.throwExceptionDir(e, folder);
        }
        
    }
    
    @Override
    protected void exeLocalAccess(Map configInfo) throws Exception
    {
        // super.exeLocalAccess();
        String userid = clogVO.getCode();
        String busiCenterCode = StringUtil.valueOf(configInfo.get("busiCenterCode"));
        String datasource = StringUtil.valueOfDefault(configInfo.get("dataSource"), "design");
        InvocationInfoProxy.getInstance().setUserDataSource(datasource);
        JudgeAssertUtil.checkAssertDir(PubEnvUtil.isEmpty(userid), folder, "未传入用户信息, usercode为空！！！");
        List<Map> list =
            YonNccDB.newUtil().getDataByWhere("sm_user", "nvl(dr, 0) = 0 AND user_code = '" + userid + "' and enablestate = 2 ", null);
        JudgeAssertUtil.checkAssertDir(PubEnvUtil.getSize(list) < 1, folder, "根据传入的用户编码【" + userid + "】没有得到对应的用户信息！！");
        
    }
    
    @Override
    protected void exeDoAction(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        YonLogUtil.debug(folder, "-----------exeDoAction Start -----------");
        Map<String, String> configInfo = YonYouUtilbc.NCLocator(NC_Itf_SsoServer.class).getConfigInfo();
        String usercode = clogVO.getCode();
        exeLocalAccess(configInfo);
        String system = request.getParameter("clienttype") == null ? "portal" : request.getParameter("clienttype");
        // 伪登录页面初始化 begin
        WebContext webCtx = LfwRuntimeEnvironment.getWebContext();
        // if (webCtx != null)
        // LfwRuntimeEnvironment.getWebContext().setResponsXe(response);
        String datasource = StringUtil.valueOfDefault(configInfo.get("dataSource"), "design");
        LfwRuntimeEnvironment.setDatasource(datasource);
        LfwWindow win = RunTimeDataProvider.getInstance().getWindow("login");
        webCtx.setPageMeta(win);
        String xml =
            "<root><e id='login'><gps><ps><p><k>source_id</k><v>login</v></p><p><k>event_name</k><v>onClosed</v></p><p><k>source_type</k><v>pagemeta</v></p><p><k>p_signdata</k><v></v></p><p><k>p_sn</k><v></v></p><p><k>p_challdata</k><v></v></p><p><k>p_keepstate</k><v>0</v></p><p><k>p_tz</k><v>-480</v></p><p><k>clc</k><v>nc.uap.lfw.core.app.LfwAppDefaultController</v></p><p><k>el</k><v>1</v></p><p><k>source_id</k><v>login</v></p><p><k>m_n</k><v>onPageClosed</v></p><p><k>source_type</k><v>pagemeta</v></p><p><k>event_name</k><v>onClosed</v></p><p><k>hasChanged</k><v>true</v></p></ps></gps><c>{c: 'PageUIContext', hasChanged: true}</c><widget id='main'><c>{c: 'WidgetUIContext', visible: true}</c></widget><cc>{hasChanged: true, widgets: [{id: 'main', comps: [{id: 'multiLanguageCombo', value: 'simpchn', compType: 'combobox'}, {id: 'userid', value: '', compType: 'stringtext'}, {id: 'password', value: '', compType: 'pswtext'}, {id: 'tiplabel', color: '#FF0000', compType: 'label'}]}]}</cc></e></root>";
        AppLifeCycleContext ctx = this.getRequestSerializer().serialize(xml, null);
        AppLifeCycleContext.current(ctx);
        // 伪登录页面初始化 end
        // 初始化认证VO
        AuthenticationUserVO userVO = new AuthenticationUserVO();
        userVO.setUserID(usercode);
        Map<String, String> extMap = new HashMap<String, String>();
        extMap.put("p_maxwin", "N");
        extMap.put("needverifypasswd", "N");// 免密登录
        userVO.setExtInfo(extMap);
        getLoginHelper().processLogin(userVO);
        // res.sendRedirect("/portal");
        AppLifeCycleContext.reset();
        response.sendRedirect("/portal/pt/home/index");
        YonLogUtil.debug(folder, "-----------exeDoAction end -----------");
        YonLogUtil.debug(folder, LoggerUtil.getSplitLine());
    }
    
    @Override
    protected boolean exeThirdPartyAccess(HttpServletRequest request, HttpServletResponse response)
    {
        boolean access = super.exeThirdPartyAccess(request, response);
        clogVO = YonYouUtilbc.NCLocator(NC_Itf_PortalSsoServer.class).cheeckThirdPartyAccess(request, response);
        return PubEnvUtil.equals(Boolean.TRUE, clogVO.getDef04());
    }
    
    public static String getRootPath(HttpServletRequest req)
    {
        StringBuffer path = new StringBuffer();
        path.append(req.getScheme()).append("://").append(req.getServerName()).append(IPubEvnCons.COLON).append(req.getServerPort());
        return path.toString();
    }
    
    private IXml2ObjectSerializer<AppLifeCycleContext> getRequestSerializer()
    {
        return new Xml2AppContextSerializer();
    }
    
    public LoginHelper<PtSessionBean> getLoginHelper()
    {
        return (LoginHelper<PtSessionBean>) LfwLoginFetcher.getGeneralInstance().getLoginHelper();
    }
    
    @Override
    protected void exeLocalAccess(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        // super.exeLocalAccess(request, response);
    }
}
