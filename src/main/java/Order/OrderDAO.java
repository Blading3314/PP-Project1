package Order;

import java.util.List;
import java.util.Optional;

public interface OrderDAO {
    Optional<Order> getPaidById(int orderID);
    List<Order> getAllPaid();
    Optional<Order> getPaidByPaidDate(String PaidDate);
    void savePaid(Order order);
    void updatePaid(Order order);
    void deletePaidByID(int orderID);
}
