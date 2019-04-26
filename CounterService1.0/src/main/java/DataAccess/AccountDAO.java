package DataAccess;

import Entity.Account;
import Entity.Manager;
import Entity.SessionUtil;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AccountDAO {
    private static Session session;
    private static EntityManager entityManager = Manager.getEntityManager();
    private static ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static Lock writeLock = readWriteLock.writeLock();
    private static Lock readLock = readWriteLock.readLock();

    public AccountDAO() {
        this.session = SessionUtil.getSession();
    }

    synchronized public long getBalance(String userId, long balance) {
        Account account = session.find(Account.class, userId);

        if (account == null) {
            account = new Account(userId, balance);
            session.save(account);
        }

        return account.getBalance();
    }

    synchronized public long setBalance(String userId, long balance) {
        Account account = session.find(Account.class, userId);

        if (account == null) {
            account = new Account(userId, balance);
            session.save(account);
        } else {
            account.setBalance(balance);
            session.update(account);
        }

        return account.getBalance();
    }

    synchronized public long increaseBalance(String userId, long amount) {
        Account account = session.find(Account.class, userId);

        if (account == null) {
            account = new Account(userId, amount);
            session.save(account);
        } else {
            account.setBalance(account.getBalance() + amount);
            session.update(account);
        }

        return account.getBalance();
    }

    private static int count = 0;

    synchronized public long decreaseBalance(String userId, long amount) {
        Account account = session.find(Account.class, userId);

        if (account == null) {
            account = new Account(userId, amount);
            session.save(account);
        } else {
            account.setBalance(account.getBalance() - amount);
            session.update(account);
        }

        return account.getBalance();
    }


}
