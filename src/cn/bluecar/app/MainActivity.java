package cn.bluecar.app;

import java.io.IOException;



import java.util.UUID;


import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;
	// 从 BluetoothChatService 处理程序发送的消息类型
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final String PREFS_NAME = "BluetoothCar";
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	 TextView mTitle;
	TextView DirResult;
	TextView textViewD;
	// Intent request codes意向请求代码
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	// 连接的设备名称
	private String mConnectedDeviceName = null;
	// 对话线程数组适配器

	// 发送服务
	private BluetoothCarService mCarService = null;
	// 本地蓝牙适配器
	final BluetoothAdapter bluetoothAdapter = BluetoothAdapter
			.getDefaultAdapter();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.newn);
	
	       this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	    WindowManager.LayoutParams.FLAG_FULLSCREEN);//实现全屏
	       
		if (bluetoothAdapter== null) {
			
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("No bluetooth devices");
			dialog.setMessage("Your equipment does not support bluetooth, please change device");

			dialog.setNegativeButton("cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});
			dialog.show();
		}
		DirResult = (TextView) findViewById(R.id.textView11);
		mTitle= (TextView) findViewById(R.id.textView10);
		textViewD= (TextView) findViewById(R.id.textView12);
		Button button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!bluetoothAdapter.isEnabled()) {
					Intent enableIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
					// 否则，读取指令

				} else {
					if (mCarService == null)
						setupControl();
				}
			}
		});
		Button button2 = (Button) findViewById(R.id.button2);
		button2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (bluetoothAdapter.isEnabled()) {
					bluetoothAdapter.disable();
				}
			}
		});
	}

	// 以下代码为设置按钮监听事件，响应按钮按下后发送消息

	private void setupControl() {

		// 初始化一个侦听程序，单击事件的发送按钮
		Button button3 = (Button) findViewById(R.id.button3);

		button3.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					DirResult.setText("前进");
					sendMessage("A");
					break;
				case MotionEvent.ACTION_UP:

					sendMessage("C");
					break;
				}
				return false;
			}

		});

		Button button6 = (Button) findViewById(R.id.button6);
		button6.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					DirResult.setText("左转");
					sendMessage("B");
					break;
				case MotionEvent.ACTION_UP:
					sendMessage("C");
					break;
				}
				return false;
			}

		});
		
		Button button7 = (Button) findViewById(R.id.button7);
		button7.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					DirResult.setText("右转");
					sendMessage("D");
					break;
				case MotionEvent.ACTION_UP:
					sendMessage("C");
					break;
				}
				return false;
			}

		});
		Button button10 = (Button) findViewById(R.id.button10);
		button10.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					DirResult.setText("后退");
					sendMessage("E");
					break;
				case MotionEvent.ACTION_UP:
					sendMessage("C");
					break;
				}
				return false;
			}

		});
		Button button4 = (Button) findViewById(R.id.button4);
    button4.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			textViewD.setText("1");
			sendMessage("F");//1档
		}
	});
    Button button5 = (Button) findViewById(R.id.button5);
    button5.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			textViewD.setText("2");
			sendMessage("G");//2档
		}
	});
    Button button8 = (Button) findViewById(R.id.button8);
    button8.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			textViewD.setText("3");
			sendMessage("H");//3档
		}
	});
    Button button9 = (Button) findViewById(R.id.button9);
    button9.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			textViewD.setText("4");
			sendMessage("I");//4档
		}
	});
		// 初始化执行蓝牙连接 BluetoothCarService
		mCarService = new BluetoothCarService(this, mHandler);
	}

	// 是从该 重新获取信息显示在UI的处理程序

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothCarService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					// mConversationArrayAdapter.clear();
					break;
				case BluetoothCarService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothCarService.STATE_LISTEN:
				case BluetoothCarService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					BluetoothAdapter cwjBluetoothAdapter = BluetoothAdapter
							.getDefaultAdapter();

					if (cwjBluetoothAdapter == null) {
						Toast.makeText(MainActivity.this, "本机没有找到蓝牙硬件或驱动存在问题",
								Toast.LENGTH_SHORT).show();
					}
					if (!cwjBluetoothAdapter.isEnabled()) {

						Intent TurnOnBtIntent = new Intent(
								BluetoothAdapter.ACTION_REQUEST_ENABLE);

						startActivityForResult(TurnOnBtIntent,
								REQUEST_ENABLE_BT);

					}

					break;
				}
				break;
			case MESSAGE_WRITE:

				break;
			case MESSAGE_READ:

				break;
			case MESSAGE_DEVICE_NAME:
				// 保存连接设备名称
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"已成功连接到： " + mConnectedDeviceName + "可以开始操纵小车了！",
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:  
         if (resultCode == Activity.RESULT_OK) {
                // 获取设备地址
        	
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                Toast.makeText(this, "该设备MAC地址为--->"+address, Toast.LENGTH_SHORT).show();
                //获取设备对象
               
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
                try {
               device.createRfcommSocketToServiceRecord(uuid);
    
    } catch (IOException e1) {
     // TODO Auto-generated catch block
     e1.printStackTrace();
    }
                // Attempt to connect to the device
                mCarService.connect(device);   //调用BluetoothCarService类的方法进行连接设备
             
               
            }

            break;
        case REQUEST_ENABLE_BT:
            //当启用蓝牙的请求返回时
        	
            if (resultCode == Activity.RESULT_OK) {
                // 蓝牙现已启用，读取预设指令值
             setupControl();
            } else {
                // 用户没有启用蓝牙或发生错误
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

	/**
	 * 发送数据
	 * 
	 * @param i
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {
		// 检查是否真正连上
		if (mCarService.getmState() != BluetoothCarService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		// 检查有什么实际发送
		if (message.length() > 0) {
			// 获取消息字节并写入 BluetoothCarService
			byte[] send = message.getBytes();
			mCarService.write(send);
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.str:
			Intent intent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
			break;

		case R.id.str2:
			AlertDialog.Builder dlg = new AlertDialog.Builder(this);
			final EditText editText = new EditText(this);
			dlg.setView(editText);
			dlg.setTitle("请输入用户名");
			dlg.setPositiveButton("设置", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if (editText.getText().toString().length() != 0)
						bluetoothAdapter.setName(editText.getText().toString());

				}
			});
			dlg.create();
			dlg.show();
			break;
		case R.id.str3:
					Intent intent1 = new Intent(
							BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
					intent1.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
							300);
					startActivity(intent1);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	 
	 
}
