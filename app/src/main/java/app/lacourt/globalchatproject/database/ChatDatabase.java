package app.lacourt.globalchatproject.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import app.lacourt.globalchatproject.model.Chat;

@Database(entities = {Chat.class}, version = 3, exportSchema = false)
public abstract class ChatDatabase extends RoomDatabase {
    private ChatDatabase dataBase = null;
    public abstract ChatDao chatDao();

    private static volatile ChatDatabase INSTANCE;
    private static final String DATABASE_NAME = "chat_database";
    private static final int DATABASE_VERSION = 1;

    public static ChatDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ChatDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ChatDatabase.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
