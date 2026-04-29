package Comic;

public class Comic {
    private int comicID;
    private String Name;
    private String Issue;
    private String Publisher;
    public Comic(int comicID, String Name, String Issue, String Publisher) {
        this.comicID = comicID;
        this.Name = Name;
        this.Issue = Issue;
        this.Publisher = Publisher;
    }

    public int getComicID() {
        return comicID;
    }

    public void setComicID(int comicID) {
        this.comicID = comicID;
    }
    public String getName() {
        return Name;
    }
    public void setName(String name) {
        Name = name;
    }

    public String getPublisher() {
        return Publisher;
    }

    public void setPublisher(String publisher) {
        Publisher = publisher;
    }

    public String getIssue() {
        return Issue;
    }

    public void setIssue(String issue) {
        Issue = issue;
    }
    @Override
    public String toString(){
        return "Comic.Comic ID: " + comicID +
                "\n" + "Name: " + Name +
                "\n" + "Issue: " + Issue +
                "\n" + "Publisher: " + Publisher;
    }
}
