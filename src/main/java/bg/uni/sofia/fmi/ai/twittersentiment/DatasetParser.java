package bg.uni.sofia.fmi.ai.twittersentiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

public class DatasetParser {
	private String datasetPath;
	private static final String DEFAULT_DATASET_PATH = "src/main/resources/dataset/metacritic_game_user_comments.csv";
	private static final String OUTPUT_FILENAME = "src/main/resources/dataset/parsed_dataset.csv";
	private static final String FINAL_DATASET_FILENAME = "src/main/resources/dataset/final_dataset.csv";
	private static final String PROFILES_PATH = "src/main/resources/profiles";
	private static final String SPOILERS_STRING = "This review contains spoilers, click expand to view.";

	/*
	 * countReviews() output:
	 * Rating 0: 19198
	 * Rating 1: 7079
	 * Rating 2: 5858
	 * Rating 3: 6974
	 * Rating 4: 7755
	 * Rating 5: 9881
	 * Rating 6: 11690
	 * Rating 7: 16396
	 * Rating 8: 28802
	 * Rating 9: 51416
	 * Rating 10: 106349
	 */
	private static final int MAGIC_NUMBER = 20000;

	public DatasetParser() {
		this.datasetPath = DEFAULT_DATASET_PATH;
	}

	public DatasetParser(String datasetPath) {
		this.datasetPath = datasetPath;
	}

	/*
	 * removes the text "This review contains spoilers, click expand to view."
	 * removes the name of the game from the review where possible
	 * removes reviews which aren't in English
	 * removes columns which aren't relevant
	 * removes symbols which won't be needed
	 * writes new dataset in a new csv file
	 */
	public void parseFile() throws LangDetectException {
		DetectorFactory.loadProfile(PROFILES_PATH);

		try (Reader reader = Files.newBufferedReader(Paths.get(datasetPath));
				CSVParser csvParser = new CSVParser(reader,
						CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());

				BufferedWriter writer = Files.newBufferedWriter(Paths.get(OUTPUT_FILENAME));
				CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("Userscore", "Comment"));) {

			for (CSVRecord csvRecord : csvParser) {
				String gameName = csvRecord.get("Title");
				String userscore = csvRecord.get("Userscore");
				String comment = csvRecord.get("Comment");

				comment = comment.replace(SPOILERS_STRING, "");

				Detector detector = DetectorFactory.create();
				detector.append(comment);

				try {
					if (detector.detect().equals("en")) {
						comment = comment.toLowerCase().replaceAll("[^a-z0-9' ]+", " ");

						gameName = gameName.toLowerCase().replaceAll("[^a-z0-9' ]+", " ").trim().replaceAll(" +", " ");

						comment = comment.replace(gameName, "").trim().replaceAll(" +", " ");

						if (!comment.equals("")) {
							csvPrinter.printRecord(userscore, comment);
						}
					}
				} catch (LangDetectException e) {
					System.err.println("Error detecting language.");
				}
			}

		} catch (IOException e) {
			System.err.println("An error has occurred while parsing the dataset file.");
			e.printStackTrace();
		}

	}

	/*
	 * the dataset contains a lot of positive reviews compared to the negative ones
	 * so we need to normalize the count of each rating to do that we'll scramble
	 * the reviews and remove ratings pass a certain threshold this way each rating
	 * 0-10 will have the same number of reviews scramble in order not to be biased
	 * from the reviews of one game only
	 */
	public void normalizeFile(String file) {
		ArrayList<String> lines = new ArrayList<>(300000);

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			// skip headers
			String line = br.readLine();

			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			System.err.println("Error reading file " + file);
			e.printStackTrace();
		}

		Collections.shuffle(lines);

		int countRatings[] = new int[11];

		try (PrintWriter writer = new PrintWriter(new File(FINAL_DATASET_FILENAME));) {
			for (String line : lines) {
				int rating = Integer.parseInt(line.split(",")[0]);

				if (countRatings[rating] <= MAGIC_NUMBER) {
					writer.println(line);
					countRatings[rating]++;
				}
			}
		} catch (IOException e) {
			System.err.println("Error writing to file " + FINAL_DATASET_FILENAME);
			e.printStackTrace();
		}
	}

	/*
	 * counts the number of reviews for each rating in the dataset
	 */
	public int[] countReviews(String file) {
		int ratingCount[] = new int[11];

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			// skip headers
			String line = br.readLine();

			while ((line = br.readLine()) != null) {

				int rating = Integer.parseInt(line.split(",")[0]);
				ratingCount[rating]++;
			}
		} catch (IOException e) {
			System.err.println("Error reading file " + file);
			e.printStackTrace();
		}

		return ratingCount;
	}
}
