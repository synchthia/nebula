package net.synchthia.nebula.bukkit.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.synchthia.nebula.api.NebulaProtos;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerMessage {
    public static List<TagResolver> getServerEntryResolver(NebulaProtos.ServerEntry server) {
        return new ArrayList<>(Arrays.asList(
                // name
                Placeholder.unparsed("server_id", server.getName()),

                // display_name
                Placeholder.parsed("server_name", server.getDisplayName()),

                // address
                Placeholder.unparsed("server_address", server.getAddress()),

                // port
                Placeholder.unparsed("server_port", String.format("%d", server.getPort())),

                //
                // Status
                //

                // players
                Placeholder.unparsed("server_online_players", String.format("%d", server.getStatus().getPlayers().getOnline())),

                // max players
                Placeholder.unparsed("server_max_players", String.format("%d", server.getStatus().getPlayers().getMax())),

                // motd (full length)
                Placeholder.parsed("server_motd", server.getStatus().getDescription()),

                // motd (compacted for sign)
                Placeholder.parsed("server_motd_compact",
                        server.getStatus().getDescription().length() >= 15 ?
                                server.getStatus().getDescription().substring(0, 14) :
                                server.getStatus().getDescription()
                ),

                // Fallback
                Placeholder.component("server_fallback", Message.showStatus(server.getFallback())),

                // Lockdown
                Placeholder.component("server_lockdown", Message.showStatus(server.getLockdown().getEnabled()))
        ));
    }

    public static Component getServerEntryLore(CommandSender sender, NebulaProtos.ServerEntry server) {
        String titleFmt = "<!italic><gold><b><server_name></b></gold><gray> - <server_motd></gray></!italic>";
        String separator = "<gray><st>──────────────────────</st></gray>";
        String statusFmt;
        if (server.getStatus().getOnline()) {
            if (server.getStatus().getPlayers().getMax() == 0) {
                statusFmt = "<dark_gray><b>Starting...</b></dark_gray>";
            } else {
                statusFmt = "<dark_aqua>Online:</dark_aqua> <gray><server_online_players>/<server_max_players></gray>";
            }
        } else {
            statusFmt = "<red><b>Offline</b></red>";
        }

        String settingsFmt = "";
        if (sender.hasPermission("nebula.admin")) {
            settingsFmt = Message.multilineFormat(new String[]{
                    "<dark_gray>Fallback: <server_fallback></dark_gray>",
                    "<dark_gray>Lockdown: <server_lockdown></dark_gray>"
            });
        }

        return Message.create(Message.multilineFormat(new String[]{titleFmt, separator, statusFmt, settingsFmt}), TagResolver.resolver(getServerEntryResolver(server)));
    }
}
