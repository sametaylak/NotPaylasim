package com.sametaylak.notpaylasim;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private String GLOBAL_URL = "http://www.whatsapping.org/";
    private List<Fotograf> mUrls = new ArrayList<>();


    public ImageAdapter(Context c, List<Fotograf> inUrls) {
        mContext = c;
        mUrls = inUrls;
    }

    public int getCount() {
        return mUrls.size();
    }

    public Fotograf getItem(int position) {
        return mUrls.get(position);
    }

    public List<Fotograf> getItems() {
        return mUrls;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null) {
            gridView = new View(mContext);
            gridView = inflater.inflate(R.layout.gridview_layout, null);
        } else {
            gridView = (View) convertView;
        }

        Fotograf url = mUrls.get(position);

        ImageView img = (ImageView) gridView.findViewById(R.id.gridLayoutImage);
        ImageView plus = (ImageView) gridView.findViewById(R.id.plusImg);
        TextView title = (TextView) gridView.findViewById(R.id.gridLayoutTitle);

        title.setText(url.getTitle());

        if(url.getType() != 1) {
            plus.setVisibility(View.GONE);
        }

        Picasso.with(mContext)
                .load(GLOBAL_URL + url.getPhotos().split(",")[0])
                .resize(128,128)
                .centerCrop()
                .placeholder(R.drawable.default_img)
                .into(img);

        return gridView;
    }

}
