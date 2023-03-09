package com.ttsplugin.main;

import com.google.common.io.ByteStreams;
import com.google.inject.Provides;
import com.ttsplugin.enums.Gender;
import com.ttsplugin.enums.MessageType;
import com.ttsplugin.enums.Voice;
import com.ttsplugin.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NotificationFired;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@PluginDescriptor(name = "Text to speech", description = "Text to speech for chat, dialog, menu options and notifications", tags = {"tts", "text", "voice", "chat", "dialog", "speak", "notification"})
public class TTSPlugin extends Plugin {	
	private final Map<String, List<Long>> spamHash = new HashMap<>();
	private final BlockingQueue<TTSMessage> queue = new LinkedBlockingQueue<>();
	private long lastProcess;
	private Dialog lastDialog;
	private volatile Clip currentClip;
	private final AtomicReference<Future<?>> queueTask = new AtomicReference<>();

	@Inject
	private Client client;
	
	@Inject
	private TTSConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private KeyManager keyManager;
	@Inject
	private KeyboardHandler keyboardHandler;

	@Inject
	private MouseManager mouseManager;
	@Inject
	private MouseHandler mouseHandler;

	@Getter
	@Setter
	private Point menuOpenPoint;

	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> this.config.narrateHotkey()) {
		@Override
		public void hotkeyPressed() {
			keyboardHandler.handleHotkey(config.narrateHotkey());
		}
	};
	
	private final HotkeyListener quantityHotkeyListener = new HotkeyListener(() -> this.config.narrateQuantityHotkey()) {
		@Override
		public void hotkeyPressed() {
			keyboardHandler.handleHotkey(config.narrateQuantityHotkey());
		}
	};
	
	@Override
	protected void startUp() {
		// TODO: consolidate hotkey vs click message processing
		this.keyManager.registerKeyListener(this.hotkeyListener);
		this.keyManager.registerKeyListener(this.quantityHotkeyListener);
		this.mouseManager.registerMouseListener(this.mouseHandler);
		
		// New task for playing messages from queue. this will be terminated when the plugin is disabled
		Future<?> future = executor.scheduleWithFixedDelay(() -> {
			TTSMessage message;
			while ((message = queue.poll()) != null) {
				if ((double) Math.abs(message.getTime() - System.currentTimeMillis()) / (double) 1000 <= this.config.queueSeconds()) {
					play(message);
				}
			}
		}, 50, 50, TimeUnit.MILLISECONDS);
		queueTask.set(future);
	}

	@Override
	protected void shutDown() {
		this.keyManager.unregisterKeyListener(this.hotkeyListener);
		this.keyManager.unregisterKeyListener(this.quantityHotkeyListener);
		this.mouseManager.unregisterMouseListener(this.mouseHandler);

		lastProcess = 0;
		lastDialog = null;
		menuOpenPoint = null;
		currentClip = null;

		// Terminate queue task
		queue.clear();
		Future<?> task = queueTask.getAndSet(null);
		if (task != null)
			task.cancel(false);
	}

	@Provides
	TTSConfig getConfig(ConfigManager configManager) {
		return configManager.getConfig(TTSConfig.class);
	}

	@Subscribe
	public void onNotificationFired(NotificationFired event) {
		if (config.notificationMessages() && passesBlacklist(event.getMessage())) {
			processMessage(event.getMessage(), MessageType.NOTIFICATION);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if (passesBlacklist(event.getMessage())) {
			processMessage(event.getMessage(), event.getName(), event.getType(), MessageType.CHAT);
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (config.dialogs()) {
			Dialog dialog = Dialog.getCurrentDialog(client);

			if (dialog != null && !dialog.equals(lastDialog)) {
				Clip current = this.currentClip;
				if (current != null) current.stop();
				processMessage(dialog.getMessage(), dialog.getSender(), MessageType.DIALOG);
			}
			
			lastDialog = dialog;
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked) {
		if (!this.config.enableOnClick())
			return;

		boolean blacklist = menuOptionClicked.getMenuAction() != MenuAction.WALK &&
			menuOptionClicked.getMenuAction() != MenuAction.CANCEL &&
			menuOptionClicked.getMenuAction() != MenuAction.WIDGET_CONTINUE;

		// If the menu is open, and you click on a menu option, say it
		// If the menu is not open (clicking on something), only say it if it is not Walk, Cancel, or a dialog option
		if (this.client.isMenuOpen() || blacklist) {
			this.sayMenuOptionClicked(menuOptionClicked);
		}
	}

	public boolean passesBlacklist(String message) {
		boolean found = false;
		for (String line : config.blacklistedWords().split("\\r?\\n")) {
			if (line.length() > 1 && message.contains(line)) {
				if(config.whitelist()) {
					found = true;
					break;
				} else {
					return false;
				}
			}
		}

		return !config.whitelist() || found;
	}

	public void processMessage(String message, MessageType messageType) {
		processMessage(message, "", null, messageType);
	}
	
	public void processMessage(String message, String sender, MessageType messageType) {
		processMessage(message, sender, null, messageType);
	}

	//TODO: Fix this mess (more convenient way to check conditions)
	public void processMessage(String message, String sender, ChatMessageType type, MessageType messageType) {
		if (Math.abs(System.currentTimeMillis() - lastProcess) < 50) return;
		
		int voice = 0;
		int distance = 1;
		if (messageType == MessageType.CHAT) {
			Player player = getPlayerFromUsername(sender);
			if (type != ChatMessageType.PUBLICCHAT && type != ChatMessageType.AUTOTYPER && !config.gameMessages()) return;
			if (type == ChatMessageType.AUTOTYPER && !config.autoChat()) return;
			if (!sender.isEmpty() && ignoreSpam(message, sender) && config.ignoreSpam()) return;
			if (!config.chatMessages() && !sender.isEmpty()) return;
			if (config.chatMessagesFriendsOnly() && !player.isFriend()) return;
			
			voice = getVoice(sender, player == null ? Gender.UNKNOWN : Gender.get(player.getPlayerComposition().isFemale())).id;
			distance = player == null ? 0 : client.getLocalPlayer().getWorldLocation().distanceTo(player.getWorldLocation());
		} else if (messageType == MessageType.DIALOG) {
			if (sender.equals(client.getLocalPlayer().getName())) {
				voice = getVoice(sender, Gender.get(client.getLocalPlayer().getPlayerComposition().isFemale())).id;
			} else {
				if (config.randomVoice() && !config.useDialogVoiceWithRandom()) {
					voice = getVoice(sender, Gender.UNKNOWN).id;
				} else {
					voice = config.dialogVoice().id;
				}
			}
		} else if (messageType == MessageType.ACCESSIBILITY) {
			voice = config.gameMessageVoice().id;
		} else if (messageType == MessageType.NOTIFICATION) {
			voice = config.notificationMessageVoice().id;
		}
		
		lastProcess = System.currentTimeMillis();
		
		final int voice2 = voice;
		final int distance2 = distance;
		executor.execute(() -> {
			try {
				addToQueue(ConvertMessage.convert(message), voice2, distance2);
			} catch (Exception e) {
				log.warn("Failed to queue message", e);
			}
		});
	}
	
	/**
	 * Adds this message to the queue.
	 */
	public void addToQueue(String message, int voice, int distance) {
		this.queue.add(new TTSMessage(message, voice, distance, System.currentTimeMillis()));
	}
	
	/**
	 * Plays the text with the specified voice and distance
	 */
	private void play(TTSMessage message) {
		try {
			String request = "https://ttsplugin.com?m=" + URLEncoder.encode(message.getMessage(), "UTF-8") + "&r=" + config.rate() + "&v=" + message.getVoice();

			byte[] bytes;
			try (InputStream stream = new URL(request).openConnection().getInputStream()) {
				bytes = ByteStreams.toByteArray(stream);
			}

			try (AudioInputStream inputStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(bytes))) {
				try (Clip clip = AudioSystem.getClip()) {
					clip.open(inputStream);
					currentClip = clip;

					if (config.distanceVolume()) {
						Utils.setClipVolume((config.volume() / (float) 10) - ((float) message.getDistance() / (float) config.distanceVolumeEffect()), clip);
					} else {
						Utils.setClipVolume(config.volume() / (float) 10, clip);
					}

					clip.start();
					do {
						Utils.sleep(50);
						if (client.getGameState() == GameState.LOGIN_SCREEN || queueTask.get() == null) {
							clip.stop();
							break;
						}
					} while (clip.isRunning());

					currentClip = null;
				}
			}
		} catch (Exception e) {
			log.warn("Failed to play clip", e);
		}
	}
	
	public Voice getVoice(String sender, Gender gender) {
		if (config.useVoiceForSelfWithRandom() && sender.equals(client.getLocalPlayer().getName())) {
			return config.voice();
		}
		
		if (config.gameMessages() && sender.isEmpty()) {
			return config.gameMessageVoice();
		}
		
		if (config.randomVoice()) {
			List<Voice> voices = new ArrayList<>();
			for (Voice voice : Voice.values()) {
				if (voice.language.equals(config.randomVoiceLanguage()) && (voice.gender == gender || gender == Gender.UNKNOWN)) {
					voices.add(voice);
				}
			}
			
			if (voices.isEmpty()) {
				return getVoice(sender, gender.reverse());
			}
			
			return voices.get(Math.abs(sender.hashCode()) % voices.size());
		}
		
		return config.voice();
	}
	
	@Synchronized
	public boolean ignoreSpam(String message, String sender) {
		long ms = System.currentTimeMillis();

		spamHash.values().removeIf(values -> {
			values.removeIf(value -> Math.abs(ms - value) > 30000);
			return values.isEmpty();
		});

		String key = message + sender;
		List<Long> list = spamHash.computeIfAbsent(key, k -> new ArrayList<>());
		list.add(ms);
		return list.size() > config.spamMessages();
	}
	
	private Player getPlayerFromUsername(String username) {
		String sanitized = Text.sanitize(username);
		for (Player player : client.getCachedPlayers()) {
			if (player != null && player.getName() != null && Text.sanitize(player.getName()).equals(sanitized)) {
				return player;
			}
		}
		return null;
	}

	private void sayMenuOptionClicked(MenuOptionClicked menuOptionClicked) {
		String actionName = menuOptionClicked.getMenuOption();
		String itemName = menuOptionClicked.getMenuTarget();
		final Widget widget = this.client.getWidget(menuOptionClicked.getParam1());
		if (widget != null) {
			if (widget.getChildren() != null) {
				if (widget.getParent().getId() == WidgetInfo.BANK_PIN_CONTAINER.getId()) {
					actionName = widget.getChild(1).getText();
				} else if (widget.getId() == WidgetInfo.PACK(553, 14)) { // Report reason
					actionName = widget.getChild(menuOptionClicked.getParam0() + 1).getText() + " " +
						widget.getChild(menuOptionClicked.getParam0() + 2).getText();
					// In bank ui (maybe other things too like deposit boxes or things like that?)
				} else {
					Widget child = widget.getChild(menuOptionClicked.getParam0());
					if (child != null && child.getItemId() > -1) {
						itemName = this.itemManager.getItemComposition(child.getItemId()).getName();
					}
				}
			} else if (widget.getParent().getId() == WidgetInfo.PACK(553, 7)) { // Report add to ignore
				actionName = this.client.getWidget(553, 8).getText();
				// normal inventory
			} else if (widget.getId() == WidgetInfo.INVENTORY.getId()) {
				int itemID = widget.getItemId();
				ItemComposition item = this.itemManager.getItemComposition(itemID);
				itemName = item.getName();
				// Fallback
			} else if (menuOptionClicked.getParam0() > -1) {
				itemName = widget.getChild(menuOptionClicked.getParam0()).getText();
			}
		}
		
		processMessage(actionName + " " + itemName, MessageType.ACCESSIBILITY);
	}
}
