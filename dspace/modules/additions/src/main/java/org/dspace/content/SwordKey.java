package org.dspace.content;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.dspace.content.factory.DatashareContentServiceFactory;
import org.dspace.content.service.SwordKeyService;
import org.dspace.core.Constants;
import org.dspace.eperson.EPerson;

@Entity
@Table(name="sword_keys")
public class SwordKey extends DSpaceObject {
	
	@Id
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "eperson_id")
	@Column(name="eperson_id", insertable = true, updatable = false)
    private EPerson ePerson;

	@Column(name= "key")
    private String key;

	@Transient
    private transient SwordKeyService swordKeyService;

	public EPerson getEPerson() {
		return ePerson;
	}

	public void setEPerson(EPerson ePerson) {
		this.ePerson = ePerson;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return Constants.SWORD_KEY;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public SwordKeyService getSwordKeyService()
    {
		if(swordKeyService == null)
        {
        	swordKeyService = DatashareContentServiceFactory.getInstance().getSwordKeyService();
        }
        return swordKeyService;
    }

}
