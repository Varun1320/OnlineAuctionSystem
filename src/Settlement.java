public class Settlement {

    private long settlementId;
    private int auctionId;
    private double amountPaid;

    public Settlement(long settlementId,
                      int auctionId,
                      double amountPaid) {

        this.settlementId = settlementId;
        this.auctionId = auctionId;
        this.amountPaid = amountPaid;
    }

    public long getSettlementId() { return settlementId; }
    public int getAuctionId() { return auctionId; }
    public double getAmountPaid() { return amountPaid; }
}