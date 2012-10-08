package slides.main;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class Main {

	public static void main(String... args) {
		try {
			if (Boolean.valueOf(getSwitchArgValue("--help", "false", "true", args))) {
				showHelp();
			} else {
				createSlides(args);
			}
		} catch (Exception e) {
			System.out.println("Something went wrong! " + e.getMessage());
			showHelp();
		}
	}

	private static void showHelp() {
		System.out.println("");
		System.out.println("Generate HTML5 slides from template");
		System.out.println("");
		System.out.println("-i <path>\tWhere to find html files to include in template. \n\t\tDefault is \"includes\".\n\t\tIncluded files must be named 1.html, 2.html etc.\n\t\t in ascending order if -alt is not defined");
		System.out.println("");
		System.out.println("-alt <path>\tPath to a file with an alternative page configuration.\n\t\tThe file must contain a comma separated list of html files to use in the slides.");
		System.out.println("");
		System.out.println("-alt <list>\tComma separated list of html files to use in slides.");
		System.out.println("");
		System.out.println("-rf <filename>\tName of result file.\n\t\tDefault is index.html");
		System.out.println("");
		System.out.println("-t <transition>\tType of transition to use in slides.\n\t\tDefault is \"cube\".\n\t\tOptions are cube, concave, page, linear");
		System.out.println("");
		System.out.println("-vs\t\tUse vertical sliding");
		System.out.println("");
		System.out.println("-title\t\tChange title in webbrowser\n\t\tDefault is \"KnowIT\"");
		System.out.println("");
		System.out.println("--help\t\tShow this help page");
	}

	private static void createSlides(String... args) throws Exception {
		String includesMap = getIncludesMap(args);
		StringBuilder sectionIncludes = new StringBuilder();
		if (Boolean.valueOf(verticalScroll(args))) {
			System.out.println("Vertical scroll selected");
			sectionIncludes.append("<section>");
		}

		String slideOrder = "";
		System.out.println("Folder to include files from: " + includesMap);
		if (getSwitchArgValue("-alt", null, null, args) != null) {
			System.out.println("Alternative page layout selected");
			slideOrder = alternativePageLayout(sectionIncludes, includesMap, args);
		} else { 
			slideOrder = standardPageLayout(sectionIncludes, includesMap);
		}
		
		if (Boolean.valueOf(verticalScroll(args))) {
			sectionIncludes.append("</section>");
		}
		
		String transition = getTransition(args);
		System.out.println("Transition type: " + transition);
		if (!(transition.equals("cube") || transition.equals("linear") || transition.equals("concave") || transition.equals("page")) ) {
			throw new Exception("Invalid transition " + transition + " Valid transitions are cube, page, linear and concave");
		}
		String indexToCreate = FileUtils.readFileToString(new File("templates/index.html")).replaceAll("#sections#", sectionIncludes.toString()).replaceAll("#transition#", transition);
		indexToCreate = indexToCreate.replaceAll("#title#", getTitle(args));
		String resultFile = getResultFile(args);
		FileUtils.writeStringToFile(new File(resultFile), indexToCreate);
		System.out.println("Result generated to file: " + resultFile);
		System.out.println("Slide order " + slideOrder);
	}

	private static String alternativePageLayout(StringBuilder sectionIncludes, String includesMap, String... args) throws IOException {
		String slideOrder = "";
		String alternative = getSwitchArgValue("-alt", null, null, args);
		String readFile;
		try {
			readFile = FileUtils.readFileToString(new File(alternative));
		} catch (Exception e) {
			readFile = alternative;
		}
		String[] pages = readFile.split(",");
		for (String page : pages) {
			readSite(sectionIncludes, new File(includesMap + "/" + page.trim()));
			slideOrder += page.trim() + " ";
		}
		return slideOrder;
	}

	private static String standardPageLayout(StringBuilder sectionIncludes, String includesMap
			) throws IOException {
		String slideOrder = "";
		String fileName = "1.html";
		File file = new File(includesMap + "/" + fileName);
		int fileIndex = 2;
		while (file.exists()) {
			slideOrder += fileName + " ";
			readSite(sectionIncludes, file).toString();
			fileName = fileIndex++ + ".html";
			file = new File(includesMap + "/" + fileName);
		}
		return slideOrder;
	}

	private static StringBuilder readSite(StringBuilder sectionIncludes,
			File file) throws IOException {
		return sectionIncludes.append("<section>").append(FileUtils.readFileToString(file)).append("</section>");
	}

	private static String getTitle(String[] args) {
		return getSwitchArgValue("-title", "KnowIT", null, args);
	}

	private static String getResultFile(String[] args) {
		return getSwitchArgValue("-rf", "index.html", null, args);
	}

	private static String getIncludesMap(String[] args) {
		return getSwitchArgValue("-i", "includes", null, args);
	}

	private static String getTransition(String... args) {
		return getSwitchArgValue("-t", "cube", null, args);
	}

	private static String verticalScroll(String... args) {
		return getSwitchArgValue("-vs", "false", "true", args);
	}

	private static String getSwitchArgValue(String s, String defaultVal, String matchval, String... args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(s)) {
				return matchval != null ? matchval : args[i+1];
			}
		}
		return defaultVal;
	}
}
