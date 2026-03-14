package nc.starter.client;

import nc.bs.logging.Logger;
import nc.bs.mw.fm.LoggerAbs;
import nc.starter.ui.NCLauncher;

public class NCStarterClient
{
    
    public static void main(String[] args)
    {
        org.springframework.util.StopWatch watch = new org.springframework.util.StopWatch();
        watch.start();
        long logTraceId = System.currentTimeMillis();
        try
        {
            System.err.println(LoggerAbs.getYonYouStr().toString());
            System.err.println(LoggerAbs.getSystemInfo());
            System.err.println(LoggerAbs.getYonYouCStr().toString());
            System.err.println(LoggerAbs.getJVMInfo());
            System.err.println(LoggerAbs.getXDDStr());
            System.err.println("NC重量端开始启动........");
            System.err.println("NCservice没有启动完，会等待一会儿请稍等。。。");
            System.setProperty("sun.swing.enableImprovedDragGesture", "");
            NCLauncher.main(args);
            watch.stop();
            // System.err.println(LoggerUtil.getBeautyWomanStr().toString());
            System.err.println(String.format("[{%s}][1/7]NC重量端启动,耗时:{%s}ms", logTraceId, watch.getLastTaskTimeMillis()));
            
        }
        catch (Throwable e)
        {
            Logger.error(e.getMessage(), e);
            // System.err.println(LoggerUtil.getDangerStr().toString());
            System.err.println(String.format("[{%s}][1/7]NC重量端启动异常：,耗时:{%s}ms\r\n异常：{%s}", logTraceId, watch.getLastTaskTimeMillis()));
        }
    }
    
}
