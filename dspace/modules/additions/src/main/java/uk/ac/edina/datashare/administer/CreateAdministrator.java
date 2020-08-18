package uk.ac.edina.datashare.administer;

import org.apache.log4j.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.factory.DatashareContentServiceFactory;
import org.dspace.content.service.UUN2EmailService;
import org.dspace.core.Context;

/**
 * DataShare specific CreateAdministrator.
 */
public class CreateAdministrator {
	
	/** log4j category */
    private static Logger LOG = Logger.getLogger(CreateAdministrator.class);
	/**
	 * For invoking via the command line
	 * 
	 * @param argv command-line arguments
	 */
	public static void main(String[] argv) {
		CommandLineParser parser = new PosixParser();
		Options options = new Options();

		// get mandatory args -e and -u
		options.addOption("u", "uun", true, "University User Name");
		options.addOption("e", "email", true, "Email address");
		LOG.info("Adding uun to uun2email table");
		LOG.info("argv: " + argv);
		try {
			CommandLine line = parser.parse(options, argv);

			if (line.hasOption('u') && line.hasOption('e')) {
				// got values - update database with UUN/email values
				Context context = new Context();
				UUN2EmailService datasetService = DatashareContentServiceFactory.getInstance().getUUN2EmailService();
				datasetService.insertUUNEntry(context, line.getOptionValue('u'), line.getOptionValue('e'));

				context.complete();
				
				LOG.info("Added uun to uun2email table");

				System.exit(0);
			} else {
				// mandatory fields not found
				LOG.info("Incorrect sytax. Syntax: -u <UUN> -e <email>");
				System.out.println("Syntax: -u <UUN> -e <email>");
				System.exit(1);
			}
		} catch (Exception ex) {
			LOG.error("Problem parsing command args " + ex.getMessage());
			System.err.println("Problem parsing command args " + ex.getMessage());
		}
	}
}
