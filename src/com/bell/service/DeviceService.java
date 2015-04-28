package com.bell.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.time.DateTimeZone;

import com.bell.User;
import com.bell.util.BellException;
import com.bell.util.DBConnection;
import com.bell.util.JsonTransformer;
import com.bell.util.WebServiceCallLogger;

@Path("device")
public class DeviceService {

	static {
		DateTimeZone.setDefault(DateTimeZone.UTC);
	}
	
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/register")
	public String registerGCM(
			@FormParam("deviceUUID") String deviceUUID,
			@FormParam("messagerID") String messagerID,
			@FormParam("deviceType") String deviceType)
	{
		String response = null;
		Connection con = null;
		CallableStatement cs = null;
		long id = -1; 
		
		try {
			con = DBConnection.getDBConnection();
			
			id = WebServiceCallLogger.openWebServiceCallReport(con, "device/register", deviceUUID + "," + deviceType+ "," + messagerID);
			
			cs = con.prepareCall("{call sp_RegisterMessagerID(?,?,?,?,?)}");
			cs.setString("DeviceMessagerID", messagerID);
			cs.setString("DeviceUUID", deviceUUID);
			cs.setString("DeviceType", deviceType);
			cs.registerOutParameter("Success", java.sql.Types.BIT);
			cs.registerOutParameter("ErrorID", java.sql.Types.SMALLINT);
			
			if( cs.execute() ) {
				ResultSet rs = cs.getResultSet();
				rs.next();

				User u = new User();				
				u.setUserID(rs.getLong("PK_UserID"));
				u.setFirstName(rs.getString("FirstName"));
				u.setLastName(rs.getString("LastName"));
				u.setEmail(rs.getString("Email"));
				
				con.commit();
				response = JsonTransformer.toJson(u);				
			} else {			
				if( cs.getBoolean("Success") ) {
					response = null;
				} else {
					throw new BellException(cs.getInt("ErrorID"));
				}
			}
			
			WebServiceCallLogger.closeWebServiceCallReport(con, id);
		} catch (SQLException e) {
			WebServiceCallLogger.failWebServiceCall(con, id, e);
			e.printStackTrace();
			response = JsonTransformer.toJson(new BellException(e));
		} catch( BellException be) {
			WebServiceCallLogger.failWebServiceCall(con, id, be);
			be.printStackTrace();
			response = JsonTransformer.toJson(be);			
		} catch( Exception e) {
			WebServiceCallLogger.failWebServiceCall(con, id, e);
			e.printStackTrace();
			response = JsonTransformer.toJson(new BellException(e));			
		} finally {
			DBConnection.closeConnection(con);
			DBConnection.closeCallableStatement(cs);
		}
		
		return response;
	}
}
