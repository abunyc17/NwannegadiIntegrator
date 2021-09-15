package com.neptunesoftware.reuseableClasses.Database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.neptunesoftware.reuseableClasses.ResponseConstants;
import com.neptunesoftware.reuseableClasses.Database.config.Database;
import com.neptunesoftware.reuseableClasses.Database.config.DatabaseCredentials;
import com.neptunesoftware.reuseableClasses.Database.config.DatabaseProperty;
import com.neptunesoftware.reuseableClasses.Database.config.ValueDatatypePair;

abstract class BaseDatabase {

	private static String driver = "";
	private static String username = "";
	private static String password = "";
	private static String connectionURL = "";
	@SuppressWarnings("unused")
	private static String databaseType = "";
	@SuppressWarnings("unused")
	private static String ipAddress = "";
	@SuppressWarnings("unused")
	private static String portNumber = "";
	@SuppressWarnings("unused")
	private static String serviceName = "";
	
	public BaseDatabase() {
		DatabaseCredentials dbCredentials = getDatabaseCredentials();
		
		BaseDatabase.driver = dbCredentials.getDriver();
		BaseDatabase.username = dbCredentials.getUsername();
		BaseDatabase.password = dbCredentials.getPassword();
		BaseDatabase.ipAddress = dbCredentials.getIpAddress();
		BaseDatabase.portNumber = dbCredentials.getPortNumber();
		BaseDatabase.serviceName = dbCredentials.getServiceName();
		BaseDatabase.connectionURL = dbCredentials.getConnectionURL();
		BaseDatabase.databaseType = dbCredentials.getDatabaseType();
	}
	
	public BaseDatabase(final String databaseAliasOrUsername) {
		System.out.println(databaseAliasOrUsername);
		
		DatabaseCredentials dbCredentials = getDatabaseCredentials(databaseAliasOrUsername);
		
		BaseDatabase.driver = dbCredentials.getDriver();
		BaseDatabase.username = dbCredentials.getUsername();
		BaseDatabase.password = dbCredentials.getPassword();
		BaseDatabase.ipAddress = dbCredentials.getIpAddress();
		BaseDatabase.portNumber = dbCredentials.getPortNumber();
		BaseDatabase.serviceName = dbCredentials.getServiceName();
		BaseDatabase.connectionURL = dbCredentials.getConnectionURL();
		BaseDatabase.databaseType = dbCredentials.getDatabaseType();		
	}
	
	public BaseDatabase(final String driver, final String connectionURL, final String username, final String password, final String databaseType) {
		BaseDatabase.driver = driver;
		BaseDatabase.username = username;
		BaseDatabase.password = password;
		BaseDatabase.connectionURL = connectionURL;
		BaseDatabase.databaseType = databaseType;
	}

	
	public static Connection connect() {
		//return DatabaseUtil.connect(driver, connectionURL, username, password, databaseType, ipAddress, portNumber, serviceName);
		return DatabaseUtil.connect(driver, connectionURL, username, password);
	}
	
	public static boolean tableExist(String tableName) {
		return DatabaseUtil.tableExist(tableName, connect());
	}
	
	public static boolean procedureExist(String procedureName) {
	    return DatabaseUtil.procedureExist(procedureName, connect());
	}

	public static boolean isDatabaseObjectCreated(String query) {
		return DatabaseUtil.isDatabaseObjectCreated(query, connect());		
	}
	

	public String selectProcessingDate() {
		return null;
	}

	
	/* To execute Oracle Procedures  */
	
	public LinkedHashMap<Integer, ValueDatatypePair> executeProcedure(String procedureName) {
		// execute procedures without parameters
		return executeProcedure(procedureName, null, null);
	}

	public LinkedHashMap<Integer, ValueDatatypePair> executeProcedure(String procedureName,
			LinkedHashMap<Integer, ValueDatatypePair> inParam) {
		// execute procedures with only IN parameters
		return executeProcedure(procedureName, inParam, null);
	}

	public LinkedHashMap<Integer, ValueDatatypePair> executeProcedure(String procedureName,
			LinkedHashMap<Integer, ValueDatatypePair> inParam, LinkedHashMap<Integer, ValueDatatypePair> outParam) {
		
		String storedProcedure= "{call " + procedureName + DatabaseUtil.withParameters(inParam, outParam) + "}";

		Connection dbConnection = connect();

		CallableStatement callableStatement = null;

		try {
			callableStatement = dbConnection.prepareCall(storedProcedure);
			
			callableStatement = DatabaseUtil.setInParamCallablestatement(callableStatement, inParam);

			callableStatement = DatabaseUtil.registerOutParameter(callableStatement, outParam);
			
			callableStatement.executeUpdate();
			
			//System.out.println("\nExecuted procedure " +procedureName.toUpperCase()+" successfully\n");
						
			LinkedHashMap<Integer, ValueDatatypePair> getOutParam = DatabaseUtil.getOutParamCallablestatement(callableStatement, outParam);
			
			LinkedHashMap<Integer, ValueDatatypePair> resultParam = new LinkedHashMap<Integer, ValueDatatypePair>();
			
			if(getOutParam != null)
				resultParam = getOutParam;
			
			//indicates success
			resultParam.put(0, new ValueDatatypePair(ResponseConstants.SUCCEESS_CODE));
			
			return resultParam;
			
		} catch (Exception e) {
			
			System.out.println("Exception: Error while trying to execute procedure " + procedureName.toUpperCase());
			
			//indicates failure
			outParam.put(0, new ValueDatatypePair(ResponseConstants.PROCEDURE_CODE));
			return outParam;
		}finally {
			try {
				if (dbConnection != null) {
					dbConnection.close();
				}
				if (callableStatement != null) {
					callableStatement.close();
				}
			} catch (SQLException e) {
			}
		}
	}

	
	/* To execute SELECT statement  */
	
	public HashMap<Integer, HashMap<String, String>> executeSelect(String query) {
		return executeSelect(query, null);
	}

	public HashMap<Integer, HashMap<String, String>> executeSelect(String query,
			List<ValueDatatypePair> inputParameter) {
		
		Connection dbConnection = connect();
			
		PreparedStatement pst = null;
		ResultSet rs = null;
		
		try {
			
			pst = dbConnection.prepareStatement(query);
			
			pst = DatabaseUtil.setInParamPreparedStatement(pst, inputParameter);
			
			rs = pst.executeQuery();
									
			System.out.println("Select successful");
			
			return DatabaseUtil.convertResultSetToHashMap(rs);
		} catch (Exception e) {
			System.out.println("Select failed");
			return null;
		} finally {
			try {
				if (dbConnection != null) {
					dbConnection.close();
				}
				if (pst != null) {
					pst.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
			}
		}
	}

	
	/* To execute INSERT, DELETE, UPDATE statements  */
	
	public int executeDML(String query) {
		
		List<ValueDatatypePair> inputParameter = new ArrayList<ValueDatatypePair>();
		
		return executeDML(query, inputParameter);	
	}

	public int executeDML(String query, List<ValueDatatypePair> inputParameter) {
		int result = executeSQL(query, inputParameter);
		
		String message = "";
		if (result > 0)
			message = "successful";
		else
			message = "failed";
		
		if(query.toLowerCase().trim().startsWith("insert"))
			System.out.println("Insert " + message);
		
		else if(query.toLowerCase().trim().startsWith("update"))
			System.out.println("Update " + message);
		
		else if(query.toLowerCase().trim().startsWith("delete"))
			System.out.println("Delete " + message);
		
		else
			System.out.println("your query is likely not correct!");
				
		return result;	
	}

	public int executeSQL(String query, List<ValueDatatypePair> inputParameter) {
		Connection dbConnection = null;
		dbConnection = connect();

		PreparedStatement pst = null;

		int result = -1;
		
		try {
			pst = dbConnection.prepareStatement(query);
			
			pst = DatabaseUtil.setInParamPreparedStatement(pst, inputParameter);
			
			result = pst.executeUpdate();
			
		} catch (Exception e) {
			System.out.println("exception: " + e.getMessage());
		} finally {
			try {
				if (dbConnection != null) {
					dbConnection.close();
				}
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {
			}
		}
		
		return result;
	}

	
	/* To read database config file containing database credentials*/
	
	public Database readConfig() {
		return null;
	}
	
	public DatabaseCredentials getDatabaseCredentials() {
		
		String driver = "";
		String username = "";
		String password = "";
		String ipAddress = "";
		String portNumber = "";
		String serviceName = "";
		String connectionURL = "";   
		String databaseType = "";
		
		// used to know whether OracleDatabaseInfo.xml has a default DB set
		boolean hasDefaultDB = false;
		
		Database database = readConfig();
		
		if (database.getResponseCode().equals(ResponseConstants.SUCCEESS_CODE)) {
			for (DatabaseProperty dbProperty : database.getDatabaseProps()) {
				if (dbProperty.getDefaultDB().trim().equalsIgnoreCase("true")
					|| dbProperty.getDefaultDB().trim().equalsIgnoreCase("yes")) {

					hasDefaultDB = true; // yes there is a default DB set
					
					driver = DatabaseUtil.getDatabaseDriver(dbProperty.getType().toUpperCase());
					username = dbProperty.getUsername().trim();
					password = dbProperty.getPassword().trim();
					ipAddress = dbProperty.getIpAddress().trim();
					portNumber = dbProperty.getPortNumber().trim();
					serviceName = dbProperty.getServiceName().trim();
					connectionURL = DatabaseUtil.getDatabaseConnectionUrl(dbProperty.getType().toUpperCase(), ipAddress, portNumber,
							serviceName);
					databaseType = "default [" + dbProperty.getAlias().trim().toUpperCase() + "]";
				
					break;
				}
			}
			
			// in the absence of a default DB, the first DB config is used
			if(!hasDefaultDB) {
				for (DatabaseProperty dbProperty : database.getDatabaseProps()) {					
					driver = DatabaseUtil.getDatabaseDriver(dbProperty.getType().toUpperCase());
					username = dbProperty.getUsername().trim();
					password = dbProperty.getPassword().trim();
					ipAddress = dbProperty.getIpAddress().trim();
					portNumber = dbProperty.getPortNumber().trim();
					serviceName = dbProperty.getServiceName().trim();
					connectionURL = DatabaseUtil.getDatabaseConnectionUrl(dbProperty.getType().toUpperCase(), ipAddress, portNumber,
							serviceName);
					databaseType = dbProperty.getType().toUpperCase();
					
					System.out.println("\nNo default database was set. First database config is used instead \n");
					break;
				}
			}
		}
		
		DatabaseCredentials dbCredentials = new DatabaseCredentials();
		dbCredentials.setDriver(driver);
		dbCredentials.setUsername(username);
		dbCredentials.setPassword(password);
		dbCredentials.setIpAddress(ipAddress);
		dbCredentials.setPortNumber(portNumber);
		dbCredentials.setServiceName(serviceName);
		dbCredentials.setConnectionURL(connectionURL);
		dbCredentials.setDatabaseType(databaseType);
		
		return dbCredentials;
	}
	
	public DatabaseCredentials getDatabaseCredentials(String databaseAliasOrUsername) {
		String driver = "";
		String username = "";
		String password = "";
		String ipAddress = "";
		String portNumber = "";
		String serviceName = "";
		String connectionURL = "";
		
		Database database = readConfig();
			
		if (database.getResponseCode().equals(ResponseConstants.SUCCEESS_CODE)) {
			for (DatabaseProperty dbProperty : database.getDatabaseProps()) {
				if (dbProperty.getAlias().trim().toUpperCase().equals(databaseAliasOrUsername.toUpperCase())
					|| dbProperty.getUsername().trim().toUpperCase().equals(databaseAliasOrUsername.toUpperCase())) {

					driver = DatabaseUtil.getDatabaseDriver(dbProperty.getType().toUpperCase());
					username = dbProperty.getUsername().trim();
					password = dbProperty.getPassword().trim();
					ipAddress = dbProperty.getIpAddress().trim();
					portNumber = dbProperty.getPortNumber().trim();
					serviceName = dbProperty.getServiceName().trim();
					connectionURL = DatabaseUtil.getDatabaseConnectionUrl(dbProperty.getType().toUpperCase(), ipAddress, portNumber,
							serviceName);
				}
			}
		}
		
		DatabaseCredentials dbCredentials = new DatabaseCredentials();
		dbCredentials.setDriver(driver);
		dbCredentials.setUsername(username);
		dbCredentials.setPassword(password);
		dbCredentials.setIpAddress(ipAddress);
		dbCredentials.setPortNumber(portNumber);
		dbCredentials.setServiceName(serviceName);
		dbCredentials.setConnectionURL(connectionURL);
		dbCredentials.setDatabaseType(databaseAliasOrUsername);
		
		return dbCredentials;
		
	}
	
	
}
