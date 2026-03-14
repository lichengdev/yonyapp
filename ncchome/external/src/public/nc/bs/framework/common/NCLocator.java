package nc.bs.framework.common;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import nc.bs.framework.exception.FrameworkRuntimeException;
import nc.bs.framework.naming.Context;
import nc.vo.jcom.util.ClassUtil;
import pers.bc.utils.pub.LoggerUtil;
import pers.bc.utils.pub.PubEnvUtil;

public abstract class NCLocator implements Context
{
    private static final RuntimeEnv runtimeEnv = RuntimeEnv.getInstance();
    public static final String SERVICEDISPATCH_URL = "SERVICEDISPATCH_URL";
    public static final String CLIENT_COMMUNICATOR = "CLIENT_COMMUNICATOR";
    public static final String TARGET_MODULE = "nc.targetModule";
    private static NCLocator DEFAULT_SERVER_LOCATOR = null;
    private static NCLocator DEFAULT_CLIENT_LOCATOR = null;
    private static Map<String, NCLocator> locatorMap = new ConcurrentHashMap();
    public static final String LOCATOR_PROVIDER_PROPERTY = "nc.locator.provider";
    private static final String CLIENT_LOCATOR = "nc.bs.framework.rmi.RmiNCLocator";
    private static final String SERVER_LOCATOR = "nc.bs.framework.server.ServerNCLocator";
    
    public NCLocator()
    {
    }
    
    public static NCLocator getInstance()
    {
        return getInstance((Properties) null);
    }
    
    public static NCLocator getInstance(Properties props)
    {
        NCLocator locator = null;
        String svcDispatchURL = getProperty(props, "SERVICEDISPATCH_URL");
        String locatorProvider = getProperty(props, "nc.locator.provider");
        String targetModule = getProperty(props, "nc.targetModule");
        String key = ":" + svcDispatchURL + ":" + locatorProvider + ":" + targetModule;
        locator = (NCLocator) locatorMap.get(key);
        if (locator != null)
        {
            return locator;
        }
        else
        {
            if (!isEmpty(locatorProvider))
            {
                locator = newInstance(locatorProvider);
            }
            else if (!isEmpty(svcDispatchURL))
            {
                locator = newInstance("nc.bs.framework.rmi.RmiNCLocator");
            }
            else
            {
                locator = getDefaultLocator();
            }
            
            locator.init(props);
            locatorMap.put(key, locator);
            return locator;
        }
    }
    
    private static NCLocator getDefaultLocator()
    {
        Class var0;
        if (RuntimeEnv.getInstance().isThreadRunningInServer())
        {
            if (DEFAULT_SERVER_LOCATOR == null)
            {
                var0 = NCLocator.class;
                synchronized (NCLocator.class)
                {
                    if (DEFAULT_SERVER_LOCATOR == null)
                    {
                        DEFAULT_SERVER_LOCATOR = newInstance("nc.bs.framework.server.ServerNCLocator");
                    }
                }
            }
            
            return DEFAULT_SERVER_LOCATOR;
        }
        else
        {
            if (DEFAULT_CLIENT_LOCATOR == null)
            {
                var0 = NCLocator.class;
                synchronized (NCLocator.class)
                {
                    if (DEFAULT_CLIENT_LOCATOR == null)
                    {
                        DEFAULT_CLIENT_LOCATOR = newInstance("nc.bs.framework.rmi.RmiNCLocator");
                    }
                }
            }
            
            return DEFAULT_CLIENT_LOCATOR;
        }
    }
    
    private static NCLocator newInstance(String name)
    {
        try
        {
            return (NCLocator) ClassUtil.loadClass(name).newInstance();
        }
        catch (Exception var2)
        {
            throw new FrameworkRuntimeException("Can't find the class: " + name);
        }
    }
    
    private static boolean isEmpty(String str)
    {
        return str == null || "".equals(str.trim());
    }
    
    protected abstract void init(Properties var1);
    
    protected static String getProperty(Properties env, String name)
    {
        String value = env == null ? null : env.getProperty(name);
        if (value == null)
        {
            value = InvocationInfoProxy.getInstance().getProperty(name);
            if (value == null)
            {
                value = runtimeEnv.getProperty(name);
            }
        }
        
        return value;
    }
    
    public <T> T lookup(Class<T> clazz)
    {
        // if (PubEnvUtil.containStr(clazz.getName(), new String[]{"**********"}))
        // System.err.println(clazz.getName());
        
        if (PubEnvUtil.equals("develop", System.getProperty("nc.runMode"))//
            && PubEnvUtil.equals("true", System.getProperty("nc.printlogs")))
        {
            String invokMethod = LoggerUtil.getInvokMethod(1) + "clazz_invoking��" + clazz.getName();
            LoggerUtil.getInstance("devLogs" + File.separator + "nclookLogs").info(invokMethod);
            System.err.println(invokMethod);
        }
        
        return (T) lookup(clazz.getName());
    }
    
}
