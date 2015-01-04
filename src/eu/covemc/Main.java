/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.covemc;

import static eu.covemc.Config.getForcedHosts;
import static eu.covemc.Config.getMainServer;
import static eu.covemc.Config.getMainServerDomain;
import static eu.covemc.Config.getOfflineServerMessage;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

/**
 *
 * @author Mr_Dice
 */
public class Main extends Plugin implements Listener {

    HashMap<String, ServerPing> serverpings = new HashMap<>();
    ServerPing mainServerPing;

    @Override
    public void onEnable() {
        try {
            new Config(this);
        } catch (IOException ex) {
        }
        getProxy().getPluginManager().registerListener(this, this);
        pingServers();
    }

    @Override
    public void onDisable() {
        this.getProxy().getPluginManager().unregisterListener(this);
    }

    public void pingServers() {
        getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                for (String s : getForcedHosts().keySet()) {
                    String server = getForcedHosts().get(s);
                    getProxy().getServerInfo(server).ping(new Callback<ServerPing>() {
                        @Override
                        public void done(ServerPing ping, Throwable throwable) {
                            if (throwable == null) {
                                //ping.setVersion(mainServerPing.getVersion());
                                if (!serverpings.containsKey(server)) {
                                    serverpings.put(server, ping);
                                } else {
                                    serverpings.replace(server, ping);
                                }
                            } else {
                                serverpings.remove(server);
                            }
                        }
                    });
                }
                getProxy().getServerInfo(getMainServer()).ping(new Callback<ServerPing>() {
                    @Override
                    public void done(ServerPing ping, Throwable throwable) {
                        if (throwable == null) {
                            mainServerPing = ping;
                        }
                    }
                });
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    @EventHandler
    public void pingHandler(ProxyPingEvent p) {
        PendingConnection ping = p.getConnection();
        String domain = ping.getVirtualHost().toString().split(":")[0];
        if (getForcedHosts().containsKey(domain)) {
            if (serverpings.containsKey(getForcedHosts().get(domain))) {
                ServerPing newPing = p.getResponse();
                newPing.setDescription(serverpings.get(getForcedHosts().get(domain)).getDescription());
                newPing.setPlayers(serverpings.get(getForcedHosts().get(domain)).getPlayers());
                p.setResponse(newPing);
            } else {
                ServerPing offlinePing = p.getResponse();
                offlinePing.setDescription(getOfflineServerMessage());
                p.setResponse(offlinePing);
            }
        } else if (getMainServerDomain().equalsIgnoreCase(domain)) {
            if (mainServerPing != null) {
                ServerPing newPing = p.getResponse();
                newPing.setDescription(mainServerPing.getDescription());
                p.setResponse(newPing);
            } else {
                ServerPing offlinePing = p.getResponse();
                offlinePing.setDescription(getOfflineServerMessage());
                p.setResponse(offlinePing);
            }
        }
    }
}
