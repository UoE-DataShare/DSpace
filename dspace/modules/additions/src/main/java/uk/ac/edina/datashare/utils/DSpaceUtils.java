package uk.ac.edina.datashare.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dspace.app.util.SubmissionInfo;
import org.dspace.app.util.Util;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.DCDate;
import org.dspace.content.Item;
import org.dspace.content.MetadataSchema;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.GroupService;
import org.dspace.license.factory.LicenseServiceFactory;
import org.dspace.license.service.CreativeCommonsService;
import org.dspace.services.factory.DSpaceServicesFactory;

/**
 * Some DSpace utility methods.
 */
public class DSpaceUtils {
	// private static IFactory factory = null;
	private static final String ENCODING_SCHEME = "W3C-DTF";
	private static final Logger LOG = Logger.getLogger(DSpaceUtils.class);

	public static final String DOI_URL = "https://doi.org";

	/**
	 * Helper method for appending publisher to a buffer string.
	 * 
	 * @param buffer The string buffer to append publisher to.
	 * @param item   DSpace item.
	 */
	private static void appendPublisher(StringBuffer buffer, Item item) {
		String publisher = MetaDataUtil.getPublisher(item);
		if (publisher != null) {
			buffer.append(" ");
			buffer.append(publisher);
			buffer.append(".");
		}
	}

	/**
	 * Clear metadata value for depositor field.
	 * 
	 * @param item DSpace item.
	 * @throws SQLException
	 */
	public static void clearDepositor(Context context, Item item) throws SQLException {
		MetaDataUtil.clearContributor(context, item);
	}

	/**
	 * Clear hijacked metadata value for spatial coverage. This uses DC Subject DCC.
	 * 
	 * @param info Ingest persistence object.
	 * @throws SQLException
	 */
	public static void clearHijackedSpatial(Context context, Item item) throws SQLException {
		MetaDataUtil.clearSubjectDcc(context, item);
	}

	/**
	 * Clear hijacked metadata value for Is published Before. This uses Relation Is
	 * Format Of.
	 * 
	 * @param info Ingest persistence object.
	 * @throws SQLException
	 */
	public static void clearIsPublishedBefore(Context context, SubmissionInfo info) throws SQLException {
		MetaDataUtil.clearIsFormatOf(context, info);
	}

	/**
	 * Clear hijacked metadata value for embargo. This used Date Copyright.
	 * 
	 * @param item The item to clear value in.
	 * @throws SQLException
	 */
	public static void clearUseEmbargo(Context context, Item item) throws SQLException {
		MetaDataUtil.clearDateCopyright(context, item);
	}

	/**
	 * Clear license type metadata values.
	 * 
	 * @param item The item to clear value in.
	 * @throws SQLException
	 */
	public static void clearUserLicenseType(Context context, Item item) throws SQLException {
		MetaDataUtil.clearRightsUri(context, item);
	}

	/**
	 * Finalise a datashare deposit.
	 * 
	 * @param context DSpace context.
	 * @param item    DSpace item.
	 * @param source  Free text source of the deposit.
	 * @throws IOException
	 * @throws AuthorizeException
	 * @throws SQLException
	 */
	public static void completeDeposit(Context context, Item item, String source)
			throws IOException, AuthorizeException, SQLException {
		// populate depositor as logged in user
		DSpaceUtils.setDepositor(context, item, context.getCurrentUser());

		// create user license if specified
		String rights = MetaDataUtil.getRights(item).toLowerCase();
		if (rights.equals("cc-by")) {
			CreativeCommonsService creativeCommonsService = LicenseServiceFactory.getInstance()
					.getCreativeCommonsService();
			// make sure there are no other licenses
			creativeCommonsService.removeLicense(context, item);
			MetaDataUtil.clearRights(context, item);

			// now insert license
			creativeCommonsService.setLicense(context, item, new FileInputStream(Consts.CREATIVE_COMMONS_BY_LICENCE),
					"text/plain");
		}

		// update citation
		DSpaceUtils.updateCitation(context, item);

		// make sure bitstream source is being set
		List<Bundle> bundles = item.getBundles();
		BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
		for (int i = 0; i < bundles.size(); i++) {
			List<Bitstream> bits = bundles.get(i).getBitstreams();
			for (int j = 0; j < bits.size(); j++) {
				Bitstream bs = bits.get(j);
				if (bs.getSource() == null) {
					bs.setSource(context, source);
					bitstreamService.update(context, bs);
				}
			}
		}
		 ItemService itemService = ContentServiceFactory.getInstance().getItemService();
		itemService.update(context, item);
	}

	/**
	 * Decode time period W3CDTF profile of ISO 8601.
	 * 
	 * @param encoding The ISO 8601 encoded string.
	 * @return An array of two date strings. The start date is first element, the
	 *         end date is the second element.
	 */
	public static String[] decodeTimePeriod(String encoding) {
		String[] dates = null;

		if (encoding != null) {
			String startStr = null;
			String endStr = null;

			// get tokens delimited by ";"- there should be three -
			// start=, end= and scheme=
			StringTokenizer st = new StringTokenizer(encoding, ";");

			if (st.countTokens() > 1) {
				for (int i = 0; i < st.countTokens(); i++) {
					if (i == 0) {
						startStr = st.nextToken();
					} else if (i == 1) {
						endStr = st.nextToken();
					} else {
						break;
					}
				}

				String startArray[] = startStr.split("=");
				String endArray[] = endStr.split("=");

				if (startArray.length == 2 || endArray.length == 2) {
					dates = new String[2];
				}

				if (startArray.length == 2) {
					dates[0] = startArray[1];
				}

				if (endArray.length == 2) {
					dates[1] = endArray[1];
				}
			}
		}

		return dates;
	}

	/**
	 * Encode a start and end date into W3CDTF profile of ISO 8601.
	 * 
	 * @param start Start date.
	 * @param end   End date.
	 * @return W3CDTF profile of ISO 8601.
	 */
	public static String encodeTimePeriod(String start, String end) {
		String startStr = "";
		String endString = "";

		if (start != null) {
			startStr = start;
		}

		if (end != null) {
			endString = end;
		}

		StringBuffer buf = new StringBuffer("start=");
		buf.append(startStr);
		buf.append("; end=");
		buf.append(endString);
		buf.append("; ");
		buf.append("scheme=");
		buf.append(ENCODING_SCHEME);

		return buf.toString();
	}

	/**
	 * Find eperson by email, switching off the authorisation system.
	 * 
	 * @param context DSpace context.
	 * @param email   Email address
	 * @return DSpace eperson object.
	 */
	public static EPerson findByEmail(Context context, String email) {
		EPerson eperson = null;

		context.turnOffAuthorisationSystem();

		try {
			EPersonService ePersonService = EPersonServiceFactory.getInstance().getEPersonService();
			eperson = ePersonService.findByEmail(context, email);
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		} catch (Exception ex) {/* this should never happen */
			throw new RuntimeException(ex);
		} finally {
			context.restoreAuthSystemState();
		}

		return eperson;
	}

	/**
	 * Get dublin core depositor value for submission info object.
	 * 
	 * @param info Submission ingest info object.
	 * @return Dublin core depositor value.
	 */
	public static String getDepositor(SubmissionInfo info) {
		return getDepositor(info.getSubmissionItem().getItem());
	}

	/**
	 * Get dublin core depositor value for item object.
	 * 
	 * @param item DSpace item.
	 * @return Dublin core depositor value.
	 */
	public static String getDepositor(Item item) {
		String depositor = "";

		List<MetadataValue> contributors = MetaDataUtil.getContributors(item);
		if (contributors.size() > 0) {
			depositor = contributors.get(0).getValue();
		}

		return depositor;
	}

	/**
	 * Get dublin core embargo value for submission info object.
	 * 
	 * @param info Submission ingest info object.
	 * @return Dublin core embargo value.
	 */
	public static DCDate getEmbargoValue(SubmissionInfo info) {
		return getEmbargoValue(info.getSubmissionItem().getItem());
	}

	/**
	 * Get dublin core embargo value for DSpace item object.
	 * 
	 * @param item DSpace item.
	 * @return Dublin core embago value.
	 */
	public static DCDate getEmbargoValue(Item item) {
		return MetaDataUtil.getDateAvailable(item);
	}

	/**
	 * @return Hijacked spatial (country) values.
	 */
	public static List<MetadataValue> getHijackedSpatial(Item item) {
		return MetaDataUtil.getSubjectDccs(item);
	}

	/**
	 * Get Is Published Before value for DSpace item object. This used DC Relation
	 * Is Format Of.
	 * 
	 * @param item DSpace item.
	 * @return Is Published Before value.
	 */
	public static boolean getIsPublishedBefore(SubmissionInfo info) {
		boolean isPublishedBefore = false;

		String value = MetaDataUtil.getIsFormatOf(info);

		if (value != null) {
			isPublishedBefore = Boolean.getBoolean(MetaDataUtil.getIsFormatOf(info));
		}

		return isPublishedBefore;
	}

	/**
	 * @param element DC element.
	 * @return DSpace metadata string.
	 */
	public static String getMdString(String element) {
		return getMdString(element, null);
	}

	/**
	 * @param element   DC element.
	 * @param qualifier DC qualifier.
	 * @return DSpace metadata string.
	 */
	public static String getMdString(String element, String qualifier) {
		return getMdString(MetadataSchema.DC_SCHEMA, element, qualifier);
	}

	/**
	 * @param schema    Schema identifer.
	 * @param element   DC element.
	 * @param qualifier DC qualifier.
	 * @return DSpace metadata string.
	 */
	public static String getMdString(String schema, String element, String qualifier) {
		StringBuilder sb = new StringBuilder();
		sb.append(schema).append(".");
		sb.append(element);

		if (qualifier != null) {
			sb.append(".").append(qualifier);
		}

		return sb.toString();
	}

	/**
	 * @param context DSpace context.
	 * @param request HTTP request.
	 * @return Special login group for datashare.
	 */
	public static List<Group> getSpecialGroups(Context context, HttpServletRequest request) {
		// Prevents anonymous users from being added to this group, and the second check
		// ensures they are password users
		List<Group> specialGroups = new ArrayList<Group>(0);
		try {
			String groupName = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("authentication-password", "login.specialgroup");
			if ((groupName != null) && (!groupName.trim().equals(""))) {
				GroupService groupService = EPersonServiceFactory.getInstance().getGroupService();
				Group specialGroup = groupService.findByName(context, groupName);
				if (specialGroup == null) {
					// Oops - the group isn't there.
					LOG.warn(LogManager.getHeader(context, "password_specialgroup",
							"Group defined in modules/authentication-password.cfg login.specialgroup does not exist"));
				} else {
					specialGroups.add(specialGroup);
				}
			}
		} catch (Exception e) {
			// The user is not an EASE user, so we don't need to worry about them
		}

		return specialGroups;
	}

	/**
	 * Get the current user license attached to a DSpace item.
	 * 
	 * @param info The DSpace submission object.
	 * @return The user license.
	 */
	public static UserLicense getUserLicenseType(SubmissionInfo info) {
		String str = MetaDataUtil.getRightsUri(info);
		int iVal = -1;

		if (str != null) {
			try {
				iVal = Integer.parseInt(str);
			} catch (NumberFormatException ex) {
				iVal = -1;
			}
		}

		return UserLicense.convert(iVal);
	}

	/**
	 * Does the item have an embargo?
	 * 
	 * @param context DSpace context.
	 * @param item    DSpace item.
	 * @return True if the dspace item is embargoed.
	 */
	public static boolean hasEmbargo(Context context, Item item) {
		return Util.hasEmbargo(context, item) || !Util.canReadAllBitstreams(context, item);
	}

	/**
	 * Does this item has a DOI?
	 * 
	 * @param item DSpace item.
	 * @return True if the dspace item has a DOI.
	 */
	public static boolean hasDoi(Item item) {
		boolean hasDoi = false;
		List<MetadataValue> identifers = MetaDataUtil.getIdentifierUris(item);
		for (MetadataValue dcValue : identifers) {
			if (dcValue.getValue().startsWith(DOI_URL)) {
				hasDoi = true;
				break;
			}
		}

		return hasDoi;
	}

	// NO LONGER IN USE
//	/**
//	 * @return True if is dspace running on a development environment.
//	 */
//	public static boolean isDev() {
//		return DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("dspace.hostname").substring(0, 5).equals("dlib-");
//	}
//
//	/**
//	 * @return True if is dspace running on a BETA environment.
//	 */
//	public static boolean isBeta() {
//		return DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("dspace.hostname").substring(0, 5).equals("devel");
//	}
//
//	/**
//	 * @return True if is dspace running on a live environment.
//	 */
//	public static boolean isLive() {
//		return (!isDev() && !isBeta());
//	}

	/**
	 * Remove a DSpace item from a collection (without database commit).
	 * 
	 * @param context    DSpace context.
	 * @param collection DSpace collection.
	 * @param item       The item to remove.
	 */
	public static void removeItem(Context context, Collection collection, Item item) {
		removeItem(context, collection, item, false);
	}

	/**
	 * Remove a DSpace item from a collection.
	 * 
	 * @param context    DSpace context.
	 * @param collection DSpace collection.
	 * @param item       The item to remove.
	 * @param commit     Commit to database?
	 */
	public static void removeItem(Context context, Collection collection, Item item, boolean commit) {
		context.turnOffAuthorisationSystem();

		try {
			CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
			collectionService.removeItem(context, collection, item);

			if (commit) {
				context.commit();
			}
		} catch (AuthorizeException ex) {
			/* this should never happen */} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		} finally {
			context.restoreAuthSystemState();
		}
	}

	/**
	 * Set the depositor value to be current user.
	 * 
	 * @param context DSpace context.
	 * @param info    The DSpace submission object.
	 * @param user    The current user.
	 * @throws SQLException
	 */
	public static void setDepositor(Context context, SubmissionInfo info, EPerson user) throws SQLException {
		MetaDataUtil.setContributor(context, info, user.getLastName() + ", " + user.getFirstName());
	}

	/**
	 * Set the contributor.
	 * 
	 * @param context DSpace context.
	 * @param item    The DSpace item.
	 * @param user    The current user.
	 * @throws SQLException
	 */
	public static void setDepositor(Context context, Item item, EPerson user) throws SQLException {
		MetaDataUtil.setContributor(context, item, user.getLastName() + ", " + user.getFirstName());
	}

	/**
	 * Set the current DSpace embargo end date.
	 * 
	 * @param context DSpace context.
	 * @param item    The DSpace item.
	 * @param date    The embargo end date.
	 * @throws SQLException
	 */
	public static void setEmbargoValue(Context context, Item item, DCDate date) throws SQLException {
		if (date != null) {
			MetaDataUtil.setDateAvailable(context, item, date);
		} else {
			MetaDataUtil.clearDateAvailable(context, item);
		}
	}

	/**
	 * Set the hijacked spatial (country) value.
	 * 
	 * @param context DSpace context.
	 * @param item    The DSpace item.
	 * @param value   New country value.
	 * @throws SQLException
	 */
	public static void setHijackedSpatial(Context context, Item item, String value) throws SQLException {
		MetaDataUtil.setSubjectDcc(context, item, value);
	}

	/**
	 * Set the current DSpace Is Published Before value. This uses DC Relation Is
	 * Format Of.
	 * 
	 * @param context DSpace context.
	 * @param info    The DSpace submission object.
	 * @param value   The new Is Published Before value.
	 * @throws SQLException
	 */
	public static void setIsPublishedBefore(Context context, SubmissionInfo info, boolean value) throws SQLException {
		MetaDataUtil.setIsFormatOf(context, info, Boolean.toString(value));
	}

	/**
	 * Set the current DSpace user license.
	 * 
	 * @param context DSpace context.
	 * @param info    The DSpace submission object.
	 * @param value   The new license value.
	 * @throws SQLException
	 */
	public static void setUserLicenseType(Context context, SubmissionInfo info, String value) throws SQLException {
		MetaDataUtil.setRightsUri(context, info, value, true);
	}

	/**
	 * Set the whether to use DataShare embargo option. This hijacks DC Date
	 * Copyright field.
	 * 
	 * @param context DSpace context.
	 * @param info    The DSpace submission object.
	 * @param value   Is the embago option being used?
	 * @throws SQLException
	 */
	public static void setUseEmbargo(Context context, SubmissionInfo info, boolean useEmbargo) throws SQLException {
		MetaDataUtil.setDateCopyright(context, info, Boolean.toString(useEmbargo));
	}

	/**
	 * Shoe tombstone.
	 * 
	 * @param context DSpace context.
	 * @param item    DSpace item
	 * @return Whether a tombstone item record be shown?
	 */
	public static boolean showTombstone(Context context, Item item) {
		boolean show = false;

		try {
			AuthorizeService authorizeService = AuthorizeServiceFactory.getInstance().getAuthorizeService();
			if (!authorizeService.isAdmin(context)) {
				String tomb = MetaDataUtil.getShowTombstone(item);
				if (tomb != null) {
					show = Boolean.parseBoolean(tomb);
				}
			}
		} catch (SQLException ex) {
			throw new RuntimeException("Problem determining access right", ex);
		}

		return show;
	}

	/**
	 * Create a citation for a given DSpace submission item.
	 * 
	 * @param subInfo DSpace submission item.
	 * @throws SQLException
	 */
	public static void updateCitation(Context context, SubmissionInfo subInfo) throws SQLException {
		updateCitation(context, subInfo.getSubmissionItem().getItem());
	}

	/**
	 * Create a citation for a given DSpace item.
	 * 
	 * @param item DSpace item.
	 * @throws SQLException
	 */
	public static void updateCitation(Context context, Item item) throws SQLException {
		StringBuffer buffer = new StringBuffer(200);

		List<MetadataValue> surnames = MetaDataUtil.getCreators(item);
		boolean creatorGiven = surnames.size() > 0;

		if (creatorGiven) {
			// add creators
			for (int i = 0; i < surnames.size(); i++) {
				if (i > 0) {
					buffer.append("; ");
				}

				buffer.append(surnames.get(i).getValue());
			}

			buffer.append(". ");
		} else {
			appendPublisher(buffer, item);
			buffer.append(" ");
		}

		// add date available year if available
		buffer.append("(");
		DCDate dateAvailable = MetaDataUtil.getDateAvailable(item);
		if (dateAvailable != null) {
			buffer.append(MetaDataUtil.getDateAvailable(item).getYear());
		} else {
			// no date available, use current date
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(new Date());
			buffer.append(calendar.get(Calendar.YEAR));
		}
		buffer.append("). ");

		// add title
		buffer.append(MetaDataUtil.getTitle(item));
		buffer.append(", ");

		// add time period
		String timePeriod = MetaDataUtil.getTemporal(item);
		if (timePeriod != null) {
			String dates[] = DSpaceUtils.decodeTimePeriod(timePeriod);

			if (dates == null || dates.length != 2) {
				LOG.warn("Invalid time period:" + timePeriod);
			} else {
				String from = dates[0].substring(0, 4);
				String to = dates[1].substring(0, 4);

				if (from.equals(to)) {
					// if both years are the same just use one
					timePeriod = from;
				} else {
					timePeriod = from + "-" + to;
				}

				if (timePeriod != null) {
					buffer.append(timePeriod);
					buffer.append(" ");
				}
			}
		}

		// add item type
		buffer.append("[");
		buffer.append(MetaDataUtil.getType(item));
		buffer.append("].");

		// append publisher if creator is specified
		if (creatorGiven) {
			appendPublisher(buffer, item);
		}

		List<MetadataValue> identifers = MetaDataUtil.getIdentifierUris(item);
		for (MetadataValue dcValue : identifers) {
			if (dcValue.getValue().startsWith(DOI_URL)) {
				buffer.append(" ");
				buffer.append(dcValue.getValue());
				buffer.append(".");
				break;
			}
		}

		// finally update citation
		MetaDataUtil.setCitation(context, item, buffer.toString());
	}

	/**
	 * Is the embargo option being used?
	 * 
	 * @param info The DSpace submission object.
	 * @return True if the embargo option is being used.
	 */
	public static boolean useEmbargo(SubmissionInfo info) {
		boolean use = false;

		String value = MetaDataUtil.getDateCopyright(info);

		if (value != null) {
			use = Boolean.parseBoolean(value);
		}

		return use;
	}
}
