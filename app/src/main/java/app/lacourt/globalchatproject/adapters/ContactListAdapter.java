package app.lacourt.globalchatproject.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import app.lacourt.globalchatproject.R;
import app.lacourt.globalchatproject.model.Contact;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListViewHolder> {
    private Context context;
    private ContactItemClick contactItemClick;
    private ArrayList<Contact> contacts;

    public ContactListAdapter(ContactItemClick contactItemClick, ArrayList<Contact> contacts) {
        this.context = (Context) contactItemClick;
        this.contactItemClick = contactItemClick;
        this.contacts = contacts;

    }

    @NonNull
    @Override
    public ContactListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactListViewHolder(LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ContactListViewHolder holder, final int position) {
        final Contact contact = contacts.get(position);
        holder.userName.setText(contact.getName());
        holder.userPhone.setText(contact.getPhone());
        if(!contact.getPicture().isEmpty())
            holder.userPicture.setImageBitmap(decodeProfilePicture(contact.getPicture()));
        holder.contectListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactItemClick.onContactClick(contact.getId(), contact.getName());
                Log.d("createchat", "userId = " + contacts.get(position).getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    private Bitmap decodeProfilePicture(String strBase64) {
        byte[] b = Base64.decode(strBase64, Base64.DEFAULT);

        Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        b = null;
        return bitmap;
    }
}