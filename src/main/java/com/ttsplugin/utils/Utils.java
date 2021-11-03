package com.ttsplugin.utils;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class Utils {
	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String toLowerCaseWithFirstUppercase(String text) {
		String first = text.substring(0, 1).toUpperCase();
		return first + text.toLowerCase().substring(1);
	}
	
	public static void setClipVolume(float volume, Clip clip) {
	    FloatControl gainControl = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);        
	    gainControl.setValue(20f * (float) Math.log10(volume));
	}
}
