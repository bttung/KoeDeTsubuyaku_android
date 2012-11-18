package jp.zanmai.TestTwitter;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class DeviceListActivity extends Activity {
	
	/**   インテントのコード定義   */
	public static final String EXTRA_DEVICE_ADDRESS = "device_address";
	
	/**   デバッグ関連   */
	private static final String LOGGER_TAG				= "NEURO_SKY";
	private static final boolean LOGGER_ENABLE		= false;
	
	/**   定数   */
	private static final int DEVICE_ADDRESS_LENGTH = 17;
	
	/**   メンバ変数   */
	private BluetoothAdapter btAdapter;
	private ArrayAdapter<String> pairedDeviceArrayAdapter;
	private ArrayAdapter<String> newDeviceArrayAdapter;
	
	/**   メンバ変数（匿名クラス実装）   */
	private OnItemClickListener deviceClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(
				android.widget.AdapterView<?> adapterView, View view, int arg0, long arg1) {
			btAdapter.cancelDiscovery();
			String deviceInfo = ((TextView) view).getText().toString();
			String deviceAddress = deviceInfo.substring(deviceInfo.length() - DEVICE_ADDRESS_LENGTH);
			
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, deviceAddress);
			setResult(Activity.RESULT_OK, intent);
			finish();
		};
	};
	
	/**   メンバ変数（匿名クラス実装）   */
	private BroadcastReceiver bcReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				//   デバイスがボンディングされていない場合は新規デバイスリストへ追加
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					newDeviceArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				}
			} else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.select_device);
				if (newDeviceArrayAdapter.getCount() == 0) {
					String noDevice = getResources().getText(R.string.none_found).toString();
					newDeviceArrayAdapter.add(noDevice);
				}
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (LOGGER_ENABLE) Log.d(LOGGER_TAG, "_/_/_/_/_/   ON-CREATE   _/_/_/_/_/");
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);
		setResult(Activity.RESULT_CANCELED);
		
		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				discovery();
				v.setVisibility(View.GONE);
			}
		});
		//   接続済みデバイス
		pairedDeviceArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(pairedDeviceArrayAdapter);
		pairedListView.setOnItemClickListener(deviceClickListener);
		//   未接続デバイス
		newDeviceArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		ListView newDeviceListView = (ListView) findViewById(R.id.new_devices);
		newDeviceListView.setAdapter(newDeviceArrayAdapter);
		newDeviceListView.setOnItemClickListener(deviceClickListener);
		//   レシーバ登録
		IntentFilter actionFoundFilter= new IntentFilter(BluetoothDevice.ACTION_FOUND);
		IntentFilter discoveryFinishedFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(bcReceiver, actionFoundFilter);
		registerReceiver(bcReceiver, discoveryFinishedFilter);
		
		//   接続済みデバイスを取得
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
            	pairedDeviceArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDeviceArrayAdapter.add(noDevices);
        }
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (pairedDeviceArrayAdapter != null) pairedDeviceArrayAdapter.clear();
		if (newDeviceArrayAdapter != null) newDeviceArrayAdapter.clear();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (LOGGER_ENABLE) Log.d(LOGGER_TAG, "_/_/_/_/_/   ON-DESTROY   _/_/_/_/_/");
		
		if (btAdapter != null) btAdapter.cancelDiscovery();
		unregisterReceiver(bcReceiver);
	}
	
	/**
	 *  デバイスディスカバリ
	 */
	private void discovery() {
		if (LOGGER_ENABLE) Log.d(LOGGER_TAG, "discovery");
		
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);
		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
		
		//   既にディスカバリ中の場合は停止＆再開
		if (btAdapter.isDiscovering()) {
			btAdapter.cancelDiscovery();
		}
		btAdapter.startDiscovery();
	}
}
