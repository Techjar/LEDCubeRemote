package com.techjar.ledcr;

import android.content.SharedPreferences;
import android.content.Context;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.techjar.ledcr.network.TCPClient;
import com.techjar.ledcr.network.packet.Packet;
import com.techjar.ledcr.network.packet.PacketSetAnimation;
import com.techjar.ledcr.network.packet.PacketSetAnimationOption;
import com.techjar.ledcr.network.packet.PacketSetColorPicker;
import com.techjar.ledcr.util.AnimationOption;
import com.techjar.ledcr.util.Color;
import com.techjar.ledcr.util.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;


public class LEDCubeRemoteActivity extends ActionBarActivity {
    public static LEDCubeRemoteActivity instance;
    SharedPreferences sharedPref;
    SharedPreferences.Editor sharedPrefEditor;
    Map<String, AnimationOption> animOptionMap = new HashMap<>();
    Map<String, String> animOptionValueMap = new HashMap<>();
    int prevAnimOption;
    Spinner animSpinner;
    Spinner animOptionsSpinner;
    SeekBar seekBarRed;
    SeekBar seekBarGreen;
    SeekBar seekBarBlue;
    View colorRectangle;
    CheckBox optionCheckBox;
    Spinner optionSpinner;
    SeekBar optionSeekBar;
    EditText optionText;
    SeekBar optionSeekBar2;
    SeekBar optionSeekBar3;
    View optionColorRectangle;
    TextWatcher optionTextWatcher;
    TCPClient tcpClient;

    public LEDCubeRemoteActivity() {
        instance = this;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledcube_remote);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
        animSpinner = (Spinner)findViewById(R.id.anim_spinner);
        animOptionsSpinner = (Spinner)findViewById(R.id.options_spinner);
        colorRectangle = findViewById(R.id.color_rectangle);
        final EditText serverAddressText = (EditText)findViewById(R.id.server_address_text);
        serverAddressText.setText(sharedPref.getString("server_address", ":7545"));
        final Button connectButton = (Button)findViewById(R.id.connect_button);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = serverAddressText.getText().toString();
                sharedPrefEditor.putString("server_address", text);
                sharedPrefEditor.commit();
                if (tcpClient == null || tcpClient.isClosed()) {
                    if (connectToServer(text)) {
                        connectButton.setText("Disconnect");
                    }
                } else {
                    try {
                        tcpClient.close();
                        connectButton.setText("Connect");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        animSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item = animSpinner.getItemAtPosition(position);
                if (item != null && tcpClient != null && !tcpClient.isClosed()) {
                    tcpClient.queuePacket(new PacketSetAnimation(item.toString()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        animOptionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item = animOptionsSpinner.getItemAtPosition(position);
                if (item != null) {
                    final AnimationOption option = animOptionMap.get(item.toString());
                    final String optionValue = animOptionValueMap.get(item.toString());
                    if (option != null) {
                        switch (option.getType()) {
                            case TEXT:
                            case SPINNER:
                                hideOptionComponents();
                                if (optionTextWatcher != null) optionText.removeTextChangedListener(optionTextWatcher);
                                if (option.getType() == AnimationOption.OptionType.SPINNER) {
                                    optionText.setInputType(InputType.TYPE_CLASS_NUMBER | (Integer.parseInt(option.params[4].toString()) > 0 ? InputType.TYPE_NUMBER_FLAG_DECIMAL : 0) | (Integer.parseInt(option.params[1].toString()) < 0 ? InputType.TYPE_NUMBER_FLAG_SIGNED : 0));
                                    optionText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                        @Override
                                        public void onFocusChange(View v, boolean hasFocus) {
                                            if (!hasFocus) {
                                                try {
                                                    float num = Float.parseFloat(optionText.getText().toString());
                                                    optionText.setText(Float.toString(Math.max(Math.min(num, Float.parseFloat(option.params[2].toString())), Float.parseFloat(option.params[1].toString()))));
                                                } catch (NumberFormatException ex) {
                                                }
                                            }
                                        }
                                    });
                                } else {
                                    optionText.setInputType(0);
                                    optionText.setOnFocusChangeListener(null);
                                }
                                optionText.setText(optionValue);
                                optionTextWatcher = new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {
                                        String text = optionText.getText().toString();
                                        try {
                                            if (option.getType() == AnimationOption.OptionType.SPINNER) Float.parseFloat(text);
                                            fireOptionChange(option.getId(), text);
                                            animOptionValueMap.put(option.name, text);
                                        } catch (NumberFormatException ex) {
                                        }
                                    }
                                };
                                optionText.addTextChangedListener(optionTextWatcher);
                                optionText.setVisibility(View.VISIBLE);
                                break;
                            case SLIDER:
                                hideOptionComponents();
                                optionSeekBar.setOnSeekBarChangeListener(null);
                                if (option.params.length >= 2) {
                                    float increment = Float.parseFloat(option.params[1].toString());
                                    optionSeekBar.setMax(increment > 0 ? Math.round(1 / increment) : 2000);
                                } else {
                                    optionSeekBar.setMax(2000);
                                }
                                optionSeekBar.setProgress(Math.round(optionSeekBar.getMax() * Float.parseFloat(optionValue)));
                                optionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                        String value = Float.toString(progress / (float)optionSeekBar.getMax());
                                        fireOptionChange(option.getId(), value);
                                        System.out.println(option.getId());
                                        animOptionValueMap.put(option.name, value);
                                    }

                                    @Override
                                    public void onStartTrackingTouch(SeekBar seekBar) {
                                    }

                                    @Override
                                    public void onStopTrackingTouch(SeekBar seekBar) {
                                    }
                                });
                                optionSeekBar.setVisibility(View.VISIBLE);
                                break;
                            case COMBOBOX:
                            case COMBOBUTTON:
                            case RADIOGROUP:
                                hideOptionComponents();
                                int selection = 0;
                                String[] items = new String[(option.params.length - 1) / 2];
                                for (int i = 0; i < items.length; i++) {
                                    items[i] = option.params[i * 2 + 2].toString();
                                    if (option.params[i * 2 + 2].equals(optionValue)) {
                                        selection = i;
                                    }
                                }
                                optionSpinner.setOnItemSelectedListener(null);
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(LEDCubeRemoteActivity.this, android.R.layout.simple_spinner_item, items);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                optionSpinner.setAdapter(adapter);
                                optionSpinner.setSelection(selection);
                                optionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        String selected = optionSpinner.getItemAtPosition(position).toString();
                                        fireOptionChange(option.getId(), selected);
                                        animOptionValueMap.put(option.name, selected);
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                    }
                                });
                                optionSpinner.setVisibility(View.VISIBLE);
                                break;
                            case CHECKBOX:
                                hideOptionComponents();
                                optionCheckBox.setOnCheckedChangeListener(null);
                                optionCheckBox.setChecked(Boolean.parseBoolean(optionValue));
                                optionCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        fireOptionChange(option.getId(), Boolean.toString(isChecked));
                                        animOptionValueMap.put(option.name, Boolean.toString(isChecked));
                                    }
                                });
                                optionCheckBox.setVisibility(View.VISIBLE);
                                break;
                            case BUTTON:
                                fireOptionChange(option.getId(), "");
                                if (animOptionMap.get(animOptionsSpinner.getItemAtPosition(prevAnimOption).toString()).getType() != AnimationOption.OptionType.BUTTON) {
                                    animOptionsSpinner.setSelection(prevAnimOption);
                                }
                                break;
                            case COLORPICKER:
                                hideOptionComponents();
                                SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                        int red = optionSeekBar.getProgress();
                                        int green = optionSeekBar2.getProgress();
                                        int blue = optionSeekBar3.getProgress();
                                        optionColorRectangle.setBackgroundColor(0xFF000000 | (red << 16) | (green << 8) | blue);
                                        fireOptionChange(option.getId(), Util.colorToString(new Color(red, green, blue), false));
                                    }

                                    @Override
                                    public void onStartTrackingTouch(SeekBar seekBar) {
                                    }

                                    @Override
                                    public void onStopTrackingTouch(SeekBar seekBar) {
                                    }
                                };
                                Color color = Util.stringToColor(optionValue);
                                optionSeekBar.setOnSeekBarChangeListener(seekBarListener);
                                optionSeekBar.setMax(255);
                                optionSeekBar.setProgress(color.getRed());
                                optionSeekBar.setVisibility(View.VISIBLE);
                                optionSeekBar2.setOnSeekBarChangeListener(seekBarListener);
                                optionSeekBar2.setMax(255);
                                optionSeekBar2.setProgress(color.getGreen());
                                optionSeekBar2.setVisibility(View.VISIBLE);
                                optionSeekBar3.setOnSeekBarChangeListener(seekBarListener);
                                optionSeekBar3.setMax(255);
                                optionSeekBar3.setProgress(color.getBlue());
                                optionSeekBar3.setVisibility(View.VISIBLE);
                                optionColorRectangle.setVisibility(View.VISIBLE);
                                break;
                        }
                        prevAnimOption = position;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int red = seekBarRed.getProgress();
                int green = seekBarGreen.getProgress();
                int blue = seekBarBlue.getProgress();
                colorRectangle.setBackgroundColor(0xFF000000 | (red << 16) | (green << 8) | blue);
                if (tcpClient != null && !tcpClient.isClosed()) {
                    tcpClient.queuePacket(new PacketSetColorPicker(red / 255F, green / 255F, blue / 255F));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };

        seekBarRed = (SeekBar)findViewById(R.id.seekbar_red);
        seekBarRed.setMax(255);
        seekBarRed.setOnSeekBarChangeListener(seekBarListener);
        seekBarGreen = (SeekBar)findViewById(R.id.seekbar_green);
        seekBarGreen.setMax(255);
        seekBarGreen.setOnSeekBarChangeListener(seekBarListener);
        seekBarBlue = (SeekBar)findViewById(R.id.seekbar_blue);
        seekBarBlue.setMax(255);
        seekBarBlue.setOnSeekBarChangeListener(seekBarListener);

        optionCheckBox = (CheckBox)findViewById(R.id.animoption_checkbox);
        optionSpinner = (Spinner)findViewById(R.id.animoption_spinner);
        optionSeekBar = (SeekBar)findViewById(R.id.animoption_seekbar);
        optionText = (EditText)findViewById(R.id.animoption_text);
        optionSeekBar2 = (SeekBar)findViewById(R.id.animoption_seekbar2);
        optionSeekBar3 = (SeekBar)findViewById(R.id.animoption_seekbar3);
        optionColorRectangle = findViewById(R.id.animoption_colorrectangle);

        Thread closeWatcher = new Thread("TCPClient Close Watcher") {
            @Override
            public void run() {
                boolean lastState = false;
                while (true) {
                    if (tcpClient != null && tcpClient.isClosed() != lastState) {
                        lastState = tcpClient.isClosed();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Button connectButton = (Button)findViewById(R.id.connect_button);
                                connectButton.setText(tcpClient.isClosed() ? "Connect" : "Disconnect");
                            }
                        });
                    }
                    try { Thread.sleep(1000); }
                    catch (InterruptedException ex) {}
                }
            }
        };
        closeWatcher.setDaemon(true);
        closeWatcher.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ledcube_remote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setAnimSpinnerItems(String[] items, String current) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        animSpinner.setAdapter(adapter);
        animSpinner.setSelection(adapter.getPosition(current));
    }

    public void setPickerColor(float red, float green, float blue) {
        seekBarRed.setProgress(Math.round(red * 255));
        seekBarGreen.setProgress(Math.round(green * 255));
        seekBarBlue.setProgress(Math.round(blue * 255));
    }

    public void loadAnimationOptions(AnimationOption[] options, String[] values) {
        animOptionMap.clear();
        if (options.length > 0) {
            String[] optionNames = new String[options.length];
            for (int i = 0; i < options.length; i++) {
                AnimationOption option = options[i];
                String name = option.getName() + (option.getType() == AnimationOption.OptionType.BUTTON ? " | " + option.getParams()[0].toString() : "");
                optionNames[i] = name;
                animOptionMap.put(name, option);
                animOptionValueMap.put(name, values[i]);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, optionNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            animOptionsSpinner.setAdapter(adapter);
            for (int i = 0; i < options.length; i++) {
                if (options[i].getType() != AnimationOption.OptionType.BUTTON) {
                    animOptionsSpinner.setSelection(i);
                    break;
                }
            }
            animOptionsSpinner.setVisibility(View.VISIBLE);
        } else {
            animOptionsSpinner.setVisibility(View.INVISIBLE);
            hideOptionComponents();
        }
    }

    private void hideOptionComponents() {
        optionCheckBox.setVisibility(View.INVISIBLE);
        optionSpinner.setVisibility(View.INVISIBLE);
        optionSeekBar.setVisibility(View.INVISIBLE);
        optionText.setVisibility(View.INVISIBLE);
        optionSeekBar2.setVisibility(View.INVISIBLE);
        optionSeekBar3.setVisibility(View.INVISIBLE);
        optionColorRectangle.setVisibility(View.INVISIBLE);
    }

    private void fireOptionChange(String id, String value) {
        if (tcpClient != null && !tcpClient.isClosed()) {
            tcpClient.queuePacket(new PacketSetAnimationOption(id, value));
        }
    }

    private boolean connectToServer(String address) {
        try {
            Util.IPInfo info = Util.parseIPAddress(address);
            tcpClient = new TCPClient(info.getAddress(), info.getPort() == -1 ? 7546 : info.getPort());
            new Thread("Packet Processing Thread") {
                @Override
                public void run() {
                    Packet packet;
                    Queue<Packet> queue = tcpClient.getReceiveQueue();
                    while (!tcpClient.isClosed()) {
                        while ((packet = queue.poll()) != null) {
                            final Packet thePacket = packet;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    thePacket.process();
                                }
                            });
                        }
                        try { Thread.sleep(10); }
                        catch (InterruptedException ex) {} // I hate checked exceptions
                    }
                }
            }.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
