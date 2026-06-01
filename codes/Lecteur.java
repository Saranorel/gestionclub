package gestionclublecteur.modele;

import javax.persistence.*;

@Entity
@Table(name = "LECTEUR")
@SequenceGenerator(name = "lecteur_seq", sequenceName = "SEQ_LECTEUR", allocationSize = 1)
public class Lecteur {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lecteur_seq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOM", length = 100)
    private String nom;

    @Column(name = "PRENOM", length = 100)
    private String prenom;

    @Column(name = "EMAIL", length = 150, unique = true)
    private String email;

    public Lecteur() {}

    public Lecteur(String nom, String prenom, String email) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }

    public Long getId()            { return id; }
    public void setId(Long id)     { this.id = id; }
    public String getNom()         { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom()         { return prenom; }
    public void setPrenom(String p)   { this.prenom = p; }
    public String getEmail()          { return email; }
    public void setEmail(String e)    { this.email = e; }

    @Override
    public String toString() {
        return (nom != null ? nom : "") + " " + (prenom != null ? prenom : "");
    }
}
