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
				.replace('-', ' ')
				// The synthesizer seems to treat an ellipsis as nothing. Replace it with a period.
				.replace("...", ". ")
		);

		this.sender = sender;
	}
	
	public static Dialog getCurrentDialog(Client client) {
		Widget playerText = client.getWidget(ComponentID.DIALOG_PLAYER_TEXT);
		if (isVisible(playerText)) {
			return new Dialog(playerText.getText(), client.getLocalPlayer().getName());
		}
		Widget npcText = client.getWidget(ComponentID.DIALOG_NPC_TEXT);
		if (isVisible(npcText)) {
			return new Dialog(npcText.getText(), client.getWidget(ComponentID.DIALOG_NPC_NAME).getText());
		}
		Widget dialogOptions = client.getWidget(ComponentID.DIALOG_OPTION_OPTIONS);
		if (isVisible(dialogOptions)) {
			return new Dialog(dialogOptions.getText(), "");
		}
		Widget levelSkill = client.getWidget(ComponentID.LEVEL_UP_SKILL);
		if (isVisible(levelSkill)) {
			return new Dialog(levelSkill.getText(), client.getWidget(ComponentID.LEVEL_UP_LEVEL).getText());
		}
		Widget catAge = client.getWidget(WidgetUtil.packComponentId(229, 1));
		if (isVisible(catAge)) {
			return new Dialog(catAge.getText(), "");
		}
		return null;
	}

	private static boolean isVisible(Widget widget) {
		return widget != null && !widget.isHidden();
	}
}
