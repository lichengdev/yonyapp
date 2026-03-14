package ycm.yonyou.agent.vo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import nc.bs.logging.Logger;
import pers.bc.utils.pub.CollectionUtil;
import pers.bc.utils.pub.LoggerUtil;
import ycm.yonyou.agent.gather.RemoteCallHandleCenter;
import ycm.yonyou.agent.gather.swingworker.KeyConstants;

/**
 * 业务调用信息类
 * @author yyy_liyue9 2017-02-15
 */
public class BusiCallInfo implements Cloneable, KeyConstants
{
    
    private List<BusiCallInfo> children;
    private BusiCallInfo parent;
    
    // 是不是异步线程
    private boolean isAsynThread = false;
    
    // 是不是参照
    private boolean isRefOption = false;
    
    private boolean isNCLogin = false;
    
    // 业务下事务列表：Object为String或基本数据类型（Number）
    @CloneIgnore
    private List<Map<String, Object>> transationList = new ArrayList<Map<String, Object>>();
    
    @CloneIgnore
    private BusinessState state = BusinessState.RUNNING;
    
    /**
     * 创建业务信息时所处的线程id。
     */
    private long threadID;
    
    /**
     * 业务开始时的时间戳
     */
    private long startTs;
    
    /**
     * 业务耗时：从开始到结束
     */
    private long businessTotalTime = 0;
    
    /**
     * 当前正在进行的远程调用
     */
    @CloneIgnore
    private RemoteCallHandleCenter currentRemoteCall = null;
    
    /**
     * 当前业务的唯一编码
     */
    private String businessId;
    
    /**
     * 业务名称
     */
    private String businessName;
    
    /**
     * 节点编码
     */
    private String nodeCode;
    
    /**
     * 节点名称
     */
    private String nodeName;
    
    /**
     * 按钮名称
     */
    private String buttonName;
    
    /**
     * 操作人员编码
     */
    private String userCode;
    
    /**
     * SwingWorker在进行当前业务操作时，分配的线程已经执行了多少个task
     */
    private int taskCount;
    
    private boolean remoteInvocated;
    
    /**
     * <p>
     * 业务类型标识{@code KeyConstants}，
     * <p>
     * 类型说明：<br/>
     * 0-同步业务<br/>
     * 1-异步业务（由同步业务提交的异步业务，此时异步对应的同步业务bsid是准确的）<br/>
     * 2-异步业务（{@code Swingworker}的子线程提交的异步业务，此时的绑定是准确的，但是{@code childBusinessCnt}的个数可能是不准确的）<br/>
     * 3-异步业务（由其他线程（非{@code Swingworker}线程池中的线程）提交的异步业务，此时的绑定可能是不准确的）<br/>
     * 4-未知类型的异步业务<br/>
     */
    private int businessType;
    
    /**
     * 异步子业务个数
     */
    private AtomicInteger childBusinessCnt = new AtomicInteger(0);
    
    /**
     * 异步业务的父业务id
     */
    private String parentBsid;
    
    /**
     * 标记父业务的子业务个数
     */
    private AtomicInteger sblingBusinessCnt = new AtomicInteger(0);
    
    private AtomicLong suspendTime = new AtomicLong(0);
    private long suspendTs = 0;
    private boolean success = true;
    
    public void setSuccess(boolean success)
    {
        this.success = success;
    }
    
    public boolean isSuccess()
    {
        return this.success;
    }
    
    public BusiCallInfo()
    {
        this(true);
        
    }
    
    public BusiCallInfo(boolean start)
    {
        if (start)
        {
            startWatch();
        }
        
        Thread thread = Thread.currentThread();
        setThreadID(thread.getId());
        this.children = new ArrayList<BusiCallInfo>();
        this.parent = null;
    }
    
    private void startWatch()
    {
        this.startTs = System.currentTimeMillis();
        setState(BusinessState.RUNNING);
    }
    
    /**
     * 暂停 挂起
     */
    public void suspend()
    {
        suspendTs = System.currentTimeMillis();
    }
    
    /**
     * 继续执行
     */
    public void resume()
    {
        if (suspendTs > 0)
        {
            suspendTime.getAndAdd(System.currentTimeMillis() - suspendTs);
        }
        suspendTs = 0;
    }
    
    public void stopWatch()
    {
        setState(BusinessState.STOPPED);
        businessTotalTime = System.currentTimeMillis() - startTs - suspendTime.get();
    }
    
    @Override
    public BusiCallInfo clone()
    {
        // 克隆四部分：是否异步、是否参照、属性、事务列表
        BusiCallInfo cloneInfo = new BusiCallInfo(false);
        // 1、克隆是否异步
        cloneInfo.isAsynThread = this.isAsynThread;
        // 2、克隆是否参照
        cloneInfo.isRefOption = this.isRefOption;
        // 3、克隆属性：String类型的
        Field[] fields = BusiCallInfo.class.getDeclaredFields();
        for (Field field : fields)
        {
            boolean isCloneIgnore = (field.getAnnotation(CloneIgnore.class) != null);
            if (!isCloneIgnore)
            {
                try
                {
                    field.set(cloneInfo, field.get(this));
                }
                catch (Exception e)
                {
                    Logger.warn("yyy:clone error:" + field.getName(), e);
                }
            }
        }
        // 4、克隆事务列表
        Map<String, Object> cloneTxMap = null;
        List<Map<String, Object>> cloneMapList = new ArrayList<Map<String, Object>>();
        Iterator<Entry<String, Object>> iter_t = null;
        for (Map<String, Object> map : transationList)
        {
            cloneTxMap = new HashMap<String, Object>();
            iter_t = map.entrySet().iterator();
            while (iter_t.hasNext())
            {
                Entry<String, Object> entry = iter_t.next();
                cloneTxMap.put(entry.getKey(), entry.getValue());
            }
            cloneMapList.add(cloneTxMap);
        }// end for
        cloneInfo.transationList = cloneMapList;
        
        return cloneInfo;
    }
    
    public List<Map<String, Object>> getTransationList()
    {
        return transationList;
    }
    
    public void clearTransactionList()
    {
        transationList.clear();
    }
    
    public void addTransaction(Map<String, Object> transation)
    {
        transationList.add(transation);
    }
    
    public boolean isAsynThread()
    {
        return isAsynThread;
    }
    
    public void setAsynThread(boolean isAsynThread)
    {
        this.isAsynThread = isAsynThread;
    }
    
    public RemoteCallHandleCenter getCurrentRemoteCall()
    {
        return currentRemoteCall;
    }
    
    public void setCurrentRemoteCall(RemoteCallHandleCenter currentRemoteCall)
    {
        this.currentRemoteCall = currentRemoteCall;
    }
    
    public boolean isRefOption()
    {
        return isRefOption;
    }
    
    public void setRefOption(boolean isRefOption)
    {
        this.isRefOption = isRefOption;
    }
    
    public BusinessState getState()
    {
        return state;
    }
    
    public void setState(BusinessState state)
    {
        this.state = state;
    }
    
    public long getStartTs()
    {
        return startTs;
    }
    
    public void setStartTs(long startTs)
    {
        this.startTs = startTs;
    }
    
    public long getBusinessTotalTime()
    {
        return businessTotalTime;
    }
    
    /**
     * 修正业务停止的时间
     * @param businessTotalTime
     */
    public void reviseBusinessTotalTime(long businessTotalTime)
    {
        this.businessTotalTime = businessTotalTime;
    }
    
    public String getBusinessId()
    {
        return businessId;
    }
    
    public void setBusinessId(String businessId)
    {
        this.businessId = businessId;
    }
    
    public String getBusinessName()
    {
        return businessName;
    }
    
    public void setBusinessName(String businessName)
    {
        this.businessName = businessName;
    }
    
    public String getNodeCode()
    {
        return nodeCode;
    }
    
    public void setNodeCode(String nodeCode)
    {
        this.nodeCode = nodeCode;
    }
    
    public String getNodeName()
    {
        return nodeName;
    }
    
    public void setNodeName(String nodeName)
    {
        if (this.nodeName == null && nodeName != null) this.nodeName = nodeName;
    }
    
    public String getButtonName()
    {
        return buttonName;
    }
    
    public void setButtonName(String buttonName)
    {
        this.buttonName = buttonName;
    }
    
    public String getUserCode()
    {
        return userCode;
    }
    
    public void setUserCode(String userCode)
    {
        this.userCode = userCode;
    }
    
    public int getTaskCount()
    {
        return taskCount;
    }
    
    public void setTaskCount(int taskCount)
    {
        this.taskCount = taskCount;
    }
    
    public void setTransationList(List<Map<String, Object>> transationList)
    {
        this.transationList = transationList;
    }
    
    public boolean isNCLogin()
    {
        return isNCLogin;
    }
    
    public void setNCLogin(boolean isNCLogin)
    {
        this.isNCLogin = isNCLogin;
    }
    
    public long getThreadID()
    {
        return threadID;
    }
    
    public void setThreadID(long threadID)
    {
        this.threadID = threadID;
    }
    
    public boolean isRemoteInvocated()
    {
        return remoteInvocated;
    }
    
    public void setRemoteInvocated(boolean remoteInvocated)
    {
        this.remoteInvocated = remoteInvocated;
    }
    
    public int getBusinessType()
    {
        return businessType;
    }
    
    public void setBusinessType(int businessType)
    {
        this.businessType = businessType;
    }
    
    public AtomicInteger getChildBusinessCnt()
    {
        return childBusinessCnt;
    }
    
    public void setChildBusinessCnt(AtomicInteger childBusinessCnt)
    {
        this.childBusinessCnt = childBusinessCnt;
    }
    
    public AtomicInteger getSblingBusinessCnt()
    {
        return sblingBusinessCnt;
    }
    
    public void setSblingBusinessCnt(AtomicInteger sblingBusinessCnt)
    {
        this.sblingBusinessCnt = sblingBusinessCnt;
    }
    
    public String getParentBsid()
    {
        return parentBsid;
    }
    
    public void setParentBsid(String parentBsid)
    {
        this.parentBsid = parentBsid;
    }
    
    @Override
    public String toString()
    {
        try
        {
            return LoggerUtil.getInvokMethodUpStep() + CollectionUtil.transObj2Josn(this).replace("\r\n", "");
        }
        catch (Exception e)
        {
            return LoggerUtil.getInvokMethodUpStep() + "BusiCallInfo [businessId=" + businessId + ", isRefOption=" + isRefOption
                    + ", isNCLogin=" + isNCLogin + ", transationList=" + transationList + ", state=" + state + ", threadID=" + threadID
                    + ", startTs=" + startTs + ", businessTotalTime=" + businessTotalTime + ", currentRemoteCall=" + currentRemoteCall
                    + ", isAsynThread=" + isAsynThread + ", businessName=" + businessName + ", nodeCode=" + nodeCode + ", nodeName="
                    + nodeName + ", buttonName=" + buttonName + ", userCode=" + userCode + ", taskCount=" + taskCount
                    + ", remoteInvocated=" + remoteInvocated + "]";
        }
    }
    
    public void setParent(BusiCallInfo parent)
    {
        this.parent = parent;
    }
    
    public BusiCallInfo getParent()
    {
        return this.parent;
    }
    
    public boolean removeChild(BusiCallInfo child)
    {
        return this.children.remove(child);
    }
    
    public void addChild(BusiCallInfo child)
    {
        this.children.add(child);
        child.setParent(this);
    }
}
