package edu.deu.resumeie;

import edu.deu.resumeie.shared.SharedObjects;
import edu.deu.resumeie.training.nlp.TrainingDriver;
import org.apache.commons.cli.*;

import java.util.Locale;

public class App {

    public static void main(String[] args) {
        // Set system (current VM) wide locale to Turkish Locale
        Locale.setDefault(Locale.forLanguageTag("TR"));

        parseAndStart(args);
    }


    private static void parseAndStart(String[] args) {
        Options options = new Options();

        Option temp = new Option("m", "mode", true, "Work Mode");
        options.addOption(temp);

        temp = new Option("l", "locale", true, "Locale");
        options.addOption(temp);

        temp = new Option("t", "text", true, "Results contain text fields");
        options.addOption(temp);

        temp = new Option("cv", "vectorizer", true, "Vectorizer to be used in clustering");
        options.addOption(temp);

        temp = new Option("v", "version", false, "Version");
        options.addOption(temp);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("v")){
                System.out.println("Version: " + SharedObjects.VERSION);
                return;
            }
            if (!cmd.hasOption("m")){
                throw new ParseException("Missing required option: m");
            }
			if (!(cmd.getOptionValue("m").equalsIgnoreCase("service") || cmd.getOptionValue("m").equalsIgnoreCase("train"))) {
				throw new IllegalArgumentException(String.format("Unknown Work Mode '%s'. Work Mode must be 'service' or 'train'", cmd.getOptionValue("m")));
			}
			if (cmd.getOptionValue("m").equalsIgnoreCase("service")){
                if (cmd.hasOption("cv") && !(cmd.getOptionValue("cv").equalsIgnoreCase("tfidf")
                        || cmd.getOptionValue("cv").equalsIgnoreCase("count"))) {
                    throw new IllegalArgumentException("--vectorizer must be 'tfidf' or 'count'");
                }
                if (cmd.hasOption("t") && !(cmd.getOptionValue("t").equals("0") || cmd.getOptionValue("t").equals("1"))){
                    throw new IllegalArgumentException("--text value must be either '0' or '1'");
                }
            }

        } catch (ParseException | IllegalArgumentException e) {
            new HelpFormatter().printHelp("Information Extractor", options);
            System.err.println(e.getLocalizedMessage());
            return;
        }

        // Set Flags
        if (cmd.hasOption("l")) {
            Locale.setDefault(Locale.forLanguageTag(cmd.getOptionValue("l")));
        }

        if (cmd.getOptionValue("m").equalsIgnoreCase("service")) {
            if (cmd.hasOption("t")) {
                SharedObjects.serviceParams.getTextFields = Integer.parseInt(cmd.getOptionValue("t")) == 1;
            }

            if (cmd.hasOption("cv")) {
                SharedObjects.serviceParams.vectorizer = cmd.getOptionValue("cv");
            }
            serviceMain(args);
        } else {
            trainingMain();
        }
    }

    private static void trainingMain() {
        TrainingDriver driver = new TrainingDriver();
        driver.start();
    }

    private static void serviceMain(String[] args) {
        WebApp.start(args);
    }
}
