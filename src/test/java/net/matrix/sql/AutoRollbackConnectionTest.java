/*
 * 版权所有 2020 Matrix。
 * 保留所有权利。
 */
package net.matrix.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class AutoRollbackConnectionTest {
    @Test
    public void testClose()
        throws SQLException {
        Connection connection = Mockito.mock(Connection.class);

        try (Connection autoRollbackConnection = new AutoRollbackConnection(connection)) {
        }
        Mockito.verify(connection).isClosed();
        Mockito.verify(connection).getAutoCommit();
        Mockito.verify(connection).rollback();
        Mockito.verify(connection).close();
        Mockito.verifyNoMoreInteractions(connection);
    }

    @Test
    public void testClose_rollbackFail()
        throws SQLException {
        Connection connection = Mockito.mock(Connection.class);
        Mockito.doThrow(SQLException.class).when(connection).rollback();

        assertThatExceptionOfType(SQLException.class).isThrownBy(() -> {
            try (Connection autoRollbackConnection = new AutoRollbackConnection(connection)) {
            }
        });
        Mockito.verify(connection).isClosed();
        Mockito.verify(connection).getAutoCommit();
        Mockito.verify(connection).rollback();
        Mockito.verify(connection).close();
        Mockito.verifyNoMoreInteractions(connection);
    }

    @Test
    public void testClose_withAutoCommit()
        throws SQLException {
        Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.getAutoCommit()).thenReturn(true);

        try (Connection autoRollbackConnection = new AutoRollbackConnection(connection)) {
        }
        Mockito.verify(connection).isClosed();
        Mockito.verify(connection).getAutoCommit();
        Mockito.verify(connection).close();
        Mockito.verifyNoMoreInteractions(connection);
    }

    @Test
    public void testClose_closed()
        throws SQLException {
        Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.isClosed()).thenReturn(true);

        try (Connection autoRollbackConnection = new AutoRollbackConnection(connection)) {
        }
        Mockito.verify(connection).isClosed();
        Mockito.verifyNoMoreInteractions(connection);
    }

    @Test
    public void testClose_realConnection()
        throws SQLException {
        String driverClass = "org.apache.derby.jdbc.EmbeddedDriver";
        String url = "jdbc:derby:memory:mx_base;create=true";
        ConnectionInfo info = new ConnectionInfo(driverClass, url, "", "");
        Connection connection = info.getConnection();

        try (Connection autoRollbackConnection = new AutoRollbackConnection(connection)) {
            PreparedStatement ps = autoRollbackConnection.prepareStatement("VALUES CURRENT_DATE");
            ResultSet rs = ps.executeQuery();
            assertThat(rs.next()).isTrue();
            assertThat(rs.next()).isFalse();
        }
        assertThat(connection.isClosed()).isTrue();
    }
}
