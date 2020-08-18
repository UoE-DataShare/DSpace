/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.submission.submit;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.content.Bitstream;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.core.Context;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

// DATASHARE specific class

/**
 * Build XML response for resumable upload.
 */
public class ResumableUploadGenerator extends AbstractGenerator
{
	private static final Logger log =
			Logger.getLogger(ResumableUploadGenerator.class);
	private Bitstream bitstream;
	private String error;
	private static final AttributesImpl emptyAttr = new AttributesImpl();
	private BitstreamService bitstreamService = ContentServiceFactory.getInstance()
			.getBitstreamService();

	/*
	 * (non-Javadoc)
	 * @see org.apache.cocoon.generation.AbstractGenerator#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
	 */
	public void setup(
			SourceResolver resolver,
			@SuppressWarnings("rawtypes") Map objectModel, 
			String src,
			Parameters par)  
					throws ProcessingException, SAXException, IOException 
	{
		super.setup(resolver, objectModel, src, par);
		try{
			String bId = par.getParameter("bitstream_id");
			log.debug("bId :" + bId);

			if(bId != null && bId.length() > 0){
				Context context = ContextUtil.obtainContext(objectModel);
				this.bitstream = bitstreamService.find(context, UUID.fromString(bId));
			}
			else
			{
				this.bitstream = null;
			}

			this.error = par.getParameter("error");
		}
		catch(ParameterException ex){}
		catch(SQLException ex){
			log.error(ex);
		}
	} 


	/**
	 * Generate XML response based on bitstream or error.
	 */
	@Override
	public void generate() throws IOException, SAXException, ProcessingException {
		contentHandler.startDocument();
		contentHandler.startElement("", "upload", "upload", new AttributesImpl());

		if(this.bitstream != null)
		{
			long size = 0;
			String format = "";
			try {
			   size = this.bitstream.getSizeBytes();
			   Context context = new Context();
			   format = this.bitstream.getFormatDescription(context);
			} catch(SQLException e) {
				log.warn("Unable to get information on bitstream: ", e);
			}

			log.debug("this.bitstream.getID().toString(): " + this.bitstream.getID().toString());
			this.addElement(contentHandler, "bitstreamId", this.bitstream.getID().toString());
			this.addElement(contentHandler, "size", Long.toString(size));
			this.addElement(contentHandler, "format", format);
			this.addElement(contentHandler, "checksum", this.bitstream.getChecksumAlgorithm() + ":" + this.bitstream.getChecksum());
			this.addElement(contentHandler, "sequenceId", String.valueOf(this.bitstream.getSequenceID()));
		}

		if(this.error != null && this.error.length() > 0){
			this.addElement(contentHandler, "errorkey", this.error);
		}

		contentHandler.endElement("", "upload", "upload");
		contentHandler.endDocument();
	}

	/**
	 * Element creation helper. 
	 * @param contentHandler
	 * @param name
	 * @param value
	 * @throws SAXException
	 */
	private void addElement(ContentHandler contentHandler, String name, String value) throws SAXException{
		contentHandler.startElement("", name, name, emptyAttr);
		contentHandler.characters(value.toCharArray(), 0, value.length());
		contentHandler.endElement("", name, name);
	}
}
