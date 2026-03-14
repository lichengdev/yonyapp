package nc.web.sso.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.logging.Log;
import nc.bs.vo.ApiLogUtil;
import nc.bs.vo.ClogVO;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.web.sso.itf.NC_Itf_PortalSsoServer;
import nc.web.sso.itf.NC_Itf_SsoServer;
import pers.bc.utils.constant.IPubEvnCons;
import pers.bc.utils.pub.JudgeAssertUtil;
import pers.bc.utils.pub.LoggerUtil;
import pers.bc.utils.pub.PubEnvUtil;
import pers.bc.utils.pub.StringUtil;
import pers.bc.utils.sql.SqlHelper;
import pers.bc.utils.yonyou.NCSSOUtil;
import pers.bc.utils.yonyou.NccSsoPartyAccess;
import pers.bc.utils.yonyou.YonLogUtil;
import pers.bc.utils.yonyou.YonYouUtilbc;

/**
 * ip+/service/nccPortalSsoLogin portal 单点登录
 */
public class PortalSsoKeyServlet extends NccSsoPartyAccess
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
    protected void exeLocalAccess(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        // super.exeLocalAccess(request, response);
    }
    
    @Override
    protected void exeLocalAccess(Map configInfo) throws Exception
    {
        // super.exeLocalAccess();
        String userid = clogVO.getCode();
        String busiCenterCode = StringUtil.valueOf(configInfo.get("busiCenterCode"));
        String datasource = StringUtil.valueOfDefault(configInfo.get("dataSource"), "design");
        if (PubEnvUtil.isEmpty(InvocationInfoProxy.getInstance().getUserDataSource()))
            InvocationInfoProxy.getInstance().setUserDataSource(datasource);
        JudgeAssertUtil.checkAssertDir(PubEnvUtil.isEmpty(userid), folder, "未传入用户信息, usercode为空！！！");
        String sql = SqlHelper.getSelectSQL("sm_user", (String[]) null,
            "nvl(dr, 0) = 0 AND user_code = '" + userid + "' and enablestate = 2 ", "user_code");
        List<Map> list = (List<Map>) new BaseDAO().executeQuery(sql, new MapListProcessor());
        // List<Map> list = (List<Map>) YonYouUtilbc.getBaseDAOQueryBSyC().executeQuery(sql, new
        // MapListProcessor());
        JudgeAssertUtil.checkAssertDir(PubEnvUtil.getSize(list) < 1, folder, "根据传入的用户编码【" + userid + "】没有得到对应的用户信息！！");
        
    }
    
    @Override
    protected void exeDoAction(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        YonLogUtil.debug(folder, "-----------exeDoAction Start -----------");
        Map<String, String> configInfo = YonYouUtilbc.NCLocator(NC_Itf_SsoServer.class).getConfigInfo();
        String userid = clogVO.getCode();
        exeLocalAccess(configInfo);
        YonLogUtil.debug(folder, "[SSO-Portal]单点网报请求userid:" + userid);
        String path = request.getSession().getServletContext().getRealPath("");
        Log.getInstance("gylc").info("[SSO-Portal]单点网报请求path:" + path);
        YonLogUtil.debug(folder, "[SSO-Portal]单点网报请求path:" + path);
        // 需要翻译OA系统给的是用户编码
        String reqIp = getRootPath(request);// 解决内网外网都可以访问的问题（用请求地址）
        if (PubEnvUtil.isNotEmptyObj(configInfo.get("baseUrl")))//
            reqIp = configInfo.get("baseUrl");
        Log.getInstance("gylc").info("[SSO-Portal]单点网报请求reqIp:" + reqIp);
        YonLogUtil.debug(folder, "[SSO-Portal]单点网报请求reqIp:" + reqIp);
        // String registryUrl = local + "/portal/registerServlet";// 注册用内网地址
        String datasource = StringUtil.valueOfDefault(configInfo.get("dataSource"), "design");
        String registeResult = NCSSOUtil.getTokenByNc(reqIp, datasource, userid + "&loginsuccess=y").replace("t:", "");
        
        if (PubEnvUtil.isNotEmptyObj(configInfo.get("portalurl")))//
            reqIp = configInfo.get("portalurl");
        
        String portalLoginUrl = reqIp + "/portal/pt/home/index?ssoKey=" + registeResult;
        if (PubEnvUtil.equals("Y", configInfo.get("registerServlet")))
        {
            StringBuffer parameters = new StringBuffer("?ssoKey=").append(registeResult).append("&type=1").append("&dsname=")
                .append(datasource).append("&userid=").append(userid).append("&loginsuccess=").append("y");
            portalLoginUrl = reqIp + "/portal/registerServlet" + parameters.toString();
        }
        
        clogVO.setDef06(portalLoginUrl);
        if (PubEnvUtil.isNotEmptyObj(clogVO.getPk_log())) ApiLogUtil.getInstance().updateLog(clogVO);
        YonLogUtil.debug(folder, "[SSO-Portal]单点网报请求portalLoginUrl:" + portalLoginUrl);
        response.sendRedirect(portalLoginUrl);
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
}
