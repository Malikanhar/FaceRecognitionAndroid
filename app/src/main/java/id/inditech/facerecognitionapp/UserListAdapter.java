package id.inditech.facerecognitionapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends ArrayAdapter<User> {
    public UserListAdapter(Context context, List<User> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        User user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_item, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        TextView tvJumlah = convertView.findViewById(R.id.tvJumlah);

        // Populate the data into the template view using the data object
        tvName.setText(user.getNama());
        tvJumlah.setText("Jumlah Data Wajah : " + user.getFaces().size());

        // Return the completed view to render on screen
        return convertView;
    }



}
