package com.ttsplugin.main;

import lombok.Synchronized;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Singleton
public class SettingsManager {
	private final Collection<String> specialPhrases = new CopyOnWriteArrayList<>();

	@Inject
	private TTSConfig config;

	void init() {
		setSpecialPhrases(config.denylistedWords());
	}

	void clear() {
		setSpecialPhrases("");
	}

	void onConfigChange(String key, String value) {
		if ("blacklistedWords".equals(key)) {
			setSpecialPhrases(value);
		}
	}

	public boolean passesAllowDenyList(String message) {
		final boolean mode = config.allowlist();
		final String msg = message.toLowerCase();
		for (String phrase : specialPhrases) {
			if (msg.contains(phrase)) {
				return mode;
			}
		}
		return !mode;
	}

	@Synchronized
	private void setSpecialPhrases(String configValue) {
		specialPhrases.clear();
		specialPhrases.addAll(
				configValue.lines()
						.map(String::trim)
						.filter(s -> !s.isEmpty())
						.map(String::toLowerCase)
						.distinct()
						.collect(Collectors.toList())
		);
	}
}
