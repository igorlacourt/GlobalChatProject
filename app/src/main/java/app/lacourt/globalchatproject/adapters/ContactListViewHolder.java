package app.lacourt.globalchatproject.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import app.lacourt.globalchatproject.R;

public class ContactListViewHolder extends RecyclerView.ViewHolder {
    public TextView userName;
    public TextView userPhone;
    public ImageView userPicture;
    public ConstraintLayout contectListItem;

    public ContactListViewHolder(View itemView) {
        super(itemView);
        userName = (TextView) itemView.findViewById(R.id.user_list_name);
        userPhone = (TextView) itemView.findViewById(R.id.user_list_phone);
        userPicture = (ImageView) itemView.findViewById(R.id.user_list_picture);
        contectListItem = (ConstraintLayout) itemView.findViewById(R.id.contact_list_item);
    }

}
