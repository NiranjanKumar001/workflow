package com.nabin.taskmanager;

import org.hibernate.validator.internal.util.Contracts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootTest
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testDatabaseConnection() throws Exception {
        Contracts.assertNotNull(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            Contracts.assertNotNull(connection);
            System.out.println("Database connection successful!");
            System.out.println("Database: " + connection.getCatalog());
        }
    }
}