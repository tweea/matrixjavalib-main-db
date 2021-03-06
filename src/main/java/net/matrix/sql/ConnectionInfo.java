/*
 * 版权所有 2020 Matrix。
 * 保留所有权利。
 */
package net.matrix.sql;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库连接信息。
 */
public class ConnectionInfo
    implements Serializable {
    private static final long serialVersionUID = 1L;

    // 连接信息
    /**
     * {@link Driver} 类名。
     */
    private final String driverClass;

    /**
     * 连接 URL。
     */
    private final String url;

    /**
     * 用户名。
     */
    private final String username;

    /**
     * 密码。
     */
    private final String password;

    // 元数据
    /**
     * 数据库类型。
     */
    private String databaseType;

    /**
     * {@link Driver} 名称。
     */
    private String driverName;

    /**
     * 构造并获取连接信息。
     * 
     * @param driverClass
     *     {@link Driver} 类名
     * @param url
     *     连接 URL
     * @param username
     *     用户名
     * @param password
     *     密码
     * @throws SQLException
     *     获取信息时出错
     */
    public ConnectionInfo(final String driverClass, final String url, final String username, final String password)
        throws SQLException {
        this.driverClass = driverClass;
        this.url = url;
        this.username = username;
        this.password = password;
        readMetaData();
    }

    /**
     * 从数据库读取元数据。
     * 
     * @throws SQLException
     *     建立连接失败或读取信息失败
     */
    private void readMetaData()
        throws SQLException {
        try (Connection connection = getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            databaseType = metadata.getDatabaseProductName();
            driverName = metadata.getDriverName();
        }
    }

    /**
     * {@link Driver} 类名。
     */
    public String getDriverClass() {
        return driverClass;
    }

    /**
     * 连接 URL。
     */
    public String getUrl() {
        return url;
    }

    /**
     * 用户名。
     */
    public String getUsername() {
        return username;
    }

    /**
     * 密码。
     */
    public String getPassword() {
        return password;
    }

    /**
     * 数据库类型。
     */
    public String getDatabaseType() {
        return databaseType;
    }

    /**
     * {@link Driver} 名称。
     */
    public String getDriverName() {
        return driverName;
    }

    /**
     * 使用数据库连接信息建立一个连接。
     * 
     * @return 新建的连接。
     * @throws SQLException
     *     找不到驱动类或建立连接失败
     */
    public Connection getConnection()
        throws SQLException {
        try {
            Class.forName(driverClass);
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
    }
}
