package id.inditech.facerecognitionapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class HistoryListAdapter extends ArrayAdapter<User> {
    public HistoryListAdapter(Context context, List<User> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        User user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.history_item, parent, false);
        }
        // Lookup view for data population
        TextView mName = convertView.findViewById(R.id.tvName);
        TextView mDate = convertView.findViewById(R.id.tvDate);
        TextView mTime = convertView.findViewById(R.id.tvTime);

        // Populate the data into the template view using the data object
        mName.setText(user.getNama());
        mDate.setText(user.getTanggal());
        mTime.setText(user.getWaktu());


        // Return the completed view to render on screen
        return convertView;
    }



}
