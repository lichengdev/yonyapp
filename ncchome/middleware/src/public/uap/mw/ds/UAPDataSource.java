package uap.mw.ds;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import nc.bs.logging.Log;
import nc.bs.mw.ejb.xares.IerpDataSource;
import nc.bs.mw.pm.EJBDataSource;
import nc.bs.mw.pm.JdbcProperty;
import nc.middleware.tran.NCDataSource;
import oracle.jdbc.pool.OracleDataSource;
import uap.mw.trans.UAPTransaction;
import uap.mw.trans.UAPTransactionManagerProxy;
import uap.mw.trans.itf.IUAPTransactionManager;
import uap.mw.trans.util.ConnectionStatus;
import uap.mw.trans.util.DBLogger;

public class UAPDataSource implements DataSource, NCDataSource, UAPDSMonitorMBean
{
    private static Log log = Log.getInstance(UAPDataSource.class);
    private String dataSourceName;
    private UAPDBConnectionPool connPool;
    private DataSource realDataSource;
    private int maxTryNum;
    private int databaseType;
    private int preparedStatementCacheSize;
    private String dsUrl;
    public static int loginTimeout = 20000;
    private PrintWriter m_printWriter;
    
    public UAPDataSource()
    {
        this.realDataSource = null;
        this.maxTryNum = 2;
        
        this.preparedStatementCacheSize = 0;
        
        this.dsUrl = "";
        
        this.m_printWriter = null;
    }
    
    public void init(EJBDataSource eds) throws Exception
    {
        setDataSourceName(eds.getDataSourceName());
        this.dsUrl = eds.getDatabaseUrl();
        Properties jdbcProperties = new Properties();
        if (eds.getJdbcProperties() != null)
        {
            for (JdbcProperty p : eds.getJdbcProperties())
            {
                jdbcProperties.setProperty(p.getPropertyName(), p.getPropertyValue());
            }
        }
        
        this.realDataSource = (DataSource) ((UAPDSMonitorMBean) Class.forName(eds.getDataSourceClassName()).newInstance());
        
        if (this.realDataSource.getClass() == OracleDataSource.class)
        {
            OracleDataSource ods = (OracleDataSource) this.realDataSource;
            ods.setURL(eds.getDatabaseUrl());
            ods.setUser(eds.getUser());
            ods.setPassword(eds.getPassword());
            setDatabaseType(1);
        }
        if (this.realDataSource.getClass() == IerpDataSource.class)
        {
            IerpDataSource ixds = (IerpDataSource) this.realDataSource;
            ixds.setDriver(eds.getDriverClassName());
            ixds.setURL(eds.getDatabaseUrl());
            ixds.setUser(eds.getUser());
            ixds.setPassword(eds.getPassword());
            ixds.setJdbcProperties(jdbcProperties);
            ixds.init();
            setDatabaseType(2);
        }
        setPreparedStatementCacheSize(eds.getPreStateNum());
        this.connPool = new UAPDBConnectionPool(eds.getMaxCon(), eds.getMinCon(), this);
        
        checkCompatibility(eds.getDriverClassName());
    }
    
    private void setDatabaseType(int i)
    {
        this.databaseType = i;
    }
    
    public int getDatabaseType()
    {
        return this.databaseType;
    }
    
    private void checkCompatibility(String driverName)
    {
        if (driverName.toLowerCase().indexOf("oracle") > -1)
        {
            setDatabaseType(1);
        }
        if (driverName.toLowerCase().indexOf("db2") > -1)
        {
            setDatabaseType(0);
        }
        if (driverName.toLowerCase().indexOf("sqlserver") > 0) setDatabaseType(2);
    }
    
    Connection creatNewPhysicalConnection() throws SQLException
    {
        Connection conn = null;
        int i = 0;
        while (i < this.maxTryNum)
        {
            try
            {
                conn = this.realDataSource.getConnection();
                if (conn != null) i = this.maxTryNum;
            }
            catch (SQLException e)
            {
                log.error(e);
                conn = null;
                ++i;
            }
        }
        if (conn == null)
        {
            throw new SQLException("can not get connection,please check the DBSet");
        }
        
        return conn;
    }
    
    public DBConnection getConnection() throws SQLException
    {
        DBConnection conn = null;
        if (UAPTransactionManagerProxy.isTrans()) conn = getTransConnection();
        else
        {
            for (int i = 0; i < 3; ++i)
            {
                conn = this.connPool.getConnection();
                if (conn != null)
                {
                    setConnToUse(conn, false, null, null);
                    break;
                }
            }
        }
        if (conn == null)
        {
            throw new SQLException("get connection error!!");
        }
        return conn;
    }
    
    boolean closeConnection(DBConnection conn) throws SQLException
    {
        return this.connPool.closeConnection(conn);
    }
    
    private DBConnection getTransConnection() throws SQLException
    {
        IUAPTransactionManager curTranManager = UAPTransactionManagerProxy.getCurTransManager();
        
        UAPTransaction curTrans = (UAPTransaction) curTranManager.getTransaction();
        
        DBConnection conn = curTrans.getConnectionFromCurTrans(this.dataSourceName);
        if (conn == null)
        {
            conn = this.connPool.getConnection();
            if (conn != null)
            {
                setConnToUse(conn, true, curTrans, curTranManager);
            }
        }
        return conn;
    }
    
    private void setConnToUse(DBConnection conn, boolean isTrans, UAPTransaction curTrans, IUAPTransactionManager curTranManager)
            throws SQLException
    {
        try
        {
            conn.changeStates(ConnectionStatus.INUSE);
            if (isTrans)
            {
                curTrans.enlistConnResource(this.dataSourceName, conn);
                conn.setAutoCommit(false);
            }
        }
        catch (Exception e)
        {
            conn.setStatus(ConnectionStatus.NEED_DESTROY);
            conn.realDestroyConn();
            this.connPool.releasePhore();
            DBLogger.error("set Conn State error!", e);
            throw new SQLException("set Conn State error!!");
        }
    }
    
    public Connection getConnection(String username, String password) throws SQLException
    {
        throw new SQLException("not support this mode to access db");
    }
    
    public PrintWriter getLogWriter() throws SQLException
    {
        return this.m_printWriter;
    }
    
    public void setLogWriter(PrintWriter out) throws SQLException
    {
        this.m_printWriter = out;
    }
    
    public void setLoginTimeout(int seconds) throws SQLException
    {
        loginTimeout = seconds;
    }
    
    public int getLoginTimeout() throws SQLException
    {
        return (loginTimeout / 1000);
    }
    
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        throw new SQLException("not supported");
    }
    
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return false;
    }
    
    public String getDataSourceName()
    {
        return this.dataSourceName;
    }
    
    public void setDataSourceName(String dataSourceName)
    {
        this.dataSourceName = dataSourceName;
    }
    
    public String getDataSourceType()
    {
        int type = getDatabaseType();
        switch (type)
        {
            case 0 :
                return "db2";
            case 1 :
                return "oracle";
            case 2 :
                return "sqlserver";
        }
        return "otherDB";
    }
    
    public int getMaxConnection()
    {
        return this.connPool.getMaxNum();
    }
    
    public int getMinConnection()
    {
        return this.connPool.getMinNum();
    }
    
    public void setMaxConnection(int num)
    {
        this.connPool.reSize(num);
    }
    
    public int getInUsedConnection()
    {
        return this.connPool.getInUseSize();
    }
    
    public int getFreeConnection()
    {
        return (this.connPool.getCurConnectionSize() - this.connPool.getInUseSize());
    }
    
    public String printConnectionState()
    {
        return this.connPool.getConnectionState();
    }
    
    public int getPoolConnPermit()
    {
        return this.connPool.getavailablePermits();
    }
    
    public int getPreparedStatementCacheSize()
    {
        return this.preparedStatementCacheSize;
    }
    
    public void setPreparedStatementCacheSize(int preparedStatementCacheSize)
    {
    }
    
    public String getDataSourceURL()
    {
        return this.dsUrl;
    }
    
    public int getMaxTryNum()
    {
        return this.maxTryNum;
    }
    
    public void setMaxTryNum(int maxTryNum)
    {
        this.maxTryNum = maxTryNum;
    }
    
    private void setConnectionDestroy()
    {
        this.connPool.setConnectionDestroy();
    }
    
    public void updatePoolSize(int newSize)
    {
        this.connPool.reSize(newSize);
    }
    
    public void checkConn(boolean desAll, String testSQL)
    {
        ResultSet rs = null;
        DBConnection conn = null;
        boolean connError = false;
        PreparedStatement ps = null;
        try
        {
            conn = getConnection();
            ps = conn.prepareStatement(testSQL);
            rs = ps.executeQuery();
        }
        catch (SQLException e)
        {
            DBLogger.error("sqlTest error :sql：" + testSQL + ",datasource::" + getDataSourceName(), e);
            
            connError = true;
        }
        finally
        {
            if (ps != null) try
            {
                ps.close();
            }
            catch (Exception e)
            {
            }
            if (rs != null) try
            {
                rs.close();
            }
            catch (SQLException e)
            {
            }
            if (conn != null) try
            {
                conn.close();
            }
            catch (SQLException e)
            {
            }
            if (connError) if (desAll) setConnectionDestroy();
            else
                this.connPool.destroyConnectionUnBlockQueue(conn);
        }
    }
    
    public void connGC()
    {
        this.connPool.connGC();
    }
    
    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        return null;
    }
}
