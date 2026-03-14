package ufmiddle.start.tomcat;

import nc.bs.mw.fm.ServiceManagerDaemon;

public class StartDirectServer
{
    
    public static void main(String[] args)
    {
        System.setProperty("nc.run.side", "server");
        if (args.length > 0)
        {
            if (args[0].endsWith(".xml"))
            {
                System.setProperty("nc.server.prop", args[0]);
            }
            
            if (!"start".equals(args[0]) && !"stop".equals(args[0]))
            {
                args = new String[]{"start"};
            }
        }
        System.err.println("NC.Service启动中...");
        
        // System.err.println(LoggerAbs.getSystemInfo());
        // System.err.println(LoggerAbs.getYonYouStr().toString());
        // System.err.println(LoggerAbs.getYonYouCStr().toString());
        // System.err.println(LoggerAbs.getYonYouWenjian().toString());
        // System.err.println(LoggerAbs.getJVMInfo());
        // System.err.println(LoggerAbs.getXDDStr());
        
        ServiceManagerDaemon.main(args);
    }
    
}
