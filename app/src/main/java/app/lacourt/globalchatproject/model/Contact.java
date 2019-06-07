package app.lacourt.globalchatproject.model;

public class Contact {
    private String id;
    private String name;
    private String phone;
    private String picture;

    public Contact(String id, String name, String phone, String picture) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.picture = picture;
    }

    public String getId() {
        return id;
    }

    public void setId(String uid) {
        this.id = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
