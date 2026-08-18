package ac.ghost.anticheat.packets;

import ac.ghost.anticheat.protocol.BedrockProtocolCapabilities;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;

import java.util.BitSet;





public final class ClientMovementPredictionSyncPacket extends DataPacket {
    public static final int NETWORK_ID = ProtocolInfo.CLIENT_MOVEMENT_PREDICTION_SYNC_PACKET;
    private final BitSet actorFlags = new BitSet();

    public float boundingBoxScale;
    public float boundingBoxWidth;
    public float boundingBoxHeight;
    public float speed;
    public float underwaterSpeed;
    public float lavaSpeed;
    public float jumpStrength;
    public float health;
    public float hunger;
    public float unknown1;
    public float unknown2;
    public float unknown3;
    public long runtimeEntityId;
    public boolean flying;

    @Override
    public int packetId() {
        return NETWORK_ID;
    }

    @Override
    public byte pid() {
        throw new UnsupportedOperationException("Packet uses an extended network ID");
    }

    @Override
    public void decode() {
        
        
        
        this.actorFlags.clear();

        
        
        
        int encodedByteIndex = 0;
        int value;
        do {
            value = this.getByte() & 0xff;
            for (int bit = 0; bit < 7; bit++) {
                if ((value & (1 << bit)) != 0) {
                    this.actorFlags.set(encodedByteIndex * 7 + bit);
                }
            }
            encodedByteIndex++;
            if (encodedByteIndex > 32) {
                throw new IllegalArgumentException("Actor flags VarInt is too large");
            }
        } while ((value & 0x80) != 0);

        this.boundingBoxScale = this.getLFloat();
        this.boundingBoxWidth = this.getLFloat();
        this.boundingBoxHeight = this.getLFloat();
        this.speed = this.getLFloat();
        this.underwaterSpeed = this.getLFloat();
        this.lavaSpeed = this.getLFloat();
        this.jumpStrength = this.getLFloat();
        this.health = this.getLFloat();
        this.hunger = this.getLFloat();
        if (BedrockProtocolCapabilities.movementPredictionSyncHasExtraScalars(
                this.protocol)) {
            this.unknown1 = this.getLFloat();
            this.unknown2 = this.getLFloat();
            this.unknown3 = this.getLFloat();
        }
        this.runtimeEntityId = this.getUnsignedVarLong();
        if (BedrockProtocolCapabilities.movementPredictionSyncHasFlying(
                this.protocol)) {
            this.flying = this.getBoolean();
        }
    }

    @Override
    public void encode() {
        this.encodeUnsupported();
    }

    public boolean hasFlag(final int flag) {
        return this.actorFlags.get(flag);
    }
}
