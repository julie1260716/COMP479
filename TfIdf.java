import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class TfIdf extends Corpus{

	public TfIdf() {
		System.out.println("creating tf-idf weighting scheme...");
	}
	
	public double computeScore(double tf, double N, double df) {
				
		double idf = 0; 
		
		//calculate inverse document f
		idf = Math.log(N/df);
		
		//return tf-idf		 
		return tf * idf;
	}
	
	/* 
	 * tf-idf(t, d) = tf(t, d)* idf(t, d)
	 * @param idf(t) = log(N/ df(t))
	 */
	public void computeRank() {
		
		System.out.println("\nTf-Idf ranking :");	
	
		Map<String, Double> topK = new TreeMap<String,Double>(); // <url, Score>
		ArrayList<String> termsOfDoc = new ArrayList<>();
		double score = 0;
		int tf = 0;
		int df = 0;
		
		
		for (Map.Entry<String, ArrayList<String>> entry : docToTerm.entrySet()) {
			
			String url = entry.getKey();
			
			//iterate through terms associated to url
			termsOfDoc = entry.getValue();
			
			for(String t : termsOfDoc) {
		
				//getting tf of terms in doc
				tf = getTf(url, t);
				
				//getting num docs containing t
				df = get_n(url);
				//must sum the tf*idf for each term to gain overall score
				score = computeScore(tf, getN(), df);
				
				//add to topk map
				if(!topK.containsKey(url))
					topK.putIfAbsent(url, score);
				else
				{
					double oldScore = topK.get(url);
					topK.remove(url);
					topK.put(url, score + oldScore);
				}								
			}							
		}
		
			// sort by value
				final Map<String, Double> ranked = sortByValue(topK);
				int k = 0;
				// printing results of ranked
						for (Map.Entry<String, Double> entry : ranked.entrySet()) 
						{
							k++;
							
							if(k < 100) { //top 100
								String url = entry.getKey();
							
								score = entry.getValue();
							
								System.out.println("\nurl : " + url + "\nscore : " + score);
								}
							else 
								break;
						}
	}
	//https://dzone.com/articles/how-to-sort-a-map-by-value-in-java-8
	public static Map<String, Double> sortByValue(final Map<String, Double> topK) {
	        return topK.entrySet()
	                .stream()
	                .sorted((Map.Entry.<String, Double>comparingByValue().reversed()))
	                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}
}
