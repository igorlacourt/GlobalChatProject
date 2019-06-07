package app.lacourt.globalchatproject.adapters;

import app.lacourt.globalchatproject.model.Chat;

public interface ChatItemClick {
    void onChatClick(String name, String chatId);
    void onChatLongClick(Chat chat);
}
