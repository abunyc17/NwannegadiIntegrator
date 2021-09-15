package com.neptunesoftware.reuseableClasses.Database;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;

import com.neptunesoftware.reuseableClasses.CommonMethods;
import com.neptunesoftware.reuseableClasses.CypherCrypt;
import com.neptunesoftware.reuseableClasses.ResponseConstants;
import com.neptunesoftware.reuseableClasses.Database.config.Database;
import com.neptunesoftware.reuseableClasses.Database.config.DatabaseProperty;
import com.neptunesoftware.reuseableClasses.Database.config.ValueDatatypePair;

public class DatabaseUtil {

		
	public static boolean tableExist(String tableName, Connection dbConnection) {
	    boolean tExists = false;
	    
	    tableName = tableName.toUpperCase();
	    		
	    try {
	    	ResultSet rs = dbConnection.getMetaData().getTables(null, null, tableName, null);
	        while (rs.next()) { 
	            String tName = rs.getString("TABLE_NAME");
	            if (tName != null && tName.equals(tableName)) {
	                tExists = true;
	                break;
	            }
	        }
	    } catch (SQLException e) {
	    	System.out.println("table check failed");
		}
	    return tExists;
	}
	
	public static boolean procedureExist(String procedureName, Connection dbConnection) {
	    boolean tExists = false;
	    
	    procedureName = procedureName.toUpperCase();
	    		
	    try {
	    	ResultSet rs = dbConnection.getMetaData().getProcedures(null, null, procedureName);
	        while (rs.next()) { 
	            String tName = rs.getString("PROCEDURE_NAME");
	            if (tName != null && tName.equals(procedureName)) {
	                tExists = true;
	                break;
	            }
	        }
	    } catch (SQLException e) {
	    	System.out.println("procedure check failed");
		}
	    return tExists;
	}

	public static boolean isDatabaseObjectCreated(String query, Connection dbConnection) {
		
		PreparedStatement pst = null;
		
		try {

			pst = dbConnection.prepareStatement(query);
			int result = pst.executeUpdate();

			if(!(result == 0)) {
				System.out.println("failed to create object");
				return false;
			}
			
			System.out.println("object created!");			
			return true;
		}

		catch (Exception e) {
			System.out.println("failed to create object(check query)");
			return false;
		} finally {
			try {
				if (dbConnection != null) {
					dbConnection.close();
				}
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException e) {}
		}
		
	}
	
	
	public static String withParameters(final LinkedHashMap<Integer, ValueDatatypePair> inParam, final LinkedHashMap<Integer, ValueDatatypePair> outParam) {
				
		int numOfParameters = calculateNumberOfParameters(inParam, outParam);
		String parameters = "(" + questionMarks(numOfParameters) + ")" ;
				
		return parameters.equals("()") ? "" : parameters;
	}
	
	public static int calculateNumberOfParameters(final LinkedHashMap<Integer, ValueDatatypePair> inParam, final LinkedHashMap<Integer, ValueDatatypePair> outParam) {
		
		int numOfDuplicateParameters = 0;
		
		if (inParam != null && outParam != null) {
			for(Map.Entry<Integer, ValueDatatypePair> entry : inParam.entrySet()) {
				if(outParam.containsKey(entry.getKey()))
					numOfDuplicateParameters++;
			}
		}
		
		int numOfInparameters = inParam == null ? 0 : inParam.size();
		int numOfOutparameters = outParam == null ? 0 : outParam.size();
		
		int estimatedNumOfParameters = numOfInparameters + numOfOutparameters;
		
		int actualNumOfParameters = estimatedNumOfParameters - numOfDuplicateParameters;
		
		return actualNumOfParameters;
	}
	
	public static String questionMarks(int numOfParam) {
		if (numOfParam == 1)
			return "?";
		else if(numOfParam > 1)
			return "?," + questionMarks(numOfParam - 1);
		else
			return "";
	}
	
	public static CallableStatement setInParamCallablestatement(final CallableStatement callableStatement, final LinkedHashMap<Integer, ValueDatatypePair> param) {
		try {
			
			int paramIndex = 0, datatype = 0;
			String value = "";
			
			for (Map.Entry<Integer, ValueDatatypePair> entry : param.entrySet()) {
				
				paramIndex = entry.getKey();
				value = entry.getValue().getValue();
				datatype = entry.getValue().getType();

				switch (datatype) {
				case Types.VARCHAR:
					callableStatement.setString(paramIndex, value);
					break;
				case Types.DECIMAL:
				case Types.NUMERIC:
					callableStatement.setBigDecimal(paramIndex, new BigDecimal(value));
					break;
				case Types.DOUBLE:
					callableStatement.setDouble(paramIndex, Double.valueOf(value));
					break;
				case Types.FLOAT:
					callableStatement.setFloat(paramIndex, Float.valueOf(value));
					break;
				case Types.INTEGER:
				case Types.BIGINT:
				case Types.SMALLINT:
					callableStatement.setInt(paramIndex, Integer.valueOf(value));
					break;
				case Types.DATE:
					callableStatement.setDate(paramIndex, getCurrentDate(value));
					break;
				case Types.NULL:
					callableStatement.setNull(paramIndex, Types.NULL);
					break;
				case Types.BLOB:
					callableStatement.setBytes(paramIndex, convertBase64StringToByte(value));
					break;
//				case Types.TIMESTAMP:
//					preparedStatement.setTimestamp(paramIndex, Timestamp.valueOf(value));
//					break;
				default:
					System.out.println("\nDatatype wrongly or not specified when setting in params for prepared statement");
				}
			}
		} catch (Exception e) {
			System.out.println("preparedStatement/param is probably null in setting IN parameters");
		}
		return callableStatement;
	}
	
	public static CallableStatement registerOutParameter(final CallableStatement callableStatement, final LinkedHashMap<Integer, ValueDatatypePair> param) {
		try {
			
			int paramIndex = 0, datatype = 0;
			//String value = "";
			
			for (Map.Entry<Integer, ValueDatatypePair> entry : param.entrySet()) {
				
				//value = entry.getValue().getValue();
				paramIndex = entry.getKey();
				datatype = entry.getValue().getType();

				switch (datatype) {
				case Types.VARCHAR:
					callableStatement.registerOutParameter(paramIndex, java.sql.Types.VARCHAR);
					break;
				case Types.DECIMAL:
				case Types.NUMERIC:
					callableStatement.registerOutParameter(paramIndex, java.sql.Types.NUMERIC);
					break;
				case Types.DOUBLE:
					callableStatement.registerOutParameter(paramIndex, java.sql.Types.DOUBLE);
					break;
				case Types.FLOAT:
					callableStatement.registerOutParameter(paramIndex, java.sql.Types.FLOAT);
					break;
				case Types.INTEGER:
				case Types.BIGINT:
				case Types.SMALLINT:
					callableStatement.registerOutParameter(paramIndex, java.sql.Types.INTEGER);
					break;
				case Types.DATE:
					callableStatement.registerOutParameter(paramIndex, java.sql.Types.DATE);
					break;
//				case Types.TIMESTAMP:
//					callableStatement.registerOutParameter(paramIndex, java.sql.Types.TIMESTAMP);
//					break;
				default:
					System.out.println("\nDatatype wrongly or not specified for out parameter");
				}
			}
		} catch (Exception e) {
			System.out.println("callablestatement/param is probably null in registering out parameters");
		}
		return callableStatement;
	}	
	
	public static LinkedHashMap<Integer, ValueDatatypePair> getOutParamCallablestatement(final CallableStatement callableStatement, final LinkedHashMap<Integer, ValueDatatypePair> param) {
		try {
			
			int paramIndex = 0, datatype = 0;
			//String value = "";
			
			for (Map.Entry<Integer, ValueDatatypePair> entry : param.entrySet()) {
				
				//value = entry.getValue().getValue();
				datatype = entry.getValue().getType();
				paramIndex = entry.getKey();

				switch (datatype) {
				case Types.VARCHAR:
					entry.getValue().setValue(callableStatement.getString(paramIndex) + "");
					break;
				case Types.DECIMAL:
				case Types.NUMERIC:
					entry.getValue().setValue(callableStatement.getBigDecimal(paramIndex) + "");
					break;
				case Types.DOUBLE:
					entry.getValue().setValue(callableStatement.getDouble(paramIndex) + "");
					break;
				case Types.FLOAT:
					entry.getValue().setValue(callableStatement.getFloat(paramIndex) + "");
					break;
				case Types.INTEGER:
				case Types.BIGINT:
				case Types.SMALLINT:
					entry.getValue().setValue(callableStatement.getInt(paramIndex) + "");
					break;
				case Types.DATE:
					entry.getValue().setValue(callableStatement.getDate(paramIndex) + "");
					break;
//				case Types.TIMESTAMP:
//					entry.getValue().setValue(callableStatement.getTimestamp(paramIndex) + "");
//					break;
				default:
					System.out.println("\nDatatype wrongly or not specified for getting out parameter");
				}
			}
		} catch (Exception e) {
			System.out.println("callablestatement is probably null in getting out parameters");
		}
		
		return param;
	}
		
	public static HashMap<Integer, HashMap<String, String>> convertResultSetToHashMap(ResultSet rs) {
		
		HashMap<Integer, HashMap<String, String>> row = new HashMap<Integer, HashMap<String, String>>();
				
		try {
			
			ResultSetMetaData rsmd = rs.getMetaData();
			
			// get the number of columns returned from select query
			int numberOfColumns = rsmd.getColumnCount();
						
			int rowIndex = 0;
			String key = "", value = "";
			
			// loop through each row of records
			while(rs.next()) {

				HashMap<String, String> column = new HashMap<String, String>();
				
				// loop through each column returned
				for(int columnIndex = 1; columnIndex <= numberOfColumns; columnIndex++) {
					
					key = rsmd.getColumnLabel(columnIndex);
					value = rs.getString(columnIndex) == null ? "" : rs.getString(columnIndex);
					
					// add columns accessed by column name
					column.put(key.toUpperCase(), value);
				}
				
				// add rows accessed by row index
				row.put(++rowIndex, column);
			}
			
		} catch (Exception e) {
			System.out.println("Exception when converting ResultSet to HashMap");
		}
		
		return row;
	}
		
	public static PreparedStatement setInParamPreparedStatement(final PreparedStatement preparedStatement, final List<ValueDatatypePair> param) {
		try {
			
			int paramIndex = 0;
			
			for (ValueDatatypePair entry : param) {
				
				String value = entry.getValue();
				int datatype = entry.getType();

				switch (datatype) {
				case Types.VARCHAR:
					preparedStatement.setString(++paramIndex, value);
					break;
				case Types.DECIMAL:
				case Types.NUMERIC:
					preparedStatement.setBigDecimal(++paramIndex, new BigDecimal(value));
					break;
				case Types.DOUBLE:
					preparedStatement.setDouble(++paramIndex, Double.valueOf(value));
					break;
				case Types.FLOAT:
					preparedStatement.setFloat(++paramIndex, Float.valueOf(value));
					break;
				case Types.INTEGER:
				case Types.BIGINT:
				case Types.SMALLINT:
					preparedStatement.setInt(++paramIndex, Integer.valueOf(value));
					break;
				case Types.DATE:
					preparedStatement.setDate(++paramIndex, getCurrentDate());
					break;
				case Types.NULL:
					preparedStatement.setNull(++paramIndex, Types.NULL);
					break;
				case Types.BLOB:
					preparedStatement.setBytes(++paramIndex, convertBase64StringToByte(value));
					break;
//				case Types.TIMESTAMP:
//					preparedStatement.setTimestamp(++paramIndex, Timestamp.valueOf(value));
//					break;
				default:
					System.out.println("\nDatatype wrongly or not specified when setting in params for prepared statement");
				}
			}
		} catch (Exception e) {
			System.out.println("preparedStatement is probably null");
		}
		return preparedStatement;
	}


	
	
	public static java.sql.Date getCurrentDate() {
	    java.util.Date today = new java.util.Date();
	    //System.out.println("dateString1: " + new java.sql.Date(today.getTime()));
	    return new java.sql.Date(today.getTime());
	}
	
	public static java.sql.Date getCurrentDate(final String inputDate, final String dateFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Date javaDate = getCurrentDate();
		
		try {
			if (inputDate.trim().length() > 0)
				javaDate = sdf.parse(inputDate);
			
		} catch (ParseException e) {}		
	    return new java.sql.Date(javaDate.getTime());
	}
	
	public static java.sql.Date getCurrentDate(final String inputDate) {
	    return getCurrentDate(inputDate, "dd/MM/yyyy");
	}
	
	public static java.sql.Date getCurrentTimestamp(final String inputDate, final String dateFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Date javaDate = getCurrentDate();
		
		try {
			javaDate = sdf.parse(inputDate);
		} catch (ParseException e) {}
		
	    return new java.sql.Date(javaDate.getTime());
	}
	
	public static java.sql.Date getCurrentTimestamp(final String inputDate) {
	    return getCurrentTimestamp(inputDate, "dd/MM/yyyy HH:mm:ss a");
	} 
	
	
	public static byte[] convertBase64StringToByte(String fileString){
		byte[] data = Base64.getDecoder().decode(fileString);
		return data;
	}
	
	
	public static String getDatabaseDriver(final String databaseType) {
		String driver = "";
		switch(databaseType.toUpperCase()) {
		case ResponseConstants.ORACLE_DATABASE : 
			driver = ResponseConstants.ORACLE_DRIVER;
			break;
		case ResponseConstants.SYBASE_DATABASE : 
			driver = ResponseConstants.SYBASE_DRIVER;
			break;
		}
		
		return driver;
	}

	public static String getDatabaseConnectionUrl(final String databaseType, final String ipAddress, final String portNo, final String serviceName) {
		String connectionUrl = "";
		switch(databaseType.toUpperCase()) {
		case ResponseConstants.ORACLE_DATABASE : 
			connectionUrl = ResponseConstants.ORACLE_CONNECTION_URL_PREFIX + ipAddress + ":" + portNo + "/" + serviceName;
			break;
		case ResponseConstants.SYBASE_DATABASE : 
			connectionUrl = ResponseConstants.SYBASE_CONNECTION_URL_PREFIX + ipAddress + ":" + portNo + "/" + serviceName;
			break;
		}
		//10.152.2.32:5000/banking
		return connectionUrl;
	}
	
	
	public static Connection connect(final String driver, final String connectionURL, final String username,
			final String password, final String databaseType, final String ipAddress, final String portNumber,
			final String serviceName) {
		
		Connection connection = null;

		try {
			Class.forName(driver);

			Properties props = new Properties();
			props.setProperty("user", username);
			props.setProperty("password", password);
			props.setProperty("charset", "iso_1");
			
			connection = DriverManager.getConnection(connectionURL, props);
			//connection = DriverManager.getConnection(connectionURL, username, password);
			
			System.out.println("connection to " + databaseType.toUpperCase() + " database established");

		} catch (ClassNotFoundException e) {
			System.out.println("** Suggested fix **");
			System.out.println("1. Ensure the appropriate jar needed for your database connection has been added to the project");
			System.out.println("2. Ensure the TYPE specified in ...DatabaseInfo.xml file for your database, " + databaseType.toUpperCase() + ", is either \"Oracle\" or \"Sybase\" ");
			System.out.println("\nconnection to " + databaseType.toUpperCase() + " database failed");
			
		} catch (SQLException e) {
			System.out.println("Please Verify your connection parameters and that your " + databaseType.toUpperCase() + " database is started");
			System.out.println("driver: " + driver + "\nusername: " + username + "\npassword: " + password);
			System.out.println("ipAddress: " + ipAddress + "\nportNumber: " + portNumber+ "\nserviceName: " + serviceName);
			System.out.println("\nconnection to " + databaseType.toUpperCase() + " database failed");
		
		} catch (Exception e) {
			System.out.println("This error pass me!");
			System.out.println("*** Stack Trace *** \n" + e);
			System.out.println("\nconnection to " + databaseType.toUpperCase() + " database failed");
		}
		
		return connection;
	}
	
	public static Connection connect(final String driver, final String connectionURL, final String username, 
			final String password) {
		
		Connection dbConnection = null;
		try {
			dbConnection = getDataSource(driver, connectionURL, username, password).getConnection();
		} catch (SQLException e) {
			System.out.println("Please Verify your connection parameters and that your database is started");
			System.out.println("username: " + username + "\npassword: " + password);
			System.out.println("connectionURL: " + connectionURL);
			System.out.println("\nconnection to database failed");
		}

		return dbConnection;
	}
	
	private static BasicDataSource dataSource;
	
    private static BasicDataSource getDataSource(final String driver, final String connectionURL, final String username,
			final String password) {
    	
        if (dataSource == null)
        {
            BasicDataSource ds = new BasicDataSource();
            ds.setUrl(connectionURL);// + ";sendStringParametersAsUnicode=false");
            ds.setUsername(username);
            ds.setPassword(password); 
            ds.setDriverClassName(driver);
            ds.setValidationQuery("select 1");
 
            ds.setInitialSize(30);
            ds.setMinIdle(100);
            ds.setMaxIdle(200);
            ds.setMaxTotal(500);
            ds.setPoolPreparedStatements(true);
            ds.setMaxOpenPreparedStatements(100);
 
            dataSource = ds;
        }
        System.out.println("*** No. Connections given out:" + dataSource.getNumActive());
        System.out.println("*** No. of Connections left in the pool: " + dataSource.getNumIdle());
        return dataSource;
    }
	
	
	public static Database readConfig(String configXMLFileName) {
		Database database = new Database();

		try {
			String content = CommonMethods.getInfo(configXMLFileName, Database.class);
			//content = content.replaceAll("\\<\\!DOCTYPE(.+?)\\>", "").trim();
			
			database = CommonMethods.xmlStringToObject(content, Database.class);
			database = DatabaseUtil.decryptContent(database);


			database.setResponseCode(ResponseConstants.SUCCEESS_CODE);
		} catch (Exception e) {
			System.out.println("Cannot read " + configXMLFileName);
			database.setResponseCode(ResponseConstants.FILE_ERROR_CODE);
		}

		return database;
	}
	
	public static Database decryptContent(final Database database) {
		
		Database databaseDup  = database;
		try {
			if (!database.getDatabaseProps().isEmpty())
				for (DatabaseProperty dbProperty : database.getDatabaseProps()) {
					String defaultDB = CypherCrypt.deCypher(dbProperty.getDefaultDB().trim()) == null || CypherCrypt.deCypher(dbProperty.getDefaultDB().trim()).equals("")
							? dbProperty.getDefaultDB() : CypherCrypt.deCypher(dbProperty.getDefaultDB());
					
					String alias = CypherCrypt.deCypher(dbProperty.getAlias().trim()) == null || CypherCrypt.deCypher(dbProperty.getAlias().trim()).equals("")
							? dbProperty.getAlias() : CypherCrypt.deCypher(dbProperty.getAlias());
							
					String type = CypherCrypt.deCypher(dbProperty.getType().trim()) == null || CypherCrypt.deCypher(dbProperty.getType().trim()).equals("")
							? dbProperty.getType() : CypherCrypt.deCypher(dbProperty.getType());
							
					String username = CypherCrypt.deCypher(dbProperty.getUsername().trim()) == null || CypherCrypt.deCypher(dbProperty.getUsername().trim()).equals("")
							? dbProperty.getUsername() : CypherCrypt.deCypher(dbProperty.getUsername());
							
					String password = CypherCrypt.deCypher(dbProperty.getPassword().trim()) == null || CypherCrypt.deCypher(dbProperty.getPassword().trim()).equals("")
							? dbProperty.getPassword() : CypherCrypt.deCypher(dbProperty.getPassword());
							
					String ipAddress = CypherCrypt.deCypher(dbProperty.getIpAddress().trim()) == null || CypherCrypt.deCypher(dbProperty.getIpAddress().trim()).equals("")
							? dbProperty.getIpAddress() : CypherCrypt.deCypher(dbProperty.getIpAddress());
							
					String portNumber = CypherCrypt.deCypher(dbProperty.getPortNumber().trim()) == null || CypherCrypt.deCypher(dbProperty.getPortNumber().trim()).equals("")
							? dbProperty.getPortNumber() : CypherCrypt.deCypher(dbProperty.getPortNumber());
							
					String serviceName = CypherCrypt.deCypher(dbProperty.getServiceName().trim()) == null || CypherCrypt.deCypher(dbProperty.getServiceName().trim()).equals("")
							? dbProperty.getServiceName() : CypherCrypt.deCypher(dbProperty.getServiceName());
					
					dbProperty.setDefaultDB(defaultDB);
					dbProperty.setAlias(alias);
					dbProperty.setType(type);
					dbProperty.setUsername(username);
					dbProperty.setPassword(password);
					dbProperty.setIpAddress(ipAddress);
					dbProperty.setPortNumber(portNumber);
					dbProperty.setServiceName(serviceName);
				}

			return database;
		} catch (Exception e) {
			System.out.println("DatabaseCredential: \n" + CommonMethods.objectToXml(database));
			System.out.println("Cannot decrypt content");
			
			return databaseDup;
		}
		
	}
	
	
	
	
}
