package org.example.nes.display;

import org.example.nes.input.StandardControllerAdapter;
import org.example.nes.input.StandardControllerButton;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class AWTStandardControllerAdapter extends KeyAdapter implements StandardControllerAdapter {
    private static final Map<Integer, StandardControllerButton> KEY_MAP = Map.ofEntries(
            Map.entry(KeyEvent.VK_W, StandardControllerButton.UP),
            Map.entry(KeyEvent.VK_A, StandardControllerButton.LEFT),
            Map.entry(KeyEvent.VK_S, StandardControllerButton.DOWN),
            Map.entry(KeyEvent.VK_D, StandardControllerButton.RIGHT),
            Map.entry(KeyEvent.VK_ENTER, StandardControllerButton.A),
            Map.entry(KeyEvent.VK_1, StandardControllerButton.SELECT),
            Map.entry(KeyEvent.VK_SPACE, StandardControllerButton.B),
            Map.entry(KeyEvent.VK_2, StandardControllerButton.START)
    );

    private final Set<StandardControllerButton> activeKeys = EnumSet.noneOf(StandardControllerButton.class);

    @Override
    public void keyPressed(KeyEvent e) {
        final StandardControllerButton button = KEY_MAP.get(e.getKeyCode());
        if (button == null) {
            return;
        }
        synchronized (this) {
            activeKeys.add(button);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        final StandardControllerButton button = KEY_MAP.get(e.getKeyCode());
        if (button == null) {
            return;
        }
        synchronized (this) {
            activeKeys.remove(button);
        }
    }

    @Override
    public synchronized boolean isButtonPressed(StandardControllerButton button) {
        return activeKeys.contains(button);
    }
}
