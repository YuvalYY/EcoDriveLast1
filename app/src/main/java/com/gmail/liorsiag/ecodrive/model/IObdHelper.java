package com.gmail.liorsiag.ecodrive.model;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.MassAirFlowCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand;
import com.github.pires.obd.commands.pressure.IntakeManifoldPressureCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AirIntakeTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Set;

public class IObdHelper implements ObdHelper {

    private boolean mIsConnected = false;
    private String obdType;

    private Thread mThread;
    private DataManager mDataManager;
    private volatile boolean mStop;
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mSocket;
    private String mObdType;
    private LinkedList<ObdCommand> mObdCommands;

    public IObdHelper(Context c, String obdType) {
        mObdType = obdType;
        mContext = c;
        mDataManager = DataManager.instance();
    }

    @Override
    public boolean isConnected() {
        return mIsConnected;
    }

    @Override
    public boolean connect() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (enableBT()) {
            closeSocket();
            if (getOBDSocket()) {
                resetOBD();
                if (mObdType == null) {
                    mObdType = testObdType();
                    if (mObdType != null)
                        mDataManager.saveObdType(mObdType);
                }
                if (mObdType == null)
                    closeSocket();
                else {
                    mIsConnected = true;
                    startRecording();
                }
            }
        }
        return mIsConnected;
    }

    @Override
    public void disconnect() {
        closeSocket();
        stopRecording();
        mIsConnected = false;
    }

    @Override
    public void startRecording() {
        mStop = false;
        Toast.makeText(mContext, mDataManager.getObdType(), Toast.LENGTH_SHORT).show();
        if (mDataManager.getObdType() != null)
            mObdType = mDataManager.getObdType();
        setCommands();
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mStop) {
                    try {
                        for (ObdCommand command : mObdCommands) {
                            command.run(mSocket.getInputStream(), mSocket.getOutputStream()); //consider getting the streams outisde of the while
                            mDataManager.updateObd(new String[]{String.valueOf(System.currentTimeMillis()), command.getName(), command.getCalculatedResult()});
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        });
        mThread.start();
    }

    @Override
    public void stopRecording() {
        mStop = true;
        mObdCommands = null;
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
    }

    @Override
    public String testObdType() {
        OutputStream out = null;
        InputStream in = null;
        try {
            out = mSocket.getOutputStream();
            in = mSocket.getInputStream();
        } catch (Exception ignored) {
        }

        if (in != null && out != null) {
            try {
                new ConsumptionRateCommand().run(in, out);
                mObdType = "FUEL";
                return mObdType;
            } catch (Exception ignored) {
            }
            try {
                new MassAirFlowCommand().run(in, out);
                mObdType = "MAF";
                return mObdType;
            } catch (Exception ignored) {
            }
            mObdType = "RPM";
            return mObdType;
        }
        Toast.makeText(mContext, "Try to test the obd type again", Toast.LENGTH_SHORT).show();
        mObdType = null;
        return null;
    }

    private boolean enableBT() {
        if (mBluetoothAdapter == null) {
            return false;
        }
        if (!mBluetoothAdapter.isEnabled())
            Toast.makeText(mContext, "Enable Bluetooth", Toast.LENGTH_SHORT).show();
        return true;
    }

    private void closeSocket() {
        if (mSocket != null)
            try {
                mSocket.close();
            } catch (IOException ignored) {
            }
        mSocket = null;
    }

    private boolean getOBDSocket() {
        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice d : pairedDevices) {
                String deviceName = d.getName();
                if (deviceName != null && (deviceName.contains("obd") || deviceName.contains("OBD") || deviceName.toUpperCase().contains("V-LINK"))) {
                    connectOBD(d);
                }
            }
        }
        if (mSocket == null)
            return false;
        if (mSocket.isConnected())
            Toast.makeText(mContext, "Connection success", Toast.LENGTH_SHORT).show();
        return mSocket.isConnected();
    }

    private void connectOBD(BluetoothDevice device) {
        try {
            mSocket = (BluetoothSocket) device.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class}).invoke(device, 1);
        } catch (Exception e) {
            Toast.makeText(mContext, "Connection failed", Toast.LENGTH_SHORT).show();
            closeSocket();
        }
        if (mSocket != null) {
            try {
                mBluetoothAdapter.cancelDiscovery();
                Thread.sleep(500);
                //first ask the socket from the device
                mSocket.connect();
            } catch (Exception e) {
                Toast.makeText(mContext, "Connection failed", Toast.LENGTH_SHORT).show();
                closeSocket();
            }
        }
    }

    private void resetOBD() {
        try {
            new EchoOffCommand().run(mSocket.getInputStream(), mSocket.getOutputStream());
            new LineFeedOffCommand().run(mSocket.getInputStream(), mSocket.getOutputStream());
            new TimeoutCommand(100).run(mSocket.getInputStream(), mSocket.getOutputStream());
            new SelectProtocolCommand(ObdProtocols.AUTO).run(mSocket.getInputStream(), mSocket.getOutputStream());
        } catch (Exception ignored) {
        }
    }

    private void setCommands() {
        mObdCommands = new LinkedList<>();
        mObdCommands.add(new SpeedCommand());
        if (mObdType.equals("FUEL"))
            mObdCommands.add(new ConsumptionRateCommand());
        else if (mObdType.equals("MAF"))
            mObdCommands.add(new MassAirFlowCommand());
        else {
            mObdCommands.add(new RPMCommand());
            mObdCommands.add(new IntakeManifoldPressureCommand());
            mObdCommands.add(new AirIntakeTemperatureCommand());
        }
    }
}
