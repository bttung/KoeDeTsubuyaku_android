package jp.zanmai.TestTwitter;

import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class UsageActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usage);
        
        String languageStr = Locale.getDefault().getLanguage().toString();

        TextView usage_tv = (TextView) findViewById(R.id.usage);

        if ( languageStr.equals("ja") ) {
			String usageStr = 
				"【使い方】\n" +
				"まずはTwitter認証をして下さい。\n" +
				"認証後、マイクのボタンを押し、携帯に向かってつぶやくとその内容がTwitterに自動投稿されます。\n" + 
				"音声は日本語・英語の切り替えができますので、英語でTwitterにも投稿できます。\n\n" +
				"【操作説明】\n" +
				"ボタン説明\n" +
				"「マイク」→このボタンを押して携帯に向かってつぶやいて下さい。\n" +
				"「言語」→日本語と英語に切り替えます。\n" +
				"「つぶやく」→文字を入力してつぶやきたい時に押して下さい。\n" +
				"・リツイート、返信→該当するつぶやきを長押ししたらボタンが表示されます。\n"+
				"・更新頻度設定・ログアウト→メニューボタンから操作して下さい。\n";
			usage_tv.setText(usageStr);	
		}
        
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
    }

	@Override
	protected void onPause () {
		super.onPause();
	}
	
	@Override
	protected void onRestart () {
		super.onRestart();
	}
	
}
