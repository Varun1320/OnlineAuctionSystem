public class Bidder {

    private int bidderId;
    private String name;
    private String contact;

    public Bidder(int bidderId, String name, String contact) {

        this.bidderId = bidderId;
        this.name = name;
        this.contact = contact;
    }

    public int getBidderId() { return bidderId; }
    public String getName() { return name; }
    public String getContact() { return contact; }
}