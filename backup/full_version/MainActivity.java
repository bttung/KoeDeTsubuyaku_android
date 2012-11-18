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


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
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

public class MainActivity extends Activity {

	public static RequestToken requestToken = null;
	public static OAuthAuthorization OAuth = null;

	public static String consumerKeyStr = "xnss7Rw44vvrHs9Gwto7ZA";
	public static String consumerSecretStr = "Fs2mAPOaBAKmt4bvet7Xgp98Q1yOXtUUtQAtMkGIMpU";
	public String tokenStr, tokenSecretStr = "";

	//地域・言語
	private static final int LANGUAGE_JAPANESE = 0;
	private static final int LANGUAGE_ENGLISH = 1;
	private static int myLanguage = 0; // 初期は日本語
	private static String languageStr = null; 
	

	public static final String PREFERENCE_NAME = "MyPrefsFile";

	private static final String TAG = "TwitterActivity";
	private static final int REQUEST_CODE = 1234;
	private boolean hasFooter = false;
	
	private ArrayList<String> m_speechToChar;

	private List<TwitListItem> twitList = null;

	private Twitter tw = null;
	private String status = null;

	private final String APPDIR_PATH = "TwitterProfileImage"; // microSD以下に作成されるディレクトリ。
	private boolean folderExistFlag, imgExistFlag = false;

	public int pageNumber = 1;
	public int count = 20;

	public ListView listView = null;
	private String myScreenName = null;
	private String item_list[] = null;

	/** ASYNCTASKによる進捗状況 　Progressbar **/
	private static final String ASYNCTASK_TAG = "ASYNC_TASK";
	ProgressDialog mProgressDialog = null;
	static final int DIALOG_ID = 47;

	/** デバッグ関連 */
	private static final String LOGGER_TAG = "NEURO_SKY";
	private static final boolean LOGGER_ENABLE = true;

	/** Intent リクエストコード */
	private static final int REQUEST_CONNECT_DEVICE = 0x01;
	private static final int REQUEST_ENABLE_BT = 0x02;
	
	private static final int DIAG_SELECT_SPEECH_RESULT = 100;

	/** RAW データ要否フラグ */
	private static final boolean RAW_DATA_ENABLE = true;

	/** メンバ変数 */
	private BluetoothAdapter btAdapter;
	private NeuroSky neuroSky;

	// Layout Views
	private TextView mTitle;
	// Name of the connected device
	private String mConnectedDeviceName = "";

	private boolean mConnectedTwitter = false;
	private int changedStatus, sumAttention = 0, meanAttention = 0, dataTransCount = 0;

	private SQLLOGManager m_SQLLOG;

	private final Handler btHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {

			int attention;
			String mindSetMsg = "";

			if (mConnectedTwitter) {

				tw = CreateTwitterInstance();

				switch (msg.what) {
				case (NeuroSky.MESSAGE_BT_DEVICE_NAME):
					mConnectedDeviceName = "MindSet";
					if (LOGGER_ENABLE)
						Log.d(LOGGER_TAG, "[ MESSAGE_BT_DEVICE_NAME ]");
					break;

				case (NeuroSky.MESSAGE_BT_STATUS_CHANGE):
					if (LOGGER_ENABLE)
						Log.d(LOGGER_TAG, "[ MESSAGE_BT_STATUS_CHANGE ]");
					// 変更されたステータス
					changedStatus = msg.arg1;

					if (changedStatus == NeuroSky.BT_STATE_NONE) {
						mTitle.setText(R.string.title_not_connected);
						Log.i(LOGGER_TAG, "BT_STATE_NONE");
					} else if (changedStatus == NeuroSky.BT_STATE_CONNECTING) {
						mTitle.setText(R.string.title_connecting);
						Log.i(LOGGER_TAG, "BT_STATE_CONNECTING");
					} else if (changedStatus == NeuroSky.BT_STATE_CONNECTED) {
						mTitle.setText(R.string.title_connected_to);
						mTitle.append(":MindSet");
						Log.i(LOGGER_TAG, "BT_STATE_CONNECTED"
								+ mConnectedDeviceName);
					}
					break;

				case (NeuroSky.MESSAGE_BT_TOAST):
					if (LOGGER_ENABLE)
						Log.d(LOGGER_TAG, "[ MESSAGE_BT_TOAST ]");
					Toast.makeText(getApplicationContext(),
							msg.getData().getString("toast"),
							Toast.LENGTH_SHORT).show();
					break;

				case (NeuroStreamParser.MESSAGE_READ_DIGEST_DATA_PACKET):
					if (LOGGER_ENABLE)
						Log.d(LOGGER_TAG, "[ MESSAGE_READ_DIGEST_DATA_PACKET ]");
					mTitle.setText(R.string.title_getting_data);

					NeuroData data = (NeuroData) msg.obj;
					attention = data.getAttention();

					sumAttention += attention;
					dataTransCount++;

					if (dataTransCount >= 15) {
						meanAttention = sumAttention / dataTransCount;

						if (meanAttention >= 0 && meanAttention < 20) {
							mindSetMsg = "まだまだだなー";
						} else if (meanAttention >= 20 && meanAttention < 40) {
							mindSetMsg = "お！ちょっと集中してきた。";
						} else if (meanAttention >= 40 && meanAttention < 60) {
							mindSetMsg = "この調子で頑張れ！";
						} else if (meanAttention >= 60 && meanAttention < 80) {
							mindSetMsg = "すごい集中力！！！";
						} else if (meanAttention >= 80 && meanAttention <= 100) {
							mindSetMsg = "集中の達人認定！！！";
						}

						mindSetMsg = meanAttention + ":" + mindSetMsg;
						if (meanAttention > 0)
							EasyUpdateStatusTimeLine(mindSetMsg);

						sumAttention = 0;
						dataTransCount = 0;

						if (meanAttention > 0)
							RefreshStatusTimeLine();
					}

					if (LOGGER_ENABLE)
						Log.i(LOGGER_TAG, getNeuroData(msg));
					break;

				case (NeuroStreamParser.MESSAGE_READ_RAW_DATA_PACKET):
					if (LOGGER_ENABLE)
						Log.d(LOGGER_TAG, "[ MESSAGE_READ_RAW_DATA_PACKET ]");
					NeuroRawData rawData = (NeuroRawData) msg.obj;
					if (LOGGER_ENABLE)
						Log.i(LOGGER_TAG,
								"Raw values : " + rawData.getRawWaveValue());
					break;
				}
			}
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		m_SQLLOG = new SQLLOGManager(getApplicationContext());
		Uri uri = getIntent().getData();
		boolean isAuthenticated = false;

		// コールバックならtokenを登録
		if (uri != null) {
			if (uri.toString().startsWith("Callback://MainActivity")
					&& uri.toString().indexOf("oauth_token") != -1
					&& uri.toString().indexOf("oauth_verifier") != -1) {

				showToast("Callback");
				m_SQLLOG.Log("MainActivity", "uri = " + uri.toString());
				isAuthenticated = RegistToken(uri);
			} else {
				m_SQLLOG.Log("MainActivity", "uri = " + uri.toString());
				disconnectTwitter(); // 解除
			}
		}

		// GUI生成
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

		SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME,
				MODE_PRIVATE);
		status = pref.getString("status", "");

		if (status != "") {
			// showToast("Available");

			m_SQLLOG.Log("MainActivity", "statusあり");

			Button Refresh_bt = (Button) findViewById(R.id.refresh_bt);
			Button LogIn_bt = (Button) findViewById(R.id.login_bt);
			Button Voice_bt = (Button) findViewById(R.id.voice_bt);
			Button Twitter_bt = (Button) findViewById(R.id.Twitter_bt);
			Button changeLanguage_bt = (Button) findViewById(R.id.changeLanguage_bt);

			Refresh_bt.setOnClickListener(refreshOnClickListener);
			LogIn_bt.setOnClickListener(LoginClickListenter);
			Voice_bt.setOnClickListener(voiceRecogListener);
			Twitter_bt.setOnClickListener(tweetListener);
			changeLanguage_bt.setOnClickListener(ChangeLanguageClickListener);

			// 認証済みなのでボタンを消す
			// LogIn_bt.setVisibility(View.GONE);

			// 認証済みなので名前を「sign in」 から「logout」へ変える
			if ( languageStr.equals("ja") ) {
				LogIn_bt.setText("アウト");
				// LogIn_bt.setTextSize(8.0f);
			} else {
				LogIn_bt.setText("logout"); 
			}

		} else {
			showToast("Invisible");
			mConnectedTwitter = false;
			Button LogIn_bt = (Button) findViewById(R.id.login_bt);
			LogIn_bt.setOnClickListener(LoginClickListenter);

			// 余分なインターフェースを消す
			Button Refresh_bt = (Button) findViewById(R.id.refresh_bt);
			Button Voice_bt = (Button) findViewById(R.id.voice_bt);
			Button Twitter_bt = (Button) findViewById(R.id.Twitter_bt);
			Button changeLanguage_bt = (Button) findViewById(R.id.changeLanguage_bt);
			EditText tweet_ed = (EditText) findViewById(R.id.tweet_edt);

			Refresh_bt.setVisibility(View.GONE);
			Voice_bt.setVisibility(View.GONE);
			Twitter_bt.setVisibility(View.GONE);
			changeLanguage_bt.setVisibility(View.GONE);
			tweet_ed.setVisibility(View.GONE);
		}

		// Get local Bluetooth adapter
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		// If the adapter is null, then Bluetooth is not supported
		if (btAdapter == null) {
			showToast("Bluetooth is not available");
			finish();
			return;
		}

		m_SQLLOG.Log("MainActivity", "onCreate()完了");
	}

	private boolean RegistToken(Uri uri) {

		boolean isSuccess = false;

		// oauth_verifierを取得する
		String verifier = uri.getQueryParameter("oauth_verifier");
		try {
			// AccessTokenオブジェクトを取得
			Log.d(TAG, "before get token ");
			// ここでよく落ちた泣く泣く

			m_SQLLOG.Log("CallBackActivity", "token前");

			if (verifier != null) {

				AccessToken token = null;

				m_SQLLOG.Log("CallBackActivity",
						"verifier = " + verifier.toString());

				if (OAuth != null) {
					token = OAuth.getOAuthAccessToken(verifier);
					m_SQLLOG.Log("CallBackActivity", "token取得成功");
					isSuccess = true;
				} else {
					m_SQLLOG.Log("CallBackActivity", "OAuth is null");
				}

				// 連携状態とトークンの書き込み
				SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME,
						MODE_PRIVATE);
				SharedPreferences.Editor editor = pref.edit();

				CharSequence tokenInfo = "";

				if (token != null) {

					tokenStr = token.getToken();
					tokenSecretStr = token.getTokenSecret();
					tokenInfo = "token：" + tokenStr + "\r\n" + "token secret："
							+ tokenSecretStr;

					editor.putString("status", "available");
					editor.putString("oauth_token", token.getToken());
					editor.putString("oauth_token_secret",
							token.getTokenSecret());

					m_SQLLOG.Log("CallBackActivity", "tokenStr=" + tokenStr);
					m_SQLLOG.Log("CallBackActivity", "tokenSecretStr="
							+ tokenSecretStr);

					// Log.d(TAG, token.getToken() + ":"
					// +token.getTokenSecret());
					Log.d(TAG, (String) tokenInfo);
				} else {

					tokenInfo = "token get failed　";
					Log.d(TAG, "token failed");
					showToast("トークンfailed!");

				}

				editor.commit();
			}

			m_SQLLOG.Log("CallBackActivity", "token取得終了");

			Log.d(TAG, "after get token ");
		} catch (TwitterException e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	// Sign inボタン
	View.OnClickListener LoginClickListenter = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			Log.d(TAG, "Login button Clicked");
			SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME,
					MODE_PRIVATE);
			final String nechatterStatus = pref.getString("status", "");

			if (isConnected(nechatterStatus)) {

				showToast("連携済み\n解除します");

				// 連携済みの場合は連携解除
				Log("before disconect");
				disconnectTwitter();
				Log("after disconect");

			} else {
				// 未連携の場合は連携設定
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

	// Twitter連携状態の確認
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

		String tweetContentStr = getTwitEditText();

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

	private void EasyUpdateStatusTimeLine(String twitStr) {
		try {
			Log.d(LOGGER_TAG, "EasyTwit : " + twitStr);
			tw.updateStatus(twitStr);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}

	private void UpdateStatusTimeLine(String twitStr) {

		try {
			Log.d(TAG, "tweet content : " + twitStr);
			tw.updateStatus(twitStr);

		} catch (TwitterException e) {
			e.printStackTrace();
		}

		Log.d(TAG, "tweet OK");

		showToast("tweeted");

		twitList = GetTwitterTimeLine();
		ShowTwitterTimeline(twitList);
	}

	// 言語切替ボタンが押された
	private void ChangeLanguage() {

		if (myLanguage == LANGUAGE_ENGLISH) {

			myLanguage = LANGUAGE_JAPANESE;
			showToast("日本語に切り替えました");

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

				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
						RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); // 自由分指定

				if (myLanguage == LANGUAGE_JAPANESE) {

					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
							Locale.JAPANESE.toString()); // 日本語指定
					// 表示させる文字列
					intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "日本語をどうぞ");

				} else if (myLanguage == LANGUAGE_ENGLISH) {

					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
							Locale.ENGLISH.toString()); // 英語指定
					// 表示させる文字列
					intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
							"English please");

				}

				Log.d(TAG, "start Voice Recog Activity!");
				startActivityForResult(intent, REQUEST_CODE);

			} catch (ActivityNotFoundException e) {

				showToast("ActivityNotFoundException");
				e.printStackTrace();

			}

			Log.d(TAG, "Voice Recognize finish!");

		}
	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (LOGGER_ENABLE)
			Log.d(LOGGER_TAG, "onActivityResult : " + resultCode);

		switch (requestCode) {

		case (REQUEST_CONNECT_DEVICE):
			if (resultCode == Activity.RESULT_OK) {
				String deviceAddress = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				BluetoothDevice device = btAdapter
						.getRemoteDevice(deviceAddress);
				// 対象デバイスと接続

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

		case (REQUEST_CODE):
			if (resultCode == Activity.RESULT_OK) {
				String resultsString = "";

				// 結果文字列リスト
				ArrayList<String> results = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				resultsString = results.get(0);
				
				m_speechToChar = new ArrayList<String>();
				
				for ( int i=0;i<results.size();i++ ) {
					m_speechToChar.add(results.get(i));
				}

				Log.d(TAG, "voice recog: " + resultsString);

				// トーストを使って結果を表示
				//Toast.makeText(MainActivity.this, resultsString,
				//		Toast.LENGTH_LONG).show();
				removeDialog(DIAG_SELECT_SPEECH_RESULT);
				showDialog(DIAG_SELECT_SPEECH_RESULT);

				//UpdateStatusTimeLine(resultsString);

				super.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	// Twitter連携解除
	private void disconnectTwitter() {

		// 連携状態とトークンの削除
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

		// 設定おしまい。
		// finish();

	}

	private void connectTwitter() throws TwitterException {

		// Twitetr4jの設定を読み込む
		Configuration conf = ConfigurationContext.getInstance();

		// デバッグ
		conf.getOAuthAccessToken();
		conf.getOAuthAccessTokenSecret();
		Log.d(TAG, "事前アクセストークン、アクセストークンセクレット: " + conf.getOAuthAccessToken()
				+ " " + conf.getOAuthAccessTokenSecret());

		// Oauth認証オブジェクト作成
		OAuth = new OAuthAuthorization(conf);

		// Oauth認証オブジェクトにconsumerKeyとconsumerSecretを設定
		OAuth.setOAuthConsumer(consumerKeyStr, consumerSecretStr);

		// アプリの認証オブジェクト作成
		try {
			m_SQLLOG.Log("MainActivity", "connectTwitter() start callback");
			Log.d(TAG, "before Start CallbackActivity");
			// requestToken =
			// OAuth.getOAuthRequestToken("Callback://CallBackActivity");
			requestToken = OAuth
					.getOAuthRequestToken("Callback://MainActivity");
			m_SQLLOG.Log("MainActivity", "requestToken取得完了");
			Log.d(TAG, "Start OAuth.getOAuthRequestToken");

		} catch (TwitterException e) {
			Log.d(TAG, "Cannot Start OAuth.getOAuthRequestToken");
			e.printStackTrace();
			throw e;
		}

		String _uri;

		if (requestToken != null) {
			_uri = requestToken.getAuthorizationURL();
			m_SQLLOG.Log("MainActivity", "getAuthorizationURL()完了");
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(_uri)));
			// startActivityForResult(new Intent(Intent.ACTION_VIEW ,
			// Uri.parse(_uri)), 0);
		} else {
			m_SQLLOG.Log("MainActivity", "requestToken is null");
			showToast("失敗");
		}

	}

	public boolean isHasFooter() {
		return hasFooter;
	}

	public void setHasFooter(boolean hasFooter) {
		this.hasFooter = hasFooter;
	}

	// Twitterインスタンスを作成
	public Twitter CreateTwitterInstance() {

		// ﾌﾟﾘﾌｧﾚﾝｽを取得
		SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME,
				MODE_PRIVATE);

		// 型に合わせたgetメソッドで値を取得
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

	// Twitterタイムラインのリストを取得
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

	// Twitterタイムラインのリストを取得
	public List<TwitListItem> EasyGetTwitterTimeLine() {

		Bitmap defaultImage = BitmapFactory.decodeResource(getResources(),
				R.drawable.default_image);
		twitList = new ArrayList<TwitListItem>();

		try {
			// TLの取得
			ResponseList<Status> homeTl;
			// Paging
			Paging paging = new Paging(pageNumber, count);
			Log.d(TAG, "count : " + count);
			// homeTl = tw.getHomeTimeline();

			homeTl = tw.getHomeTimeline(paging);

			for (Status status : homeTl) {

				// つぶやきのユーザーIDの取得
				String userName = status.getUser().getScreenName();
				String imgName = userName + ".png";
				// プロフィールイメージを取得
				String imgURL = status.getUser().getProfileImageURL()
						.toString();
				Log.d(TAG, "imageURL: " + imgURL);

				DownloadOrNot_Image(imgURL, APPDIR_PATH, imgName);

				Log.d(TAG, "before load " + imgName);
				Bitmap userImage = loadImage(APPDIR_PATH, imgName);
				Log.d(TAG, "After load " + imgName);

				// つぶやきの取得
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
				showToast("ネットワークに接続して下さい");
			} else {
				showToast("エラーが発生しました");
			}

		}

		Log.d(TAG, "finished GetTwitterTimeLine()");

		return twitList;

	}

	// Twitter タイムライン を表示させる
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
			elapsedTime = elapsedTimeMinCeiled + "分前";
		else
			elapsedTime = elapsedTimeHour + "時間 " + elapsedTimeMinCeiled + "分前";

		// Log.d(TAG, elapsedTimeHour + "時 " + elapsedTimeMinCeiled + "分前");

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

	// タイムラインをクリックされた時
	ListView.OnItemClickListener ListViewOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			// hideSoftKeyboard();

			if (view.getId() != R.id.Footer) {
				// ListViewにキャスト
				Log.d(TAG, view.getId() + "clicked");
				ListView listView = (ListView) parent;

				// リストアイテムを取得
				TwitListItem item = (TwitListItem) listView
						.getItemAtPosition(position);

				String userName = item.name;
				Log.d(TAG, "選択された項目の名前:" + userName);
				// Log.d(TAG, "選択されたアイテムのcomment:" + item.comment);

				SetTwitEdtTxt("@", userName);

				showToast(item.name + " clicked");

			} else if (view.getId() == R.id.Footer) {

				showToast("読み込んだ");
				count += 5;

				List<TwitListItem> refreshedTwitList = GetTwitterTimeLine();
				ShowTwitterTimeline(refreshedTwitList);

				listView.setSelectionFromTop(count, 100);

			}
		}
	};

	// タイムライン長押し
	AdapterView.OnItemLongClickListener ListViewOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {

			Toast.makeText(MainActivity.this, " Long clicked",
					Toast.LENGTH_SHORT).show();
			ClearTwitEdtTxt();
			hideSoftKeyboard();

			ListView listView = (ListView) parent;

			// リストアイテムを取得
			TwitListItem item = (TwitListItem) listView
					.getItemAtPosition(position);
			final String tmpUserName = item.name;
			final String tmpTwit = item.comment;
			final long tmpId = item.id;

			if (view.getId() != R.id.Footer) {
				if (tmpUserName.equals(myScreenName)) {
					item_list = new String[] { "Favorite", "Reply", "Delete" };
					// item_list = new String[] {"Reply", "Delete" };
				} else {
					item_list = new String[] { "Favorite", "ReTweet", "Reply" };
					// item_list = new String[] {"ReTweet", "Reply" };
				}

				// 処理：メッセージダイアログの表示
				new AlertDialog.Builder(MainActivity.this).setItems(item_list,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

								if (item_list[which].equals("Favorite")) {

									showToast("Favorite");

									// Twitter tw = CreateTwitterInstance();
									// tw = CreateTwitterInstance();

									try {
										tw.createFavorite(tmpId);
									} catch (TwitterException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								} else if (item_list[which].equals("Reply")) {

									showToast("Reply");
									SetTwitEdtTxt("@", tmpUserName);

								} else if (item_list[which].equals("ReTweet")) {

									showToast("ReTweet Clicked");
									UpdateStatusTimeLine("@" + tmpUserName
											+ " " + tmpTwit);

								} else if (item_list[which].equals("Delete")) {

									showToast("Delete Clicked");

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

	// あるURLからビットマップイメージをゲットする
	public Bitmap getBitmap(URL bitmapUrl) {

		try {
			return BitmapFactory.decodeStream(bitmapUrl.openConnection()
					.getInputStream());
		} catch (Exception ex) {
			return null;
		}

	}

	// アクセストークンを取得する
	public void getAccessToken(String tokenStr, String tokenSecretStr) {

		// ﾌﾟﾘﾌｧﾚﾝｽを取得
		SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME,
				MODE_PRIVATE);

		// 型に合わせたgetメソッドで値を取得
		tokenStr = pref.getString("oauth_token", "");
		tokenSecretStr = pref.getString("oauth_token_secret", "");

	}

	// あるフォルダからあるイメージを読み込む
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

	// メモリにあるフォルダがあるかどうか
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

	// あるフォルダにあるファイルがあるかどうか？
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

	// プロフィルイメージの有無によって、ダウンロードするかしない
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
		    	builder.setTitle("認識結果");
		    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// 何もしない
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
		if (mConnectedTwitter)
			tw = CreateTwitterInstance();

		if (btAdapter.isEnabled()) {
			neuroSky = new NeuroSky(btAdapter, btHandler);
		} else {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, REQUEST_ENABLE_BT);
		}

		hideSoftKeyboardOnStart();

		if (status != "") {
			mConnectedTwitter = true;
			tw = CreateTwitterInstance();

			twitList = GetTwitterTimeLine();
			ShowTwitterTimeline(twitList);

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

		case (R.id.scan):

			neuroSky = new NeuroSky(btAdapter, btHandler);

			Intent intent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
			result = true;
			break;

		case (R.id.clear):

			if (neuroSky != null) {
				// データ受信終了
				neuroSky.stop();
				// 接続終了
				neuroSky.close();

				Log.d(LOGGER_TAG, "_/_/_/_/_/   neuroSky 初期化   _/_/_/_/_/");
				neuroSky = new NeuroSky(btAdapter, btHandler);

				mTitle.setText(R.string.title_not_connected);
			}
			result = true;
			break;

		case (R.id.start):

			if (neuroSky != null) {
				// データ受信開始
				neuroSky.start(RAW_DATA_ENABLE);
			} else
				neuroSky = new NeuroSky(btAdapter, btHandler);

			result = true;
			break;

		case (R.id.stop):

			if (neuroSky != null) {
				// データ受信終了
				neuroSky.stop();
				mTitle.setText(R.string.stop);
			} else
				neuroSky = new NeuroSky(btAdapter, btHandler);

			result = true;
			break;

		case (R.id.log): {

			Intent intent_ = new Intent(MainActivity.this, LogActivity.class);
			startActivity(intent_);

			result = true;
			break;
		}
		}
		return result;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode != KeyEvent.KEYCODE_BACK) {
			return super.onKeyDown(keyCode, event);
		} else {
			// バックボタンの無効化
			return false;
		}
	}

	// NeuroData ゲット
	public String getNeuroData(android.os.Message msg) {

		NeuroData data = (NeuroData) msg.obj;

		String mNeuroDataStr;
		mNeuroDataStr = "Attention:" + data.getAttention() + "Meditation:"
				+ data.getMeditation() + "Delta:" + data.getDelta()
				+ "HighAlpha:" + data.getHighAlpha() + "HighBeta:"
				+ data.getHighBeta() + "LowAlpha:" + data.getLowAlpha()
				+ "LowBeta:" + data.getLowBeta() + "LowGamma:"
				+ data.getLowGamma() + "MidGamma:" + data.getMidGamma()
				+ "Theta:" + data.getTheta();

		return mNeuroDataStr;

	}

	// Log記録
	public void Log(String message) {
		Log.d(TAG, message);
	}

	public void Log(String TAG, String message) {
		Log.d(TAG, message);
	}

	// メッセージ表示
	private void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

}