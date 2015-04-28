package com.bell.service;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

import org.joda.time.DateTimeZone;

import com.amazonaws.services.directconnect.model.Connection;
import com.bell.Manager;
import com.bell.Server;
import com.bell.User;
import com.bell.util.BellException;
import com.bell.util.DBConnection;
import com.bell.util.JsonTransformer;
import com.bell.util.WebServiceCallLogger;

@Path("user")
public class UserService {
	
	static {
		DateTimeZone.setDefault(DateTimeZone.UTC);
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/")
	public String login(
			@FormParam("email") String email,
			@FormParam("password") String password ) {
		String response = null;
		Connection con = null;
		CallableStatement cs = null;
		long id = -1;
		
		try {
			con = DBConnection.getDBConnection();
			
			id = WebServiceCallLogger.openWebServiceCallReport(con, "user/login", email);
			
			cs = con.prepareCall("{call sp_Login(?,?,?,?)}");
			cs.setString("Email", email);
			cs.setString("Password", password);
			cs.registerOutParameter("Success", java.sql.Types.BIT);
			cs.registerOutParameter("ErrorID", java.sql.Types.SMALLINT);
			
			User u = null;
			if( cs.execute() ) {
				ResultSet rs = cs.getResultSet();
				rs.next();
				
				if( rs.getLong("PK_ManagerID") > 0 ) {
					u = new Manager(rs.getLong("PK_ManagerID"));
				} else if( rs.getLong("PK_ServerID") > 0 ) {
					u = new Server(rs.getLong("PK_ServerID"));
				} else {
					u = new User();
				}
				
				u.setUserID(rs.getLong("PK_UserID"));
				u.setFirstName(rs.getString("FirstName"));
				u.setLastName(rs.getString("LastName"));
				u.setEmail(rs.getString("Email"));
				
				response = JsonTransformer.toJson(u);
			} else {
				if( !cs.getBoolean("Success") ) {
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
	

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/guest/{deviceUUID}")
	public String login(
			@PathParam("deviceUUID") String deviceUUID
			) {
		String response = null;
		Connection con = null;
		CallableStatement cs = null;
		long id = -1;
		
		try {
			con = DBConnection.getDBConnection();
			
			id = WebServiceCallLogger.openWebServiceCallReport(con, "service/login/guest", deviceUUID);
			
			cs = con.prepareCall("{call sp_GuestLogin(?,?,?)}");
			cs.setString("DeviceUUID", deviceUUID);
			cs.registerOutParameter("Success", java.sql.Types.BIT);
			cs.registerOutParameter("ErrorID", java.sql.Types.SMALLINT);
			
			User u = null;
			if( cs.execute() ) {
				ResultSet rs = cs.getResultSet();
				rs.next();
				
				u = new User();				
				u.setUserID(rs.getLong("PK_UserID"));
				u.setFirstName(rs.getString("FirstName"));
				u.setLastName(rs.getString("LastName"));
				u.setEmail(rs.getString("Email"));
				
				response = JsonTransformer.toJson(u);
			} else {
				if( !cs.getBoolean("Success") ) {
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
