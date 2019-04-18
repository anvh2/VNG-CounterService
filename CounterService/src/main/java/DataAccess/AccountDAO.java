package DataAccess;

import Entity.Account;
import Entity.SessionUtil;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import server.CounterServiceImpl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AccountDAO {
    private static Session session;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final Lock readLock = readWriteLock.readLock();
    private static Logger logger = Logger.getLogger(CounterServiceImpl.class);
    private static int connPool = 0;
    ExecutorService executorService = Executors.newFixedThreadPool(100);//max of connection is 100, fix in hibernate.cfg.xml

    public AccountDAO() {
        this.session = SessionUtil.getSession();
    }

    public long getBalance(String userId, long balance) throws ExecutionException, InterruptedException {
        Future<Account> future = executorService.submit(()->{
            Account account = null;

            readLock.lock();
            try{
                //tim record nay duoi db
                account = session.find(Account.class, userId);
            }catch (Exception e){
                //log exception
                logger.error(e.getStackTrace());
            } finally {
                readLock.unlock();
            }

            if (account == null) {//neu record chua co thi ta tien hanh ghi vao db
                account = new Account(userId, balance);

                writeLock.lock();
                try{
                    session.save(account);
                }finally {
                    writeLock.unlock();
                }
            }

            return account;
        });

        return future.get().getBalance();
    }

    public long setBalance(String userId, long balance) throws ExecutionException, InterruptedException {
        Future<Account> future = executorService.submit(()->{
            Account account = null;

            readLock.lock();
            try{
                account = session.find(Account.class, userId);
            }finally {
                readLock.unlock();
            }

            if (account == null) {
                account = new Account(userId, balance);

                writeLock.lock();
                try{
                    session.save(account);
                }finally {
                    writeLock.unlock();
                }
            }else {//neu da co thi ta set lai balance
                account.setBalance(balance);

                writeLock.lock();
                try{
                    session.update(account);
                } finally {
                    writeLock.unlock();
                }
            }

            return account;
        });

        return future.get().getBalance();
    }

    public long increaseBalance(String userId, long amount) throws ExecutionException, InterruptedException {
        Future<Account> future = executorService.submit(()->{
            Account account = null;

            readLock.lock();
            try{
                account = session.find(Account.class, userId);
            }finally {
                readLock.unlock();
            }

            if (account == null) {
                account = new Account(userId, amount);

                writeLock.lock();
                try {
                    session.save(account);
                } finally {
                    writeLock.unlock();
                }
            }else{
                account.setBalance(account.getBalance() + amount);

                writeLock.lock();
                try {
                    session.update(account);
                }finally {
                    writeLock.unlock();
                }
            }

            return account;
        });

        return future.get().getBalance();
     }

     public long decreaseBalance(String userId, long amount) throws ExecutionException, InterruptedException {
        Future<Account> future = executorService.submit(()->{
            Account account = null;

            readLock.lock();
            try{
                account = session.find(Account.class, userId);
            }finally {
                readLock.unlock();
            }

            if (account == null) {
                account = new Account(userId, amount);

                writeLock.lock();
                try {
                    session.save(account);
                } finally {
                    writeLock.unlock();
                }
            }else{
                account.setBalance(account.getBalance() - amount);

                writeLock.lock();
                try {
                    session.update(account);
                }finally {
                    writeLock.unlock();
                }
            }

            return account;
        });

        return future.get().getBalance();
    }
}
