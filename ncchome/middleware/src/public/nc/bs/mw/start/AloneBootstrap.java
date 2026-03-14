package nc.bs.mw.start;

import java.lang.reflect.Method;

public class AloneBootstrap
{
    static String CRLF = "\r\n";
    
    public AloneBootstrap()
    {
    }
    
    public static void main(String[] args) throws Exception
    {
        // System.err.println(LoggerAbs.getYonYouStr().toString());
        // System.err.println(LoggerAbs.getSystemInfo().toString());
        // System.err.println(LoggerAbs.getYonYouCStr().toString());
        // System.err.println(LoggerAbs.getYonYouWenjian().toString());
        // System.out.println("-----------------------------------------------------------");
        System.setProperty("nc.run.side", "server");
        System.setProperty("run.side", "server");
        String ncHome = System.getProperty("nc.server.location", ".");
        ClassLoader loader = BootstrapClassLoader.getBootstrapClassLoader(ncHome);
        Thread.currentThread().setContextClassLoader(loader);
        Class<?> mainClass = loader.loadClass("nc.bs.mw.fm.ServiceManagerDaemon");
        Method method = mainClass.getMethod("main", String[].class);
        method.invoke((Object) null, args);
        
    }
    
}
