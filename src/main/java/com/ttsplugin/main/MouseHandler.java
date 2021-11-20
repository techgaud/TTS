package com.ttsplugin.main;

import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.input.MouseListener;

import javax.inject.Inject;
import java.awt.event.MouseEvent;

public class MouseHandler implements MouseListener {
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private TTSPlugin plugin;

    @Override
    public MouseEvent mouseClicked(MouseEvent mouseEvent) {
        return mouseEvent;
    }

    @Override
    public MouseEvent mousePressed(MouseEvent mouseEvent) {
        return mouseEvent;
    }

    @Override
    public MouseEvent mouseReleased(MouseEvent mouseEvent) {
        return mouseEvent;
    }

    @Override
    public MouseEvent mouseEntered(MouseEvent mouseEvent) {
        return mouseEvent;
    }

    @Override
    public MouseEvent mouseExited(MouseEvent mouseEvent) {
        return mouseEvent;
    }

    @Override
    public MouseEvent mouseDragged(MouseEvent mouseEvent) {
        this.updateMousePoint();
        return mouseEvent;
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent mouseEvent) {
        this.updateMousePoint();
        return mouseEvent;
    }

    private void updateMousePoint() {
        if (!client.isMenuOpen()) {
            // Invoke on client thread because this was the previous frames position
            clientThread.invokeLater(() -> {
                plugin.setMenuOpenPoint(client.getMouseCanvasPosition());
            });
        }
    }
}
