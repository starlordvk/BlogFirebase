package xyz.varun.blogfirebase;

/**
 * Created by Tarun on 12/22/2016.
 */
public class Blog {

    private String Title;
    private String Description;
    private String Image;
    public Blog(){

    }

    public Blog(String description, String image, String title) {
        Description = description;
        Image = image;
        Title = title;
    }




    public String getImage() {
        return Image;
    }

    public void setImage(String image) {

        Image = image;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String desciption) {
        Description = desciption;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }
}
