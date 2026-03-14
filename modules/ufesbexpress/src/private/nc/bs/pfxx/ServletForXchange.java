package nc.bs.pfxx;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ufida.eip.parser.ContextMessageListener;
import com.ufida.eip.parser.LargeXMLParser;

import nc.bs.framework.adaptor.IHttpServletAdaptor;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.component.RemoteProcessComponetFactory;
import nc.bs.framework.execute.ThreadFactoryManager;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.bs.logging.Logger;
import nc.bs.ml.NCLangResOnserver;
import nc.bs.pfxx.log.PfxxLogManager;
import nc.bs.pfxx.process.XChangeProcessor;
import nc.bs.pfxx.xxconfig.FileConfigInfoReadFacade;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.jcom.xml.XMLUtil;
import nc.vo.pfxx.exception.EnvInitException;
import nc.vo.pfxx.exception.FileConfigException;
import nc.vo.pfxx.exception.ISendResult;
import nc.vo.pfxx.pub.PfxxConstants;
import nc.vo.pfxx.util.BytesSource;
import nc.vo.pfxx.util.FileUtils;
import nc.vo.pfxx.util.InputStreamFileHelper;
import nc.vo.pfxx.util.PfxxUtils;
import nc.vo.pfxx.xxconfig.RequestParameter;
import nc.vo.pfxx.xxconfig.SysConfigInfo;
import nc.vo.pub.BusinessException;
import pers.bc.utils.constant.IPubCons;
import pers.bc.utils.file.FileUtilbc;
import pers.bc.utils.pub.CollectionUtil;
import pers.bc.utils.pub.JsonUtil;
import pers.bc.utils.pub.LoggerAbs;
import pers.bc.utils.pub.LoggerUtil;

/**
 * 信息交换平台SERVLET统一入口
 * 
 * @author cch
 * 
 */
public class ServletForXchange implements IHttpServletAdaptor
{
    
    private RemoteProcessComponetFactory factory = null;
    private static ThreadPoolExecutor threadPoolExecutor;
    
    private LoggerUtil logUtil = LoggerUtil.getInstance("webXChangelogs");
    
    /*
     * servlet通用处理接口
     * @see nc.bs.framework.adaptor.IHttpServletAdaptor#doAction(javax.servlet.http .HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        logUtil.info("请求处理开始......" + request);
        logUtil.info("请求处理开始......" + response);
        try
        {
            String filePatch = LoggerUtil.getWorkLogPath() + File.separator + "webXChangelogs" + File.separator + "tempxml" + File.separator
                + "webXChange" + System.currentTimeMillis() + ".xml";
            ServletInputStream inputStream = request.getInputStream();
            if (null != inputStream)
            {
                SysConfigInfo globalParameter = FileConfigInfoReadFacade.getGlobalParameter();
                String outputEncoding = globalParameter.getOutputEncoding();
                // tring xml = StreamUtil.InputStreamTOString(inputStream ,outputEncoding);
                
                final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                final byte[] data = new byte[4096];
                int count = -1;
                while ((count = inputStream.read(data, 0, 4096)) != -1)
                {
                    outStream.write(data, 0, count);
                }
                final byte[] byteArray = outStream.toByteArray();
                // FileUtilbc.close(new Closeable[]{in, outStream});
                String xml = new String(byteArray, outputEncoding);
                
                File file = new File(filePatch);
                if (!file.exists()) FileUtilbc.createFiles(filePatch);
                FileUtilbc.write(file, xml.replace(IPubCons.RN, IPubCons.EMPTY), outputEncoding);
                logUtil.debug(filePatch + logUtil.CRLF + "提取URL请求中的报文:" + logUtil.CRLF + xml);
            }
            RequestParameter requestParameter = ConfigInfoAnalyser.initRequestParameter(request);
            logUtil.info("提取URL请求中的报文:" + filePatch);
            logUtil.info("提取URL请求中的参数." + JsonUtil.transObj2Josn(CollectionUtil.transBean2Map(requestParameter)));
            logUtil.error(LoggerUtil.getInvokMethod(0, -1));
            
            ISecurityTokenCallback sc = NCLocator.getInstance().lookup(ISecurityTokenCallback.class);
            sc.token("NCSystem".getBytes(), "pfxx".getBytes());
            
            SysConfigInfo globalParameter = FileConfigInfoReadFacade.getGlobalParameter();
            if (globalParameter.isBigFileTransmission())
            {
                IXChangeContext xcContext = new XChangeContext();
                boolean bcompress = false; // 是否压缩流
                requestParameter = null;
                String inputStreamFileName = null;
                String clientAddress = null;
                FileInputStream fis = null;
                try
                {
                    // 取得请求地址
                    clientAddress = request.getRemoteAddr();
                    Logger.info("交换平台收到来自" + clientAddress + "传送数据请求,请求处理开始......");
                    logUtil.info("交换平台收到来自" + clientAddress + "传送数据请求,请求处理开始......");
                    //
                    Logger.info("校验发送方客户端地址");
                    logUtil.info("校验发送方客户端地址");
                    checkClientAddress(clientAddress, globalParameter);
                    //
                    Logger.info("提取URL请求中的参数.");
                    requestParameter = ConfigInfoAnalyser.initRequestParameter(request);
                    logUtil.info("提取URL请求中的参数." + pers.bc.utils.pub.StringUtil.toString(requestParameter));
                    
                    // 将请求参数设置到当前上下文中
                    ServletRequestManager.getCurrentContext().setRequestParameter(requestParameter);
                    //
                    requestParameter.setServerURL(request.getServerName() + ":" + request.getServerPort());
                    //
                    bcompress = requestParameter.isBcompress();
                    //
                    setResponseContentType(response);
                    //
                    Logger.info("将输入流转换为文档.");
                    logUtil.info("将输入流转换为文档.");
                    inputStreamFileName =
                        InputStreamFileHelper.getFileName(requestParameter.getGroupCode(), requestParameter.getFileName());
                    //
                    //
                    final File localFile = writeInputStreamToFile(request.getInputStream(), bcompress, clientAddress);
                    logUtil.info("准备初始化上下文......");
                    Logger.info("准备初始化上下文......");
                    LargeXMLParser p = new LargeXMLParser("ufinterface", "abcd");
                    p.setNotProcessChildren(true);
                    final ContextMessageListener ufCML = new ContextMessageListener();
                    logUtil.info("开始解析本地临时文件......" + localFile);
                    Logger.info("开始解析本地临时文件......" + localFile);
                    fis = new FileInputStream(localFile);
                    p.parse(fis, ufCML);
                    fis.close();
                    logUtil.info("开始解析Ufinterface头完成......" + ufCML.getUfinterface());
                    Logger.info("开始解析Ufinterface头完成......" + ufCML.getUfinterface());
                    //
                    newThreadDoAction(clientAddress, localFile, ufCML, requestParameter, inputStreamFileName,
                        requestParameter.getGroupCode());
                    ResponseMessage rm = xcContext.newResponseMessage();
                    rm.setResultCode(ISendResult.NO_ERROR_DEAL_SUCCEED);
                    rm.appendResultDescription(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pfxx",
                        "0pfxx0111")/* @res "大文件传输模式，文件已接收，请稍后在交换平台日志查询！" */);
                    xcContext.setFileSuccessfulProcess(true);
                }
                catch (EnvInitException e)
                {
                    String msg = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pfxx",
                        "0pfxx0112")/*
                                     * @res "交换环境初始化异常"
                                     */;
                    logUtil.exception(msg, e);
                    Logger.error(msg, e);
                    ResponseMessage rm = xcContext.newResponseMessage();
                    rm.setResultCode(e.getErrorCodeString());
                    rm.appendResultDescription(e.getMessage());
                    xcContext.setFileSuccessfulProcess(false);
                }
                catch (IOException ioe)
                {
                    Logger.error("从网络读取数据流出错!请检查网络状况，并重新发送数据!", ioe);
                    logUtil.exception("从网络读取数据流出错!请检查网络状况，并重新发送数据!", ioe);
                    ResponseMessage rm = xcContext.newResponseMessage();
                    rm.setResultCode(ISendResult.ERR_ENV_SERVLET);
                    rm.appendResultDescription(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pfxx",
                        "0pfxx0090")/* @res "从网络读取数据流出错!请检查网络状况，并重新发送数据!" */);
                    xcContext.setFileSuccessfulProcess(false);
                }
                catch (Exception e)
                {
                    Logger.error("未知错误", e);
                    logUtil.exception("未知错误", e);
                    ResponseMessage rm = xcContext.newResponseMessage();
                    rm.setResultCode(ISendResult.ERR_UNKOWN_EXCEPTION);
                    rm.appendResultDescription(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pfxx",
                        "0pfxx0113")/* @res "未知错误" */);
                    xcContext.setFileSuccessfulProcess(false);
                }
                finally
                {
                    IOUtils.closeQuietly(fis);
                    //
                    String outputEncoding = globalParameter.getOutputEncoding();
                    // 多语言需要
                    Writer writer = null;
                    if (bcompress) writer = new OutputStreamWriter(new GZIPOutputStream(response.getOutputStream()), outputEncoding);
                    else
                        writer = new OutputStreamWriter(response.getOutputStream(), outputEncoding);
                    Document doc = xcContext.getResponseMessage();
                    logUtil.info(LoggerUtil.getInvokMethod(1) + " 运行结束！返回信息：" + doc.toString());
                    logUtil.info(LoggerUtil.getInvokMethod(1) + " 运行结束！返回信息：" + doc.getTextContent());
                    
                    XMLUtil.printDOMTree(writer, doc, 0, outputEncoding);
                    writer.close();
                }
            }
            // 正常
            else
            {
                normalDoAction(request, response, globalParameter);
            }
        }
        catch (Throwable e)
        {
            Logger.error("读取信息交换平台全局配置文件出错", e);
            logUtil.exception("读取信息交换平台全局配置文件出错", e);
        }
        finally
        {
            logUtil.info(LoggerAbs.getSplitLine());
            logUtil.debug(LoggerAbs.getSplitLine());
        }
    }
    
    @SuppressWarnings("restriction")
    private void normalDoAction(HttpServletRequest request, HttpServletResponse response, SysConfigInfo globalParameter)
    {
        // 往线程注册上下文环境
        Logger.init("pfxx");
        IXChangeContext context = new XChangeContext();
        PfxxUtils.registerContext(context);
        boolean bcompress = false; // 是否压缩流
        RequestParameter requestParameter = null;
        String inputStreamFileName = null;
        String receiver = "Unknow";
        String clientAddress = null;
        try
        {
            clientAddress = request.getRemoteAddr();
            Logger.info("交换平台收到来自" + clientAddress + "传送数据请求,请求处理开始......");
            //
            Logger.info("校验发送方客户端地址");
            checkClientAddress(clientAddress, globalParameter);
            //
            Logger.info("提取URL请求中的参数.");
            requestParameter = ConfigInfoAnalyser.initRequestParameter(request);
            //
            ServletRequestManager.getCurrentContext().setRequestParameter(requestParameter);
            //
            requestParameter.setServerURL(request.getServerName() + ":" + request.getServerPort());
            receiver = requestParameter.getGroupCode();
            //
            bcompress = requestParameter.isBcompress();
            setResponseContentType(response);
            //
            logUtil.info("校验文件长度");
            Logger.info("校验文件长度");
            checkContentLength(request.getContentLength());
            //
            logUtil.info("将输入流转换为文档.");
            Logger.info("将输入流转换为文档.");
            inputStreamFileName = InputStreamFileHelper.getFileName(receiver, requestParameter.getFileName());
            Document doc = getDocumentFromInputStream(request.getInputStream(), bcompress, inputStreamFileName, globalParameter);
            
            logUtil.info("开始初始化上下文......");
            Logger.info("开始初始化上下文......");
            logUtil.info("信息内容......" + doc.getTextContent());
            context.init(requestParameter, doc);// 这块获取数据源
            Logger.info("初始化上下文完成!!!");
            logUtil.info("初始化上下文完成!!!");
            
            // 处理信息内容
            logUtil.info("开始处理文档......");
            Logger.info("开始处理文档......");
            // 读取控制参数，以决定是按一个文档一个事务发送还是每张单据起独立事务
            if (globalParameter.getDealRule() == PfxxConstants.DEALRULE_ALL_OR_NONE)
            {
                PfxxUtils.lookUpPFxxEJBService().processMessage(doc);
            }
            else
            {
                new XChangeProcessor().processMessage_Alone(doc);
            }
            Logger.info("处理文档完成!");
            logUtil.info("处理文档完成!");
            //
            logUtil.info("交换平台处理来自" + clientAddress + "传送的数据完成。");
            Logger.info("交换平台处理来自" + clientAddress + "传送的数据完成。");
        }
        catch (IOException ioe)
        {
            logUtil.exception("从网络读取数据流出错!请检查网络状况，并重新发送数据!", ioe);
            Logger.error("从网络读取数据流出错!请检查网络状况，并重新发送数据!", ioe);
            PfxxUtils.lookUpPFxxFileService().setInputStreamNetError(inputStreamFileName);
            ResponseMessage rm = context.newResponseMessage();
            rm.setResultCode(ISendResult.ERR_ENV_SERVLET);
            rm.appendResultDescription(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pfxx",
                "0pfxx0090")/*
                             * @res "从网络读取数据流出错!请检查网络状况，并重新发送数据!"
                             */);
            context.setFileSuccessfulProcess(false);
            // 同样需要在xlog记录日志，对整个输入流写一条记录
            PfxxLogManager.writeExceptionToXlog(context, receiver, clientAddress, ioe);
        }
        catch (SAXException saxe)
        {
            Logger.error("从输入流转换document出错,请检验文档格式!", saxe);
            logUtil.exception("从输入流转换document出错,请检验文档格式!", saxe);
            PfxxUtils.lookUpPFxxFileService().setInputStreamDocFormatError(inputStreamFileName);
            ResponseMessage rm = context.newResponseMessage();
            rm.setResultCode(ISendResult.ERR_PFXX_BILL_FORMAT_INVALID);
            rm.appendResultDescription(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pfxx",
                "0pfxx0114")/*
                             * @res "从输入流转换document出错,请检验文档格式!"
                             */);
            context.setFileSuccessfulProcess(false);
            // 同样需要在xlog记录日志，对整个输入流写一条记录
            PfxxLogManager.writeExceptionToXlog(context, receiver, clientAddress, saxe);
        }
        catch (EnvInitException e)
        { // 环境初始化异常, 以文件写回执
            logUtil.exception("交换平台初始化异常", e);
            Logger.error("交换平台初始化异常", e);
            PfxxUtils.lookUpPFxxFileService().setInputStreamBizError(inputStreamFileName);
            ResponseMessage rm = context.newResponseMessage();
            rm.setResultCode(e.getErrorCodeString());
            rm.appendResultDescription(e.getMessage());
            context.setFileSuccessfulProcess(false);
            // 同样需要在xlog记录日志，对整个输入流写一条记录
            PfxxLogManager.writeExceptionToXlog(context, receiver, clientAddress, e);
        }
        catch (BusinessException e)
        { // 非环境初始化异常, 以单据形式写回执
            logUtil.exception(e.getMessage(), e);
            Logger.error(e.getMessage(), e);
            PfxxUtils.lookUpPFxxFileService().setInputStreamBizError(inputStreamFileName);
            context.setFileSuccessfulProcess(false);
        }
        catch (Throwable e)
        { // 初始化过程中的其他未明异常
            logUtil.exception(e.getMessage(), e);
            Logger.error(e.getMessage(), e);
            PfxxUtils.lookUpPFxxFileService().setInputStreamBizError(inputStreamFileName);
            ResponseMessage rm = context.newResponseMessage();
            rm.setResultCode(ISendResult.ERR_UNKOWN_EXCEPTION);
            rm.appendResultDescription(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pfxx", "0pfxx0091")/*
                                                                                                                  * @res
                                                                                                                  * "初始化过程中的其他未明异常:"
                                                                                                                  */
                + e.getMessage());
            context.setFileSuccessfulProcess(false);
            // 同样需要在xlog记录日志，对整个输入流写一条记录
            PfxxLogManager.writeExceptionToXlog(context, receiver, clientAddress, e);
        }
        finally
        {
            // 处理大事务时单据号相关内存
            dealBillCode(context.isFileSuccessfulProcess());
            logUtil.info("开始发送回执......");
            Logger.info("开始发送回执......");
            sendBackMessage(response, bcompress);
            logUtil.info("发送回执结束!!!");
            Logger.info("发送回执结束!!!");
            PfxxUtils.releaseFormulaParser();
            PfxxUtils.releaseContext();
            // 释放所有锁资源
            // 2009-01-10 有可能释放其它线程的锁，导致锁失效
            // releaseAllLocks();
            // 日志
            Logger.reset();
        }
    }
    
    private void newThreadDoAction(final String clientAddress, final File localFile, final ContextMessageListener ufCML,
            final RequestParameter requestParameter, final String inputStreamFileName, final String receiver)
    {
        getThreadPoolExecutor().execute(new Runnable()
        {
            
            public void run()
            {
                // 往线程注册上下文环境
                Logger.init("pfxx");
                ISecurityTokenCallback sc = NCLocator.getInstance().lookup(ISecurityTokenCallback.class);
                sc.token("NCSystem".getBytes(), "pfxx".getBytes());
                IXChangeContext context = new XChangeContext();
                PfxxUtils.registerContext(context);
                try
                {
                    logUtil.info("开始初始化上下文......");
                    Logger.info("开始初始化上下文......");
                    context.init(requestParameter, ufCML.getUfinterface());
                    logUtil.info("初始化上下文完成!!!");
                    Logger.info("初始化上下文完成!!!");
                    try
                    {
                        // 处理信息内容
                        logUtil.info("开始处理文档...");
                        Logger.info("开始处理文档...");
                        // 读取控制参数，以决定是按一个文档一个事务发送还是每张单据起独立事务
                        if (FileConfigInfoReadFacade.getGlobalParameter().getDealRule() == PfxxConstants.DEALRULE_ALL_OR_NONE)
                        {
                            PfxxUtils.lookUpPFxxEJBService().processMessage(localFile.getAbsolutePath());
                        }
                        else
                        {
                            new XChangeProcessor().processMessage_Alone(localFile.getAbsolutePath());
                        }
                        Logger.info("处理文档完成!");
                        logUtil.info("处理文档完成!");
                    }
                    catch (FileConfigException e)
                    {
                        logUtil.exception(e.getMessage(), e);
                        Logger.error(e.getMessage(), e);
                        throw new EnvInitException(e);
                    }
                    if (!PfxxUtils.lookUpPFxxFileService().getGlobalParameter().isReserveTransTempFile())
                    {
                        localFile.delete();
                    }
                }
                catch (EnvInitException e)
                { // 环境初始化异常, 以文件写回执
                    PfxxUtils.lookUpPFxxFileService().setInputStreamBizError(inputStreamFileName);
                    ResponseMessage rm = context.newResponseMessage();
                    rm.setResultCode(e.getErrorCodeString());
                    rm.appendResultDescription(e.getMessage());
                    logUtil.exception("交换平台初始化异常", e);
                    Logger.error("交换平台初始化异常", e);
                    context.setFileSuccessfulProcess(false);
                    // 同样需要在xlog记录日志，对整个输入流写一条记录
                    PfxxLogManager.writeExceptionToXlog(context, receiver, clientAddress, e);
                }
                catch (BusinessException e)
                { // 非环境初始化异常, 以单据形式写回执
                    PfxxUtils.lookUpPFxxFileService().setInputStreamBizError(inputStreamFileName);
                    logUtil.exception(e.getMessage(), e);
                    Logger.error(e.getMessage(), e);
                    context.setFileSuccessfulProcess(false);
                }
                catch (Throwable e)
                { // 初始化过程中的其他未明异常
                    PfxxUtils.lookUpPFxxFileService().setInputStreamBizError(inputStreamFileName);
                    ResponseMessage rm = context.newResponseMessage();
                    rm.setResultCode(ISendResult.ERR_UNKOWN_EXCEPTION);
                    rm.appendResultDescription(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pfxx", "0pfxx0091")/*
                                                                                                                          * @res
                                                                                                                          * "初始化过程中的其他未明异常:"
                                                                                                                          */
                        + e.getMessage());
                    logUtil.exception(e.getMessage(), e);
                    Logger.error(e.getMessage(), e);
                    context.setFileSuccessfulProcess(false);
                    // 同样需要在xlog记录日志，对整个输入流写一条记录
                    PfxxLogManager.writeExceptionToXlog(context, receiver, clientAddress, e);
                }
                finally
                {
                    // 处理大事务时单据号相关内存
                    dealBillCode(context.isFileSuccessfulProcess());
                    logUtil.info("开始发送回执......");
                    Logger.info("开始发送回执......");
                    // sendBackMessage(response, bcompress);
                    logUtil.info("发送回执结束!!!");
                    Logger.info("发送回执结束!!!");
                    PfxxUtils.releaseFormulaParser();
                    PfxxUtils.releaseContext();
                    // 释放所有锁资源
                    // 2009-01-10 有可能释放其它线程的锁，导致锁失效
                    // releaseAllLocks();
                    // 日志
                    Logger.reset();
                }
                //
            }
        });
    }
    
    public synchronized ThreadPoolExecutor getThreadPoolExecutor()
    {
        if (threadPoolExecutor == null)
        {
            int max = 0;
            try
            {
                max = FileConfigInfoReadFacade.getGlobalParameter().getMaxThreadNumber();
            }
            catch (FileConfigException e)
            {
                Logger.error("读取配置文件出错", e);
            }
            if (max < 1)
            {
                max = 1;
            }
            threadPoolExecutor = new ThreadPoolExecutor(1, max, 3, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),
                ThreadFactoryManager.newThreadFactory(), new ThreadPoolExecutor.DiscardOldestPolicy());
        }
        return threadPoolExecutor;
    }
    
    /**
     * 发送回执信息
     * 
     * @param response
     * @bcompress 是否需要压缩,如果来的数据是压缩的，那么回执也可以压缩
     */
    private void sendBackMessage(HttpServletResponse response, boolean bcompress)
    {
        try
        {
            // 需要对回执文件进行压缩
            IXChangeContext context = PfxxUtils.getCurrentContext();
            Document doc = context.getResponseMessage();
            SysConfigInfo globalParameter = FileConfigInfoReadFacade.getGlobalParameter();
            String outputEncoding = globalParameter.getOutputEncoding();
            // 服务器本地备份
            if (globalParameter.isBackupResponses())
            {
                String filename = context.getFileName();
                PfxxUtils.lookUpPFxxFileService().backupResponse(FileUtils.getDocBinaryByteData(doc, outputEncoding), filename);
            }
            // 多语言需要
            Writer writer = null;
            if (bcompress) writer = new OutputStreamWriter(new GZIPOutputStream(response.getOutputStream()), outputEncoding);
            else
                writer = new OutputStreamWriter(response.getOutputStream(), outputEncoding);
            XMLUtil.printDOMTree(writer, doc, 0, outputEncoding);
            writer.close();
        }
        catch (Exception e)
        {
            Logger.error(e.getMessage(), e);
        }
    }
    
    /**
     * 从输入流得到文档
     * 
     * @param in
     * @param bcompress 是否压缩流
     * @param inputStreamFileName
     * @param globalParameter
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws EnvInitException
     * @deprecated
     */
    private Document getDocumentFromInputStream(InputStream in, boolean bcompress, String inputStreamFileName,
            SysConfigInfo globalParameter) throws IOException, SAXException, EnvInitException
    {
        Document doc = null;
        // 需要判断是否压缩流
        BytesSource byteSrc = null;
        try
        {
            InputStream input = in;
            if (bcompress)
            {
                input = new GZIPInputStream(in);
            }
            if (globalParameter.isRecordinputstream())
            {
                // 先转换本地byte流
                byteSrc = new BytesSource(input, false);
                // 解析流
                doc = XMLUtil.getDocumentBuilder().parse(byteSrc.getInputStream());
            }
            else
                // 解析流
                doc = XMLUtil.getDocumentBuilder().parse(input);
        }
        finally
        {
            // 写到服务器上
            if (byteSrc != null) PfxxUtils.lookUpPFxxFileService().writeInputStreamData(byteSrc.getData(), inputStreamFileName);
        }
        if (doc == null)
        {
            throw new EnvInitException(ISendResult.ERR_PFXX_BILL_FORMAT_INVALID, nc.bs.ml.NCLangResOnserver.getInstance().getStrByID("pfxx",
                "UPPpfxx-000045")/*
                                  * @res "收到空XML对象，源XML文件的文件内容或编码错误!"
                                  */);
        }
        return doc;
    }
    
    private File writeInputStreamToFile(InputStream in, boolean bcompress, String clientAddress) throws IOException
    {
        //
        File worktempDir = new File(PfxxConstants.WORK_TEMP_PATH);
        if (!worktempDir.exists()) worktempDir.mkdirs();
        if (clientAddress != null)
        {
            clientAddress = clientAddress.replace('.', '_');
        }
        File tmpFile = File.createTempFile(clientAddress + "_pfxxinput", ".xml", worktempDir);
        //
        Logger.info("创建流写入临时文件: " + tmpFile.getAbsolutePath());
        //
        FileOutputStream fos = null;
        try
        {
            InputStream input = in;
            if (bcompress)
            {
                input = new GZIPInputStream(in);
            }
            fos = new FileOutputStream(tmpFile);
            
            int length;
            byte[] buffer = new byte[1024];
            while ((length = input.read(buffer, 0, 1000)) != -1)
            {
                fos.write(buffer, 0, length);
            }
        }
        finally
        {
            // 写到服务器上
            if (fos != null) fos.close();
        }
        return tmpFile;
    }
    
    /**
     * 处理大事务时单据号相关内存
     * 
     * @param bExp
     */
    private void dealBillCode(boolean bExp)
    {
        RemoteProcessComponetFactory tmpfactory = getRemoteProcessFactory();
        if (tmpfactory != null)
        {
            if (bExp) getRemoteProcessFactory().postProcess();
            else
                getRemoteProcessFactory().postErrorProcess(null);
            getRemoteProcessFactory().clearThreadScopePostProcess();
        }
    }
    
    private RemoteProcessComponetFactory getRemoteProcessFactory()
    {
        if (factory != null) return factory;
        try
        {
            factory = (RemoteProcessComponetFactory) NCLocator.getInstance().lookup("RemoteProcessComponetFactory");
        }
        catch (Throwable throwable)
        {
            Logger.warn("RemoteCallPostProcess is not found");
        }
        return factory;
    }
    
    /**
     * 设置多语言环境
     * 
     * @param response
     */
    private void setResponseContentType(HttpServletResponse response) throws EnvInitException
    {
        try
        {
            response.setContentType("text/html; charset=" + FileConfigInfoReadFacade.getGlobalParameter().getOutputEncoding());
        }
        catch (FileConfigException e)
        {
            Logger.error(e.getMessage(), e);
            throw new EnvInitException(e);
        }
    }
    
    /**
     * 校验文件长度。
     * 
     * @param length
     * @throws EnvInitException
     */
    private void checkContentLength(int length) throws EnvInitException
    {
        int setLength;
        try
        {
            setLength = FileConfigInfoReadFacade.getGlobalParameter().getMaxTransferSize();
        }
        catch (FileConfigException e)
        {
            Logger.error(e.getMessage(), e);
            throw new EnvInitException(e);
        }
        if (length > setLength * 1024)
        {
            throw new EnvInitException(ISendResult.ERR_CONFIG_LIMIT_SIZE, nc.bs.ml.NCLangResOnserver.getInstance().getStrByID("pfxx",
                "UPPpfxx-000044")/*
                                  * @res "文件长度超出设定长度"
                                  */
                + setLength + "KB");
        }
    }
    
    /**
     * 根据是否设置客户端IP范围限制对客户端地址进行校验。
     * 
     * @param clientAddress
     * @param globalParameter
     * @throws EnvInitException
     */
    private void checkClientAddress(String clientAddress, SysConfigInfo globalParameter) throws EnvInitException
    {
        boolean isEffective = globalParameter.isEffective();
        String[] addresses = globalParameter.getAddresses();
        if (isEffective)
        { // 需要对客户端地址做校验
            boolean b = false;
            if (addresses != null)
            {
                for (String addresse : addresses)
                {
                    b = StringUtil.match(addresse, clientAddress) ? true : b;
                }
            }
            if (!b)
            {
                throw new EnvInitException(ISendResult.ERR_PFXX_SENDERADDRESS_INVALID,
                    NCLangResOnserver.getInstance().getStrByID("pfxx", "UPPpfxx-V50018")/* "发送方地址不合法!" */);
            }
        }
    }
}
