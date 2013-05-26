package de.chaosdorf.meteroid.lazylist;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class LazyAdapter extends BaseAdapter
{
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;
	private Activity activity;
	private String[] data;

	public LazyAdapter(Activity a, String[] d)
	{
		activity = a;
		data = d;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(activity.getApplicationContext());
	}

	public int getCount()
	{
		return data.length;
	}

	public Object getItem(int position)
	{
		return position;
	}

	public long getItemId(int position)
	{
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		View vi = convertView;
		/*
		if (convertView == null)
		{
			vi = inflater.inflate(R.layout.activity_list_item, null);
		}

		TextView text = (TextView) vi.findViewById(R.id.text);
		ImageView image = (ImageView) vi.findViewById(R.id.image);
		text.setText("item " + position);
		imageLoader.DisplayImage(data[position], image);
		*/
		return vi;
	}
}