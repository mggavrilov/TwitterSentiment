package bg.uni.sofia.fmi.ai.twittersentiment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class TwitterSentiment {

	public static void main(String[] args) {
		
		TwitterCommunicator twitter = new TwitterCommunicator();
		ArrayList<String> tweets1 = twitter.searchTwitter("#Warcraft3Reforged");
		ArrayList<String> tweets2 = twitter.searchTwitter("#journeytothesavageplanet");
		
		MySentimentAnalyzer analyzer = new MySentimentAnalyzer("src/main/resources/dataset/final_dataset.csv", "src/main/resources/stopwords.txt");
		
		double totalScore = 0.0;
		int totalReviews = 0;
		
		for(String line : tweets1) {
			Properties props = new Properties();
	        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
	        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	        int mainSentiment = -1;
	        if (line != null && line.length() > 0) {
	            int longest = 0;
	            Annotation annotation = pipeline.process(line);
	            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
	                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
	                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
	                String partText = sentence.toString();
	                if (partText.length() > longest) {
	                    mainSentiment = sentiment;
	                    longest = partText.length();
	                }
	 
	            }
	        }
	        
	        if(mainSentiment > -1) {
	        	totalScore += mainSentiment;
	        	totalReviews ++;
	        }
		}
		
		System.out.println("CoreNLP (0-4)");
		System.out.println("#Warcraft3Reforged");
		System.out.println(totalScore / totalReviews);
		System.out.println();
		
		totalScore = 0.0;
		totalReviews = 0;
		
		for(String line : tweets2) {
			Properties props = new Properties();
	        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
	        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	        int mainSentiment = -1;
	        if (line != null && line.length() > 0) {
	            int longest = 0;
	            Annotation annotation = pipeline.process(line);
	            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
	                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
	                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
	                String partText = sentence.toString();
	                if (partText.length() > longest) {
	                    mainSentiment = sentiment;
	                    longest = partText.length();
	                }
	 
	            }
	        }
	        
	        if(mainSentiment > -1) {
	        	totalScore += mainSentiment;
	        	totalReviews ++;
	        }
		}
		
		System.out.println("CoreNLP (0-4)");
		System.out.println("#journeytothesavageplanet");
		System.out.println(totalScore / totalReviews);
		System.out.println();
        
        totalScore = 0.0;
		totalReviews = 0;
		
		for(String s : tweets1) {
			totalScore += analyzer.getReviewSentiment(s);
			totalReviews++;
		}

		System.out.println("My Sentiment Analyzer (0-10)");
		System.out.println("#Warcraft3Reforged");
		System.out.println(totalScore / totalReviews);
		System.out.println();
		
		totalScore = 0.0;
		totalReviews = 0;
		
		for(String s : tweets2) {
			totalScore += analyzer.getReviewSentiment(s);
			totalReviews++;
		}
		
		System.out.println("My Sentiment Analyzer (0-10)");
		System.out.println("#journeytothesavageplanet");
		System.out.println(totalScore / totalReviews);
		
		Scanner sc = new Scanner(System.in);
		while (true) {
			System.out.println();
			System.out.print("Text: ");
			String review = sc.nextLine();
			double rating = analyzer.getReviewSentiment(review);
			System.out.println("My sentiment analyzer (0-10): " + rating);
			
			Properties props = new Properties();
	        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
	        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	        int mainSentiment = -1;
	        if (review != null && review.length() > 0) {
	            int longest = 0;
	            Annotation annotation = pipeline.process(review);
	            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
	                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
	                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
	                String partText = sentence.toString();
	                if (partText.length() > longest) {
	                    mainSentiment = sentiment;
	                    longest = partText.length();
	                }
	 
	            }
	        }
	        System.out.println("CoreNLP (0-4): " + mainSentiment);
		}
		
		
		
		//parse dataset
		/*
		DatasetParser datasetParser = new DatasetParser("src/main/resources/dataset/metacritic_game_user_comments.csv");

		final long startTime = System.currentTimeMillis();

		try {
			datasetParser.parseFile();
		} catch (LangDetectException e) {
			e.printStackTrace();
		}
		final long endTime = System.currentTimeMillis();

		System.out.println("Total execution time: " + (endTime - startTime));
		*/

		/*
		// normalize parsed dataset
		DatasetParser parser = new DatasetParser();
		parser.normalizeFile("src/main/resources/dataset/parsed_dataset.csv");
		System.out.println("Done");
		MySentimentAnalyzer analyzer = new MySentimentAnalyzer("src/main/resources/dataset/final_dataset.csv",
				"src/main/resources/stopwords.txt");

		Collection<String> bestWords = new ArrayList<>();
		bestWords = analyzer.getMostPositiveWords(1000);
		Collection<String> worstWords = new ArrayList<>();
		worstWords = analyzer.getMostNegativeWords(1000);

		System.out.println("BEST");
		for (String s : bestWords) {
			System.out.println(s);
		}
		System.out.println();
		System.out.println();
		System.out.println();

		System.out.println("WORST");
		for (String s : worstWords) {
			System.out.println(s);
		}

		Scanner sc = new Scanner(System.in);

		while (true) {
			String review = sc.nextLine();
			double rating = analyzer.getReviewSentiment(review);
			System.out.println(rating);
		}

		
		// count number of reviews by rating
		DatasetParser parser = new DatasetParser();
		int count[] = parser.countReviews("src/main/resources/dataset/parsed_dataset.csv");

		for (int i = 0; i < 11; i++) {
			System.out.println("Rating " + i + ": " + count[i]);
		}
		*/
	}

}
