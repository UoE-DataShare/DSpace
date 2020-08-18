
------------------------------------------------------
-- Adding Datashare changes to DSpace 6.x in a single commit. 
------------------------------------------------------

-- Add uuid to Datashare specific tables
ALTER TABLE  uun2email ADD COLUMN uuid UUID DEFAULT gen_random_uuid() UNIQUE;
ALTER TABLE  sword_keys ADD COLUMN uuid UUID DEFAULT gen_random_uuid() UNIQUE;
ALTER TABLE  dataset ADD COLUMN uuid UUID DEFAULT gen_random_uuid() UNIQUE;
ALTER TABLE  batch_import ADD COLUMN uuid UUID DEFAULT gen_random_uuid() UNIQUE;
----------------------------------------------------------------------
-- Add constraints to the tables
----------------------------------------------------------------------

-- uun2email
ALTER TABLE uun2email  ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE uun2email  DROP CONSTRAINT uun2email_pkey;
ALTER TABLE uun2email ADD PRIMARY KEY (uuid);


-- sword_keys
-- ALTER TABLE sword_keys DROP CONSTRAINT sword_keys_eperson_id_fkey;                                 
ALTER TABLE sword_keys RENAME COLUMN eperson_id to eperson_legacy_id;
ALTER TABLE sword_keys ADD COLUMN eperson_id UUID REFERENCES eperson(uuid);
UPDATE sword_keys SET eperson_id = eperson.uuid FROM eperson WHERE sword_keys.eperson_legacy_id = eperson.eperson_id;
ALTER TABLE sword_keys ALTER COLUMN eperson_id SET NOT NULL;
ALTER TABLE sword_keys ALTER COLUMN eperson_legacy_id DROP NOT NULL;
ALTER TABLE sword_keys  ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE sword_keys ADD PRIMARY KEY (uuid);

-- dataset
-- ALTER TABLE dataset DROP CONSTRAINT dataset_item_id_fkey;
ALTER TABLE dataset  DROP CONSTRAINT dataset_pkey;
ALTER TABLE dataset RENAME COLUMN item_id to item_legacy_id;
ALTER TABLE dataset ADD COLUMN item_id UUID REFERENCES Item(uuid);
UPDATE dataset SET item_id = Item.uuid FROM Item WHERE dataset.item_legacy_id = Item.item_id;
ALTER TABLE dataset ALTER COLUMN item_id SET NOT NULL;
ALTER TABLE dataset ALTER COLUMN item_legacy_id DROP NOT NULL;
ALTER TABLE dataset ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE dataset ADD PRIMARY KEY (uuid);

-- batch_import
-- ALTER TABLE batch_import DROP CONSTRAINT batch_import_eperson_id_fkey;
ALTER TABLE batch_import DROP CONSTRAINT batch_import_pkey;
ALTER TABLE batch_import RENAME COLUMN eperson_id to eperson_legacy_id;
ALTER TABLE batch_import ADD COLUMN eperson_id UUID REFERENCES eperson(uuid);
UPDATE batch_import SET eperson_id = eperson.uuid FROM eperson WHERE batch_import.eperson_legacy_id = eperson.eperson_id;
ALTER TABLE batch_import ALTER COLUMN eperson_id SET NOT NULL;
ALTER TABLE batch_import ALTER COLUMN eperson_legacy_id DROP NOT NULL;
ALTER TABLE batch_import  ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE batch_import ADD PRIMARY KEY (uuid);