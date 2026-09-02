-- Παραμετρικός κατάλογος σιτηρών (χειμερινά και εαρινά).
INSERT INTO crop_types (id, name, latin_name, season) VALUES
(1,'Σκληρό σιτάρι','Triticum durum','WINTER'),
(2,'Μαλακό σιτάρι','Triticum aestivum','WINTER'),
(3,'Κριθάρι','Hordeum vulgare','WINTER'),
(4,'Βρώμη','Avena sativa','WINTER'),
(5,'Σίκαλη','Secale cereale','WINTER'),
(6,'Τριτικάλε','x Triticosecale','WINTER'),
(7,'Αραβόσιτος','Zea mays','SPRING'),
(8,'Σόργο','Sorghum bicolor','SPRING'),
(9,'Ρύζι','Oryza sativa','SPRING');
ALTER TABLE crop_types AUTO_INCREMENT = 10;
