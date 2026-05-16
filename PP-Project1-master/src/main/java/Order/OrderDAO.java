package Order;

import java.util.List;
import java.util.Optional;

/**
 * Database contract for order records.
 * The rest of the app uses this interface instead of depending on SQL statements.
 */
public interface OrderDAO {
    Optional<Order> getPaidById(int orderID);
    List<Order> getAllPaid();
    Optional<Order> getPaidByPaidDate(String PaidDate);
    void savePaid(Order order);
    void updatePaid(Order order);
    void deletePaidByID(int orderID);
}
