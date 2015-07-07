
package com.techjar.ledcr.network.packet;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Techjar
 */
public abstract class Packet {
    public enum ID {
        CUBE_FRAME,
        AUDIO_DATA,
        AUDIO_INIT,
        VISUAL_FRAME,
        SET_ANIMATION,
        ANIMATION_LIST,
        SET_COLOR_PICKER,
        ANIMATION_OPTION_LIST,
        SET_ANIMATION_OPTION;
    }

    public static final BiMap<ID, Class<? extends Packet>> packetMap = HashBiMap.create();
    static {
        packetMap.put(ID.CUBE_FRAME, PacketCubeFrame.class);
        //packetMap.put(ID.AUDIO_DATA, PacketAudioData.class); // Not implemented
        //packetMap.put(ID.AUDIO_INIT, PacketAudioInit.class); // Not implemented
        packetMap.put(ID.VISUAL_FRAME, PacketVisualFrame.class);
        packetMap.put(ID.SET_ANIMATION, PacketSetAnimation.class);
        packetMap.put(ID.ANIMATION_LIST, PacketAnimationList.class);
        packetMap.put(ID.SET_COLOR_PICKER, PacketSetColorPicker.class);
        packetMap.put(ID.ANIMATION_OPTION_LIST, PacketAnimationOptionList.class);
        packetMap.put(ID.SET_ANIMATION_OPTION, PacketSetAnimationOption.class);
    }

    public abstract void readData(DataInputStream stream) throws IOException;
    public abstract void writeData(DataOutputStream stream) throws IOException;
    public abstract void process();

    public ID getId() {
        return packetMap.inverse().get(this.getClass());
    }

    public static Packet readPacket(DataInputStream stream) throws IOException, InstantiationException, IllegalAccessException {
        int id = stream.readUnsignedByte();
        if (id > ID.values().length) throw new IOException(new StringBuilder("Unknown packet ID: ").append(id).toString());
        Class<? extends Packet> cls = packetMap.get(ID.values()[id]);
        Packet packet = cls.newInstance();
        packet.readData(stream);
        return packet;
    }

    public static void writePacket(DataOutputStream stream, Packet packet) throws IOException {
        stream.write(packet.getId().ordinal());
        packet.writeData(stream);
    }
}
