
package com.techjar.ledcr.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Techjar
 */
public class PacketSetAnimationOption extends Packet {
    private String optionId;
    private String value;

    public PacketSetAnimationOption() {
    }

    public PacketSetAnimationOption(String optionId, String value) {
        this.optionId = optionId;
        this.value = value;
    }

    @Override
    public void readData(DataInputStream stream) throws IOException {
        optionId = stream.readUTF();
        value = stream.readUTF();
    }

    @Override
    public void writeData(DataOutputStream stream) throws IOException {
        stream.writeUTF(optionId);
        stream.writeUTF(value);
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException();
    }

    public String getOptionId() {
        return optionId;
    }

    public String getValue() {
        return value;
    }
}
