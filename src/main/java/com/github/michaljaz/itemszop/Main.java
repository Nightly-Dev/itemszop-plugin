package com.github.michaljaz.itemszop;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main extends JavaPlugin {
    String serverId;
    String databaseUrl;
    String triggerPort;
    FirebaseSync sync;
    HttpServer triggerServer;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        getLogger().info("\n" +
                "§6(_)| |                                           \n" +
                "§6 _ | |_   ___  _ __ ___   ___  ____  ___   _ __  \n" +
                "§6| || __| / _ \\| '_ ` _ \\ / __||_  / / _ \\ | '_ \\ \n" +
                "§6| || |_ |  __/| | | | | |\\__ \\ / / | (_) || |_) |\n" +
                "§6|_| \\__| \\___||_| |_| |_||___//___| \\___/ | .__/ \n §2" +
                "                          " + this.getDescription().getVersion() + "            §6| |    \n" +
                "\n" + "§fDeveloped by " + this.getDescription().getAuthors() + " dla https://github.com/michaljaz/itemszop §a" + "\n§fPlugin został załadowany w §a" + (System.currentTimeMillis() - startTime) + "ms§7.\n§fWykryty silnik: " + Bukkit.getVersion().split("-")[1]);

        //config file
        FileConfiguration config = this.getConfig();
        config.addDefault("serverId", "");
        config.addDefault("databaseUrl", "https://sklepmc-c7516-default-rtdb.europe-west1.firebasedatabase.app");
        config.addDefault("triggerPort", 8001);
        config.options().copyDefaults(true);
        saveConfig();
        serverId = config.getString("serverId");
        databaseUrl = config.getString("databaseUrl");
        triggerPort = config.getString("triggerPort");

        getLogger().info("Server ID: " + serverId);
        sync = new FirebaseSync(this);
        this.getCommand("itemszop_update").setExecutor(new ItemszopUpdate(this));

        try {
            triggerServer = HttpServer.create(new InetSocketAddress("localhost", Integer.parseInt(triggerPort)), 0);
            triggerServer.createContext("/itemszop_update", new MyHandler(this));
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            triggerServer.setExecutor(threadPoolExecutor);
            triggerServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        triggerServer.stop(0);
        // Plugin shutdown logic
    }

    static class MyHandler implements HttpHandler {
        Main plugin;
        public MyHandler(Main plugin){
            this.plugin = plugin;
        }
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "OK";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            plugin.sync.syncWithFirebase();
            plugin.getLogger().info("Itemszop commands updated [web-trigger]");
        }
    }
}

