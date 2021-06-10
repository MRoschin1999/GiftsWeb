package com.nectcracker.studyproject.json.friendsFromVK;

public class Nickname {
    private Long id;
    private String first_name;
    private String last_name;
    private String photo_50;
    private String photo_100;

    public String getPhoto_100() {
        return photo_100;
    }

    public Long getId() {
        return id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getPhoto_50(){
        return photo_50;
    }
}
