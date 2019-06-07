package app.lacourt.globalchatproject.repository;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import androidx.lifecycle.MutableLiveData;
import app.lacourt.globalchatproject.database.ChatDao;
import app.lacourt.globalchatproject.database.ChatDatabase;
import app.lacourt.globalchatproject.model.Chat;

public class Repository implements interactor{
    private ChatDao chatDao;
    public MutableLiveData<ArrayList<Chat>> allChats;
    public MutableLiveData<String> userPicture;

    public Repository(Application application) {

        ChatDatabase db = ChatDatabase.getDatabase(application);
        this.chatDao = db.chatDao();
        this.allChats = new MutableLiveData<>();
        this.userPicture = new MutableLiveData<>();

    }

    public void insert (Chat chat) {
        Log.d("insert", "repository insert called: chat = " + chat.getName() + " - " + chat.getChatId());
        new insertAsyncTask(chatDao).execute(chat);
    }

    public String getChatNameById(String id) {
        return chatDao.getChatById(id).getName();
    }

    public int getChatCount() {
        return chatDao.getChatCount();
    }

    @Override
    public void onChatNameResponse(String name) {

    }

    public void updateChatPicture(String id, String picture) {
        new updateChatPictureAsyncTask(chatDao, id).execute(picture);
    }

    public void getPicture(String contactId) {
        new getPictureAsyncTask(chatDao).execute(contactId);
    }

    public void getAllChats() {
        new getAllChatsAsyncTask(chatDao).execute();
    }

    public void deleteChat(Chat chat) {
        Log.d("mydelete", "repository's deleteChat called.");
        new deleteChatAsyncTask(chatDao).execute(chat);

    }

    public void deleAllChats() {
        new deleteAllChatsAsyncTask(chatDao).execute();
    }

    private class getPictureAsyncTask extends AsyncTask<String, Void, String> {
        private ChatDao mAsyncTaskDao;

        public getPictureAsyncTask(ChatDao dao) {
            this.mAsyncTaskDao = dao;
        }

        @Override
        protected String doInBackground(String... ids) {
            if (ids[0] != null)
                return mAsyncTaskDao.getPicture(ids[0]);

            return null;
        }

        @Override
        protected void onPostExecute(String id) {
            super.onPostExecute(id);
            userPicture.setValue(id);
        }
    }

    private class getAllChatsAsyncTask extends AsyncTask<Void, Void, ArrayList<Chat>> {

        private ChatDao mAsyncTaskDao;

        getAllChatsAsyncTask(ChatDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected ArrayList<Chat> doInBackground(Void... voids) {

            ArrayList<Chat> chats = (ArrayList<Chat>) mAsyncTaskDao.getAllChats();
            if(chats == null)
                chats = new ArrayList<>();
            return chats;
        }

        @Override
        protected void onPostExecute(ArrayList<Chat> chatList) {
            super.onPostExecute(chatList);
            allChats.setValue(chatList);
        }
    }

    private static class updateChatPictureAsyncTask extends AsyncTask<String, Void, Void> {
        private ChatDao mAsyncTaskDao;
        private String id;

        updateChatPictureAsyncTask(ChatDao dao, String id) {
            Log.d("myupdate", "repository updateChatPictureAsyncTask called.");
            this.mAsyncTaskDao = dao;
            this.id = id;
        }

        @Override
        protected Void doInBackground(String... strings) {
            Log.d("myupdate", "repository doInBackground called.");
            Log.d("myupdate", "strings[0] = " + strings[0]);
            mAsyncTaskDao.updateChatPicture(strings[0], id);
            return null;
        }
    }

    private static class insertAsyncTask extends AsyncTask<Chat, Void, Void> {

        private ChatDao mAsyncTaskDao;

        insertAsyncTask(ChatDao dao) {
            Log.d("insert", "repository insertAsyncTask called");
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Chat... params) {
            Log.d("insert", "repository insertAsyncTask, doInbackground called: params[0] = " + params[0].getName() + " - " + params[0].getChatId());

            mAsyncTaskDao.insert(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("insert", "repository insertAsyncTask, onPostExecute called");
        }
    }


    private class deleteChatAsyncTask extends AsyncTask<Chat, Void, Void> {
        private ChatDao mAsyncTaskDao;

        public deleteChatAsyncTask(ChatDao chatDao) {
            Log.d("mydelete", "AsyncTask constructor called.");
            this.mAsyncTaskDao = chatDao;
        }

        @Override
        protected Void doInBackground(Chat... chats) {
            mAsyncTaskDao.deleteChat(chats[0]);
            Log.d("mydelete", "mAsyncTaskDao.deleteChat called.");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            getAllChats();
            Log.d("mydelete", "onPostExecute called.");
        }
    }

    private class deleteAllChatsAsyncTask extends AsyncTask<Void, Void, Void> {
        private ChatDao mAsyncDao;

        public deleteAllChatsAsyncTask(ChatDao chatDao) {
            mAsyncDao = chatDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mAsyncDao.deleteAll();
            return null;
        }
    }
}

