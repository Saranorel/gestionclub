package gestionclublecteur.modele;

import javax.persistence.*;
import java.sql.Date;

/**
 * Enregistre le résultat d'un examen passé par un lecteur pour un livre donné.
 * Un lecteur peut passer l'examen d'un livre une seule fois (contrainte UNIQUE sur lecteur+livre).
 */
@Entity
@Table(
    name = "RESULTAT_EXAMEN",
    uniqueConstraints = @UniqueConstraint(columnNames = {"LECTEUR_ID", "LIVRE_ID"})
)
@SequenceGenerator(name = "examen_seq", sequenceName = "SEQ_EXAMEN", allocationSize = 1)
public class ResultatExamen {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "examen_seq")
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "LECTEUR_ID", nullable = false)
    private Lecteur lecteur;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "LIVRE_ID", nullable = false)
    private Livre livre;

    /** Score obtenu à l'examen (sur 100) */
    @Column(name = "SCORE", nullable = false)
    private Integer score;

    /** Date à laquelle l'examen a été passé */
    @Column(name = "DATEEXAMEN")
    private Date dateExamen;

    /** Mention calculée automatiquement selon le score */
    @Column(name = "MENTION", length = 30)
    private String mention;

    public ResultatExamen() {}

    public ResultatExamen(Lecteur lecteur, Livre livre, Integer score, Date dateExamen) {
        this.lecteur     = lecteur;
        this.livre       = livre;
        this.score       = score;
        this.dateExamen  = dateExamen;
        this.mention     = calculerMention(score);
    }

    /** Calcule la mention à partir du score (sur 100) */
    public static String calculerMention(int score) {
        if (score >= 90) return "Excellent";
        if (score >= 75) return "Très Bien";
        if (score >= 60) return "Bien";
        if (score >= 50) return "Passable";
        return "Insuffisant";
    }

    public Long getId()                   { return id; }
    public void setId(Long id)            { this.id = id; }
    public Lecteur getLecteur()           { return lecteur; }
    public void setLecteur(Lecteur l)     { this.lecteur = l; }
    public Livre getLivre()               { return livre; }
    public void setLivre(Livre l)         { this.livre = l; }
    public Integer getScore()             { return score; }
    public void setScore(Integer s)       { this.score = s; this.mention = calculerMention(s); }
    public Date getDateExamen()           { return dateExamen; }
    public void setDateExamen(Date d)     { this.dateExamen = d; }
    public String getMention()            { return mention; }
    public void setMention(String m)      { this.mention = m; }
}