/*
 * 版权所有 2020 Matrix。
 * 保留所有权利。
 */
package net.matrix.sql.hibernate;

import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.matrix.lang.Resettable;
import net.matrix.sql.ConnectionInfo;

/**
 * Hibernate {@link SessionFactory} 管理器。
 */
public final class SessionFactoryManager
    implements Resettable {
    /**
     * 日志记录器。
     */
    private static final Logger LOG = LoggerFactory.getLogger(SessionFactoryManager.class);

    /**
     * 默认的 {@link SessionFactory} 名称。
     */
    public static final String DEFAULT_NAME = "";

    /**
     * 所有的实例。
     */
    private static final Map<String, SessionFactoryManager> INSTANCES = new ConcurrentHashMap<>();

    private final String factoryName;

    private final String configResource;

    private final ThreadLocal<HibernateTransactionContext> threadContext;

    private Configuration configuration;

    private ServiceRegistry serviceRegistry;

    private SessionFactory sessionFactory;

    /**
     * 默认实例。
     */
    public static SessionFactoryManager getInstance() {
        return INSTANCES.computeIfAbsent(DEFAULT_NAME, SessionFactoryManager::new);
    }

    /**
     * 命名实例。
     * 
     * @param name
     *     {@link SessionFactory} 名称
     * @return 实例
     * @throws IllegalStateException
     *     还未命名实例
     */
    public static SessionFactoryManager getInstance(final String name) {
        if (DEFAULT_NAME.equals(name)) {
            return getInstance();
        }
        if (!isNameUsed(name)) {
            throw new IllegalStateException("名称 " + name + " 没有命名");
        }
        return INSTANCES.get(name);
    }

    /**
     * 判断 {@link SessionFactory} 名称是否已被占用。
     * 
     * @param name
     *     {@link SessionFactory} 名称
     * @return 是否已被占用
     */
    public static boolean isNameUsed(final String name) {
        return INSTANCES.containsKey(name);
    }

    /**
     * 命名默认配置文件到指定名称。
     * 
     * @param name
     *     {@link SessionFactory} 名称
     * @throws IllegalStateException
     *     名称已被占用
     */
    public static void nameSessionFactory(final String name) {
        synchronized (INSTANCES) {
            if (isNameUsed(name)) {
                throw new IllegalStateException("名称 " + name + " 已被占用");
            }
            INSTANCES.put(name, new SessionFactoryManager(name));
        }
    }

    /**
     * 命名一个配置文件到指定名称。
     * 
     * @param name
     *     {@link SessionFactory} 名称
     * @param configResource
     *     {@link SessionFactory} 配置资源
     * @throws IllegalStateException
     *     名称已被占用
     */
    public static void nameSessionFactory(final String name, final String configResource) {
        synchronized (INSTANCES) {
            if (isNameUsed(name)) {
                throw new IllegalStateException("名称 " + name + " 已被占用");
            }
            INSTANCES.put(name, new SessionFactoryManager(name, configResource));
        }
    }

    /**
     * 清除所有 {@link SessionFactory} 配置。
     */
    public static void clearAll() {
        resetAll();
        INSTANCES.clear();
    }

    /**
     * 重置所有 {@link SessionFactory} 配置。
     */
    public static void resetAll() {
        for (SessionFactoryManager instance : INSTANCES.values()) {
            instance.reset();
        }
    }

    private SessionFactoryManager(final String name) {
        this.factoryName = name;
        this.configResource = null;
        this.threadContext = new ThreadLocal<>();
    }

    private SessionFactoryManager(final String name, final String configResource) {
        this.factoryName = name;
        this.configResource = configResource;
        this.threadContext = new ThreadLocal<>();
    }

    /**
     * 关闭 {@link SessionFactory}。
     */
    @Override
    public void reset() {
        if (sessionFactory != null) {
            try {
                sessionFactory.close();
                LOG.info("{} 配置的 Hibernate SessionFactory 已关闭。", factoryName);
            } catch (HibernateException e) {
                LOG.error("{} 配置的 Hibernate SessionFactory 关闭失败。", factoryName, e);
            } finally {
                sessionFactory = null;
            }
        }
        if (serviceRegistry != null) {
            StandardServiceRegistryBuilder.destroy(serviceRegistry);
            LOG.info("{} 配置的 Hibernate ServiceRegistry 已销毁。", factoryName);
            serviceRegistry = null;
        }
        configuration = null;
    }

    /**
     * 获取 {@link SessionFactory} 配置。
     * 
     * @return {@link SessionFactory} 配置
     */
    public Configuration getConfiguration()
        throws SQLException {
        try {
            if (configuration == null) {
                if (configResource == null) {
                    LOG.info("读取默认的 Hibernate 配置。");
                    configuration = new Configuration().configure();
                } else {
                    LOG.info("读取 {} 的 Hibernate 配置。", configResource);
                    configuration = new Configuration().configure(configResource);
                }
            }
            return configuration;
        } catch (HibernateException e) {
            throw new SQLException(e);
        }
    }

    /**
     * 获取 {@link ServiceRegistry}。
     * 
     * @return {@link ServiceRegistry}
     */
    public ServiceRegistry getServiceRegistry()
        throws SQLException {
        try {
            if (serviceRegistry == null) {
                if (DEFAULT_NAME.equals(factoryName)) {
                    LOG.info("以默认配置构建 Hibernate ServiceRegistry。");
                } else {
                    LOG.info("以 {} 配置构建 Hibernate ServiceRegistry。", factoryName);
                }
                serviceRegistry = new StandardServiceRegistryBuilder().applySettings(getConfiguration().getProperties()).build();
            }
            return serviceRegistry;
        } catch (HibernateException e) {
            throw new SQLException(e);
        }
    }

    /**
     * 获取 {@link SessionFactory}。
     * 
     * @return {@link SessionFactory}
     */
    public SessionFactory getSessionFactory()
        throws SQLException {
        try {
            if (sessionFactory == null) {
                if (DEFAULT_NAME.equals(factoryName)) {
                    LOG.info("以默认配置构建 Hibernate SessionFactory。");
                } else {
                    LOG.info("以 {} 配置构建 Hibernate SessionFactory。", factoryName);
                }
                sessionFactory = getConfiguration().buildSessionFactory(getServiceRegistry());
            }
            return sessionFactory;
        } catch (HibernateException e) {
            throw new SQLException(e);
        }
    }

    /**
     * 使用 {@link SessionFactory} 建立 {@link Session}。
     * 
     * @return 新建的 {@link Session}
     * @throws SQLException
     *     建立失败
     */
    public Session createSession()
        throws SQLException {
        try {
            return getSessionFactory().openSession();
        } catch (HibernateException e) {
            throw new SQLException(e);
        }
    }

    /**
     * 获取当前顶层事务上下文，没有则建立。
     * 
     * @return 当前顶层事务上下文
     */
    public HibernateTransactionContext getTransactionContext() {
        HibernateTransactionContext context = threadContext.get();
        if (context == null) {
            context = new HibernateTransactionContext(factoryName);
            threadContext.set(context);
        }
        return context;
    }

    /**
     * 丢弃顶层事务上下文。
     * 
     * @throws SQLException
     *     回滚发生错误
     */
    public void dropTransactionContext()
        throws SQLException {
        HibernateTransactionContext context = threadContext.get();
        if (context == null) {
            return;
        }
        threadContext.remove();
        try {
            context.rollback();
        } finally {
            context.release();
        }
    }

    /**
     * 获取 {@link SessionFactory} 相关连接信息。
     * 
     * @return 连接信息
     * @throws SQLException
     *     信息获取失败
     */
    public ConnectionInfo getConnectionInfo()
        throws SQLException {
        Properties properties = getConfiguration().getProperties();
        String driver = properties.getProperty(AvailableSettings.DRIVER);
        String url = properties.getProperty(AvailableSettings.URL);
        String user = properties.getProperty(AvailableSettings.USER);
        String pass = properties.getProperty(AvailableSettings.PASS);
        return new ConnectionInfo(driver, url, user, pass);
    }
}
