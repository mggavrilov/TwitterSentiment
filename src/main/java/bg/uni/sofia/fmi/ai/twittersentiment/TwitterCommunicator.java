package bg.uni.sofia.fmi.ai.twittersentiment;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterCommunicator {
	private static final String CONSUMER_KEY = "mpuKltAyJtms3UhTnAWKk2s2X";
	private static final String CONSUMER_SECRET = "CnbvL57jRjlq3zsi1YnQQJEdBtGkn2pwshMPaqZq3ggiW6MpnP";
	private static final String ACCESS_TOKEN = "1480419014-rqrM4tG4yNwZ4Apvt5FJPmqrqb03VDHFy0rfAUH";
	private static final String ACCESS_SECRET = "klQA65enWiaSmHXSmZKHITH4d7oDVl6MW45LkOOfRyyKZ";
	private Twitter twitter;

	public TwitterCommunicator() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(CONSUMER_KEY).setOAuthConsumerSecret(CONSUMER_SECRET)
				.setOAuthAccessToken(ACCESS_TOKEN).setOAuthAccessTokenSecret(ACCESS_SECRET);

		this.twitter = new TwitterFactory(cb.build()).getInstance();
	}

	public ArrayList<String> searchTwitter(String q) {
		ArrayList<String> returnTweets = new ArrayList<String>();
		try {
			Query query = new Query(q + "&lang:en -filter:retweets");
			QueryResult result;
			// do {
			result = twitter.search(query);
			List<Status> tweets = result.getTweets();
			for (Status tweet : tweets) {
				String text = tweet.getText();
				text = text.toLowerCase();
				text = text.replaceAll("#\\s*(\\w+)", "");
				text = text.replaceAll("@\\s*(\\w+)", "");
				text = text.replaceAll("http://[^\\s]+", "");
				text = text.replaceAll("https://[^\\s]+", "");
				text = text.replaceAll("[^a-z0-9 ]+", " ").trim().replaceAll(" +", " ");

				if (!text.equals("")) {
					returnTweets.add(text);
				}
			}
			// } while ((query = result.nextQuery()) != null);
		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to search tweets: " + te.getMessage());
		}

		return returnTweets;
	}
}
