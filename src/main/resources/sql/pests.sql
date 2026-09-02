-- Παραμετρικός κατάλογος εχθρών, ασθενειών και ζιζανίων των σιτηρών.
INSERT INTO pests (id, name, latin_name, type) VALUES
(1,'Κίτρινη σκωρίαση','Puccinia striiformis','FUNGAL_DISEASE'),
(2,'Καστανή σκωρίαση','Puccinia triticina','FUNGAL_DISEASE'),
(3,'Ωίδιο','Blumeria graminis','FUNGAL_DISEASE'),
(4,'Σεπτορίαση','Zymoseptoria tritici','FUNGAL_DISEASE'),
(5,'Ελμινθοσπορίωση κριθαριού','Pyrenophora teres','FUNGAL_DISEASE'),
(6,'Φουζαρίωση στάχυος','Fusarium graminearum','FUNGAL_DISEASE'),
(7,'Αφίδες σιτηρών','Sitobion avenae','INSECT'),
(8,'Ζαβρός','Zabrus tenebrioides','INSECT'),
(9,'Πυραλίδα αραβοσίτου','Ostrinia nubilalis','INSECT'),
(10,'Σεζάμια','Sesamia nonagrioides','INSECT'),
(11,'Αγριοβρώμη','Avena sterilis','WEED'),
(12,'Ήρα','Lolium rigidum','WEED'),
(13,'Αγριοσινάπι','Sinapis arvensis','WEED');
ALTER TABLE pests AUTO_INCREMENT = 14;
