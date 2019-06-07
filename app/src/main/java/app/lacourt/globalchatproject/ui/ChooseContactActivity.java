package app.lacourt.globalchatproject.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import app.lacourt.globalchatproject.R;
import app.lacourt.globalchatproject.adapters.ContactItemClick;
import app.lacourt.globalchatproject.adapters.ContactListAdapter;
import app.lacourt.globalchatproject.model.Contact;
import app.lacourt.globalchatproject.utils.ConnectivityHelper;
import app.lacourt.globalchatproject.utils.CountryToPhonePrefix;

public class ChooseContactActivity extends AppCompatActivity implements ContactItemClick {
    private RecyclerView recyclerView;
    private ContactListAdapter contactListAdapter;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<Contact> contactList;
    private ArrayList userList;
    private ActionBar actionBar;
    private ProgressBar loadingUsers;
    private Button btnRetry;
    private LinearLayout lyNoConnection;
    private DatabaseReference dbReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_contact);

        dbReference = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_screen_toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle(getString(R.string.choose_contact_title));
        loadingUsers = (ProgressBar) findViewById(R.id.loading_users);
        loadingUsers.setVisibility(View.VISIBLE);

        contactList = new ArrayList();
        userList = new ArrayList();

        lyNoConnection = (LinearLayout) findViewById(R.id.include_choose_contact_no_connection);
        btnRetry = (Button) findViewById(R.id.btn_retry);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });
        lyNoConnection.setVisibility(View.INVISIBLE);

        initilizeRecycyclerView();
        getContactList();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == getResources().getInteger(R.integer.CHAT_RESULT)) {
            boolean extra = getIntent().getBooleanExtra(getString(R.string.NEW_CHAT_CREATED), false);
            setResult(getResources().getInteger(R.integer.CHOOSE_CONTACT_RESULT), data);
            Log.d("getchats", "Choose contact, requestCode OK");
        }

    }

    //Get contact list from the device
    private void getContactList() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, getResources().getInteger(R.integer.PERMISSIONS_REQUEST_READ_CONTACTS));
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {

            getContacts();
        }


    }

    private void getContacts() {
        String ISOPrefix = getCountryISO();
        //CommonDataKinds
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        if (phones != null) {
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                phone = phone.replace(" ", "");
                phone = phone.replace("-", "");
                phone = phone.replace("(", "");
                phone = phone.replace(")", "");

                Log.d("userlist", "ChooseContactActivity, from cellphone: name = " + name + ", phone = " + phone);

                getUserDetails(name, phone);
                Log.d("contacts", "calling getContactDetails...");
            }
            phones.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == getResources().getInteger(R.integer.PERMISSIONS_REQUEST_READ_CONTACTS)) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                getContactList();
            } else {
                Toast.makeText(this, "We can't continue without the permission.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getContactDetails(final String name, final String phone) {

        Query query = dbReference.child("user").orderByChild("phone").equalTo(phone);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("contacts", "getContactDetails, dataSnapshot NOT NULL");
                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        String contactId = user.getKey();
                        String picture = user.child("picture").getValue().toString();

                        Log.d("contacts", "getContactDetails, contactId = " + contactId + "\n" +
                                "                                           picture = " + picture);

                        Contact contact = new Contact(contactId, name, phone, picture);
                        contactList.add(contact);
                    }
                } else {
                    Log.d("contacts", "getContactDetails, dataSnapshot NULL");
                }
                loadingUsers.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Fetch the data from the database
    private void getUserDetails(final String name, final String phone) {

        if (ConnectivityHelper.isConnectedToNetwork(this)) {
            Query query = dbReference.child("user").orderByChild("phone").equalTo(phone);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.d("createchat", "name = " + name + ", userId = " + dataSnapshot.getKey());

                        String s = dataSnapshot.getValue().toString();
                        String userId = s.substring(s.indexOf("{") + 1, s.indexOf("="));

                        String encodedPicture = "";
                        Object pictureObject = dataSnapshot.child(userId).child("picture").getValue();
                        if (pictureObject != null) {
                            encodedPicture = pictureObject.toString();
                        }

                        Contact contact = new Contact(userId, name, phone, encodedPicture);

                        boolean listContainsUser = false;

                        for (Contact item : contactList) {
                            if (item.getId().equals(contact.getId())) {
                                listContainsUser = true;

                            }
                        }

                        if (!listContainsUser) {
                            contactList.add(contact);
                            contactListAdapter.notifyDataSetChanged();
                            actionBar.setSubtitle(contactList.size() + " " + getString(R.string.choose_contact_subtitle));
                        }

                        Log.d("userlist", "contactList = " + contactList );

                    } else {

                        Log.d("userlist", "ChooseContactActivity: onDataChangeCalled! dataSnapshot = " + dataSnapshot.getValue());
                    }
                    loadingUsers.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {
            loadingUsers.setVisibility(View.INVISIBLE);
            lyNoConnection.setVisibility(View.VISIBLE);
        }
    }

    private String getCountryISO() {
        TelephonyManager telephonyManager =
                (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);

        String iso = telephonyManager.getNetworkCountryIso();

        if (iso != null && !iso.equals("")) {
            return CountryToPhonePrefix.getPhone(iso);
        }

        return null;
    }

    private void initilizeRecycyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.user_list_recyclerview);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(false);
        linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        contactListAdapter = new ContactListAdapter(this, contactList);
        recyclerView.setAdapter(contactListAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onContactClick(String userId, String name) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(getString(R.string.user_id_intent_key), userId);
        intent.putExtra(getString(R.string.user_name_intent), name);
        startActivityForResult(intent, getResources().getInteger(R.integer.CHAT_RESULT));

    }

}
