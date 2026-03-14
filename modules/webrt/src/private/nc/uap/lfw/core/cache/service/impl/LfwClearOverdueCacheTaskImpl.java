package nc.uap.lfw.core.cache.service.impl;

import nc.uap.lfw.core.cache.service.ILfwClearOverdueCacheTask;
import nc.uap.lfw.core.cache.util.ClearCacheUtil;
import pers.bc.utils.pub.PubEnvUtil;

/**
 * NC65,开发环境不更新缓存
 * @qualiFild nc.uap.lfw.core.cache.service.impl.LfwClearOverdueCacheTaskImpl.java<br>
 * @author：LiBencheng<br>
 * @date Created on 2020-12-18<br>
 * @version 1.0<br>
 */
public class LfwClearOverdueCacheTaskImpl implements ILfwClearOverdueCacheTask
{
    
    @Override
    public void clearOverdueCache()
    {
        exeClearOverdueCache();
        
    }
    
    /**
     * *********************************************************** <br>
     * 说明：NC65-,开发环境不打印打印SQL语句和调用数据库的堆栈信息 <br>
     * @see <br>
     *      <br>
     * @void <br>
     * @methods nc.uap.lfw.core.cache.service.impl.LfwClearOverdueCacheTaskImpl#exeClearOverdueCache <br>
     * @author LiBencheng <br>
     * @date Created on 2020-12-18 <br>
     * @time 下午6:18:54 <br>
     * @version 1.0 <br>
     *************************************************************          <br>
     */
    public void exeClearOverdueCache()
    {
        if (!PubEnvUtil.equals("develop", System.getProperty("nc.runMode")))
        {
            ClearCacheUtil.clearOverdueCache();
        }
    }
    
}
