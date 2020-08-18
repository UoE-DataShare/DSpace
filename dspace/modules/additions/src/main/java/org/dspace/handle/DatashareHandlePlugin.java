package org.dspace.handle;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.handle.factory.ShareGeoHandleServiceFactory;
import org.dspace.handle.service.ShareGeoHandleService;
import org.dspace.identifier.HandleIdentifierProvider;

import net.handle.hdllib.HandleException;
import net.handle.hdllib.Util;
import net.handle.util.StreamTable;

public class DatashareHandlePlugin extends HandlePlugin {
	private static Logger log = Logger.getLogger(DatashareHandlePlugin.class);

	protected ShareGeoHandleService shareGeoHandleService;

	/**
	 * HandleStorage interface init method.
	 * <p>
	 * For DSpace, we have to startup the DSpace Kernel when ShareGeoHandlePlugin
	 * initializes, as the HandlePlugin relies on HandleService (and other services)
	 * which are loaded by the Kernel.
	 * 
	 * @param st StreamTable
	 * @throws Exception if DSpace Kernel fails to startup
	 */
	@Override
	public void init(StreamTable st) throws Exception {
		super.init(st);
		this.shareGeoHandleService = ShareGeoHandleServiceFactory.getInstance().getShareGeoHandleService();
	}

	/**
	 * Return the raw values for this handle. This implementation returns a single
	 * URL value.
	 * 
	 * @param theHandle byte array representation of handle
	 * @param indexList ignored
	 * @param typeList  ignored
	 * @return A byte array with the raw data for this handle. Currently, this
	 *         consists of a single URL value.
	 * @exception HandleException If an error occurs while calling the Handle API.
	 */
	@Override
	public byte[][] getRawHandleValues(byte[] theHandle, int[] indexList, byte[][] typeList) throws HandleException {
		if (log.isInfoEnabled()) {
			log.info("Called getRawHandleValues");
		}

		Context context = null;

		try {
			if (theHandle != null) {
				String handle = Util.decodeString(theHandle);
				String parts[] = handle.split("/");
				String prefix = null;

				if (parts.length == 2) {
					prefix = parts[0];
				}
				if (prefix != null && prefix.equals("10672")) {
					// this is a sharegeo handle, convert it to a datashare handle
					context = new Context();
					ShareGeoHandle shareGeoHandleObject = shareGeoHandleService.findByShareGeoHandle(context, handle);

					if (shareGeoHandleObject != null) {
						theHandle = (HandleIdentifierProvider.getPrefix() + "/" + shareGeoHandleObject.getHandleId())
								.getBytes();
						log.info("sharegeo handle " + handle + " mapped to " + new String(theHandle));
					} else {
						log.warn("Can't find sharegeo handle: " + handle);
					}
				}

			}
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Exception in getRawHandleValues", e);
			}

			// Stack loss as exception does not support cause
			throw new HandleException(HandleException.INTERNAL_ERROR);
		} finally {
			if (context != null) {
				try {
					context.complete();
				} catch (SQLException sqle) {
				}
			}
		}

		return super.getRawHandleValues(theHandle, indexList, typeList);
	}
}
