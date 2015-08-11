/**
 * Copyright 2012 2Lines Software Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.twolinessoftware.android.orm.provider;

import java.util.HashMap;
import java.util.List;

public class DatabaseInfo {
		
		private String databaseName; 
		private int databaseVersion; 
		
		private TableInfo primaryTable;
		private HashMap<String, String> projectionMap;
	
		
		public String getDatabaseName() {
			return databaseName;
		}

		public void setDatabaseName(String databaseName) {
			this.databaseName = databaseName;
		}

		public int getDatabaseVersion() {
			return databaseVersion;
		}

		public void setDatabaseVersion(int databaseVersion) {
			this.databaseVersion = databaseVersion;
		}
		
		public TableInfo getPrimaryTable() {
			return primaryTable;
		}

		public void setPrimaryTable(TableInfo primaryTable) {
			this.primaryTable = primaryTable;
		}
		
		
		public HashMap<String,String> getFullProjectionMap(){
			projectionMap = new HashMap<String,String>(); 
			
			buildProjectionMap(getPrimaryTable(),true);
			
			return projectionMap; 
		}
		private void buildProjectionMap(TableInfo info, boolean isPrimary){
			projectionMap.putAll(info.getProjectionMap(isPrimary));
			for(TableInfo table: info.getJoinedTables())
				buildProjectionMap(table,false);
		}
		
		
		public String fromJoinedTablesString(){
			StringBuilder sb = new StringBuilder(); 
			TableInfo primaryTable = getPrimaryTable(); 
		
			sb.append(primaryTable.getName());
			sb.append(" as ");
			sb.append(primaryTable.getName());
			
			
			buildJoinString(sb,primaryTable);
			
			return sb.toString();
		}
		
		private StringBuilder buildJoinString(StringBuilder sb, TableInfo tableInfo){
			
			List<TableJoinInfo> tables = tableInfo.getAllJoinedTables();
			
			for(TableJoinInfo tableJoinInfo:tables){
				sb = appendTableJoin(sb,tableJoinInfo.getJoinField(), tableInfo, tableJoinInfo.getTableInfo());
				buildJoinString(sb,tableJoinInfo.getTableInfo());
			}
			return sb;
		}
		

		private StringBuilder appendTableJoin(StringBuilder sb, String joinField, TableInfo fromTableInfo, TableInfo toTableInfo){
		
			sb.append(" LEFT JOIN ");
			sb.append(toTableInfo.getName());
			sb.append(" as ");
			sb.append(toTableInfo.getName());
			sb.append(" ON (");
			sb.append(fromTableInfo.getName());
			sb.append(".");
			sb.append(joinField);
			sb.append("=");
			sb.append(toTableInfo.getName());
			sb.append(".");
			sb.append(toTableInfo.getPrimaryKey());
			sb.append(") ");
			
			return sb;
			
		}
	

		public static class TableMapping{
			String fromTable; 
			String toTable; 
			String joinField;
			public TableMapping(String fromTable, String toTable,
					String fromField) {
				super();
				this.fromTable = fromTable;
				this.toTable = toTable;
				this.joinField = fromField;
			}
	
		}
	
}
