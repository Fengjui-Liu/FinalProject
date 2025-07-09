package FinalProject.model;

public class Diary {
    private String date;
    private String mood;
    private String weather;
    private String content;
    private String imagePath;

    public Diary(String date, String mood, String weather, String content, String imagePath) {
        this.date = date;
        this.mood = mood;
        this.weather = weather;
        this.content = content;
        this.imagePath = imagePath;
    }

    // Getter
    public String getDate() {
        return date;
    }

    public String getMood() {
        return mood;
    }

    public String getWeather() {
        return weather;
    }

    public String getContent() {
        return content;
    }

    public String getImagePath() {
        return imagePath;
    }

    // Setter
    public void setDate(String date) {
        this.date = date;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return date + " - " + mood + " - " + weather;
    }
    private String password;

    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {
        return password;
    }

}
