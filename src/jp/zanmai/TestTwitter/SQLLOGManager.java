package jp.zanmai.TestTwitter;

import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import android.content.Context;

@SuppressWarnings("unused")
public class SQLLOGManager {
/*
	private static final String DB_NAME = "LOG.db";
	private static final int DB_VERSION = 1;
	
	private static SQLiteDatabase sdb;
	private static boolean isOpened;
	private static String LastErrorMessage="";
	
	public SQLLOGManager(Context context) {
		
		SQL_Manager_Helper helper = new SQL_Manager_Helper(context);
		
		try {
			sdb = helper.getWritableDatabase();
			//sdb = helper.getReadableDatabase();
			isOpened=true;
		} catch ( SQLiteException e ) {
			//異常終了
			isOpened=false;
			//return;
		}
	}
	*/
	public void Log(String tag, String mes) {
		
/*		if ( isOpened )
			sdb.execSQL("insert into table1 values('" + tag + "', '" +
														mes + "',  " +
														System.currentTimeMillis() + ")"
														);
		*/
	}
	
	/*
	public Cursor GetSortTime() {
		
		if ( isOpened )
			return sdb.rawQuery("select * from table1 order by time", null);
		
		return null;
	}
	
	public Cursor GetSortTag() {
		
		if ( isOpened )
			return sdb.rawQuery("select * from table1 order by tag", null);
		
		return null;
	}
	
	public void DeleteData() {
		
		if ( isOpened )
			sdb.execSQL("delete from table1");
	}
	
	public int getDataNum() {
		
		Cursor c = sdb.rawQuery("select * from table1", null);
		return c.getCount();
	}
	
	// DBのオープンが成功したか
	public boolean isOpened() {
		return isOpened;
	}
	
	public String getError() {
		return LastErrorMessage;
	}

 
	// DBオープンのためのヘルパークラス
	public class SQL_Manager_Helper extends SQLiteOpenHelper {
		
	    public SQL_Manager_Helper(Context context) {
	        super(context, DB_NAME, null, DB_VERSION);
	    }
	 
	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	        // TODO Auto-generated method stub
	    }
	 
	    @Override
	    public void onCreate(SQLiteDatabase db) {
	        // table create
	    	
	    	String str = "create table table1 ( " +
								    	"tag text ," +
								    	"mes text ," +
								    	"time long " +
								    	");";
	    	
	    	LastErrorMessage = str;
	        
		    db.execSQL(str);
	    }
	}*/
}
