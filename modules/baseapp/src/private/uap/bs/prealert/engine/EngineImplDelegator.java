package uap.bs.prealert.engine;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.yonyou.cloud.ncc.NCCSagas;

import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.LiveServerAgent;
import nc.bs.framework.server.RemoteCallPostProcessFactory;
import nc.bs.framework.server.ServerInstance;
import nc.bs.logging.Logger;
import nc.bs.pub.pa.PaUtils;
import nc.bs.pub.pa.PreAlertObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.pa.AlertregistryVO;
import ncc.bs.ls.ScheduleJobManager;
import ncc.task.quartz.dao.entity.ScheduleJob;
import nccloud.baseapp.core.log.NCCSysOutWrapper;
import uap.bs.prealert.engine.executor.INCCPreAlertConfirmCall;
import uap.itf.prealert.engine.ExecutionLogger;
import uap.itf.prealert.engine.ExecutionLoggerFactory;
import uap.itf.prealert.engine.Executor;
import uap.vo.prealert.engine.EngineContext;

/**
 * <b>后台引擎代理</b>
 * 
 * <p>
 * <b>执行的流程：</b>
 * <p>
 * 1. 使用Executor执行插件/自定义预警，获得PreAlertObject
 * <p>
 * 2. 使用MessageConstructor构建Message (in a collection)
 * <p>
 * 3. 使用MessagePersister持久化Message
 * <p>
 * 4. 使用MessageSender发送Message
 * <p>
 * 5. 使用ExecutionLogger记录执行日志
 * 
 * @author yanke1
 */
public class EngineImplDelegator
{
    private String getLogSubjectStr(EngineContext context)
    {
        AlertregistryVO registry = context.getAlertRegistry();
        return registry == null ? null
            : "  --[deployname:" + registry.getAlertname() + "],[alerttyppe:" + registry.getAlertTypeVo().getBusi_plugin() + "]--  ";
    }
    
    public void execute(PreAlertEngineImpl engine, EngineContext context) throws BusinessException
    {
        this.execute4Func(engine, context);
    }
    
    public String[] execute4Func(PreAlertEngineImpl engine, EngineContext context) throws BusinessException
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String originModule = Logger.getModule();
        Logger.init("prealert");
        Throwable exception = null;
        long beginMillis = System.currentTimeMillis();
        Logger.error("palog========任务开始执行时间：" + LocalTime.now().format(formatter));
        Logger.error("palog========  接下来执行业务插件了");
        
        String[] var13;
        try
        {
            Executor executor = engine.getExecutorFactory().getExecutor(context);
            
            String plugin = context.getAlertRegistry().getAlertTypeVo().getBusi_plugin();
            String typeName = context.getAlertRegistry().getAlertTypeVo().getType_name();
            NCCSysOutWrapper.println("业务插件：" + plugin + " -> " + typeName);
            
            PreAlertObject result = executor.execute(context);
            Logger.error("palog========  业务代码执行完毕了。");
            Logger.error("palog========任务执行结束时间：" + LocalTime.now().format(formatter));
            long endMillis = System.currentTimeMillis();
            Logger.error("palog========任务耗时：" + (endMillis - beginMillis));
            Map<String, Serializable> params = new HashMap();
            params.put("context", context);
            params.put("result", result);
            params.put("beginMillis", beginMillis);
            params.put("testlog", this.getLogSubjectStr(context));
            NCCSagas.confirm(INCCPreAlertConfirmCall.class, params);
            var13 = context.getGeneratedAttachmentID();
        }
        catch (Throwable var17)
        {
            exception = var17;
            Logger.error(var17.getMessage(), var17);
            String log = PaUtils.getStack(var17);
            ExecutionLogger logger =
                ((ExecutionLoggerFactory) NCLocator.getInstance().lookup(ExecutionLoggerFactory.class)).getLogger(context);
            logger.log(context, beginMillis, false, log);
            String taskId = context.getAlertRegistry().getPrimaryKey();
            ScheduleJob job = ScheduleJobManager.getInstance().queryScheduleJobByTaskId(taskId);
            this.updateJobStatus(job, beginMillis);
            throw new BusinessException(var17.toString(), var17);
        }
        finally
        {
            this.releaseLock(exception);
            Logger.init(originModule);
        }
        
        return var13;
    }
    
    public void updateJobStatus(ScheduleJob job, long beginMillis)
    {
        if (job != null)
        {
            if (job.getScheExecTime() == -2L)
            {
                ScheduleJobManager.getInstance().deleteScheduleJobByTaskId(job.getTaskId());
            }
            
            job.setTaskStatus(1);
            job.setRunCount(job.getRunCount() + 1);
            job.setAvgExecTime(System.currentTimeMillis() - beginMillis);
            ServerInstance localhost = ((LiveServerAgent) NCLocator.getInstance().lookup(LiveServerAgent.class)).getServerInstance();
            job.setExecServerName(localhost.getName());
            job.setExecServerAddress(localhost.getAddress());
            job.setExecServerInstanceid(localhost.getInstanceId());
            ScheduleJobManager.getInstance().updateScheduleJob(job);
        }
    }
    
    public void releaseLock(Throwable exception)
    {
        RemoteCallPostProcessFactory remoteProcessor =
            (RemoteCallPostProcessFactory) NCLocator.getInstance().lookup("RemoteProcessComponetFactory");
        if (exception == null)
        {
            remoteProcessor.postProcess();
        }
        else
        {
            remoteProcessor.postErrorProcess(exception);
        }
        
        remoteProcessor.clearThreadScopePostProcess();
    }
}
