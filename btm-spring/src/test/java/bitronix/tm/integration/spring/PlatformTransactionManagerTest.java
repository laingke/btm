package bitronix.tm.integration.spring;

import bitronix.tm.mock.events.EventRecorder;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.SQLException;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:test-context.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class PlatformTransactionManagerTest {

    private static final Logger log = LoggerFactory.getLogger(PlatformTransactionManagerTest.class);

    @Inject
    private TransactionalBean bean;

    @BeforeEach
    @AfterEach
    public void clearEvents() {
        EventRecorder.clear();
    }

    @AfterEach
    public void logEvents() {
        if (log.isDebugEnabled()) {
            log.debug(EventRecorder.dumpToString());
        }
    }

    @Test
    @Repeat(2)
    @DirtiesContext
    public void testTransactionalMethod() throws SQLException {
        bean.doSomethingTransactional(1);
        bean.verifyEvents(1);
    }
}
