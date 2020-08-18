package org.dspace.content;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.dspace.content.factory.DatashareContentServiceFactory;
import org.dspace.content.service.BatchImportService;
import org.dspace.core.Constants;
import org.dspace.eperson.EPerson;

@Entity
@Table(name="batch_import")
public class BatchImport extends DSpaceObject implements DSpaceObjectLegacySupport {
 
	@Column(name="id", insertable = false, updatable = false)
    private Integer legacyId;
	
	@OneToOne(optional = false)
    @JoinColumn(name = "eperson_id")
    private EPerson ePerson = null;
	
    @Column(name= "map_file")
    private String mapFile = null;
    
    @Transient
    private transient BatchImportService batchImportService;

   
    protected BatchImport()
    {

    }
    
    
	@Override
	public Integer getLegacyId() {
		return this.legacyId;
	}

	@Override
	public int getType() {
		return Constants.BATCH_IMPORT;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public EPerson getEPerson() {
		return ePerson;
	}

	public void setEPerson(EPerson ePerson) {
		this.ePerson = ePerson;
	}

	public String getMapFile() {
		return mapFile;
	}

	public void setMapFile(String mapFile) {
		this.mapFile = mapFile;
	}

	public void setLegacyId(Integer legacyId) {
		this.legacyId = legacyId;
	}
	
	public BatchImportService getBatchImportService()
    {
        if(batchImportService == null)
        {
        	batchImportService = DatashareContentServiceFactory.getInstance().getBatchImportService();
        }
        return batchImportService;
    }

}
