public class AuctionItem {

    private int itemId;
    private int sellerId;
    private String name;
    private String description;
    private double basePrice;

    public AuctionItem(int itemId, int sellerId,
                       String name, String description,
                       double basePrice) {

        this.itemId = itemId;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
    }

    public int getItemId() { return itemId; }
    public int getSellerId() { return sellerId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getBasePrice() { return basePrice; }
}