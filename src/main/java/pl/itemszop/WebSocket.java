package pl.itemszop;

import org.bukkit.Bukkit;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URI;
import java.util.Objects;

import static org.bukkit.Bukkit.getServer;

public class WebSocket extends WebSocketClient {
    private static final Itemszop plugin = Itemszop.getInstance();
    public WebSocket(URI serverUri) {
        super(serverUri);
    }
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        send("{\"t\":\"d\",\"d\":{\"r\":1,\"a\":\"q\",\"b\":{\"p\":\"/servers/" + plugin.serverId + "/commands/" + plugin.secret + "\",\"h\":\"\"}}}");
        if (Settings.IMP.DEBUG == true) { plugin.getLogger().info("Połączono z " + plugin.serverId); }
    }
    @Override
    public void onMessage(String message) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(message);
            if (Objects.equals(json.get("t").toString(), "d")) {
                JSONObject json_data = (JSONObject) parser.parse(json.get("d").toString());
                json_data = (JSONObject) parser.parse(json_data.get("b").toString());
                if (json_data.get("d") != null && json_data.get("d").toString().length() > 0) {
                    json_data = (JSONObject) parser.parse(json_data.get("d").toString());
                    for (Object commandId : json_data.keySet()) {
                        String command = json_data.get(commandId).toString();
                        send("{\"t\":\"d\",\"d\":{\"r\":1,\"a\":\"p\",\"b\":{\"p\":\"/servers/" + plugin.serverId + "/commands/" + plugin.secret + "/" + commandId + "\",\"d\":null}}}");
                        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(getServer().getConsoleSender(), command));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (Settings.IMP.DEBUG == true) { plugin.getLogger().info("Rozłączono z WebSocketem: " + reason); }
        if (code != 1000) {
            new WebSocketReconnectTask().runTaskTimer(plugin, 0L, (Settings.IMP.CHECK_TIME * 20 ));
        }
    }
    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }
}