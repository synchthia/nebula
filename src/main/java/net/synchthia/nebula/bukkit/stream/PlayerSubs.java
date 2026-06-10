package net.synchthia.nebula.bukkit.stream;

import lombok.RequiredArgsConstructor;
import net.synchthia.nebula.api.APIClient;
import net.synchthia.nebula.api.NebulaProtos;
import net.synchthia.nebula.api.player.PlayerProperty;
import net.synchthia.nebula.bukkit.NebulaPlugin;
import net.synchthia.nebula.bukkit.tablist.TabListEntry;
import redis.clients.jedis.JedisPubSub;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

@RequiredArgsConstructor
public class PlayerSubs extends JedisPubSub {
    private final NebulaPlugin plugin;

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        NebulaProtos.PlayerPropertiesStream playerPropertiesStream = APIClient.playerPropertiesStreamFromJson(message);
        assert playerPropertiesStream != null;
        switch (playerPropertiesStream.getType()) {
            case JOIN_SOLO -> {
                NebulaProtos.PlayerProfile profile = playerPropertiesStream.getSolo();
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getTabList().addPlayer(new TabListEntry(
                            UUID.fromString(profile.getPlayerUUID()),
                            profile.getPlayerName(),
                            profile.getPlayerLatency(),
                            profile.getCurrentServer(),
                            PlayerProperty.fromProtobuf(profile.getPropertiesList()),
                            profile.getHide()
                    ));
                    plugin.getPlayerAPI().addPlayer(profile);
                });
            }
            case QUIT_SOLO -> {
                NebulaProtos.PlayerProfile profile = playerPropertiesStream.getSolo();
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getTabList().removePlayer(UUID.fromString(profile.getPlayerUUID()));
                    plugin.getPlayerAPI().removePlayer(profile);
                });
            }
            case ADVERTISE_ALL -> {
                List<NebulaProtos.PlayerProfile> profiles = playerPropertiesStream.getAllList();
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getPlayerAPI().updatePlayers(profiles);

                    Map<UUID, TabListEntry> entries = new HashMap<>();
                    for (NebulaProtos.PlayerProfile profile : profiles) {
                        entries.put(UUID.fromString(profile.getPlayerUUID()), new TabListEntry(
                                UUID.fromString(profile.getPlayerUUID()),
                                profile.getPlayerName(),
                                profile.getPlayerLatency(),
                                profile.getCurrentServer(),
                                PlayerProperty.fromProtobuf(profile.getPropertiesList()),
                                profile.getHide()
                        ));
                    }

                    plugin.getTabList().updatePlayer(entries);
                });
            }
        }
    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        plugin.getLogger().log(Level.INFO, "P Subscribed : " + pattern);
    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        plugin.getLogger().log(Level.INFO, "P UN Subscribed : " + pattern);
    }
}
