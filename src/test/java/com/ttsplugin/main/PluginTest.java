package com.ttsplugin.main;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PluginTest {
	public static void main(String[] args) throws Exception {
		ExternalPluginManager.loadBuiltin(TTSPlugin.class);
		RuneLite.main(args);
	}
}