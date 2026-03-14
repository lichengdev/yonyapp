package nc.jdbc.framework;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nc.bs.logging.Logger;
import nc.jdbc.framework.crossdb.CrossDBConnection;
import nc.jdbc.framework.exception.DbException;
import nc.jdbc.framework.exception.ExceptionFactory;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.jdbc.framework.util.DBUtil;
import pers.bc.utils.pub.LoggerUtil;
import pers.bc.utils.pub.PubEnvUtil;
import pers.bc.utils.pub.StringUtil;

/**
 * User: 贺扬 Date: 2005-1-14 Time: 16:29:51 数据库访问对象， 提供一个统一简单灵活的数据访问API,简化数据访问操作
 */
public final class JdbcSession
{
    private final int DEFAULT_BATCH_SIZE = 800;
    
    private Connection conn = null;
    // do not limit maxRows
    // add by tyh 2022-12-01
    private int maxRows = -1;
    
    private int dbType = -1;
    
    private int timeoutInSec = 0;
    
    private int fetchSize = 40;
    
    private PreparedStatement prepStatement = null;
    
    private Statement statement = null;
    
    private String lastSQL = null;
    
    private Batch batch = null;
    
    private DatabaseMetaData dbmd = null;
    
    private int batchSize = DEFAULT_BATCH_SIZE;
    
    private int size = 0;
    
    private int batchRows = 0;
    
    /**
     * 构造有参数JdbcSession对象
     * 
     * @param con 数据库连接
     */
    public JdbcSession(Connection con)
    {
        
        dbType = DBUtil.getDbType(con);
        this.conn = con;
    }
    
    /**
     * 构造默认JdbcSession该JdbcSession会默认从当前访问的DataSource得到连接
     */
    public JdbcSession() throws DbException
    {
        try
        {
            Connection con = ConnectionFactory.getConnection();
            dbType = DBUtil.getDbType(con);
            // dbType = DataSourceCenter.getInstance().getDatabaseType();
            this.conn = con;
        }
        catch (SQLException e)
        {
            throw ExceptionFactory.getException(dbType, e.getMessage(), e);
        }
        
    }
    
    /**
     * 构造JdbcSession，该JdbcSession会从指定的DataSource中得到连接
     * 
     * @param dataSourceName 数据源名称
     * @throws DbException 如果访问数据源出错则抛出DbException
     */
    public JdbcSession(String dataSourceName) throws DbException
    {
        try
        {
            Connection con = ConnectionFactory.getConnection(dataSourceName);
            dbType = DataSourceCenter.getInstance().getDatabaseType(dataSourceName);
            
            this.conn = con;
        }
        catch (SQLException e)
        {
            throw ExceptionFactory.getException(dbType, e.getMessage(), e);
        }
        
    }
    
    /**
     * 设置是否自动添加版本(ts)信息
     * 
     * @param isAddTimeStamp
     */
    public void setAddTimeStamp(boolean isAddTimeStamp)
    {
        if (conn instanceof CrossDBConnection) ((CrossDBConnection) conn).setAddTimeStamp(isAddTimeStamp);
    }
    
    /**
     * 是否进行SQL翻译
     * 
     * @param isTranslator参数
     */
    public void setSQLTranslator(boolean isTranslator)
    {
        
        if (conn instanceof CrossDBConnection) ((CrossDBConnection) conn).enableSQLTranslator(isTranslator);
    }
    
    /**
     * 设置自动提交
     * 
     * @param autoCommit参数
     */
    void setAutoCommit(boolean autoCommit) throws DbException
    {
        try
        {
            conn.setAutoCommit(autoCommit);
            
        }
        catch (SQLException e)
        {
            throw ExceptionFactory.getException(dbType, e.getMessage(), e);
        }
    }
    
    /**
     * 得到当前连接的FetchSize大小
     * 
     * @return int 返回 FetchSize
     */
    public int getFetchSize()
    {
        return fetchSize;
    }
    
    /**
     * 设置当前连接的FetchSize大小
     * 
     * @param fetchSize参数
     */
    public void setFetchSize(int fetchSize)
    {
        this.fetchSize = fetchSize;
    }
    
    public int getBatchSize()
    {
        return batchSize;
    }
    
    /**
     * 设置批处理大小
     * 
     * @param batchSize
     */
    public void setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
    }
    
    /**
     * 设置当前连接的事务级别
     * 
     * @param level参数
     */
    void setTransactionIsolation(int level) throws DbException
    {
        try
        {
            conn.setTransactionIsolation(level);
        }
        catch (SQLException e)
        {
            throw ExceptionFactory.getException(dbType, e.getMessage(), e);
        }
    }
    
    /**
     * 提交当前连接的事务
     */
    void commitTrans() throws DbException
    {
        try
        {
            conn.commit();
        }
        catch (SQLException e)
        {
            throw ExceptionFactory.getException(dbType, e.getMessage(), e);
        }
    }
    
    /**
     * 回滚当前连接的事务
     */
    void rollbackTrans() throws DbException
    {
        try
        {
            conn.rollback();
        }
        catch (SQLException e)
        {
            throw ExceptionFactory.getException(dbType, e.getMessage(), e);
        }
    }
    
    /**
     * 设置当前连接的只读
     * 
     * @param readOnly参数
     */
    public void setReadOnly(boolean readOnly) throws DbException
    {
        try
        {
            conn.setReadOnly(readOnly);
        }
        catch (SQLException e)
        {
            throw ExceptionFactory.getException(dbType, e.getMessage(), e);
        }
    }
    
    /**
     * 当前连接的是否只读
     * 
     * @return 返回是否只读
     */
    public boolean isReadOnly() throws DbException
    {
        try
        {
            return conn.isReadOnly();
        }
        catch (SQLException e)
        {
            throw ExceptionFactory.getException(dbType, e.getMessage(), e);
        }
    }
    
    /**
     * 设置执行最大行数
     * 
     * @param maxRows
     */
    public void setMaxRows(int maxRows)
    {
        this.maxRows = maxRows;
    }
    
    /**
     * 得到执行最大行数
     * 
     * @return
     */
    public int getMaxRows()
    {
        return maxRows;
    }
    
    /**
     * 取消查询
     */
    public void cancelQuery() throws DbException
    {
        try
        {
            if (prepStatement != null) prepStatement.cancel();
        }
        catch (SQLException e)
        {
            throw ExceptionFactory.getException(dbType, e.getMessage());
        }
    }
    
    /**
     * 执行有参数查询
     * 
     * @param sql 查询SQL语句
     * @param parameter 查询参数
     * @param processor 结果集处理对象
     * @return 查询对象
     */
    public Object executeQuery(String sql, SQLParameter parameter, ResultSetProcessor processor) throws DbException
    {
        // if (!isSelectStatement(sql))
        // throw new IllegalArgumentException(sql + "--不是合法的查询语句");
        Object result = null;
        ResultSet rs = null;
        
        try
        {
            debugSQL(sql, parameter);
            if ((!sql.equalsIgnoreCase(lastSQL)) || (prepStatement == null))
            {
                if (prepStatement != null)
                {
                    closeStmt(prepStatement);
                }
                prepStatement = conn.prepareStatement(sql);
                lastSQL = sql;
            }
            prepStatement.clearParameters();
            if (parameter != null)
            {
                DBUtil.setStatementParameter(prepStatement, parameter);
            }
            if (timeoutInSec > 0) prepStatement.setQueryTimeout(timeoutInSec);
            
            prepStatement.setMaxRows(maxRows > 0 ? maxRows : 0);
            if (fetchSize > 0)
            {
                if (maxRows > 0 && fetchSize > maxRows)
                {
                    fetchSize = maxRows;
                }
                prepStatement.setFetchSize(fetchSize);
            }
            rs = prepStatement.executeQuery();
            result = processor.handleResultSet(rs);
        }
        
        catch (SQLException e)
        {
            throw ExceptionFactory.getException(dbType, "sql:" + sql + " " + e.getMessage(), e);
        }
        catch (NullPointerException e)
        {
            Logger.error("conn error!", e);
            SQLException e1 = new SQLException("db connection has interrupted!");
            throw ExceptionFactory.getException(dbType, "sql:" + sql + " " + e1.getMessage(), e1);
        }
        finally
        {
            closeRs(rs);
        }
        return result;
    }
    
    /**
     * 执行无参数查询
     * 
     * @param sql 查询SQL语句
     * @param processor 结果集处理对象
     * @return 查询结果对象
     */
    public Object executeQuery(String sql, ResultSetProcessor processor) throws DbException
    {
        Object result = null;
        ResultSet rs = null;
        try
        {
            debugSQL(sql, null);
            if (statement == null) statement = conn.createStatement();
            if (timeoutInSec > 0) statement.setQueryTimeout(timeoutInSec);
            
            statement.setMaxRows(maxRows > 0 ? maxRows : 0);
            
            if (fetchSize > 0)
            {
                if (maxRows > 0 && fetchSize > maxRows)
                {
                    fetchSize = maxRows;
                }
                statement.setFetchSize(fetchSize);
            }
            rs = statement.executeQuery(sql);
            result = processor.handleResultSet(rs);
        }
        catch (SQLException e)
        {
            // 如果异常信息中包含Read too much data from
            // 则表示这个异常信息是因为 -Dnc.resultset.max=30W 导致的，就提示
            if (e.getMessage() != null && e.getMessage().contains("Read too much data from"))
            {
                throw ExceptionFactory.getException(dbType, "您一次查询数据量过大，请联系管理员调整系统参数：nc.resultset.max", e);
            }
            else
            {
                throw ExceptionFactory.getException(dbType, "sql:" + sql + " " + e.getMessage(), e);
            }
        }
        catch (NullPointerException e)
        {
            Logger.error("nullpoint exception", e);
            SQLException e1 = new SQLException("NullPointException cause query error");
            throw ExceptionFactory.getException(dbType, "sql:" + sql + " " + e1.getMessage(), e1);
        }
        finally
        {
            closeRs(rs);
        }
        return result;
    }
    
    /**
     * 执行有更新操作
     * 
     * @param sql 预编译SQL语句
     * @param parameter 参数对象
     * @return 变化行数
     */
    public int executeUpdate(String sql, SQLParameter parameter) throws DbException
    {
        int updateRows;
        try
        {
            debugSQL(sql, parameter);
            if ((!sql.equalsIgnoreCase(lastSQL)) || (prepStatement == null))
            {
                if (prepStatement != null)
                {
                    closeStmt(prepStatement);
                }
                prepStatement = conn.prepareStatement(sql);
                lastSQL = sql;
            }
            prepStatement.clearParameters();
            DBUtil.setStatementParameter(prepStatement, parameter);
            updateRows = prepStatement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw ExceptionFactory.getException(dbType, "sql:" + sql + " " + e.getMessage(), e);
        }
        catch (NullPointerException e)
        {
            SQLException e1 = new SQLException("db connection has interrupted!");
            throw ExceptionFactory.getException(dbType, "sql:" + sql + " " + e1.getMessage(), e1);
        }
        return updateRows;
    }
    
    /**
     * 执行无更新操作
     * 
     * @param sql 更新SQL语句
     * @return 更新行数
     */
    public int executeUpdate(String sql) throws DbException
    {
        int updateRows = 0;
        
        try
        {
            debugSQL(sql, null);
            if (statement == null) statement = conn.createStatement();
            updateRows = statement.executeUpdate(sql);
        }
        catch (SQLException e)
        {
            throw ExceptionFactory.getException(dbType, "sql:" + sql + " " + e.getMessage(), e);
        }
        catch (NullPointerException e)
        {
            SQLException e1 = new SQLException("db connection has interrupted!");
            throw ExceptionFactory.getException(dbType, "sql:" + sql + " " + e1.getMessage(), e1);
        }
        return updateRows;
    }
    
    /**
     * 添加有参数批量查询
     * 
     * @param sql
     * @param parameters
     */
    public void addBatch(String sql, SQLParameter parameters) throws DbException
    {
        if (batch == null) batch = new Batch();
        try
        {
            debugSQL(sql, parameters);
            batch.addBatch(sql, parameters);
            size++;
            if (size % batchSize == 0)
            {
                batchRows = internalExecuteBatch();
                size = 0;
            }
        }
        catch (SQLException e)
        {
            throw ExceptionFactory.getException(dbType, "sql:" + sql + " " + e.getMessage(), e);
        }
        catch (NullPointerException e)
        {
            SQLException e1 = new SQLException("db connection has interrupted!");
            throw ExceptionFactory.getException(dbType, "sql:" + sql + " " + e1.getMessage(), e1);
        }
    }
    
    /**
     * 添加有参数批量查询
     * 
     * @param sql
     * @param parameters
     */
    public void addBatch(String sql, SQLParameter[] parametersArray) throws DbException
    {
        try
        {
            debugSQL(sql, parametersArray);
            if (batch == null) batch = new Batch();
            size = size + parametersArray.length;
            batch.addBatch(sql, parametersArray);
            if (size % batchSize == 0 || size > batchSize)
            {
                batchRows = internalExecuteBatch();
                size = 0;
            }
        }
        catch (SQLException e)
        {
            throw ExceptionFactory.getException(dbType, "sql:" + sql + " " + e.getMessage(), e);
        }
        catch (NullPointerException e)
        {
            SQLException e1 = new SQLException("db connection has interrupted!");
            throw ExceptionFactory.getException(dbType, "sql:" + sql + " " + e1.getMessage(), e1);
        }
    }
    
    /**
     * 添加无参数批量查询
     * 
     * @param sql
     */
    public void addBatch(String sql) throws DbException
    {
        addBatch(sql, (SQLParameter) null);
    }
    
    private int internalExecuteBatch() throws DbException
    {
        try
        {
            int rows = 0;
            if (batch != null)
            {
                rows = batchRows + batch.executeBatch();
            }
            batchRows = 0;
            size = 0;
            return rows;
        }
        catch (SQLException e)
        {
            Logger.error("execute batch exception", e.getNextException());
            throw ExceptionFactory.getException(dbType, e.getMessage(), e);
        }
        catch (NullPointerException e)
        {
            Logger.error("NullPointerException connection uap", e);
            SQLException e1 = new SQLException("db connection has interrupted!");
            throw ExceptionFactory.getException(dbType, e1.getMessage(), e1);
        }
    }
    
    /**
     * 执行批量更新
     * 
     * @return
     */
    public int executeBatch() throws DbException
    {
        try
        {
            return internalExecuteBatch();
        }
        finally
        {
            if (batch != null)
            {
                batch.cleanupBatch();
                batch = null;
            }
        }
    }
    
    /**
     * 关闭数据库连接
     */
    public void closeAll()
    {
        closeStmt();
        closePreStmt();
        closeConnection();
    }
    
    /**
     * 得到当前数据库的MetaData
     * 
     * @return 返回当前数据库的MetaData
     * @throws SQLException
     */
    public DatabaseMetaData getMetaData()
    {
        if (dbmd == null) try
        {
            dbmd = conn.getMetaData();
        }
        catch (SQLException e)
        {
            Logger.error("get metadata error", e);
        }
        return dbmd;
    }
    
    /**
     * 创建事物处理类
     * 
     * @return JdbcTransaction
     */
    public JdbcTransaction createTransaction()
    {
        return new JdbcTransaction(this);
    }
    
    private class BatchStruct
    {
        String sql = null;
        
        SQLParameter[] params;
        
        public BatchStruct(String sql, SQLParameter[] params)
        {
            this.sql = sql;
            this.params = params;
        }
        
        public BatchStruct(String sql, SQLParameter param)
        {
            this.sql = sql;
            if (param != null)
            {
                this.params = new SQLParameter[]{param};
            }
        }
    }
    
    /**
     * 私有Batch类
     */
    private class Batch
    {
        
        private List<BatchStruct> batchStructs = new ArrayList<BatchStruct>();
        
        private Map<String, PreparedStatement> cachedStatement = new HashMap<String, PreparedStatement>();
        
        private Statement stmt = null;
        
        public Batch()
        {
        }
        
        public void addBatch(String sql, SQLParameter[] pas) throws SQLException
        {
            batchStructs.add(new BatchStruct(sql, pas));
        }
        
        public void addBatch(String sql, SQLParameter pa) throws SQLException
        {
            batchStructs.add(new BatchStruct(sql, pa));
        }
        
        private Statement getStatement(String sql, boolean prepare) throws SQLException
        {
            if (prepare)
            {
                PreparedStatement stmt = cachedStatement.get(sql);
                if (stmt == null)
                {
                    stmt = conn.prepareStatement(sql);
                    cachedStatement.put(sql, stmt);
                }
                return stmt;
            }
            else
            {
                if (stmt == null)
                {
                    stmt = conn.createStatement();
                }
                return stmt;
            }
        }
        
        public int executeBatch() throws SQLException
        {
            int totalRowCount = 0;
            Iterator<BatchStruct> itr = batchStructs.iterator();
            int rbSize = 0;
            Statement lastStmt = null;
            String lastSql = null;
            while (itr.hasNext())
            {
                BatchStruct bs = itr.next();
                itr.remove();
                Statement now = getStatement(bs.sql, bs.params != null);
                if (now != lastStmt)
                {
                    if (lastStmt != null)
                    {
                        totalRowCount += internalExecute(lastStmt);
                        rbSize = 0;
                        if (now != stmt)
                        {
                            closeStmt(lastStmt);
                            cachedStatement.remove(lastSql);
                        }
                    }
                    lastStmt = now;
                    lastSql = bs.sql;
                }
                if (bs.params != null)
                {
                    PreparedStatement ps = (PreparedStatement) now;
                    for (SQLParameter parameter : bs.params)
                    {
                        if (parameter != null)
                        {
                            DBUtil.setStatementParameter(ps, parameter);
                        }
                        ps.addBatch();
                        rbSize++;
                        if (rbSize % batchSize == 0)
                        {
                            totalRowCount += internalExecute(ps);
                        }
                    }
                }
                else
                {
                    now.addBatch(bs.sql);
                    rbSize++;
                    if (rbSize % batchSize == 0)
                    {
                        totalRowCount += internalExecute(now);
                    }
                    
                }
            }
            
            if (lastStmt != null && rbSize % batchSize != 0)
            {
                totalRowCount += internalExecute(lastStmt);
            }
            
            return totalRowCount;
        }
        
        private int internalExecute(Statement ps) throws SQLException
        {
            int tc = 0;
            int[] rowCounts = ps.executeBatch();
            if (rowCounts != null && rowCounts.length > 0)
            {
                for (int j = 0; j < rowCounts.length; j++)
                {
                    if (rowCounts[j] == Statement.SUCCESS_NO_INFO)
                    {
                    }
                    else if (rowCounts[j] == Statement.EXECUTE_FAILED)
                    {
                        // throw new SQLException("批量执行第 " + j + "条语句出错！");
                    }
                    else
                    {
                        tc += rowCounts[j];
                    }
                }
            }
            return tc;
            
        }
        
        /**
         * 清理批量查询
         */
        public void cleanupBatch() throws DbException
        {
            Map<String, PreparedStatement> old = cachedStatement;
            cachedStatement = new HashMap<String, PreparedStatement>();
            for (PreparedStatement ps : old.values())
            {
                closeStmt(ps);
            }
            batchStructs.clear();
            closeStmt(stmt);
            stmt = null;
        }
    }
    
    /**
     * 返回数据库连接
     * 
     * @return 返回 conn。
     */
    public Connection getConnection()
    {
        
        return conn;
    }
    
    /**
     * @return 返回 dbType。
     */
    public int getDbType()
    {
        return dbType;
    }
    
    private void closeConnection()
    {
        try
        {
            if (conn != null)
            {
                conn.close();
            }
        }
        catch (SQLException e)
        {
        }
        conn = null;
    }
    
    private void closeStmt()
    {
        try
        {
            if (statement != null)
            {
                statement.close();
            }
        }
        catch (SQLException e)
        {
        }
        statement = null;
    }
    
    private void closePreStmt()
    {
        try
        {
            if (prepStatement != null)
            {
                prepStatement.close();
            }
        }
        catch (SQLException e)
        {
        }
        prepStatement = null;
    }
    
    private void closeStmt(Statement stmt)
    {
        try
        {
            if (stmt != null)
            {
                stmt.close();
                stmt = null;
            }
        }
        catch (SQLException e)
        {
        }
    }
    
    private void closeRs(ResultSet rs)
    {
        try
        {
            if (rs != null)
            {
                rs.close();
                rs = null;
            }
        }
        catch (SQLException e)
        {
        }
    }
    // private boolean isSelectStatement(String sql) {
    // StringBuffer sb = new StringBuffer(sql.trim());
    // String s = (sb.substring(0, 6));
    // return (s.equalsIgnoreCase("SELECT"));
    // }
    
    // private boolean isSupportBatch() throws SQLException {
    // return getMetaData().supportsBatchUpdates();
    // }
    
    /**
     * *********************************************************** <br>
     * *说明： <br>
     * @see <br>
     * @param sql
     * @param parameter <br>
     * @void <br>
     * @methods nc.jdbc.framework.JdbcSession#debugSQL <br>
     * @author LiBencheng <br>
     * @date Created on 2000年1月1日 <br>
     * @time 上午12:13:35 <br>
     * @version 1.0 <br>
     *************************************************************          <br>
     */
    public final void debugSQL(String sql, SQLParameter... parameter)
    {
        LoggerUtil loggerUtil = LoggerUtil.getInstance("JdbcSessionlogs");
        if (nc.vo.jcom.lang.StringUtil.isEmptyWithTrim(sql))
            throw new IllegalArgumentException("sql is null !!!" + LoggerUtil.getInvokMethodAll());
        // Dnc.runMode=develop
        // nc.bs.framework.common.RuntimeEnv.isDevelopMode()
        // if (nc.bs.framework.common.RuntimeEnv.isDevelopMode())
        // if (true)
        if (PubEnvUtil.equals("develop", System.getProperty("nc.runMode")))
            if (PubEnvUtil.equals("true", System.getProperty("nc.printlogs")))
        {
            loggerUtil.infoAppointPrefix(sql, "infoSQL");
            loggerUtil.infoAppointPrefix("SQL" + sql + "\r\n" + LoggerUtil.getInvokMethodAll(), "debugSQLStackTrace");
            if (PubEnvUtil.getSize(parameter) > 0)
            {
                for (int i = 0, j = PubEnvUtil.getSize(parameter); i < j; i++)
                {
                    SQLParameter param = parameter[i];
                    if (PubEnvUtil.isEmptyObj(param)) continue;
                    
                    String str = " select SQL Parameters：";
                    
                    if (PubEnvUtil.containStr(sql, new String[]{"insert", "INSERT"})) str = "insert SQL Parameters：";
                    if (PubEnvUtil.containStr(sql, new String[]{"delete", "DELETE"})) str = "delete SQL Parameters：";
                    if (PubEnvUtil.containStr(sql, new String[]{"update", "UPDATE"})) str = "update SQL Parameters：";
                    
                    loggerUtil.infoAppointPrefix(str + StringUtil.toString(parameter[i].getParameters()) + "\r\nSQL：" + sql, "SQLPrams");
                    
                }
            }
        }
        
    }
}
