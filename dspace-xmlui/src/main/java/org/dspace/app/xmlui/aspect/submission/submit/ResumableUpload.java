/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.submission.submit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
//import org.apache.cocoon.servlet.multipart.PartOnDisk;
import org.dspace.app.xmlui.cocoon.servlet.multipart.DSpacePartOnDisk;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.log4j.Logger;
import org.dspace.app.util.SubmissionInfo;
import org.dspace.app.xmlui.aspect.submission.FlowUtils;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BundleService;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.BitstreamFormatService;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.curate.Curator;
import org.dspace.eperson.EPerson;

//DATASHARE specific class

/**
 * Action that responds to requests related to resumable upload.
 * 
 * .
 */
public class ResumableUpload extends AbstractAction
{
	private static final Logger log = Logger.getLogger(ResumableUpload.class);

	private static String tempDir;
	private String submissionDir;
	private String chunkDir;
	private String handle;

	private BitstreamFormatService bitstreamFormatService = ContentServiceFactory.getInstance()
			.getBitstreamFormatService();
	private BitstreamService bitstreamService = ContentServiceFactory.getInstance()
			.getBitstreamService();
	private BundleService bundleService = ContentServiceFactory.getInstance().getBundleService();
	private ItemService itemService = ContentServiceFactory.getInstance().getItemService();
	private WorkspaceItemService workspaceItemService = ContentServiceFactory.getInstance().getWorkspaceItemService();


	static
	{
		if (ConfigurationManager.getProperty("upload.temp.dir") != null)
		{
			tempDir = ConfigurationManager.getProperty("upload.temp.dir");
		}
		else
		{
			tempDir = System.getProperty("java.io.tmpdir");
		}
	}

	/**
	 * Set up directories for resumable upload.
	 * @param request
	 */
	private void init(Request request)
	{
		String sId = request.getParameter("submissionId");

		if(!sId.isEmpty()){
			// parent directory containing all bitstreams related to a submission
			this.submissionDir = tempDir + File.separator + request.getParameter("submissionId");

			// directory containing chunks of an individual bitstream
			this.chunkDir = this.submissionDir + File.separator + request.getParameter("resumableIdentifier");

			// create upload directories if required
			File uploadDir = new File(this.submissionDir);
			if(!uploadDir.exists())
			{
				uploadDir.mkdir();
			}

			if(!this.chunkDir.isEmpty())
			{ 
				File finalDir = new File(this.chunkDir);
				if (!finalDir.exists())
				{
					finalDir.mkdir();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.cocoon.acting.Action#act(org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Map act(
			Redirector redirector,
			SourceResolver resolver,
			Map objectModel,
			String source,
			Parameters parameters) throws Exception
	{
		Request request = ObjectModelHelper.getRequest(objectModel);
		this.init(request);

		this.handle = parameters.getParameter("handle", null);
		HashMap<String, String> returnValues = null;

		if(request.getMethod().equals("GET"))
		{
			log.debug("Processing GET");
			returnValues = new HashMap<String, String>();
			String complete = request.getParameter("complete");
			if(complete != null && Boolean.valueOf(complete))
			{
				log.debug("Processing GET complete");
				String bId = this.getCompletedBitstreamId(objectModel);    
				if(bId != null)
				{
					log.info("bitstream created " + bId + " for " +
							this.handle);
					returnValues.put("bitstream", bId);
				}
			}
			else{
				log.debug("Starting doGetResumable");
				doGetResumable(objectModel);
				log.debug("Finishing doGetResumable");
			}
		}
		else if(request.getMethod().equals("POST"))
		{
			log.debug("Processing POST");
			returnValues = new HashMap<String, String>();
			log.debug("Starting doPostResumable");
			doPostResumable(objectModel);
			log.debug("Finishing doPostResumable");
		}
		else if(request.getMethod().equals("DELETE"))
		{
			if(this.doDeleteResumable(objectModel))
			{
				returnValues = new HashMap<String, String>();
				returnValues.put("status", "200");
			}
		}

		return returnValues;
	}

	/**
	 * Resumable.js uses HTTP GET to recognize whether a specific part/chunk of
	 * a file was uploaded already. This method handles those requests.
	 * @param objectModel
	 * @throws IOException
	 */
	private void doGetResumable(@SuppressWarnings("rawtypes") Map objectModel) 
			throws IOException
	{
		Response response = ObjectModelHelper.getResponse(objectModel);
		Request request = ObjectModelHelper.getRequest(objectModel);

		int resumableTotalChunks =
				Integer.parseInt(request.getParameter("resumableTotalChunks").toString());
		int resumableChunkNumber =
				Integer.parseInt(request.getParameter("resumableChunkNumber"));
		long resumableCurrentChunkSize = 
				Long.valueOf(request.getParameter("resumableCurrentChunkSize"));

		String chunkPath;
		if(resumableTotalChunks == 1)
		{
			// there is only one chunk give it the name of the file
			chunkPath = this.submissionDir + File.separator +
					request.getParameter("resumableFilename").toString(); 
		}
		else
		{
			// use the String "part" and the chunkNumber as filename of a chunk
			chunkPath = this.chunkDir + File.separator + "part" +
					request.getParameter("resumableChunkNumber").toString();
		}

		File chunkFile = new File(chunkPath);
		// if the chunk was uploaded already, we send a status code of 200
		if(chunkFile.exists())
		{
			if(chunkFile.length() == resumableCurrentChunkSize)
			{
				response.setStatus(HttpServletResponse.SC_OK);

				if(resumableChunkNumber == resumableTotalChunks)
				{
					// its possible for the final request to be a GET request,
					// in this case process file
					log.debug("Got the final chunk start uploadComplete (Get version)");
					this.uploadComplete(objectModel, resumableTotalChunks);
					log.debug("Got the final chunk start uploadComplete (Get version)");
				}

			}
			else
			{
				// The chunk file does not have the expected size, delete it and 
				// pretend that it wasn't uploaded already.
				chunkFile.delete();
			}
		}
		else{
			// if we don't have the chunk send a http status code 204 No content
			response.sendError(HttpServletResponse.SC_NO_CONTENT);
		}
	}    

	/**
	 * Resumable.js sends chunks of files using HTTP POST. If a chunk was the
	 * last missing one, we have to assemble the file and return it. If other
	 * chunks are missing, we just return null.
	 * @param objectModel
	 * @throws FileSizeLimitExceededException
	 * @throws IOException
	 * @throws ServletException
	 */
	private void doPostResumable(@SuppressWarnings("rawtypes") Map objectModel)
			throws FileSizeLimitExceededException, IOException, ServletException 
	{
		Request request = ObjectModelHelper.getRequest(objectModel);
		Response response = ObjectModelHelper.getResponse(objectModel);

		int resumableTotalChunks =
				Integer.parseInt(request.getParameter("resumableTotalChunks"));
		int resumableChunkNumber =
				Integer.parseInt(request.getParameter("resumableChunkNumber"));
		String resumableFilename = request.getParameter("resumableFilename");
		String resumableIdentifier = request.getParameter("resumableIdentifier");

		// determine name of chunk
		String chunkPath;
		if(resumableTotalChunks == 1)
		{
			// there is only one chunk give it the name of the file
			chunkPath = this.chunkDir + File.separator + resumableFilename; 
		}
		else
		{
			chunkPath = this.chunkDir + File.separator + "part" +
					request.get("resumableChunkNumber").toString();
		}

		// cocoon will have uploaded the chunk automatically
		// now move it to temporary directory
		File chunkOrg = ((DSpacePartOnDisk)request.get("file")).getFile();
		File chunk = new File(chunkPath);

		log.debug("rename file " + chunkOrg + " to " + chunk);

		if(chunkOrg.renameTo(chunk))
		{
			if(resumableTotalChunks > 1)
			{ 
				if(resumableChunkNumber == resumableTotalChunks)
				{
					// we have the final chunk process upload
					log.debug("Got the final chunk start uploadComplete (Post version)");
					this.uploadComplete(objectModel, resumableTotalChunks);
					log.debug("Got the final chunk end uploadComplete (Post version)");
				}
			}
			else
			{
				log.debug(chunkPath + " Uploaded");

				try
				{
					// chunk is the whole file
					this.processFile(
							ContextUtil.obtainContext(objectModel).getCurrentUser(),
							Integer.parseInt(request.getParameter("submissionId")),
							request.getSession(),
							chunk,
							resumableIdentifier);
				}
				catch(SQLException ex)
				{
					throw new RuntimeException(ex);
				}
			}
		}
		else
		{
			String msg = "Failed to rename file " + chunkOrg + " to " + chunk;
			log.error(msg);

			// this should signal a retry
			response.sendError(HttpServletResponse.SC_NO_CONTENT);
		}
	}    

	/**
	 * Delete previously uploaded bitstream
	 * @param objectModel
	 * @return True if bitstream successfully deleted.
	 */
	private boolean doDeleteResumable(
			@SuppressWarnings("rawtypes") Map objectModel)
	{
		boolean success = false;


		Request request = ObjectModelHelper.getRequest(objectModel);

		try
		{
			Context context = ContextUtil.obtainContext(objectModel);

			// DATASHARE - start
			String sBid = request.getParameter("bitstreamId");

			log.debug("sBid: " + sBid);
			// DATASHARE - end

			if(sBid != null && sBid.length() > 0)
			{
				// delete bitstream from dspace item
				Bitstream bitstream = bitstreamService.findByIdOrLegacyId(context, sBid);

				if(bitstream != null)
				{
					log.debug("delete " + bitstream.getID());

					// remove bitstream from bundle..
					// delete bundle if it's now empty
					List<Bundle> bundles = bitstream.getBundles();
					log.debug("bundles.size(): " + bundles.size());


					if(bitstream.getBundles().size() > 0) {
						Bundle bundle = bundles.get(0);
						bundle.removeBitstream(bitstream);
						bitstream.getBundles().remove(bundle);
 
						List<Bitstream> bitstreams = bundle.getBitstreams();
						
						log.debug("bitstreams.size(): " + bitstreams.size());
						
						// REMOVED THIS FROM DSPACE 5 CODE
						// HOPEFULLY NOT FATAL, AS IT DID NOT WORK HERE
						
						// remove bundle if it's now empty
//						if(bitstreams.size() < 1)
//						{
//							SubmissionInfo si = FlowUtils.obtainSubmissionInfo(
//									objectModel, 'S' + request.getParameter("submissionId"));
//							Item item = si.getSubmissionItem().getItem();
//
//							itemService.removeBundle(context, item, bundle);
//							itemService.update(context, item);
//						}
					}
					success = true;
				}
				else
				{
					log.error("Bitstream " + sBid + " not found. Can't delete");
				}
			}
			else
			{
				// delete incomplete upload from disk
				if(!deleteDirectory(new File(this.chunkDir)))
				{
					log.warn("Could not delete incomplete upload: " + chunkDir);
				}
				else{
					log.debug(chunkDir + " deleted");
					success = true;
				}
			}
		}
		catch(NumberFormatException nfe)
		{
			log.error("Invalid bitstream id " +
					request.getParameter("bitstreamId") + " Can't delete");
		}
		catch(SQLException ex)
		{
			log.error(ex);
		}
//		catch(AuthorizeException ex)
//		{
//			log.error(ex);
//		}
//		catch(IOException ex){
//			log.error(ex);
//		}
		catch(Exception ex){
			log.error(ex);
		}

		log.debug("success: " + success);
		return success;
	}

	/**
	 * Complete resumable upload.
	 * @param objectModel
	 * @return
	 */
	private String getCompletedBitstreamId(
			@SuppressWarnings("rawtypes") Map objectModel)
	{
		String id = null;

		Request request = ObjectModelHelper.getRequest(objectModel);

		// get upload details from session
		String resumableIdentifier = request.getParameter("resumableIdentifier");
		Object obj = request.getSession().getAttribute(resumableIdentifier);

		if(obj != null){
			id = obj.toString();
		}
		else
		{
			log.warn("No attribute found in session for " + resumableIdentifier);
		}

		return id;
	}

	/**
	 * Create DSpace bitstream from file.
	 * @param context DSpace context.
	 * @param file The bitstream on file system.
	 * @param item DSpace item.
	 * @return new DSpace bitstream.
	 */
	private Bitstream createBitstream(Context context, File file, Item item)
	{
		Bitstream b = null; 
		log.debug("In createBitstream");
		try
		{
			FileInputStream fis = new FileInputStream(file);

			// do we already have a bundle?
			List<Bundle> bundles = itemService.getBundles(item, "ORIGINAL");

			if (bundles.size() < 1)
			{
				log.debug("We don't already have a bundle");
				// set bundle's name to ORIGINAL
				log.debug("About to createSingleBitstreeam");
				b = itemService.createSingleBitstream(context, fis, item, "ORIGINAL");
				log.debug("Leaving createSingleBitstreeam");
			}
			else
			{
				// we have a bundle already, just add bitstream
				log.debug("We do already have a bundle");
				log.debug("About to create Bitstreeam");
				b = bitstreamService.create(context, fis);
				log.debug("About add Bitstreeam");
				bundleService.addBitstream(context, bundles.get(0), b);
				log.debug("About to update bundle");
				bundleService.update(context, bundles.get(0));

			}

			b.setName(context, file.getName()); 
			b.setSource(context, file.getAbsolutePath());

			// identify the format
			log.debug("About to set the format");
			b.setFormat(context, bitstreamFormatService.guessFormat(context, b));
			log.debug("Finished setting the format");

			log.debug("Update bitstream");
			bitstreamService.update(context, b);
			log.debug("Update item");
			itemService.update(context, item);
		}
		catch(FileNotFoundException ex)
		{
			log.error("Can't find file " + file + ": " + ex);
		}
		catch(SQLException ex)
		{
			log.error(ex);
		}
		catch(AuthorizeException ex)
		{
			log.error(ex);
		}
		catch(IOException ex){
			log.error(ex);
		}

		log.debug("Leaving createBitstream");
		return b;
	}


	/**
	 * Delete directory and children from file system.
	 * @param path
	 * @return True is successful.
	 */
	private boolean deleteDirectory(File path) 
	{
		if (path.exists()) 
		{
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) 
			{
				if (files[i].isDirectory()) 
				{
					deleteDirectory(files[i]);
				} 
				else 
				{
					files[i].delete();
				}
			}
		}

		return (path.delete());
	}

	/**
	 * Do some post processing on reassembled / uploaded file.
	 * @param user DSpace EPerson.
	 * @param submissionId DSpace submission identifier.
	 * @param session User session
	 * @param file The reassembled/uploaded file
	 * @param resumableIdentifier Unique identifier of resumable upload.
	 */
	private void processFile(
			EPerson user,
			int submissionId,
			HttpSession session,
			File file,
			String resumableIdentifier)
	{
		log.debug("In process file");
		int status = Curator.CURATE_SUCCESS;
		String scan = ConfigurationManager.getProperty("submission-curation", "virus-scan");

		// Virus scan if scan not set to false
		if(!scan.equalsIgnoreCase("false"))
		{
			status = ResumableUpload.this.virusCheck(file);
			log.info("Virus check " + file + ", status is " + status);

			if(status == Curator.CURATE_ERROR)
			{
				String msg = "Problem with virus checker"; 
				session.setAttribute(resumableIdentifier, new VirusCheckException(msg));
				log.error(msg);
				return;
			}
			else if (status == Curator.CURATE_FAIL)
			{
				String msg = file + " failed virus check";
				session.setAttribute(resumableIdentifier, new VirusCheckException(msg));
				log.warn(msg);
				return;
			}
		}

		// Virus check good, attach file to DSpace item
		try
		{
			// create new context as this can be executed outside of a request
			Context ctx = new Context();
			ctx.setCurrentUser(user);

			log.info("Create bitstream for user " + user.getEmail() + " submissionId: " + submissionId);

			// find item
			InProgressSubmission ips = workspaceItemService.find(ctx, submissionId);
			Item item = ips.getItem();

			log.info("Create bitstream on item " + item.getID());

			// create new bitstream
			Bitstream b = this.createBitstream(ctx, file, item);

			// delete file and upload
			log.info("deleting " + this.chunkDir);
			if(!deleteDirectory(new File(this.chunkDir)))
			{
				log.warn("Couldn't delete submission upload path " + this.submissionDir + ", ignoring it.");
			}

			ctx.complete();

			// all good, store bitstream id in session
			session.setAttribute(resumableIdentifier, b.getID());
		}
		catch(SQLException ex)
		{
			log.error("Problem with bitstream creation: " + ex.getMessage());
			throw new RuntimeException(ex);
		}
		log.debug("Leaving process file");

	}

	/**
	 * All the chunks have been uploaded, kick off file re-assembly
	 * @param objectModel
	 * @param resumableTotalChunks Total number of chunks uploaded.
	 */
	private void uploadComplete(
			@SuppressWarnings("rawtypes") Map objectModel,
			int resumableTotalChunks)
	{
		Request request = ObjectModelHelper.getRequest(objectModel);

		// check chunk count is correct
		int noOfChunks = new File(this.chunkDir).listFiles().length;
		log.debug("NoOfChunks :'" + noOfChunks + "'");
		log.debug("Expected noOfChunks :'" +resumableTotalChunks + "'");
		if(noOfChunks >= resumableTotalChunks)
		{
			String resumableFilename = request.getParameter("resumableFilename");
			log.debug("ResumableFilename :'" + resumableFilename + "'");
			String resumableIdentifier = request.getParameter("resumableIdentifier");
			log.debug("ResumableIdentifier :'" + resumableIdentifier + "'");

			log.info("Upload of " + resumableIdentifier + " complete. Start file reassembly");

			try
			{
				// recreate file in a new thread
				log.debug("Start to reassemble in new thread");
				new FileReassembler(
						ContextUtil.obtainContext(objectModel),
						Integer.parseInt(request.getParameter("submissionId")),
						request.getSession(),
						resumableFilename,
						resumableIdentifier,
						resumableTotalChunks).start();
			}
			catch(SQLException ex)
			{
				throw new RuntimeException(ex);
			}
		}
		else
		{
			String msg = "something has gone wrong with " + this.handle +
					", we have received last chunk but some are missing: expected " +
					resumableTotalChunks + " , have: " + noOfChunks;
			log.error(msg);
			throw new RuntimeException(msg);
		}           
	}

	/**
	 * Is file virus free? Check using clamdscan process.
	 * @param file The file to check for virus.
	 * @return Curator.CURATE_SUCCESS if file is virus free?
	 */
	private int virusCheck(File file)
	{ 
		int bstatus = Curator.CURATE_FAIL;

		final Pattern PATTERN = Pattern.compile(".*Infected files: 0.*");
		BufferedReader stdInput = null;

		try
		{
			log.debug("file.getPath(): " + file.getPath());
			// spawn clamscan process and wait for result
			Process p = Runtime.getRuntime().exec(
					new String[] {"clamdscan", file.getPath()});

			p.waitFor();
			int retVal = p.exitValue();
			if(retVal == 0)
			{
				// good status returned check if pattern is in output 
				stdInput = new BufferedReader(
						new InputStreamReader(p.getInputStream()));

				String s = null;
				while((s = stdInput.readLine()) != null)
				{
					Matcher matchHandle = PATTERN.matcher(s);
					if(matchHandle.find())
					{
						bstatus = Curator.CURATE_SUCCESS;
						break;
					}
				}
			}
			else
			{
				bstatus = Curator.CURATE_ERROR;
			}
		}
		catch(InterruptedException ex){log.warn(ex);}
		catch(IOException ex){log.warn(ex);}
		finally
		{
			if(stdInput != null)
			{
				try{stdInput.close();} catch (Exception e){log.warn(e);}
			}
		}

		if(bstatus != Curator.CURATE_SUCCESS)
		{
			log.warn("*** File " + file + " has failed virus check. status = " + bstatus);
		}

		return bstatus;
	}

	/**
	 * Reassemble file from individually uploaded chunks. The file will also be virus checked.
	 */
	public class FileReassembler extends Thread
	{
		private EPerson user;
		private int submissionId;
		private String resumableFilename;
		private String resumableIdentifier;
		private int resumableTotalChunks;
		private HttpSession session;

		/**
		 * @param context DSpace context.
		 * @param submissionId DSpace submission identifier.
		 * @param session HTTPS session.
		 * @param resumableFilename Name of the file uploaded.
		 * @param resumableIdentifier Unique identifier of resumable upload.
		 * @param resumableTotalChunks Total number of chunks uploaded.
		 */
		public FileReassembler(
				Context context,
				int submissionId,
				HttpSession session,
				String resumableFilename,
				String resumableIdentifier,
				int resumableTotalChunks)
		{
			log.debug("In FileReassembler constructor");
			this.submissionId = submissionId;
			this.resumableFilename = resumableFilename;
			this.resumableIdentifier = resumableIdentifier;
			this.resumableTotalChunks = resumableTotalChunks;
			this.session = session;
			this.user = context.getCurrentUser();
			log.debug("User is '" + this.user.getEmail() + "'");
			log.debug("Leaving FileReassembler constructor");
		}

		/**
		 * Assemble a file from its chunks using known number of chunks.
		 * @return newly assembled file
		 * @throws IOException
		 */
		private File makeFileFromChunks() throws IOException
		{
			log.debug("In FileReassembler makeFileFromChunks");
			String chunkPath = ResumableUpload.this.chunkDir + File.separator + "part";
			File destFile = null;

			String destFilePath = ResumableUpload.this.chunkDir + File.separator +
					this.resumableFilename;
			destFile = new File(destFilePath);
			OutputStream os = null;

			try {
				destFile.createNewFile();
				os = new FileOutputStream(destFile);

				for (int i = 1; i <= this.resumableTotalChunks; i++) 
				{
					File fi = new File(chunkPath.concat(Integer.toString(i)));
					try 
					{
						InputStream is = new FileInputStream(fi);

						byte[] buffer = new byte[1024];

						int length;

						while ((length = is.read(buffer)) > 0) 
						{
							os.write(buffer, 0, length);
						}

						try 
						{
							is.close();
						} 
						catch (IOException ex){}
					}
					catch (IOException e) 
					{
						log.warn(e);
						// try to delete destination file,
						// as we got an exception while writing it.
						if(!destFile.delete())
						{
							log.warn("While writing an uploaded file an error occurred. "
									+ "We were unable to delete the damaged file: " 
									+ destFile.getAbsolutePath() + ".");
						}

						// throw IOException to handle it in the calling method
						throw e;
					}
				}
			} 
			finally 
			{
				try 
				{
					if (os != null) 
					{
						os.close();
					}
				} 
				catch (IOException ex){}
			}

			log.debug("Leaving FileReassembler makeFileFromChunks");
			return destFile;
		}        

		public void run() {
			try
			{
				log.debug("In FileReassembler run");
				// recreate file from chunks and process file
				ResumableUpload.this.processFile(
						this.user,
						this.submissionId,
						this.session,
						this.makeFileFromChunks(),
						this.resumableIdentifier);
				log.debug("Leaving FileReassembler run");
			}
			catch(IOException ex)
			{
				log.error(ex);
			}
		}
	}

	public class VirusCheckException extends RuntimeException
	{
		private static final long serialVersionUID = 29162294005697162L;

		public VirusCheckException(String message)
		{
			super(message);
		}
	}
}