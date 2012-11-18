package jp.zanmai.TestTwitter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

public class SettingActivity extends Activity {
	
	public static final String PREFERENCE_NAME = "MyPrefsFile";
	
	//地域・言語
	//private static final int LANGUAGE_JAPANESE = 0;
	//private static final int LANGUAGE_ENGLISH = 1;
	//private static int myLanguage = 0; // 初期は日本語
	private String tweet_way = null;	
	
	private CharSequence[] m_RadioItems = new CharSequence[6];
	private static int m_ClickedItemIndex;
	private RadioGroup way_to_tweet_radioGroup;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		
		TextView guide_setting_tv = (TextView)findViewById(R.id.guide_setting_tv);
		guide_setting_tv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				Intent intent = new Intent(SettingActivity.this, UsageActivity.class);
				startActivity(intent);
				
			}
		});
		
		
		//TextView tv_lang = (TextView)findViewById(R.id.lang_tv);
		TextView tv_logout = (TextView)findViewById(R.id.logout_tv);
		TextView tv_refresh = (TextView)findViewById(R.id.freq_tv);
		way_to_tweet_radioGroup = (RadioGroup)findViewById(R.id.way_to_tweet_radioGroup);
		
		// いきなりつぶやくかいくつかの候補から選ぶかの判断
		SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
		tweet_way = pref.getString("tweet_way", "");
		
		if (tweet_way.equals("confirm"))
			way_to_tweet_radioGroup.check(R.id.way_to_tweet_confirm);
		else if (tweet_way.equals("directly"))
			way_to_tweet_radioGroup.check(R.id.way_to_tweet_directly);

		
		way_to_tweet_radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				SharedPreferences settings = getSharedPreferences(PREFERENCE_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				
				if (group == way_to_tweet_radioGroup) {
					switch (checkedId) {
					case R.id.way_to_tweet_directly:
						editor.putString("tweet_way", "directly");
						//Toast.makeText(SettingActivity.this, "tweet_directly", Toast.LENGTH_SHORT).show();
						break;
					case R.id.way_to_tweet_confirm:
						editor.putString("tweet_way", "confirm");
						//Toast.makeText(SettingActivity.this, "tweet_confirm", Toast.LENGTH_SHORT).show();
						break;
					default:
						break;
					}
				}

				editor.commit();				
			}
		});
		
		
		
		/*
		tv_lang.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Restore preferences
				SharedPreferences settings = getSharedPreferences(PREFERENCE_NAME, 0);
				myLanguage = settings.getInt("Language", LANGUAGE_JAPANESE);
				
		        SharedPreferences.Editor editor = settings.edit();
		        
		        if ( myLanguage == LANGUAGE_JAPANESE )
		        	editor.putInt("Language", LANGUAGE_ENGLISH);
		        else if ( myLanguage == LANGUAGE_ENGLISH )
		        	editor.putInt("Language", LANGUAGE_JAPANESE);

		        editor.commit();
		        
		        myLanguage = settings.getInt("Language", LANGUAGE_JAPANESE);
		        
		        if ( myLanguage == LANGUAGE_JAPANESE )
		        	Toast.makeText(SettingActivity.this, getString(R.string.change_language_to_jp), Toast.LENGTH_SHORT).show();
		        else if ( myLanguage == LANGUAGE_ENGLISH )
		        	Toast.makeText(SettingActivity.this, getString(R.string.change_language_to_en), Toast.LENGTH_SHORT).show();
			}
		});
		*/
		
		tv_logout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Restore preferences
				// 連携状態とトークンの削除
				SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, 0);

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
				
				Intent intent = new Intent(SettingActivity.this, MainActivity.class);
				startActivity(intent);
			}
		});
		
		tv_refresh.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				RadioDialog(); 
			}
		});
	}
	
	private void RadioDialog() {
		
		m_RadioItems[0] = "15s";
		m_RadioItems[1] = "60s";
		m_RadioItems[2] = "2m";
		m_RadioItems[3] = "5m";
		m_RadioItems[4] = "10m";
		m_RadioItems[5] = "15m";
		m_ClickedItemIndex=0;
    	
		new AlertDialog.Builder(SettingActivity.this)
				.setTitle(getString(R.string.freq_refresh))
				.setSingleChoiceItems(m_RadioItems, 0,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								// [item]が選択された時の処理
								m_ClickedItemIndex = item;
								//Toast.makeText(SettingActivity.this, "" + item, Toast.LENGTH_SHORT).show();
							}
						})
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						
						//Toast.makeText(SettingActivity.this, "" + m_ClickedItemIndex, Toast.LENGTH_SHORT).show();
						
						SharedPreferences settings = getSharedPreferences(PREFERENCE_NAME, 0);
						SharedPreferences.Editor editor = settings.edit();
						
						switch ( m_ClickedItemIndex )
						{
							case 0:
							{
								editor.putInt("FreqTime", 30*1000);
								//Toast.makeText(SettingActivity.this, "30", Toast.LENGTH_SHORT).show();
								break;
							}
							case 1:
							{
								editor.putInt("FreqTime", 60*1000);
								//Toast.makeText(SettingActivity.this, "60", Toast.LENGTH_SHORT).show();
								break;
							}
							case 2:
							{
								editor.putInt("FreqTime", 60*2*1000);
								//Toast.makeText(SettingActivity.this, "2", Toast.LENGTH_SHORT).show();
								break;
							}
							case 3:
							{
								editor.putInt("FreqTime", 60*5*1000);
								//Toast.makeText(SettingActivity.this, "5", Toast.LENGTH_SHORT).show();
								break;
							}
							case 4:
							{
								editor.putInt("FreqTime", 60*10*1000);
								//Toast.makeText(SettingActivity.this, "10", Toast.LENGTH_SHORT).show();
								break;
							}
							case 5:
							{
								editor.putInt("FreqTime", 60*15*1000);
								//Toast.makeText(SettingActivity.this, "15", Toast.LENGTH_SHORT).show();
								break;
							}
						}
						
						editor.commit();
						
						//dialog.cancel();
					}
				}).show();
    }
	
}
