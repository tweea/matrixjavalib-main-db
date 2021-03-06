/*
 * 版权所有 2020 Matrix。
 * 保留所有权利。
 */
package net.matrix.sql.hibernate.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.jadira.usertype.spi.shared.AbstractParameterizedUserType;
import org.jadira.usertype.spi.shared.ConfigurationHelper;

/**
 * 将数据库中的字符串值作为整形列表值处理的类型。
 */
public class IntegerListAsStringType
    extends AbstractParameterizedUserType<List<Integer>, String, StringColumnIntegerListMapper> {
    private static final long serialVersionUID = -4260341801303326744L;

    @Override
    public List<Integer> nullSafeGet(final ResultSet resultSet, final String[] strings, final SharedSessionContractImplementor session, final Object object)
        throws SQLException {
        List<Integer> list = super.nullSafeGet(resultSet, strings, session, object);
        if (list == null) {
            return new ArrayList();
        }
        return list;
    }

    @Override
    public void nullSafeSet(final PreparedStatement preparedStatement, final Object value, final int index, final SharedSessionContractImplementor session)
        throws SQLException {
        if (value == null) {
            preparedStatement.setNull(index, Types.VARCHAR);
            return;
        }
        List<Integer> v = (List) value;
        if (v.isEmpty()) {
            preparedStatement.setNull(index, Types.VARCHAR);
            return;
        }
        super.nullSafeSet(preparedStatement, value, index, session);
    }

    @Override
    public void applyConfiguration(final SessionFactory sessionFactory) {
        super.applyConfiguration(sessionFactory);

        StringColumnIntegerListMapper columnMapper = getColumnMapper();

        String separator = null;
        if (getParameterValues() != null) {
            separator = getParameterValues().getProperty("separator");
        }
        if (separator == null) {
            separator = ConfigurationHelper.getProperty("separator");
        }

        if (separator == null) {
            columnMapper.setSeparator(",");
        } else {
            columnMapper.setSeparator(separator);
        }
    }
}
