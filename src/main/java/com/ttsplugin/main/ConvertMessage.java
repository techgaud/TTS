package com.ttsplugin.main;

import java.util.ArrayList;
import java.util.List;

public class ConvertMessage {
	/**
	 * Changes the message, so it will be spelled better by the tts
	 * By changing shortens and stuff.
	 */
	public static String convert(String message) {
		String output = message;
		for (Convert convert : Convert.list) {
			output = convert.convert(output);
		}
		
		return output;
	}
	
	public static class Convert {
		public ConvertMode mode;
		public String target, replacement;
		public boolean caseSensitive;
		public static List<Convert> list = new ArrayList<>();
		
		static {
			new Convert(ConvertMode.REPLACE_WORD, "idk", "i don't know");
			new Convert(ConvertMode.REPLACE_WORD, "imo", "in my opinion");
			new Convert(ConvertMode.REPLACE_WORD, "afaik", "as far as i know");
			new Convert(ConvertMode.REPLACE_WORD, "rly", "really");
			new Convert(ConvertMode.REPLACE_WORD, "tbow", "twisted bow");
			new Convert(ConvertMode.REPLACE_WORD, "tbows", "twisted bows");
			new Convert(ConvertMode.REPLACE_WORD, "p2p", "pay to play");
			new Convert(ConvertMode.REPLACE_WORD, "f2p", "free to play");
			new Convert(ConvertMode.REPLACE_WORD, "p2p?", "pay to play?");
			new Convert(ConvertMode.REPLACE_WORD, "f2p?", "free to play?");
			new Convert(ConvertMode.REPLACE_WORD, "ty", "thank you");
			new Convert(ConvertMode.REPLACE_WORD, "tysm", "thank you so much");
			new Convert(ConvertMode.REPLACE_WORD, "tyvm", "thank you very much");
			new Convert(ConvertMode.REPLACE_WORD, "im", "i'm");
			new Convert(ConvertMode.REPLACE_WORD, "np", "no problem");
			new Convert(ConvertMode.REPLACE_WORD, "acc", "account");
			new Convert(ConvertMode.REPLACE_WORD, "irl", "in real life");
			new Convert(ConvertMode.REPLACE_WORD, "wtf", "what the fuck");
			new Convert(ConvertMode.REPLACE_WORD, "jk", "just kidding");
			new Convert(ConvertMode.REPLACE_WORD, "gl", "good luck");
			new Convert(ConvertMode.REPLACE_WORD, "pls", "please");
			new Convert(ConvertMode.REPLACE_WORD, "plz", "please");
			new Convert(ConvertMode.REPLACE_WORD, "osrs", "oldschool runescape");
			new Convert(ConvertMode.REPLACE_WORD, "rs3", "runescape 3");
			new Convert(ConvertMode.REPLACE_WORD, "lvl", "level");
			new Convert(ConvertMode.REPLACE_WORD, "lvl?", "level?");
			new Convert(ConvertMode.REPLACE_WORD, "ffs", "for fuck's sake");
			new Convert(ConvertMode.REPLACE_WORD, "af", "as fuck");
			new Convert(ConvertMode.REPLACE_WORD, "smh", "shake my head");
			new Convert(ConvertMode.REPLACE_WORD, "pls?", "please?");
			new Convert(ConvertMode.REPLACE_WORD, "plz?", "please?");
			new Convert(ConvertMode.REPLACE_WORD, "wby", "what about you");
			new Convert(ConvertMode.REPLACE_WORD, "brb", "be right back");
			new Convert(ConvertMode.REPLACE_WORD, "ik", "i know");
			new Convert(ConvertMode.REPLACE_WORD, "<lt>3", "heart");
			new Convert(ConvertMode.REPLACE_WORD, "fcape", "fire cape");
			new Convert(ConvertMode.REPLACE_WORD, "xp", "experience");
			new Convert(ConvertMode.REPLACE_WORD, "nty", "no thank you");
			
			new Convert(ConvertMode.REPLACE_TEXT, "dhide", "dragonhide");
			
			new Convert(ConvertMode.REPLACE_TEXT_AFTER_NUMBER_IN_WORD, "b", "billion");
			new Convert(ConvertMode.REPLACE_TEXT_AFTER_NUMBER_IN_WORD, "m", "million");
			new Convert(ConvertMode.REPLACE_TEXT_AFTER_NUMBER_IN_WORD, "b?", "billion?");
			new Convert(ConvertMode.REPLACE_TEXT_AFTER_NUMBER_IN_WORD, "m?", "million?");
		}
		
		public Convert(ConvertMode mode, String target, String replacement) {
			this.mode = mode;
			this.target = target;
			this.replacement = replacement;
			list.add(this);
		}

		public String convert(String input) {
			if (!this.caseSensitive) {
				input = input.toLowerCase();
			}
			
			switch(this.mode) {
				case REPLACE_TEXT:
					return input.replace(this.target, this.replacement);
				case REPLACE_WORD:
					input = input.replace(" " + this.target + " ", " " + this.replacement + " ");
					if (input.indexOf(this.target + " ") == 0) input = input.replace(this.target + " ", this.replacement + " ");
					if (input.indexOf(" " + this.target) + this.target.length() + 1 == input.length()) input = input.replace(" " + this.target, " " + this.replacement);
					if (input.equals(this.target)) input = input.replace(this.target, this.replacement);
					return input;
				case REPLACE_TEXT_AFTER_NUMBER_IN_WORD:
					String output = "";
					for (String word : input.split(" ")) {
						String test = word.replaceAll("\\d", "").replace(".", "");
						if (test.equals(this.target) && word.endsWith(this.target)) {
							word = word.replace(this.target, this.replacement);
						}
						
						output += word + " ";
					}
					
					if (!input.endsWith(" ")) output = output.substring(0, output.length() - 1);
					return output;
				default:
					return null;
			}
		}
	}
	
	public enum ConvertMode {
		REPLACE_TEXT,
		REPLACE_WORD,
		REPLACE_TEXT_AFTER_NUMBER_IN_WORD
	}
}
