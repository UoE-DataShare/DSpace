/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.handle;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.dspace.core.ReloadableEntity;

/**
 * Database entity representation of the sharegeo_handle table
 *
 */
@Entity
@Table(name = "sharegeo_handle")
public class ShareGeoHandle implements ReloadableEntity<Integer> {

	@Column(name = "id")
	private Integer id;

	@Column(name = "handle_id")
	private Integer handleId;

	@Column(name = "original", unique = true)
	private String originalHandle;

	protected ShareGeoHandle() {

	}

	@Override
	public Integer getID() {
		return id;
	}

	public void setID(Integer id) {
		this.id = id;
	}

	public Integer getHandleId() {
		return handleId;
	}

	public void setHandleId(Integer handleId) {
		this.handleId = handleId;
	}

	public String getOriginalHandle() {
		return originalHandle;
	}

	public void setOriginalHandle(String originalHandle) {
		this.originalHandle = originalHandle;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		ShareGeoHandle handle1 = (ShareGeoHandle) o;

		return new EqualsBuilder().append(id, handle1.id).append(handleId, handle1.handleId)
				.append(originalHandle, handle1.originalHandle).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(id).append(handleId).append(originalHandle).toHashCode();
	}

}
