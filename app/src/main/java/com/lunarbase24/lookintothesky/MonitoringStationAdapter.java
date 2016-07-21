package com.lunarbase24.lookintothesky;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Wada on 2016/07/21.
 */
class ViewHolder
{
    public TextView stationname, address ;
    public ImageView iPM25, iOX, iWS;
    public CheckBox sel;
}

public class MonitoringStationAdapter extends ArrayAdapter<Soramame> {
    private LayoutInflater mInflater;
    private boolean mbFlag = true;      // チェックボックスあり(true)、なし(false)

    public MonitoringStationAdapter(Context context, List<Soramame> objects, boolean bFlag){
        super(context, 0, objects);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mbFlag = bFlag;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder ;
        if(convertView == null )
        {
            convertView = mInflater.inflate(R.layout.stationlayout, parent, false);
            holder = new ViewHolder();
            holder.sel = (CheckBox)convertView.findViewById(R.id.select);
            holder.iPM25 = (ImageView)convertView.findViewById(R.id.imagePM25);
            holder.iOX = (ImageView)convertView.findViewById(R.id.imageOX);
            holder.iWS = (ImageView)convertView.findViewById(R.id.imageWS);
            holder.stationname = (TextView)convertView.findViewById(R.id.name);
            holder.address = (TextView)convertView.findViewById(R.id.address);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }
        final Soramame data = getItem(position);

        if(mbFlag) {
            holder.sel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int flag = 0;
                    if (holder.sel.isChecked()) {
                        flag = 1;
                    }
                    data.setSelected(flag);
                    data.setEdit(true);
                }
            });
            holder.sel.setChecked(data.isSelected());
        }else{
            holder.sel.setVisibility(View.INVISIBLE);
        }
        holder.stationname.setText(data.getMstName());
        holder.address.setText(data.getAddress());
        holder.iOX.setImageResource(R.mipmap.ic_launcher_ox_off);
        if(data.getAllow(0)){
            holder.iOX.setImageResource(R.mipmap.ic_launcher_ox_on);
        }
        holder.iPM25.setImageResource(R.mipmap.ic_launcher_pm25_off);
        if(data.getAllow(1)){
            holder.iPM25.setImageResource(R.mipmap.ic_launcher_pm25_on);
        }
        holder.iWS.setImageResource(R.mipmap.ic_launcher_ws_off);
        if(data.getAllow(2)){
            holder.iWS.setImageResource(R.mipmap.ic_launcher_ws_on);
        }

        return convertView;
    }
}
