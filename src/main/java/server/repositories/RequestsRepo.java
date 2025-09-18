package server.repositories;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import server.RedisCache;
import server.model.Requests;

import org.hibernate.Transaction;

import java.util.List;

public class RequestsRepo {
    private SessionFactory sessionFactory;
    private RedisCache redis;

    public RequestsRepo() {
        this.sessionFactory = new Configuration().configure().buildSessionFactory();
        this.redis = new RedisCache();
    }

    public void addRequest(Requests request) throws HibernateException {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(request);
            transaction.commit();

            redis.addRequest(request);
        } catch (HibernateException e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public List<Requests> getAllRequests() throws HibernateException {
        List<Requests> cached = redis.getHistory();
        if (!cached.isEmpty()) {
            return cached;
        }

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            List<Requests> requests = session.createQuery("from Requests", Requests.class).list();
            transaction.commit();

            for (Requests r : requests) {
                redis.addRequest(r);
            }

            return requests;
        }
    }
}


