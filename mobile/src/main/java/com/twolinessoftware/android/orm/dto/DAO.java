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

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.twolinessoftware.android.orm.provider.AbstractContentProvider;
import com.twolinessoftware.android.orm.provider.DatabaseInfo;
import com.twolinessoftware.android.orm.provider.SessionFactory;
import com.twolinessoftware.android.orm.provider.TableInfo;
import com.twolinessoftware.android.orm.provider.annotation.DatabaseField;
import com.twolinessoftware.android.orm.provider.annotation.Index;
import com.twolinessoftware.android.orm.provider.annotation.OneToOne;
import com.twolinessoftware.smarterlist.util.Ln;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public abstract class DAO<T> implements GenericDAO<T> {

	private static final String LOGNAME = "DAO";

	private AbstractContentProvider<T> provider;
	private Context context;

	private ArrayList<ContentProviderOperation> ops;

	public DAO(Context context, AbstractContentProvider<T> provider) {
		this.provider = provider;
		this.context = context;
	}


	public AbstractContentProvider<T> getProvider() {
		return provider;
	}

	protected Context getContext() {
		return context;
	}

    public ContentResolver getContentResolver(){ return  getContext().getContentResolver();}

	private int getIndexValue(Object t) {
		Field[] fieldList = t.getClass().getDeclaredFields();
		try {

			for (Field field : fieldList) {
				Index indexAnnot = field.getAnnotation(Index.class);
				if (indexAnnot != null)
					return (Integer) getValueFromObject(field, t);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Unable to access index value in "
							+ t.getClass().getSimpleName() + " Class");
		}

		throw new IllegalArgumentException(
				"No Mapping Element with @Index found in class "
						+ t.getClass().getSimpleName());

	}



    /**
     * Based on SQLBrite implementation https://github.com/square/sqlbrite
     */
    public interface Query {
        /** Execute the query on the underlying database and return the resulting cursor. */
        Cursor run();
    }

    public Observable<Query> createMonitoredQuery(@Nullable final String[] projection,
                                         @Nullable final String selection, @Nullable final String[] selectionArgs, @Nullable
    final String sortOrder, final boolean notifyForDescendents) {

        final Query query = new Query() {
            @Override public Cursor run() {
                return getContentResolver().query(getProvider().getBaseContentUri(), projection, selection, selectionArgs, sortOrder);
            }
        };


        return Observable.create(new Observable.OnSubscribe<Query>() {
            @Override
            public void call(final Subscriber<? super Query> subscriber) {
                final ContentObserver observer = new ContentObserver(new Handler(Looper.getMainLooper())) {
                    @Override
                    public void onChange(boolean selfChange)
                    {
                        Ln.v("Triggering update");
                        subscriber.onNext(query);
                    }
                };
                getContentResolver().registerContentObserver(getProvider().getBaseContentUri(), notifyForDescendents, observer);

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        Ln.v("Removing contentobserver");
                        getContentResolver().unregisterContentObserver(observer);
                    }
                }));
            }
        }).startWith(query);
    }

    public Observable<Query> createFtSearch(final String selection, @Nullable final String[] selectionArgs, @Nullable
    final String sortOrder) {

        final Query query = new Query() {
            @Override public Cursor run() {
                return getContentResolver().query(Uri.withAppendedPath(getProvider().getBaseContentUri(), "search"), null, selection, selectionArgs, sortOrder);
            }
        };

        return Observable.create(new Observable.OnSubscribe<Query>() {
            @Override
            public void call(final Subscriber<? super Query> subscriber) {
                subscriber.onNext(query);
            }
        }).startWith(query);
    }

    public Observable<Query> createQuery(@Nullable final String[] projection,
                                          @Nullable final String selection, @Nullable final String[] selectionArgs, @Nullable
    final String sortOrder) {

        final Query query = new Query() {
            @Override public Cursor run() {
                return getContentResolver().query(getProvider().getBaseContentUri(), projection, selection, selectionArgs, sortOrder);
            }
        };

        return Observable.create(new Observable.OnSubscribe<Query>() {
            @Override
            public void call(final Subscriber<? super Query> subscriber) {
                //subscriber.onNext(query);
                subscriber.onCompleted();
            }
        }).startWith(query);
    }


    public Observable<List<T>> mapQuerytoList(Query query) {
        return Observable.create(subscriber -> {

            Cursor cursor = query.run();
            ArrayList<T> items = new ArrayList<T>();

            try {
                if (cursor != null && cursor.moveToFirst()) {

                    do {
                        items.add(fromCursor(cursor));
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                subscriber.onError(e);
            } finally {
				assert cursor != null;
				cursor.close();
            }


            subscriber.onNext(items);
            subscriber.onCompleted();
        });

    }



    public Observable<List<T>> monitoredQuery(@Nullable final String[] projection,
                                                  @Nullable final String selection, @Nullable final String[] selectionArgs, @Nullable
    final String sortOrder, final boolean notifyForDescendents) {

        final Query query = new Query() {
            @Override public Cursor run() {
                return getContentResolver().query(getProvider().getBaseContentUri(), projection, selection, selectionArgs, sortOrder);
            }
        };

        return Observable.create(new Observable.OnSubscribe<List<T>>() {
            @Override
            public void call(final Subscriber<? super List<T>> subscriber) {
                final ContentObserver observer = new ContentObserver(new Handler(Looper.getMainLooper())) {
                    @Override
                    public void onChange(boolean selfChange) {

                        Cursor cursor = query.run();

                        ArrayList<T> items = new ArrayList<T>();

                        try {
                            if (cursor != null && cursor.moveToFirst()) {

                                do {
                                    items.add(fromCursor(cursor));
                                } while (cursor.moveToNext());
                            }
                        } catch (Exception e) {
                            subscriber.onError(e);
                        } finally {
							assert cursor != null;
							cursor.close();
                        }


                        subscriber.onNext(items);
                    }
                };

                getContentResolver().registerContentObserver(getProvider().getBaseContentUri(), notifyForDescendents, observer);

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        getContentResolver().unregisterContentObserver(observer);
                    }
                }));

                // trigger the initial request
                observer.onChange(true);
            }
        });
    }

    public void cleanTable(boolean notify){
        getContentResolver().delete(getProvider().getBaseContentUri(),null,null);

        if(notify) {
            notifyObservers();
        }
    }

    private void notifyObservers() {

        getContext().getContentResolver().notifyChange(getProvider().getBaseContentUri(), null);

    }

    public Observable<Integer> bulkReplaceAndInsert(List<T> itemList){

        return  Observable.create(subscriber -> {

            cleanTable(false);

            ContentValues[] insertValues = new ContentValues[itemList.size()];

            int i =0;
            for (T smartListItem:itemList){
                insertValues[i] = toContentValues(smartListItem);
                i++;
            }

            getContentResolver().bulkInsert(getProvider().getBaseContentUri(),insertValues);

            subscriber.onNext(itemList.size());
            subscriber.onCompleted();


        });
    }


	public int save(List<T> bulk) throws DAOException {
		for(T t:bulk)
			saveAll(t);
		getContext().getContentResolver().notifyChange(provider.getBaseContentUri(), null);
        return bulk.size();
	}
	
	
	public int save(T t) throws DAOException {
		int id = saveAll(t);
		getContext().getContentResolver().notifyChange(provider.getBaseContentUri(), null);
		return id; 
	}
	
	private int saveAll(T t) throws DAOException {

		DatabaseInfo info = SessionFactory.getInstance().getDatabaseInfo(
				t.getClass());
		
			
		TableInfo primaryTable = info.getPrimaryTable();
		int id = -1;
		try {
			return saveIterative(primaryTable, t);
		} catch (Exception e) {
            Log.e(LOGNAME,"Unable to save:"+Log.getStackTraceString(e));
			throw new DAOException("Unable to save object:Cause:"
					+ e.getMessage());
		}

		
		
	}

	private int saveIterative(TableInfo tableInfo, Object t)
			throws DAOException {

		HashMap<String, Integer> linkedFields = new HashMap<String, Integer>();

		Class elementClass = t.getClass();

		Field[] fieldList = elementClass.getDeclaredFields();

		for (Field field : fieldList) {
			// Check for one to one mapping
			OneToOne oneToOneAnnot = field
					.getAnnotation(OneToOne.class);

			if (oneToOneAnnot != null) {
				TableInfo info = tableInfo
						.getJoinedTable(oneToOneAnnot.table());

				Object object = getValueFromObject(field, t);

				// @TODO check for nullable
				if (object != null) {
					int result = saveIterative(info, object);
					linkedFields.put(oneToOneAnnot.joinField(), result);
				}
			}
		}

		return save(tableInfo, linkedFields, t);

	}

	private int save(TableInfo info, HashMap<String, Integer> linkedFields,
			Object t) {

		ContentValues values = null;
		try {
			values = toContentValues(t);
		} catch (Exception e) {
			Log.e(LOGNAME, "Unable to map to contentvalues for:"+info.getName()+":" + e.getMessage());
			return -1;
		}

		for (String field : linkedFields.keySet())
			values.put(field, linkedFields.get(field));

		long id = getIndexValue(t);

		String where = info.getName() + "."
				+ info.getPrimaryKey() + " = " + id;

		String[] whereArgs = null;

		Uri appendedProvider = Uri.parse(provider.getBaseContentUri()
				.toString() + "/" + info.getName());

		if (id == 0
				|| context.getContentResolver().update(appendedProvider,
						values, where, null) == 0) {

			Uri insertUri = context.getContentResolver().insert(
					appendedProvider, values);
			if (insertUri != null)
				id = (int) ContentUris.parseId(insertUri);
			
		}

		return (int) id;
	}

	public boolean delete(long id)  {

		boolean result = true; 
		
		DatabaseInfo info = SessionFactory.getInstance().getDatabaseInfo(
				getSuperClass());

		TableInfo primaryTable = info.getPrimaryTable(); 

		String where = primaryTable.getName() + "."
				+ primaryTable.getPrimaryKey() + " = " + id;

		String[] whereArgs = null;

		
		if (context.getContentResolver().delete(
				provider.getBaseContentUri(), where, null) == 0)
			result = false;
		
		getContext().getContentResolver().notifyChange(provider.getBaseContentUri(), null);
		return result; 
	}
	
	
	public T findById(long id) {

		DatabaseInfo info = SessionFactory.getInstance().getDatabaseInfo(
				getSuperClass());

		TableInfo primaryTable = info.getPrimaryTable();

		String where = primaryTable.getName() + "."
				+ primaryTable.getPrimaryKey() + " = " + id;

		String[] whereArgs = null;

		T element = null;

		Cursor cursor = null;
		try {

			cursor = context.getContentResolver().query(
					provider.getBaseContentUri(), null, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				element = fromCursor(cursor);
			}

		} catch (Exception e) {
			throw new SQLException("Unable to map class:"
					+ Log.getStackTraceString(e));
		} finally {
			if (cursor != null)
				cursor.close();
		}

		return element;

	}

	/* (non-Javadoc)
	 * @see com.twolinessoftware.android.jts.dto.GenericDAO#findByCriteria(java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	public List<T> findByCriteria(String[] fields, String where,
			String[] whereArgs, String sort) {

		List<T> elements = new ArrayList<T>();

		Cursor cursor = null;

		try {

            cursor = context.getContentResolver().query(
					provider.getBaseContentUri(), fields, where, whereArgs,
					sort);

			if (cursor != null && cursor.moveToFirst()) {

				do {
					elements.add(fromCursor(cursor));
				} while (cursor.moveToNext());
			}

		} catch (Exception e) {
			throw new SQLException("Unable to map class:" + e.getMessage());
		} finally {
			if (cursor != null)
				cursor.close();
		}

		return elements;
	}

    public T findFirstByCriteria(String[] fields, String where,
                                  String[] whereArgs, String sort) {

        List<T> elements = new ArrayList<T>();

        Cursor cursor = null;

        try {

            cursor = context.getContentResolver().query(
                    provider.getBaseContentUri(), fields, where, whereArgs,
                    sort);

            if (cursor != null && cursor.moveToFirst()) {

                do {
                    elements.add(fromCursor(cursor));
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            throw new SQLException("Unable to map class:" + e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }

        if(elements.size() > 0){
            return elements.get(0);
        }

        return null;
    }



	/* (non-Javadoc)
	 * @see com.twolinessoftware.android.jts.dto.GenericDAO#findAll()
	 */
	public List<T> findAll() {
		return findByCriteria(null, null, null, null);
	}

	protected ContentValues toContentValues(Object t) throws DAOException {

		ContentValues values = new ContentValues();

		Class elementClass = t.getClass();

		Field[] fieldList = elementClass.getDeclaredFields();

		for (Field field : fieldList) {
			DatabaseField fieldAnnot = field
					.getAnnotation(DatabaseField.class);

			Index indexAnnot = field.getAnnotation(Index.class);

			boolean exclude = (indexAnnot != null && indexAnnot.autoIncrement());

			if (fieldAnnot != null && !exclude) {
				String columnName = fieldAnnot.name();

				Object value = getValueFromObject(field, t);
				
				if(value != null){
					if (field.getType() == Long.TYPE)
						values.put(columnName, (Long) value);
					else if (field.getType() == Integer.TYPE)
						values.put(columnName, (Integer) value);
					else if (field.getType() == Float.TYPE)
						values.put(columnName, (Float) value);
					else if (field.getType() == Double.TYPE)
						values.put(columnName, (Double) value);
					else if (field.getType() == Boolean.TYPE)
						values.put(columnName, ((Boolean) value) ? 1 : 0);
                    else if (field.getType().isAssignableFrom(Date.class))
                        values.put(columnName,((Date)value).getTime());
					else
						values.put(columnName, value.toString());
				}

			}

		}

		return values;
	}

	public T fromCursor(Cursor cursor) throws IllegalAccessException,
			InstantiationException {

		DatabaseInfo info = SessionFactory.getInstance().getDatabaseInfo(
				getSuperClass());

		Class elementClass = getSuperClass();

		T element = getSuperClass().newInstance();

		//noinspection unchecked
		element = (T) populateObject(info.getPrimaryTable().getName(),
				elementClass, cursor,true);

		return element;
	}

	private Object populateObject(String tableName, Class clazz, Cursor cursor, boolean isPrimaryTable)
			throws InstantiationException, IllegalAccessException {

		Object element = clazz.newInstance();
		Field[] fieldList = clazz.getDeclaredFields();

		for (Field field : fieldList) {
			DatabaseField fieldAnnot = field
					.getAnnotation(DatabaseField.class);

			boolean accessible = field.isAccessible();

			if (!accessible)
				field.setAccessible(true);
			
			if (fieldAnnot != null) {

				String sqlFieldName = tableName + fieldAnnot.name();

                Index indexAnnot = field.getAnnotation(Index.class);
                if(indexAnnot != null && isPrimaryTable)
                    sqlFieldName = fieldAnnot.name();

                int columnIndex = cursor.getColumnIndex(sqlFieldName);

                if(columnIndex != -1){

                //for(String name:cursor.getColumnNames()) {

                     if (field.getType() == Long.TYPE)
                         field.setLong(element,
                                 cursor.getLong(cursor.getColumnIndex(sqlFieldName)));
                     else if (field.getType() == Integer.TYPE)
                         field.setInt(element,
                                 cursor.getInt(cursor.getColumnIndex(sqlFieldName)));
                     else if (field.getType() == Float.TYPE)
                         field.setFloat(element, cursor.getFloat(cursor
                                 .getColumnIndex(sqlFieldName)));
                     else if (field.getType() == Double.TYPE)
                         field.setDouble(element, cursor.getDouble(cursor
                                 .getColumnIndex(sqlFieldName)));
                     else if (field.getType().isAssignableFrom(Date.class)){
                        long time = cursor.getLong(cursor.getColumnIndex(sqlFieldName));
                         field.set(element,new Date(time));
                     }

                     else if (field.getType().isPrimitive() && field.getType().equals(boolean.class))
                         field.setBoolean(element, cursor.getInt(cursor
                                 .getColumnIndex(sqlFieldName)) > 0);
                     else if (field.getType().isAssignableFrom(String.class))
                         field.set(element, cursor.getString(cursor
                                 .getColumnIndex(sqlFieldName)));
           //      }

                }

             }

			OneToOne oneToOneAnnot = field
					.getAnnotation(OneToOne.class);
			if (oneToOneAnnot != null) {
				
				
				String tName = oneToOneAnnot.table();
				String joinField = oneToOneAnnot.joinField();
				
				String sqlFieldName = tableName+joinField; 
				
				int joinedId = cursor.getInt(cursor.getColumnIndex(sqlFieldName));
		
				if(joinedId != 0)
					field.set(element,
							populateObject(tName, field.getType(), cursor,false));
				
			}
			field.setAccessible(accessible);

		}
		return element;
	}

	private Class<T> getSuperClass() {
		ParameterizedType superclass = (ParameterizedType) getClass()
				.getGenericSuperclass();

		//noinspection unchecked
		return (Class<T>) superclass
				.getActualTypeArguments()[0];

	}

	private Object getValueFromObject(Field field, Object t)
			throws DAOException {
		Object object = null;

		boolean accessible = field.isAccessible();

		if (!accessible)
			field.setAccessible(true);

		try {
			object = field.get(t);
		} catch (IllegalArgumentException e) {
			throw new DAOException("Unable to access member:" + field.getName()+":"+ Log.getStackTraceString(e));
		} catch (IllegalAccessException e) {
			throw new DAOException("Unable to access member:" + field.getName()+":"+Log.getStackTraceString(e));
		}

		field.setAccessible(accessible);


		return object;
	}

}
