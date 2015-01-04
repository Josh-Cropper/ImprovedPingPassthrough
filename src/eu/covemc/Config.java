package eu.covemc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Config {

    public static Configuration config;
    public static Configuration forcedHostsConf;
    static Main pl;
    static HashMap<String, String> forcedHosts = new HashMap<>();
    static String mainServer;
    static String mainServerDomain;
    static String offlineServerMessage;
    static boolean toggle = false;

    Config(Main aThis) throws IOException {
        this.pl = aThis;
        getConfig();
    }

    public static void getConfig() throws IOException {

        config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File("config.yml"));

        BufferedReader br = new BufferedReader(new FileReader("config.yml"));
        BufferedWriter bw = new BufferedWriter(new FileWriter("config.yml", true));
        try {
            StringBuilder sb = new StringBuilder();
            Object[] lines = br.lines().toArray();

            for (Object o : lines) {
                String s = o.toString();
                if (s.contains("  ping_passthrough: false") || s.contains("  ping_passthrough: true")) {
                    toggle = false;
                }
                if (toggle == true) {
                    String[] lineSplit = s.split(": ");
                    forcedHosts.put(lineSplit[0].replace(" ", ""), lineSplit[1]);
                }
                if (s.contains(" default_server:")) {
                    mainServer = s.split(": ")[1];
                    pl.getProxy().getLogger().info("Line = " + s);
                    pl.getProxy().getLogger().info("Main Server 1 = " + mainServer);
                }
                if (s.equals("  forced_hosts:")) {
                    toggle = true;
                }
                if (s.contains("default_server_domain:")) {
                    mainServerDomain = s.split(": ")[1];
                }
                if (s.contains("offline_server_message:")){
                 StringBuilder sb2 = new StringBuilder();
                 for (String s2 : s.split(": ")){
                 if(!s2.equals("offline_server_message")){
                 sb2.append(s2);
                 }
                 }
                 offlineServerMessage = ChatColor.translateAlternateColorCodes('&', sb2.toString()).replace("\"", "");
                 }
            }
            if (!lines[lines.length - 1].toString().contains("default_server_domain:")
                    && !lines[lines.length - 2].toString().contains("default_server_domain:")) {
                bw.append("\ndefault_server_domain: covemc.eu");
                mainServerDomain = "covemc.eu";
            }
            if (!lines[lines.length - 1].toString().contains("offline_server_message:")
                    && !lines[lines.length - 2].toString().contains("offline_server_message:")) {
                bw.append("\noffline_server_message: \"&9Cove&e&lNetwork &f» &cSorry, This server is offline!\"");
                offlineServerMessage = ChatColor.BLUE + "Cove" + ChatColor.YELLOW + ChatColor.BOLD + "Network" + ChatColor.WHITE + " » " + ChatColor.RED + "Sorry, This server is offline!";
                pl.getProxy().getLogger().info("Test");
            }
        } finally {
            bw.close();
            br.close();
        }
    }

    public static HashMap<String, String> getForcedHosts() {
        return forcedHosts;
    }

    public static String getMainServer() {
        return mainServer;
    }

    public static String getMainServerDomain() {
        return mainServerDomain;
    }

    public static String getOfflineServerMessage() {
        return offlineServerMessage;
    }
}
