package uap.bs.prealert.engine.executor;

import com.yonyou.hrcloud.attendnccsyn.web.NCCSynController;
import com.yonyou.hrcloud.tcommon.impl.NCCSynServiceImpl;

import nc.bs.logging.Logger;
import nc.bs.pub.pa.PreAlertObject;
import nc.bs.pub.taskcenter.IBackgroundWorkPlugin;
import nc.vo.pub.BusinessException;
import nccloud.baseapp.core.log.NCCSysOutWrapper;
import uap.itf.prealert.engine.Executor;
import uap.vo.prealert.engine.EngineContext;

/**
 * 
 **
 * @qualiFild uap.bs.prealert.engine.executor.BackgroungTaskPluginExecutor.java<br>
 * @author：LiBencheng<br>
 * @date Created on 2025年12月18日<br>
 * @version 1.0<br>
 */
public class BackgroungTaskPluginExecutor implements Executor
{
    public PreAlertObject execute(EngineContext context) throws BusinessException
    {
        Object instantiated = PluginExecutorUtil.instantiatePlugin(context.getAlertType());
        Logger.error("in BackgroungTaskPluginExecutor ");
        String classname = instantiated == null ? "null" : instantiated.getClass().getName();
        Logger.error("Backgroud Class name = " + classname);
        if (instantiated instanceof IBackgroundWorkPlugin)
        {
            IBackgroundWorkPlugin plugin = (IBackgroundWorkPlugin) instantiated;
            NCCSysOutWrapper.println("后台调用业务插件：" + plugin.getClass().getName());
            return plugin.executeTask(context.getExecutorContext());
        }
        else
        {
            String pluginClassName = context.getAlertType().getBusi_plugin();
            throw new BusinessException(
                "Plugin class: " + pluginClassName + " did not implement specified interface: " + IBackgroundWorkPlugin.class.getName());
        }
    }
}
