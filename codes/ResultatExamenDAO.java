package gestionclub.dao;

import gestionclub.NewHibernateUtil;
import gestionclublecteur.modele.Lecteur;
import gestionclublecteur.modele.Livre;
import gestionclublecteur.modele.ResultatExamen;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Collections;
import java.util.List;

public class ResultatExamenDAO {

    @SuppressWarnings("unchecked")
    public List<ResultatExamen> getAll() {
        Session session = NewHibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery(
                "FROM ResultatExamen r " +
                "LEFT JOIN FETCH r.lecteur " +
                "LEFT JOIN FETCH r.livre " +
                "ORDER BY r.dateExamen DESC"
            ).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            session.close();
        }
    }

    /** Récupère tous les résultats d'un lecteur donné */
    @SuppressWarnings("unchecked")
    public List<ResultatExamen> getByLecteur(Long lecteurId) {
        Session session = NewHibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery(
                "FROM ResultatExamen r " +
                "LEFT JOIN FETCH r.livre " +
                "WHERE r.lecteur.id = :id " +
                "ORDER BY r.dateExamen DESC"
            ).setParameter("id", lecteurId).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            session.close();
        }
    }

    /** Récupère tous les résultats pour un livre donné */
    @SuppressWarnings("unchecked")
    public List<ResultatExamen> getByLivre(Long livreId) {
        Session session = NewHibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery(
                "FROM ResultatExamen r " +
                "LEFT JOIN FETCH r.lecteur " +
                "WHERE r.livre.id = :id " +
                "ORDER BY r.score DESC"
            ).setParameter("id", livreId).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            session.close();
        }
    }

    /** Vérifie si un lecteur a déjà passé l'examen d'un livre */
    @SuppressWarnings("unchecked")
    public boolean existeDeja(Lecteur lecteur, Livre livre) {
        Session session = NewHibernateUtil.getSessionFactory().openSession();
        try {
            Long count = (Long) session.createQuery(
                "SELECT COUNT(r) FROM ResultatExamen r " +
                "WHERE r.lecteur.id = :lid AND r.livre.id = :bid"
            ).setParameter("lid", lecteur.getId())
             .setParameter("bid", livre.getId())
             .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    /** Score moyen pour un livre donné */
    public double scoreMoyen(Long livreId) {
        Session session = NewHibernateUtil.getSessionFactory().openSession();
        try {
            Double avg = (Double) session.createQuery(
                "SELECT AVG(r.score) FROM ResultatExamen r WHERE r.livre.id = :id"
            ).setParameter("id", livreId).uniqueResult();
            return avg != null ? avg : 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        } finally {
            session.close();
        }
    }

    public void saveOrUpdate(ResultatExamen r) {
        Session session = NewHibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.saveOrUpdate(r);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void delete(ResultatExamen r) {
        Session session = NewHibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            ResultatExamen managed = (ResultatExamen) session.get(ResultatExamen.class, r.getId());
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
