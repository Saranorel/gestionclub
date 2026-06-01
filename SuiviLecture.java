package gestionclublecteur.modele;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "SUIVI_LECTURE")
@SequenceGenerator(name = "suivi_seq", sequenceName = "SEQ_SUIVI", allocationSize = 1)
public class SuiviLecture {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suivi_seq")
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "LECTEUR_ID")
    private Lecteur lecteur;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "LIVRE_ID")
    private Livre livre;

    // Oracle ne supporte pas camelCase sans mappage explicite
    @Column(name = "DATEDEBUT")
    private Date dateDebut;

    @Column(name = "DATEFIN")
    private Date dateFin;

    @Column(name = "STATUT", length = 50)
    private String statut;

    public SuiviLecture() {}

    public Long getId()                   { return id; }
    public void setId(Long id)            { this.id = id; }
    public Lecteur getLecteur()           { return lecteur; }
    public void setLecteur(Lecteur l)     { this.lecteur = l; }
    public Livre getLivre()               { return livre; }
    public void setLivre(Livre l)         { this.livre = l; }
    public Date getDateDebut()            { return dateDebut; }
    public void setDateDebut(Date d)      { this.dateDebut = d; }
    public Date getDateFin()              { return dateFin; }
    public void setDateFin(Date d)        { this.dateFin = d; }
    public String getStatut()             { return statut; }
    public void setStatut(String s)       { this.statut = s; }
}