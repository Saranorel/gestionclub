package gestionclub;

import gestionclub.dao.LecteurDAO;
import gestionclub.dao.LivreDAO;
import gestionclub.dao.ResultatExamenDAO;
import gestionclub.dao.SuiviDAO;
import gestionclublecteur.modele.Lecteur;
import gestionclublecteur.modele.Livre;
import gestionclublecteur.modele.ResultatExamen;
import gestionclublecteur.modele.SuiviLecture;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.File;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class Gestionclub extends Application {

    private final LecteurDAO        lecteurDAO   = new LecteurDAO();
    private final LivreDAO          livreDAO     = new LivreDAO();
    private final SuiviDAO          suiviDAO     = new SuiviDAO();
    private final ResultatExamenDAO examenDAO    = new ResultatExamenDAO();
    private final ExcelService      excelService = new ExcelService();

    private final TableView<SuiviLecture>          tableDashboard = new TableView<>();
    private final TableView<Lecteur>               tableLecteurs  = new TableView<>();
    private final TableView<Livre>                 tableLivres    = new TableView<>();
    private final TableView<ResultatExamen>        tableExamens   = new TableView<>();
    private final ComboBox<Livre>   cbStatsLivres = new ComboBox<>();
    private final ComboBox<Lecteur> cbFormLecteur = new ComboBox<>();
    private final ComboBox<Livre>   cbFormLivre   = new ComboBox<>();
    private final ComboBox<Lecteur> cbExamFiltreL = new ComboBox<>();
    private final ComboBox<Livre>   cbExamFiltreB = new ComboBox<>();
    private final ComboBox<Lecteur> cbExamLecteur = new ComboBox<>();
    private final ComboBox<Livre>   cbExamLivre   = new ComboBox<>();

    private final Label lblStatus = new Label("Prêt");

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Gestion des Lecteurs de Sana");
        afficherEcranConnexion(primaryStage);
    }

    // ================================================================
    // ECRAN DE CONNEXION — formulaire simple centré
    // ================================================================
    private void afficherEcranConnexion(final Stage stage) {

        // ---- Titre principal affiché AU-DESSUS de la carte ----
        Label lblTitre = new Label("Gestion des Lecteurs");
        lblTitre.setWrapText(true);
        lblTitre.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblTitre.setStyle(
            "-fx-font-family: 'Poppins', 'Segoe UI', sans-serif;" +
            "-fx-font-size: 28px;" +
            "-fx-font-weight: 700;" +
            "-fx-text-fill: white;");

        Label lblSub = new Label("Plateforme de gestion des lecteurs");
        lblSub.setWrapText(true);
        lblSub.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblSub.setStyle(
            "-fx-font-family: 'Poppins', 'Segoe UI', sans-serif;" +
            "-fx-font-size: 13px;" +
            "-fx-text-fill: rgba(255,255,255,0.70);");

        VBox headerBox = new VBox(8, lblTitre, lblSub);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setMaxWidth(380);

        // ---- Formulaire de connexion ----
        Label lblUser = new Label("Identifiant");
        lblUser.getStyleClass().add("login-field-label");
        final TextField txtUser = new TextField();
        txtUser.setPromptText("Entrez votre identifiant");
        txtUser.getStyleClass().add("login-field");
        txtUser.setMaxWidth(Double.MAX_VALUE);

        Label lblPass = new Label("Mot de passe");
        lblPass.getStyleClass().add("login-field-label");
        final PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Entrez votre mot de passe");
        txtPass.getStyleClass().add("login-field");
        txtPass.setMaxWidth(Double.MAX_VALUE);

        final Label lblErr = new Label();
        lblErr.getStyleClass().add("login-error");
        lblErr.setWrapText(true);
        lblErr.setMaxWidth(300);
        lblErr.setVisible(false);

        final Button btnConnexion = new Button("Se connecter");
        btnConnexion.getStyleClass().add("btn-primary");
        btnConnexion.setMaxWidth(Double.MAX_VALUE);
        btnConnexion.setPrefHeight(44);

        VBox formBox = new VBox(12,
                lblUser, txtUser,
                lblPass, txtPass,
                lblErr,
                btnConnexion);
        formBox.setAlignment(Pos.TOP_LEFT);
        formBox.setPadding(new Insets(32, 40, 32, 40));
        formBox.setMaxWidth(380);
        formBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 14px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.20), 24, 0, 0, 8);");

        // Pied de page
        Label lblFooter = new Label("Système de gestion interne — Tous droits réservés");
        lblFooter.getStyleClass().add("login-footer");

        VBox root = new VBox(24, headerBox, formBox, lblFooter);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1E2A3A;");

        // Logique connexion
        final Runnable doLogin = () -> {
            String u = txtUser.getText().trim();
            String p = txtPass.getText();
            lblErr.setVisible(false);
            if (u.isEmpty() || p.isEmpty()) {
                lblErr.setText("Veuillez remplir tous les champs.");
                lblErr.setVisible(true);
                return;
            }
            if ("admin".equals(u) && "admin".equals(p)) {
                try {
                    afficherDashboard(stage);
                } catch (Exception ex) {
                    lblErr.setText("Erreur de connexion : " + ex.getMessage());
                    lblErr.setVisible(true);
                }
            } else {
                lblErr.setText("Identifiant ou mot de passe incorrect.");
                lblErr.setVisible(true);
                txtPass.clear();
                txtPass.requestFocus();
            }
        };

        btnConnexion.setOnAction(e -> doLogin.run());
        txtPass.setOnAction(e -> doLogin.run());
        txtUser.setOnAction(e -> txtPass.requestFocus());

        Scene scene = new Scene(root, 600, 480);
        css(scene);
        stage.setScene(scene);
        stage.setMinWidth(480);
        stage.setMinHeight(400);
        stage.setResizable(true);
        stage.show();
        txtUser.requestFocus();
    }

    // ================================================================
    // ECRAN PRINCIPAL
    // ================================================================
    private void afficherDashboard(Stage stage) {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().add(creerOngletDashboard(stage));
        tabPane.getTabs().add(creerOngletLecteurs());
        tabPane.getTabs().add(creerOngletLivres());
        tabPane.getTabs().add(creerOngletExamens(stage));

        tabPane.getSelectionModel().selectedItemProperty().addListener(
            (obs, o, n) -> actualiserTout());

        lblStatus.getStyleClass().add("status-bar");
        HBox statusBar = new HBox(lblStatus);
        statusBar.getStyleClass().add("status-bar-bg");
        statusBar.setPadding(new Insets(4, 12, 4, 12));

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 1280, 760);
        css(scene);
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        actualiserTout();
    }

    // ================================================================
    // ONGLET DASHBOARD
    // ================================================================
    private Tab creerOngletDashboard(final Stage stage) {
        Tab tab = new Tab("  Dashboard  ");

        Label lblStats = new Label("Statistiques livre :");
        lblStats.getStyleClass().add("toolbar-label");
        configurerCbLivre(cbStatsLivres);
        cbStatsLivres.setPromptText("Choisir un livre...");
        cbStatsLivres.setPrefWidth(260);

        final Label lblCount = new Label();
        lblCount.getStyleClass().add("stat-badge");

        cbStatsLivres.setOnAction(e -> {
            Livre l = cbStatsLivres.getValue();
            if (l != null) {
                long count = suiviDAO.countLecteursParLivre(l.getId());
                lblCount.setText(count + " lecteur" + (count > 1 ? "s" : ""));
            } else {
                lblCount.setText("");
            }
        });

        HBox toolbar = new HBox(12, lblStats, cbStatsLivres, lblCount);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("toolbar");

        TableColumn<SuiviLecture, String> cLec = new TableColumn<>("Lecteur");
        cLec.setCellValueFactory(d -> {
            Lecteur lec = d.getValue().getLecteur();
            return new SimpleStringProperty(lec != null ? lec.getNom() + " " + lec.getPrenom() : "-");
        });

        TableColumn<SuiviLecture, String> cLiv = new TableColumn<>("Livre");
        cLiv.setCellValueFactory(d -> {
            Livre liv = d.getValue().getLivre();
            return new SimpleStringProperty(liv != null ? liv.getTitre() : "-");
        });

        // Colonne Nb Pages dans le dashboard
        TableColumn<SuiviLecture, String> cPages = new TableColumn<>("Nb Pages");
        cPages.setCellValueFactory(d -> {
            Livre liv = d.getValue().getLivre();
            Integer p = (liv != null) ? liv.getNombrePages() : null;
            return new SimpleStringProperty(p != null ? String.valueOf(p) : "-");
        });
        cPages.setMaxWidth(80);

        TableColumn<SuiviLecture, String> cStat = new TableColumn<>("Statut");
        cStat.setCellValueFactory(new PropertyValueFactory<>("statut"));

        TableColumn<SuiviLecture, String> cDate = new TableColumn<>("Date début");
        cDate.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));

        tableDashboard.getColumns().clear();
        tableDashboard.getColumns().addAll(cLec, cLiv, cPages, cStat, cDate);
        tableDashboard.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableDashboard.setPlaceholder(new Label("Aucune donnée."));

        Label lblForm = new Label("Associer Lecteur / Livre");
        lblForm.getStyleClass().add("form-title");
        configurerCbLecteur(cbFormLecteur);
        cbFormLecteur.setPromptText("Sélectionner un lecteur...");
        cbFormLecteur.setMaxWidth(Double.MAX_VALUE);
        configurerCbLivre(cbFormLivre);
        cbFormLivre.setPromptText("Sélectionner un livre...");
        cbFormLivre.setMaxWidth(Double.MAX_VALUE);
        final TextField txtStatut = new TextField();
        txtStatut.setPromptText("Statut (ex: En cours, Terminé...)");

        Button btnAssocier  = btn("Ajouter l'association",  "btn-primary");
        Button btnModifier  = btn("Modifier la sélection",  "btn-accent");
        Button btnSupprimer = btn("Supprimer la sélection", "btn-danger");

        // Clic sur une ligne → remplit le formulaire
        tableDashboard.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                cbFormLecteur.setValue(selected.getLecteur());
                cbFormLivre.setValue(selected.getLivre());
                txtStatut.setText(selected.getStatut() != null ? selected.getStatut() : "");
            }
        });

        btnAssocier.setOnAction(e -> {
            Lecteur lec = cbFormLecteur.getValue();
            Livre   liv = cbFormLivre.getValue();
            if (lec == null || liv == null) { afficherInfo("Sélectionnez un lecteur ET un livre."); return; }
            try {
                SuiviLecture sl = new SuiviLecture();
                sl.setLecteur(lec); sl.setLivre(liv);
                String s = txtStatut.getText().trim();
                sl.setStatut(s.isEmpty() ? "En cours" : s);
                sl.setDateDebut(new Date(System.currentTimeMillis()));
                suiviDAO.saveOrUpdate(sl);
                actualiserTout();
                cbFormLecteur.getSelectionModel().clearSelection();
                cbFormLivre.getSelectionModel().clearSelection();
                txtStatut.clear();
                tableDashboard.getSelectionModel().clearSelection();
                setStatus("Association ajoutée.");
            } catch (Exception ex) { ex.printStackTrace(); afficherErreur("Erreur Oracle :\n" + ex.getMessage()); }
        });

        btnModifier.setOnAction(e -> {
            SuiviLecture sl = tableDashboard.getSelectionModel().getSelectedItem();
            if (sl == null) { afficherInfo("Sélectionnez une ligne à modifier."); return; }
            Lecteur lec = cbFormLecteur.getValue();
            Livre   liv = cbFormLivre.getValue();
            if (lec == null || liv == null) { afficherInfo("Sélectionnez un lecteur ET un livre."); return; }
            try {
                sl.setLecteur(lec); sl.setLivre(liv);
                String s = txtStatut.getText().trim();
                sl.setStatut(s.isEmpty() ? "En cours" : s);
                suiviDAO.saveOrUpdate(sl);
                actualiserTout();
                cbFormLecteur.getSelectionModel().clearSelection();
                cbFormLivre.getSelectionModel().clearSelection();
                txtStatut.clear();
                tableDashboard.getSelectionModel().clearSelection();
                setStatus("Association modifiée.");
            } catch (Exception ex) { ex.printStackTrace(); afficherErreur("Erreur Oracle :\n" + ex.getMessage()); }
        });

        btnSupprimer.setOnAction(e -> {
            SuiviLecture sl = tableDashboard.getSelectionModel().getSelectedItem();
            if (sl == null) { afficherInfo("Sélectionnez une ligne à supprimer."); return; }
            try {
                suiviDAO.delete(sl); actualiserTout();
                tableDashboard.getSelectionModel().clearSelection();
                setStatus("Association supprimée.");
            }
            catch (Exception ex) { ex.printStackTrace(); afficherErreur("Impossible de supprimer :\n" + ex.getMessage()); }
        });

        VBox formBox = formPanel(lblForm,
                "Lecteur :", cbFormLecteur,
                "Livre :",   cbFormLivre,
                "Statut :",  txtStatut,
                btnAssocier, btnModifier, btnSupprimer);

        BorderPane layout = new BorderPane();
        layout.setTop(toolbar);
        layout.setCenter(tableDashboard);
        layout.setRight(formBox);
        BorderPane.setMargin(formBox, new Insets(0, 0, 0, 12));
        tab.setContent(layout);
        return tab;
    }

    // ================================================================
    // ONGLET LECTEURS
    // ================================================================
    private Tab creerOngletLecteurs() {
        Tab tab = new Tab("  Lecteurs  ");

        TableColumn<Lecteur, String> cNom   = new TableColumn<>("Nom");
        cNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        TableColumn<Lecteur, String> cPren  = new TableColumn<>("Prénom");
        cPren.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        TableColumn<Lecteur, String> cEmail = new TableColumn<>("Email");
        cEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        tableLecteurs.getColumns().clear();
        tableLecteurs.getColumns().addAll(cNom, cPren, cEmail);
        tableLecteurs.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableLecteurs.setPlaceholder(new Label("Aucun lecteur enregistré."));

        // Bouton import Excel lecteurs
        final Button btnImportLecteurs = new Button("Importer lecteurs (Excel)");
        btnImportLecteurs.getStyleClass().add("btn-import");
        btnImportLecteurs.setMaxWidth(Control.USE_PREF_SIZE);
        btnImportLecteurs.setOnAction(e -> {
            Stage st = (Stage) btnImportLecteurs.getScene().getWindow();
            FileChooser fc = new FileChooser();
            fc.setTitle("Choisir un fichier Excel de lecteurs");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx", "*.xls"));
            File file = fc.showOpenDialog(st);
            if (file != null) {
                setStatus("Import lecteurs en cours...");
                btnImportLecteurs.setDisable(true);
                final File ff = file;
                Task<String> task = new Task<String>() {
                    @Override protected String call() {
                        return excelService.importerLecteurs(ff);
                    }
                };
                task.setOnSucceeded(ev -> {
                    actualiserTout();
                    setStatus(task.getValue());
                    afficherInfo(task.getValue());
                    btnImportLecteurs.setDisable(false);
                });
                task.setOnFailed(ev -> {
                    setStatus("Erreur import lecteurs.");
                    btnImportLecteurs.setDisable(false);
                });
                new Thread(task).start();
            }
        });

        // Toolbar avec bouton import
        Label lblInfo = new Label("");
        lblInfo.getStyleClass().add("toolbar-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(10, lblInfo, spacer, btnImportLecteurs);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("toolbar");

        Label lblForm = new Label("Ajouter / Modifier un Lecteur");
        lblForm.getStyleClass().add("form-title");
        final TextField txtNom    = new TextField(); txtNom.setPromptText("Nom *");
        final TextField txtPrenom = new TextField(); txtPrenom.setPromptText("Prénom");
        final TextField txtEmail  = new TextField(); txtEmail.setPromptText("Email *");

        Button btnAjouter   = btn("Ajouter le lecteur",   "btn-primary");
        Button btnModifier  = btn("Modifier la sélection","btn-accent");
        Button btnSupprimer = btn("Supprimer le lecteur", "btn-danger");

        // Clic sur une ligne → remplit le formulaire
        tableLecteurs.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                txtNom.setText(selected.getNom() != null ? selected.getNom() : "");
                txtPrenom.setText(selected.getPrenom() != null ? selected.getPrenom() : "");
                txtEmail.setText(selected.getEmail() != null ? selected.getEmail() : "");
            }
        });

        btnAjouter.setOnAction(e -> {
            String nom = txtNom.getText().trim(), email = txtEmail.getText().trim();
            if (nom.isEmpty() || email.isEmpty()) { afficherInfo("Nom et email obligatoires."); return; }
            try {
                lecteurDAO.saveOrUpdate(new Lecteur(nom, txtPrenom.getText().trim(), email));
                actualiserTout();
                txtNom.clear(); txtPrenom.clear(); txtEmail.clear();
                tableLecteurs.getSelectionModel().clearSelection();
                setStatus("Lecteur " + nom + " ajouté.");
            } catch (Exception ex) { ex.printStackTrace(); afficherErreur("Erreur Oracle :\n" + ex.getMessage()); }
        });

        btnModifier.setOnAction(e -> {
            Lecteur l = tableLecteurs.getSelectionModel().getSelectedItem();
            if (l == null) { afficherInfo("Sélectionnez un lecteur à modifier."); return; }
            String nom = txtNom.getText().trim(), email = txtEmail.getText().trim();
            if (nom.isEmpty() || email.isEmpty()) { afficherInfo("Nom et email obligatoires."); return; }
            try {
                l.setNom(nom); l.setPrenom(txtPrenom.getText().trim()); l.setEmail(email);
                lecteurDAO.saveOrUpdate(l);
                actualiserTout();
                txtNom.clear(); txtPrenom.clear(); txtEmail.clear();
                tableLecteurs.getSelectionModel().clearSelection();
                setStatus("Lecteur " + nom + " modifié.");
            } catch (Exception ex) { ex.printStackTrace(); afficherErreur("Erreur Oracle :\n" + ex.getMessage()); }
        });

        btnSupprimer.setOnAction(e -> {
            Lecteur l = tableLecteurs.getSelectionModel().getSelectedItem();
            if (l == null) { afficherInfo("Sélectionnez un lecteur."); return; }
            try {
                lecteurDAO.delete(l); actualiserTout();
                txtNom.clear(); txtPrenom.clear(); txtEmail.clear();
                tableLecteurs.getSelectionModel().clearSelection();
                setStatus("Lecteur supprimé.");
            }
            catch (Exception ex) { ex.printStackTrace(); afficherErreur("Impossible de supprimer :\n" + ex.getMessage()); }
        });

        VBox formBox = formPanel(lblForm,
                "Nom * :", txtNom, "Prénom :", txtPrenom, "Email * :", txtEmail,
                btnAjouter, btnModifier, btnSupprimer);
        BorderPane layout = new BorderPane();
        layout.setTop(toolbar);
        layout.setCenter(tableLecteurs); layout.setRight(formBox);
        layout.setPadding(new Insets(12));
        BorderPane.setMargin(formBox, new Insets(0, 0, 0, 12));
        tab.setContent(layout);
        return tab;
    }

    // ================================================================
    // ONGLET LIVRES (avec colonne Score moyen)
    // ================================================================
    private Tab creerOngletLivres() {
        Tab tab = new Tab("  Livres  ");

        TableColumn<Livre, String>  cTitre  = new TableColumn<>("Titre");
        cTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        TableColumn<Livre, String>  cAuteur = new TableColumn<>("Auteur");
        cAuteur.setCellValueFactory(new PropertyValueFactory<>("auteur"));
        TableColumn<Livre, Integer> cPages  = new TableColumn<>("Nb Pages");
        cPages.setCellValueFactory(new PropertyValueFactory<>("nombrePages"));
        cPages.setMaxWidth(90);
        TableColumn<Livre, String>  cDebut  = new TableColumn<>("Date début");
        cDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        TableColumn<Livre, String>  cFin    = new TableColumn<>("Date fin");
        cFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));

        tableLivres.getColumns().clear();
        tableLivres.getColumns().addAll(cTitre, cAuteur, cPages, cDebut, cFin);
        tableLivres.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableLivres.setPlaceholder(new Label("Aucun livre enregistré."));

        Label lblForm = new Label("Ajouter / Modifier un Livre");
        lblForm.getStyleClass().add("form-title");
        final TextField  txtTitre  = new TextField(); txtTitre.setPromptText("Titre *");
        final TextField  txtAuteur = new TextField(); txtAuteur.setPromptText("Auteur");
        final TextField  txtPages  = new TextField(); txtPages.setPromptText("Nombre de pages");
        final DatePicker dpDebut   = new DatePicker();
        dpDebut.setPromptText("Date de début"); dpDebut.setMaxWidth(Double.MAX_VALUE);
        final DatePicker dpFin     = new DatePicker();
        dpFin.setPromptText("Date de fin"); dpFin.setMaxWidth(Double.MAX_VALUE);

        Button btnAjouter   = btn("Ajouter le livre",    "btn-primary");
        Button btnModifier  = btn("Modifier la sélection","btn-accent");
        Button btnSupprimer = btn("Supprimer le livre",  "btn-danger");

        // Clic sur une ligne → remplit le formulaire
        tableLivres.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                txtTitre.setText(selected.getTitre() != null ? selected.getTitre() : "");
                txtAuteur.setText(selected.getAuteur() != null ? selected.getAuteur() : "");
                txtPages.setText(selected.getNombrePages() != null ? String.valueOf(selected.getNombrePages()) : "");
                dpDebut.setValue(selected.getDateDebut() != null ? selected.getDateDebut().toLocalDate() : null);
                dpFin.setValue(selected.getDateFin() != null ? selected.getDateFin().toLocalDate() : null);
            }
        });

        btnAjouter.setOnAction(e -> {
            String titre = txtTitre.getText().trim();
            if (titre.isEmpty()) { afficherInfo("Le titre est obligatoire."); return; }
            int pages = 0;
            try { pages = Integer.parseInt(txtPages.getText().trim()); } catch (Exception ignored) {}
            Date debut = dpDebut.getValue() != null ? Date.valueOf(dpDebut.getValue()) : null;
            Date fin   = dpFin.getValue()   != null ? Date.valueOf(dpFin.getValue())   : null;
            try {
                livreDAO.saveOrUpdate(new Livre(titre, txtAuteur.getText().trim(), pages, debut, fin));
                actualiserTout();
                txtTitre.clear(); txtAuteur.clear(); txtPages.clear();
                dpDebut.setValue(null); dpFin.setValue(null);
                tableLivres.getSelectionModel().clearSelection();
                setStatus("Livre " + titre + " ajouté.");
            } catch (Exception ex) { ex.printStackTrace(); afficherErreur("Erreur Oracle :\n" + ex.getMessage()); }
        });

        btnModifier.setOnAction(e -> {
            Livre l = tableLivres.getSelectionModel().getSelectedItem();
            if (l == null) { afficherInfo("Sélectionnez un livre à modifier."); return; }
            String titre = txtTitre.getText().trim();
            if (titre.isEmpty()) { afficherInfo("Le titre est obligatoire."); return; }
            int pages = 0;
            try { pages = Integer.parseInt(txtPages.getText().trim()); } catch (Exception ignored) {}
            try {
                l.setTitre(titre);
                l.setAuteur(txtAuteur.getText().trim());
                l.setNombrePages(pages);
                l.setDateDebut(dpDebut.getValue() != null ? Date.valueOf(dpDebut.getValue()) : null);
                l.setDateFin(dpFin.getValue()     != null ? Date.valueOf(dpFin.getValue())   : null);
                livreDAO.saveOrUpdate(l);
                actualiserTout();
                txtTitre.clear(); txtAuteur.clear(); txtPages.clear();
                dpDebut.setValue(null); dpFin.setValue(null);
                tableLivres.getSelectionModel().clearSelection();
                setStatus("Livre " + titre + " modifié.");
            } catch (Exception ex) { ex.printStackTrace(); afficherErreur("Erreur Oracle :\n" + ex.getMessage()); }
        });

        btnSupprimer.setOnAction(e -> {
            Livre l = tableLivres.getSelectionModel().getSelectedItem();
            if (l == null) { afficherInfo("Sélectionnez un livre."); return; }
            try {
                livreDAO.delete(l); actualiserTout();
                txtTitre.clear(); txtAuteur.clear(); txtPages.clear();
                dpDebut.setValue(null); dpFin.setValue(null);
                tableLivres.getSelectionModel().clearSelection();
                setStatus("Livre supprimé.");
            }
            catch (Exception ex) { ex.printStackTrace(); afficherErreur("Impossible de supprimer :\n" + ex.getMessage()); }
        });

        VBox formBox = formPanel(lblForm,
                "Titre * :", txtTitre, "Auteur :", txtAuteur,
                "Nb pages :", txtPages, "Date début :", dpDebut, "Date fin :", dpFin,
                btnAjouter, btnModifier, btnSupprimer);
        BorderPane layout = new BorderPane();
        layout.setCenter(tableLivres); layout.setRight(formBox);
        layout.setPadding(new Insets(12));
        BorderPane.setMargin(formBox, new Insets(0, 0, 0, 12));
        tab.setContent(layout);
        return tab;
    }

    // ================================================================
    // ONGLET EXAMENS (Score déjà présent + colonne Nb Pages du livre)
    // ================================================================
    private Tab creerOngletExamens(final Stage stage) {
        Tab tab = new Tab("  Examens  ");

        TableColumn<ResultatExamen, String> cLec = new TableColumn<>("Lecteur");
        cLec.setCellValueFactory(d -> {
            Lecteur l = d.getValue().getLecteur();
            return new SimpleStringProperty(l != null ? l.getNom() + " " + l.getPrenom() : "-");
        });

        TableColumn<ResultatExamen, String> cLiv = new TableColumn<>("Livre");
        cLiv.setCellValueFactory(d -> {
            Livre l = d.getValue().getLivre();
            return new SimpleStringProperty(l != null ? l.getTitre() : "-");
        });

        // Colonne Nb Pages
        TableColumn<ResultatExamen, String> cPages = new TableColumn<>("Nb Pages");
        cPages.setCellValueFactory(d -> {
            Livre l = d.getValue().getLivre();
            Integer p = (l != null) ? l.getNombrePages() : null;
            return new SimpleStringProperty(p != null && p > 0 ? String.valueOf(p) : "—");
        });
        cPages.setMaxWidth(80);

        // Colonne Score /100 colorisée
        TableColumn<ResultatExamen, Integer> cScore = new TableColumn<>("Score /100");
        cScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        cScore.setMaxWidth(100);
        cScore.setCellFactory(tc -> new TableCell<ResultatExamen, Integer>() {
            @Override
            protected void updateItem(Integer score, boolean empty) {
                super.updateItem(score, empty);
                if (empty || score == null) { setText(null); setStyle(""); return; }
                setText(score + "/100");
                if      (score >= 90) setStyle("-fx-text-fill:#0a6640;-fx-font-weight:700;");
                else if (score >= 75) setStyle("-fx-text-fill:#1B7C7F;-fx-font-weight:700;");
                else if (score >= 50) setStyle("-fx-text-fill:#b45309;-fx-font-weight:600;");
                else                  setStyle("-fx-text-fill:#b91c1c;-fx-font-weight:600;");
            }
        });

        TableColumn<ResultatExamen, String> cMen  = new TableColumn<>("Mention");
        cMen.setCellValueFactory(new PropertyValueFactory<>("mention"));

        TableColumn<ResultatExamen, String> cDate = new TableColumn<>("Date examen");
        cDate.setCellValueFactory(new PropertyValueFactory<>("dateExamen"));

        tableExamens.getColumns().clear();
        tableExamens.getColumns().addAll(cLec, cLiv, cPages, cScore, cMen, cDate);
        tableExamens.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableExamens.setPlaceholder(new Label("Aucun résultat d'examen enregistré."));

        // Filtres
        Label lblFiltreL = new Label("Filtrer par lecteur :");
        lblFiltreL.getStyleClass().add("toolbar-label");
        configurerCbLecteur(cbExamFiltreL);
        cbExamFiltreL.setPromptText("Tous les lecteurs");
        cbExamFiltreL.setPrefWidth(190);

        Label lblFiltreB = new Label("Filtrer par livre :");
        lblFiltreB.getStyleClass().add("toolbar-label");
        configurerCbLivre(cbExamFiltreB);
        cbExamFiltreB.setPromptText("Tous les livres");
        cbExamFiltreB.setPrefWidth(190);

        final Button btnFiltre = btn("Filtrer",       "btn-secondary");
        final Button btnReset  = btn("Tout afficher", "btn-accent");
        btnFiltre.setMaxWidth(Control.USE_PREF_SIZE);
        btnReset.setMaxWidth(Control.USE_PREF_SIZE);

        btnFiltre.setOnAction(e -> {
            Lecteur lec = cbExamFiltreL.getValue();
            Livre   liv = cbExamFiltreB.getValue();
            if (lec != null) {
                tableExamens.getItems().setAll(examenDAO.getByLecteur(lec.getId()));
                setStatus("Résultats filtrés pour " + lec.getNom());
            } else if (liv != null) {
                tableExamens.getItems().setAll(examenDAO.getByLivre(liv.getId()));
                setStatus("Résultats filtrés pour : " + liv.getTitre());
            } else {
                tableExamens.getItems().setAll(examenDAO.getAll());
                setStatus("Tous les résultats affichés.");
            }
        });

        btnReset.setOnAction(e -> {
            cbExamFiltreL.getSelectionModel().clearSelection();
            cbExamFiltreL.getEditor().clear();
            cbExamFiltreB.getSelectionModel().clearSelection();
            cbExamFiltreB.getEditor().clear();
            tableExamens.getItems().setAll(examenDAO.getAll());
            setStatus("Tous les résultats affichés.");
        });

        // Bouton Export Excel (tous les résultats)
        final Button btnExport = new Button("Exporter Excel");
        btnExport.getStyleClass().add("btn-export");
        btnExport.setMaxWidth(Control.USE_PREF_SIZE);
        btnExport.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Enregistrer le fichier Excel");
            fc.setInitialFileName("resultats_examens.xlsx");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
            File file = fc.showSaveDialog(stage);
            if (file != null) {
                btnExport.setDisable(true);
                final File ff = file;
                Task<String> task = new Task<String>() {
                    @Override protected String call() { return excelService.exporterResultats(ff); }
                };
                task.setOnSucceeded(ev -> { setStatus(task.getValue()); afficherInfo(task.getValue()); btnExport.setDisable(false); });
                task.setOnFailed(ev  -> { setStatus("Erreur export."); btnExport.setDisable(false); });
                new Thread(task).start();
            }
        });

        // Bouton Import Excel
        final Button btnImport = new Button("Importer Excel (Notes)");
        btnImport.getStyleClass().add("btn-import");
        btnImport.setMaxWidth(Control.USE_PREF_SIZE);
        btnImport.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choisir un fichier Excel");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx", "*.xls"));
            File file = fc.showOpenDialog(stage);
            if (file != null) {
                setStatus("Import en cours...");
                btnImport.setDisable(true);
                final File ff = file;
                Task<String> task = new Task<String>() {
                    @Override protected String call() {
                        return excelService.importerLecteurs(ff);
                    }
                };
                task.setOnSucceeded(ev -> {
                    actualiserTout();
                    setStatus(task.getValue());
                    afficherInfo(task.getValue());
                    btnImport.setDisable(false);
                });
                task.setOnFailed(ev -> { setStatus("Erreur importation."); btnImport.setDisable(false); });
                new Thread(task).start();
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(10,
                lblFiltreL, cbExamFiltreL, sep(),
                lblFiltreB, cbExamFiltreB,
                btnFiltre, btnReset,
                spacer, btnImport, btnExport);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("toolbar");

        // Formulaire saisie examen
        Label lblForm = new Label("Enregistrer un Examen");
        lblForm.getStyleClass().add("form-title");

        configurerCbLecteur(cbExamLecteur);
        cbExamLecteur.setPromptText("Lecteur *");
        cbExamLecteur.setMaxWidth(Double.MAX_VALUE);
        configurerCbLivre(cbExamLivre);
        cbExamLivre.setPromptText("Livre *");
        cbExamLivre.setMaxWidth(Double.MAX_VALUE);

        final TextField  txtScore    = new TextField(); txtScore.setPromptText("Score obtenu (0-100) *");
        final DatePicker dpDateExam  = new DatePicker(LocalDate.now());
        dpDateExam.setMaxWidth(Double.MAX_VALUE);

        final Label lblMentionPreview = new Label();
        lblMentionPreview.getStyleClass().add("mention-preview");
        txtScore.textProperty().addListener((obs, o, n) -> {
            try {
                int s = Integer.parseInt(n.trim());
                lblMentionPreview.setText("→  " + ResultatExamen.calculerMention(s));
                if      (s >= 90) lblMentionPreview.setStyle("-fx-text-fill:#0a6640;-fx-font-weight:700;");
                else if (s >= 75) lblMentionPreview.setStyle("-fx-text-fill:#1B7C7F;-fx-font-weight:700;");
                else if (s >= 50) lblMentionPreview.setStyle("-fx-text-fill:#b45309;-fx-font-weight:600;");
                else              lblMentionPreview.setStyle("-fx-text-fill:#b91c1c;-fx-font-weight:600;");
            } catch (Exception ignored) { lblMentionPreview.setText(""); }
        });

        Button btnSauver    = btn("Enregistrer le résultat", "btn-primary");
        Button btnModifier  = btn("Modifier la sélection",   "btn-accent");
        Button btnSupprimer = btn("Supprimer la sélection",  "btn-danger");

        // Clic sur une ligne → remplit le formulaire pour modification
        tableExamens.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                cbExamLecteur.setValue(selected.getLecteur());
                cbExamLivre.setValue(selected.getLivre());
                txtScore.setText(selected.getScore() != null ? String.valueOf(selected.getScore()) : "");
                if (selected.getDateExamen() != null)
                    dpDateExam.setValue(selected.getDateExamen().toLocalDate());
            }
        });

        btnSauver.setOnAction(e -> {
            Lecteur lec = cbExamLecteur.getValue();
            Livre   liv = cbExamLivre.getValue();
            String  sc  = txtScore.getText().trim();
            if (lec == null || liv == null || sc.isEmpty()) {
                afficherInfo("Lecteur, livre et score sont obligatoires."); return;
            }
            int score;
            try { score = Integer.parseInt(sc); }
            catch (Exception ex) { afficherInfo("Le score doit être un entier (0-100)."); return; }
            if (score < 0 || score > 100) { afficherInfo("Le score doit être entre 0 et 100."); return; }
            if (examenDAO.existeDeja(lec, liv)) {
                afficherInfo(lec.getNom() + " a déjà passé l'examen de : " + liv.getTitre()
                        + ".\nUtilisez « Modifier » pour changer le résultat existant.");
                return;
            }
            try {
                Date dateExam = dpDateExam.getValue() != null
                        ? Date.valueOf(dpDateExam.getValue())
                        : new Date(System.currentTimeMillis());
                ResultatExamen re = new ResultatExamen(lec, liv, score, dateExam);
                examenDAO.saveOrUpdate(re);
                tableExamens.getItems().setAll(examenDAO.getAll());
                cbExamLecteur.getSelectionModel().clearSelection();
                cbExamLecteur.getEditor().clear();
                cbExamLivre.getSelectionModel().clearSelection();
                cbExamLivre.getEditor().clear();
                txtScore.clear(); lblMentionPreview.setText("");
                tableExamens.getSelectionModel().clearSelection();
                setStatus("Résultat : " + lec.getNom() + " → " + score + "/100 (" + ResultatExamen.calculerMention(score) + ")");
            } catch (Exception ex) { ex.printStackTrace(); afficherErreur("Erreur Oracle :\n" + ex.getMessage()); }
        });

        btnModifier.setOnAction(e -> {
            ResultatExamen re = tableExamens.getSelectionModel().getSelectedItem();
            if (re == null) { afficherInfo("Sélectionnez d'abord un résultat dans le tableau."); return; }
            Lecteur lec = cbExamLecteur.getValue();
            Livre   liv = cbExamLivre.getValue();
            String  sc  = txtScore.getText().trim();
            if (lec == null || liv == null || sc.isEmpty()) {
                afficherInfo("Lecteur, livre et score sont obligatoires."); return;
            }
            int score;
            try { score = Integer.parseInt(sc); }
            catch (Exception ex) { afficherInfo("Le score doit être un entier (0-100)."); return; }
            if (score < 0 || score > 100) { afficherInfo("Le score doit être entre 0 et 100."); return; }
            try {
                Date dateExam = dpDateExam.getValue() != null
                        ? Date.valueOf(dpDateExam.getValue())
                        : new Date(System.currentTimeMillis());
                re.setLecteur(lec);
                re.setLivre(liv);
                re.setScore(score);
                re.setDateExamen(dateExam);
                examenDAO.saveOrUpdate(re);
                tableExamens.getItems().setAll(examenDAO.getAll());
                cbExamLecteur.getSelectionModel().clearSelection();
                cbExamLecteur.getEditor().clear();
                cbExamLivre.getSelectionModel().clearSelection();
                cbExamLivre.getEditor().clear();
                txtScore.clear(); lblMentionPreview.setText("");
                tableExamens.getSelectionModel().clearSelection();
                setStatus("Résultat modifié : " + lec.getNom() + " → " + score + "/100 (" + re.getMention() + ")");
            } catch (Exception ex) { ex.printStackTrace(); afficherErreur("Erreur Oracle :\n" + ex.getMessage()); }
        });

        btnSupprimer.setOnAction(e -> {
            ResultatExamen re = tableExamens.getSelectionModel().getSelectedItem();
            if (re == null) { afficherInfo("Sélectionnez un résultat à supprimer."); return; }
            try {
                examenDAO.delete(re);
                tableExamens.getItems().setAll(examenDAO.getAll());
                cbExamLecteur.getSelectionModel().clearSelection();
                cbExamLecteur.getEditor().clear();
                cbExamLivre.getSelectionModel().clearSelection();
                cbExamLivre.getEditor().clear();
                txtScore.clear(); lblMentionPreview.setText("");
                tableExamens.getSelectionModel().clearSelection();
                setStatus("Résultat supprimé.");
            }
            catch (Exception ex) { ex.printStackTrace(); afficherErreur("Impossible de supprimer :\n" + ex.getMessage()); }
        });

        VBox formBox = formPanel(lblForm,
                "Lecteur * :",     cbExamLecteur,
                "Livre * :",       cbExamLivre,
                "Score /100 * :",  txtScore,
                lblMentionPreview,
                "Date d'examen :", dpDateExam,
                btnSauver, btnModifier, btnSupprimer);

        BorderPane layout = new BorderPane();
        layout.setTop(toolbar);
        layout.setCenter(tableExamens);
        layout.setRight(formBox);
        BorderPane.setMargin(formBox, new Insets(0, 0, 0, 12));
        tab.setContent(layout);
        return tab;
    }



    // ================================================================
    // RAFRAICHISSEMENT GLOBAL
    // ================================================================
    private void actualiserTout() {
        try {
            tableDashboard.getItems().setAll(suiviDAO.getAll());
            tableLecteurs.getItems().setAll(lecteurDAO.getAll());
            tableLivres.getItems().setAll(livreDAO.getAll());
            tableExamens.getItems().setAll(examenDAO.getAll());

            Livre   ls  = cbStatsLivres.getValue();
            Lecteur lf  = cbFormLecteur.getValue();
            Livre   bf  = cbFormLivre.getValue();
            Lecteur le  = cbExamLecteur.getValue();
            Livre   be  = cbExamLivre.getValue();
            Lecteur lfl = cbExamFiltreL.getValue();
            Livre   bfl = cbExamFiltreB.getValue();

            List<Lecteur> tousLecteurs = lecteurDAO.getAll();
            List<Livre>   tousLivres   = livreDAO.getAll();

            cbStatsLivres.getItems().setAll(tousLivres);
            cbFormLecteur.getItems().setAll(tousLecteurs);
            cbFormLivre.getItems().setAll(tousLivres);
            cbExamLecteur.getItems().setAll(tousLecteurs);
            cbExamLivre.getItems().setAll(tousLivres);
            cbExamFiltreL.getItems().setAll(tousLecteurs);
            cbExamFiltreB.getItems().setAll(tousLivres);

            if (ls  != null) cbStatsLivres.setValue(ls);
            if (lf  != null) cbFormLecteur.setValue(lf);
            if (bf  != null) cbFormLivre.setValue(bf);
            if (le  != null) cbExamLecteur.setValue(le);
            if (be  != null) cbExamLivre.setValue(be);
            if (lfl != null) cbExamFiltreL.setValue(lfl);
            if (bfl != null) cbExamFiltreB.setValue(bfl);

        } catch (Exception e) {
            e.printStackTrace();
            setStatus("Erreur chargement : " + e.getMessage());
        }
    }

    // ================================================================
    // UTILITAIRES
    // ================================================================
    private VBox formPanel(Label titre, Object... elements) {
        VBox box = new VBox(12);
        box.getStyleClass().add("form-pane");
        box.setPrefWidth(320);
        box.getChildren().add(titre);
        box.getChildren().add(new Separator());
        for (Object el : elements) {
            if (el instanceof String) {
                Label l = new Label((String) el);
                l.getStyleClass().add("field-label");
                box.getChildren().add(l);
            } else if (el instanceof javafx.scene.Node) {
                box.getChildren().add((javafx.scene.Node) el);
            }
        }
        return box;
    }

    private Button btn(String text, String styleClass) {
        Button b = new Button(text);
        b.getStyleClass().add(styleClass);
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    private Separator sep() { return new Separator(javafx.geometry.Orientation.VERTICAL); }

    // ================================================================
    // COMBOBOX LECTEUR — avec recherche en temps réel
    // ================================================================
    private void configurerCbLecteur(ComboBox<Lecteur> cb) {
        cb.setEditable(true);
        cb.setConverter(new StringConverter<Lecteur>() {
            @Override public String toString(Lecteur l)   { return l == null ? "" : l.getNom() + " " + l.getPrenom(); }
            @Override public Lecteur fromString(String s) { return null; }
        });

        cb.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            // Ne pas filtrer si le changement vient d'une sélection déjà effectuée
            Lecteur selected = cb.getValue();
            String selectedStr = selected == null ? "" : selected.getNom() + " " + selected.getPrenom();
            if (newVal != null && newVal.equals(selectedStr)) return;

            String filtre = newVal == null ? "" : newVal.trim().toLowerCase();
            List<Lecteur> tous = lecteurDAO.getAll();
            List<Lecteur> filtres = filtre.isEmpty() ? tous
                : tous.stream()
                    .filter(l -> (l.getNom() + " " + l.getPrenom()).toLowerCase().contains(filtre)
                              || (l.getEmail() != null && l.getEmail().toLowerCase().contains(filtre)))
                    .collect(Collectors.toList());

            cb.getItems().setAll(filtres);
            if (!filtres.isEmpty() && !filtre.isEmpty()) cb.show();
        });
    }

    // ================================================================
    // COMBOBOX LIVRE — avec recherche en temps réel
    // ================================================================
    private void configurerCbLivre(ComboBox<Livre> cb) {
        cb.setEditable(true);
        cb.setConverter(new StringConverter<Livre>() {
            @Override public String toString(Livre l)   { return l == null ? "" : l.getTitre(); }
            @Override public Livre fromString(String s) { return null; }
        });

        cb.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            // Ne pas filtrer si le changement vient d'une sélection déjà effectuée
            Livre selected = cb.getValue();
            String selectedStr = selected == null ? "" : selected.getTitre();
            if (newVal != null && newVal.equals(selectedStr)) return;

            String filtre = newVal == null ? "" : newVal.trim().toLowerCase();
            List<Livre> tous = livreDAO.getAll();
            List<Livre> filtres = filtre.isEmpty() ? tous
                : tous.stream()
                    .filter(l -> l.getTitre().toLowerCase().contains(filtre)
                              || (l.getAuteur() != null && l.getAuteur().toLowerCase().contains(filtre)))
                    .collect(Collectors.toList());

            cb.getItems().setAll(filtres);
            if (!filtres.isEmpty() && !filtre.isEmpty()) cb.show();
        });
    }

    private void setStatus(final String msg) {
        Platform.runLater(() -> lblStatus.setText(msg));
    }

    private void afficherInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Information"); a.setHeaderText(null); a.setContentText(msg);
        cssAlert(a); a.show();
    }

    private void afficherErreur(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText("Une erreur s'est produite"); a.setContentText(msg);
        cssAlert(a); a.show();
    }

    private void cssAlert(Alert a) { try { css(a.getDialogPane().getScene()); } catch (Exception ignored) {} }

    private void css(Scene s) {
        try { s.getStylesheets().add(getClass().getResource("zaz.css").toExternalForm()); }
        catch (Exception ignored) {}
    }

    public static void main(String[] args) { launch(args); }
}