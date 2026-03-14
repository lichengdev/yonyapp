package nc.impl.codefactory.codecreater;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

import nc.codeplatform.framework.commons.Manifest;
import nc.vo.ml.NCLangRes4VoTransl;

public class ICAHandler
{
    static Logger LOG;
    private String currectVersion;
    private boolean success;
    private String message;
    
    static
    {
        ICAHandler.LOG = Logger.getLogger(ICAHandler.class.getName());
    }
    
    public ICAHandler(final String currectVersion)
    {
        this.currectVersion = Manifest.VERSION_DEF;
        this.currectVersion = currectVersion;
    }
    
    public void handler()
    {
        try
        {
            this.handler0();
        }
        catch (final Throwable e)
        {
            this.success = false;
            this.message = NCLangRes4VoTransl.getNCLangRes().getStrByID("codecreater", "0codecreater0078");
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(out));
            ICAHandler.LOG.warning(out.toString());
        }
    }
    
    private void handler0()
    {
        this.success = true;
        return;
//        final Date now = new Date();
//        if (!"NCC2111".equals(this.currectVersion))
//        {
//            if ("NCC2105".equals(this.currectVersion) || "NCC2005".equals(this.currectVersion))
//            {
//                final Calendar cal = Calendar.getInstance();
//                cal.set(1, 2021);
//                cal.set(2, 9);
//                cal.set(5, 14);
//                cal.set(11, 0);
//                cal.set(12, 0);
//                cal.set(13, 0);
//                cal.set(14, 0);
//                final Date startDate = cal.getTime();
//                cal.set(1, 2025);
//                cal.set(2, 2);
//                cal.set(5, 31);
//                cal.set(11, 23);
//                cal.set(12, 59);
//                cal.set(13, 59);
//                final Date endDate = cal.getTime();
//                if (startDate.getTime() <= now.getTime() && now.getTime() < endDate.getTime())
//                {
//                    this.success = true;
//                }
//                else
//                {
//                    this.success = false;
//                    this.message = NCLangRes4VoTransl.getNCLangRes().getStrByID("codecreater", "0codecreater0082");
//                }
//            }
//            else
//            {
//                this.success = false;
//                this.message = NCLangRes4VoTransl.getNCLangRes().getStrByID("codecreater", "0codecreater0081");
//            }
//            return;
//        }
//        final ICilService sercvice = (ICilService) NCLocator.getInstance().lookup((Class) ICilService.class);
//        final Integer pvalue = sercvice.getAllProducts().get("1088CODEFAC");
//        if (pvalue != null && pvalue == 1)
//        {
//            this.success = true;
//            return;
//        }
//        final Integer tempValue = sercvice.getAllProducts().get("*");
//        if (tempValue == null || tempValue != 50)
//        {
//            this.success = false;
//            this.message = NCLangRes4VoTransl.getNCLangRes().getStrByID("codecreater", "0codecreater0079");
//            return;
//        }
//        final Date endDate2 = sercvice.getEndDate();
//        if (now.getTime() < DateTimeUtil.getEndDate(endDate2).getTime())
//        {
//            this.success = true;
//            return;
//        }
//        this.success = false;
//        this.message = NCLangRes4VoTransl.getNCLangRes().getStrByID("codecreater", "0codecreater0079");
    }
    
    public boolean isSuccess()
    {
        return this.success;
    }
    
    public String getMessage()
    {
        return this.message;
    }
}
