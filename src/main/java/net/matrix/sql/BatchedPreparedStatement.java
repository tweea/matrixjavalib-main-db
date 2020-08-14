/*
 * Copyright(C) 2009 matrix
 * All right reserved.
 */
package net.matrix.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.commons.lang3.ArrayUtils;

/**
 * 可以批量执行的 PreparedStatement。
 */
public class BatchedPreparedStatement
    implements PreparedStatement {
    /**
     * 被包装的 PreparedStatement。
     */
    private final PreparedStatement statement;

    /**
     * 最大批量执行的语句数量。
     */
    private final int batchSize;

    /**
     * 等待批量执行的语句数量。
     */
    private int batchCount;

    /**
     * 记录的批量执行结果。
     */
    private int[] batchResult;

    /**
     * 包装一个 PreparedStatement，不使用批量执行。
     * 
     * @param statement
     *     被包装的 PreparedStatement
     */
    public BatchedPreparedStatement(final PreparedStatement statement) {
        this(statement, 0);
    }

    /**
     * 包装一个 PreparedStatement，指定最大批量执行数量。
     * 
     * @param statement
     *     被包装的 PreparedStatement
     * @param batchSize
     *     最大批量执行数量
     */
    public BatchedPreparedStatement(final PreparedStatement statement, final int batchSize) {
        this.statement = statement;
        this.batchSize = batchSize;
        this.batchCount = 0;
        this.batchResult = ArrayUtils.EMPTY_INT_ARRAY;
    }

    /**
     * 检查等待批量执行的语句数量，如果已达到最大数量，则返回 true。
     */
    private boolean checkBatchCount() {
        return batchSize > 0 && batchCount >= batchSize;
    }

    /**
     * 等待批量执行的语句数量加一。
     */
    private void addBatchCount() {
        if (batchSize > 0) {
            batchCount++;
        }
    }

    /**
     * 等待批量执行的语句数量清零。
     */
    private void resetBatchCount() {
        batchCount = 0;
    }

    /**
     * 将新的执行结果加入记录。
     * 
     * @param result
     *     新的执行结果
     */
    private void addBatchResult(final int[] result) {
        batchResult = ArrayUtils.addAll(batchResult, result);
    }

    /**
     * 等待批量执行的语句数量清零。
     */
    private void resetBatchResult() {
        batchResult = ArrayUtils.EMPTY_INT_ARRAY;
    }

    @Override
    public void addBatch()
        throws SQLException {
        statement.addBatch();
        addBatchCount();
        if (checkBatchCount()) {
            int[] result = statement.executeBatch();
            addBatchResult(result);
            resetBatchCount();
        }
    }

    @Override
    public void addBatch(final String sql)
        throws SQLException {
        // 这里会抛出异常
        statement.addBatch(sql);
    }

    @Override
    public void cancel()
        throws SQLException {
        statement.cancel();
    }

    @Override
    public void clearBatch()
        throws SQLException {
        statement.clearBatch();
        resetBatchCount();
        resetBatchResult();
    }

    @Override
    public void clearParameters()
        throws SQLException {
        statement.clearParameters();
    }

    @Override
    public void clearWarnings()
        throws SQLException {
        statement.clearWarnings();
    }

    @Override
    public void close()
        throws SQLException {
        statement.close();
        resetBatchCount();
        resetBatchResult();
    }

    @Override
    public boolean execute()
        throws SQLException {
        return statement.execute();
    }

    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys)
        throws SQLException {
        return statement.execute(sql, autoGeneratedKeys);
    }

    @Override
    public boolean execute(final String sql, final int[] columnIndexes)
        throws SQLException {
        return statement.execute(sql, columnIndexes);
    }

    @Override
    public boolean execute(final String sql, final String[] columnNames)
        throws SQLException {
        return statement.execute(sql, columnNames);
    }

    @Override
    public boolean execute(final String sql)
        throws SQLException {
        return statement.execute(sql);
    }

    @Override
    public int[] executeBatch()
        throws SQLException {
        int[] result = statement.executeBatch();
        addBatchResult(result);
        result = batchResult;
        resetBatchCount();
        resetBatchResult();
        return result;
    }

    @Override
    public ResultSet executeQuery()
        throws SQLException {
        return statement.executeQuery();
    }

    @Override
    public ResultSet executeQuery(final String sql)
        throws SQLException {
        return statement.executeQuery(sql);
    }

    @Override
    public int executeUpdate()
        throws SQLException {
        return statement.executeUpdate();
    }

    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys)
        throws SQLException {
        return statement.executeUpdate(sql, autoGeneratedKeys);
    }

    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes)
        throws SQLException {
        return statement.executeUpdate(sql, columnIndexes);
    }

    @Override
    public int executeUpdate(final String sql, final String[] columnNames)
        throws SQLException {
        return statement.executeUpdate(sql, columnNames);
    }

    @Override
    public int executeUpdate(final String sql)
        throws SQLException {
        return statement.executeUpdate(sql);
    }

    @Override
    public Connection getConnection()
        throws SQLException {
        return statement.getConnection();
    }

    @Override
    public int getFetchDirection()
        throws SQLException {
        return statement.getFetchDirection();
    }

    @Override
    public int getFetchSize()
        throws SQLException {
        return statement.getFetchSize();
    }

    @Override
    public ResultSet getGeneratedKeys()
        throws SQLException {
        return statement.getGeneratedKeys();
    }

    @Override
    public int getMaxFieldSize()
        throws SQLException {
        return statement.getMaxFieldSize();
    }

    @Override
    public int getMaxRows()
        throws SQLException {
        return statement.getMaxRows();
    }

    @Override
    public ResultSetMetaData getMetaData()
        throws SQLException {
        return statement.getMetaData();
    }

    @Override
    public boolean getMoreResults()
        throws SQLException {
        return statement.getMoreResults();
    }

    @Override
    public boolean getMoreResults(final int current)
        throws SQLException {
        return statement.getMoreResults(current);
    }

    @Override
    public ParameterMetaData getParameterMetaData()
        throws SQLException {
        return statement.getParameterMetaData();
    }

    @Override
    public int getQueryTimeout()
        throws SQLException {
        return statement.getQueryTimeout();
    }

    @Override
    public ResultSet getResultSet()
        throws SQLException {
        return statement.getResultSet();
    }

    @Override
    public int getResultSetConcurrency()
        throws SQLException {
        return statement.getResultSetConcurrency();
    }

    @Override
    public int getResultSetHoldability()
        throws SQLException {
        return statement.getResultSetHoldability();
    }

    @Override
    public int getResultSetType()
        throws SQLException {
        return statement.getResultSetType();
    }

    @Override
    public int getUpdateCount()
        throws SQLException {
        return statement.getUpdateCount();
    }

    @Override
    public SQLWarning getWarnings()
        throws SQLException {
        return statement.getWarnings();
    }

    @Override
    public boolean isClosed()
        throws SQLException {
        return statement.isClosed();
    }

    @Override
    public boolean isPoolable()
        throws SQLException {
        return statement.isPoolable();
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface)
        throws SQLException {
        if (iface.isAssignableFrom(statement.getClass())) {
            return true;
        }
        return statement.isWrapperFor(iface);
    }

    @Override
    public void setArray(final int parameterIndex, final Array x)
        throws SQLException {
        statement.setArray(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final int length)
        throws SQLException {
        statement.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final long length)
        throws SQLException {
        statement.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x)
        throws SQLException {
        statement.setAsciiStream(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(final int parameterIndex, final BigDecimal x)
        throws SQLException {
        statement.setBigDecimal(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final int length)
        throws SQLException {
        statement.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final long length)
        throws SQLException {
        statement.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x)
        throws SQLException {
        statement.setBinaryStream(parameterIndex, x);
    }

    @Override
    public void setBlob(final int parameterIndex, final Blob x)
        throws SQLException {
        statement.setBlob(parameterIndex, x);
    }

    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream, final long length)
        throws SQLException {
        statement.setBlob(parameterIndex, inputStream, length);
    }

    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream)
        throws SQLException {
        statement.setBlob(parameterIndex, inputStream);
    }

    @Override
    public void setBoolean(final int parameterIndex, final boolean x)
        throws SQLException {
        statement.setBoolean(parameterIndex, x);
    }

    @Override
    public void setByte(final int parameterIndex, final byte x)
        throws SQLException {
        statement.setByte(parameterIndex, x);
    }

    @Override
    public void setBytes(final int parameterIndex, final byte[] x)
        throws SQLException {
        statement.setBytes(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final int length)
        throws SQLException {
        statement.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final long length)
        throws SQLException {
        statement.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader)
        throws SQLException {
        statement.setCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setClob(final int parameterIndex, final Clob x)
        throws SQLException {
        statement.setClob(parameterIndex, x);
    }

    @Override
    public void setClob(final int parameterIndex, final Reader reader, final long length)
        throws SQLException {
        statement.setClob(parameterIndex, reader, length);
    }

    @Override
    public void setClob(final int parameterIndex, final Reader reader)
        throws SQLException {
        statement.setClob(parameterIndex, reader);
    }

    @Override
    public void setCursorName(final String name)
        throws SQLException {
        statement.setCursorName(name);
    }

    @Override
    public void setDate(final int parameterIndex, final Date x)
        throws SQLException {
        statement.setDate(parameterIndex, x);
    }

    @Override
    public void setDate(final int parameterIndex, final Date x, final Calendar cal)
        throws SQLException {
        statement.setDate(parameterIndex, x, cal);
    }

    @Override
    public void setDouble(final int parameterIndex, final double x)
        throws SQLException {
        statement.setDouble(parameterIndex, x);
    }

    @Override
    public void setEscapeProcessing(final boolean enable)
        throws SQLException {
        statement.setEscapeProcessing(enable);
    }

    @Override
    public void setFetchDirection(final int direction)
        throws SQLException {
        statement.setFetchDirection(direction);
    }

    @Override
    public void setFetchSize(final int rows)
        throws SQLException {
        statement.setFetchSize(rows);
    }

    @Override
    public void setFloat(final int parameterIndex, final float x)
        throws SQLException {
        statement.setFloat(parameterIndex, x);
    }

    @Override
    public void setInt(final int parameterIndex, final int x)
        throws SQLException {
        statement.setInt(parameterIndex, x);
    }

    @Override
    public void setLong(final int parameterIndex, final long x)
        throws SQLException {
        statement.setLong(parameterIndex, x);
    }

    @Override
    public void setMaxFieldSize(final int max)
        throws SQLException {
        statement.setMaxFieldSize(max);
    }

    @Override
    public void setMaxRows(final int max)
        throws SQLException {
        statement.setMaxRows(max);
    }

    @Override
    public void setNCharacterStream(final int parameterIndex, final Reader value, final long length)
        throws SQLException {
        statement.setNCharacterStream(parameterIndex, value, length);
    }

    @Override
    public void setNCharacterStream(final int parameterIndex, final Reader value)
        throws SQLException {
        statement.setNCharacterStream(parameterIndex, value);
    }

    @Override
    public void setNClob(final int parameterIndex, final NClob value)
        throws SQLException {
        statement.setNClob(parameterIndex, value);
    }

    @Override
    public void setNClob(final int parameterIndex, final Reader reader, final long length)
        throws SQLException {
        statement.setNClob(parameterIndex, reader, length);
    }

    @Override
    public void setNClob(final int parameterIndex, final Reader reader)
        throws SQLException {
        statement.setNClob(parameterIndex, reader);
    }

    @Override
    public void setNString(final int parameterIndex, final String value)
        throws SQLException {
        statement.setNString(parameterIndex, value);
    }

    @Override
    public void setNull(final int parameterIndex, final int sqlType)
        throws SQLException {
        statement.setNull(parameterIndex, sqlType);
    }

    @Override
    public void setNull(final int parameterIndex, final int sqlType, final String typeName)
        throws SQLException {
        statement.setNull(parameterIndex, sqlType, typeName);
    }

    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scaleOrLength)
        throws SQLException {
        statement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType)
        throws SQLException {
        statement.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void setObject(final int parameterIndex, final Object x)
        throws SQLException {
        statement.setObject(parameterIndex, x);
    }

    @Override
    public void setPoolable(final boolean poolable)
        throws SQLException {
        statement.setPoolable(poolable);
    }

    @Override
    public void setQueryTimeout(final int seconds)
        throws SQLException {
        statement.setQueryTimeout(seconds);
    }

    @Override
    public void setRef(final int parameterIndex, final Ref x)
        throws SQLException {
        statement.setRef(parameterIndex, x);
    }

    @Override
    public void setRowId(final int parameterIndex, final RowId x)
        throws SQLException {
        statement.setRowId(parameterIndex, x);
    }

    @Override
    public void setShort(final int parameterIndex, final short x)
        throws SQLException {
        statement.setShort(parameterIndex, x);
    }

    @Override
    public void setSQLXML(final int parameterIndex, final SQLXML xmlObject)
        throws SQLException {
        statement.setSQLXML(parameterIndex, xmlObject);
    }

    @Override
    public void setString(final int parameterIndex, final String x)
        throws SQLException {
        statement.setString(parameterIndex, x);
    }

    @Override
    public void setTime(final int parameterIndex, final Time x)
        throws SQLException {
        statement.setTime(parameterIndex, x);
    }

    @Override
    public void setTime(final int parameterIndex, final Time x, final Calendar cal)
        throws SQLException {
        statement.setTime(parameterIndex, x, cal);
    }

    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x)
        throws SQLException {
        statement.setTimestamp(parameterIndex, x);
    }

    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal)
        throws SQLException {
        statement.setTimestamp(parameterIndex, x, cal);
    }

    @Deprecated
    @Override
    public void setUnicodeStream(final int parameterIndex, final InputStream x, final int length)
        throws SQLException {
        statement.setUnicodeStream(parameterIndex, x, length);
    }

    @Override
    public void setURL(final int parameterIndex, final URL x)
        throws SQLException {
        statement.setURL(parameterIndex, x);
    }

    @Override
    public <T> T unwrap(final Class<T> iface)
        throws SQLException {
        if (iface.isAssignableFrom(statement.getClass())) {
            return (T) statement;
        }
        return statement.unwrap(iface);
    }

    @Override
    public void closeOnCompletion()
        throws SQLException {
        statement.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion()
        throws SQLException {
        return statement.isCloseOnCompletion();
    }
}
