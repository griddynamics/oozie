package org.apache.oozie.tools;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.*;

public class FakeConnection implements Connection {
    public static boolean CREATE = true;
    public static boolean SYSTEM_TABLE=true;
    @Override
    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> arg0) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void commit() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Blob createBlob() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Clob createClob() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NClob createNClob() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Statement createStatement() throws SQLException {
        Statement result = mock(Statement.class);
        if (CREATE) {
            when(result.executeQuery("select count(*) from WF_JOBS where status IN ('RUNNING', 'SUSPENDED')")).thenReturn(
                    new FakeResultSet(-1));
        }
        else {
            when(result.executeQuery("select count(*) from WF_JOBS where status IN ('RUNNING', 'SUSPENDED')")).thenReturn(
                    new FakeResultSet(1));
        }
        if(SYSTEM_TABLE){
            when(result.executeQuery("select count(*) from OOZIE_SYS")).thenReturn(new FakeResultSet(1));

        }else{
            when(result.executeQuery("select count(*) from OOZIE_SYS")).thenReturn(new FakeResultSet(-1));

        }
        return result;
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getCatalog() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getHoldability() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        DatabaseMetaData result = mock(DatabaseMetaData.class);
        when(result.getJDBCMajorVersion()).thenReturn(4);
        when(result.supportsGetGeneratedKeys()).thenReturn(true);
        return result;
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isClosed() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void rollback() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        // TODO Auto-generated method stub

    }

    private static class FakeResultSet implements ResultSet {

        private int counter = 0;

        public FakeResultSet(int counter) {
            this.counter = counter;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean absolute(int row) throws SQLException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void afterLast() throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void beforeFirst() throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void cancelRowUpdates() throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void clearWarnings() throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void close() throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void deleteRow() throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public int findColumn(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean first() throws SQLException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Array getArray(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Array getArray(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public InputStream getAsciiStream(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public InputStream getAsciiStream(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public InputStream getBinaryStream(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public InputStream getBinaryStream(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Blob getBlob(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Blob getBlob(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean getBoolean(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean getBoolean(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public byte getByte(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public byte getByte(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public byte[] getBytes(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public byte[] getBytes(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Reader getCharacterStream(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Reader getCharacterStream(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Clob getClob(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Clob getClob(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getConcurrency() throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String getCursorName() throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Date getDate(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Date getDate(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Date getDate(int columnIndex, Calendar cal) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Date getDate(String columnLabel, Calendar cal) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public double getDouble(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public double getDouble(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getFetchDirection() throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getFetchSize() throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public float getFloat(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public float getFloat(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getHoldability() throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getInt(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getInt(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public long getLong(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public long getLong(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public ResultSetMetaData getMetaData() throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Reader getNCharacterStream(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Reader getNCharacterStream(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public NClob getNClob(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public NClob getNClob(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getNString(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getNString(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object getObject(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object getObject(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Ref getRef(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Ref getRef(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getRow() throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public RowId getRowId(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public RowId getRowId(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public SQLXML getSQLXML(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public SQLXML getSQLXML(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public short getShort(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public short getShort(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Statement getStatement() throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getString(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Time getTime(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Time getTime(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Time getTime(int columnIndex, Calendar cal) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Time getTime(String columnLabel, Calendar cal) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Timestamp getTimestamp(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Timestamp getTimestamp(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getType() throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public URL getURL(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public URL getURL(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public InputStream getUnicodeStream(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public InputStream getUnicodeStream(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void insertRow() throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isAfterLast() throws SQLException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isBeforeFirst() throws SQLException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isClosed() throws SQLException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isFirst() throws SQLException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isLast() throws SQLException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean last() throws SQLException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void moveToCurrentRow() throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void moveToInsertRow() throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean next() throws SQLException {
            if (counter == -1) {
                throw new SQLException();
            }
            counter--;
            return counter < 0;
        }

        @Override
        public boolean previous() throws SQLException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void refreshRow() throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean relative(int rows) throws SQLException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean rowDeleted() throws SQLException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean rowInserted() throws SQLException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean rowUpdated() throws SQLException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setFetchDirection(int direction) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void setFetchSize(int rows) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateArray(int columnIndex, Array x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateArray(String columnLabel, Array x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateBlob(int columnIndex, Blob x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateBlob(String columnLabel, Blob x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateBoolean(int columnIndex, boolean x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateBoolean(String columnLabel, boolean x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateByte(int columnIndex, byte x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateByte(String columnLabel, byte x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateBytes(int columnIndex, byte[] x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateBytes(String columnLabel, byte[] x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateClob(int columnIndex, Clob x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateClob(String columnLabel, Clob x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateClob(int columnIndex, Reader reader) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateClob(String columnLabel, Reader reader) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateDate(int columnIndex, Date x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateDate(String columnLabel, Date x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateDouble(int columnIndex, double x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateDouble(String columnLabel, double x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateFloat(int columnIndex, float x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateFloat(String columnLabel, float x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateInt(int columnIndex, int x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateInt(String columnLabel, int x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateLong(int columnIndex, long x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateLong(String columnLabel, long x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateNClob(int columnIndex, Reader reader) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateNClob(String columnLabel, Reader reader) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateNString(int columnIndex, String nString) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateNString(String columnLabel, String nString) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateNull(int columnIndex) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateNull(String columnLabel) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateObject(int columnIndex, Object x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateObject(String columnLabel, Object x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateRef(int columnIndex, Ref x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateRef(String columnLabel, Ref x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateRow() throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateRowId(int columnIndex, RowId x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateRowId(String columnLabel, RowId x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateShort(int columnIndex, short x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateShort(String columnLabel, short x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateString(int columnIndex, String x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateString(String columnLabel, String x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateTime(int columnIndex, Time x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateTime(String columnLabel, Time x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean wasNull() throws SQLException {
            // TODO Auto-generated method stub
            return false;
        }

    }
}
