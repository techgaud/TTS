package com.ttsplugin.enums;

public enum Gender {
	FEMALE,
	MALE,
	UNKNOWN;

	public Gender reverse() {
		return this == FEMALE ? MALE : FEMALE;
	}

	@Deprecated
	public static Gender get(boolean female) {
		return get(female ? 1 : 0);
	}

	public static Gender get(int gender) {
		return gender == 1 ? FEMALE : MALE;
	}
}
