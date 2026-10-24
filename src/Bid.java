public class Bid {

    private long bidId;
    private int auctionId;
    private int bidderId;
    private double amount;

    public Bid(long bidId,
               int auctionId,
               int bidderId,
               double amount) {

        this.bidId = bidId;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
    }

    public long getBidId() { return bidId; }
    public int getAuctionId() { return auctionId; }
    public int getBidderId() { return bidderId; }
    public double getAmount() { return amount; }
}