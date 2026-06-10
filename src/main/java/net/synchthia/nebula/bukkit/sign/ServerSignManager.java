package net.synchthia.nebula.bukkit.sign;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.synchthia.nebula.api.NebulaProtos;
import net.synchthia.nebula.bukkit.NebulaPlugin;
import net.synchthia.nebula.bukkit.messages.Message;
import net.synchthia.nebula.bukkit.messages.ServerMessage;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

/**
 * @author misterT2525, Laica-Lunasys
 */

@Getter
public class ServerSignManager {
    private static final Component STARTING = Message.create("<dark_gray><bold>● STARTING ●</bold></dark_gray>");
    private static final Component ONLINE = Message.create("<dark_blue><bold>● ONLINE ●</bold></dark_blue>");
    private static final Component OFFLINE = Message.create("<dark_red><bold>■ OFFLINE ■</bold></dark_red>");
    private final NebulaPlugin plugin;
    private final SignManager signManager = new SignManager();

    public ServerSignManager(NebulaPlugin plugin) {
        this.plugin = plugin;
        File signFile = new File(plugin.getDataFolder(), "signs.json");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        if (signFile.isFile()) {
            try {
                signManager.load(signFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load sign data", e);
            }
        }
        Bukkit.getPluginManager().registerEvents(new ServerSignListener(this.plugin, this), plugin);
    }

    public void onDisable() {
        try {
            signManager.save(new File(plugin.getDataFolder(), "signs.json"));
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save sign data", e);
        }
    }

    public void updateSigns() {
        signManager.findAllSigns().forEach(sign -> {
            Component[] format = getFormat(sign.getKey());
            Sign bukkitSign = sign.getSign();
            for (int i = 0; i < format.length; i++) {
                bukkitSign.line(i, format[i]);
            }

            bukkitSign.update();
        });
    }

    public Component[] getFormat(String key) {
        NebulaProtos.ServerEntry server = plugin.getServerAPI().getServer(key).orElse(null);
        if (server != null) {
            List<TagResolver> resolvers = ServerMessage.getServerEntryResolver(server);

            if (server.getStatus().getOnline()) {
                if (server.getStatus().getPlayers().getMax() == 0) {
                    // Starting
                    return new Component[]{
                            Component.empty(),
                            Message.create("<dark_blue><bold>[<server_name>]</bold></dark_blue>", TagResolver.resolver(resolvers)),
                            STARTING,
                            Component.empty(),
                    };
                } else {
                    // Online
                    return new Component[]{
                            Message.create("<dark_blue><bold>[<server_name>]</bold></dark_blue>", TagResolver.resolver(resolvers)),
                            Message.create(server.getMotd(), TagResolver.resolver(resolvers)),
                            Message.create("<dark_gray><bold><server_online_players>/<server_max_players></bold></dark_gray>", TagResolver.resolver(resolvers)),
                            ONLINE
                    };
                }
            } else {
                // Offline
                return new Component[]{
                        Component.empty(),
                        Message.create("<dark_blue><bold>[<server_name>]</bold></dark_blue>", TagResolver.resolver(resolvers)),
                        OFFLINE,
                        Component.empty(),
                };
            }
        }

        return new Component[]{
                Component.empty(),
                Component.empty(),
                Component.empty(),
                Component.empty(),
        };
    }
}
