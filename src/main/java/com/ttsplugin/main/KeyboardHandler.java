package com.ttsplugin.main;

import javax.inject.Inject;

import com.ttsplugin.enums.MessageType;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.Keybind;

public class KeyboardHandler {
    private static final int MENU_OPTION_HEIGHT = 15;
    private static final int MENU_EXTRA_TOP = 4;
    private static final int MENU_EXTRA_BOTTOM = 3;
    private static final int MENU_BORDERS_TOTAL = MENU_EXTRA_TOP + MENU_OPTION_HEIGHT + MENU_EXTRA_BOTTOM;

    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private TTSPlugin plugin;
    @Inject
    private TTSConfig config;

    public void handleHotkey(Keybind keybind) {
        // This solves an issue where sometimes client.getMenuEntries().last() was "Examine object-behind-widget" ¯\_(ツ)_/¯
        this.clientThread.invokeLater(() -> {
            MenuEntry[] entries = this.client.getMenuEntries();
            int idx = entries.length - 1;
            if (this.config.narrateQuantityHotkey() == keybind) {
                if (idx == -1)
                    return;
                MenuEntry hoveredEntry = entries[idx];
                Widget hoveredWidget = this.client.getWidget(hoveredEntry.getParam1());
                if (hoveredWidget != null) {
                    Widget childWidget = hoveredWidget.getChild(hoveredEntry.getParam0());
                    if (childWidget != null && childWidget.getItemId() != -1) {
                        plugin.processMessage(childWidget.getItemQuantity() + "", MessageType.ACCESSIBILITY);
                    } else if (hoveredWidget.getId() == WidgetInfo.INVENTORY.getId()) {
                        WidgetItem itemWidget = hoveredWidget.getWidgetItem(hoveredEntry.getParam0());
                        int quantity = itemWidget.getQuantity();
                        plugin.processMessage(quantity + "", MessageType.ACCESSIBILITY);
                    }
                }
            } else if (this.config.narrateHotkey() == keybind) {
                if (this.client.isMenuOpen()) {
                    // Shamelessly stolen from contextual-cursor :)
                    final int menuTop;
                    final int menuHeight = (entries.length * MENU_OPTION_HEIGHT) + MENU_BORDERS_TOTAL;
                    if (menuHeight + this.plugin.getMenuOpenPoint().getY() > this.client.getCanvasHeight()) {
                        menuTop = this.client.getCanvasHeight() - menuHeight;
                    } else {
                        menuTop = this.plugin.getMenuOpenPoint().getY();
                    }

                    final int fromTop = Math.max((this.client.getMouseCanvasPosition().getY() - MENU_EXTRA_TOP) - menuTop, MENU_OPTION_HEIGHT);

                    idx = entries.length - (fromTop / MENU_OPTION_HEIGHT);
                }
                idx = Math.min(entries.length - 1, Math.max(0, idx));
                MenuEntry hoveredEntry = entries[idx];
                Widget hoveredWidget = this.client.getWidget(hoveredEntry.getParam1());
                if (hoveredWidget != null && hoveredWidget.getId() == WidgetInfo.PACK(553, 14)) { // Report reason
                    String msg = hoveredWidget.getChild(hoveredEntry.getParam0() + 1).getText() + " " + hoveredWidget.getChild(hoveredEntry.getParam0() + 2).getText();
                    plugin.processMessage(msg, MessageType.ACCESSIBILITY);
                } else if (hoveredWidget != null && hoveredWidget.getParent().getId() == WidgetInfo.PACK(553, 7)) { // Report add to ignore
                    plugin.processMessage(this.client.getWidget(553, 8).getText(), MessageType.ACCESSIBILITY);
                } else if (hoveredWidget != null && hoveredWidget.getParent() != null && hoveredWidget.getParent().getId() == WidgetInfo.BANK_PIN_CONTAINER.getId()) {
                    String number = hoveredWidget.getChild(1).getText();
                    plugin.processMessage(number, MessageType.ACCESSIBILITY);
                } else if (hoveredWidget != null && hoveredWidget.getId() == WidgetInfo.INVENTORY.getId()) {
                    plugin.processMessage(hoveredEntry.getOption() + " " + hoveredEntry.getTarget(), MessageType.ACCESSIBILITY);
                } else {
                    if (this.client.isMenuOpen() || (hoveredEntry.getType() != MenuAction.WALK && hoveredEntry.getType() != MenuAction.CANCEL)) {
                        plugin.processMessage(hoveredEntry.getOption() + " " + hoveredEntry.getTarget(), MessageType.ACCESSIBILITY);
                    }
                }
            }
        });
    }
}
