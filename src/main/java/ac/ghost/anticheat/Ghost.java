package ac.ghost.anticheat;

import ac.ghost.anticheat.alert.AlertManager;
import ac.ghost.anticheat.config.Config;
import ac.ghost.anticheat.command.GhostCommand;
import ac.ghost.anticheat.config.ConfigLoader;
import ac.ghost.anticheat.listener.PacketListener;
import ac.ghost.anticheat.packets.ClientMovementPredictionSyncPacket;
import ac.ghost.anticheat.packets.ServerNetworkHandler;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.port.nukkit.NukkitPistonMovementAdapter;
import ac.ghost.anticheat.packets.other.NetworkLatencyPackets;
import ac.ghost.anticheat.packets.other.VehiclePackets;
import ac.ghost.anticheat.packets.player.*;
import ac.ghost.anticheat.packets.server.ServerChunkPackets;
import ac.ghost.anticheat.packets.server.ServerDataPackets;
import ac.ghost.anticheat.packets.server.ServerEntityPackets;
import ac.ghost.anticheat.protocol.BedrockProtocolCapabilities;
import ac.ghost.anticheat.player.manager.GhostPlayerManager;
import ac.ghost.mappings.BlockMappings;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.Player;
import cn.nukkit.network.protocol.ProtocolInfo;

import java.util.ArrayList;

public class Ghost {







    private final static Ghost instance = new Ghost();
    private static Config config;
    private static PluginBase pluginInstance;
    private Ghost() {}

    private GhostPlayerManager playerManager;
    private AlertManager alertManager;

    public void init(PluginBase instance) {
        pluginInstance = instance;
        config = ConfigLoader.load(instance);

        BlockMappings.load();

        
        
        for (final int protocol : ProtocolInfo.SUPPORTED_PROTOCOLS) {
            if (BedrockProtocolCapabilities.hasMovementPredictionSync(protocol)) {
                instance.getServer().getNetwork().registerPacketNew(protocol,
                        ProtocolInfo.CLIENT_MOVEMENT_PREDICTION_SYNC_PACKET,
                        ClientMovementPredictionSyncPacket.class);
            }
        }

        this.playerManager = new GhostPlayerManager();
        this.alertManager = new AlertManager();

        
        
        
        instance.getServer().callDataPkSendEv = true;

        PluginManager pm = instance.getServer().getPluginManager();
        pm.registerEvents(new NetworkLatencyPackets(), instance);
        pm.registerEvents(new ServerChunkPackets(), instance);
        pm.registerEvents(new ServerEntityPackets(), instance);
        pm.registerEvents(new ServerDataPackets(), instance);
        pm.registerEvents(new NukkitPistonMovementAdapter(), instance);
        pm.registerEvents(new PlayerEffectPackets(), instance);
        pm.registerEvents(new PlayerVelocityPackets(), instance);
        pm.registerEvents(new PlayerInventoryPackets(), instance);
        pm.registerEvents(new VehiclePackets(), instance);
        
        
        pm.registerEvents(new PacketListener(), instance);
        pm.registerEvents(new ServerNetworkHandler(), instance);

        for (Player onlinePlayer : instance.getServer().getOnlinePlayers().values()) {
            GhostPlayer ghostPlayer = this.playerManager.add(onlinePlayer);
            ghostPlayer.runtimeEntityId = onlinePlayer.getId();
        }

        instance.getServer().getScheduler().scheduleRepeatingTask(instance,
                () -> new ArrayList<>(this.playerManager.values()).forEach(player -> {
                    if (player.isClosed()) {
                        this.playerManager.remove(player.getSession());
                    } else {
                        player.latencyAdapter.serverTick();
                    }
                }), 1);

        instance.getServer().getCommandMap().register("ghost", new GhostCommand());
    }

    public void terminate(PluginBase instance) {
        if (this.playerManager != null) {
            this.playerManager.clear();
        }
    }


    public GhostPlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public AlertManager getAlertManager() {
        return this.alertManager;
    }

    public static Ghost getInstance() {
        return instance;
    }

    public static Config getConfig() {
        return config;
    }

    public static void setConfig(final Config config) {
        Ghost.config = config;
    }

    public static PluginBase getPluginInstance() {
        return pluginInstance;
    }

    public static void setPluginInstance(final PluginBase pluginInstance) {
        Ghost.pluginInstance = pluginInstance;
    }

}
