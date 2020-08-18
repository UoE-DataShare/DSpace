INSERT INTO metadataschemaregistry ("namespace",short_id) VALUES ('http://datashare.is.ed.ac.uk','ds');

INSERT INTO metadatafieldregistry (metadata_schema_id,"element",qualifier,scope_note) 
Select metadata_schema_id, 'not-emailable','bitstream','If this metadata field is set to ''true'', the item bitstream must not be emailed. ' FROM metadataschemaregistry where short_id = 'ds';
INSERT INTO metadatafieldregistry (metadata_schema_id,"element",qualifier,scope_note)
Select metadata_schema_id,'withdrawn','showtombstone','If this metadata field is set to ''true'', the item bitstream will not be available.' FROM metadataschemaregistry where short_id = 'ds';
INSERT INTO metadatafieldregistry (metadata_schema_id,"element",qualifier,scope_note) 
Select metadata_schema_id, 'not-emailable','item','If this metadata field is set to ''true'', the item bitstreams must not be emailed. ' FROM metadataschemaregistry where short_id = 'ds';
INSERT INTO metadatafieldregistry (metadata_schema_id,"element",qualifier,scope_note) 
Select metadata_schema_id, 'date','embargo','Embargo date.' FROM metadataschemaregistry where short_id = 'dc';


                                                                                                                                                                  