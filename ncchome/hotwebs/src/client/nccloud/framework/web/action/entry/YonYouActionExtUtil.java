package nccloud.framework.web.action.entry;

import java.io.File;
import java.util.Map;

import nccloud.framework.web.action.excutor.IActionExcutor;
import nccloud.framework.web.container.ClientInfo;
import nccloud.framework.web.container.IHttpOperator;
import nccloud.framework.web.container.SessionContext;
import pers.bc.utils.file.FileUtilbc;
import pers.bc.utils.pub.CollectionUtil;
import pers.bc.utils.pub.JsonUtil;
import pers.bc.utils.pub.JudgeAssertUtil;
import pers.bc.utils.pub.LoggerUtil;
import pers.bc.utils.pub.PropertiesUtil;
import pers.bc.utils.pub.PubEnvUtil;
import pers.bc.utils.pub.StringUtil;

public class YonYouActionExtUtil
{
    @Deprecated
    private YonYouActionExtUtil(){}  
    
    private static Map<String, String> serverConfig = CollectionUtil.newHashMap();
    
    public static Map<String, String> getServerConfig()
    {
        if (serverConfig.isEmpty()) try
        {
            String patch = FileUtilbc.getWorkServerPath() + File.separator + "resources" + File.separator + "server.properties";
            serverConfig = PropertiesUtil.getAllProperties(patch);
        }
        catch (Exception e)
        {
            
        }
        
        return serverConfig;
    }
    
    public Object excute(IActionExcutor excutor, Object instance, IHttpOperator operator)
    {
        Object object = null;
        
        String actionInfo = StringUtil.valueOfEmpty(getServerConfig().get("actionInfo"));
        String actionThrow = StringUtil.valueOfEmpty(getServerConfig().get("actionThrow"));
        String actions = StringUtil.valueOfEmpty(getServerConfig().get("actions"));
        
        try
        {
            if (PubEnvUtil.equals("true", actionInfo.toLowerCase()) || PubEnvUtil.equals("y", actionInfo.toLowerCase()))
            {
                if (!PubEnvUtil.containStr(excutor.getClass().getName(), actions.split(PubEnvUtil.COMMA)))
                    LoggerUtil.getInstance("actionlogs")
                        .info("当前活动action：" + instance.getClass().getName() + "    "
                            + JsonUtil.compressJson(JsonUtil.toJSONString(operator.getRequest().read())) + " \r\n Json串：<"
                            + JsonUtil.compressJson(operator.getRequest().getJson() + ">"));
            }
        }
        catch (Exception e)
        {
            
        }
        
        if (PubEnvUtil.isEmpty(actionThrow) || PubEnvUtil.equals("true", actionThrow.toLowerCase())
            || PubEnvUtil.equals("y", actionThrow.toLowerCase()))
        {
            try
            {
                object = excutor.excute(instance, operator);
            }
            catch (Throwable e)
            {
                ClientInfo cInfo = SessionContext.getInstance().getClientInfo();
                String msg = String.format("当前活动action：%s，clientip：%s，userid：%s@%s，\r\nJSONc串：<%s>，\r\n异常信息：<%s>",
                    instance.getClass().getName(), cInfo.getClientip(), cInfo.getUserid(), cInfo.getUsercode(),
                    JsonUtil.compressJson(operator.getRequest().getJson()), e.getMessage());
                LoggerUtil.getInstance("actionlogs").info(msg);
                JudgeAssertUtil.throwException(e, "actionlogs" + File.separator + instance.getClass().getSimpleName(),
                    instance.getClass().getName());
            }
        }
        else
            object = excutor.excute(instance, operator);
        
        return object;
    }
    
    
    public static YonYouActionExtUtil getLocalThread()   {  return extUtil.get();  }
    
    private static ThreadLocal<YonYouActionExtUtil> extUtil = new ThreadLocal<YonYouActionExtUtil>()
    {   protected YonYouActionExtUtil initialValue()  {  return new YonYouActionExtUtil();  }; };

    public static YonYouActionExtUtil getInstance()  { return InnerInstance.extUtil;  }
    
    private static class InnerInstance
    {
        
        private static volatile YonYouActionExtUtil extUtil = new YonYouActionExtUtil();
        
        // 懶漢式
        public static YonYouActionExtUtil getInstance()
        {
            if (null == extUtil)
                synchronized (InnerInstance.class)
                {
                    if (null == extUtil)
                        extUtil = new YonYouActionExtUtil();
                }
            
            return extUtil;
        }
    }
    
}
