package bitronix.tm.resource.jdbc;

import bitronix.tm.mock.resource.jdbc.MockitoXADataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Ludovic Orban
 */
public class PoolingDataSourceTest {

    @Test
    public void testInjectedXaFactory() throws Exception {
        PoolingDataSource pds = new PoolingDataSource();
        try {
            pds.setUniqueName("pds");
            pds.setMinPoolSize(1);
            pds.setMaxPoolSize(1);
            pds.setXaDataSource(new MockitoXADataSource());

            pds.init();

            Connection connection = pds.getConnection();

            connection.close();
        } finally {
            pds.close();
        }
    }

    @Test
    public void testEffectiveConnectionTimeoutWhenSet() {
        PoolingDataSource pds = new PoolingDataSource();
        pds.setConnectionTestTimeout(10);
        assertEquals(10, pds.getEffectiveConnectionTestTimeout());
    }

    @Test
    public void testEffectiveConnectionTimeoutWhenAcquisitionTimeoutSet() {
        PoolingDataSource pds = new PoolingDataSource();
        pds.setAcquisitionTimeout(10);
        assertEquals(10, pds.getEffectiveConnectionTestTimeout());
    }

    @Test
    public void testEffectiveConnectionTimeoutIsMinimumValue() {
        PoolingDataSource pds = new PoolingDataSource();

        pds.setConnectionTestTimeout(5);
        pds.setAcquisitionTimeout(10);
        assertEquals(5, pds.getEffectiveConnectionTestTimeout());

        pds.setAcquisitionTimeout(15);
        pds.setConnectionTestTimeout(20);
        assertEquals(15, pds.getEffectiveConnectionTestTimeout());
    }

    @Test
    public void testDefaultEffectiveAcquisitionTimeout() {
        PoolingDataSource pds = new PoolingDataSource();
        assertEquals(30, pds.getEffectiveConnectionTestTimeout());
    }

}
