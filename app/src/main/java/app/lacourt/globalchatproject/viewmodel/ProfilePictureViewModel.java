package app.lacourt.globalchatproject.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import app.lacourt.globalchatproject.repository.Repository;

public class ProfilePictureViewModel extends AndroidViewModel {

    private Repository repository;

    public ProfilePictureViewModel(@NonNull Application application) {
        super(application);

        this.repository = new Repository(application);
    }

    public void updateChatPicture(String id, String picture) {
        Log.d("myupdate", "updateChatPicture called.");
        this.repository.updateChatPicture(id, picture);
    }
}

