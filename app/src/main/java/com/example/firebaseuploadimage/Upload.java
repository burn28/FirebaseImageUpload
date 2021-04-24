package com.example.firebaseuploadimage;

public class Upload {

    private String imageName;
    private String imageUri;

    public Upload(){
        //empty constructor
    }

    public Upload(String imageName, String imageUri) {

        this.imageName = imageName;
        this.imageUri = imageUri;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
