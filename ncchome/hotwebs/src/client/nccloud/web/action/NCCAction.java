package nccloud.web.action;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import nc.bs.framework.exception.FrameworkSecurityException;
import nc.bs.logging.Logger;
import nc.bs.uif2.BusinessExceptionAdapter;
import nc.bs.uif2.LockFailedException;
import nc.bs.uif2.VersionConflictException;
import nc.bs.uif2.validation.ValidationException;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.util.bizlock.BizLockFailedException;
import nccloud.framework.core.exception.ExceptionUtils;
import nccloud.framework.web.action.itf.ICommonAction;
import nccloud.framework.web.container.ClientInfo;
import nccloud.framework.web.container.IRequest;
import nccloud.framework.web.container.SessionContext;
import nccloud.framework.web.json.JsonFactory;
import pers.bc.utils.file.FileUtilbc;
import pers.bc.utils.pub.CollectionUtil;
import pers.bc.utils.pub.JsonUtil;
import pers.bc.utils.pub.JudgeAssertUtil;
import pers.bc.utils.pub.LoggerUtil;
import pers.bc.utils.pub.PropertiesUtil;
import pers.bc.utils.pub.StringUtil;

import org.apache.commons.lang3.StringUtils;

public abstract class NCCAction implements ICommonAction
{
    private String strMdOperateCode = null;
    private String strOperateCode = null;
    private String strResourceCode = null;
    
    protected void checkDataPermission(IRequest request, Object objData) throws Exception
    {
        DataPermissionAction dataPermissionAction = new DataPermissionAction();
        ValidationException validException =
            dataPermissionAction.checkDataPermission(this.getResourceCode(), this.getMdOperateCode(), this.getOperateCode(), objData);
        dataPermissionAction.dealValidationException(validException);
    }
    
    public final Object doAction(IRequest request)
    {
        Logger.debug("begin action-->" + this.getClass().getName() + ".doAction()");
        Object para = null;
        Object result = null;
        
        String actionInfo = StringUtil.valueOfEmpty(getServerConfig().get("actionInfo"));
        String actionThrow = StringUtil.valueOfEmpty(getServerConfig().get("actionThrow"));
        String actions = StringUtil.valueOfEmpty(getServerConfig().get("actions"));
        
        try
        {
            para = this.getPara(request, this.getParaClass());
            if (!this.doBeforeAction(request, para))
            {
                Object var4 = result;
                return var4;
            }
            
            result = this.execute(request, para); 
            this.doAfterSuccess(request, para);
        }
        catch (Exception e)
        {
            LoggerUtil loggerUtil = LoggerUtil.getInstance("actionlogs");
            ClientInfo cInfo = SessionContext.getInstance().getClientInfo();
            String msg = String.format("当前活动action：%s，clientip：%s，userid：%s@%s，\r\nJSONc串：<%s>，\r\n异常信息：<%s>", getClass().getName(),
                cInfo.getClientip(), cInfo.getUserid(), cInfo.getUsercode(), JsonUtil.compressJson(request.getJson()), e.getMessage());
            Logger.debug(msg);
            loggerUtil.info(msg);
            loggerUtil.info(getClass().getName()+"👊👊👊👉param -->" + JsonUtil.compressJson(JsonUtil.toJSONString(para)));
            loggerUtil.info(LoggerUtil.getSplitLine2());
            this.doAfterFailure(request, para);
            this.handleException(e);
        }
        finally
        {
            Logger.debug("end action-->" + this.getClass().getName() + ".doAction()");
        }
        
        return result;
    }
    
    protected <T> void doAfterFailure(IRequest request, T para)
    {
        Logger.debug("Failure action-->" + this.getClass().getName() + ".doAction()");
        Logger.error("Failure action-->" + this.getClass().getName() + ".doAction()");
    }
    
    protected <T> void doAfterSuccess(IRequest request, T para) throws Exception
    {
        Logger.debug("Success action-->" + this.getClass().getName() + ".doAction()");
        Logger.error("Success action-->" + this.getClass().getName() + ".doAction()");
    }
    
    protected <T> boolean doBeforeAction(IRequest request, T para) throws Exception
    {
        return true;
    }
    
    public abstract <T> Object execute(IRequest var1, T var2) throws Exception;
    
    public final String getMdOperateCode()
    {
        return this.strMdOperateCode;
    }
    
    public final String getOperateCode()
    {
        return this.strOperateCode;
    }
    
    public <T> T getPara(IRequest request, Class paraClass)
    {
        String strRead = request.read();
        T para = JsonFactory.create().fromJson(strRead, paraClass);
        return para;
    }
    
    protected Class getParaClass()
    {
        return HashMap.class;
    }
    
    public final String getResourceCode()
    {
        return this.strResourceCode;
    }
    
    protected void handleBusinessException(BusinessException ex)
    {
        String strMsg = null;
        if (ex instanceof BizLockFailedException)
            strMsg = NCLangRes4VoTransl.getNCLangRes().getStrByID("uif2", "DefaultExceptionHanler-000004");
        else if (ex instanceof LockFailedException)
            strMsg = NCLangRes4VoTransl.getNCLangRes().getStrByID("uif2", "DefaultExceptionHanler-000000");
        else if (ex instanceof ValidationException) strMsg = ((ValidationException) ex).getMessage();
        else if (ex instanceof VersionConflictException)
            strMsg = NCLangRes4VoTransl.getNCLangRes().getStrByID("uif2", "DefaultExceptionHanler-000002");
        else
            strMsg = ex.getMessage();
        
        // ExceptionUtils.wrapBusinessException(strMsg);
        JudgeAssertUtil.throwException(ex, "actionlogs" + File.separator + getClass().getSimpleName(), getClass().getName());
    }
    
    protected void handleException(Exception ex)
    {
        Throwable ex2 = ExceptionUtils.unmarsh(ex);
        Logger.error(
            "**************************************************** NCCAction Error ****************************************************");
        Logger.error(ex2.getMessage(), ex);
        if (ex2 instanceof RuntimeException) this.handleRuntimeException((RuntimeException) ex2);
        else if (ex2 instanceof BusinessException) this.handleBusinessException((BusinessException) ex2);
        else
            this.handleUnknownException(ex2);
        
    }
    
    protected void handleRuntimeException(RuntimeException ex)
    {
        if (ex instanceof FrameworkSecurityException)
            ExceptionUtils.wrapBusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("uif2", "DefaultExceptionHanler-000006"));
        else if (ex instanceof BusinessExceptionAdapter) this.handleBusinessException(((BusinessExceptionAdapter) ex).originalException);
        else if (!(ex instanceof nccloud.base.exception.BusinessException)
            && !(ex instanceof nccloud.framework.core.exception.BusinessException)) this.handleUnknownException(ex);
        else
            this.handleUnknownException(ex);
    }
    
    protected void handleUnknownException(Throwable ex)
    {
        String strMsg = ex.getMessage();
        if (StringUtils.isBlank(strMsg)) strMsg = NCLangRes4VoTransl.getNCLangRes().getStrByID("uif2", "DefaultExceptionHanler-000001");
        
        BusinessException business = new BusinessException(strMsg);
        business.setStackTrace(ex.getStackTrace());
        // ExceptionUtils.wrapException(business);
        
        JudgeAssertUtil.throwException(ex, "actionlogs" + File.separator + getClass().getSimpleName(), getClass().getName());
    }
    
    public void setMdOperateCode(String mdOperateCode)
    {
        this.strMdOperateCode = mdOperateCode;
    }
    
    public void setOperateCode(String operateCode)
    {
        this.strOperateCode = operateCode;
    }
    
    public void setResourceCode(String resourceCode)
    {
        this.strResourceCode = resourceCode;
    }
    
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
    
}
