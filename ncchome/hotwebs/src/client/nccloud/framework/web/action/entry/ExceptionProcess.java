package nccloud.framework.web.action.entry;

import java.util.Iterator;
import java.util.List;

import nc.bs.framework.exception.FrameworkSecurityException;
import nc.bs.uif2.BusinessExceptionAdapter;
import nc.bs.uif2.LockFailedException;
import nc.bs.uif2.VersionConflictException;
import nc.bs.uif2.validation.ValidationException;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.util.bizlock.BizLockFailedException;
import nccloud.base.exception.BusinessTypeException;
import nccloud.commons.lang.StringUtils;
import nccloud.framework.core.exception.CarryException;
import nccloud.framework.core.exception.DuplicateLogInExcepton;
import nccloud.framework.core.exception.ExceptionUtils;
import nccloud.framework.core.exception.SecurityPubKeyLackException;
import nccloud.framework.core.exception.SessionInvalidateException;
import nccloud.framework.core.exception.StackPrint;
import nccloud.framework.core.exception.TransferException;
import nccloud.framework.core.exception.TransferSqlException;
import nccloud.framework.core.log.Log;
import nccloud.framework.web.container.SessionContext;
import nccloud.framework.web.convert.formula.MsgControlFormula;
import nccloud.framework.web.ui.meta.FormulaControlType;
import pers.bc.utils.pub.StringUtil;

public class ExceptionProcess
{
    private static final int CodeStatus = 1000;
    private static final String strategyErrorMsg = "sentinel error 4002 exceeds the maximum limit";
    
    public ExceptionProcess()
    {
    }
    
    public void process(Result result, Throwable ex)
    {
        ResultError error = this.handlerExeption(ex);
        
        result.setError(error);
        result.setData((Object) null);
        result.setSuccess(false);
    }
    
    private ResultError handlerBusinessException(BusinessException ex)
    {
        ResultError error = new ResultError();
        String msg = null;
        Log.error(ex);
        if (ex instanceof BizLockFailedException)
            msg = NCLangRes4VoTransl.getNCLangRes().getStrByID("1501003_0", "01501003-0021");
        else if (ex instanceof LockFailedException)
            msg = NCLangRes4VoTransl.getNCLangRes().getStrByID("1501003_0", "01501003-0022");
        else if (ex instanceof ValidationException)
            msg = ((ValidationException) ex).getMessage();
        else if (ex instanceof VersionConflictException)
            msg = NCLangRes4VoTransl.getNCLangRes().getStrByID("1501003_0", "01501003-0023");
        else
            msg = ex.getMessage();
        
        error.setMessage(msg);
        String stack = StackPrint.getInstance().printStack(ex);
        error.setStack(stack);
        error.setExceptionClass(ex.getClass().getName());
        String exceptionCode = ex.getErrorCodeString();
        if (!StringUtils.isEmpty(exceptionCode) && StringUtils.isNumeric(exceptionCode))
        {
            error.setStatus(Integer.valueOf(exceptionCode));
        }
        else
        {
            error.setStatus(1000);
        }
        
        return error;
    }
    
    private ResultError handlerExeption(Throwable ex)
    {
        Throwable cause = ExceptionUtils.unmarsh(ex);
        Log.error(cause);
        ResultError error = null;
        if (cause instanceof RuntimeException)
        {
            error = this.handlerRuntimeException((Exception) cause);
        }
        else if (cause instanceof BusinessException)
        {
            error = this.handlerBusinessException((BusinessException) cause);
        }
        else if (cause instanceof Error)
        {
            error = this.handleError((Error) cause);
        }
        else
        {
            error = this.handlerUnknownException((Exception) cause);
        }
        
        if (cause instanceof BusinessTypeException)
        {
            String type = ((BusinessTypeException) cause).getType();
            error.setType(type);
        }
        
        if (error.getMessage() != null && error.getMessage().contains("sentinel error 4002 exceeds the maximum limit")
            || error.getStack() != null && error.getStack().contains("sentinel error 4002 exceeds the maximum limit"))
        {
            error.setMessage("服务器繁忙，请稍后");
            error.setStack("违反限流规则：服务器繁忙，请稍后");
        }

        String msg = error.getMessage();
        if (!StringUtil.toString(msg).contains("👉")) msg = "👉" + msg;
        error.setMessage(msg);
            
        return error;
    }
    
    private ResultError handlerRuntimeException(Exception ex)
    {
        ResultError error = new ResultError();
        String stack;
        if (ex instanceof TransferSqlException)
        {
            TransferSqlException exception = (TransferSqlException) ex;
            StringBuilder builder = new StringBuilder();
            builder.append(exception.getSql());
            builder.append("\r\n");
            stack = StackPrint.getInstance().printStack(ex);
            builder.append(stack);
            error.setStack(builder.toString());
            error.setMessage(ex.getMessage());
            error.setExceptionClass(exception.getClass().getName());
            error.setStatus(1000);
        }
        else if (ex instanceof CarryException)
        {
            CarryException exception = (CarryException) ex;
            Object obj = exception.getObj();
            if (obj != null && obj instanceof ResultError)
            {
                return (ResultError) obj;
            }
            
            error.setStatus(1000);
            error.setMessage(NCLangRes4VoTransl.getNCLangRes().getStrByID("1501003_0", "01501003-0024"));
            stack = StackPrint.getInstance().printStack(ex);
            error.setStack(stack);
            error.setExceptionClass(ex.getClass().getName());
        }
        else
        {
            if (ex instanceof DuplicateLogInExcepton)
            {
                DuplicateLogInExcepton exception = (DuplicateLogInExcepton) ex;
                error.setMessage(exception.getMessage());
                stack = StackPrint.getInstance().printStack(ex);
                error.setStack(stack);
                error.setExceptionClass(exception.getClass().getName());
                error.setStatus(2001);
            }
            else if (ex instanceof SessionInvalidateException)
            {
                SessionInvalidateException exception = (SessionInvalidateException) ex;
                error.setMessage(exception.getMessage());
                stack = StackPrint.getInstance().printStack(ex);
                error.setStack(stack);
                error.setExceptionClass(exception.getClass().getName());
                error.setStatus(2002);
            }
            else if (ex instanceof SecurityPubKeyLackException)
            {
                SecurityPubKeyLackException exception = (SecurityPubKeyLackException) ex;
                error.setMessage(exception.getMessage());
                stack = StackPrint.getInstance().printStack(ex);
                error.setStack(stack);
                error.setExceptionClass(exception.getClass().getName());
                error.setStatus(2003);
            }
            else if (ex instanceof TransferException)
            {
                TransferException exception = (TransferException) ex;
                error.setMessage(exception.getMessage());
                stack = StackPrint.getInstance().printStack(ex);
                error.setStack(stack);
                error.setExceptionClass(exception.getClass().getName());
                error.setStatus(1000);
            }
            else if (ex instanceof FrameworkSecurityException)
            {
                error.setMessage(NCLangRes4VoTransl.getNCLangRes().getStrByID("1501003_0", "01501003-0025"));
                stack = StackPrint.getInstance().printStack(ex);
                error.setStack(stack);
                error.setExceptionClass(ex.getClass().getName());
                error.setStatus(1000);
            }
            else if (ex instanceof BusinessExceptionAdapter)
            {
                error = this.handlerBusinessException(((BusinessExceptionAdapter) ex).originalException);
            }
            else if (ex.getCause() != null)
            {
                Log.error(ex);
                if (ex.getCause() instanceof Exception)
                {
                    error = this.handlerExeption(ex.getCause());
                }
                else
                {
                    error = this.handlerUnknownException(ex);
                }
            }
            else
            {
                error = this.handlerUnknownException(ex);
            }
        }
        
        return error;
    }
    
    private ResultError handlerUnknownException(Exception ex)
    {
        ResultError error = new ResultError();
        String msg = null;
        Log.error(ex);
        List<MsgControlFormula> formula = SessionContext.getInstance().getFormulamsg();
        if (formula != null && formula.size() > 0)
        {
            StringBuffer sb = new StringBuffer();
            Iterator var6 = formula.iterator();
            
            while (var6.hasNext())
            {
                MsgControlFormula msgc = (MsgControlFormula) var6.next();
                if (msgc.getType().equals(FormulaControlType.$ERROR))
                {
                    sb.append(msgc.getMessage());
                }
            }
            
            if (sb.length() > 0)
            {
                msg = sb.toString();
            }
        }
        
        if (msg == null)
        {
            msg = ex.getMessage();
        }
        
        if (msg == null)
        {
            msg = NCLangRes4VoTransl.getNCLangRes().getStrByID("1501003_0", "01501003-0026");
        }
        
        error.setMessage(msg);
        // String stack = StackPrint.getInstance().printStack(ex);
        // error.setStack(stack);
        error.setExceptionClass(ex.getClass().getName());
        error.setStatus(1000);
        return error;
    }
    
    private ResultError handleError(Error cause)
    {
        ResultError error = new ResultError();
        Log.error(cause);
        error.setMessage(cause.getMessage());
        error.setStack(StackPrint.getInstance().printStack(cause));
        error.setExceptionClass(cause.getClass().getName());
        error.setStatus(1000);
        return error;
    }
}
