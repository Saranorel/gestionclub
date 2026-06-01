package gestionclublecteur.modele;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "LIVRE")
@SequenceGenerator(name = "livre_seq", sequenceName = "SEQ_LIVRE", allocationSize = 1)
public class Livre {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "livre_seq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "TITRE", length = 200)
    private String titre;

    @Column(name = "AUTEUR", length = 150)
    private String auteur;

    // ISBN supprimé — remplacé par les informations suivantes :

    @Column(name = "NOMBREPAGES")
    private Integer nombrePages;

    @Column(name = "DATEDEBUT")
    private Date dateDebut;

    @Column(name = "DATEFIN")
    private Date dateFin;

    public Livre() {}

    public Livre(String titre, String auteur, Integer nombrePages, Date dateDebut, Date dateFin) {
        this.titre      = titre;
        this.auteur     = auteur;
        this.nombrePages = nombrePages;
        this.dateDebut  = dateDebut;
        this.dateFin    = dateFin;
    }

    public Long getId()                { return id; }
    public void setId(Long id)         { this.id = id; }
    public String getTitre()           { return titre; }
    public void setTitre(String t)     { this.titre = t; }
    public String getAuteur()          { return auteur; }
    public void setAuteur(String a)    { this.auteur = a; }
    public Integer getNombrePages()    { return nombrePages; }
    public void setNombrePages(Integer n) { this.nombrePages = n; }
    public Date getDateDebut()         { return dateDebut; }
    public void setDateDebut(Date d)   { this.dateDebut = d; }
    public Date getDateFin()           { return dateFin; }
    public void setDateFin(Date d)     { this.dateFin = d; }

    @Override
    public String toString() { return titre != null ? titre : ""; }
}
