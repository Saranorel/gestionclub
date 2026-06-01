package gestionclub.dao;

import gestionclub.NewHibernateUtil;
import gestionclublecteur.modele.SuiviLecture;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Collections;
import java.util.List;

public class SuiviDAO {

    @SuppressWarnings("unchecked")
    public List<SuiviLecture> getAll() {
        Session session = NewHibernateUtil.getSessionFactory().openSession();
        try {
            // JOIN FETCH pour charger Lecteur et Livre en une seule requête (évite les LazyLoad)
            return session.createQuery(
                    "FROM SuiviLecture s " +
                    "LEFT JOIN FETCH s.lecteur " +
                    "LEFT JOIN FETCH s.livre " +
                    "ORDER BY s.dateDebut DESC"
            ).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            session.close();
        }
    }

    public void saveOrUpdate(SuiviLecture suivi) {
        Session session = NewHibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.saveOrUpdate(suivi);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void delete(SuiviLecture suivi) {
        Session session = NewHibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            SuiviLecture managed = (SuiviLecture) session.get(SuiviLecture.class, suivi.getId());
            if (managed != null) session.delete(managed);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    @SuppressWarnings("unchecked")
    public long countLecteursParLivre(Long idLivre) {
        Session session = NewHibernateUtil.getSessionFactory().openSession();
        try {
            Long result = (Long) session
                    .createQuery("SELECT COUNT(s) FROM SuiviLecture s WHERE s.livre.id = :id")
                    .setParameter("id", idLivre)
                    .uniqueResult();
            return result != null ? result : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        } finally {
            session.close();
        }
    }
}
