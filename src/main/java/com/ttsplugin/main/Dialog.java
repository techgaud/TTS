package com.ttsplugin.main;

import lombok.Value;
import net.runelite.api.Client;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.util.Text;

@Value
public class Dialog {
	String message;
	String sender;
	
	public Dialog(String message, String sender) {
		this.message = Text.sanitizeMultilineText(
			message
				// Replace hyphens with spaces. It has trouble processing utterances.
				.replaceAll("-", " ")
				// The synthesizer seems to treat an ellipsis as nothing. Replace it with a period.
				.replaceAll("\\.\\.\\.", ". ")
		);

		this.sender = sender;
	}
	
	public static Dialog getCurrentDialog(Client client) {
		if (isVisible(client.getWidget(ComponentID.DIALOG_PLAYER_TEXT))) {
			return new Dialog(client.getWidget(ComponentID.DIALOG_PLAYER_TEXT).getText(), client.getLocalPlayer().getName());
		} else if (isVisible(client.getWidget(ComponentID.DIALOG_NPC_TEXT))) {
			return new Dialog(client.getWidget(ComponentID.DIALOG_NPC_TEXT).getText(), client.getWidget(ComponentID.DIALOG_NPC_NAME).getText());
		} else if (isVisible(client.getWidget(ComponentID.DIALOG_OPTION_OPTIONS))) {
			return new Dialog(client.getWidget(ComponentID.DIALOG_OPTION_OPTIONS).getText(), "");
		} else if (isVisible(client.getWidget(ComponentID.LEVEL_UP_SKILL))) {
			return new Dialog(client.getWidget(ComponentID.LEVEL_UP_SKILL).getText(), client.getWidget(ComponentID.LEVEL_UP_LEVEL).getText());
		} else if (isVisible(client.getWidget(WidgetUtil.packComponentId(229, 1)))) { // Cat age
			return new Dialog(client.getWidget(WidgetUtil.packComponentId(229, 1)).getText(), "");
		} else {
			return null;
		}
	}

	private static boolean isVisible(Widget widget) {
		return widget != null && !widget.isHidden();
	}
}
