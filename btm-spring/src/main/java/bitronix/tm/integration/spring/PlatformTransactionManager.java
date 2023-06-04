package bitronix.tm.integration.spring;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.BitronixTransactionSynchronizationRegistry;
import bitronix.tm.TransactionManagerServices;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.UserTransaction;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * Bitronix-specific Spring PlatformTransactionManager implementation.
 *
 * @author Marcus Klimstra (CGI)
 */
public class PlatformTransactionManager extends JtaTransactionManager implements DisposableBean {

    private final BitronixTransactionManager transactionManager;

    private final BitronixTransactionSynchronizationRegistry bitronixTransactionSynchronizationRegistry = TransactionManagerServices.getTransactionSynchronizationRegistry();

    public PlatformTransactionManager() {
        this.transactionManager = TransactionManagerServices.getTransactionManager();
    }

    @Override
    protected UserTransaction retrieveUserTransaction() throws TransactionSystemException {
        return transactionManager;
    }

    @Override
    protected TransactionManager retrieveTransactionManager() throws TransactionSystemException {
        return transactionManager;
    }

    @Override
    protected TransactionSynchronizationRegistry retrieveTransactionSynchronizationRegistry() throws TransactionSystemException {
        return bitronixTransactionSynchronizationRegistry;
    }

    @Override
    public void destroy() throws Exception {
        transactionManager.shutdown();
    }
}
