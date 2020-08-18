package org.dspace.content;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.dspace.content.factory.DatashareContentServiceFactory;
import org.dspace.content.service.DatasetService;
import org.dspace.core.Constants;


/**
 * DataShare item dataset. That is a zip file that contains all item bitstreams.
 */
@Entity
@Table(name="dataset")
public class Dataset extends DSpaceObject implements DSpaceObjectLegacySupport {
	
	
	@Column(name="id", insertable = false, updatable = false)
    private Integer legacyId;
	
    @OneToOne(optional = false)
    @JoinColumn(name = "item_id")
    private Item item = null;
	
    @Column(name= "file_name")
    private String fileName = null;
    
    @Column(name= "checksum")
    private String checksum;
    
    @Column(name= "checksum_algorithm")
    private String checkSumAlgorithm;
    
    @Transient
    private transient DatasetService datasetService;

   
    protected Dataset()
    {

    }

	public Item getItem() {
		return item;
	}




	public void setItem(Item item) {
		this.item = item;
	}




	public String getFileName() {
		return fileName;
	}




	public void setFileName(String fileName) {
		this.fileName = fileName;
	}




	public String getChecksum() {
		return checksum;
	}




	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}




	public String getCheckSumAlgorithm() {
		return checkSumAlgorithm;
	}




	public void setCheckSumAlgorithm(String checkSumAlgorithm) {
		this.checkSumAlgorithm = checkSumAlgorithm;
	}




	public void setLegacyId(Integer legacyId) {
		this.legacyId = legacyId;
	}




	@Override
	public Integer getLegacyId() {
		return this.legacyId;
	}

	@Override
	public int getType() {
		return Constants.DATASET;
	}

	@Override
	public String getName() {
		return fileName;
	}
	
	public DatasetService getDatasetService()
    {
        if(datasetService == null)
        {
        	datasetService = DatashareContentServiceFactory.getInstance().getDatasetService();
        }
        return datasetService;
    }

}
