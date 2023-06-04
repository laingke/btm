package bitronix.tm.integration.spring;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:test-context.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class PoolingDataSourceFactoryBeanTest {

    @Inject
    @Named("dataSource2")
    private PoolingDataSource dataSource2;

    @Test
    public void validateProperties() {
        assertEquals("btm-spring-test-ds2", dataSource2.getUniqueName());
        assertEquals("bitronix.tm.mock.resource.jdbc.MockitoXADataSource", dataSource2.getClassName());
        assertEquals(1, dataSource2.getMinPoolSize());
        assertEquals(2, dataSource2.getMaxPoolSize());
        assertTrue(dataSource2.getAutomaticEnlistingEnabled());
        assertFalse(dataSource2.getUseTmJoin());
        assertEquals(60, dataSource2.getMaxIdleTime()); // default value not overridden in bean configuration
        assertEquals("5", dataSource2.getDriverProperties().get("loginTimeout"));
    }

    @Test
    public void validateConnection() throws SQLException {
        try (Connection connection = dataSource2.getConnection()) {
            assertNotNull(connection);
        }
    }
}
