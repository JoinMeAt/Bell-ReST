package com.bell.util;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import org.joda.time.DateTime;

public class WebServiceCallLogger {

	public static long openWebServiceCallReport(Connection con, String path, String inputs) {
		long id = -1;
		CallableStatement cs = null;
		
		try {
			cs = con.prepareCall("{call sp_OpenWebServiceReport(?,?,?,?,?,?,?)}");
			cs.setString("WebServicePath", path);
			cs.setString("InputParameters", inputs);
			cs.setLong("TimeStartedUTC", DateTime.now().getMillis());
			cs.setLong("WebServiceVersion", Constants.VERSION);
			cs.registerOutParameter("WebServiceCallReportID", java.sql.Types.BIGINT);
			cs.registerOutParameter("Success", java.sql.Types.BIT);
			cs.registerOutParameter("ErrorID", java.sql.Types.SMALLINT);
			cs.executeUpdate();
			
			if( cs.getBoolean("Success") ) {
				id = cs.getLong("WebServiceCallReportID");
				con.commit();
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.closeCallableStatement(cs);
		}
		
		return id;
	}
	
	public static boolean closeWebServiceCallReport(Connection con, long id) {
		CallableStatement cs = null;
		
		try {
			cs = con.prepareCall("{call sp_CloseWebServiceReport(?,?,?,?)}");
			cs.setLong("WebServiceCallReportID", id);
			cs.setLong("TimeFinishedUTC", DateTime.now().getMillis());
			cs.registerOutParameter("Success", java.sql.Types.BIT);
			cs.registerOutParameter("ErrorID", java.sql.Types.SMALLINT);
			cs.executeUpdate();
			
			if( cs.getBoolean("Success") ) {
				con.commit();
				return true;
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.closeCallableStatement(cs);
		}
		
		return false;
	}
	
	public static long failWebServiceCall(Connection con, long id, Exception e) {
		return failWebServiceCall(con, id, e.getMessage());
	}

	public static long failWebServiceCall(Connection con, long id, BellException e) {
		return failWebServiceCall(con, id, e.getMessage());
	}
		
	public static long failWebServiceCall(Connection con, long id, String message) {
		CallableStatement cs = null;
		
		try {
			cs = con.prepareCall("{call sp_FailWebServiceReport(?,?,?,?,?)}");
			cs.setLong("WebServiceCallReportID", id);
			cs.setLong("TimeFinishedUTC", DateTime.now().getMillis());
			cs.setString("ExceptionText", message);
			cs.registerOutParameter("Success", java.sql.Types.BIT);
			cs.registerOutParameter("ErrorID", java.sql.Types.SMALLINT);
			cs.executeUpdate();
			
			if( cs.getBoolean("Success") ) {
				id = cs.getLong("WebServiceCallReportID");
				con.commit();
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.closeCallableStatement(cs);
		}
		
		return id;
	}
}
