package com.github.housepower.jdbc.statement;

import com.github.housepower.jdbc.ClickHouseConnection;
import com.github.housepower.jdbc.misc.Validate;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;

public abstract class AbstractPreparedStatement extends ClickHouseStatement {

    private final String[] queryParts;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

    protected Object[] parameters;

    public AbstractPreparedStatement(ClickHouseConnection connection, String[] queryParts) {
        super(connection);
        this.queryParts = queryParts;
        if (queryParts != null && queryParts.length > 0)
            this.parameters = new Object[queryParts.length];
    }

    @Override
    public void setURL(int index, URL x) throws SQLException {
        setObject(index, x);
    }

    @Override
    public void setInt(int index, int x) throws SQLException {
        setObject(index, x);
    }

    @Override
    public void setByte(int index, byte x) throws SQLException {
        setObject(index, x);
    }

    @Override
    public void setLong(int index, long x) throws SQLException {
        setObject(index, x);
    }

    @Override
    public void setDate(int index, Date x) throws SQLException {
        setObject(index, x);
    }

    @Override
    public void setShort(int index, short x) throws SQLException {
        setObject(index, x);
    }

    @Override
    public void setFloat(int index, float x) throws SQLException {
        setObject(index, x);
    }

    @Override
    public void setArray(int index, Array x) throws SQLException {
        setObject(index, x);
    }

    @Override
    public void setNull(int index, int type) throws SQLException {
        setObject(index, null);
    }

    @Override
    public void setDouble(int index, double x) throws SQLException {
        setObject(index, x);
    }

    @Override
    public void setString(int index, String x) throws SQLException {
        setObject(index, x);
    }

    @Override
    public void setTimestamp(int index, Timestamp x) throws SQLException {
        setObject(index, x);
    }

    @Override
    public void setBigDecimal(int index, BigDecimal x) throws SQLException {
        setObject(index, x);
    }

    @Override
    public void setObject(int index, Object x, int targetSqlType) throws SQLException {
        setObject(index, x);
    }

    @Override
    public void setObject(int index, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        setObject(index, x);
    }

    @Override
    public void clearParameters() throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = null;
        }
    }

    protected String assembleQueryPartsAndParameters() throws SQLException {
        // TODO: move to DataType
        StringBuilder queryBuilder = new StringBuilder();
        for (int i = 0; i < queryParts.length; i++) {
            if (i - 1 >= 0 && i - 1 < parameters.length) {
                Validate.isTrue(assembleParameter(parameters[i - 1], queryBuilder),
                        "UNKNOWN DataType :" + (parameters[i - 1] == null ? null : parameters[i - 1].getClass()));
            }
            queryBuilder.append(queryParts[i]);
        }
        return queryBuilder.toString();
    }

    private boolean assembleParameter(Object parameter, StringBuilder queryBuilder) throws SQLException {
        return assembleSimpleParameter(queryBuilder, parameter)
                || assembleComplexQuotedParameter(queryBuilder, parameter);

    }

    private boolean assembleSimpleParameter(StringBuilder queryBuilder, Object parameter) {
        if (parameter == null) {
            return assembleWithoutQuotedParameter(queryBuilder, "Null");
        } else if (parameter instanceof Number) {
            return assembleWithoutQuotedParameter(queryBuilder, parameter);
        } else if (parameter instanceof String) {
            return assembleQuotedParameter(queryBuilder, String.valueOf(parameter));
        } else if (parameter instanceof Date || parameter instanceof Timestamp) {
            SimpleDateFormat format = parameter instanceof Date ? dateFormat : timestampFormat;
            return assembleQuotedParameter(queryBuilder, format.format(parameter));
        }
        return false;
    }

    private boolean assembleQuotedParameter(StringBuilder queryBuilder, String parameter) {
        queryBuilder.append("'");
        queryBuilder.append(parameter.replaceAll("'", Matcher.quoteReplacement("\\'")));
        queryBuilder.append("'");
        return true;
    }

    private boolean assembleWithoutQuotedParameter(StringBuilder queryBuilder, Object parameter) {
        queryBuilder.append(parameter);
        return true;
    }

    private boolean assembleComplexQuotedParameter(StringBuilder queryBuilder, Object parameter) throws SQLException {
        if (parameter instanceof Array) {
            queryBuilder.append("[");
            Object[] arrayData = (Object[]) ((Array) parameter).getArray();
            for (int arrayIndex = 0; arrayIndex < arrayData.length; arrayIndex++) {
                assembleParameter(arrayData[arrayIndex], queryBuilder);
                queryBuilder.append(arrayIndex == arrayData.length - 1 ? "]" : ",");
            }
            return true;
        } else if (parameter instanceof Struct) {
            queryBuilder.append("(");
            Object[] structData = ((Struct) parameter).getAttributes();
            for (int structIndex = 0; structIndex < structData.length; structIndex++) {
                assembleParameter(structData[structIndex], queryBuilder);
                queryBuilder.append(structIndex == structData.length - 1 ? ")" : ",");
            }
            return true;
        }
        return false;
    }
}