-- Αρχικά δεδομένα ρόλων και δικαιωμάτων.
--
-- Χωρίς αυτές τις εγγραφές κανένας χρήστης δεν μπορεί να συνδεθεί: ο πίνακας
-- users έχει role_id NOT NULL και το getAuthorities() διαβάζει τα capabilities
-- του ρόλου.
--
-- Εκτελείται ΜΙΑ ΦΟΡΑ. Στο application-dev.properties:
--   spring.sql.init.mode=always
--   spring.jpa.defer-datasource-initialization=true
-- και μετά την πρώτη επιτυχή εκτέλεση ξανασχολιάζονται, αλλιώς η επόμενη
-- εκκίνηση σκάει με duplicate key. Το defer-datasource-initialization είναι
-- απαραίτητο ώστε το script να τρέξει ΜΕΤΑ τη δημιουργία των πινάκων από
-- το Hibernate.

INSERT INTO capabilities (id, name, description) VALUES
(1,'MANAGE_PARCELS','Δημιουργία και επεξεργασία αγροτεμαχίων και καλλιεργειών'),
(2,'RECORD_ACTIVITIES','Καταχώρηση εργασιών στο ημερολόγιο αγρού'),
(3,'VIEW_REPORTS','Προβολή ημερολογίου και αναφορών'),
(4,'MANAGE_USERS','Διαχείριση λογαριασμών χρηστών');
ALTER TABLE capabilities AUTO_INCREMENT = 5;

INSERT INTO roles (id, name) VALUES
(1,'FARMER'),
(2,'ADMIN');
ALTER TABLE roles AUTO_INCREMENT = 3;

-- FARMER: διαχείριση αγροτεμαχίων, καταχώρηση εργασιών, προβολή αναφορών
-- ADMIN:  διαχείριση χρηστών, προβολή αναφορών
INSERT INTO roles_capabilities (role_id, capability_id) VALUES
(1,1),
(1,2),
(1,3),
(2,3),
(2,4);
