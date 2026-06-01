package gestionclub.dao;

import gestionclub.NewHibernateUtil;
import gestionclublecteur.modele.Lecteur;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Collections;
import java.util.List;

public class LecteurDAO {

    @SuppressWarnings("unchecked")
    public List<Lecteur> getAll() {
        Session session = NewHibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery("FROM Lecteur ORDER BY nom, prenom").list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            session.close();
        }
    }

    public void saveOrUpdate(Lecteur lecteur) {
        Session session = NewHibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.saveOrUpdate(lecteur);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e; // Re-lancer pour que l'UI affiche l'erreur
        } finally {
            session.close();
        }
    }

    public void delete(Lecteur lecteur) {
        Session session = NewHibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            // Recharger l'entité dans cette session pour éviter les détachements
            Lecteur managed = (Lecteur) session.get(Lecteur.class, lecteur.getId());
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