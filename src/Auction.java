public class Auction {

    private int auctionId;
    private int itemId;
    private int sellerId;
    private String startTime;
    private String endTime;
    private String status;

    public Auction(int auctionId,
                   int itemId,
                   int sellerId,
                   String startTime,
                   String endTime,
                   String status) {

        this.auctionId = auctionId;
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public int getAuctionId() { return auctionId; }
    public int getItemId() { return itemId; }
    public int getSellerId() { return sellerId; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getStatus() { return status; }
}