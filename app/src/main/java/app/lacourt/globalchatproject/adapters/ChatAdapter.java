package app.lacourt.globalchatproject.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import app.lacourt.globalchatproject.R;
import app.lacourt.globalchatproject.model.Chat;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> implements Filterable {
    private Context context;
    private ArrayList<Chat> chats;
    private ArrayList<Chat> chatsFiltered;
    private ChatItemClick chatItemClick;

    public ChatAdapter(ChatItemClick chatItemClick, ArrayList<Chat> chats) {
        this.context = (Context) chatItemClick;
        this.chats = chats;
        this.chatsFiltered = new ArrayList<>();
        this.chatItemClick = chatItemClick;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, final int position) {
        final Chat chat = chats.get(position);
        holder.name.setText(chat.getName());
        holder.listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatItemClick.onChatClick(chat.getName(), chat.getChatId());
            }
        });
        holder.listItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete chat")
                        .setMessage("Are you sure you want to delete this chat?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                                chatItemClick.onChatLongClick(chat);

                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
//                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return false;
            }
        });

        if(!chat.getProfilePicture().isEmpty())
            holder.userPicture.setImageBitmap(decodeProfilePicture(chat.getProfilePicture()));
//        holder.lastMessage.setText(chat.getLastMessage());
    }

    private Bitmap decodeProfilePicture(String strBase64) {
        byte[] b = Base64.decode(strBase64, Base64.DEFAULT);

        Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        b = null;

        return bitmap;
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public void setchats(ArrayList<Chat> chats) {
        this.chats = chats;
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        public ImageView userPicture;
        public TextView name;
        public ConstraintLayout listItem;
//        public TextView lastMessage;

        public ChatViewHolder(View itemView) {
            super(itemView);
            userPicture = (ImageView) itemView.findViewById(R.id.chat_list_profile_picture);
            name = (TextView) itemView.findViewById(R.id.chat_list_name);
            listItem = (ConstraintLayout) itemView.findViewById(R.id.chat_list_item);
//            lastMessage = (TextView) itemView.findViewById(R.id.recylcerview_contact_last_message);
        }

    }

    public void filter(String text) {
        if (chats != null) {
            Log.d("myfilter", "chats NOT NULL");

            if(chatsFiltered.isEmpty())
                chatsFiltered.addAll(chats);

            int i = 0;
            for(Chat chat : chatsFiltered){
                Log.d("myfilter", "chatsFiltered[" + i + "] = " + chat.getName());
                i++;
            }

            chats.clear();

            if (!text.isEmpty()) {

                String lowerCaseText = text.toLowerCase();
                Log.d("myfilter", "LowerCaseText = " + lowerCaseText);

                boolean addChat = true;

                for (Chat chat : chatsFiltered) {

                    String lowerCaseName = chat.getName().toLowerCase();

                    Log.d("myfilter", "LowerCaseName = " + lowerCaseName);

                    if (lowerCaseName.contains(text)) {
                        chats.add(chat);
                        Log.d("myfilter", "" + lowerCaseName + " added to chats");
                    }

                }
            } else {
                chats.addAll(chatsFiltered);
            }
            notifyDataSetChanged();
        }

    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    chatsFiltered = chats;
                } else {
                    ArrayList<Chat> filteredList = new ArrayList<>();
                    for (Chat row : chats) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    chatsFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = chatsFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                chatsFiltered = (ArrayList<Chat>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}

