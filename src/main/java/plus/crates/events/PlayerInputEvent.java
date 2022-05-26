package plus.crates.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import plus.crates.Utils.SignInputHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerInputEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final List<String> lines;

    public PlayerInputEvent(Player player, List<String> lines) {
        this.player = player;
        this.lines = lines;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public List<String> getLines() {
        return Collections.unmodifiableList(lines);
    }

}