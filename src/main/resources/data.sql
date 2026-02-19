-- Normalize old enum-style contract_type values to readable names
UPDATE contracts SET contract_type = 'Service' WHERE contract_type = 'SERVICE';
UPDATE contracts SET contract_type = 'Employment' WHERE contract_type = 'EMPLOYMENT';
UPDATE contracts SET contract_type = 'NDA' WHERE contract_type = 'NDA';
UPDATE contracts SET contract_type = 'Lease' WHERE contract_type = 'LEASE';
UPDATE contracts SET contract_type = 'Sales' WHERE contract_type = 'SALES';
UPDATE contracts SET contract_type = 'Partnership' WHERE contract_type = 'PARTNERSHIP';
