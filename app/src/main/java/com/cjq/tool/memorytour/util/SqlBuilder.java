package com.cjq.tool.memorytour.util;

import android.database.SQLException;

import java.sql.SQLDataException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by KAT on 2016/9/7.
 */
public class SqlBuilder {

    private StringBuilder builder;
    private Pattern pattern;
    private String tableName;
    private LinkedList<String> fields = new LinkedList<>();
    private LinkedList<Object> values = new LinkedList<>();

    public SqlBuilder setPattern(Pattern pattern) {
        if (pattern == null)
            throw new SQLException("SQL命令模式不得为空");
        this.pattern = pattern;
        return this;
    }

    public SqlBuilder setTableName(String tableName) {
        if (tableName == null || tableName.isEmpty())
            throw new SQLException("列表名称不得为空");
        this.tableName = tableName;
        return this;
    }

    public SqlBuilder appendElement(String field, Object value) {
        if (field == null || field.isEmpty())
            throw new SQLException("字段名称不得为空");
        fields.offer(field);
        values.offer(value);
        return this;
    }

    //只保证正确输入导出正确输出
    @Override
    public String toString() {
        builder.setLength(0);
        switch (pattern) {
            case INSERT:{
                if (fields.isEmpty())
                    throw new SQLException("至少插入一个字段值");
                builder.append("INSERT INTO ")
                        .append(tableName)
                        .append(" (")
                        .append(fields.remove());
                while (!fields.isEmpty()) {
                    builder.append(",")
                            .append(fields.remove());
                }
                builder.append(") VALUES (");
                while (true) {
                    Object value = values.remove();
                    if (value instanceof String) {
                        builder.append("'")
                                .append(value)
                                .append("'");
                    } else {
                        builder.append(value);
                    }
                    if (values.isEmpty()) {
                        break;
                    } else {
                        builder.append(",");
                    }
                }
                builder.append(")");
            } break;
        }
        return builder.toString();
    }

    public enum Pattern {
        SELECT,
        INSERT,
        UPDATE,
        DELETE,
        CREATE,
        DROP;

    }

//    public enum DataType {
//        VARCHAR("VARCHAR"),
//        NVARCHAR("NVARCHAR"),
//        TEXT("TEXT"),
//        INTEGER("INTEGER"),
//        FLOAT("FLOAT"),
//        BOOLEAN("BOOLEAN"),
//        CLOB("CLOB"),
//        BLOB("BLOB"),
//        TIMESTAMP("TIMESTAMP"),
//        NUMERIC("NUMERIC"),
//        VARYING_CHARACTER("VARYING CHARACTER"),
//        NATIONAL_VARYING_CHARACTER("NATIONAL VARYING CHARACTER"),
//        REAL("REAL");
//
//        DataType(String label) {
//            this.label = label;
//        }
//
//        @Override
//        public String toString() {
//            return label;
//        }
//
//        private String label;
//    }
//
//    public static class Table {
//
//        private String name;
//        private String primaryKeyName;
//        private DataType primaryKeyType;
//        private String foreignTableName;
//        private String foreignKeyName;
//        private StringBuilder builder;
//
//        public Table setName(String name) {
//            this.name = name;
//            return this;
//        }
//
//        public Table setPrimaryKeyName(String primaryKeyName) {
//            this.primaryKeyName = primaryKeyName;
//            return this;
//        }
//
//        public Table setPrimaryKeyType(DataType primaryKeyType) {
//            this.primaryKeyType = primaryKeyType;
//            return this;
//        }
//
//        public Table setForeignTableName(String foreignTableName) {
//            this.foreignTableName = foreignTableName;
//            return this;
//        }
//
//        public Table setForeignKeyName(String foreignKeyName) {
//            this.foreignKeyName = foreignKeyName;
//            return this;
//        }
//    }
}
