package com.bell.util;

public class Constants {
	
	public static final long VERSION = 2;

	// Database Connection
	public static final String DATABASE = "jdbc:sqlserver://jma-db.c5wfusvdk0sq.us-east-1.rds.amazonaws.com:1433;database=Bell";
	public static final String USERNAME = "Bell";
	public static final String PASSWORD = "QpdyEKjbxPBV2NQrXm2AzhNT";
	
	// Google
	public static final String GOOGLE_PROJECT_ID = "461976639558";
	public static final String GOOGLE_API_KEY = "AIzaSyC143EMxV1bChnIPILp3EYWPqSK7JeW6xA";
	
	// Mandrill
	public static final String MANDRILL_API_KEY = "vfQx6A5ZlGdIQ7PPgoqgVQ";
	
	public class DeviceTypes {
		public static final int ANDROID = 1;
		public static final int APPLE = 2;
		public static final int BLACKBERRY = 3;
		public static final int AMAZON = 4;
	}
}
