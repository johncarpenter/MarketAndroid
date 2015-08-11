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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TableInfo {
		
		private static final String LOGNAME = "TableInfo";

		private Class clazz; 
	
		private String name; 
		private String primaryKey;
		
		private HashMap<String,FieldInfo> fields = new HashMap<String,FieldInfo>();
		
		private ArrayList<TableJoinInfo> joinedTables = new ArrayList<TableJoinInfo>(); 

        private HashMap<String,TableJoinInfo> duplicates = new HashMap<String, TableJoinInfo>();

		public void addJoinedTable(TableJoinInfo tableJoinInfo){
			if(!duplicates.containsKey(tableJoinInfo.getTableInfo().getName())){
                joinedTables.add(tableJoinInfo);
                duplicates.put(tableJoinInfo.getTableInfo().getName(),tableJoinInfo);
            }

		}
		
		public TableInfo getJoinedTable(String name){
			for(TableJoinInfo tableJoinInfo:joinedTables){
				if(tableJoinInfo.getTableInfo().getName().equalsIgnoreCase(name))
					return tableJoinInfo.getTableInfo();
				
			}
			return null;
		}
		
		public List<TableInfo> getJoinedTables(){
			ArrayList<TableInfo> temp = new ArrayList<TableInfo>(); 
			for(TableJoinInfo tableJoinInfo:joinedTables)
				temp.add(tableJoinInfo.getTableInfo());
			
			return temp; 
		}
		
		public List<TableJoinInfo> getAllJoinedTables(){
			return joinedTables;
		}
		
		public void addField(String simpleName, FieldInfo fieldInfo) {
			fields.put(simpleName, fieldInfo);
		}

		public String getName() {
			return name;
		}


		public void setName(String name) {
			this.name = name;
		}


		public String getPrimaryKey() {
			return primaryKey;
		}


		public void setPrimaryKey(String primaryKey) {
			this.primaryKey = primaryKey;
		}



		public HashMap<String, FieldInfo> getFields() {
			return fields;
		}


		public void setFields(HashMap<String, FieldInfo> fields) {
			this.fields = fields;
		}

		public boolean hasPrimary() {
			return primaryKey != null;
		}

		public Class getClazz() {
			return clazz;
		}

		public void setClazz(Class clazz) {
			this.clazz = clazz;
		}

		public HashMap<String,String> getProjectionMap(boolean isPrimaryTable){
			HashMap<String,String> projectionMap = new HashMap<String,String>(); 
			
			for(String key: fields.keySet()){
				FieldInfo info = fields.get(key);

                if(info.isPrimary() && isPrimaryTable)
                   projectionMap.put(info.getName(),info.getName());
                 else
				   projectionMap.put(name+""+info.getName(),name+"."+info.getName());
				//Log.d(LOGNAME, "Projection:"+info.getName()+","+name+""+info.getName());
			}
			return projectionMap; 
		}
		
		
		
}
