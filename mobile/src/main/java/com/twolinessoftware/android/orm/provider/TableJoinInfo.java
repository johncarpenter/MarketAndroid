package com.twolinessoftware.android.orm.provider;

import com.twolinessoftware.android.orm.provider.annotation.OneToOne.Cascade;

public class TableJoinInfo {

	private Cascade cascadeType; 
	
	private String joinField; 
	
	private TableInfo tableInfo;

	public TableJoinInfo(Cascade cascadeType, String joinField,
			TableInfo tableInfo) {
		super();
		this.cascadeType = cascadeType;
		this.joinField = joinField;
		this.tableInfo = tableInfo;
	}

	public Cascade getCascadeType() {
		return cascadeType;
	}

	public void setCascadeType(Cascade cascadeType) {
		this.cascadeType = cascadeType;
	}

	public String getJoinField() {
		return joinField;
	}

	public void setJoinField(String joinField) {
		this.joinField = joinField;
	}

	public TableInfo getTableInfo() {
		return tableInfo;
	}

	public void setTableInfo(TableInfo tableInfo) {
		this.tableInfo = tableInfo;
	} 
	
	
	
}
