package bg.uni.sofia.fmi.ai.twittersentiment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MySentimentAnalyzer {
	private Set<String> stopWords;
	private Map<String, Double> wordScores;

	private List<String> frequencyDesc;
	private List<String> positivityDesc;

	public MySentimentAnalyzer(String reviewsFileName, String stopwordsFileName) {
		stopWords = new HashSet<String>();
		wordScores = new HashMap<String, Double>();

		readStopWords(stopwordsFileName);
		readMovieReviews(reviewsFileName);
	}

	private void readStopWords(String stopwordsFileName) {
		try (BufferedReader br = new BufferedReader(new FileReader(stopwordsFileName))) {
			String line;
			while ((line = br.readLine()) != null) {
				stopWords.add(line);
			}
		} catch (IOException e) {
			System.err.println("Error reading stopwords file " + stopwordsFileName);
		}
	}

	private void readMovieReviews(String reviewsFileName) {
		Map<String, Integer> wordEncounters = new HashMap<String, Integer>();
		Map<String, Integer> wordTotalScore = new HashMap<String, Integer>();

		try (BufferedReader br = new BufferedReader(new FileReader(reviewsFileName))) {
			// skip headers
			String line = br.readLine();

			while ((line = br.readLine()) != null) {
				String[] columns = line.split(",");

				if (columns.length > 0) {
					int wordScore = Integer.parseInt(columns[0]);

					String[] parts = columns[1].split("[^a-z0-9]+");

					for (int i = 0; i < parts.length; i++) {
						if (!isStopWord(parts[i])) {
							Integer encounters = wordEncounters.get(parts[i]);

							if (encounters == null) {
								// first encounter
								wordEncounters.put(parts[i], 1);
							} else {
								wordEncounters.put(parts[i], encounters + 1);
							}

							Integer totalScore = wordTotalScore.get(parts[i]);

							if (totalScore == null) {
								// first encounter
								wordTotalScore.put(parts[i], wordScore);
							} else {
								wordTotalScore.put(parts[i], totalScore + wordScore);
							}
						}
					}
				}
			}

			for (Map.Entry<String, Integer> entry : wordEncounters.entrySet()) {
				String key = entry.getKey();

				Integer encounters = entry.getValue();
				Integer totalScore = wordTotalScore.get(key);

				double avgScore = (double) totalScore / encounters;

				wordScores.put(key, avgScore);
			}

			// sort by word frequency in descending order
			frequencyDesc = new ArrayList<String>(wordEncounters.keySet());
			frequencyDesc.sort(new Comparator<String>() {
				@Override
				public int compare(String s1, String s2) {
					return Integer.compare(wordEncounters.get(s2), wordEncounters.get(s1));
				}
			});

			// sort by word positivity in descending order
			positivityDesc = new ArrayList<String>(wordTotalScore.keySet());
			positivityDesc.sort(new Comparator<String>() {
				@Override
				public int compare(String s1, String s2) {
					return Double.compare(wordScores.get(s2), wordScores.get(s1));
				}
			});

		} catch (IOException e) {
			System.err.println("Error reading movie reviews file " + reviewsFileName);
		}
	}

	public double getReviewSentiment(String review) {
		double finalScore = -1.0;
		int wordCount = 0;

		review = review.toLowerCase();
		String[] words = review.split("[^a-z0-9]+");

		for (String word : words) {
			if (!isStopWord(word) && wordScores.containsKey(word)) {
				wordCount++;
				if (finalScore < 0) {
					finalScore = wordScores.get(word);
				} else {
					finalScore += wordScores.get(word);
				}
			}
		}

		if (wordCount == 0) {
			return finalScore;
		} else {
			return finalScore / wordCount;
		}
	}

	public double getWordSentiment(String word) {
		word = word.toLowerCase();
		return wordScores.containsKey(word) ? wordScores.get(word) : -1.0;
	}

	public Collection<String> getMostFrequentWords(int n) {
		return frequencyDesc.subList(0, n);
	}

	public Collection<String> getMostPositiveWords(int n) {
		return positivityDesc.subList(0, n);
	}

	public Collection<String> getMostNegativeWords(int n) {
		List<String> returnList = positivityDesc.subList(positivityDesc.size() - n, positivityDesc.size());
		Collections.reverse(returnList);
		return returnList;
	}

	public int getSentimentDictionarySize() {
		return wordScores.size();
	}

	public boolean isStopWord(String word) {
		return stopWords.contains(word);
	}

}
