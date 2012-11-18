package jp.zanmai.TestTwitter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class LogActivity extends Activity {
	
	ListView m_listView;
	
	SQLLOGManager m_SQLLOG;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log);

        m_SQLLOG = new SQLLOGManager(getApplicationContext());
        TimeSort();
    }
	
	private void TimeSort() {
		List<String> arr = new ArrayList<String>();
        
        Cursor c = m_SQLLOG.GetSortTime();
        String tag, mes, strTime;
        long tm;
        SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
        
        if ( c.moveToFirst() ) {
        	
        	do {
        		tag = c.getString(c.getColumnIndex("tag"));
        		mes = c.getString(c.getColumnIndex("mes"));
        		tm  = c.getLong(c.getColumnIndex("time"));
        		strTime = sdf1.format(tm);
        		arr.add(strTime + "\n" + tag + "\n" + mes);
        	} while ( c.moveToNext() );
        }
        
        m_listView = (ListView)findViewById(R.id.listview_db);
        setAdapters(m_listView, arr);
	}
	
	private void TagSort() {
		List<String> arr = new ArrayList<String>();
	    
	    Cursor c = m_SQLLOG.GetSortTag();
	    String tag, mes, strTime;
	    long tm;
	    SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
	    
	    if ( c.moveToFirst() ) {
	    	
	    	do {
	    		tag = c.getString(c.getColumnIndex("tag"));
	    		mes = c.getString(c.getColumnIndex("mes"));
	    		tm  = c.getLong(c.getColumnIndex("time"));
	    		strTime = sdf1.format(tm);
	    		arr.add(strTime + "\n" + tag + "\n" + mes);
	    	} while ( c.moveToNext() );
	    }
	    
	    m_listView = (ListView)findViewById(R.id.listview_db);
	    setAdapters(m_listView, arr);
	}
	
	// �I�v�V�������j���[���ŏ��ɌĂяo����鎞��1�x�����Ăяo����܂�
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // ���j���[�A�C�e����ǉ����܂�
        menu.add(Menu.NONE, 1, Menu.NONE, "����");
        menu.add(Menu.NONE, 2, Menu.NONE, "�^�O");
        menu.add(Menu.NONE, 3, Menu.NONE, "�폜");
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;
        switch (item.getItemId()) {
	        default:
	        {
	            ret = super.onOptionsItemSelected(item);
	            break;
	        }
	        case 1: // ����
	        {
	            ret = true;
	            TimeSort();
	            break;
	        }
	        case 2: // �^�O
	        {
	            ret = true;
	            TagSort();
	            break;
	        }
	        case 3: // �폜
	        {
	            ret = true;
	            m_SQLLOG.DeleteData();
	            TimeSort();
	        }
        }
        return ret;
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
	
	private void setAdapters(ListView lv, List<String> objects) {
		
		ArrayAdapter<String> adapt = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, objects);
		lv.setAdapter(adapt);
	}
}
