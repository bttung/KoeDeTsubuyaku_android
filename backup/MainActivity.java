package jp.zanmai.TestTwitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;


//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.android.NeuroData;
import com.neurosky.android.NeuroRawData;
import com.neurosky.android.NeuroSky;
import com.neurosky.android.NeuroStreamParser;

public class MainActivity<MaindActivity> extends Activity {

	public static RequestToken requestToken = null;
	public static OAuthAuthorization OAuth = null;

	public static String consumerKeyStr = "xnss7Rw44vvrHs9Gwto7ZA";
	public static String consumerSecretStr = "Fs2mAPOaBAKmt4bvet7Xgp98Q1yOXtUUtQAtMkGIMpU";
	public String tokenStr, tokenSecretStr = "";

	//�n��E����
	private static final int LANGUAGE_JAPANESE = 0;
	private static final int LANGUAGE_ENGLISH = 1;
	private static int myLanguage = 0; // �����͓��{��
	private static String languageStr = null; 
	

	public static final String PREFERENCE_NAME = "MyPrefsFile";

	private static final String TAG = "TwitterActivity";
	private static final int REQUEST_CODE = 1234;
	private boolean hasFooter = false;
	
	private ArrayList<String> m_speechToChar;

	private List<TwitListItem> twitList = null;

	private Twitter tw = null;
	private String status = null;

	private final String APPDIR_PATH = "TwitterProfileImage"; // microSD�ȉ��ɍ쐬�����f�B���N�g���B
	private boolean folderExistFlag, imgExistFlag = false;

	public int pageNumber = 1;
	public int count = 20;

	public ListView listView = null;
	private String myScreenName = null;
	private String item_list[] = null;

	/** ASYNCTASK�ɂ��i���� �@Progressbar **/
	private static final String ASYNCTASK_TAG = "ASYNC_TASK";
	ProgressDialog mProgressDialog = null;
	static final int DIALOG_ID = 47;

	/** �f�o�b�O�֘A */
	private static final String LOGGER_TAG = "NEURO_SKY";
	private static final boolean LOGGER_ENABLE = true;

	
	/** �����X�V�̂��߂̃X���b�h*/
	private Runnable m_UpdateRunnable=null;
	private Handler m_handler = null;
	private static boolean m_isOtherProccess=false; // �����̏ꍇ��onPause()���Ă΂�X���b�h���~�܂�̂Ō��݂͎g���Ă��Ȃ�
	private static final int FIRST_INTERVAL = 1000;
	private static int m_REPEAT_INTERVAL = 120000;
	
	
	
	/** Intent bluetooth�p�̃��N�G�X�g�R�[�h */
	@SuppressWarnings("unused")
	private static final int REQUEST_CONNECT_DEVICE = 0x01;
	@SuppressWarnings("unused")
	private static final int REQUEST_ENABLE_BT = 0x02;
	
	
	
	
	
	private static final int DIAG_SELECT_SPEECH_RESULT = 100;

	/** RAW �f�[�^�v�ۃt���O */
	private static final boolean RAW_DATA_ENABLE = true;

	/** �����o�ϐ� */

	//private BluetoothAdapter btAdapter;
	private NeuroSky neuroSky;

	// Layout Views
	private TextView mTitle;
	// Name of the connected device
	//private String mConnectedDeviceName = "";

	private boolean mConnectedTwitter = false;
	//private int changedStatus, sumAttention = 0, meanAttention = 0, dataTransCount = 0;

	private SQLLOGManager m_SQLLOG;

	
	private static String providerLink = " powered by http://min7.jp/"; 
	
	
	
	
//	@SuppressWarnings("unused")
//	private final Handler btHandler = new Handler() {
//		@Override
//		public void handleMessage(android.os.Message msg) {
//
//			int attention;
//			String mindSetMsg = "";
//
//			if (mConnectedTwitter) {
//
//				tw = CreateTwitterInstance();
//
//				switch (msg.what) {
//				case (NeuroSky.MESSAGE_BT_DEVICE_NAME):
//					mConnectedDeviceName = "MindSet";
//					if (LOGGER_ENABLE)
//						Log.d(LOGGER_TAG, "[ MESSAGE_BT_DEVICE_NAME ]");
//					break;
//
//				case (NeuroSky.MESSAGE_BT_STATUS_CHANGE):
//					if (LOGGER_ENABLE)
//						Log.d(LOGGER_TAG, "[ MESSAGE_BT_STATUS_CHANGE ]");
//					// �ύX���ꂽ�X�e�[�^�X
//					changedStatus = msg.arg1;
//
//					if (changedStatus == NeuroSky.BT_STATE_NONE) {
//						mTitle.setText(R.string.title_not_connected);
//						Log.i(LOGGER_TAG, "BT_STATE_NONE");
//					} else if (changedStatus == NeuroSky.BT_STATE_CONNECTING) {
//						mTitle.setText(R.string.title_connecting);
//						Log.i(LOGGER_TAG, "BT_STATE_CONNECTING");
//					} else if (changedStatus == NeuroSky.BT_STATE_CONNECTED) {
//						mTitle.setText(R.string.title_connected_to);
//						mTitle.append(":MindSet");
//						Log.i(LOGGER_TAG, "BT_STATE_CONNECTED"
//								+ mConnectedDeviceName);
//					}
//					break;
//
//				case (NeuroSky.MESSAGE_BT_TOAST):
//					if (LOGGER_ENABLE)
//						Log.d(LOGGER_TAG, "[ MESSAGE_BT_TOAST ]");
//					Toast.makeText(getApplicationContext(),
//							msg.getData().getString("toast"),
//							Toast.LENGTH_SHORT).show();
//					break;
//
//				case (NeuroStreamParser.MESSAGE_READ_DIGEST_DATA_PACKET):
//					if (LOGGER_ENABLE)
//						Log.d(LOGGER_TAG, "[ MESSAGE_READ_DIGEST_DATA_PACKET ]");
//					mTitle.setText(R.string.title_getting_data);
//
//					NeuroData data = (NeuroData) msg.obj;
//					attention = data.getAttention();
//
//					sumAttention += attention;
//					dataTransCount++;
//
//					if (dataTransCount >= 15) {
//						meanAttention = sumAttention / dataTransCount;
//
//						if (meanAttention >= 0 && meanAttention < 20) {
//							mindSetMsg = "�܂��܂����ȁ[";
//						} else if (meanAttention >= 20 && meanAttention < 40) {
//							mindSetMsg = "���I������ƏW�����Ă����B";
//						} else if (meanAttention >= 40 && meanAttention < 60) {
//							mindSetMsg = "���̒��q�Ŋ撣��I";
//						} else if (meanAttention >= 60 && meanAttention < 80) {
//							mindSetMsg = "�������W���́I�I�I";
//						} else if (meanAttention >= 80 && meanAttention <= 100) {
//							mindSetMsg = "�W���̒B�l�F��I�I�I";
//						}
//
//						mindSetMsg = meanAttention + ":" + mindSetMsg;
//						if (meanAttention > 0)
//							EasyUpdateStatusTimeLine(mindSetMsg);
//
//						sumAttention = 0;
//						dataTransCount = 0;
//
//						if (meanAttention > 0)
//							RefreshStatusTimeLine();
//					}
//
//					if (LOGGER_ENABLE)
//						Log.i(LOGGER_TAG, getNeuroData(msg));
//					break;
//
//				case (NeuroStreamParser.MESSAGE_READ_RAW_DATA_PACKET):
//					if (LOGGER_ENABLE)
//						Log.d(LOGGER_TAG, "[ MESSAGE_READ_RAW_DATA_PACKET ]");
//					NeuroRawData rawData = (NeuroRawData) msg.obj;
//					if (LOGGER_ENABLE)
//						Log.i(LOGGER_TAG,
//								"Raw values : " + rawData.getRawWaveValue());
//					break;
//				}
//			}
//		};
//	};
	








	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//m_SQLLOG = new SQLLOGManager(getApplicationContext());
		m_SQLLOG = new SQLLOGManager();
		Uri uri = getIntent().getData();
		boolean isAuthenticated = false;
		
		SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
		status = pref.getString("status", "");

		// �R�[���o�b�N�Ȃ�token��o�^
		if (uri != null && !status.equals("available") ) {
			if (uri.toString().startsWith("Callback://MainActivity")
					&& uri.toString().indexOf("oauth_token") != -1
					&& uri.toString().indexOf("oauth_verifier") != -1) {

				//showToast("Callback");
				m_SQLLOG.Log("MainActivity", "uri = " + uri.toString());
				isAuthenticated = RegistToken(uri);
			} else {
				m_SQLLOG.Log("MainActivity", "uri = " + uri.toString());
				//disconnectTwitter(); // ����
			}
		}

		// GUI����
		CreateGUI(isAuthenticated);
	}

	private void CreateGUI(boolean isSAuthenticated) {

		if (LOGGER_ENABLE)
			Log.d(LOGGER_TAG, "_/_/_/_/_/   ON-CREATE   _/_/_/_/_/");

		// Find the user Language
		languageStr = Locale.getDefault().getLanguage().toString();
		// the result should be: en, ja, ko, de, fr, it, zh
		m_SQLLOG.Log("MainActivity", "Default Language: " + languageStr );

		
		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		// requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
		status = pref.getString("status", "");

		if (status != "") {
			// showToast("Available");

			m_SQLLOG.Log("MainActivity", "status����");

			//Button Refresh_bt = (Button) findViewById(R.id.refresh_bt);
			Button LogIn_bt = (Button) findViewById(R.id.login_bt);
			Button Twitter_bt = (Button) findViewById(R.id.Twitter_bt);
			Button Voice_bt = (Button) findViewById(R.id.voice_bt);
			Button changeLanguage_bt = (Button) findViewById(R.id.changeLanguage_bt);

			
			int btWidth = Twitter_bt.getWidth();
			Voice_bt.setWidth(btWidth);
			
			int btHeight = Twitter_bt.getHeight();
			Voice_bt.setHeight(btHeight);
			
			//Refresh_bt.setOnClickListener(refreshOnClickListener);
			LogIn_bt.setOnClickListener(LoginClickListenter);
			Voice_bt.setOnClickListener(voiceRecogListener);
			Twitter_bt.setOnClickListener(tweetListener);
			changeLanguage_bt.setOnClickListener(ChangeLanguageClickListener);

			// �F�؍ς݂Ȃ̂Ń{�^��������
			// LogIn_bt.setVisibility(View.GONE);

			// �F�؍ς݂Ȃ̂Ŗ��O���usign in�v ����ulogout�v�֕ς���
//			if ( languageStr.equals("ja") ) {
//				LogIn_bt.setText("�A�E�g");
//				// LogIn_bt.setTextSize(8.0f);
//			} else {
//				LogIn_bt.setText("logout"); 
//			}
			
			LogIn_bt.setVisibility(View.GONE);
			
			
			
			// �����X�V�X���b�h
			m_handler = new Handler();
			m_UpdateRunnable = new Runnable() {
	            //@Override
	            public void run() {

	                //2.�J��Ԃ�����
	            	if ( !m_isOtherProccess ) {
		            	if (mConnectedTwitter) {
		            		
		            		//showToast("�X�V���܂�");
		            		
		        			tw = CreateTwitterInstance();
		        			RefreshStatusTimeLine();
		            	}
		            	else if (status != "") {
		        			mConnectedTwitter = true;
		        			tw = CreateTwitterInstance();
		        
		        			twitList = GetTwitterTimeLine();
		        			ShowTwitterTimeline(twitList);
		        		}
	            	}
	            	
	            	// Restore preferences
					SharedPreferences settings = getSharedPreferences(PREFERENCE_NAME, 0);
					m_REPEAT_INTERVAL = settings.getInt("FreqTime", m_REPEAT_INTERVAL);
	                
	                //3.���񏈗����Z�b�g
	                m_handler.postDelayed(this, m_REPEAT_INTERVAL);
	            }
	        };
		} else {
			//showToast("Invisible");
			mConnectedTwitter = false;
			Button LogIn_bt = (Button) findViewById(R.id.login_bt);
			LogIn_bt.setOnClickListener(LoginClickListenter);

			// �]���ȃC���^�[�t�F�[�X������
			//Button Refresh_bt = (Button) findViewById(R.id.refresh_bt);
			Button Voice_bt = (Button) findViewById(R.id.voice_bt);
			Button Twitter_bt = (Button) findViewById(R.id.Twitter_bt);
			//Button changeLanguage_bt = (Button) findViewById(R.id.changeLanguage_bt);
			EditText tweet_ed = (EditText) findViewById(R.id.tweet_edt);

			//Refresh_bt.setVisibility(View.GONE);
			Voice_bt.setVisibility(View.GONE);
			Twitter_bt.setVisibility(View.GONE);
			//changeLanguage_bt.setVisibility(View.GONE);
			tweet_ed.setVisibility(View.GONE);
		}

		// Get local Bluetooth adapter
		/*				
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		// If the adapter is null, then Bluetooth is not supported
		if (btAdapter == null) {
			showToast("Bluetooth is not available");
			finish();
			return;
		}
		*/

		m_SQLLOG.Log("MainActivity", "onCreate()����");
	}

	private boolean RegistToken(Uri uri) {

		boolean isSuccess = false;

		// oauth_verifier���擾����
		String verifier = uri.getQueryParameter("oauth_verifier");
		try {
			// AccessToken�I�u�W�F�N�g���擾
			Log.d(TAG, "before get token ");
			// �����ł悭��������������

			m_SQLLOG.Log("MainActivity", "token�O");

			if (verifier != null) {

				AccessToken token = null;

				m_SQLLOG.Log("MainActivity",
						"verifier = " + verifier.toString());

				if (OAuth != null) {
					token = OAuth.getOAuthAccessToken(verifier);
					m_SQLLOG.Log("MainActivity", "token�擾����");
					isSuccess = true;
				} else {
					m_SQLLOG.Log("MainActivity", "OAuth is null");
				}

				// �A�g��Ԃƃg�[�N���̏�������
				SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
				SharedPreferences.Editor editor = pref.edit();

				CharSequence tokenInfo = "";

				if (token != null) {

					tokenStr = token.getToken();
					tokenSecretStr = token.getTokenSecret();
					tokenInfo = "token�F" + tokenStr + "\r\n" + "token secret�F"
							+ tokenSecretStr;

					editor.putString("status", "available");
					editor.putString("oauth_token", token.getToken());
					editor.putString("oauth_token_secret",
							token.getTokenSecret());

					m_SQLLOG.Log("MainActivity", "tokenStr=" + tokenStr);
					m_SQLLOG.Log("MainActivity", "tokenSecretStr="
							+ tokenSecretStr);

					// Log.d(TAG, token.getToken() + ":"
					// +token.getTokenSecret());
					Log.d(TAG, (String) tokenInfo);
				} else {

					tokenInfo = "token get failed�@";
					Log.d(TAG, "token failed");
					//showToast("�g�[�N��failed!");

				}

				editor.commit();
			}

			m_SQLLOG.Log("MainActivity", "token�擾�I��");

			Log.d(TAG, "after get token ");
		} catch (TwitterException e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	// Sign in�{�^��
	View.OnClickListener LoginClickListenter = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			Log.d(TAG, "Login button Clicked");
			SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME,
					MODE_PRIVATE);
			final String nechatterStatus = pref.getString("status", "");

			if (isConnected(nechatterStatus)) {

				//showToast("�A�g�ς�\n�������܂�");

				// �A�g�ς݂̏ꍇ�͘A�g����
				Log("before disconect");
				disconnectTwitter();
				Log("after disconect");

			} else {
				// ���A�g�̏ꍇ�͘A�g�ݒ�
			}

			if (isConnected(nechatterStatus)) {
				Intent intent = new Intent(MainActivity.this,
						MainActivity.class);
				startActivity(intent);
			} else {
				try {
					connectTwitter();
				} catch (TwitterException e) {
					showToast("Connect Problem!");
				}
			}

		}

	};

	// Twitter�A�g��Ԃ̊m�F
	final private boolean isConnected(String nechatterStatus) {

		if (nechatterStatus != null && nechatterStatus.equals("available")) {
			return true;
		} else {
			return false;
		}

	}

	View.OnClickListener ChangeLanguageClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			hideSoftKeyboard();
			ChangeLanguage();
		}
	};

	View.OnClickListener refreshOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			hideSoftKeyboard();

			/*
			 * showDialog(REFRESH_DIALOG_ID); new
			 * TwitterTimelineService().execute("");
			 */

			RefreshStatusTimeLine();
			showToast("refreshed");
		}

	};

	public void hideSoftKeyboard() {

		EditText twit_edt = (EditText) findViewById(R.id.tweet_edt);
		InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(twit_edt.getWindowToken(), 0);

	}

	public void hideSoftKeyboardOnStart() {
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	public void RefreshStatusTimeLine() {

		twitList = GetTwitterTimeLine();
		ShowTwitterTimeline(twitList);

	}

	View.OnClickListener tweetListener = new View.OnClickListener() {

		@Override
		public void onClick(View arg0) {

			hideSoftKeyboard();
			UpdateStatusTimeLine();

		}
	};

	private void UpdateStatusTimeLine() {

		String tweetContentStr = getTwitEditText() + providerLink; 

		try {

			tw.updateStatus(tweetContentStr);
			Log.d(TAG, "tweet content : " + tweetContentStr);
			SetTwitEdtTxt("");

		} catch (TwitterException e) {
			e.printStackTrace();
		}

		Toast.makeText(MainActivity.this, "tweeted", Toast.LENGTH_LONG).show();

		twitList = GetTwitterTimeLine();
		ShowTwitterTimeline(twitList);

	}

//	private void EasyUpdateStatusTimeLine(String twitStr) {
//		try {
//			Log.d(LOGGER_TAG, "EasyTwit : " + twitStr);
//			tw.updateStatus(twitStr);
//		} catch (TwitterException e) {
//			e.printStackTrace();
//		}
//	}

	private void UpdateStatusTimeLine(String twitStr) {

		try {
			Log.d(TAG, "tweet content : " + twitStr);
			tw.updateStatus(twitStr);

		} catch (TwitterException e) {
			e.printStackTrace();
		}

		Log.d(TAG, "tweet OK");

		showToast(getString(R.string.tweeted));

		twitList = GetTwitterTimeLine();
		ShowTwitterTimeline(twitList);
	}

	// ����ؑփ{�^���������ꂽ
	private void ChangeLanguage() {

		if (myLanguage == LANGUAGE_ENGLISH) {

			myLanguage = LANGUAGE_JAPANESE;
			showToast("���{��ɐ؂�ւ��܂���");

		} else {

			myLanguage = LANGUAGE_ENGLISH;
			Toast.makeText(MainActivity.this, "Language changed to English",
					Toast.LENGTH_SHORT).show();
			showToast("Language changed to English");

		}

	}

	View.OnClickListener voiceRecogListener = new View.OnClickListener() {

		@Override
		public void onClick(View arg0) {

			try {

				Intent intent = new Intent(
						RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
						RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
				intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
						"VoiceRecognitionTest");
				
				// Restore preferences
				SharedPreferences settings = getSharedPreferences(PREFERENCE_NAME, 0);
				myLanguage = settings.getInt("Language", LANGUAGE_JAPANESE);

				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
						RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); // ���R���w��

				if (myLanguage == LANGUAGE_JAPANESE) {

					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
							Locale.JAPANESE.toString()); // ���{��w��
					// �\�������镶����
					intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "���{����ǂ���");

				} else if (myLanguage == LANGUAGE_ENGLISH) {

					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
							Locale.ENGLISH.toString()); // �p��w��
					// �\�������镶����
					intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
							"English please");

				}

				Log.d(TAG, "start Voice Recog Activity!");
				startActivityForResult(intent, REQUEST_CODE);

			} catch (ActivityNotFoundException e) {

				//showToast("ActivityNotFoundException");
				e.printStackTrace();

			}

			Log.d(TAG, "Voice Recognize finish!");

		}
	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (LOGGER_ENABLE)
			Log.d(LOGGER_TAG, "onActivityResult : " + resultCode);

		switch (requestCode) {
		
		
		/*
		case (REQUEST_CONNECT_DEVICE):
			if (resultCode == Activity.RESULT_OK) {
				String deviceAddress = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				BluetoothDevice device = btAdapter
						.getRemoteDevice(deviceAddress);
				// �Ώۃf�o�C�X�Ɛڑ�

				neuroSky.connect(device);
			}
			break;

		case (REQUEST_ENABLE_BT):

			if (resultCode == Activity.RESULT_OK) {
				neuroSky = new NeuroSky(btAdapter, btHandler);
			} else {
				Toast.makeText(this, R.string.err_bluetooth_not_enabled,
						Toast.LENGTH_SHORT).show();
			}
			break;
		*/

		case (REQUEST_CODE):
			
			if (resultCode == Activity.RESULT_OK) {
				String resultsString = "";

				// ���ʕ����񃊃X�g
				ArrayList<String> results = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				resultsString = results.get(0);
				
				/*
				m_speechToChar = new ArrayList<String>();
				
				for ( int i=0;i<results.size();i++ ) {
					m_speechToChar.add(results.get(i));
				}
				*/

				Log.d(TAG, "voice recog: " + resultsString);

				// �g�[�X�g���g���Č��ʂ�\��
				//Toast.makeText(MainActivity.this, resultsString,
				//		Toast.LENGTH_LONG).show();
				removeDialog(DIAG_SELECT_SPEECH_RESULT);
				showDialog(DIAG_SELECT_SPEECH_RESULT);

				UpdateStatusTimeLine(resultsString + providerLink);

				super.onActivityResult(requestCode, resultCode, data);
				
			}
		}
	}

	// Twitter�A�g����
	private void disconnectTwitter() {

		// �A�g��Ԃƃg�[�N���̍폜
		SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME,
				MODE_PRIVATE);

		SharedPreferences.Editor editor = pref.edit();

		String oauth_token = pref.getString("oauth_token", "");
		String oauth_token_secret = pref.getString("oauth_token_secret", "");
		String status = pref.getString("status", "");

		if (!oauth_token.equals(""))
			editor.remove("oauth_token");
		if (!oauth_token_secret.equals(""))
			editor.remove("oauth_token_secret");
		if (!status.equals(""))
			editor.remove("status");

		editor.commit();

		// �ݒ肨���܂��B
		// finish();

	}

	private void connectTwitter() throws TwitterException {

		// Twitetr4j�̐ݒ��ǂݍ���
		Configuration conf = ConfigurationContext.getInstance();

		// �f�o�b�O
		conf.getOAuthAccessToken();
		conf.getOAuthAccessTokenSecret();
		Log.d(TAG, "���O�A�N�Z�X�g�[�N���A�A�N�Z�X�g�[�N���Z�N���b�g: " + conf.getOAuthAccessToken()
				+ " " + conf.getOAuthAccessTokenSecret());

		// Oauth�F�؃I�u�W�F�N�g�쐬
		OAuth = new OAuthAuthorization(conf);

		// Oauth�F�؃I�u�W�F�N�g��consumerKey��consumerSecret��ݒ�
		OAuth.setOAuthConsumer(consumerKeyStr, consumerSecretStr);

		// �A�v���̔F�؃I�u�W�F�N�g�쐬
		try {
			m_SQLLOG.Log("MainActivity", "connectTwitter() start callback");
			Log.d(TAG, "before Start CallbackActivity");
			// requestToken =
			// OAuth.getOAuthRequestToken("Callback://CallBackActivity");
			requestToken = OAuth
					.getOAuthRequestToken("Callback://MainActivity");
			m_SQLLOG.Log("MainActivity", "requestToken�擾����");
			Log.d(TAG, "Start OAuth.getOAuthRequestToken");

		} catch (TwitterException e) {
			Log.d(TAG, "Cannot Start OAuth.getOAuthRequestToken");
			e.printStackTrace();
			throw e;
		}

		String _uri;

		if (requestToken != null) {
			_uri = requestToken.getAuthorizationURL();
			m_SQLLOG.Log("MainActivity", "getAuthorizationURL()����");
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(_uri)));
			// startActivityForResult(new Intent(Intent.ACTION_VIEW ,
			// Uri.parse(_uri)), 0);
		} else {
			m_SQLLOG.Log("MainActivity", "requestToken is null");
			//showToast("���s");
		}

	}

	public boolean isHasFooter() {
		return hasFooter;
	}

	public void setHasFooter(boolean hasFooter) {
		this.hasFooter = hasFooter;
	}

	// Twitter�C���X�^���X���쐬
	public Twitter CreateTwitterInstance() {

		// ���̧�ݽ���擾
		SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME,
				MODE_PRIVATE);

		// �^�ɍ��킹��get���\�b�h�Œl���擾
		String tokenStr = pref.getString("oauth_token", "");
		String tokenSecretStr = pref.getString("oauth_token_secret", "");

		// getAccessToken(tokenStr, tokenSecretStr);
		AccessToken token = new AccessToken(tokenStr, tokenSecretStr);

		Configuration config = new ConfigurationBuilder().build();
		OAuthAuthorization auth = new OAuthAuthorization(config);

		auth.setOAuthConsumer(consumerKeyStr, consumerSecretStr);
		auth.setOAuthAccessToken(token);

		Twitter tw = new TwitterFactory().getInstance(auth);

		return tw;

	}

	// Twitter�^�C�����C���̃��X�g���擾
	public List<TwitListItem> GetTwitterTimeLine() {

		showMyScreenName();
		twitList = EasyGetTwitterTimeLine();

		return twitList;

	}

	public void showMyScreenName() {

		try {
			myScreenName = tw.getScreenName();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (TwitterException e) {
			e.printStackTrace();
		}

		Log.d(TAG, "myScreenName : " + myScreenName);
		// TextView screenName_tv =
		// (TextView)findViewById(R.id.myScreenName_tv);
		// screenName_tv.setText(myScreenName);

		TextView title_tv_left = (TextView) findViewById(R.id.title_left_text);
		TextView title_tv_right = (TextView) findViewById(R.id.title_right_text);
		title_tv_left.setText(getString(R.string.app_name));
		title_tv_right.setText(myScreenName);

	}

	// Twitter�^�C�����C���̃��X�g���擾
	public List<TwitListItem> EasyGetTwitterTimeLine() {

		Bitmap defaultImage = BitmapFactory.decodeResource(getResources(),
				R.drawable.default_image);
		twitList = new ArrayList<TwitListItem>();

		try {
			// TL�̎擾
			ResponseList<Status> homeTl;
			// Paging
			Paging paging = new Paging(pageNumber, count);
			Log.d(TAG, "count : " + count);
			// homeTl = tw.getHomeTimeline();

			homeTl = tw.getHomeTimeline(paging);

			for (Status status : homeTl) {

				// �Ԃ₫�̃��[�U�[ID�̎擾
				String userName = status.getUser().getScreenName();
				String imgName = userName + ".png";
				// �v���t�B�[���C���[�W���擾
				String imgURL = status.getUser().getProfileImageURL()
						.toString();
				Log.d(TAG, "imageURL: " + imgURL);

				DownloadOrNot_Image(imgURL, APPDIR_PATH, imgName);

				Log.d(TAG, "before load " + imgName);
				Bitmap userImage = loadImage(APPDIR_PATH, imgName);
				Log.d(TAG, "After load " + imgName);

				// �Ԃ₫�̎擾
				String tweet = status.getText();

				TwitListItem item = new TwitListItem();
				if (userImage == null) {
					item.image = defaultImage;
				} else {
					item.image = userImage;
				}

				Date createdDate = status.getCreatedAt();

				item.name = userName;
				item.comment = tweet;
				item.id = status.getId();
				item.time = GetPassedTime(createdDate);

				twitList.add(item);

				Log.d(TAG, "Added " + userName + " to twitList");

			}

		} catch (TwitterException e) {

			e.printStackTrace();

			if (e.isCausedByNetworkIssue()) {
				showToast(getString(R.string.error));
			} else {
				showToast(getString(R.string.network_error));
			}

		}

		Log.d(TAG, "finished GetTwitterTimeLine()");

		return twitList;

	}

	// Twitter �^�C�����C�� ��\��������
	public void ShowTwitterTimeline(List<TwitListItem> twitList) {

		TwitListItemAdapter adapter = new TwitListItemAdapter(this, 0, twitList);

		listView = (ListView) findViewById(R.id.Twit_listview);

		if (!isHasFooter()) {
			listView.addFooterView(
					getLayoutInflater().inflate(R.layout.footer, null), null,
					true);
			setHasFooter(true);
		}

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(ListViewOnItemClickListener);
		listView.setOnItemLongClickListener(ListViewOnItemLongClickListener);

	}

	public String getTwitEditText() {

		EditText twit_edt = (EditText) findViewById(R.id.tweet_edt);
		return twit_edt.getText().toString();

	}

	public void SetTwitEdtTxt(String artMark, String str) {

		EditText twit_edt = (EditText) findViewById(R.id.tweet_edt);
		twit_edt.setText(artMark + str);

	}

	public void SetTwitEdtTxt(String str) {

		EditText twit_edt = (EditText) findViewById(R.id.tweet_edt);
		twit_edt.setText(str);

	}

	public void ClearTwitEdtTxt() {

		EditText twit_edt = (EditText) findViewById(R.id.tweet_edt);
		twit_edt.setText("");

	}

	public String GetPassedTime(Date createdDate) {

		String elapsedTime = null;

		Date nowDate = new Date();

		// Log.d(TAG, "Time : " + createdDate.toString());
		// Log.d(TAG, "Time : " + nowDate.toString());

		long tmpTime = createdDate.getTime();
		long nowTime = nowDate.getTime();

		// Get elapsed time in milliseconds
		long passedTime = nowTime - tmpTime;

		float elapsedTimeMin = passedTime / (60 * 1000F);
		if (elapsedTimeMin < 0)
			elapsedTimeMin = 0;

		int elapsedTimeMinCeiled = (int) Math.round(elapsedTimeMin);

		// Get elapsed time in hours
		int elapsedTimeHour = elapsedTimeMinCeiled / 60;
		elapsedTimeMinCeiled = elapsedTimeMinCeiled % 60;

		if (elapsedTimeHour < 1)
			elapsedTime = elapsedTimeMinCeiled + "���O";
		else
			elapsedTime = elapsedTimeHour + "���� " + elapsedTimeMinCeiled + "���O";

		// Log.d(TAG, elapsedTimeHour + "�� " + elapsedTimeMinCeiled + "���O");

		return elapsedTime;

	}

	public boolean Timer(int timer_second, long start_time) {

		boolean timer_flag = false;
		long elapsedSecond = System.currentTimeMillis() - start_time;
		if (elapsedSecond >= timer_second * 1000)
			timer_flag = true;
		Log.d(LOGGER_TAG, "elapsedSecond : " + elapsedSecond / 1000.0);
		return timer_flag;

	}

	// �^�C�����C�����N���b�N���ꂽ��
	ListView.OnItemClickListener ListViewOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			// hideSoftKeyboard();
			
			//�N���b�N���ꂽ���̐F�ݒ�
			view.setBackgroundColor(Color.GREEN);

			if (view.getId() != R.id.Footer) {
				// ListView�ɃL���X�g
				Log.d(TAG, view.getId() + "clicked");
				ListView listView = (ListView) parent;

				// ���X�g�A�C�e�����擾
				TwitListItem item = (TwitListItem) listView
						.getItemAtPosition(position);

				String userName = item.name;
				Log.d(TAG, "�I�����ꂽ���ڂ̖��O:" + userName);
				// Log.d(TAG, "�I�����ꂽ�A�C�e����comment:" + item.comment);

				SetTwitEdtTxt("@", userName);

				//showToast(item.name + " clicked");

			} else if (view.getId() == R.id.Footer) {

				//showToast("�ǂݍ���");
				count += 5;

				List<TwitListItem> refreshedTwitList = GetTwitterTimeLine();
				ShowTwitterTimeline(refreshedTwitList);

				listView.setSelectionFromTop(count, 100);

			}
		}
	};

	// �^�C�����C��������
	AdapterView.OnItemLongClickListener ListViewOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {

			//Toast.makeText(MainActivity.this, " Long clicked", Toast.LENGTH_SHORT).show();
			ClearTwitEdtTxt();
			hideSoftKeyboard();

			ListView listView = (ListView) parent;
			//�N���b�N���ꂽ���̐F�ݒ�
			listView.setBackgroundColor(Color.GREEN);
			
			// ���X�g�A�C�e�����擾
			TwitListItem item = (TwitListItem) listView
					.getItemAtPosition(position);
			final String tmpUserName = item.name;
			final String tmpTwit = item.comment;
			final long tmpId = item.id;
			
			final String Favorite = getString(R.string.add_favorite);
			final String Retweet = getString(R.string.retweet);
			final String Reply = getString(R.string.reply);
			final String Delete = getString(R.string.cancel_retweet);

			if (view.getId() != R.id.Footer) {
				
				if (tmpUserName.equals(myScreenName)) {
					item_list = new String[] { Favorite, Reply, Delete };

				} else {
					item_list = new String[] { Favorite, Retweet, Reply };

				}

				// �����F���b�Z�[�W�_�C�A���O�̕\��
				new AlertDialog.Builder(MainActivity.this).setItems(item_list,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

								if (item_list[which].equals(Favorite)) {

									//showToast("Favorite");

									// Twitter tw = CreateTwitterInstance();
									// tw = CreateTwitterInstance();

									try {
										tw.createFavorite(tmpId);
									} catch (TwitterException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								} else if (item_list[which].equals(Reply)) {

									//showToast("Reply");
									SetTwitEdtTxt("@", tmpUserName + providerLink);

								} else if (item_list[which].equals(Retweet)) {

									//showToast("ReTweet Clicked");
									UpdateStatusTimeLine("@" + tmpUserName
											+ " " + tmpTwit + providerLink);

								} else if (item_list[which].equals(Delete)) {

									//showToast("Delete Clicked");

									try {
										
										m_SQLLOG.Log("MainActivity",
												"destroyStatus()");
										tw.destroyStatus(tmpId);
										m_SQLLOG.Log("MainActivity", "Status deleted");
										
										RefreshStatusTimeLine();
										m_SQLLOG.Log("MainActivity", "Refreshed StatusTimeLine");
										
									} catch (TwitterException e) {
										// TODO Auto-generated catch block
										m_SQLLOG.Log("MainActivity",
												e.toString());
										e.printStackTrace();
									}

								}
							}
						}).show();
			}

			return false;

		}

	};

	// ����URL����r�b�g�}�b�v�C���[�W���Q�b�g����
	public Bitmap getBitmap(URL bitmapUrl) {

		try {
			return BitmapFactory.decodeStream(bitmapUrl.openConnection()
					.getInputStream());
		} catch (Exception ex) {
			return null;
		}

	}

	// �A�N�Z�X�g�[�N�����擾����
	public void getAccessToken(String tokenStr, String tokenSecretStr) {

		// ���̧�ݽ���擾
		SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME,
				MODE_PRIVATE);

		// �^�ɍ��킹��get���\�b�h�Œl���擾
		tokenStr = pref.getString("oauth_token", "");
		tokenSecretStr = pref.getString("oauth_token_secret", "");

	}

	// ����t�H���_���炠��C���[�W��ǂݍ���
	public Bitmap loadImage(String APPDIR_PATH, String imgName) {

		Bitmap myBitmap = null;

		if (CheckFolderExist(APPDIR_PATH)) {

			// Log.d(TAG, "Checked Image folder");

			if (CheckFileExist(APPDIR_PATH, imgName)) {

				// Log.d(TAG, "reading Image From saved folder");
				String imgNameAppDir = "/sdcard/" + APPDIR_PATH + "/" + imgName;
				myBitmap = BitmapFactory.decodeFile(imgNameAppDir);

			}

		}

		return myBitmap;

	}

	// �������ɂ���t�H���_�����邩�ǂ���
	public boolean CheckFolderExist(String APPDIR_PATH) {

		File file_to_basedir = new File(
				Environment.getExternalStorageDirectory(), APPDIR_PATH);

		if (!file_to_basedir.exists()) {

			file_to_basedir.mkdirs();
			folderExistFlag = true;
			// Log.i(TAG,"DirectoryHasCreated!");

		} else {

			folderExistFlag = true;
			// Log.i(TAG,"Directory Already exist");

		}

		folderExistFlag = true;

		return folderExistFlag;

	}

	// ����t�H���_�ɂ���t�@�C�������邩�ǂ����H
	public boolean CheckFileExist(String APPDIR_PATH, String imgName) {

		if (folderExistFlag) {

			File file_to_basedir = new File(
					Environment.getExternalStorageDirectory(), APPDIR_PATH);
			File file_to_appdir = new File(file_to_basedir.getPath(), imgName);

			if (!file_to_appdir.exists()) {

				imgExistFlag = false;

			} else {

				imgExistFlag = true;

			}
		}

		return imgExistFlag;
	}

	// �v���t�B���C���[�W�̗L���ɂ���āA�_�E�����[�h���邩���Ȃ�
	public boolean DownloadOrNot_Image(String imgURL, String APPDIR_PATH,
			String imgName) {

		Boolean imgOKFlag = false;

		if (CheckFolderExist(APPDIR_PATH)) {
			if (CheckFileExist(APPDIR_PATH, imgName)) {

				imgOKFlag = true;

			} else {

				URL myImgURL = null;

				try {
					myImgURL = new URL(imgURL);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

				Bitmap myBitMap = null;

				try {
					myBitMap = BitmapFactory.decodeStream(myImgURL
							.openConnection().getInputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}

				File file_to_basedir = new File(
						Environment.getExternalStorageDirectory(), APPDIR_PATH);
				File file_to_appdir = new File(file_to_basedir.getPath(),
						imgName);

				try {

					if (file_to_appdir.createNewFile() == true) {
						FileOutputStream fileoutput = new FileOutputStream(
								file_to_appdir);
						myBitMap.compress(CompressFormat.PNG, 100, fileoutput);
						fileoutput.flush();
						fileoutput.close();
						imgOKFlag = true;
					}

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

		return imgOKFlag;

	}

	// Dialog Stuff
	@Override
	protected Dialog onCreateDialog(int id) {
		
		Dialog dialog=null;

		switch (id) {
			case DIALOG_ID: {
				mProgressDialog = new ProgressDialog(this);
				mProgressDialog.setMessage(MainActivity.this
						.getString(R.string.msgPleaseWaitWhileLoading));
				mProgressDialog.setIndeterminate(true);
				mProgressDialog.setCancelable(true);
	
				// return mProgressDialog;
	
			}
			case DIAG_SELECT_SPEECH_RESULT:
			{
				CharSequence[] items = new CharSequence [m_speechToChar.size()];
		    	
		    	for ( int i=0;i<m_speechToChar.size();i++ ) {
		    		items[i] = m_speechToChar.get(i);
		    	}
	
		    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    	builder.setTitle("�F������");
		    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// �������Ȃ�
					}
				});
		    	builder.setItems(items, new DialogInterface.OnClickListener() {
		    	    public void onClick(DialogInterface dialog, int item) {
		    	    	String str = m_speechToChar.get(item);
		    	        //Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
		    	        //m_speechToChar.clear();
		    	        //m_speechToChar.add(str);
		    	        
		    	        
		    	        
		    	        EditText tweet_edt = (EditText) findViewById(R.id.tweet_edt);

						String originText = tweet_edt.getText().toString();
						if (originText.startsWith("@")) {
							str = originText + " " + str;
						}
						else {
							str = originText + str;
						}
						
						tweet_edt.setText(str);
		    	    }
		    	});
		    	dialog = builder.create();
		    	return dialog;

			}
		}

		return mProgressDialog;
	}

	// Download & Show Twitter Timeline
	@SuppressWarnings("unused")
	private class TwitterTimelineService extends
			AsyncTask<String, Integer, List<TwitListItem>> {

		@Override
		protected List<TwitListItem> doInBackground(String... params) {
			List<TwitListItem> twitList;
			twitList = EasyGetTwitterTimeLine();
			return twitList;
		}

		@Override
		protected void onPostExecute(List<TwitListItem> twitList) {

			Log(ASYNCTASK_TAG, "+++  onPostExecute  +++");

			Log(ASYNCTASK_TAG,
					"+++  onPostExecute  before dismiss ProgressDialog+++");

			if (mProgressDialog != null) {
				dismissDialog(DIALOG_ID);
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}

			// Log(ASYNCTASK_TAG,
			// "+++  onPostExecute  dismiss ProgressDialog+++");

			showMyScreenName();
			ShowTwitterTimeline(twitList);
		}

	}

	@Override
	protected void onStart() {
		super.onStart();

		if (LOGGER_ENABLE)
			Log.d(LOGGER_TAG, "_/_/_/_/_/   ON-START   _/_/_/_/_/");
		
		
		//if (mConnectedTwitter)
		//	tw = CreateTwitterInstance();

		
		
		
		/*
		if (btAdapter.isEnabled()) {
			neuroSky = new NeuroSky(btAdapter, btHandler);
		} else {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, REQUEST_ENABLE_BT);
		}
		*/
		
		
		
		
		hideSoftKeyboardOnStart();

		if (status != "") {
//			mConnectedTwitter = true;
//			tw = CreateTwitterInstance();
//
//			twitList = GetTwitterTimeLine();
//			ShowTwitterTimeline(twitList);

			/*
			 * Log(ASYNCTASK_TAG, "+++  onStart start ProgressDialog  +++");
			 * showDialog(DIALOG_ID); new TwitterTimelineService().execute("");
			 */
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (LOGGER_ENABLE)
			Log.d(LOGGER_TAG, "_/_/_/_/_/   ON-RESUME   _/_/_/_/_/");

		if (mConnectedTwitter)
			tw = CreateTwitterInstance();

		if ((neuroSky != null)
				&& (neuroSky.getStatus() == NeuroSky.BT_STATE_CONNECTED)) {
			neuroSky.start(RAW_DATA_ENABLE);
		}
		
		if ( m_handler != null && m_UpdateRunnable != null )
			m_handler.postDelayed(m_UpdateRunnable, FIRST_INTERVAL); // �X���b�h�J�n
		
		m_SQLLOG.Log("MainActivity", "onResume()");


		hideSoftKeyboardOnStart();
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (LOGGER_ENABLE)
			Log.d(LOGGER_TAG, "_/_/_/_/_/   ON-PAUSE   _/_/_/_/_/");

		if ((neuroSky != null)
				&& (neuroSky.getStatus() == NeuroSky.BT_STATE_CONNECTED)) {
			neuroSky.stop();
		}
		
		if ( m_UpdateRunnable != null ) {
			m_handler.removeCallbacks(m_UpdateRunnable); // �X���b�h��~
			m_UpdateRunnable=null;
			m_handler = null;
		}

		m_SQLLOG.Log("MainActivity", "onPause()");
		
		/*
		 * Log.d(ASYNCTASK_TAG, "+++  onPause dismiss ProgressDialog  +++");
		 * dismissDialog(DIALOG_ID);
		 */

	}

	@Override
	protected void onStop() {
		super.onStop();

		if (LOGGER_ENABLE)
			Log.d(LOGGER_TAG, "_/_/_/_/_/   ON-STOP   _/_/_/_/_/");

		if (neuroSky != null)
			neuroSky.stop();
		
		m_SQLLOG.Log("MainActivity", "onStop()");

		/*
		 * Log.d(ASYNCTASK_TAG, "+++  onStop dismiss ProgressDialog  +++");
		 * dismissDialog(DIALOG_ID);
		 */

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (LOGGER_ENABLE)
			Log.d(LOGGER_TAG, "_/_/_/_/_/   ON-DESTROY   _/_/_/_/_/");

		if (neuroSky != null)
			neuroSky.close();
		
		System.exit(0);
		
		m_SQLLOG.Log("MainActivity", "onDestroy()");

		/*
		 * Log.d(ASYNCTASK_TAG, "+++  onDestroy dismiss ProgressDialog  +++");
		 * dismissDialog(DIALOG_ID);
		 */

	}

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {

		hideSoftKeyboard();

		MenuInflater inflater = getMenuInflater();
		// When release NeuroWitter app, uncomment this line to show the option_menu
		inflater.inflate(R.menu.option_menu, menu);

		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		switch (item.getItemId()) {

		
		
		/*
		case (R.id.scan):

			neuroSky = new NeuroSky(btAdapter, btHandler);

			Intent intent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
			result = true;
			break;

		case (R.id.clear):

			if (neuroSky != null) {
				// �f�[�^��M�I��
				neuroSky.stop();
				// �ڑ��I��
				neuroSky.close();

				Log.d(LOGGER_TAG, "_/_/_/_/_/   neuroSky ������   _/_/_/_/_/");
				neuroSky = new NeuroSky(btAdapter, btHandler);

				mTitle.setText(R.string.title_not_connected);
			}
			result = true;
			break;

		case (R.id.start):

			if (neuroSky != null) {
				// �f�[�^��M�J�n
				neuroSky.start(RAW_DATA_ENABLE);
			} else
				neuroSky = new NeuroSky(btAdapter, btHandler);

			result = true;
			break;

		case (R.id.stop):

			if (neuroSky != null) {
				// �f�[�^��M�I��
				neuroSky.stop();
				mTitle.setText(R.string.stop);
			} else
				neuroSky = new NeuroSky(btAdapter, btHandler);

			result = true;
			break;
		*/
		
		case (R.id.reflesh):{
			
			if ( m_handler != null )
				m_handler.removeCallbacks(m_UpdateRunnable); // �X���b�h��~
			
			if (mConnectedTwitter) {
    			tw = CreateTwitterInstance();
    			RefreshStatusTimeLine();
        	}
        	else if (status != "") {
    			mConnectedTwitter = true;
    			tw = CreateTwitterInstance();
    
    			twitList = GetTwitterTimeLine();
    			ShowTwitterTimeline(twitList);
    		}
			
			
			// Restore preferences
			SharedPreferences settings = getSharedPreferences(PREFERENCE_NAME, 0);
			m_REPEAT_INTERVAL = settings.getInt("FreqTime", m_REPEAT_INTERVAL);
			
			if ( m_handler != null )
				m_handler.postDelayed(m_UpdateRunnable, m_REPEAT_INTERVAL); // �X���b�h�J�n
			
			
			result = true;
			break;
		}
		
		case (R.id.setting): {
			
			if ( m_handler != null )
				m_handler.removeCallbacks(m_UpdateRunnable); // �X���b�h��~
			
			Intent intent_ = new Intent(MainActivity.this, SettingActivity.class);
			startActivity(intent_);

			result = true;
			break;
		}
		

//		case (R.id.log): {
//
//			Intent intent_ = new Intent(MainActivity.this, LogActivity.class);
//			startActivity(intent_);
//
//			result = true;
//			break;
//		}
		}
		return result;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode != KeyEvent.KEYCODE_BACK) {
			return super.onKeyDown(keyCode, event);
		} else {
			// �o�b�N�{�^���̖�����
			return false;
		}
	}
	
	@Override
	public void onLowMemory() {
		//showToast("�����������Ȃ��Ă���");
		m_SQLLOG.Log("MainActivity", "�����������Ȃ��Ă���");
	}

	// NeuroData �Q�b�g
//	public String getNeuroData(android.os.Message msg) {
//
//		NeuroData data = (NeuroData) msg.obj;
//
//		String mNeuroDataStr;
//		mNeuroDataStr = "Attention:" + data.getAttention() + "Meditation:"
//				+ data.getMeditation() + "Delta:" + data.getDelta()
//				+ "HighAlpha:" + data.getHighAlpha() + "HighBeta:"
//				+ data.getHighBeta() + "LowAlpha:" + data.getLowAlpha()
//				+ "LowBeta:" + data.getLowBeta() + "LowGamma:"
//				+ data.getLowGamma() + "MidGamma:" + data.getMidGamma()
//				+ "Theta:" + data.getTheta();
//
//		return mNeuroDataStr;
//
//	}

	// Log�L�^
	public void Log(String message) {
		Log.d(TAG, message);
	}

	public void Log(String TAG, String message) {
		Log.d(TAG, message);
	}

	// ���b�Z�[�W�\��
	private void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

}