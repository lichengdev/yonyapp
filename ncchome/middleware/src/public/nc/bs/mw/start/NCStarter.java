package nc.bs.mw.start;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NCStarter
{
    static String CRLF = "\r\n";
    
    public NCStarter()
    {
    }
    
    public static void main(String[] args) throws Exception
    {
        // System.err.println(LoggerAbs.getYonYouStr().toString());
        // System.err.println(LoggerAbs.getSystemInfo().toString());
        // System.err.println(LoggerAbs.getYonYouCStr().toString());
        // System.err.println(LoggerAbs.getYonYouWenjian().toString());
        File f = new File(System.getProperty("user.dir", "."));
        f = new File(f, "work");
        clean(f);
        System.setProperty("nc.run.side", "server");
        System.setProperty("run.side", "server");
        String ncHome = System.getProperty("nc.server.location", ".");
        ClassLoader loader = BootstrapClassLoader.getBootstrapClassLoader(ncHome);
        Thread.currentThread().setContextClassLoader(loader);
        Class<?> mainClass = loader.loadClass("nc.bs.mw.domain.Main");
        Method method = mainClass.getMethod("main", String.class);
        method.invoke((Object) null, args);
    }
    
    private static void clean(File dir)
    {
        if (dir.exists())
        {
            List<File> dirs = new ArrayList();
            File[] files = dir.listFiles(new YonYou1234(dirs));
            File[] var6 = files;
            int var5 = files.length;
            
            File f;
            for (int var4 = 0; var4 < var5; ++var4)
            {
                f = var6[var4];
                f.delete();
            }
            
            Iterator var7 = dirs.iterator();
            
            while (var7.hasNext())
            {
                f = (File) var7.next();
                clean(f);
            }
            
        }
    }
    
}

class YonYou1234 implements FileFilter
{
    public YonYou1234(List var1)
    {
        this.dirs = var1;
    }
    
    List dirs;
    
    public boolean accept(File f)
    {
        if (f.isDirectory())
        {
            this.dirs.add(f);
            return false;
        }
        else
        {
            return f.isFile() && ("tldCache.ser".equals(f.getName()) || "SESSIONS.ser".equals(f.getName()));
        }
    }
}
