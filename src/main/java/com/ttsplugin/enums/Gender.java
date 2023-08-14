package com.ttsplugin.enums;

public enum Gender {
	FEMALE,
	MALE,
	UNKNOWN;

	public Gender reverse() {
		return this == FEMALE ? MALE : FEMALE;
	}
	
	public static Gender get(boolean female) {
		return female ? FEMALE : MALE;
	}
}
