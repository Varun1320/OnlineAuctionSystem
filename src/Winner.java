public class Winner {

    private int auctionId;
    private long bidId;
    private int bidderId;
    private double finalPrice;
    private boolean confirmed;

    public Winner(int auctionId,
                  long bidId,
                  int bidderId,
                  double finalPrice,
                  boolean confirmed) {

        this.auctionId = auctionId;
        this.bidId = bidId;
        this.bidderId = bidderId;
        this.finalPrice = finalPrice;
        this.confirmed = confirmed;
    }

    public int getAuctionId() { return auctionId; }
    public long getBidId() { return bidId; }
    public int getBidderId() { return bidderId; }
    public double getFinalPrice() { return finalPrice; }
    public boolean isConfirmed() { return confirmed; }
}