package jp.zanmai.TestTwitter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TwitListItemAdapter extends  ArrayAdapter<TwitListItem>{

	private LayoutInflater mInflater;
	
	public TwitListItemAdapter(Context context, int textViewResourceId, List<TwitListItem> list) {
		super(context, textViewResourceId, list);
		// TODO Auto-generated constructor stub
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
	}
	
	public View getView (int position , View convertView, ViewGroup parent) {
		
		TwitListItem item  = (TwitListItem)getItem(position);
		
		View view  = mInflater.inflate(R.layout.twit_list_item, null);
		
		ImageView image  =  (ImageView)view.findViewById(R.id.image);
		image.setImageBitmap(item.image);
		
		TextView name  = (TextView)view.findViewById(R.id.name);
		name.setText(item.name);
		
		TextView comment  =  (TextView)view.findViewById(R.id.comment);
		comment.setText(item.comment);
		
		TextView time  =  (TextView)view.findViewById(R.id.time);
		time.setText(item.time);
		
		return view;
		
	}
}
