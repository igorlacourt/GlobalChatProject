package app.lacourt.globalchatproject.viewmodel;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import app.lacourt.globalchatproject.model.Chat;
import app.lacourt.globalchatproject.repository.Repository;

public class MainScreenViewModel extends AndroidViewModel {

    private Repository repository;
    public LiveData<ArrayList<Chat>> allChats;
    public LiveData<String> userPicture;

    public MainScreenViewModel(@NonNull Application application) {
        super(application);

        this.repository = new Repository(application);
        this.allChats = repository.allChats;
        this.userPicture = new MutableLiveData<>();
        Log.d("getchats", "viewModel, constructor call repository.getAllChats()");
    }

    public void getAllChats() {
        Log.d("getchats", "viewModel, LiveData<List<Chat>> getAllChats()");
        repository.getAllChats();
    }

    public void insert(Chat chat) {
        repository.insert(chat);
    }

    public void deleteChat(Chat chat) {
        Log.d("mydelete", "viewmodel's deleteChat called.");
        repository.deleteChat(chat);
    }

    public String getChatNameById(String id) {
        return this.repository.getChatNameById(id);
    }

    public int getChatCount() {
        return this.repository.getChatCount();
    }

    public void updateChatPicture(String id, String picture) {
        Log.d("myupdate", "updateChatPicture called.");
        this.repository.updateChatPicture(id, picture);
    }

    public void getPicture(String contactId) {
        this.repository.getPicture(contactId);
        userPicture = repository.userPicture;
    }

    public void deleteAllChats() {
        this.repository.deleAllChats();
    }
}
