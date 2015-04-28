package com.bell.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;

import com.bell.ServiceRequest;
import com.bell.notification.Notification;
import com.bell.notification.NotificationBroadcaster;
import com.bell.util.BellException;
import com.bell.util.DBConnection;
import com.bell.util.JsonTransformer;
import com.bell.util.WebServiceCallLogger;

@Path("request")
public class RequestService {

	@POST
	@Path("/create")
	@Produces(MediaType.TEXT_PLAIN)
	public String createRequest(
			@FormParam("deviceUUID") String deviceUUID,
			@FormParam("beaconID") String beaconID
			) {
		String response = null;
		Connection con = null;
		CallableStatement cs = null;
		long id = -1;
		
		try {
			con = DBConnection.getDBConnection();
			
			id = WebServiceCallLogger.openWebServiceCallReport(con, "request/create", deviceUUID + "," + beaconID);
			
			long now = DateTime.now().getMillis();
			cs = con.prepareCall("{call sp_CreateServiceRequest(?,?,?,?,?)}");
			cs.setString("DeviceUUID", deviceUUID);
			cs.setString("BeaconID", beaconID);
			cs.setLong("RequestTime", now);
			cs.registerOutParameter("Success", java.sql.Types.BIT);
			cs.registerOutParameter("ErrorID", java.sql.Types.SMALLINT);
			
			if( cs.execute() ) {
				ResultSet rs = cs.getResultSet();
				rs.next();
				
				ServiceRequest sr = new ServiceRequest();
				sr.setRequestID(rs.getLong("PK_RequestID"));
				sr.setRequesterID(rs.getLong("FK_RequesterID"));
				sr.setStationID(rs.getLong("FK_StationID"));
				sr.setStationNumber(rs.getInt("StationNumber"));
				sr.setServerName(rs.getString("FirstName"));
				sr.setRequestTime(rs.getLong("RequestTime"));
				con.commit();
				rs.close();
				
				response = JsonTransformer.toJson(sr);
				
				// send the notification to interested parties
				if( cs.getMoreResults() ) {
					rs = cs.getResultSet();
					
					Notification msg = new Notification(sr);
					NotificationBroadcaster.send(rs,msg);
				}
				
			} else {
				if( cs.getBoolean("Success") ) {
					response = "false";
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
	
	@POST
	@Path("/complete")
	@Produces(MediaType.TEXT_PLAIN)
	public String completeRequest(
			@FormParam("serverID") String serverID,
			@FormParam("requestID") String requestID
			) {
		String response = null;
		Connection con = null;
		CallableStatement cs = null;
		long id = -1;
		
		try {
			con = DBConnection.getDBConnection();
			
			id = WebServiceCallLogger.openWebServiceCallReport(con, "request/complete", serverID + "," + requestID);
			
			long now = DateTime.now().getMillis();
			cs = con.prepareCall("{call sp_CompleteServiceRequest(?,?,?,?,?)}");
			cs.setString("ServerID", serverID);
			cs.setString("RequestID", requestID);
			cs.setLong("CompletedTime", now);
			cs.registerOutParameter("Success", java.sql.Types.BIT);
			cs.registerOutParameter("ErrorID", java.sql.Types.SMALLINT);
			
			if( cs.execute() ) {
				
				Notification message = new Notification(Notification.Types.SERVICE_COMPLETED, Long.toString(now));
				NotificationBroadcaster.send(cs.getResultSet(),message);
			
				con.commit();
				response = "true";
			} else {
				if( cs.getBoolean("Success") ) {
					response = "true";
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
	
	@GET
	@Path("/all")
	@Produces(MediaType.TEXT_PLAIN)
	public String getServiceRequestsForServer(
			@QueryParam("serverID") String serverID
			) {
		String response = null;
		Connection con = null;
		CallableStatement cs = null;
		long id = -1;
		
		try {
			con = DBConnection.getDBConnection();
			
			id = WebServiceCallLogger.openWebServiceCallReport(con, "request/get/all", serverID);
			
			cs = con.prepareCall("{call sp_GetServiceRequestsForServer(?,?,?)}");
			cs.setString("ServerID", serverID);
			cs.registerOutParameter("Success", java.sql.Types.BIT);
			cs.registerOutParameter("ErrorID", java.sql.Types.SMALLINT);
			
			if( cs.execute() ) {
				ResultSet rs = cs.getResultSet();
				
				ArrayList<ServiceRequest> requests = new ArrayList<ServiceRequest>();
				while( rs.next() ) {
					ServiceRequest r = new ServiceRequest();
					r.setRequestID(rs.getLong("PK_RequestID"));
					r.setRequesterID(rs.getLong("FK_RequesterID"));
					r.setStationID(rs.getLong("FK_StationID"));
					r.setStationNumber(rs.getInt("StationNumber"));
					r.setServerName(rs.getString("FirstName"));
					r.setRequestTime(rs.getLong("RequestTime"));
					requests.add(r);
				}
				con.commit();
				ServiceRequest[] sr = requests.toArray(new ServiceRequest[requests.size()]);
				response = JsonTransformer.toJson(sr);
			} else {
				if( cs.getBoolean("Success") ) {
					response = "[]";
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
