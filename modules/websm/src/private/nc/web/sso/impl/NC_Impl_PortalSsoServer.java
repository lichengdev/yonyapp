package nc.web.sso.impl;

import com.icbc.ssic.base.ServerSideAuthenticator;
import nc.bs.logging.Logger;
import nc.bs.vo.ApiLogUtil;
import nc.bs.vo.ClogVO;
import nc.web.sso.itf.NC_Itf_SsoServer;
import nc.web.sso.itf.NC_Itf_PortalSsoServer;
import pers.bc.utils.constant.IPubEvnCons;
import pers.bc.utils.pub.*;
import pers.bc.utils.yonyou.YonLogUtil;
import pers.bc.utils.yonyou.YonYouUtilbc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class NC_Impl_PortalSsoServer implements NC_Itf_PortalSsoServer
{
    
    String folderName = "ssologs";
    LoggerUtil logUtil = LoggerUtil.getInstance(folderName);
    private static final String SSI_AUTH = "SSIAuth";
    private static final String SSI_SIGN = "SSISign";
    private static final String SSI_CREDENTIALS = "ssiCredentials";
    private static final String SIGN_IN = "signIn";
    private static final String SIGN_OUT_SSI = "signOutSSI";
    
    @Override
    public ClogVO cheeckThirdPartyAccess(HttpServletRequest request, HttpServletResponse response)
    {
        ClogVO clogVO = ApiLogUtil.newInitCLog();
        try
        {
            Map<String, String> configInfo = YonYouUtilbc.NCLocator(NC_Itf_SsoServer.class).getConfigInfo();
            String ssiAuth = request.getParameter(SSI_AUTH);
            String ssiSign = request.getParameter(SSI_SIGN);
            String fbipUrl = request.getScheme() + "://" + request.getServerName() + IPubEvnCons.COLON + request.getServerPort();
            if (PubEnvUtil.isNotEmptyObj(configInfo.get("portalurl")))//
                fbipUrl = configInfo.get("portalurl");
            String ssoUrl = fbipUrl + "/portal/PortalSsoKeyServlet";
            
            ServerSideAuthenticator serverSideAuth = initServerAam(ssoUrl, configInfo);
            
            clogVO.setRequest(JsonUtil.compressJson(JsonUtil.toJSONString(serverSideAuth)));
            clogVO.setCode(System.currentTimeMillis() + "");
            clogVO.setSessionid(request.getSession().getId());
            clogVO.setAction("ssoPortal");
            clogVO.setDatatype("ssoPortal");
            clogVO.setName("portal统一认证接口");
            clogVO.setIp(configInfo.get("ssicIP"));
            clogVO.setUrl(ssoUrl);
            clogVO.setDef01(ssiAuth);
            clogVO.setDef02(ssiSign);
            clogVO.setDef03(ssoUrl);
            // 请求
            clogVO = ApiLogUtil.getInstance().insertSendLog(clogVO);
            boolean execute = serverSideAuth.execute(request, response, ssiAuth, ssiSign);
            if (execute)
            {
                clogVO.setRestcode("200");
                clogVO.setMessage("统一认证验签通过!!! ");
                YonLogUtil.debug(folderName, "----------统一认证验签通过-----------------");
                com.icbc.ssic.base.Credentials cred = (com.icbc.ssic.base.Credentials) request.getAttribute(SSI_CREDENTIALS);
                com.icbc.ssic.base.SSICUser ssicUser = cred.getSSICUser();
                String userid = ssicUser.getUserName();
                clogVO.setCode(userid);
            }
            else
            {
                clogVO.setRestcode("201");
                clogVO.setMessage("统一认证验签未通过!!! ");
                YonLogUtil.debug(folderName, "----------统一认证验签未通过-----------------");
            }
            clogVO.setDef04(StringUtil.valueOfEmpty(execute));
            
            return clogVO;
        }
        catch (Exception e)
        {
            YonLogUtil.debug(folderName, "统一认证遇到的错误, msg：" + e.getMessage());
            clogVO.setRestcode("500");
            clogVO.setDef04(StringUtil.valueOfEmpty(Boolean.FALSE));
            clogVO.setMessage("统一认证遇到的错误, msg：" + e.getMessage());
            clogVO.setThwmsg(StringUtil.toString(e));
            logUtil.exception(e);
            Logger.error(e.getMessage());
            JudgeAssertUtil.throwExceptionDir(e, folderName);
        }
        finally
        {
            ApiLogUtil.getInstance().updateLog(clogVO);
            YonLogUtil.debug(folderName, LoggerUtil.getSplitLine());
        }
        
        return clogVO;
    }
    
    /**
     * *********************************************************** <br>
     * *说明： B/S ncportal 单点登录，校验统一认证平台 <br>
     *
     * @param siteUrl
     * @param configInfo
     * @throws Exception <br>
     * @void <br>
     * @methods initServerAam <br>
     * @author LiBencheng <br>
     * @date Created on 2024年1月22日 <br>
     * @time 13:48:39 <br>
     * @version 1.0 <br>
     *          ************************************************************ <br>
     * @see <br>
     */
    private ServerSideAuthenticator initServerAam(final String siteUrl, Map<String, String> configInfo) throws Exception
    {
        final String ssicIp = configInfo.get("ssicIP");
        final String ssicVersion = configInfo.get("ssicVersion");
        final String ssicClientKeyName = configInfo.get("ssicServiceKeyName");
        final String smPublicKey = configInfo.get("smPublicKey");
        final String smKeyPass = configInfo.get("smKeyPass");
        
        ServerSideAuthenticator serverSideAuth = new ServerSideAuthenticator()
        {
            {
                setServerName(ssicIp);
                setVersion(ssicVersion);
                setServiceName(ssicClientKeyName);
                setServiceURL(siteUrl);
                setSmPublicKey(smPublicKey);
                setSmKeyPass(smKeyPass);
            }
        };
        
        YonLogUtil.debug(folderName, "统一认证配置-info: " + JsonUtil.compressJson(JsonUtil.toJSONString(serverSideAuth)));
        YonLogUtil.debug(folderName, "统一认证配置：ssicIp = " + ssicIp + "，ssicVersion = " + "，ssicClientKeyName = " + ssicClientKeyName);
        
        return serverSideAuth;
        
    }
    
    @Override
    public String testString()
    {
        
        return "test***********************";
    }
}
