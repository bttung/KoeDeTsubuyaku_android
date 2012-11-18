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
	
	/**   �C���e���g�̃R�[�h��`   */
	public static final String EXTRA_DEVICE_ADDRESS = "device_address";
	
	/**   �f�o�b�O�֘A   */
	private static final String LOGGER_TAG				= "NEURO_SKY";
	private static final boolean LOGGER_ENABLE		= false;
	
	/**   �萔   */
	private static final int DEVICE_ADDRESS_LENGTH = 17;
	
	/**   �����o�ϐ�   */
	private BluetoothAdapter btAdapter;
	private ArrayAdapter<String> pairedDeviceArrayAdapter;
	private ArrayAdapter<String> newDeviceArrayAdapter;
	
	/**   �����o�ϐ��i�����N���X�����j   */
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
	
	/**   �����o�ϐ��i�����N���X�����j   */
	private BroadcastReceiver bcReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				//   �f�o�C�X���{���f�B���O����Ă��Ȃ��ꍇ�͐V�K�f�o�C�X���X�g�֒ǉ�
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
		//   �ڑ��ς݃f�o�C�X
		pairedDeviceArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(pairedDeviceArrayAdapter);
		pairedListView.setOnItemClickListener(deviceClickListener);
		//   ���ڑ��f�o�C�X
		newDeviceArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		ListView newDeviceListView = (ListView) findViewById(R.id.new_devices);
		newDeviceListView.setAdapter(newDeviceArrayAdapter);
		newDeviceListView.setOnItemClickListener(deviceClickListener);
		//   ���V�[�o�o�^
		IntentFilter actionFoundFilter= new IntentFilter(BluetoothDevice.ACTION_FOUND);
		IntentFilter discoveryFinishedFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(bcReceiver, actionFoundFilter);
		registerReceiver(bcReceiver, discoveryFinishedFilter);
		
		//   �ڑ��ς݃f�o�C�X���擾
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
	 *  �f�o�C�X�f�B�X�J�o��
	 */
	private void discovery() {
		if (LOGGER_ENABLE) Log.d(LOGGER_TAG, "discovery");
		
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);
		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
		
		//   ���Ƀf�B�X�J�o�����̏ꍇ�͒�~���ĊJ
		if (btAdapter.isDiscovering()) {
			btAdapter.cancelDiscovery();
		}
		btAdapter.startDiscovery();
	}
}
