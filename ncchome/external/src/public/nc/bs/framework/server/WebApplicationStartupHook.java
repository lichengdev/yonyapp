package nc.bs.framework.server;

import java.io.PrintStream;
import java.rmi.dgc.VMID;
import java.util.Properties;
import java.util.TimeZone;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.granite.log.GLog;

import nc.bs.framework.common.RuntimeEnv;
import nc.bs.logging.Log;
import nc.bs.logging.NCSysOutWrapper;
import nc.vo.pub.lang.Calendars;
import nc.vo.pub.lang.ICalendar;

public class WebApplicationStartupHook implements ServletContextListener
{
    private Log log;
    private PrintStream pw;
    String CRLF = "\r\n";
    private final String SPLIT_LINE = "📌✄—————————(◕‿◕✿)💞ꦿ 完美分割线❤split line ❀ ———————————————" + CRLF;
    private final String splitline =
        "👉🏻————ꕥ😍🥰😘(◡‿◡✿)༺ཌ༈魑魅魍魉༈ད༻ ꧁ღ❦ꦿ勇敢的少年啊快去创造奇迹এ⁵²ºஐఌ꧂ 🌲🚔🚺🏃🏼‍♂️❤————" + "🖥⌨️🖱☎💺👨🏻‍🍳✦💞ꦿ兲檤絒懄এ⁵²º❣🕊️--༊⁵²º⅓¼ " + CRLF//
            + "👇🏻´¯`•´`•.¸¸.•´´༺o༻`••.¸¸.•´´¯`•´ꕥꦿ";
    
    public WebApplicationStartupHook()
    {
        this.log = Log.getInstance((Class) WebApplicationStartupHook.class);
    }
    
    public void contextInitialized(final ServletContextEvent event)
    {
        try
        {
            Calendars.setGMTDefault(ICalendar.BASE_TIMEZONE);
            TimeZone.setDefault(Calendars.getGMTDefault());
            this.pw = System.out;
            if (this.pw instanceof NCSysOutWrapper)
            {
                this.pw = ((NCSysOutWrapper) this.pw).getSysStream();
            }
            else
            {
                System.setOut((PrintStream) new NCSysOutWrapper(this.pw, true));
                System.setErr((PrintStream) new NCSysOutWrapper(System.err, false));
            }
            final long now = System.currentTimeMillis();
            // this.pw.println("ESA Server starting ");
            this.pw.println("ESA Server starting ^_^");
            this.log.debug((Object) "WebApplicationStartupHook starting");
            RuntimeEnv.getInstance().setProperty("VMID", new VMID().toString());
            RuntimeEnv.getInstance().setRunningInServer(true);
            final BusinessAppServer appServer = BusinessAppServer.getInstance();
            final Properties props = new Properties();
            props.put("ServletContext", event.getServletContext());
            this.pw.println("appServer init(props)");
            appServer.init(props);
            this.pw.println("appServer start ");
            appServer.start();
            this.log.debug((Object) "WebApplicationStartupHook started");
            try
            {
                appServer.getContext().lookup("java:comp/env/ejb/nc.itf.framework.ejb.CMTProxy");
                appServer.getContext().lookup("java:comp/env/ejb/nc.itf.framework.ejb.BMTProxy");
            }
            catch (final Exception e)
            {
                this.pw.println(" not support ejb except ");
                this.log.error((Object) " not support ejb except");
            }
            this.pw.println("ESA Server started: " + (System.currentTimeMillis() - now) + CRLF);
            this.pw.println(splitline);
        }
        catch (final Exception e2)
        {
            this.log.error((Object) "startup error", (Throwable) e2);
        }
    }
    
    public void contextDestroyed(final ServletContextEvent event)
    {
        if (this.pw != null)
        {
            this.pw.println("ESA Server stopping <_<...");
        }
        this.log.debug((Object) "begin stop business application");
        final BusinessAppServer appServer = BusinessAppServer.getInstance();
        try
        {
            appServer.stop();
        }
        catch (final Exception e)
        {
            this.log.error((Object) e.getMessage(), (Throwable) e);
        }
        appServer.destroy();
        this.log.debug((Object) "end stop budiness application");
        if (this.pw != null)
        {
            this.pw.println("ESA Server stopped ");
        }
        System.setOut(this.pw);
    }
    
    static
    {
        try
        {
            GLog.setLoggerClass((Class) Class.forName("nc.bs.logging.impl.log4j.JdkLoggerAdapter"));
        }
        catch (final Exception ex)
        {
        }
    }
}
