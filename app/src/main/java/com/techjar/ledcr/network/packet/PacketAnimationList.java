
package com.techjar.ledcr.network.packet;

import android.widget.ArrayAdapter;

import com.techjar.ledcr.LEDCubeRemoteActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Techjar
 */
public class PacketAnimationList extends Packet {
    private String[] names;
    private String current;

    public PacketAnimationList() {
    }

    public PacketAnimationList(String[] names, String current) {
        this.names = names;
        this.current = current;
    }

    @Override
    public void readData(DataInputStream stream) throws IOException {
        names = new String[stream.readInt()];
        for (int i = 0; i < names.length; i++) {
            names[i] = stream.readUTF();
        }
        current = stream.readUTF();
    }

    @Override
    public void writeData(DataOutputStream stream) throws IOException {
        stream.writeInt(names.length);
        for (String name : names) {
            stream.writeUTF(name);
        }
        stream.writeUTF(current);
    }

    @Override
    public int getRequiredCapabilities() {
        return Capabilities.CONTROL_DATA;
    }

    @Override
    public void process() {
        LEDCubeRemoteActivity.instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LEDCubeRemoteActivity.instance.setAnimSpinnerItems(names, current);
            }
        });
    }

    public String[] getNames() {
        return names;
    }

    public String getCurrent() {
        return current;
    }
}
