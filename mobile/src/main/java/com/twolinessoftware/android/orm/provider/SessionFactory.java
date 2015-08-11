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

import com.twolinessoftware.android.orm.provider.annotation.Database;
import com.twolinessoftware.android.orm.provider.annotation.DatabaseField;
import com.twolinessoftware.android.orm.provider.annotation.Index;
import com.twolinessoftware.android.orm.provider.annotation.OneToOne;
import com.twolinessoftware.android.orm.provider.annotation.OneToOne.Cascade;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;

public class SessionFactory {

	private static final String LOGNAME = "SessionFactory";
	private static SessionFactory instance;

	private HashMap<Class, DatabaseInfo> mappingClasses = new HashMap<Class, DatabaseInfo>();

	public static SessionFactory getInstance() {
		if (instance == null)
			instance = new SessionFactory();

		return instance;
	}

	private SessionFactory() {
	}

	public DatabaseInfo getDatabaseInfo(Class clazz) {
	
		if (!mappingClasses.containsKey(clazz)){
			DatabaseInfo info = buildDatabaseDetails(clazz);

			mappingClasses.put(clazz, info);
		}
			

		return mappingClasses.get(clazz);
	}

	
	private TableInfo addTables( String name, Class clazz) {
 

		TableInfo tableInfo = new TableInfo();
		
		tableInfo.setClazz(clazz);
		
		tableInfo.setName(name);

		Field[] fieldList = clazz.getDeclaredFields();
					
		for (Field field : fieldList) {

			Index indexAnnot = field.getAnnotation(Index.class);

			DatabaseField fieldAnnot = field
					.getAnnotation(DatabaseField.class);

			if (fieldAnnot != null) {

				FieldInfo fi = new FieldInfo();

				fi.setName(fieldAnnot.name());

				fi.setType(field.getType());

				StringBuffer sb = new StringBuffer();
				
				sb.append(fieldAnnot.name());
				sb.append(" ");
				sb.append(getSqlNameForField(field));
				
				if (indexAnnot != null) {
					if (tableInfo.hasPrimary())
						throw new SessionInstantiationException(
								"Mapping classes cannot have multiple @INDEX calls");
				
					tableInfo.setPrimaryKey(fieldAnnot.name());
					sb.append(" primary key ");

					fi.setPrimary(true);

					fi.setAutoIncrement(indexAnnot.autoIncrement());
					
					if(indexAnnot.autoIncrement())
						sb.append(" autoincrement ");

				}

				fi.setSqlCreate(sb.toString());
				
				tableInfo.addField(field.getName(), fi );
 
 			}

			OneToOne oneToOneAnnot = field
					.getAnnotation(OneToOne.class);
	
			if (oneToOneAnnot != null) {
				String tName = oneToOneAnnot.table();
				String joinField = oneToOneAnnot.joinField();
				Cascade cascadeType = oneToOneAnnot.cascade();
				// Add the one to one mapping
				FieldInfo fi = new FieldInfo();
				
				fi.setAutoIncrement(false);
				fi.setName(joinField);
				fi.setType(Integer.TYPE);
				fi.setSqlCreate(joinField+" INTEGER");
				
				tableInfo.addField(joinField, fi );
				
				Class claz = field.getType();

				tableInfo.addJoinedTable(new TableJoinInfo(cascadeType ,  joinField,	addTables(tName, claz)));
			}
		}

		return tableInfo;
	}

	private DatabaseInfo buildDatabaseDetails(Class clazz)
			throws SessionInstantiationException {

		DatabaseInfo info = new DatabaseInfo();

		Database annot = (Database) clazz.getAnnotation(Database.class);

		if (annot == null)
			throw new SessionInstantiationException(
					"No @Database element included");

		info.setDatabaseName(annot.name() + ".db");
		info.setDatabaseVersion(annot.version());
		info.setPrimaryTable(addTables(annot.name(), clazz));
// @todo set is searchable annotation

		return info;
	}

	private String getSqlNameForField(Field field) {
		if (field.getType() == Long.TYPE || field.getType() == Integer.TYPE)
			return "INTEGER";
		else if (field.getType() == Float.TYPE
				|| field.getType() == Double.TYPE)
			return "REAL";
		else if(field.getType() == Boolean.TYPE || field.getType().isAssignableFrom(Date.class))
			return "INTEGER";
		else
			return "TEXT";

	}

	public class SessionInstantiationException extends RuntimeException {

		public SessionInstantiationException(String string) {
			super(string);
		}
	}

}
