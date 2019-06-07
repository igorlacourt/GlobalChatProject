package app.lacourt.globalchatproject.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import app.lacourt.globalchatproject.R;
import app.lacourt.globalchatproject.adapters.ChatAdapter;
import app.lacourt.globalchatproject.adapters.ChatItemClick;
import app.lacourt.globalchatproject.model.Chat;
import app.lacourt.globalchatproject.utils.ConnectivityHelper;
import app.lacourt.globalchatproject.utils.CustomNotificationReceivedHandler;
import app.lacourt.globalchatproject.utils.DialogHandler;
import app.lacourt.globalchatproject.utils.MySharedPreferences;
import app.lacourt.globalchatproject.utils.WidgetUpdater;
import app.lacourt.globalchatproject.viewmodel.MainScreenViewModel;

public class MainScreenActivity extends AppCompatActivity implements ChatItemClick {
    public static final int NEW_PICTURE_REQUEST = 72;

    private RecyclerView recyclerView;
    private FloatingActionButton newChatButton;
    private ProgressBar loadingChats;
    private TextView noChatTextView;

    private ChatAdapter adapter;
    DialogHandler dialogHandler = null;

    ArrayList<Chat> chatList;

    private MainScreenViewModel viewModel;

    private Button btnRetry;
    private LinearLayout lyNoConnection;

    private DatabaseReference dbReference;
    private String currentUserId;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_screen_toolbar);
        setSupportActionBar(toolbar);

        dbReference = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();

        initializeOneSignal();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        loadingChats = (ProgressBar) findViewById(R.id.loading_chats_progress_bar);
        loadingChats.setVisibility(View.VISIBLE);
        noChatTextView = (TextView) findViewById(R.id.no_chats_text_view);
        noChatTextView.setVisibility(View.INVISIBLE);

        lyNoConnection = (LinearLayout) findViewById(R.id.include_main_screen_no_connection);
        btnRetry = (Button) findViewById(R.id.btn_retry);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });
        lyNoConnection.setVisibility(View.INVISIBLE);

        newChatButton = (FloatingActionButton) findViewById(R.id.new_chat_button);
        newChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainScreenActivity.this,
                        ChooseContactActivity.class), getResources().getInteger(R.integer.CHOOSE_CONTACT_RESULT));
            }
        });

        dialogHandler = DialogHandler.getInstance();

        chatList = new ArrayList<Chat>();
        recyclerView = (RecyclerView) findViewById(R.id.channels_recyclerview);
        adapter = new ChatAdapter(this, chatList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewModel = ViewModelProviders.of(this).get(MainScreenViewModel.class);

        viewModel.getAllChats();
        viewModel.allChats.observe(this, new Observer<ArrayList<Chat>>() {
            @Override
            public void onChanged(@Nullable final ArrayList<Chat> chats) {
                Log.d("mydelete", "onChanged called.");

                if (chats != null && !chats.isEmpty()) {
                    Log.d("opennoconection", "chats from db NOT EMPTY ");

                    //TODO insert the uid in the shared pref file.
                    SharedPreferences sharedPref = MainScreenActivity.this.getPreferences(Context.MODE_PRIVATE);
                    String userId = sharedPref.getString(getString(R.string.uid_key), currentUserId);

                    if(!userId.equals(currentUserId)) {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.uid_key), currentUserId);
                        editor.apply();
                        viewModel.deleteAllChats();

                    } else {
                        chatList.clear();
                        chatList.addAll(chats);
                        adapter.notifyDataSetChanged();
                        loadingChats.setVisibility(View.INVISIBLE);
                        noChatTextView.setVisibility(View.INVISIBLE);
                    }

                    for (Chat dbChat : chats) {
                        Log.d("opennoconection", "dbChat = " + dbChat.getName());
                    }
                    Log.d("opennoconection", "\n");



                } else {
                    Log.d("opennoconection", "chats from db IS EMPTY ");
                    getPermissionAndFetchChats();
                }

            }
        });

    }

    private void initializeOneSignal() {
        OneSignal.startInit(this)
                .setNotificationReceivedHandler(new CustomNotificationReceivedHandler())
                .init();
        OneSignal.setSubscription(true);
//        OneSignal.sendTag("User_Id", currentUserId);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                dbReference.child("user")
                        .child(currentUserId)
                        .child("notificationKey")
                        .setValue(userId);
            }
        });
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);
        MySharedPreferences.init(this);
        WidgetUpdater.init(this);

    }

    private void getPermissionAndFetchChats() {
        Log.d("opennoconection", "\n getPermissionAndFetchChats called.");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, getResources().getInteger(R.integer.PERMISSIONS_REQUEST_READ_CONTACTS));
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {

            fetchChatList();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == getResources().getInteger(R.integer.PERMISSIONS_REQUEST_READ_CONTACTS)) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                getPermissionAndFetchChats();
            } else {
                Toast.makeText(this, "We can't continue without the permission.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchChatList() {

        Log.d("opennoconection", "fetchChatList() called.");

        if (ConnectivityHelper.isConnectedToNetwork(this)) {

            Log.d("opennoconection", "App IS connected.");

            chatList.clear();

            DatabaseReference userChatDb = dbReference.child("user")
                    .child(currentUserId)
                    .child("chat");

            userChatDb.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {

                        for (DataSnapshot chat : dataSnapshot.getChildren()) {
                            String chatId = chat.getKey();
                            String contactId = chat
                                    .child("contact")
                                    .getValue().toString();
                            Log.d("mycontacts", "chatId = " + chatId + ", contactId = " + contactId);
                            chatList.add(new Chat(chatId, contactId, "", ""));
                        }

                        fetchContactInfo();

                    } else {
                        adapter.notifyDataSetChanged();
                        loadingChats.setVisibility(View.INVISIBLE);
                        noChatTextView.setVisibility(View.VISIBLE);
                        Log.d("mycontacts", "loading contacts failed.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {
            Log.d("opennoconection", "App NOT connected.");
            loadingChats.setVisibility(View.INVISIBLE);
            lyNoConnection.setVisibility(View.VISIBLE);

        }
    }

    private void fetchContactInfo() {

        for (final Chat chat : chatList) {

            Log.d("fetchchats", "chatList.size = " + chatList.size());

            Query userDb = dbReference.child("user")
                    .child(chat.getContactId());

            userDb.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        String phone = dataSnapshot.child("phone").getValue().toString();
                        String name = getContactName(phone);
                        String picture = "";

                        Object pictureObject = dataSnapshot.child("picture").getValue();
                        if (pictureObject != null)
                            picture = pictureObject.toString();

                        Log.d("fetchchats", "phone = " + phone + ", picture = " + picture);

                        chat.setName(name);
                        chat.setProfilePicture(picture);
                        adapter.notifyDataSetChanged();

                        Log.d("opennoconection", "insert called for: " + chat.getName());
                        viewModel.insert(chat);

                    } else {
                        Log.d("fetchchats", "no dataSnapshot for .child(\"user\")\n" +
                                "                                       .child(chat.getContactId()");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        loadingChats.setVisibility(View.INVISIBLE);
    }

    //Credit for the answer on this question: https://stackoverflow.com/questions/3079365/android-retrieve-contact-name-from-phone-number
    public String getContactName(final String phoneNumber) {

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName = "";
        Cursor cursor = this.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }


    @Override
    public void onChatClick(String name, String chatId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(getString(R.string.user_name_intent), name);
        intent.putExtra(getString(R.string.chat_id_intent_key), chatId);

        startActivity(intent);
    }

    @Override
    public void onChatLongClick(Chat chat) {
        dbReference
                .child("chat")
                .child(chat.getChatId())
                .removeValue();

        dbReference
                .child("user")
                .child(currentUserId)
                .child("chat")
                .child(chat.getChatId())
                .removeValue();

        dbReference
                .child("user")
                .child(chat.getContactId())
                .child("chat")
                .child(chat.getChatId())
                .removeValue();

        Log.d("mydelete", "chat deleted from Firebase.");
        viewModel.deleteChat(chat);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);

        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setQueryHint("Search chat...");
        int searchPlateId = searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchView.findViewById(searchPlateId);
        if (searchPlate != null) {
            int searchTextId = searchPlate.getContext()
                    .getResources()
                    .getIdentifier("android:id/search_src_text", null, null);
            TextView searchText = (TextView) searchPlate.findViewById(searchTextId);
            if (searchText != null) {
                searchText.setTextColor(Color.WHITE);
                searchText.setHintTextColor(Color.WHITE);
            }
        }
        searchView.setIconified(true);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.option_logout:
                logout();
                break;
            case R.id.picture:
                if (currentUser != null) {
                    Intent intent = new Intent(this, ProfilePictureActivity.class);
                    startActivityForResult(intent, getResources().getInteger(R.integer.PICTURE_RESULT));
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == getResources().getInteger(R.integer.PICTURE_RESULT))
            viewModel.getAllChats();

        else if (requestCode == getResources().getInteger(R.integer.CHOOSE_CONTACT_RESULT)) {
            Log.d("getchats", "Main, requestCode OK");
            if (data != null && data.getExtras().getBoolean(getString(R.string.NEW_CHAT_CREATED))) {
                Log.d("getchats", "Main, data OK");
                viewModel.getAllChats();
            }

        }

    }

    private void logout() {


        if (currentUser != null) {
            FirebaseAuth.getInstance().signOut();
            OneSignal.setSubscription(false);

            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("myonstart", "onStart called.");
    }

    private Bitmap decodeProfilePicture(String strBase64) {

        byte[] b = Base64.decode(strBase64, Base64.DEFAULT);

        Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        b = null;

        return bitmap;
    }

}
