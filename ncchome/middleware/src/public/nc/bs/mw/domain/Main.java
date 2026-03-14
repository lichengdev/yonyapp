package nc.bs.mw.domain;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import nc.bs.mw.domain.info.DomainInfo;
import nc.bs.mw.domain.info.DomainInfoUtil;
import nc.bs.mw.domain.info.ProtocolInfo;
import nc.bs.mw.domain.info.ServerProcessInfo;
import nc.bs.mw.start.AloneBootstrap;

public class Main
{
    private static Timer timer;
    
    public static void main(final String[] args) throws Exception
    {
        final List<ManagedServerProcess> masterList = new ArrayList<ManagedServerProcess>();
        final List<ManagedServerProcess> serverList = new ArrayList<ManagedServerProcess>();
        String action = "start";
        final String domainInfoFile = relateToServer("ierp/bin/prop.xml");
        DomainInfo domainInfo = null;
        try
        {
            domainInfo = DomainInfoUtil.getDomainInfo(domainInfoFile);
        }
        catch (final Exception exp)
        {
            System.out.println("parse domain conf error: " + domainInfoFile + ", try to start standalone server");
            exp.printStackTrace();
        }
        if (args.length > 0)
        {
            action = args[0];
        }
        if (!"start".equalsIgnoreCase(action) && !"stop".equalsIgnoreCase(action))
        {
            usage();
            System.exit(0);
        }
        if ("start".equalsIgnoreCase(action))
        {
            System.out.println("the server will start....");
        }
        else
        {
            System.out.println("the server will stop....");
        }
        if (domainInfo != null)
        {
            System.out.println(action + " servers with domain configuraion");
            InetAddress[] localAddrs = null;
            try
            {
                final InetAddress localHost = InetAddress.getLocalHost();
                localAddrs = InetAddress.getAllByName(localHost.getHostName());
            }
            catch (final Exception exp2)
            {
                localAddrs = new InetAddress[0];
            }
            final Map<String, ServerProcessInfo> spInfoMap = DomainInfoUtil.indexServerProcessInfos(domainInfo);
            if (args.length > 1)
            {
                for (int i = 1; i < args.length; ++i)
                {
                    final ServerProcessInfo si = spInfoMap.get(args[i]);
                    final ManagedServerProcess managedProcess = new ManagedServerProcess(getJavaHome(domainInfo, si), si.getName(),
                        si.getJvmArgs(), si.getCommandArgs(), getCheckURL(si));
                    managedProcess.setClassPath(si.getClassPath());
                    if (si.isMaster() && checkAddress(getAddress(si), localAddrs))
                    {
                        masterList.add(managedProcess);
                    }
                    else if (checkAddress(getAddress(si), localAddrs))
                    {
                        serverList.add(managedProcess);
                    }
                    else
                    {
                        System.out
                            .println(si.getName() + " can't run at this server, " + "because it's address is not target at this server");
                    }
                }
            }
            else
            {
                for (final ServerProcessInfo si : spInfoMap.values())
                {
                    final ManagedServerProcess managedProcess = new ManagedServerProcess(getJavaHome(domainInfo, si), si.getName(),
                        si.getJvmArgs(), si.getCommandArgs(), getCheckURL(si));
                    managedProcess.setClassPath(si.getClassPath());
                    if (si.isMaster() && checkAddress(getAddress(si), localAddrs))
                    {
                        masterList.add(managedProcess);
                    }
                    else
                    {
                        if (!checkAddress(getAddress(si), localAddrs))
                        {
                            continue;
                        }
                        serverList.add(managedProcess);
                    }
                }
            }
        }
        else
        {
            System.out.println(action + " servers with no domain configuraion");
            String javaHome = System.getProperty("NC_JAVA_HOME");
            if (javaHome == null || javaHome.length() == 0)
            {
                javaHome = System.getProperty("JAVA_HOME");
                if (javaHome == null || javaHome.length() == 0)
                {
                    javaHome = System.getenv("JAVA_HOME");
                }
            }
            final ManagedServerProcess managedProcess2 = new ManagedServerProcess(javaHome, System.getProperty("nc.server.name"),
                System.getProperty("NC_JVM_ARGS"), System.getProperty("NC_CMD_ARGS"), (String) null);
            serverList.add(managedProcess2);
        }
        if (masterList.size() + serverList.size() == 1 && "false".equals(System.getProperty("nc.server.withDaemon")))
        {
            ManagedServerProcess process = null;
            if (masterList.size() == 1)
            {
                process = masterList.get(0);
            }
            else
            {
                process = serverList.get(0);
            }
            if (process.getServerName() != null)
            {
                System.setProperty("nc.server.name", process.getServerName());
            }
            System.out.println(action + " server: " + process.getServerName() + " as alone server");
            AloneBootstrap.main(args);
        }
        else if ("start".equalsIgnoreCase(action))
        {
            startManagedProcesses(masterList);
            try
            {
                Thread.sleep(500L);
            }
            catch (final Exception ex)
            {
            }
            startManagedProcesses(serverList);
            final MainProcessShutdownHook hook = new MainProcessShutdownHook();
            hook.addManagedServerProcesses((List) masterList);
            hook.addManagedServerProcesses((List) serverList);
            Runtime.getRuntime().addShutdownHook((Thread) hook);
        }
        else
        {
            stopManagedProcesses(serverList);
            stopManagedProcesses(masterList);
        }
    }
    
    private static void startManagedProcesses(final List<ManagedServerProcess> list)
    {
        if (Main.timer == null)
        {
            synchronized (Main.class)
            {
                if (Main.timer == null)
                {
                    Main.timer = new Timer("ServerGuard");
                }
            }
        }
        for (int i = 0; i < list.size(); ++i)
        {
            final ManagedServerProcess managedProcess = list.get(i);
            managedProcess.start();
        }
    }
    
    private static void stopManagedProcesses(final List<ManagedServerProcess> list)
    {
        System.out.println("stopManagedProcess.....");
        for (int i = 0; i < list.size(); ++i)
        {
            final ManagedServerProcess managedProcess = list.get(i);
            managedProcess.stop();
        }
    }
    
    private static void usage()
    {
        System.out.println("Usage: nc.bs.mw.start.Main <start|stop> server1 server2 ... ");
        System.out.println("or");
        System.out.println("     nc.bs.mw.start.Main");
    }
    
    private static String getAddress(final ServerProcessInfo info)
    {
        String address = null;
        if (info.getHttpProtocol() != null)
        {
            address = info.getHttpProtocol().getAddress();
            if (!isEmpty(address))
            {
                return address;
            }
        }
        if (info.getHttpsProtocol() != null)
        {
            address = info.getHttpsProtocol().getAddress();
            if (!isEmpty(address))
            {
                return address;
            }
        }
        if (info.getAjpProtocol() != null)
        {
            address = info.getAjpProtocol().getAddress();
            if (!isEmpty(address))
            {
                return address;
            }
        }
        if (isEmpty(address))
        {
            address = "127.0.0.1";
        }
        return address;
    }
    
    public static boolean isEmpty(final String str)
    {
        return str == null || "".equals(str);
    }
    
    private static boolean checkAddress(final String address, final InetAddress[] ias)
    {
        if (address == null || "127.0.0.1".equals(address) || "localhost".equals(address))
        {
            return true;
        }
        try
        {
            final InetAddress ia = InetAddress.getByName(address);
            for (int i = 0; i < ias.length; ++i)
            {
                if (ia.equals(ias[i]))
                {
                    return true;
                }
                if (ias[i].getHostAddress().equals(ia.getHostAddress()))
                {
                    return true;
                }
                final String hn1 = ias[i].getCanonicalHostName();
                final String hn2 = ia.getCanonicalHostName();
                if (hn1 != null && hn1.equals(hn2))
                {
                    return true;
                }
            }
        }
        catch (final UnknownHostException e)
        {
            return false;
        }
        return false;
    }
    
    private static String getJavaHome(final DomainInfo di, final ServerProcessInfo si)
    {
        String home = si.getJavaHome();
        if (home == null || "".equals(home))
        {
            home = di.getJavaHome();
        }
        if (home == null || "".equals(home))
        {
            home = System.getProperty("NC_JAVA_HOME");
        }
        return home;
    }
    
    private static String relateTo(final String home, final String path)
    {
        if (".".equals(home) || "./".equals(home) || home == null || path == null)
        {
            return path;
        }
        if (path.startsWith(home))
        {
            return path;
        }
        if (!path.startsWith("./"))
        {
            return path;
        }
        if (home.endsWith("/"))
        {
            return home + path.substring(2, path.length());
        }
        return home + "/" + path.substring(2, path.length());
    }
    
    private static String relateToServer(final String path)
    {
        return relateTo(System.getProperty("nc.server.location", "."), path);
    }
    
    private static String getCheckURL(final ServerProcessInfo si)
    {
        final StringBuffer sb = new StringBuffer();
        ProtocolInfo pis = si.getHttpProtocol();
        if (pis == null)
        {
            pis = si.getHttpsProtocol();
            sb.append("https://");
        }
        else
        {
            sb.append("http://");
        }
        String address = pis.getAddress();
        if (address == null || "".equals(address.trim()))
        {
            address = "localhost";
        }
        sb.append(address).append(":").append(pis.getPort());
        return sb.toString();
    }
    
    static class GuradManagedServerTask extends TimerTask
    {
        URL checkURL;
        ManagedServerProcess process;
        
        GuradManagedServerTask(final ManagedServerProcess process)
        {
            try
            {
                this.checkURL = new URL(process.checkURL);
            }
            catch (final Exception ex)
            {
            }
            this.process = process;
        }
        
        @Override
        public void run()
        {
            if (!this.check())
            {
                System.out.println("需要重新启动:" + this.process.getServerName());
                this.process.internalRestart();
            }
        }
        
        private boolean check()
        {
            if (this.checkURL == null)
            {
                return true;
            }
            InputStream in = null;
            int i;
            URLConnection url;
            for (i = 0, i = 0; i < 10; ++i)
            {
                try
                {
                    url = this.checkURL.openConnection();
                    url.setDoOutput(false);
                    url.setDoInput(true);
                    in = url.getInputStream();
                }
                catch (final IOException e)
                {
                    System.out.println("server available check:" + this.checkURL + " server:" + this.process.getServerName());
                    try
                    {
                        Thread.sleep(3000L);
                    }
                    catch (final Exception ex)
                    {
                    }
                }
                finally
                {
                    try
                    {
                        if (in != null)
                        {
                            in.close();
                            in = null;
                        }
                    }
                    catch (final Exception ex2)
                    {
                    }
                }
            }
            return i != 10;
        }
    }
}
