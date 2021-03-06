/*
 * 版权所有 2020 Matrix。
 * 保留所有权利。
 */
package net.matrix.sql.hibernate.type;

import org.jadira.usertype.spi.shared.AbstractSingleColumnUserType;

/**
 * 将数据库中的整形值作为布尔值处理的类型。
 */
public class BooleanAsIntegerType
    extends AbstractSingleColumnUserType<Boolean, Integer, IntegerColumnBooleanMapper> {
    private static final long serialVersionUID = -1033841989530038459L;
}
