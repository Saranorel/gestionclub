package gestionclub.dao;

import gestionclub.NewHibernateUtil;
import gestionclublecteur.modele.Livre;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Collections;
import java.util.List;

public class LivreDAO {

    @SuppressWarnings("unchecked")
    public List<Livre> getAll() {
        Session session = NewHibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery("FROM Livre ORDER BY titre").list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            session.close();
        }
    }

    public void saveOrUpdate(Livre livre) {
        Session session = NewHibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.saveOrUpdate(livre);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void delete(Livre livre) {
        Session session = NewHibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Livre managed = (Livre) session.get(Livre.class, livre.getId());
            if (managed != null) session.delete(managed);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }
}