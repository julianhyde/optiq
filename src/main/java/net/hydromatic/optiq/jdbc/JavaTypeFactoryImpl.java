/*
 * Copyright (c) 2007-2009 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Cascading is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cascading is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cascading.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.hydromatic.optiq.jdbc;

import net.hydromatic.optiq.impl.java.JavaTypeFactory;
import net.hydromatic.optiq.runtime.ByteString;
import org.eigenbase.reltype.RelDataType;
import org.eigenbase.reltype.RelDataTypeField;
import org.eigenbase.reltype.RelDataTypeFieldImpl;
import org.eigenbase.reltype.RelRecordType;
import org.eigenbase.sql.type.BasicSqlType;
import org.eigenbase.sql.type.SqlTypeFactoryImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class JavaTypeFactoryImpl
        extends SqlTypeFactoryImpl
        implements JavaTypeFactory {
    public RelDataType createStructType(Class type) {
        List<RelDataTypeField> list = new ArrayList<RelDataTypeField>();
        for (Field field : type.getFields()) {
            // FIXME: watch out for recursion
            list.add(
                    new RelDataTypeFieldImpl(
                            field.getName(),
                            list.size(),
                            createType(field.getType())));
        }
        return canonize(
                new JavaRecordType(
                        list.toArray(new RelDataTypeField[list.size()]),
                        type));
    }

    public RelDataType createType(Type type) {
        if (type instanceof RelDataType) {
            return (RelDataType) type;
        }
        if (!(type instanceof Class)) {
            throw new UnsupportedOperationException(
                    "TODO: implement " + type + ": " + type.getClass());
        }
        final Class clazz = (Class) type;
        if (clazz.isPrimitive()) {
            return createJavaType(clazz);
        } else if (clazz == String.class) {
            // TODO: similar special treatment for BigDecimal, BigInteger,
            //  Date, Time, Timestamp, Double etc.
            return createJavaType(clazz);
        } else if (clazz.isArray()) {
            return createMultisetType(
                    createType(clazz.getComponentType()), -1);
        } else {
            return createStructType(clazz);
        }
    }

    public Type getJavaClass(RelDataType type) {
        if (type instanceof RelRecordType) {
            JavaRecordType javaRecordType;
            if (type instanceof JavaRecordType) {
                javaRecordType = (JavaRecordType) type;
                return javaRecordType.clazz;
            } else {
                return (RelRecordType) type;
            }
        }
        if (type instanceof JavaType) {
            JavaType javaType = (JavaType) type;
            return javaType.getJavaClass();
        }
        if (type.isStruct() && type.getFieldCount() == 1) {
            return getJavaClass(type.getFieldList().get(0).getType());
        }
        if (type instanceof BasicSqlType) {
            switch (type.getSqlTypeName()) {
                case VARCHAR:
                case CHAR:
                    return String.class;
                case INTEGER:
                    return Integer.class;
                case BIGINT:
                    return Long.class;
                case SMALLINT:
                    return Short.class;
                case TINYINT:
                    return Byte.class;
                case DECIMAL:
                    return BigDecimal.class;
                case BOOLEAN:
                    return Boolean.class;
                case BINARY:
                case VARBINARY:
                    return ByteString.class;
                case DATE:
                    return java.sql.Date.class;
                case TIME:
                    return Time.class;
                case TIMESTAMP:
                    return Timestamp.class;
            }
        }
        return null;
    }
}
