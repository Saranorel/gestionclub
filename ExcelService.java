package gestionclub;

import gestionclublecteur.modele.Lecteur;
import gestionclublecteur.modele.ResultatExamen;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Service d'import/export Excel.
 *
 * Format du fichier d'import (ligne 0 = en-tête ignorée) :
 *   A = Nom lecteur
 *   B = Prénom lecteur
 *   C = Email lecteur
 *   D = Titre du livre  (doit exister en BD)
 *   E = Score examen    (0–20)
 *   F = Baji ?          (oui / yes / 1  → étudiant Baji)
 */
public class ExcelService {

    // ------------------------------------------------------------------
    // Classe interne légère pour stocker un étudiant Baji (non persisté)
    // ------------------------------------------------------------------
    public static class EtudiantBaji {
        public String nom;
        public String prenom;
        public String email;
        public int    score;

        public EtudiantBaji(String nom, String prenom, String email, int score) {
            this.nom    = nom;
            this.prenom = prenom;
            this.email  = email;
            this.score  = score;
        }
    }

    // ------------------------------------------------------------------
    // IMPORT
    // Retourne une liste des étudiants Baji trouvés dans le fichier.
    // Passe le résultat via un objet résultat pour ne pas changer la
    // signature publique initiale.
    // ------------------------------------------------------------------
    /** Résultat d'un import : message texte + liste des étudiants Baji. */
    public static class ImportResult {
        public String             message;
        public List<EtudiantBaji> etudiantsBaji = new ArrayList<EtudiantBaji>();

        public ImportResult(String message) { this.message = message; }
    }

    @SuppressWarnings("unchecked")
    public ImportResult importerDonneesAvecBaji(File file) {
        if (file == null || !file.exists()) {
            return new ImportResult("Fichier introuvable.");
        }

        Session     session    = null;
        Transaction tx         = null;
        int         nbLecteurs = 0;
        int         nbExamens  = 0;
        int         nbIgnores  = 0;
        List<EtudiantBaji> baji = new ArrayList<EtudiantBaji>();

        try (FileInputStream fis      = new FileInputStream(file);
             Workbook        workbook = WorkbookFactory.create(fis)) {

            Sheet       sheet = workbook.getSheetAt(0);
            DataFormatter fmt  = new DataFormatter();

            session = NewHibernateUtil.getSessionFactory().openSession();
            tx      = session.beginTransaction();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // en-tête

                String nom        = fmt.formatCellValue(row.getCell(0)).trim();
                String prenom     = fmt.formatCellValue(row.getCell(1)).trim();
                String email      = fmt.formatCellValue(row.getCell(2)).trim();
                String titreLivre = fmt.formatCellValue(row.getCell(3)).trim();
                String scoreStr   = fmt.formatCellValue(row.getCell(4)).trim();
                // Colonne F : Baji ? (نعم / oui / yes / 1)
                String bajiStr    = fmt.formatCellValue(row.getCell(5)).trim().toLowerCase();
                boolean estBaji   = bajiStr.equals("نعم") || bajiStr.equals("oui")
                                 || bajiStr.equals("yes") || bajiStr.equals("1");

                // Ligne vide → ignorer
                if (nom.isEmpty() && email.isEmpty()) continue;
                if (email.isEmpty()) { nbIgnores++; continue; }

                // 1. Lecteur : chercher ou créer
                List<Lecteur> lecteurs = session
                        .createQuery("FROM Lecteur WHERE email = :email")
                        .setParameter("email", email)
                        .list();

                Lecteur lecteur;
                if (lecteurs.isEmpty()) {
                    if (nom.isEmpty()) { nbIgnores++; continue; }
                    lecteur = new Lecteur(nom, prenom, email);
                    session.save(lecteur);
                    session.flush();
                    nbLecteurs++;
                } else {
                    lecteur = lecteurs.get(0);
                }

                // 2. Résultat examen (si livre + score fournis)
                if (titreLivre.isEmpty() || scoreStr.isEmpty()) continue;

                List list = session
                        .createQuery("FROM Livre WHERE UPPER(titre) = UPPER(:titre)")
                        .setParameter("titre", titreLivre)
                        .list();
                if (list.isEmpty()) { nbIgnores++; continue; }

                gestionclublecteur.modele.Livre livre =
                        (gestionclublecteur.modele.Livre) list.get(0);

                int score = 0;
                try { score = (int) Double.parseDouble(scoreStr); }
                catch (NumberFormatException ignored) { nbIgnores++; continue; }
                if (score < 0 || score > 100) { nbIgnores++; continue; }

                Long count = (Long) session
                        .createQuery("SELECT COUNT(r) FROM ResultatExamen r " +
                                     "WHERE r.lecteur.id = :lid AND r.livre.id = :bid")
                        .setParameter("lid", lecteur.getId())
                        .setParameter("bid", livre.getId())
                        .uniqueResult();

                if (count != null && count > 0) { nbIgnores++; continue; }

                ResultatExamen re = new ResultatExamen(
                        lecteur, livre, score, new Date(System.currentTimeMillis()));
                session.save(re);
                nbExamens++;

                // 3. Si Baji → ajouter à la liste
                if (estBaji) {
                    baji.add(new EtudiantBaji(
                            lecteur.getNom(), lecteur.getPrenom(), lecteur.getEmail(), score));
                }
            }

            tx.commit();

            String msg = "Import terminé : " + nbLecteurs + " lecteur(s) créé(s), "
                    + nbExamens + " examen(s) enregistré(s)"
                    + (nbIgnores > 0 ? ", " + nbIgnores + " ligne(s) ignorée(s)." : ".")
                    + (baji.size() > 0 ? " — " + baji.size() + " étudiant(s) Baji détecté(s)." : "");

            ImportResult result = new ImportResult(msg);
            result.etudiantsBaji = baji;
            return result;

        } catch (Exception e) {
            if (tx != null) try { tx.rollback(); } catch (Exception ignored) {}
            e.printStackTrace();
            return new ImportResult("Erreur d'importation : " + e.getMessage());
        } finally {
            if (session != null && session.isOpen())
                try { session.close(); } catch (Exception ignored) {}
        }
    }

    // Méthode de compatibilité (conservée pour ne pas casser d'éventuels
    // autres appels) — délègue simplement à la nouvelle méthode.
    public String importerDonnees(File file) {
        return importerDonneesAvecBaji(file).message;
    }

    // ------------------------------------------------------------------
    // IMPORT LECTEURS UNIQUEMENT
    // Format Excel attendu (ligne 0 = en-tête ignorée) :
    //   A = Nom,  B = Prénom,  C = Email
    // Crée uniquement les lecteurs absents (dédoublonnage par email).
    // ------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public String importerLecteurs(File file) {
        if (file == null || !file.exists()) return "Fichier introuvable.";

        Session     session   = null;
        Transaction tx        = null;
        int         nbCreés   = 0;
        int         nbExist   = 0;
        int         nbIgnores = 0;

        try (FileInputStream fis      = new FileInputStream(file);
             Workbook        workbook = WorkbookFactory.create(fis)) {

            Sheet         sheet = workbook.getSheetAt(0);
            DataFormatter fmt   = new DataFormatter();

            session = NewHibernateUtil.getSessionFactory().openSession();
            tx      = session.beginTransaction();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // en-tête ignorée

                String nom    = fmt.formatCellValue(row.getCell(0)).trim();
                String prenom = fmt.formatCellValue(row.getCell(1)).trim();
                String email  = fmt.formatCellValue(row.getCell(2)).trim();

                if (nom.isEmpty() && email.isEmpty()) continue;
                if (email.isEmpty() || nom.isEmpty()) { nbIgnores++; continue; }

                Long count = (Long) session
                        .createQuery("SELECT COUNT(l) FROM Lecteur l WHERE l.email = :email")
                        .setParameter("email", email)
                        .uniqueResult();

                if (count != null && count > 0) {
                    nbExist++;   // lecteur déjà présent → pas de doublon
                } else {
                    session.save(new Lecteur(nom, prenom, email));
                    nbCreés++;
                }
            }

            tx.commit();
            return "Import lecteurs terminé : " + nbCreés + " créé(s)"
                    + (nbExist   > 0 ? ", " + nbExist   + " déjà existant(s)" : "")
                    + (nbIgnores > 0 ? ", " + nbIgnores + " ligne(s) ignorée(s)" : "") + ".";

        } catch (Exception e) {
            if (tx != null) try { tx.rollback(); } catch (Exception ignored) {}
            e.printStackTrace();
            return "Erreur import lecteurs : " + e.getMessage();
        } finally {
            if (session != null && session.isOpen())
                try { session.close(); } catch (Exception ignored) {}
        }
    }

    // ------------------------------------------------------------------
    // EXPORT : tous les résultats d'examens
    // Colonnes : Nom | Prénom | Email | Livre | Score /20 | Mention | Date
    // ------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public String exporterResultats(File file) {
        if (file == null) return "Fichier non spécifié.";

        Session session = null;
        try {
            session = NewHibernateUtil.getSessionFactory().openSession();

            List<ResultatExamen> resultats = session.createQuery(
                    "FROM ResultatExamen r " +
                    "LEFT JOIN FETCH r.lecteur " +
                    "LEFT JOIN FETCH r.livre " +
                    "ORDER BY r.lecteur.nom, r.livre.titre"
            ).list();

            Workbook wb    = new XSSFWorkbook();
            Sheet    sheet = wb.createSheet("Résultats Examens");

            CellStyle headerStyle = creerStyleEntete(wb,
                    IndexedColors.DARK_TEAL, IndexedColors.WHITE);
            CellStyle altStyle    = creerStyleAlternance(wb,
                    IndexedColors.LIGHT_TURQUOISE);

            // En-tête
            String[] cols = {"Nom", "Prénom", "Email", "Livre",
                             "Nb Pages", "Score /100", "Mention", "Date Examen"};
            ecrireEntete(sheet, cols, headerStyle,
                    new int[]{5000, 5000, 7000, 8000, 4000, 4000, 5000, 5000});

            // Données
            int rowNum = 1;
            for (ResultatExamen re : resultats) {
                Row      row      = sheet.createRow(rowNum);
                CellStyle rowStyle = (rowNum % 2 == 0) ? altStyle : null;
                creerCellule(row, 0, re.getLecteur() != null ? re.getLecteur().getNom()    : "", rowStyle);
                creerCellule(row, 1, re.getLecteur() != null ? re.getLecteur().getPrenom() : "", rowStyle);
                creerCellule(row, 2, re.getLecteur() != null ? re.getLecteur().getEmail()  : "", rowStyle);
                creerCellule(row, 3, re.getLivre()   != null ? re.getLivre().getTitre()    : "", rowStyle);
                creerCellule(row, 4, re.getLivre()   != null && re.getLivre().getNombrePages() != null
                        ? String.valueOf(re.getLivre().getNombrePages()) : "", rowStyle);
                creerCellule(row, 5, re.getScore()   != null ? re.getScore() + "/100"      : "", rowStyle);
                creerCellule(row, 6, re.getMention() != null ? re.getMention()             : "", rowStyle);
                creerCellule(row, 7, re.getDateExamen() != null ? re.getDateExamen().toString() : "", rowStyle);
                rowNum++;
            }

            ecrireFichier(wb, file);
            return "Export terminé : " + resultats.size() + " résultat(s) exporté(s) → " + file.getName();

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur d'export : " + e.getMessage();
        } finally {
            if (session != null && session.isOpen())
                try { session.close(); } catch (Exception ignored) {}
        }
    }

    // ------------------------------------------------------------------
    // EXPORT BAJI : exporte uniquement les étudiants Baji passés en param
    // Colonnes : Nom | Prénom | Email | Note /20
    // ------------------------------------------------------------------
    public String exporterEtudiantsBaji(List<EtudiantBaji> etudiants, File file) {
        if (file == null)         return "Fichier non spécifié.";
        if (etudiants == null || etudiants.isEmpty())
            return "Aucun étudiant Baji à exporter.";

        try {
            Workbook wb    = new XSSFWorkbook();
            Sheet    sheet = wb.createSheet("Étudiants Baji");

            CellStyle headerStyle = creerStyleEntete(wb,
                    IndexedColors.DARK_BLUE, IndexedColors.WHITE);
            CellStyle altStyle    = creerStyleAlternance(wb,
                    IndexedColors.PALE_BLUE);

            // En-tête
            String[] cols = {"Nom", "Prénom", "Email", "Note /100"};
            ecrireEntete(sheet, cols, headerStyle,
                    new int[]{5500, 5500, 8000, 4000});

            // Données
            int rowNum = 1;
            for (EtudiantBaji e : etudiants) {
                Row       row      = sheet.createRow(rowNum);
                CellStyle rowStyle = (rowNum % 2 == 0) ? altStyle : null;
                creerCellule(row, 0, e.nom,                           rowStyle);
                creerCellule(row, 1, e.prenom,                        rowStyle);
                creerCellule(row, 2, e.email,                         rowStyle);
                creerCellule(row, 3, e.score + "/100",                rowStyle);
                rowNum++;
            }

            ecrireFichier(wb, file);
            return "Export Baji terminé : " + etudiants.size()
                    + " étudiant(s) exporté(s) → " + file.getName();

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur d'export Baji : " + e.getMessage();
        }
    }

    // ------------------------------------------------------------------
    // Helpers privés
    // ------------------------------------------------------------------
    private CellStyle creerStyleEntete(Workbook wb,
                                       IndexedColors fond, IndexedColors texte) {
        CellStyle s = wb.createCellStyle();
        Font      f = wb.createFont();
        f.setBold(true);
        f.setColor(texte.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(fond.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.THIN);
        return s;
    }

    private CellStyle creerStyleAlternance(Workbook wb, IndexedColors fond) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(fond.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private void ecrireEntete(Sheet sheet, String[] cols,
                              CellStyle style, int[] widths) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < cols.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(cols[i]);
            cell.setCellStyle(style);
            if (widths != null && i < widths.length)
                sheet.setColumnWidth(i, widths[i]);
        }
    }

    private void creerCellule(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        if (style != null) cell.setCellStyle(style);
    }

    private void ecrireFichier(Workbook wb, File file) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            wb.write(fos);
        }
        wb.close();
    }
}