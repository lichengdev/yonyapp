package nc.web.sso.impl;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import nc.bs.framework.common.RuntimeEnv;
import nc.bs.logging.Logger;
import nc.bs.vo.ApiLogUtil;
import nc.bs.vo.ClogVO;
import nc.web.sso.itf.NC_Itf_SsoServer;
import pers.bc.utils.constant.InumberCons;
import pers.bc.utils.pub.CollectionUtil;
import pers.bc.utils.pub.JsonUtil;
import pers.bc.utils.pub.JudgeAssertUtil;
import pers.bc.utils.pub.LoggerUtil;
import pers.bc.utils.pub.PropertiesUtil;
import pers.bc.utils.pub.PubEnvUtil;
import pers.bc.utils.pub.RandomUtil;
import pers.bc.utils.pub.StringUtil;
import pers.bc.utils.yonyou.YonLogUtil;

public class NC_Impl_SsoServer implements NC_Itf_SsoServer
{
    
    String folderName = "ssolos";
    LoggerUtil logUtil = LoggerUtil.getInstance(folderName);
    public static Map<String, String> properMap = CollectionUtil.newHashMap();
    
    @Override
    public ClogVO createVerifyCode(final String userName, final String clientId)
    {
        
        final Map<String, String> configInfo = getConfigInfo();
        // configInfo.put("clientIp", clientId);
        ClogVO clogVO = ApiLogUtil.newInitCLog();
        final String verifyRequestNo = RandomUtil.UUID();
        clogVO.setToken(verifyRequestNo);
        clogVO.setSessionid(verifyRequestNo);
        try
        {
            clogVO.setCode(userName + PubEnvUtil.AT + System.currentTimeMillis());
            clogVO.setAction("createVerifyCode");
            clogVO.setName("员工统一账号验证码创建");
            // clogVO.setIp(configInfo.get("CsIP"));
            clogVO.setIp(clientId);
            clogVO.setDatatype("createVerifyCode");
            // 请求
            clogVO = ApiLogUtil.getInstance().insertSendLog(clogVO);
            // 同步调用，并获取请求结果
            
        }
        catch (Exception e)
        {
            clogVO.setDef01("9999");
            clogVO.setDef02("员工统一账号验证码创建遇到的错误, msg：" + e.getMessage());
            clogVO.setRestcode("500");
            clogVO.setMessage("员工统一账号验证码创建遇到的错误, msg：" + e.getMessage());
            clogVO.setThwmsg(StringUtil.toString(e));
            logUtil.exception(e);
            YonLogUtil.debug(folderName, "员工统一账号验证码创建遇到的错误, msg：" + e.getMessage());
            Logger.error(e.getMessage());
            // JudgeAssertUtil.throwExceptionDir(e, folderName);
        }
        finally
        {
            ApiLogUtil.getInstance().updateLog(clogVO);
            YonLogUtil.debug(folderName, LoggerUtil.getSplitLine());
        }
        
        return clogVO;
    }
    
    @Override
    public ClogVO authPassword(final String userName, final String password, final String verificationCode, final String verifyRequestNo,
            final String clientIp)
    {
        final Map<String, String> configInfo = getConfigInfo();
        ClogVO clogVO = ApiLogUtil.newInitCLog();
        try
        {
            clogVO.setDef05(verificationCode);
            clogVO.setToken(verifyRequestNo);
            clogVO.setSessionid(verifyRequestNo);
            clogVO.setCode(userName + PubEnvUtil.AT + System.currentTimeMillis());
            clogVO.setAction("authPassword");
            clogVO.setName("员工统一账号密码认证");
            clogVO.setIp(clientIp);
            clogVO.setDatatype("authPassword");
            // 请求
            clogVO = ApiLogUtil.getInstance().insertSendLog(clogVO);
            // 同步调用，并获取请求结果
        }
        catch (Exception e)
        {
            clogVO.setDef01("9999");
            clogVO.setDef02("员工统一账号密码认证遇到的错误, msg：" + e.getMessage());
            clogVO.setRestcode("500");
            clogVO.setMessage("员工统一账号密码认证遇到的错误, msg：" + e.getMessage());
            clogVO.setThwmsg(StringUtil.toString(e));
            logUtil.exception(e);
            YonLogUtil.debug(folderName, "员工统一账号密码认证遇到的错误, msg：" + e.getMessage());
            Logger.error(e.getMessage());
            // JudgeAssertUtil.throwExceptionDir(e, folderName);
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
     * *说明： C/S 訪問同意認知,處理響應結果<br>
     *
     * @param resp
     * @param clogVO
     * @void <br>
     * @methods handleRest<br>
     * @author LiBencheng <br>
     * @date Created on 2024年1月22日 <br>
     * @time 13:48:39 <br>
     * @version 1.0 <br>
     *          ************************************************************ <br>
     * @see <br>
     */
    void handleRest(Map<String, Object> resp, ClogVO clogVO)
    {
        clogVO.setResponse(JsonUtil.compressJson(JsonUtil.toJSONString(resp)));
        if (PubEnvUtil.equals(InumberCons.ZERO, resp.get("return_code")))
        {
            clogVO.setDef01(StringUtil.valueOfEmpty(resp.get("return_code")));
            clogVO.setDef02(StringUtil.valueOfEmpty(resp.get("return_msg")));
            clogVO.setDef03(StringUtil.valueOfEmpty(resp.get("msg_id")));
            clogVO.setDef04(StringUtil.valueOfEmpty(resp.get("verificationCode")));
            clogVO.setMessage(StringUtil.valueOfEmpty(resp.get("return_msg")));
        }
        else
        {
            // 异常示例:{ "response_biz_content":{
            // "return_code":xxxxxx,"return_msg":"XXXXXX","msg_id":"urcnl24ciutr9"
            // },"sign":"FTGECGYUYTGREVCIKKIU" }
            Map map = (Map) resp.get("response_biz_content");
            clogVO.setRestcode("201");
            if (PubEnvUtil.isNotNullObj(map))
            {
                clogVO.setDef01(StringUtil.valueOfEmpty(map.get("return_code")));
                clogVO.setDef02(StringUtil.valueOfEmpty(map.get("return_msg")));
                clogVO.setDef03(StringUtil.valueOfEmpty(map.get("msg_id")));
                clogVO.setDef04(StringUtil.valueOfEmpty(map.get("verificationCode")));
                clogVO.setDef05(StringUtil.valueOfEmpty(resp.get("sign")));
                clogVO.setMessage(StringUtil.valueOfEmpty(resp.get("return_msg")));
            }
            else
            {
                clogVO.setDef01(StringUtil.valueOfEmpty(resp.get("return_code")));
                clogVO.setDef02(StringUtil.valueOfEmpty(resp.get("return_msg")));
                clogVO.setDef03(StringUtil.valueOfEmpty(resp.get("error_msg")));
                clogVO.setDef04(StringUtil.valueOfEmpty(resp.get("error_code")));
                clogVO.setDef05(StringUtil.valueOfEmpty(resp.get("verificationCode")));
                clogVO.setMessage(StringUtil.valueOfEmpty(resp.get("return_msg")));
            }
        }
    }
    
    /**
     * *********************************************************** <br>
     * *说明： 构建C/S 请求参数，data加密<br>
     *
     * @param dataParams
     * @void <br>
     * @methods buildParams <br>
     * @author LiBencheng <br>
     * @date Created on 2024年1月22日 <br>
     * @time 13:48:39 <br>
     * @version 1.0 <br>
     *          ************************************************************ <br>
     * @see <br>
     */
    Map buildParams(final Map<String, String> configInfo, final LinkedHashMap<String, String> dataParams)
    {
        return new LinkedHashMap<String, String>()
        {
            {
                PubEnvUtil.setValNotNON(this, "serviceName", configInfo.get("serviceName"));// 站点标识
                // PubEnvUtil.setValNotNON(this, "clientIp", configInfo.get("clientIp"));// 请求方ip或客户端ip
                // PubEnvUtil.setValNotNON(this, "serviceIp", configInfo.get("clientIp"));// 请求方ip或客户端ip
                putAll(dataParams);
                LinkedHashMap temp = new LinkedHashMap();
                temp.putAll(this);
                PubEnvUtil.setValNotNON(this, "tempData", JsonUtil.toJSONString(temp));
            }
        };
    }
    
    /**
     * *********************************************************** <br>
     * *说明： C/S 获取银行 DefaultInvoker <br>
     *
     * @param apiUrl
     * @param biz_content
     * @param clogVO
     * @void getDefaultInvoker<br>
     * @methods pers.bc.utils.yonyou.BackgroundWorkTaskPlugin#runing <br>
     * @author LiBencheng <br>
     * @date Created on 2024年1月22日 <br>
     * @time 13:48:39 <br>
     * @version 1.0 <br>
     *          ************************************************************ <br>
     * @see <br>
     */
    Object getDefaultInvoker(String apiUrl, Map<String, String> biz_content, ClogVO clogVO)
    {
        
        Object invoker = null;
        
        return invoker;
    }
    
    /**
     * *********************************************************** <br>
     * *说明： C/S 获取银行 RequestBodyInvoker <br>
     * 不好使，用getDefaultInvoker（）
     *
     * @param apiUrl
     * @param biz_content
     * @param clogVO
     * @void RequestBodyInvoker<br>
     * @methods pers.bc.utils.yonyou.BackgroundWorkTaskPlugin#runing <br>
     * @author LiBencheng <br>
     * @date Created on 2024年1月22日 <br>
     * @time 13:48:39 <br>
     * @version 1.0 <br>
     *          ************************************************************ <br>
     * @see <br>
     */
    Object getRequestBodyInvoker(String apiUrl, Map biz_content, ClogVO clogVO)
    {
        Object invoker = null;
        
        return invoker;
    }
    
    @Override
    public Map<String, String> getConfigInfo()
    {
        try
        {
            // if (properMap.isEmpty()) //
            properMap = PropertiesUtil.getAllProperties(getConfigFilePath());
        }
        catch (Exception e)
        {
            JudgeAssertUtil.throwExceptionDir(e, folderName);
        }
        
        return properMap;
    }
    
    protected String getConfigFilePath()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(RuntimeEnv.getInstance().getNCHome()).append(File.separator);
        sb.append("modules").append(File.separator);
        sb.append("tr").append(File.separator);
        sb.append("META-INF").append(File.separator);
        sb.append("ssosysteminfo.properties");
        
        return sb.toString();
    }
    
    protected String getIcbcConfig()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(RuntimeEnv.getInstance().getNCHome()).append(File.separator);
        sb.append("modules").append(File.separator);
        sb.append("tr").append(File.separator);
        sb.append("META-INF").append(File.separator);
        sb.append("icbcconfig");
        
        return sb.toString();
    }
}
