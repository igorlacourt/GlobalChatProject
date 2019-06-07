package app.lacourt.globalchatproject.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import app.lacourt.globalchatproject.R;
import app.lacourt.globalchatproject.adapters.MessagesAdapter;
import app.lacourt.globalchatproject.model.Chat;
import app.lacourt.globalchatproject.model.Message;
import app.lacourt.globalchatproject.utils.ConnectivityHelper;
import app.lacourt.globalchatproject.utils.SendNotification;
import app.lacourt.globalchatproject.viewmodel.ChatViewModel;

public class ChatActivity extends AppCompatActivity {
    private EditText messageBodyEditText;
    private ImageButton sendButton;
    private ProgressBar loadingMessages;
    private ImageView userImage;

    private RecyclerView recyclerView;
    private MessagesAdapter messagesAdapter;
    private ArrayList<Message> messageList;

    private String chatId;
    private String userId;
    private String currentUserId;

    private boolean chatExists = false;
    private String userName;
    private ChatViewModel viewModel;
    private LinearLayout lyNoConnection;
    private Button btnRetry;

    private DatabaseReference dbReference;
    private boolean newChatCreated = false;
    private TextView loadingWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        dbReference = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Toolbar toolbar = (Toolbar) findViewById(R.id.conversation_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        userName = getIntent().getStringExtra(getString(R.string.user_name_intent));
        userImage = findViewById(R.id.app_bar_picture);

        TextView title = (TextView) findViewById(R.id.conversation_user_name);
        title.setText(userName);

        messageBodyEditText = (EditText) findViewById(R.id.et_message_body);
        sendButton = (ImageButton) findViewById(R.id.send_button);
        sendButton.setVisibility(View.INVISIBLE);

        loadingWarning = (TextView) findViewById(R.id.loading_warning);
        loadingMessages = (ProgressBar) findViewById(R.id.loading_messages);
        loadingMessages.setVisibility(View.VISIBLE);

        lyNoConnection = (LinearLayout) findViewById(R.id.include_chat_no_connection);
        btnRetry = (Button) findViewById(R.id.btn_retry);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });
        lyNoConnection.setVisibility(View.INVISIBLE);

        viewModel = ViewModelProviders.of(this).get(ChatViewModel.class);

        messageList = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.conversation_recyclerview);
        messagesAdapter = new MessagesAdapter(this, messageList);
        recyclerView.setAdapter(messagesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatId = getIntent().getStringExtra(getString(R.string.chat_id_intent_key));
        userId = getIntent().getStringExtra(getString(R.string.user_id_intent_key));

        Log.d("mymessages", "userId = " + userId);
        Log.d("mymessages", "chatId = " + chatId);
        Log.d("mymessages", "currentUser = " + currentUserId);

        if (userId != null) {
            Log.d("myq", "chat Id = " + chatId);
            Log.d("myq", "user Id = " + userId);
            queryChat();
        } else {
            chatExists = true;
            fetchChatMessages();

        }

        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override

            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
        });
    }

    private void queryChat() {
        if (ConnectivityHelper.isConnectedToNetwork(this)) {
            loadingWarning.setText("Finding chat...");

            Log.d("mymessages", "queryChat() called.");

            Query query = dbReference.child("user")
                    .child(currentUserId)
                    .child("chat")
                    .orderByChild("contact")
                    .equalTo(userId);

            Log.d("myq", "done query!");

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.d("mymessages", "queryChat(), dataSnapshot exists.");
                        String value = dataSnapshot.getValue().toString();
                        String key = dataSnapshot.getKey();

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            String id = child.child("contact").getValue().toString();

                            if (id.equals(userId)) {
                                chatId = child.getKey();
                                chatExists = true;
                                break;
                            } else {
                                chatExists = false;
                            }
                        }

                    } else {
                        Log.d("mymessages", "queryChat(), dataSnapshot DO NOT exists.");
                        chatExists = false;
                    }

                    if (chatExists) {
                        fetchChatMessages();
                    } else {
                        loadingMessages.setVisibility(View.INVISIBLE);
                        loadingWarning.setVisibility(View.INVISIBLE);
                        sendButton.setVisibility(View.VISIBLE);
                        Log.d("mymessages", "Chat doesn't exists.");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            lyNoConnection.setVisibility(View.VISIBLE);
        }
    }

    private void fetchChatMessages() {
        Log.d("mymessages", ".\n");
        Log.d("mymessages", "fetchChatMessages() called.");
        Log.d("mymessages", "fetchChatMessages(), chatExists = " + chatExists + ", chatId = " + chatId);

        loadingMessages.setVisibility(View.VISIBLE);
        loadingWarning.setVisibility(View.VISIBLE);
        loadingWarning.setText("Loading messages...");
        sendButton.setVisibility(View.INVISIBLE);

        if (ConnectivityHelper.isConnectedToNetwork(this)) {

            addListenerToChat();

        } else {
            lyNoConnection.setVisibility(View.VISIBLE);
            Log.d("mymessages", "Not connected to network.");
        }

        sendButton.setVisibility(View.VISIBLE);
    }

    public void sendMessage(View view) {
        Log.d("mymessages", ".\n");
        Log.d("mymessages", "SEND BUTTON PRESSED.");
        Log.d("mymessages", "chatId = " + chatId);

        if (!messageBodyEditText.getText().toString().isEmpty()) {

            if (!chatExists) {
                Log.d("createChat()", "chatExists = " + chatExists);
                createChat();
            }
            if (chatId != null) {
                createMessage();

            } else {
                Log.d("mymessages", "chatId = " + null);
            }

        }

    }

    private void createMessage() {
        DatabaseReference newMessageDb = dbReference.child("chat").child(chatId).push();
        Log.d("mymessages", "message created.");

        final String message = messageBodyEditText.getText().toString();

        Map newMessageMap = new HashMap<>();
        newMessageMap.put("text", message);//messageBodyEditText.getText().toString().isEmpty());
        newMessageMap.put("creator", currentUserId);
        newMessageMap.put("lasM", currentUserId);
        newMessageDb.updateChildren(newMessageMap);

        Log.d("mymessages", "message's text and creator added.");

        HashMap newLastMessageMap = new HashMap();
        newLastMessageMap.put("lastMessage", message);

        // Updating last message
        dbReference.child("chat")
                .child(chatId)
                .child("info")
                .updateChildren(newLastMessageMap);

        sendPushNotification(message);

        messageBodyEditText.setText("");

        if (messageList.isEmpty()) {
            fetchChatMessages();
        }
    }

    private void sendPushNotification(final String message) {
        dbReference.child("user")
                .child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String creatorPhone = dataSnapshot.child("phone").getValue().toString();
                            String creatorName = getContactName(creatorPhone);

                            getContactId(message, creatorName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    private void getContactId(final String message, final String creatorName){
        dbReference.child("chat")
                .child(chatId)
                .child("info")
                .child("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                            for(DataSnapshot id : dataSnapshot.getChildren()) {
                                String[] contactId = new String[1];
                                contactId[0] = id.getKey();

                                if(!contactId[0].contains(currentUserId)) {
                                    contactId[0] = id.getKey();
                                    Log.d("NotificationKeyLog", "onDataChange, selected id: " + contactId[0]);
                                    Log.d("NotificationKeyLog", "onDataChange, current id: " + currentUserId);
                                    getContactNotificationKey(message, creatorName, contactId[0]);
                                }
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getContactNotificationKey(final String message, final String creatorName, final String contactId) {
        dbReference.child("user")
                .child(contactId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String[] notificationKey = new String[1];
                            notificationKey[0] = dataSnapshot.child("notificationKey").getValue().toString();
                            //TODO (1) checar geração de tag com user_id
                            //TODO (2) testar notification com id
                            new SendNotification(message, creatorName, notificationKey[0]);

                            Log.d("NotificationKeyLog", "Results...\n\n");
                            Log.d("NotificationKeyLog", "message: " + message);
                            Log.d("NotificationKeyLog", "creator name: " + creatorName);
                            Log.d("NotificationKeyLog", "notification key: " + notificationKey[0]);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void createChat() {

        Log.d("mymessages", "\n");
        Log.d("mymessages", "createChat() called.");
        final DatabaseReference chat = dbReference.child("chat");
        final DatabaseReference user = dbReference.child("user");

        chatId = chat.push().getKey();//Returns a unique id that doesn't exists inside 'chat' in database.

        Log.d("mymessages", "createChat(), chatId = " + chatId);

        HashMap newChatMap = new HashMap();
        newChatMap.put("id", chatId);
        newChatMap.put("users/" + currentUserId, true);
        newChatMap.put("users/" + userId, true);

        // Creating chats table
        chat.child(chatId)
                .child("info")
                .updateChildren(newChatMap);
        Log.d("mymessages", "Chat table created.");

        //Inserting the contact's id in the current user's chat table
        HashMap newUserChatMap = new HashMap();
        newUserChatMap.put(chatId + "/contact", userId);

        user.child(currentUserId)
                .child("chat")
                .updateChildren(newUserChatMap);
        Log.d("mymessages", "user's chat table created.");

        //Inserting the current user's id in the contact's chat table
        HashMap newContactChatMap = new HashMap();
        newContactChatMap.put(chatId + "/contact", currentUserId);

        user.child(userId)
                .child("chat")
                .updateChildren(newContactChatMap);
        Log.d("mymessages", "contact's chat table created.");

        user.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String phone = dataSnapshot.child("phone").getValue().toString();
                    String picture = "";

                    Object pictureObject = dataSnapshot.child("picture").getValue();
                    if (pictureObject != null) {
                        picture = pictureObject.toString();
                    }

                    viewModel.insert(new Chat(chatId, userId, getContactName(phone), picture));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        chatExists = true;

        newChatCreated = true;
        Intent data = new Intent();
        data.putExtra(getString(R.string.NEW_CHAT_CREATED), newChatCreated);
        setResult(RESULT_OK, data);

        Log.d("createChat()", "chat criado! chatExists = " + chatExists);
        Log.d("mymessages", "\n");

    }

    public String getContactName(final String phoneNumber) {

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName = phoneNumber;
        Cursor cursor = this.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }

    private void addListenerToChat() {
        dbReference.child("chat").child(chatId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                Log.d("mymessages", "dataSnapshot.exists() = " + dataSnapshot.exists());

                if (dataSnapshot.exists() && !dataSnapshot.getKey().equals("info")) {
                    loadingWarning.setText("Loading messages...");
                    String text = "";
                    String creatorId = "";

                    Object newText = dataSnapshot.child("text").getValue();
                    Object newCreatorId = dataSnapshot.child("creator").getValue();

                    if (newText != null) {
                        text = newText.toString();
                    }
                    if (newCreatorId != null) {
                        creatorId = newCreatorId.toString();
                    }

                    String creatorName = "";

                    if (!creatorId.equals(currentUserId))
                        creatorName = userName;
                    else
                        creatorName = creatorId;

                    Message message = new Message(dataSnapshot.getKey(), creatorName, text);
                    messageList.add(message);
                    messagesAdapter.notifyDataSetChanged();

//                        recyclerView.smoothScrollToPosition(messagesAdapter.getItemCount() - 1);
                    recyclerView.scrollToPosition(messageList.size() - 1);

                    loadingMessages.setVisibility(View.INVISIBLE);
                    loadingWarning.setVisibility(View.INVISIBLE);
                    sendButton.setVisibility(View.VISIBLE);
                    Log.d("mymessages", "fetchChatMessages(), end of addChilEventListener");

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();

    }


}
