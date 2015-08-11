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
package com.twolinessoftware.android.orm.dto;

import java.util.List;

public interface GenericDAO<T> {

	/**
	 * Iteratively saves the object into the database. Notifies only on the base
	 * URI when it is finished
	 * 
	 * @see com.twolinessoftware.android.orm.provider.AbstractContentProvider#getBaseContentUri()
	 * 
	 * @param t
	 * @return
	 * @throws DAOException
	 */
	int save(T t) throws DAOException;

	/**
	 * Runs the save command on all objects in a list. Does not run applyBatch or bulkInsert so 
	 * performance problems might still occur on large datasets
	 * 
	 * @param t
	 * @throws DAOException
	 */
	int save(List<T> t) throws DAOException;
	
	/**
	 * 
	 * @TODO needs to handle different types of Cascades
	 * 
	 * 
	 * @param id
	 * @return
	 * @throws DAOException 
	 */
	boolean delete(long id) throws DAOException;

	T findById(long id);

	List<T> findByCriteria(String[] fields, String where,
						   String[] whereArgs, String sort);

	List<T> findAll();

}