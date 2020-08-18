package uk.ac.edina.datashare.events;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.ItemDataset;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Consumer;
import org.dspace.event.Event;

import uk.ac.edina.datashare.utils.DSpaceUtils;
import uk.ac.edina.datashare.utils.MetaDataUtil;

/**
 * This listens for events raised by DSpace for DataShare.
 */
public class DataShareConsumer implements Consumer {
	private static final Logger LOG = Logger.getLogger(DataShareConsumer.class);
	private ConsumerEvent event = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dspace.event.Consumer#consume(org.dspace.core.Context,
	 * org.dspace.event.Event)
	 */
	public void consume(Context context, Event event) throws Exception {
		if (this.event == null && event.getSubjectType() == Constants.COLLECTION) {
			switch (event.getEventType()) {
			case Event.ADD: {
				DSpaceObject dso = event.getObject(context);
				if (dso instanceof Item) {
					Item item = (Item) dso;
					if (item.isArchived()) {
						// if a new item has been created and archived,
						// mark item for cleaning up
						this.event = new ConsumerEvent(item, event.getEventType());
					}
				}
				break;
			}
			case Event.REMOVE: {
				this.event = new ConsumerEvent(
						// event detail is the item handle
						event.getDetail(), event.getEventType());
				break;
			}
			default: {
				LOG.info("Unkown subject type: " + event.getSubjectType());
			}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dspace.event.Consumer#end(org.dspace.core.Context)
	 */
	public void end(Context context) throws Exception {
		if (this.event != null) {
			switch (this.event.getType()) {
			case Event.ADD: {
				this.addItem(context, this.event.getItem());
				break;
			}
			case Event.REMOVE: {
				new ItemDataset(context, this.event.getHandle()).delete();
				break;
			}
			default: {
				LOG.info("Unknown event type: " + this.event.getType());
			}
			}
			this.event = null;
		}
	}

	private void addItem(Context context, Item item) {
		try {
			// clear field used to store license type
			DSpaceUtils.clearUserLicenseType(context, item);

			// copy hijacked spatial country to dc.coverage.spatial
			List<MetadataValue> vals = DSpaceUtils.getHijackedSpatial(item);
			for (int i = 0; i < vals.size(); i++) {
				MetaDataUtil.setSpatial(context, item, vals.get(i).getValue(), false);
			}

			// clear hijacked spatial field
			DSpaceUtils.clearHijackedSpatial(context, item);

			LOG.info("DataShareConsumer: create dataset");

			// create zip file
			new ItemDataset(item).createDataset();

			context.turnOffAuthorisationSystem();

			// commit changes
			ItemService itemService = ContentServiceFactory.getInstance().getItemService();
			itemService.update(context, item);
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		} catch (AuthorizeException ex) {
			throw new RuntimeException(ex);
		} finally {
			context.restoreAuthSystemState();
		}
	}

	// not used
	public void finish(Context ctx) throws Exception {
	}

	public void initialize() throws Exception {
	}

	private class ConsumerEvent {
		private Item item;
		private String handle;
		private int type;

		public ConsumerEvent(Item item, int type) {
			this.item = item;
			this.type = type;
		}

		public ConsumerEvent(String handle, int type) {
			this.handle = handle;
			this.type = type;
		}

		public Item getItem() {
			return item;
		}

		public String getHandle() {
			return handle;
		}

		public int getType() {
			return type;
		}
	}
}
