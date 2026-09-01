INSERT INTO capabilities (name, description) VALUES
    ('MANAGE_PARCELS',    'Δημιουργία και επεξεργασία αγροτεμαχίων και καλλιεργειών'),
    ('RECORD_ACTIVITIES', 'Καταχώρηση εργασιών στο ημερολόγιο αγρού'),
    ('VIEW_REPORTS',      'Προβολή ημερολογίου και αναφορών'),
    ('MANAGE_USERS',      'Διαχείριση λογαριασμών χρηστών')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO roles (name) VALUES
    ('FARMER'),
    ('ADMIN')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
JOIN capabilities c
WHERE (r.name = 'FARMER' AND c.name IN ('MANAGE_PARCELS', 'RECORD_ACTIVITIES', 'VIEW_REPORTS'))
   OR (r.name = 'ADMIN'  AND c.name IN ('MANAGE_USERS', 'VIEW_REPORTS'))
ON DUPLICATE KEY UPDATE capability_id = VALUES(capability_id);
