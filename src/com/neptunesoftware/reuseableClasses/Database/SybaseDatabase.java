package com.neptunesoftware.reuseableClasses.Database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.neptunesoftware.reuseableClasses.Database.config.Database;
import com.neptunesoftware.reuseableClasses.Database.config.ValueDatatypePair;

public class SybaseDatabase extends BaseDatabase {
	
	public SybaseDatabase() {
		super();
	}
	
	public SybaseDatabase(final String databaseAliasOrUsername) {
		super(databaseAliasOrUsername);		
	}
	
	public SybaseDatabase(final String driver, final String connectionURL, final String username, final String password, final String databaseType) {
		super(driver, connectionURL, username, password, databaseType);
	}

	
	
	public HashMap<Integer, HashMap<String, String>> executeProcedureReturningSelectQuery(String procedureName) {
		// execute procedures without parameters
		return executeProcedureReturningSelectQuery(procedureName, null);
	}
		
	public HashMap<Integer, HashMap<String, String>> executeProcedureReturningSelectQuery(String procedureName,
			LinkedHashMap<Integer, ValueDatatypePair> inParam) {
		// execute procedures without parameters and returns a result set
		String storedProcedure= "{call " + procedureName + DatabaseUtil.withParameters(inParam, null) + "}";

		Connection dbConnection = connect();
		CallableStatement callableStatement = null;
		ResultSet rs = null;
		
		try {
			callableStatement = dbConnection.prepareCall(storedProcedure);

			callableStatement = DatabaseUtil.setInParamCallablestatement(callableStatement, inParam);
						
			//boolean flag = callableStatement.execute();
			//int flag = callableStatement.executeUpdate();
			rs = callableStatement.executeQuery();
			
			//rs = callableStatement.getResultSet();
			
			return DatabaseUtil.convertResultSetToHashMap(rs);
			
		} catch (Exception e) {
			
			System.out.println("Exception: Error while trying to execute procedure " + procedureName.toUpperCase());
			
			//indicates failure
			HashMap<Integer, HashMap<String, String>> emptyRow = new HashMap<Integer, HashMap<String, String>>();
			
			return emptyRow;
		}finally {
			try {
				if (dbConnection != null) {
					dbConnection.close();
				}
				if (callableStatement != null) {
					callableStatement.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
			}
		}
	}

	
	@Override
	public String selectProcessingDate() {
		return null;
	}
	
	@Override
	public Database readConfig() {
		/* To read database config file containing database credentials*/
		return DatabaseUtil.readConfig("SybaseDatabaseInfo.xml");
	}
	
	
//	@Override // for Sybase driver 4
//	public int executeDML(String query, List<ValueDatatypePair> inputParameter) {
//		int result = executeSQL(query, inputParameter);
//		
//		String message = "";
//		if (result++ == 0)
//			message = "successful";
//		else
//			message = "failed";
//		
//		if(query.toLowerCase().trim().startsWith("insert"))
//			System.out.println("Insert " + message);
//		
//		else if(query.toLowerCase().trim().startsWith("update"))
//			System.out.println("Update " + message);
//		
//		else if(query.toLowerCase().trim().startsWith("delete"))
//			System.out.println("Delete " + message);
//		
//		else
//			System.out.println("your query is likely not correct!");
//		
//		return result;	
//	}
		

		
	
	public static void main(String [] args) {
		//System.out.println(new SybaseDatabase().readConfig().getDatabaseProps().get(0).getAlias());
	}
	
	
	
}
