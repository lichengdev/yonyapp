package nc.bs.framework.server;

import java.lang.reflect.Method;
import nc.bs.framework.aop.Around;
import nc.bs.framework.aop.Aspect;
import nc.bs.framework.aop.Behavior;
import nc.bs.framework.aop.PatternType;
import nc.bs.framework.aop.Pointcut;
import nc.bs.framework.aop.ProceedingJoinpoint;
import nc.bs.framework.aop.rt.MethodProceedingJoinpoint;
import nc.bs.framework.aop.Behavior.Mode;
import nc.bs.framework.common.NoProtect;
import nc.bs.framework.comn.NetStreamContext;
import nc.bs.framework.exception.FrameworkSecurityException;
import nc.bs.logging.Logger;

@Aspect
public class SecurityAspect
{
    private ITokenProcessor tp;
    
    public SecurityAspect(ITokenProcessor tp)
    {
        this.tp = tp;
    }
    
    @Pointcut
    public boolean needProcess(Method m)
    {
        return m.getAnnotation(NoProtect.class) == null;
    }
    
    @Around(pointcut = "needProcess", patternType = PatternType.method)
    @Behavior(Mode.PERFLOW)
    public Object aroundMethod1(ProceedingJoinpoint pjp) throws Throwable
    {
        
        if (pjp instanceof MethodProceedingJoinpoint)
        {
            String methonName = ((MethodProceedingJoinpoint) pjp).getMethod().getName();
            
            if("".equals(methonName))
            {
                
            }
            
        }
        
        if (null == NetStreamContext.getToken())
            throw new FrameworkSecurityException("User session expired,please re login!(0) --> token is null");
        
        int status = this.tp.verifyToken(NetStreamContext.getToken());
        if (status == 9)
        {
            Logger.debug("current token check is off!");
        }
        else
        {
            Logger.debug("SecurityAspect is on! ");
            if (status == 0)
            {
                throw new FrameworkSecurityException("User session expired,please re login!(0) --> token is null");
            }
            
            if (status == 1)
            {
                throw new FrameworkSecurityException("User session expired,please re login!(1) --> token is too short");
            }
            
            if (status == 2)
            {
                throw new FrameworkSecurityException("User session expired,please re login!(2) --> token is not in cache");
            }
            
            if (status == 3)
            {
                throw new FrameworkSecurityException("User session expired,please re login!(3) --> token is overtime");
            }
            
            if (status == 4)
            {
                throw new FrameworkSecurityException("User session expired,please re login!(4) --> token is not the same");
            }
        }
        
        return pjp.proceed();
    }
}
