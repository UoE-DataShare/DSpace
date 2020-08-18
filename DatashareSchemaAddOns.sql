CREATE TABLE uun2email (
	uun varchar(32) NOT NULL UNIQUE,
	email varchar(64) NULL,
    uuid UUID DEFAULT gen_random_uuid() UNIQUE,
	CONSTRAINT uun2email_pkey PRIMARY KEY (uuid)
);
CREATE INDEX uun2email_email_index ON uun2email USING btree (email);

CREATE TABLE sword_keys (
	eperson_id uuid NOT NULL,
	"key" varchar(36) NULL,
    uuid UUID DEFAULT gen_random_uuid() UNIQUE,
	CONSTRAINT sword_keys_pkey PRIMARY KEY (uuid)
);
CREATE INDEX sword_keys_key_index ON sword_keys USING btree (key);

ALTER TABLE sword_keys ADD CONSTRAINT sword_keys_eperson_id_fkey FOREIGN KEY (eperson_id) REFERENCES eperson(uuid);


CREATE TABLE dataset (
	id integer NULL,
	item_id uuid  NOT NULL UNIQUE,
	file_name varchar(256) NULL,
	checksum varchar(64) NULL,
	checksum_algorithm varchar(32) NULL,
	uuid UUID DEFAULT gen_random_uuid() UNIQUE,
	CONSTRAINT dataset_pkey PRIMARY KEY (uuid)
);
CREATE INDEX dataset_file_name_index ON dataset USING btree (file_name);

ALTER TABLE dataset ADD CONSTRAINT dataset_item_id_fkey FOREIGN KEY (item_id) REFERENCES item(uuid) ON DELETE CASCADE;


CREATE TABLE batch_import (
	id integer NOT NULL,
	eperson_id uuid NOT NULL,
	map_file text NOT NULL,
	uuid UUID DEFAULT gen_random_uuid() UNIQUE,
	CONSTRAINT batch_import_pkey PRIMARY KEY (uuid)
);

ALTER TABLE batch_import ADD CONSTRAINT batch_import_eperson_id_fkey FOREIGN KEY (eperson_id) REFERENCES eperson(uuid);

