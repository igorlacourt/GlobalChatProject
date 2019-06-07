package app.lacourt.globalchatproject.database;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import app.lacourt.globalchatproject.model.Chat;

@Dao
public interface ChatDao {
    @Insert
    void insert(Chat chat);

    @Query("DELETE FROM chat_table")
    void deleteAll();

    @Query("SELECT * FROM chat_table")
    List<Chat> getAllChats();

    @Query("SELECT count(*) FROM chat_table")
    int getChatCount();

    @Query("SELECT * FROM chat_table WHERE chat_id = :id")
    Chat getChatById(String id);

    @Query("UPDATE chat_table\n" +
            "SET  profile_picture = :new_picture\n" +
            "WHERE contact_id = :id")
    void updateChatPicture(String new_picture, String id);

    @Query("SELECT profile_picture FROM chat_table WHERE contact_id = :id")
    String getPicture(String id);

    @Delete
    void deleteChat(Chat chat);
//    @Query("DELETE FROM chat_table WHERE contact_id = :id")
//    void deleteChat(String id);
}
