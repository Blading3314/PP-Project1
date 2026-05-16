package Comic;

/**
 * Simple data object for one comic in the store inventory.
 * The database ID stays numeric, while the UI may display it as a friendly ID like CB1.
 */
public class Comic {
    private int comicID;
    private String name;
    private String issue;
    private String publisher;
    private double price;
    private int stock;

    /**
     * Creates a comic without explicit price/stock, using default values.
     */
    public Comic(int comicID, String name, String issue, String publisher) {
        this(comicID, name, issue, publisher, 0.0, 0);
    }

    /**
     * Creates a full comic object from form input or a database row.
     */
    public Comic(int comicID, String name, String issue, String publisher, double price, int stock) {
        this.comicID = comicID;
        this.name = name;
        this.issue = issue;
        this.publisher = publisher;
        this.price = price;
        this.stock = stock;
    }

    public int getComicID() {
        return comicID;
    }

    public void setComicID(int comicID) {
        this.comicID = comicID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    /**
     * Produces a readable comic summary for debugging.
     */
    @Override
    public String toString() {
        return "Comic ID: " + comicID
                + "\nTitle: " + name
                + "\nIssue: " + issue
                + "\nPublisher: " + publisher
                + "\nPrice: " + price
                + "\nStock: " + stock;
    }
}
