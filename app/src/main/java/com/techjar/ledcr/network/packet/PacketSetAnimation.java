
package com.techjar.ledcr.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Techjar
 */
public class PacketSetAnimation extends Packet {
    private String name;

    public PacketSetAnimation() {
    }

    public PacketSetAnimation(String name) {
        this.name = name;
    }

    @Override
    public void readData(DataInputStream stream) throws IOException {
        name = stream.readUTF();
    }

    @Override
    public void writeData(DataOutputStream stream) throws IOException {
        stream.writeUTF(name);
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return name;
    }
}
