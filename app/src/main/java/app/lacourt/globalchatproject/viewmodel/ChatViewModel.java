package app.lacourt.globalchatproject.viewmodel;

import android.app.Application;
import android.util.Log;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import app.lacourt.globalchatproject.model.Chat;
import app.lacourt.globalchatproject.repository.Repository;

public class ChatViewModel extends AndroidViewModel {

    private Repository repository;
    private LiveData<List<Chat>> allChats;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        this.repository = new Repository(application);

    }

    public void insert(Chat chat) {
        Log.d("insert", "viewmodel insert called: chat = " + chat.getName() + " - " + chat.getChatId());
        repository.insert(chat); }


}
