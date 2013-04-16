package org.apache.oozie.tools;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

public class FakeDriver implements Driver{

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url!=null && url.contains("fake");
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
       
        return new FakeConnection();
    }

    @Override
    public int getMajorVersion() {
       
        return 4;
    }

    @Override
    public int getMinorVersion() {
       
        return 0;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean jdbcCompliant() {
        // TODO Auto-generated method stub
        return false;
    }

}
