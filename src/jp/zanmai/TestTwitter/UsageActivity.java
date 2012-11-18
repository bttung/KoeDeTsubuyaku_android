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
				"�y�g�����z\n" +
				"�܂���Twitter�F�؂����ĉ������B\n" +
				"�F�،�A�}�C�N�̃{�^���������A�g�тɌ������ĂԂ₭�Ƃ��̓��e��Twitter�Ɏ������e����܂��B\n" + 
				"�����͓��{��E�p��̐؂�ւ����ł��܂��̂ŁA�p���Twitter�ɂ����e�ł��܂��B\n\n" +
				"�y��������z\n" +
				"�{�^������\n" +
				"�u�}�C�N�v�����̃{�^���������Čg�тɌ������ĂԂ₢�ĉ������B\n" +
				"�u����v�����{��Ɖp��ɐ؂�ւ��܂��B\n" +
				"�u�Ԃ₭�v����������͂��ĂԂ₫�������ɉ����ĉ������B\n" +
				"�E���c�C�[�g�A�ԐM���Y������Ԃ₫�𒷉���������{�^�����\������܂��B\n"+
				"�E�X�V�p�x�ݒ�E���O�A�E�g�����j���[�{�^�����瑀�삵�ĉ������B\n";
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
