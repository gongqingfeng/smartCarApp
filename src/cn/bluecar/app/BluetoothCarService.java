package cn.bluecar.app;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BluetoothCarService {
    private static final String TAG = "BluetoothCarService";


    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private  BluetoothAdapter mAdapter;
    private  Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
     int mState;

    public int getmState() {
		return mState;
	}

	public static final int STATE_NONE = 0;       // 无任何状态 

   public static final int STATE_LISTEN = 1;     // 等待连接

    public static final int STATE_CONNECTING = 2; // 初始化连接

    public static final int STATE_CONNECTED = 3;  // 已连接设备

    public BluetoothCarService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

//设置设备连接状态

    private synchronized void setState(int state) {
        mState = state;

        mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

     public synchronized void start() {

            setState(STATE_LISTEN);
    }

//连接设备函数：

    public synchronized void connect(BluetoothDevice device) {

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // 先取消掉当前的连接线程

        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // 根据给的MAC，开始一个连接线程 
        
       mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * 开始一个ConnectedThread，开始管理蓝牙连接
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {

        // 取消已完成连接的线程
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

       

        // 开启连接到服务器线程
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // 把MAC地址发回 UI Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * 停止所有线程

     */
    public synchronized void stop() {
             if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
       
        setState(STATE_NONE);
    }

//发送数据到蓝牙的函数：

    public void write(byte[] send) {
                ConnectedThread r;
            synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
           r.write(send);
    }

    /**
     * 连接失败时在UI上显示.
     */
    private void connectionFailed() {
        setState(STATE_LISTEN);

        // 失败信息发回Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "无法连接到设备，请确认下位机蓝牙功能是否正常");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * 丢失连接时在UI上显示.
     */
    private void connectionLost() {
        setState(STATE_LISTEN);

        // 失败信息发回Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "与目标设备连接丢失");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

//连接线程：

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            //通过蓝牙设备从蓝牙连接中获取一个socket
            
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
                setName("连接线程");

            // 当关闭连接后，设备可见性设为不可见
            mAdapter.cancelDiscovery();

            try {
                 mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // 关闭Socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
               BluetoothCarService.this.start();
                return;
            }

        synchronized (BluetoothCarService.this) {
                mConnectThread = null;
            }

            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * 该线程在取得连接后执行
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                   }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            //保持监听
            while (true) {
                try {
                    // 从InputStream读取
                    bytes = mmInStream.read(buffer);

                    // 把获取的信息回传给UI

                    mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] send) {
            try {
                mmOutStream.write(send);
                // 把发送的内容在UI上显示
                mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, send)
                        .sendToTarget();
            } catch (IOException e) {
               }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
               }
        }
    }

}
