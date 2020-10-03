package org.dspace.content;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.dspace.content.factory.DatashareContentServiceFactory;
import org.dspace.content.service.UUN2EmailService;
import org.dspace.core.Constants;

@Entity
@Table(name = "uun2email")
public class UUN2Email extends DSpaceObject {
	

	@Column(name= "uun", unique = true)
    private String uun;
	
	@Column(name= "email")
    private String email;
	
	@Transient
	private transient UUN2EmailService uun2EmailService;

	protected UUN2Email() {

	}
	
	public String getUUN() {
		return uun;
	}

	public void setUUN(String uun) {
		this.uun = uun;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public int getType() {
		return Constants.UUN_2_EMAIL;
	}

	@Override
	public String getName() {
		return uun + ":" + email;
	}
	
	public UUN2EmailService getUUN2EmailService()
    {
        if(uun2EmailService == null)
        {
        	uun2EmailService = DatashareContentServiceFactory.getInstance().getUUN2EmailService();
        }
        return uun2EmailService;
    }


}
