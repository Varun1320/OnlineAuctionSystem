public class Seller {

    private int sellerId;
    private String name;
    private String contact;

    public Seller(int sellerId, String name, String contact) {
        this.sellerId = sellerId;
        this.name = name;
        this.contact = contact;
    }

    public int getSellerId() { return sellerId; }
    public String getName() { return name; }
    public String getContact() { return contact; }
}