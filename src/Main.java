
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {

    private static final String URL =
            "jdbc:mysql://localhost:3306/online_auction";
    private static final String USER = "root";
    private static final String PASSWORD = "pranav";

    private static final Scanner sc = new Scanner(System.in);
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {

        try (Connection con = DriverManager.getConnection(
                URL, USER, PASSWORD)) {

            System.out.println("MySQL connected successfully!");

            while (true) {
                System.out.println("\n===== ONLINE AUCTION SYSTEM =====");
                System.out.println("1. Add Seller");
                System.out.println("2. Add Item");
                System.out.println("3. Create Auction");
                System.out.println("4. Register Bidder");
                System.out.println("5. Place Bid");
                System.out.println("6. Close Auction");
                System.out.println("7. Settle Payment");
                System.out.println("8. Display Auctions");
                System.out.println("9. Exit");
                System.out.print("Enter choice: ");

                int choice;
                try {
                    choice = Integer.parseInt(sc.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Enter a valid number.");
                    continue;
                }

                try {
                    switch (choice) {
                        case 1 -> addSeller(con);
                        case 2 -> addItem(con);
                        case 3 -> createAuction(con);
                        case 4 -> registerBidder(con);
                        case 5 -> placeBid(con);
                        case 6 -> closeAuction(con);
                        case 7 -> settlePayment(con);
                        case 8 -> displayAuctions(con);
                        case 9 -> {
                            System.out.println("Thank you!");
                            return;
                        }
                        default -> System.out.println("Invalid choice.");
                    }
                } catch (SQLException e) {
                    System.out.println("Database error: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }

        } catch (SQLException e) {
            System.out.println("Cannot connect to MySQL: "
                    + e.getMessage());
            System.out.println(
                    "Check MySQL, schema.sql, username, password and JDBC driver.");
        }
    }

    private static String read(String message) {
        System.out.print(message);
        return sc.nextLine().trim();
    }

    private static int readInt(String message) {
        return Integer.parseInt(read(message));
    }

    private static BigDecimal readMoney(String message) {
        BigDecimal amount = new BigDecimal(read(message));
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero.");
        }
        return amount;
    }

    private static LocalDateTime readDate(String message) {
        return LocalDateTime.parse(read(message), FORMAT);
    }

    private static int insert(Connection con, String sql,
                              Object... values) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < values.length; i++) {
                ps.setObject(i + 1, values[i]);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Could not generate record ID.");
    }

    private static boolean exists(Connection con, String sql, int id)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void addSeller(Connection con) throws SQLException {
        String name = read("Seller name: ");
        String contact = read("Seller contact: ");

        int id = insert(con,
                "INSERT INTO sellers(name, contact) VALUES(?, ?)",
                name, contact);

        System.out.println("Seller added. ID: " + id);
    }

    private static void addItem(Connection con) throws SQLException {
        int sellerId = readInt("Seller ID: ");

        if (!exists(con, "SELECT seller_id FROM sellers WHERE seller_id=?",
                sellerId)) {
            System.out.println("Seller not found.");
            return;
        }

        String name = read("Item name: ");
        String description = read("Description: ");
        BigDecimal price = readMoney("Base price: ");

        int id = insert(con,
                "INSERT INTO auction_items"
                + "(seller_id, name, description, base_price) "
                + "VALUES(?, ?, ?, ?)",
                sellerId, name, description, price);

        System.out.println("Item added. ID: " + id);
    }

    private static void createAuction(Connection con) throws SQLException {
        int itemId = readInt("Item ID: ");

        int sellerId;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT seller_id FROM auction_items WHERE item_id=?")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Item not found.");
                    return;
                }
                sellerId = rs.getInt("seller_id");
            }
        }

        LocalDateTime start = readDate(
                "Start (yyyy-MM-dd HH:mm): ");
        LocalDateTime end = readDate(
                "End   (yyyy-MM-dd HH:mm): ");

        if (!end.isAfter(start)) {
            System.out.println("End time must be after start time.");
            return;
        }

        int id = insert(con,
                "INSERT INTO auctions"
                + "(item_id, seller_id, start_time, end_time, status) "
                + "VALUES(?, ?, ?, ?, 'OPEN')",
                itemId, sellerId, Timestamp.valueOf(start),
                Timestamp.valueOf(end));

        System.out.println("Auction created. ID: " + id);
    }

    private static void registerBidder(Connection con)
            throws SQLException {
        String name = read("Bidder name: ");
        String contact = read("Bidder contact: ");

        int id = insert(con,
                "INSERT INTO bidders(name, contact) VALUES(?, ?)",
                name, contact);

        System.out.println("Bidder registered. ID: " + id);
    }

    private static void placeBid(Connection con) throws SQLException {
        int auctionId = readInt("Auction ID: ");
        int bidderId = readInt("Bidder ID: ");
        BigDecimal amount = readMoney("Bid amount: ");
        LocalDateTime now = LocalDateTime.now();

        con.setAutoCommit(false);
        try {
            BigDecimal basePrice;
            Timestamp startTime;
            Timestamp endTime;
            String status;

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT a.start_time, a.end_time, a.status, "
                    + "i.base_price FROM auctions a "
                    + "JOIN auction_items i ON a.item_id=i.item_id "
                    + "WHERE a.auction_id=? FOR UPDATE")) {
                ps.setInt(1, auctionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Auction not found.");
                    }
                    startTime = rs.getTimestamp("start_time");
                    endTime = rs.getTimestamp("end_time");
                    status = rs.getString("status");
                    basePrice = rs.getBigDecimal("base_price");
                }
            }

            if (!exists(con,
                    "SELECT bidder_id FROM bidders WHERE bidder_id=?",
                    bidderId)) {
                throw new SQLException("Bidder not found.");
            }

            if (!"OPEN".equals(status)
                    || now.isBefore(startTime.toLocalDateTime())
                    || !now.isBefore(endTime.toLocalDateTime())) {
                throw new SQLException(
                        "Auction is not currently accepting bids.");
            }

            BigDecimal highest = basePrice;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT MAX(amount) AS highest FROM bids "
                    + "WHERE auction_id=?")) {
                ps.setInt(1, auctionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getBigDecimal("highest") != null) {
                        highest = rs.getBigDecimal("highest");
                    }
                }
            }

            if (amount.compareTo(highest) <= 0) {
                throw new SQLException(
                        "Bid must be greater than " + highest);
            }

            insert(con,
                    "INSERT INTO bids(auction_id, bidder_id, amount) "
                    + "VALUES(?, ?, ?)",
                    auctionId, bidderId, amount);

            con.commit();
            System.out.println("Bid placed successfully!");
        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    private static void closeAuction(Connection con) throws SQLException {
        int auctionId = readInt("Auction ID to close: ");

        con.setAutoCommit(false);
        try {
            Timestamp end;
            String status;

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT end_time, status FROM auctions "
                    + "WHERE auction_id=? FOR UPDATE")) {
                ps.setInt(1, auctionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Auction not found.");
                    }
                    end = rs.getTimestamp("end_time");
                    status = rs.getString("status");
                }
            }

            if (!"OPEN".equals(status)) {
                throw new SQLException("Auction is already closed.");
            }

            if (LocalDateTime.now().isBefore(end.toLocalDateTime())) {
                throw new SQLException(
                        "Auction cannot close before its end time.");
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE auctions SET status='CLOSED' "
                    + "WHERE auction_id=?")) {
                ps.setInt(1, auctionId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT bid_id, bidder_id, amount FROM bids "
                    + "WHERE auction_id=? "
                    + "ORDER BY amount DESC, bid_time ASC LIMIT 1")) {
                ps.setInt(1, auctionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement win = con.prepareStatement(
                                "INSERT INTO winners"
                                + "(auction_id, bid_id, bidder_id, "
                                + "final_price, confirmed) "
                                + "VALUES(?, ?, ?, ?, FALSE) "
                                + "ON DUPLICATE KEY UPDATE "
                                + "bid_id=VALUES(bid_id), "
                                + "bidder_id=VALUES(bidder_id), "
                                + "final_price=VALUES(final_price), "
                                + "confirmed=FALSE")) {
                            win.setInt(1, auctionId);
                            win.setLong(2, rs.getLong("bid_id"));
                            win.setInt(3, rs.getInt("bidder_id"));
                            win.setBigDecimal(4, rs.getBigDecimal("amount"));
                            win.executeUpdate();
                        }
                        System.out.println("Winning bidder ID: "
                                + rs.getInt("bidder_id"));
                        System.out.println("Winning amount: "
                                + rs.getBigDecimal("amount"));
                        System.out.println(
                                "Winner confirmation is required before payment.");
                    } else {
                        System.out.println("Auction closed with no bids.");
                    }
                }
            }

            con.commit();
            System.out.println("Auction closed successfully.");
        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    private static void settlePayment(Connection con) throws SQLException {
        int auctionId = readInt("Auction ID: ");
        int bidderId = readInt("Winning bidder ID confirming payment: ");
        BigDecimal paid = readMoney("Amount paid: ");

        con.setAutoCommit(false);
        try {
            BigDecimal finalPrice;
            int winnerId;
            boolean confirmed;

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT bidder_id, final_price, confirmed "
                    + "FROM winners WHERE auction_id=? FOR UPDATE")) {
                ps.setInt(1, auctionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "Winner not found. Close auction first.");
                    }
                    winnerId = rs.getInt("bidder_id");
                    finalPrice = rs.getBigDecimal("final_price");
                    confirmed = rs.getBoolean("confirmed");
                }
            }

            if (winnerId != bidderId) {
                throw new SQLException("Bidder is not the winner.");
            }

            if (!confirmed) {
                String answer = read(
                        "Confirm winner and payment? (yes/no): ");
                if (!answer.equalsIgnoreCase("yes")) {
                    con.rollback();
                    System.out.println("Settlement cancelled.");
                    return;
                }
            }

            if (paid.compareTo(finalPrice) != 0) {
                throw new SQLException(
                        "Payment must equal winning price: " + finalPrice);
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE winners SET confirmed=TRUE "
                    + "WHERE auction_id=?")) {
                ps.setInt(1, auctionId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO settlements"
                    + "(auction_id, amount_paid, payment_status, "
                    + "item_transfer_status) VALUES(?, ?, 'SUCCESS', "
                    + "'TRANSFERRED')")) {
                ps.setInt(1, auctionId);
                ps.setBigDecimal(2, paid);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE auctions SET status='SETTLED' "
                    + "WHERE auction_id=? AND status='CLOSED'")) {
                ps.setInt(1, auctionId);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException(
                            "Auction must be closed before settlement.");
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE auction_items i JOIN auctions a "
                    + "ON i.item_id=a.item_id "
                    + "SET i.transferred=TRUE WHERE a.auction_id=?")) {
                ps.setInt(1, auctionId);
                ps.executeUpdate();
            }

            con.commit();
            System.out.println(
                    "Payment recorded and item marked as transferred.");
        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    private static void displayAuctions(Connection con)
            throws SQLException {
        String sql =
                "SELECT a.auction_id, i.name AS item_name, "
                + "s.name AS seller_name, a.start_time, a.end_time, "
                + "a.status, i.base_price, "
                + "COALESCE(MAX(b.amount), i.base_price) AS highest_bid "
                + "FROM auctions a "
                + "JOIN auction_items i ON a.item_id=i.item_id "
                + "JOIN sellers s ON a.seller_id=s.seller_id "
                + "LEFT JOIN bids b ON a.auction_id=b.auction_id "
                + "GROUP BY a.auction_id, i.name, s.name, "
                + "a.start_time, a.end_time, a.status, i.base_price "
                + "ORDER BY a.auction_id";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n----- AUCTIONS -----");
            while (rs.next()) {
                System.out.println("\nAuction ID: "
                        + rs.getInt("auction_id"));
                System.out.println("Item: "
                        + rs.getString("item_name"));
                System.out.println("Seller: "
                        + rs.getString("seller_name"));
                System.out.println("Start: "
                        + rs.getTimestamp("start_time"));
                System.out.println("End: "
                        + rs.getTimestamp("end_time"));
                System.out.println("Status: "
                        + rs.getString("status"));
                System.out.println("Base price: "
                        + rs.getBigDecimal("base_price"));
                System.out.println("Highest bid: "
                        + rs.getBigDecimal("highest_bid"));
            }
        }
    }
}
