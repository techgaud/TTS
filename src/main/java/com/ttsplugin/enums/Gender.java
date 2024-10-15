package com.ttsplugin.enums;

public enum Gender {
	FEMALE,
	MALE,
	UNKNOWN;

	@Override
	public String toString() {
		if (this == FEMALE) return "Woman";
		if (this == MALE) return "Man";
		return "Non-binary";
	}

	public Gender reverse() {
		return this == FEMALE ? MALE : FEMALE;
	}

	public static Gender get(int gender) {
		if (gender == 0) return MALE;
		if (gender == 1) return FEMALE;
		return UNKNOWN;
	}
}
