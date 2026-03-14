package nc.bs.mw.fm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import nc.bs.logging.Log;

public class ServiceManagerDaemon
{
    private Log log = Log.getInstance(ServiceManagerDaemon.class);
    private File baseDir;
    private String serverName;
    private final ServiceManager sm;
    static String CRLF = "\r\n";
    
    public ServiceManagerDaemon(File baseDir, String serverName)
    {
        this.baseDir = baseDir;
        this.serverName = serverName;
        this.sm = new ServiceManager(baseDir, serverName);
    }
    
    public static void main(String[] args)
    {
        // System.out.println(LoggerAbs.getYonYouStr().toString());
        // System.out.println(LoggerAbs.getSystemInfo().toString());
        // System.out.println(LoggerAbs.getYonYouCStr().toString());
        // System.out.println(LoggerAbs.getYonYouWenjian().toString());
        
        System.err.println(LoggerAbs.getXDDStr());
        System.err.println(LoggerAbs.getJVMInfo());
        System.err.println(LoggerAbs.getYonYouStr().toString());
        System.err.println(LoggerAbs.getYonYouCStr().toString());
        System.err.println(LoggerAbs.getYonYouWenjian().toString());
        System.err.println(LoggerAbs.getSystemInfo());
        
        String serverName = System.getProperty("nc.server.name");
        if (serverName == null)
        {
            serverName = "server";
            System.setProperty("nc.server.name", serverName);
        }
        
        String ncHome = System.getProperty("nc.server.location", System.getProperty("user.dir"));
        ServiceManagerDaemon daemon = new ServiceManagerDaemon(new File(ncHome), serverName);
        if (args.length == 0)
        {
            daemon.start();
        }
        else if ("start".equals(args[0]))
        {
            daemon.start();
        }
        else if ("stop".equalsIgnoreCase(args[0]))
        {
            daemon.stopCommand();
        }
        
    }
    
    public void start()
    {
        try
        {
            this.sm.init();
        }
        catch (Exception var2)
        {
            this.log.error("service manager init error", var2);
            System.exit(-1);
        }
        
        this.sm.start();
    }
    
    public void stopCommand()
    {
        File fbase = new File(new File(this.baseDir, "work"), this.serverName);
        File pf = new File(fbase, "port");
        if (pf.exists())
        {
            FileInputStream fin = null;
            BufferedReader bin = null;
            
            try
            {
                fin = new FileInputStream(pf);
                bin = new BufferedReader(new InputStreamReader(fin));
                String s = bin.readLine();
                int port = 0;
                if (s != null)
                {
                    port = Integer.parseInt(s);
                }
                
                Socket socket = new Socket("localhost", port);
                socket.getOutputStream().write("SHUTDOWN".getBytes());
                socket.close();
            }
            catch (Exception var70)
            {
            }
            finally
            {
                try
                {
                    if (bin != null)
                    {
                        bin.close();
                    }
                }
                catch (IOException var69)
                {
                }
                finally
                {
                    if (fin != null)
                    {
                        try
                        {
                            fin.close();
                        }
                        catch (IOException var68)
                        {
                        }
                    }
                    
                }
                
            }
        }
        
    }
    
    public ServiceManager getServiceManager()
    {
        return this.sm;
    }
    
}
