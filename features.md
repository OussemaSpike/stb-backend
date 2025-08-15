# FEATURES.md - Application de Transfert de Gros Montants STB

Voici la liste des fonctionnalités prévues pour l'application web de transfert de gros montants pour la STB. L'approche
initiale pour la création des comptes est un processus manuel géré par un administrateur pour garantir une sécurité et
un contrôle maximum.

---

### **Module 1 : Gestion des Utilisateurs et Authentification**

Ce module constitue la fondation sécurisée de l'application. L'accès est strictement contrôlé par la banque.

#### **Création des Comptes Clients (par l'Administrateur)**

Le processus d'inscription est entièrement géré par le personnel de la STB via le Back-Office pour garantir l'identité
de chaque utilisateur.

* **Processus de Création :**
    * L'administrateur STB est le seul habilité à créer les comptes clients.
    * Il remplit un formulaire avec les informations vérifiées du client : `Nom`, `Prénom`, `CIN`, `email`, `téléphone`.
    * Il associe le profil utilisateur à une compte bancaires (RIB/IBAN) du client.
    * Le système génère envoi une email pour le client pour créer un mot de passe

#### **Gestion de Profil (Client)**

Une fois son compte activé, le client peut gérer ses informations non critiques.

* Consultation de ses informations personnelles.
* Modification de son adresse, numéro de téléphone (peut nécessiter une validation supplémentaire).
* Gestion de son mot de passe (modification, politique de mot de passe robuste).
* Processus de réinitialisation de mot de passe sécurisé (via email).

#### **Gestion des Rôles et Permissions**

* **`ROLE_CLIENT`** : Accès standard aux fonctionnalités de transfert pour un individu.
* **`ROLE_ADMIN_STB`** : Accès au back-office pour la gestion des clients et la surveillance des transactions.

---

### **Module 2 : Le Cœur du Métier - Le Transfert de Fonds**

#### **Initiation d'un Transfert**

* Formulaire de transfert clair : Compte à débiter, Bénéficiaire, Montant, Devise (TND), Motif du virement (
  obligatoire).
* Validation en temps réel des champs (format RIB/IBAN, montant positif, solde suffisant).

#### **Gestion des Bénéficiaires**

* Ajouter, consulter, modifier, et supprimer un bénéficiaire.
* **L'ajout d'un nouveau bénéficiaire est une opération sensible et doit être validée par 2FA.**
* Stockage sécurisé des informations des bénéficiaires (Nom, RIB/IBAN).

#### **Plafonds et Limites de Transfert**

* Le système gère des plafonds de virement (par transaction, par jour, par mois).
* Ces plafonds sont configurables dans le back-office et peuvent différer selon le type de client (Particulier,
  Entreprise, VIP).

#### **Confirmation et Validation du Transfert**

* Affichage d'un écran de résumé complet avant la confirmation finale.
* **Validation finale du transfert par 2FA :** L'utilisateur doit saisir un code unique pour autoriser l'ordre de
  virement.

#### **Historique et Suivi des Transactions**

* Tableau de bord listant tous les transferts avec leur statut : `En attente de validation`, `Exécuté`, `Rejeté`,
  `Échoué`.
* Fonctionnalités de filtre et de recherche par date, bénéficiaire ou montant.
* Génération d'un reçu/avis d'opération au format PDF pour chaque transfert réussi.

---

### **Module 3 : Sécurité et Conformité (Back-Office)**

#### **Journal d'Audit (Audit Trail)**

* Enregistrement immuable de toutes les actions sensibles :
    * Création de client, connexions (réussies/échouées), ajout de bénéficiaire, ordres de virement, approbations/rejets
      par l'admin.
    * Chaque log contient : Qui, Quoi, Quand, Adresse IP.

---

### **Module 4 : Back-Office d'Administration (Interface pour la STB)**

#### **Tableau de Bord Général**

* Vue d'ensemble des indicateurs clés : nombre de transactions du jour, montants totaux transférés, transactions en
  attente de validation.

#### **Gestion des Utilisateurs**

* **Créer un nouveau compte client** : Formulaire pour saisir les informations du client et l'associer à ses comptes
  bancaires.
* Consulter la liste et les profils des clients.
* **Bloquer / Débloquer** un compte en cas d'activité suspecte.
* Réinitialiser le mot de passe d'un utilisateur (générer un nouveau mot de passe temporaire).

#### **Gestion des Transactions "Flagguées" (AML)**

* Interface dédiée pour les agents de conformité.
* Vue détaillée des transactions en attente de validation.
* Possibilité d'**Approuver** (la transaction est alors envoyée pour exécution) ou de **Rejeter** (la transaction est
  annulée) avec un champ de commentaire obligatoire.
* L'utilisateur final est notifié par email/SMS de la décision.