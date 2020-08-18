package uk.ac.edina.datashare.utils;

import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

import org.dspace.app.util.SubmissionInfo;
import org.dspace.content.DCDate;
import org.dspace.content.Item;
import org.dspace.content.MetadataSchema;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;

/**
 * Meta data utility. This wraps the getting and setting of getting DSpace
 * metadata.
 */
public class MetaDataUtil {
	private static final Logger LOG = Logger.getLogger(MetaDataUtil.class);

	private static final String CITATION_ELEMENT = "identifier";
	private static final String CITATION_QUALIFIER = "citation";

	private static final String CONTRIBUTOR_ELEMENT = "contributor";
	private static final String CONTRIBUTOR_OTHER_QUALIFIER = "other";

	private static final String CREATOR_ELEMENT = "creator";
	static final String CREATOR_STR = MetadataSchema.DC_SCHEMA + "." + CREATOR_ELEMENT;

	private static final String DATASHARE_SCHEMA = "ds";
	private static final String DATE_ELEMENT = "date";
	private static final String DATE_AVAILABLE_QUALIFIER = "available";
	private static final String DATE_COPYRIGHT_QUALIFIER = "copyright";
	static final String DEPOSITOR_STR = MetadataSchema.DC_SCHEMA + "." + CONTRIBUTOR_ELEMENT;

	private static final String FORMAT_ELEMENT = "format";

	private static final String IDENTIFIER_ELEMENT = "identifier";
	private static final String IDENTIFIER_URI_ELEMENT = "uri";

	private static final String PUBLISHER_ELEMENT = "publisher";
	static final String PUBLISHER_STR = MetadataSchema.DC_SCHEMA + "." + PUBLISHER_ELEMENT;

	private static final String RELATION_IS_FORMAT_ELEMENT = "relation";
	private static final String RELATION_IS_FORMAT_QUALIFIER = "isformatof";

	private static final String RIGHTS_ELEMENT = "rights";
	private static final String RIGHTS_URI_ELEMENT = "uri";

	private static final String SPATIAL_ELEMENT = "coverage";
	private static final String SPATIAL_QUALIFIER = "spatial";

	private static final String SOURCE_ELEMENT = "source";

	public static final String SUBJECT_DDC_ELEMENT = "subject";
	public static final String SUBJECT_DDC_QUALIFIER = "ddc";

	private static final String TEMPORAL_ELEMENT = SPATIAL_ELEMENT;
	private static final String TEMPORAL_QUALIFIER = "temporal";

	private static final String TITLE_ELEMENT = "title";
	private static final String TITLE_ALTERNATIVE_QUALIFIER = "alternative";

	private static final String TYPE_ELEMENT = "type";

	private static final String TOMBSTONE_ELEMENT = "withdrawn";
	private static final String TOMBSTONE_SHOW_QUALIFIER = "showtombstone";

	private static final String DEFAULT_LANG = "en";

//    private static final ItemService itemService = ContentServiceFactory.getInstance().getItemService();

	/**
	 * Clear metadata values for specified element using a null qualifier and any
	 * language.
	 * 
	 * @param context DSpace context.
	 * @param item    DSpace item.
	 * @param element DSpace dublin core element.
	 * @throws SQLException
	 * @throws ,
	 */
	public static void clearMetaData(Context context, Item item, String element) throws SQLException {
		clearMetaData(context, item, element, null, Item.ANY);
	}

	/**
	 * Clear metadata values for specified element and qualifier and any language.
	 * 
	 * @param context   DSpace context.
	 * @param item      DSpace item.
	 * @param element   DSpace dublin core element.
	 * @param qualifier DSpace dublin core qualifier.
	 * @throws SQLException
	 */
	public static void clearMetaData(Context context, Item item, String element, String qualifier) throws SQLException {
		clearMetaData(context, item, element, qualifier, Item.ANY);
	}

	/**
	 * Clear metadata values for specified element, qualifier and language.
	 * 
	 * @param context   DSpace context.
	 * @param item      DSpace item.
	 * @param element   DSpace dublin core element.
	 * @param qualifier DSpace dublin core qualifier.
	 * @param language  Text language used.
	 * @throws SQLException
	 */
	public static void clearMetaData(Context context, Item item, String element, String qualifier, String lang)
			throws SQLException {
		ItemService itemService = ContentServiceFactory.getInstance().getItemService();
		itemService.clearMetadata(context, item, MetadataSchema.DC_SCHEMA, element, qualifier, lang);
	}

	/**
	 * Clear dublin core Citation metadata value.
	 * 
	 * @param context DSpace context.
	 * @param item    DSpace item object.
	 * @throws SQLException
	 */
	public static void clearCitation(Context context, Item item) throws SQLException {
		clearMetaData(context, item, CITATION_ELEMENT, CITATION_QUALIFIER);
	}

	/**
	 * Clear dublin core Contributor metadata value.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace item object.
	 * @throws SQLException
	 */
	public static void clearContributor(Context context, Item item) throws SQLException {
		clearMetaData(context, item, CONTRIBUTOR_ELEMENT, null, Item.ANY);
	}

	/**
	 * Clear dublin core Date Available metadata value.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace item object.
	 * @throws SQLException
	 */
	public static void clearDateAvailable(Context context, Item item) throws SQLException {
		clearMetaData(context, item, DATE_ELEMENT, DATE_AVAILABLE_QUALIFIER);
	}

	/**
	 * Clear dublin core Date Copyright metadata value.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission object.
	 */
	public static void clearDateCopyright(Context context, SubmissionInfo info) {
		clearDateCopyright(context, info);
	}

	/**
	 * Clear dublin core Date Copyright metadata value.
	 * 
	 * @param context DSpace context.
	 * @param item    The DSpace item.
	 * @throws SQLException
	 */
	public static void clearDateCopyright(Context context, Item item) throws SQLException {
		clearMetaData(context, item, DATE_ELEMENT, DATE_COPYRIGHT_QUALIFIER);
	}

	/**
	 * Clear dublin core is format of value.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission object.
	 * @throws SQLException
	 */
	public static void clearIsFormatOf(Context context, SubmissionInfo info) throws SQLException {
		clearMetaData(context, info.getSubmissionItem().getItem(), RELATION_IS_FORMAT_ELEMENT,
				RELATION_IS_FORMAT_QUALIFIER);
	}

	/**
	 * Clear dublin core publisher value.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission object.
	 * @throws SQLException
	 */
	public static void clearPublisher(Context context, SubmissionInfo info) throws SQLException {
		clearMetaData(context, info.getSubmissionItem().getItem(), PUBLISHER_ELEMENT);
	}

	/**
	 * Clear dublin core rights.uri value.
	 * 
	 * @param context DSpace context.
	 * @param item    DSpace item.
	 * @throws SQLException
	 */
	public static void clearRights(Context context, Item item) throws SQLException {
		clearMetaData(context, item, RIGHTS_ELEMENT);
	}

	/**
	 * Clear dublin core rights.uri value.
	 * 
	 * @param context DSpace context.
	 * @param item    DSpace item.
	 * @throws SQLException
	 */
	public static void clearRightsUri(Context context, Item item) throws SQLException {
		clearMetaData(context, item, RIGHTS_ELEMENT, RIGHTS_URI_ELEMENT);
	}

	/**
	 * Clear dublin core rights.uri value.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission object.
	 * @throws SQLException
	 */
	public static void clearRightsUri(Context context, SubmissionInfo info) throws SQLException {
		clearRightsUri(context, info.getSubmissionItem().getItem());
	}

	/**
	 * Clear dublin core Subject DCC metadata value.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission object.
	 * @throws SQLException
	 */
	public static void clearSubjectDcc(Context context, SubmissionInfo info) throws SQLException {
		clearSubjectDcc(context, info.getSubmissionItem().getItem());
	}

	/**
	 * Clear dublin core Subject DCC metadata value.
	 * 
	 * @param context DSpace context.
	 * @param item    The DSpace item.
	 * @throws SQLException
	 */
	public static void clearSubjectDcc(Context context, Item item) throws SQLException {
		clearMetaData(context, item, SUBJECT_DDC_ELEMENT, SUBJECT_DDC_QUALIFIER);
	}

	/**
	 * Clear dublin core Coverage Temporal metadata value.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission object.
	 * @throws SQLException
	 */
	public static void clearTemporal(Context context, Item item) throws SQLException {
		clearMetaData(context, item, TEMPORAL_ELEMENT, TEMPORAL_QUALIFIER);
	}

	/**
	 * Clear dublin core Alternative Title metadata value.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission object.
	 * @throws SQLException
	 */
	public static void clearTitleAlternative(Context context, SubmissionInfo info) throws SQLException {
		clearMetaData(context, info.getSubmissionItem().getItem(), TITLE_ELEMENT, TITLE_ALTERNATIVE_QUALIFIER);
	}

	/**
	 * Get dublin core identifier.citation value.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission object.
	 * @return DSpace dublin core identifier.citation value.
	 */
	public static String getCitation(Context context, SubmissionInfo info) {
		return getCitation(info.getSubmissionItem().getItem());
	}

	/**
	 * Get dublin core identifier.citation value.
	 * 
	 * @param item DSpace item.
	 * @return DSpace dublin core identifier.citation value.
	 */
	public static String getCitation(Item item) {
		return getUnique(item, CITATION_ELEMENT, CITATION_QUALIFIER);
	}

	/**
	 * Get array of dublin core contributor values.
	 * 
	 * @param info DSpace submission object.
	 * @return DSpace Item contributors.
	 */
	public static List<MetadataValue> getContributors(SubmissionInfo info) {
		return getContributors(info.getSubmissionItem().getItem());
	}

	/**
	 * Get array of dublin core contributor values.
	 * 
	 * @param item DSpace item.
	 * @return DSpace Item contributors.
	 */
	public static List<MetadataValue> getContributors(Item item) {
		ItemService itemService = ContentServiceFactory.getInstance().getItemService();
		return itemService.getMetadata(item, MetadataSchema.DC_SCHEMA, CONTRIBUTOR_ELEMENT, null, Item.ANY);
	}

	/**
	 * Get dublin core contributor.other value.
	 * 
	 * @param info DSpace submission object.
	 * @return DSpace dublin core contributor.other value.
	 */
	public static String getContributorOther(SubmissionInfo info) {
		ItemService itemService = ContentServiceFactory.getInstance().getItemService();
		return getUnique(info.getSubmissionItem().getItem(), CONTRIBUTOR_ELEMENT, CONTRIBUTOR_OTHER_QUALIFIER);
	}

	/**
	 * Get first dublin core creator value.
	 * 
	 * @param info DSpace submission object.
	 * @return First dublin core creator value.
	 */
	public static String getCreator(SubmissionInfo info) {
		return getUnique(info.getSubmissionItem().getItem(), CREATOR_ELEMENT);
	}

	/**
	 * Get all dublin core creator values.
	 * 
	 * @param info DSpace submission object.
	 * @return All dublin core creator values.
	 */
	public static List<MetadataValue> getCreators(SubmissionInfo info) {
		return getCreators(info.getSubmissionItem().getItem());
	}

	/**
	 * Get all dublin core creator values.
	 * 
	 * @param item DSpace item.
	 * @return All dublin core creator values.
	 */
	public static List<MetadataValue> getCreators(Item item) {
		ItemService itemService = ContentServiceFactory.getInstance().getItemService();
		return itemService.getMetadata(item, MetadataSchema.DC_SCHEMA, CREATOR_ELEMENT, Item.ANY, Item.ANY);
	}

	/**
	 * Get earliest date available.
	 * 
	 * @param item DSpace item.
	 * @return Earliest date available.
	 */
	public static DCDate getDateAvailable(Item item) {
		DCDate date = null;

		// get unique gets the earliest date
		String value = getUnique(item, DATE_ELEMENT, DATE_AVAILABLE_QUALIFIER);

		if (value != null) {
			date = new DCDate(value);
		}

		return date;
	}

	/**
	 * Get array of data available values.
	 * 
	 * @param item DSpace item.
	 * @return Array of data available values.
	 */
	public static List<MetadataValue> getDateAvailables(Item item) {
		ItemService itemService = ContentServiceFactory.getInstance().getItemService();
		return itemService.getMetadata(item, MetadataSchema.DC_SCHEMA, DATE_ELEMENT, DATE_AVAILABLE_QUALIFIER,
				Item.ANY);
	}

	/**
	 * Get dublin core Date Copyright value.
	 * 
	 * @param info DSpace submission object.
	 * @return Dublin core Date Copyright value.
	 */
	public static String getDateCopyright(SubmissionInfo info) {
		return getUnique(info.getSubmissionItem().getItem(), DATE_ELEMENT, DATE_COPYRIGHT_QUALIFIER);
	}

	/**
	 * Get dublin core Identifier value.
	 * 
	 * @param item DSpace item.
	 * @return Dublin core Identifier value.
	 */
	public static String getIdentifier(Item item) {
		return getUnique(item, IDENTIFIER_ELEMENT, null, DEFAULT_LANG);
	}

	/**
	 * Get array of identifer uri values.
	 * 
	 * @param item DSpace item.
	 * @return All dublin identifier uri values.
	 */
	public static List<MetadataValue> getIdentifierUris(Item item) {
		ItemService itemService = ContentServiceFactory.getInstance().getItemService();
		return itemService.getMetadata(item, MetadataSchema.DC_SCHEMA, IDENTIFIER_ELEMENT, IDENTIFIER_URI_ELEMENT,
				Item.ANY);
	}

	/**
	 * Get dublin core Relation Is Format Of value.
	 * 
	 * @param info DSpace submission object.
	 * @return Dublin core Relation Is Format value.
	 */
	public static String getIsFormatOf(SubmissionInfo info) {
		return getUnique(info.getSubmissionItem().getItem(), RELATION_IS_FORMAT_ELEMENT, RELATION_IS_FORMAT_QUALIFIER);
	}

	/**
	 * Get dublin core publisher value.
	 * 
	 * @param info DSpace submission object.
	 * @return Dublin core publisher value.
	 */
	public static String getPublisher(SubmissionInfo info) {
		return getPublisher(info.getSubmissionItem().getItem());
	}

	/**
	 * Get dublin core publisher value.
	 * 
	 * @param item DSpace item.
	 * @return Dublin core publisher value.
	 */
	public static String getPublisher(Item item) {
		return getUnique(item, PUBLISHER_ELEMENT);
	}

	/**
	 * @param info DSpace submission object.
	 * @return DSpace Item rights statement.
	 */
	public static String getRights(SubmissionInfo info) {
		return getRights(info.getSubmissionItem().getItem());
	}

	/**
	 * @param item DSpace submission object.
	 * @return DSpace DSpace item.
	 */
	public static String getRights(Item item) {
		return getUnique(item, RIGHTS_ELEMENT, null);
	}

	/**
	 * Get first rights URI of an item.
	 * 
	 * @param info DSpace submission object.
	 * @return The first rights URI.
	 */
	public static String getRightsUri(SubmissionInfo info) {
		return getUnique(info.getSubmissionItem().getItem(), RIGHTS_ELEMENT, RIGHTS_URI_ELEMENT);
	}

	/**
	 * Get array of dublin core rights.uri values.
	 * 
	 * @param info DSpace submission object.
	 * @return Dublin core rights.uri values.
	 */
	public static List<MetadataValue> getRightsUris(SubmissionInfo info) {
		ItemService itemService = ContentServiceFactory.getInstance().getItemService();
		return itemService.getMetadata(info.getSubmissionItem().getItem(), MetadataSchema.DC_SCHEMA, RIGHTS_ELEMENT,
				RIGHTS_URI_ELEMENT, Item.ANY);
	}

	/**
	 * Get dublin core source value of a submission item.
	 * 
	 * @param info DSpace submission object.
	 * @return Dublin core source value.
	 */
	public static String getSource(SubmissionInfo info) {
		return getUnique(info.getSubmissionItem().getItem(), SOURCE_ELEMENT);
	}

	/**
	 * Get dublin core value for spatial coverage from a DSpace item.
	 * 
	 * @param item DSpace item.
	 * @return Spatial coverage dublin core value.
	 */
	public static List<MetadataValue> getSpatial(Item item) {
		ItemService itemService = ContentServiceFactory.getInstance().getItemService();
		return itemService.getMetadata(item, MetadataSchema.DC_SCHEMA, SPATIAL_ELEMENT, SPATIAL_QUALIFIER, Item.ANY);
	}

	/**
	 * Get the first dublin core value for spatial coverage from a DSpace item.
	 * 
	 * @param item DSpace item.
	 * @return The first spatial coverage dublin core value.
	 */
	public static String getSpatialFirst(Item item) {
		return getUnique(item, SPATIAL_ELEMENT, SPATIAL_QUALIFIER, Item.ANY);
	}

	/**
	 * @param item DSpace item.
	 * @return Get show tombsomstone metadata value.
	 */
	public static String getShowTombstone(Item item) {
		return getUnique(item, TOMBSTONE_ELEMENT, TOMBSTONE_SHOW_QUALIFIER, Item.ANY, DATASHARE_SCHEMA);
	}

	/**
	 * Get dublin core value for Subject DCC from a submission item.
	 * 
	 * @param item DSpace submission item.
	 * @return subject dcc dublin core value.
	 */
	public static String getSubjectDcc(SubmissionInfo info) {
		return getUnique(info.getSubmissionItem().getItem(), SUBJECT_DDC_ELEMENT, SUBJECT_DDC_QUALIFIER);
	}

	/**
	 * Get dublin core values for Subject DCC from a submission item.
	 * 
	 * @param item DSpace submission item.
	 * @return subject dcc dublin core values.
	 */
	public static List<MetadataValue> getSubjectDccs(Item item) {
		ItemService itemService = ContentServiceFactory.getInstance().getItemService();
		return itemService.getMetadata(item, MetadataSchema.DC_SCHEMA, SUBJECT_DDC_ELEMENT, SUBJECT_DDC_QUALIFIER,
				Item.ANY);
	}

	/**
	 * Get dublin core value for temporal coverage from a submission item.
	 * 
	 * @param item DSpace submission item.
	 * @return Temporal coverage dublin core value.
	 */
	public static String getTemporal(SubmissionInfo info) {
		return getTemporal(info.getSubmissionItem().getItem());
	}

	/**
	 * Get dublin core value for temporal coverage from a submission item.
	 * 
	 * @param item DSpace item.
	 * @return Temporal coverage dublin core value.
	 */
	public static String getTemporal(Item item) {
		return getUnique(item, TEMPORAL_ELEMENT, TEMPORAL_QUALIFIER);
	}

	/**
	 * Get type dublin core value.
	 * 
	 * @param info DSpace submission object.
	 * @return DSpace Item type.
	 */
	public static String getType(SubmissionInfo info) {
		return getType(info.getSubmissionItem().getItem());
	}

	/**
	 * Get type dublin core value.
	 * 
	 * @param item DSpace item.
	 * @return DSpace Item type.
	 */
	public static String getType(Item item) {
		return getUnique(item, TYPE_ELEMENT);
	}

	/**
	 * Get dublin core title.
	 * 
	 * @param info DSpace submission object.
	 * @return DSpace Item title.
	 */
	public static String getTitle(SubmissionInfo info) {
		return getTitle(info.getSubmissionItem().getItem());
	}

	/**
	 * Get dublin core title.
	 * 
	 * @param item DSpace item.
	 * @return DSpace Item title.
	 */
	public static String getTitle(Item item) {
		return getUnique(item, TITLE_ELEMENT, null);
	}

	/**
	 * Get unique metadata value from DSpace item using any qualifier and any
	 * language.
	 * 
	 * @param item    DSpace item.
	 * @param element Metadata element.
	 * @return Metadata value.
	 */
	public static String getUnique(Item item, String element) {
		return getUnique(item, element, Item.ANY, Item.ANY);
	}

	/**
	 * Get unique metadata value from DSpace item using any language.
	 * 
	 * @param item      DSpace item.
	 * @param element   Metadata element.
	 * @param qualifier Metadata qualifier.
	 * @return Metadata value.
	 */
	public static String getUnique(Item item, String element, String qualifier) {
		return getUnique(item, element, qualifier, Item.ANY);
	}

	/**
	 * Get unique metadata value from DSpace item.
	 * 
	 * @param item      DSpace item.
	 * @param element   Metadata element.
	 * @param qualifier Metadata qualifier.
	 * @param lang      Metadata language.
	 * @return Metadata value.
	 */
	public static String getUnique(Item item, String element, String qualifier, String lang) {
		return getUnique(item, element, qualifier, lang, MetadataSchema.DC_SCHEMA);
	}

	/**
	 * Get unique metadata value from DSpace item.
	 * 
	 * @param item      DSpace item.
	 * @param element   Metadata element.
	 * @param qualifier Metadata qualifier.
	 * @param lang      Metadata language.
	 * @param schema    Metadata schema.
	 * @return Metadata value.
	 */
	public static String getUnique(Item item, String element, String qualifier, String lang, String schema) {
		String value = null;
		ItemService itemService = ContentServiceFactory.getInstance().getItemService();
		
		// DATASHARE - start
		LOG.debug("itemService:");
		LOG.debug(itemService);
		
		LOG.debug("item, element, qualifier, lang: ");
		LOG.debug(item);
		LOG.debug(element);
		LOG.debug(qualifier);
		LOG.debug(lang);
		LOG.debug(schema);
		// DATASHARE - end
		
		List<MetadataValue> values = itemService.getMetadata(item, schema, element, qualifier, lang);
		
		// DATASHARE - start
		LOG.debug("values: ");
		LOG.debug(values);
		// DATASHARE - end
		
		if (values != null && values.size() > 0) {
			value = values.get(0).getValue();
		}

		return value;
	}

	/**
	 * Set DSpace item citation. Any existing value will first be deleted.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission object.
	 * @param value   Item citation.
	 * @throws SQLException
	 */
	public static void setCitation(Context context, SubmissionInfo info, String value) throws SQLException {
		// rights is unique ensure entries are first removed
		setCitation(context, info.getSubmissionItem().getItem(), value);
	}

	/**
	 * Set DSpace item citation.
	 * 
	 * @param context DSpace context.
	 * @param item    DSpace item.
	 * @param value   New citation value.
	 * @throws SQLException
	 */
	public static void setCitation(Context context, Item item, String value) throws SQLException {
		// rights is unique ensure entries are first removed
		setUnique(context, item, CITATION_ELEMENT, CITATION_QUALIFIER, value);
	}

	/**
	 * Add a DSpace item contributor.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission object.
	 * @param value   Item contributor.
	 * @throws SQLException
	 */
	public static void setContributor(Context context, SubmissionInfo info, String value) throws SQLException {
		setContributor(context, info.getSubmissionItem().getItem(), value);
	}

	/**
	 * Add a DSpace item contributor.
	 * 
	 * @param context DSpace context.
	 * @param item    DSpace item.
	 * @param value   New contributor value.
	 * @throws SQLException
	 */
	public static void setContributor(Context context, Item item, String value) throws SQLException {
		ItemService itemService = ContentServiceFactory.getInstance().getItemService();
		itemService.addMetadata(context, item, MetadataSchema.DC_SCHEMA, CONTRIBUTOR_ELEMENT, null, DEFAULT_LANG,
				value);
	}

	/**
	 * Set a DSpace item contributor.other value. Any existing value will first be
	 * deleted.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission object.
	 * @param value   New contributor.other value.
	 * @throws SQLException
	 */
	public static void setContributorOther(Context context, SubmissionInfo info, String value) throws SQLException {
		setUnique(context, info.getSubmissionItem().getItem(), CONTRIBUTOR_ELEMENT, CONTRIBUTOR_OTHER_QUALIFIER, value);
	}

	/**
	 * Set a DSpace item date available value. Any existing value will first be
	 * deleted.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission object.
	 * @param value   New date available value.
	 * @throws SQLException
	 */
	public static void setDateAvailable(Context context, Item item, DCDate value) throws SQLException {
		setDateAvailable(context, item, value.toString());
	}

	/**
	 * Set a DSpace item date available value. Any existing value will first be
	 * deleted.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission object.
	 * @param value   New date available string value.
	 * @throws SQLException
	 */
	public static void setDateAvailable(Context context, Item item, String value) throws SQLException {
		setUnique(context, item, DATE_ELEMENT, DATE_AVAILABLE_QUALIFIER, Item.ANY, value);
	}

	/**
	 * Set a DSpace item Date Copyright value. Any existing value will first be
	 * deleted.
	 * 
	 * @param context DSpace context.
	 * @param item    DSpace submission object.
	 * @param value   New format value.
	 * @throws SQLException
	 */
	public static void setDateCopyright(Context context, SubmissionInfo info, String value) throws SQLException {
		setUnique(context, info.getSubmissionItem().getItem(), DATE_ELEMENT, DATE_COPYRIGHT_QUALIFIER, value);
	}

	/**
	 * Set a DSpace item format value. Any existing value will first be deleted.
	 * 
	 * @param item  DSpace submission object.
	 * @param value New format value.
	 * @throws SQLException
	 */
	public static void setFormat(Context context, Item item, String value) throws SQLException {
		setUnique(context, item, FORMAT_ELEMENT, value);
	}

	/**
	 * Set a DSpace item Identifier value.
	 * 
	 * @param item  DSpace item object.
	 * @param value The new value.
	 * @throws SQLException
	 */
	public static void setIdentifier(Context context, Item item, String value) throws SQLException {
		setIdentifier(context, item, value, DEFAULT_LANG);
	}

	/**
	 * Set a DSpace item Identifier value.
	 * 
	 * @param context DSpace context.
	 * @param item    DSpace item object.
	 * @param value   The new value.
	 * @param lang    The value language.
	 * @throws SQLException
	 */
	public static void setIdentifier(Context context, Item item, String value, String lang) throws SQLException {
		ItemService itemService = ContentServiceFactory.getInstance().getItemService();
		itemService.addMetadata(context, item, MetadataSchema.DC_SCHEMA, IDENTIFIER_ELEMENT, null, lang, value);
	}

	/**
	 * Set a DSpace item Relation Is Format Of value. Any existing value will first
	 * be deleted.
	 * 
	 * @param context DSpace context.
	 * @param item    DSpace submission object.
	 * @param value   New format value.
	 * @throws SQLException
	 */
	public static void setIsFormatOf(Context context, SubmissionInfo info, String value) throws SQLException {
		setUnique(context, info.getSubmissionItem().getItem(), RELATION_IS_FORMAT_ELEMENT, RELATION_IS_FORMAT_QUALIFIER,
				value);
	}

	/**
	 * Set a DSpace item Publisher value. Any existing value will first be deleted.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission object.
	 * @param value   New publisher value.
	 * @throws SQLException
	 */
	public static void setPublisher(Context context, SubmissionInfo info, String value) throws SQLException {
		// rights is unique ensure entries are first removed
		setUnique(context, info.getSubmissionItem().getItem(), PUBLISHER_ELEMENT, value);
	}

	/**
	 * Set DSpace item rights statement. Any existing value will first be deleted.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission object.
	 * @param value   Item rights statement.
	 * @throws SQLException
	 */
	public static void setRights(Context context, SubmissionInfo info, String value) throws SQLException {
		// rights is unique ensure entries are first removed
		setUnique(context, info.getSubmissionItem().getItem(), RIGHTS_ELEMENT, value);
	}

	/**
	 * Set/add DSpace item rights uri. * @param context DSpace context.
	 * 
	 * @param info   DSpace submission object.
	 * @param value  The new right.uri value.
	 * @param unique If true any existing values will first be deleted.
	 * @throws SQLException
	 */
	public static void setRightsUri(Context context, SubmissionInfo info, String value, boolean unique)
			throws SQLException {
		Item item = info.getSubmissionItem().getItem();

		if (unique) {
			clearRightsUri(context, item);
		}

		setRightsUri(context, item, value);
	}

	/**
	 * Add DSpace item rights.uri using default language.
	 * 
	 * @param context DSpace context.
	 * @param item    DSpace submission object.
	 * @param value   The new right.uri value.
	 * @throws SQLException
	 */
	public static void setRightsUri(Context context, Item item, String value) throws SQLException {
		ItemService itemService = ContentServiceFactory.getInstance().getItemService();
		itemService.addMetadata(context, item, MetadataSchema.DC_SCHEMA, RIGHTS_ELEMENT, RIGHTS_URI_ELEMENT,
				DEFAULT_LANG, value);
	}

	/**
	 * Set DSpace dublin core source value. Any existing source value will first be
	 * deleted.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission object.
	 * @param value   The new source value.
	 * @throws SQLException
	 */
	public static void setSource(Context context, SubmissionInfo info, String value) throws SQLException {
		setUnique(context, info.getSubmissionItem().getItem(), SOURCE_ELEMENT, value);
	}

	/**
	 * Set DSpace dublin core coverage spatial value.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission object.
	 * @param value   The new spatial value.
	 * @param unique  Detemines whether any existing values are deleted. If unique
	 *                is true all existing values will be deleted.
	 * @throws SQLException
	 */
	public static void setSpatial(Context context, Item item, String value, boolean unique) throws SQLException {
		if (unique) {
			setSpatial(context, item, value);
		} else {
			ItemService itemService = ContentServiceFactory.getInstance().getItemService();
			itemService.addMetadata(context, item, MetadataSchema.DC_SCHEMA, SPATIAL_ELEMENT, SPATIAL_QUALIFIER,
					DEFAULT_LANG, value);
		}
	}

	/**
	 * Set DSpace dublin core coverage.spatial value. Any existing source value will
	 * first be deleted.
	 * 
	 * @param context DSpace context.
	 * @param item    DSpace item.
	 * @param value   The new coverage.spatial value.
	 * @throws SQLException
	 */
	public static void setSpatial(Context context, Item item, String value) throws SQLException {
		setUnique(context, item, SPATIAL_ELEMENT, SPATIAL_QUALIFIER, value);
	}

	/**
	 * Set DSpace dublin core subject.dcc value.
	 * 
	 * @param context DSpace context.
	 * @param item    DSpace item.
	 * @param value   The new subject.dcc value.
	 * @throws SQLException
	 */
	public static void setSubjectDcc(Context context, Item item, String value) throws SQLException {
		ItemService itemService = ContentServiceFactory.getInstance().getItemService();
		itemService.addMetadata(context, item, MetadataSchema.DC_SCHEMA, SUBJECT_DDC_ELEMENT, SUBJECT_DDC_QUALIFIER,
				DEFAULT_LANG, value);
	}

	/**
	 * Set the dublin core temporal coverage value for a DSpace submission item. Any
	 * existing value will first be deleted.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission item.
	 * @param value   The new temporal coverage value.
	 * @throws SQLException
	 */
	public static void setTemporal(Context context, SubmissionInfo info, String value) throws SQLException {
		setTemporal(context, info.getSubmissionItem().getItem(), value);
	}

	/**
	 * Set the dublin core temporal coverage value for a DSpace item. Any existing
	 * value will first be deleted.
	 * 
	 * @param context DSpace context.
	 * @param item    DSpace item.
	 * @param value   The new temporal coverage value.
	 * @throws SQLException
	 */
	public static void setTemporal(Context context, Item item, String value) throws SQLException {
		setUnique(context, item, TEMPORAL_ELEMENT, TEMPORAL_QUALIFIER, value);
	}

	/**
	 * Set the dublin core alternative title value for a DSpace item.
	 * 
	 * @param context DSpace context.
	 * @param info    DSpace submission item.
	 * @param value   The new alternative title value.
	 * @throws SQLException
	 */
	public static void setTitleAlternative(Context context, SubmissionInfo info, String value) throws SQLException {
		ItemService itemService = ContentServiceFactory.getInstance().getItemService();
		itemService.addMetadata(context, info.getSubmissionItem().getItem(), MetadataSchema.DC_SCHEMA, TITLE_ELEMENT,
				TITLE_ALTERNATIVE_QUALIFIER, DEFAULT_LANG, value);
	}

	/**
	 * Set unique DSpace metadata item using a null qualifier and english language.
	 * Unique means if a value current exists it will be deleted and updated to be
	 * current value.
	 * 
	 * @param context DSpace context.
	 * @param item    DSpace item.
	 * @param element Metadata element.
	 * @param value   The new value.
	 * @throws SQLException
	 */
	public static void setUnique(Context context, Item item, String element, String value) throws SQLException {
		setUnique(context, item, element, null, DEFAULT_LANG, value);
	}

	/**
	 * Set unique DSpace metadata item using the default language.
	 * 
	 * @param item      DSpace item.
	 * @param element   Metadata element.
	 * @param qualifier Metadata qualifier.
	 * @param value     The new value.
	 * @throws SQLException
	 */
	public static void setUnique(Context context, Item item, String element, String qualifier, String value)
			throws SQLException {
		setUnique(context, item, element, qualifier, DEFAULT_LANG, value);
	}

	/**
	 * Set unique DSpace metadata item.
	 * 
	 * @param context   DSpace context.
	 * @param item      DSpace item.
	 * @param element   Metadata element.
	 * @param qualifier Metadata qualifier.
	 * @param lang      Metadata language.
	 * @param value     The new value.
	 * @throws SQLException
	 */
	public static void setUnique(Context context, Item item, String element, String qualifier, String lang,
			String value) throws SQLException {
		ItemService itemService = ContentServiceFactory.getInstance().getItemService();
		itemService.clearMetadata(context, item, MetadataSchema.DC_SCHEMA, element, qualifier, lang);

		if (value != null && value.length() > 0) {
			itemService.addMetadata(context, item, MetadataSchema.DC_SCHEMA, element, qualifier, lang, value);
		}
	}
}
