package pl.itemszop;

import org.bukkit.scheduler.BukkitRunnable;

import static pl.itemszop.Itemszop.socket;

public class WebSocketReconnectTask extends BukkitRunnable {
    private static Itemszop task = Itemszop.getInstance();

    @Override
    public void run() {
        if (!socket.isOpen()) {
            task.getLogger().info("Reconnecting to websocket...");
            socket.reconnect();
        } else {
            cancel();
        }
    }
}