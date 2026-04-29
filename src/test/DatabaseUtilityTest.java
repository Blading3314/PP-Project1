package test;

import org.junit.jupiter.api.Test;
import util.DBConnectionUtility;

import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseUtilityTest {
    @Test
    void testConnection(){
        try (Connection conn = DBConnectionUtility.getConnection()){
            assertNotNull(conn);
            System.out.println("Connection successful");
        }
        catch (SQLException e){
            fail("Connection failed" + e.getMessage());
        }
    }

}
