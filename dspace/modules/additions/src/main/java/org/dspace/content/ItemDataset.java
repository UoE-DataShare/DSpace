package org.dspace.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.atlas.logging.Log;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.factory.DatashareContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.DatasetService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.servicemanager.DSpaceKernelImpl;
import org.dspace.servicemanager.DSpaceKernelInit;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.storage.bitstore.factory.StorageServiceFactory;
import org.dspace.storage.bitstore.service.BitstreamStorageService;

import uk.ac.edina.datashare.utils.DSpaceUtils;
import uk.ac.edina.datashare.utils.MetaDataUtil;

/**
 * DataShare item dataset. That is a zip file that contains all item bitstreams.
 */
public class ItemDataset  {
	private static final Logger LOG = Logger.getLogger(ItemDataset.class);
	private Context context = null;
	private Item item = null;
	private String handle = null;
	private static final String TMP_FILE_NAME_EXT = ".tmp";
	private static final String DIR_PROP = "datasets.path";
	private static String dir = null;

	/**
	 * Initialise dataset with DSpace context and item.
	 * 
	 * @param context
	 * @param item
	 */
	public ItemDataset(Context context, Item item) {
		this.context = context;
		this.item = item;
		this.init();
	}

	/**
	 * Initialise dataset with DSpace context and item handle.
	 * 
	 * @param context
	 * @param handle
	 */
	public ItemDataset(Context context, String handle) {
		this.context = context;
		this.handle = handle;
		this.init();
	}

	/**
	 * Initialise dataset with DSpace context and dataset zip file.
	 * 
	 * @param context
	 * @param ds
	 */
	public ItemDataset(Context context, File ds) {
		this.context = context;
		this.setHandle(ds);
		this.init();
	}

	public ItemDataset(Item item) {
		this.item = item;
		this.init();
	}

	public ItemDataset(String handle) {
		this.handle = handle;
		this.init();
	}

	public ItemDataset(Context context, Bitstream bitstream) {
		try {
			BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
			DSpaceObject ob = bitstreamService.getParentObject(context, bitstream);
			if (ob instanceof Item) {
				this.context = context;
				this.item = (Item) ob;
				this.init();
			} else {
				throw new RuntimeException("Only items can be datasets.");
			}
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Initialise dataset.
	 */
	private void init() {
		dir = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(DIR_PROP);
		LOG.info("init()- dir: " + dir);
		if (dir == null) {
			throw new RuntimeException(DIR_PROP + " needs to be defined");
		}

		if (!new File(dir).exists()) {
			throw new RuntimeException(dir + " doesn't exist");
		}

	}

	/**
	 * Check if item has been put under embargo or tombstoned. If so, delete
	 * dataset.
	 */
	public void checkDataset() {
		if (this.exists()) {
			if (DSpaceUtils.hasEmbargo(this.context, this.item) || isTombstoned(this.context, item)) {
				LOG.info("Delete dataset for " + item.getHandle());
				this.delete();
			}
		} else {
			LOG.warn("No dataset exists to check " + this.item.getHandle());
		}
	}

	/**
	 * Create dataset zip file.
	 * 
	 * @return Thread that dataset is created on.
	 */
	public Thread createDataset() {
		Thread th = new Thread(new DatasetZip());
		th.start();
		return th;
	}

	/**
	 * Delete dataset from system.
	 */
	public void delete() {
		File zip = null;
		if (this.item != null) {
			zip = new File(this.getFullPath());
		} else {
			zip = new File(dir + File.separator + ItemDataset.getFileName(this.handle));
		}

		if (!zip.delete()) {
			LOG.warn("Problem deleting " + zip);
		} else {
			String fp = zip.toString();
			String fname = fp.substring(fp.lastIndexOf('/') + 1);
			DatasetService datasetService = DatashareContentServiceFactory.getInstance().getDatasetService();
			try {
				datasetService.deleteDataset(context, fname);
			} catch (SQLException e) {
				LOG.warn("Problem deleting " + fname);
			}
		}
	}

	public boolean exists() {
		LOG.info("getFullPath(): " + getFullPath());

		return new File(getFullPath()).exists();
	}

	public static boolean exists(String handle) {
		return new File(dir + getFileName(handle)).exists();
	}

	/**
	 * @param bitstream The bitstream to check.
	 * @return True is bitstream should be added to the zip file.
	 */
	private boolean includeBitstream(Context context, Bitstream bitstream) {
		final String WBFF = "Written by FormatFilter";
		return (bitstream.getSource() != null && !bitstream.getSource().startsWith(WBFF)
				&& !bitstream.getName().equals(Constants.LICENSE_BITSTREAM_NAME));
	}

	public String getChecksum() throws SQLException {
		DatasetService datasetService = DatashareContentServiceFactory.getInstance().getDatasetService();
		return datasetService.fetchDatasetChecksum(context, item);
	}

	private String getFileName() {
		return ItemDataset.getFileName(this.item.getHandle());
	}

	private String getHandle() {
		return this.handle;
	}

	public static String getFileName(String handle) {
		String aHandle[] = handle.split("/");
		return "DS_" + aHandle[0] + "_" + aHandle[1] + ".zip";
	}

	private String getFullPath() {
		return dir + File.separator + getFileName();
	}

	/**
	 * @return size in bytes of dataset zip file.
	 */
	public long getSize() {
		return new File(getFullPath()).length();
	}

	/**
	 * @return Temporary dataset file name.
	 */
	public String getTmpFileName() {
		return getFullPath() + TMP_FILE_NAME_EXT;
	}

	/**
	 * @return size in bytes of dataset tmp zip file.
	 */
	public long getTmpSize() {
		return new File(getTmpFileName()).length();
	}

	public URL getURL() {
		URL url;
		try {
			String bUrl[] = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("dspace.baseUrl").split("://");
			String protocol = bUrl[0];
			String host = bUrl[1];
			String fPath = "/download/" + getFileName();
			if (host.contains(":")) {
				String aHost[] = host.split(":");
				host = aHost[0];
				url = new URL(protocol, host, Integer.parseInt(aHost[1]), fPath);
			} else {
				url = new URL(protocol, host, fPath);
			}

		} catch (MalformedURLException ex) {
			throw new RuntimeException(ex);
		}

		return url;
	}

	private boolean itemIsAvailable(Context context, Item item) {
		LOG.info("hasEmbargo: " + DSpaceUtils.hasEmbargo(context, item));
		LOG.info("isWithdrawn: " + item.isWithdrawn());
		LOG.info("showTombstone: " + isTombstoned(context, item));
		return !DSpaceUtils.hasEmbargo(context, item) && !item.isWithdrawn()
				&& !isTombstoned(context, item);
	}

	/**
	 * Create a monitor on dataset creation, to track progress.
	 * 
	 * @return Thread that monitor is created on.
	 */
	public Thread monitorDataset() {
		Thread th = new Thread(new DatasetMonitor());
		th.start();
		return th;
	}

	/**
	 * Given a dataset file object, set handle.
	 */
	private void setHandle(File ds) {
		Pattern p = Pattern.compile(".+DS_(\\d+)_(\\d+).zip");
		Matcher matcher = p.matcher(ds.getAbsolutePath());
		while (matcher.find()) {
			this.handle = matcher.group(1) + "/" + matcher.group(2);
		}
	}

	/**
	 * Create dataset zip file in a seperate thread.
	 */
	private class DatasetZip implements Runnable {
		/**
		 * Start thread.
		 */
		public void run() {
			Context context = null;
			try {
				context = new Context();

				ItemService itemService = ContentServiceFactory.getInstance().getItemService();

				if (itemIsAvailable(context, item)) {
					LOG.info("create zip for " + item.getHandle());
					createZip(context);
					String cksum = createChecksum(context);

					LOG.info("zip complete");
					DatasetService datasetService = DatashareContentServiceFactory.getInstance().getDatasetService();
					datasetService.insertDataset(context, item.getID(), getFileName(), cksum);
				} else {
					ItemDataset.LOG.warn("Zip creation for " + item.getHandle() + " not allowed.");
				}
			} catch (SQLException | AuthorizeException ex) {
				LOG.error(ex);
				throw new RuntimeException(ex);
			} finally {
				try {
					context.complete();
				} catch (SQLException ex) {
					LOG.warn(ex);
				}
			}
		}

		private String createChecksum(Context context) {
			String cksum = null;
			try {
				FileInputStream fis = new FileInputStream(new File(getFullPath()));
				cksum = DigestUtils.md5Hex(fis);
				fis.close();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}

			return cksum;
		}

		private void createZip(Context context) {
			String tmpZip = getTmpFileName();

			try {
				final byte[] BUFFER = new byte[8192];

				FileOutputStream fos = new FileOutputStream(tmpZip);
				ZipOutputStream zos = new ZipOutputStream(fos);
				zos.setLevel(0);

				ItemService itemService = ContentServiceFactory.getInstance().getItemService();
				BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
				BitstreamStorageService bitstreamStorageService = StorageServiceFactory.getInstance().getBitstreamStorageService();


				List<Bundle> bundle = itemService.getBundles(item, "ORIGINAL");

				LOG.info("bundle.size(): " + bundle.size());
				// loop round bundles, there should be two - files and licences
				for (int i = 0; i < bundle.size(); i++) {
					// now get the actual bitstreams
					List<Bitstream> bitstreams = bundle.get(i).getBitstreams();

					for (int j = 0; j < bitstreams.size(); j++) {
						// only add bitstream if valid
						if (includeBitstream(context, bitstreams.get(j))) {
							LOG.info("do " + bitstreams.get(j).getName());
							ZipEntry entry = new ZipEntry(bitstreams.get(j).getName());
							LOG.info("ZipEntry entry " + entry);
							zos.putNextEntry(entry);
							InputStream in = bitstreamStorageService.retrieve(context, bitstreams.get(j));
							LOG.info("InputStream in " + in);
							int length = -1;
							while ((length = in.read(BUFFER)) > -1) {
								zos.write(BUFFER, 0, length);
							}

							zos.closeEntry();
							in.close();
						}
					}
				}

				// Get License
				List<Bundle> licenseBundle = itemService.getBundles(item, "CC-LICENSE");
				LOG.info("licenseBundle.size(): " + licenseBundle.size());

				for (int i = 0; i < licenseBundle.size(); i++) {
					// now get the actual bitstreams
					List<Bitstream> bitstreams = licenseBundle.get(i).getBitstreams();
					for (int j = 0; j < bitstreams.size(); j++) {

						LOG.info("do " + bitstreams.get(j).getName());
						ZipEntry entry = new ZipEntry(bitstreams.get(j).getName());
						LOG.info("ZipEntry entry " + entry);
						zos.putNextEntry(entry);
						InputStream in = bitstreamStorageService.retrieve(context, bitstreams.get(j));
						LOG.info("InputStream in " + in);
						int length = -1;
						while ((length = in.read(BUFFER)) > -1) {
							zos.write(BUFFER, 0, length);
						}

						zos.closeEntry();
						in.close();

					}
				}


				zos.close();
				fos.close();

				// rename zip with temporary file to final name
				if (!new File(tmpZip).renameTo(new File(getFullPath()))) {
					LOG.error("Problem renaming " + tmpZip + " to " + getFullPath());
				}
				LOG.info(getFileName() + " complete");
			} catch (SQLException ex) {
				LOG.error(ex);
				throw new RuntimeException(ex);
			} catch (FileNotFoundException ex) {
				LOG.error(ex);
				throw new RuntimeException(ex);
			} catch (IOException ex) {
				final String msg = "Problem with " + tmpZip + ": " + ex.getMessage();
				LOG.info(msg);
				LOG.error(msg);
				throw new RuntimeException(msg);
			} catch (Exception ex) {
				LOG.error(ex);
				throw new RuntimeException(ex);
			}
		}
	}

	/**
	 * This will monitor the progress of a creation of a dataset printing out its
	 * size.
	 */
	private class DatasetMonitor implements Runnable {
		public void run() {
			boolean cont = true;
			int sleep = 5000;
			LOG.info("Checking dataset " + item.getHandle() + " ...");
			while (cont) {
				if (exists()) {
					LOG.info("dataset exists");
					cont = false;
				} else if (!itemIsAvailable(context, item)) {
					LOG.info("dataset creation not allowed");
					cont = false;
				} else {
					try {
						Thread.sleep(sleep);
						LOG.info("size: " + getTmpSize());
					} catch (InterruptedException ex) {
						LOG.info(ex);
					}
				}
			}
		}
	}

	// Adaptation of method in DSpaceUtil (which was not working) in this class
	private static boolean isTombstoned(Context context, Item item) {
		boolean show = false;
		try {
			String tomb = MetaDataUtil.getShowTombstone(item);
			if (tomb != null) {
				show = Boolean.parseBoolean(tomb);
			}

		} catch (Exception ex) {
			throw new RuntimeException("Problem determining access right", ex);
		}

		LOG.info("isTombstoned(): " + show);
		return show;
	}

	/**
	 * Process all datasets in the system.
	 */
	public static void main(String[] args) {
		Context context = null;
		try {
			LOG.info("*** Before context: ");
			context = new Context();
			LOG.info("*** context: " + context);

			ItemService itemService = ContentServiceFactory.getInstance().getItemService();
			LOG.info("*** itemService: " + itemService);

			List<String> itemHandles = new ArrayList<String>(10000);
			Iterator<Item> iter = itemService.findAll(context);
			LOG.info("*** iter: " + iter);

			while (iter.hasNext()) {
				Item item = iter.next();
				LOG.info("*** item: " + item);
				if (item.isArchived()) {
					String handle = item.getHandle();
					LOG.info("*** handle: " + handle);

					if (handle == null) {
						LOG.info("*** Item with id " + item.getID() + " has no handle");
						continue;
					}
					itemHandles.add(item.getHandle());
					ItemDataset ds = new ItemDataset(context, item);
					if (ds.exists()) {
						if (isTombstoned(context, item)) {
							LOG.info("Delete tombstoned dataset: " + item.getHandle());
							ds.delete();
						} else {
							LOG.info("Dataset already exists " + item.getHandle());
						}
					} else {
						if (ds.itemIsAvailable(context, item)) {
							LOG.info("Create dataset for " + ds.getFullPath() + " for " + item.getHandle()
							+ ", id: " + item.getID());
							Thread th = ds.createDataset();
							try {
								th.join();
							} catch (InterruptedException ex) {
								LOG.info(ex);
							}
						} else {
							LOG.info("Item is currently unavailable: " + item.getHandle());
						}
					}
				}
			}

			// now see if any datasets are orphaned, just in case
			LOG.info("*** dir: " + dir);
			File datasets[] = new File(dir).listFiles();
			LOG.info("*** datasets: " + datasets);

			for (File zip : datasets) {
				if (zip.getName().endsWith(TMP_FILE_NAME_EXT)) {
					// if file is a temporary file delete it if more than one day old
					long diff = new Date().getTime() - zip.lastModified();
					if (diff > 24 * 60 * 60 * 1000) {
						zip.delete();
					}
				} else if (!zip.getName().equals("README.txt")) {
					ItemDataset ds = new ItemDataset(context, zip);
					if (!itemHandles.contains(ds.getHandle())) {
						LOG.info("*** dataset " + zip + " exists with no item. Delete it.");
						ds.delete();
					}
				}
			}

		} catch (SQLException ex) {
			LOG.info(ex);
		} catch (Exception e) {
			LOG.info(e);
			throw e;

		} finally {
			try {
				context.complete();
			} catch (SQLException ex) {
			} catch (Exception e) {
				LOG.info(e);
				throw e;

			}
		}

		LOG.info("exit");
	}

}
