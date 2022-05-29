package com.ttsplugin.main;

import com.ttsplugin.enums.Language;
import com.ttsplugin.enums.Voice;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;

@ConfigGroup("tts")
public interface TTSConfig extends Config {
	@ConfigSection(name = "General", description = "General settings", position = 20, closedByDefault = false)
	String generalSettings = "generalSettings";

	@ConfigItem(keyName = "ignoreSpam", name = "Ignore Spam", description = "Ignores messages that are sent multiple times by the same sender", position = 21, section = generalSettings)
	default boolean ignoreSpam() {
		return true;
	}

	@ConfigItem(keyName = "autoChat", name = "Autochat", description = "Speaks messages sent by auto chat", position = 22, section = generalSettings)
	default boolean autoChat() {
		return false;
	}

	@ConfigItem(keyName = "chatMessages", name = "Chat messages", description = "Speaks chat messages sent by players", position = 23, section = generalSettings)
	default boolean chatMessages() {
		return true;
	}

	@ConfigItem(keyName = "gameMessages", name = "Game messages", description = "Also speaks the game messages sent in chat <br> You can select the voice for this in the voice section", position = 24, section = generalSettings)
	default boolean gameMessages() {
		return false;
	}

	@ConfigItem(keyName = "notificationMessages", name = "Notification messages", description = "Speak notifications. \"RuneLite\" > \"Notification Settings\" > \"Game message notifications\" must be enabled.", position = 25, section = generalSettings)
	default boolean notificationMessages() {
		return false;
	}

	@ConfigItem(keyName = "dialogs", name = "Dialogs", description = "Applies text to speech for dialogs too <br> You can set the NPC voice by disabling random voice and setting the Dialog voice in voice settings", position = 26, section = generalSettings)
	default boolean dialogs() {
		return true;
	}

	@ConfigItem(keyName = "blacklistedWords", name = "Blacklisted words", description = "Any message that contains these words will not be spoken <br> Write the word then press enter for new line <br> Each word needs to be longer than 1 character", position = 27, section = generalSettings)
	default String blacklistedWords() {
		return "";
	}

	@ConfigItem(keyName = "whitelist", name = "Whitelist", description = "Invert blacklist functionality to only speak messages that contain those words.", position = 28, section = generalSettings)
	default boolean whitelist() {
		return false;
	}

	@Range(min = 1, max = 60)
	@ConfigItem(keyName = "queueSeconds", name = "Queue seconds", description = "If a message is already playing how long to queue the next messages for in seconds <br> So it will play them after the current one finishes", position = 87, section = generalSettings)
	default double queueSeconds() {
		return 2.5;
	}

	@ConfigSection(name = "Voice", description = "Voice settings", position = 46, closedByDefault = false)
	String voiceSettings = "voiceSettings";

	@Range(min = -6, max = 10)
	@ConfigItem(keyName = "rate", name = "Speed", description = "How fast it speaks", position = 47, section = voiceSettings)
	default int rate() {
		return 1;
	}

	@Range(min = 1, max = 20)
	@ConfigItem(keyName = "volume", name = "Volume", description = "Volume for tts. Like how loud it speaks", position = 48, section = voiceSettings)
	default int volume() {
		return 15;
	}

	@ConfigItem(keyName = "distanceVolume", name = "Distance volume", description = "Chat messages sent by players further away will be quieter than people close to you <br> You can set how much the effect is in the advanced tab", position = 49, section = voiceSettings)
	default boolean distanceVolume() {
		return true;
	}

	@ConfigItem(keyName = "randomVoice", name = "Random voice", description = "Chooses a random voice from the selected language below for usernames. <br> The same username will always have the same voice for every message <br> Note: If you want to use one single voice then disable this and set the Voice setting", position = 50, section = voiceSettings)
	default boolean randomVoice() {
		return true;
	}

	@ConfigItem(keyName = "randomVoiceLanguage", name = "Random voice language", description = "The language for the above setting", position = 51, section = voiceSettings)
	default Language randomVoiceLanguage() {
		return Language.ENGLISH;
	}

	@ConfigItem(keyName = "gameMessageVoice", name = "Game message voice", description = "Voice for game messages if \"Game messages\" is enabled (in the general settings)", position = 52, section = voiceSettings)
	default Voice gameMessageVoice() {
		return Voice.ZIRA;
	}

	@ConfigItem(keyName = "notificationMessageVoice", name = "Notification message voice", description = "Voice for notification messages if \"Notification messages\" is enabled (in the general settings)", position = 53, section = voiceSettings)
	default Voice notificationMessageVoice() {
		return Voice.ZIRA;
	}

	@ConfigItem(keyName = "voice", name = "Voice", description = "Voice for tts <br> Note: This wont do anything if random voice is enabled", position = 54, section = voiceSettings)
	default Voice voice() {
		return Voice.HAZEL;
	}

	@ConfigItem(keyName = "dialogVoice", name = "Dialog voice", description = "Voice for dialogs if Random voice is disabled and Dialogs is enabled in general settings <br> This only applies to the NPC not you. The voice above applies to you", position = 55, section = voiceSettings)
	default Voice dialogVoice() {
		return Voice.GEORGE;
	}

	@ConfigItem(keyName = "useDialogVoiceWithRandom", name = "Use dialog voice with random voice on", description = "Speaks all dialogs with the dialog voice even if random voice is enabled", position = 56, section = voiceSettings)
	default boolean useDialogVoiceWithRandom() {
		return false;
	}

	@ConfigSection(name = "Accessibility", description = "Accessibility settings <br> These settings use the Game message voice set in the voice settings", position = 47, closedByDefault = true)
	String accessibilitySettings = "accessibilitySettings";

	@ConfigItem(keyName = "enableOnClick", name = "On Click", description = "Enables narrating on click", section = accessibilitySettings, position = 1)
	default boolean enableOnClick() {
		return false;
	}

	@ConfigItem(keyName = "narrateHotkey", name = "Narrate Hotkey", description = "The hotkey that triggers narration for what you're hovering over", section = accessibilitySettings, position = 2)
	default Keybind narrateHotkey() {
		return Keybind.NOT_SET;
	}

	@ConfigItem(keyName = "narrateQuantityHotkey", name = "Narrate Quantity Hotkey", description = "The hotkey that narrates the quantity of the hovered item", section = accessibilitySettings, position = 3)
	default Keybind narrateQuantityHotkey() {
		return Keybind.NOT_SET;
	}

	@ConfigSection(name = "Advanced", description = "Advanced settings", position = 85, closedByDefault = true)
	String advancedSettings = "advancedSettings";

	@Range(min = 5, max = 30)
	@ConfigItem(keyName = "distanceVolumeEffect", name = "Distance volume effect", description = "Controls how much more quiet the sound is when the sender is further away <br> Lower value = More quieter the further away the sender is", position = 86, section = advancedSettings)
	default int distanceVolumeEffect() {
		return 18;
	}

	@Range(min = 1, max = 15)
	@ConfigItem(keyName = "spamMessages", name = "Spam messages", description = "How many same messages from the same sender are allowed for the last 30 seconds <br> This applies for the Ignore Spam setting", position = 88, section = advancedSettings)
	default int spamMessages() {
		return 2;
	}

	@ConfigItem(keyName = "useVoiceForSelfWithRandom", name = "Use voice for self with random voice", description = "Uses the voice set in the Voice setting in voice section <br> As your voice even when random voice is enabled", position = 89, section = advancedSettings)
	default boolean useVoiceForSelfWithRandom() {
		return false;
	}
	
	@ConfigItem(keyName = "chatMessagesFriendsOnly", name = "Chat friends only", description = "Only speaks messages sent by your ingame friends if Chat messages setting is enabled", position = 90, section = advancedSettings)
	default boolean chatMessagesFriendsOnly() {
		return false;
	}
}