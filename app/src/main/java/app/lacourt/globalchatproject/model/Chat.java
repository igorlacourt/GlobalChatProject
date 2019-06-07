package app.lacourt.globalchatproject.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_table")
public class Chat {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "chat_id")
    private String chatId;

    @NonNull
    @ColumnInfo
    private String name;

    @ColumnInfo(name = "profile_picture")
    private String profilePicture;

    @ColumnInfo(name = "contact_id")
    private String contactId;

    public Chat(String chatId, String contactId, String name, String profilePicture) {
        this.chatId = chatId;
        this.contactId = contactId;
        this.name = name;
        this.profilePicture = profilePicture;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

}
