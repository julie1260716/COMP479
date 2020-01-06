import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

//consulted the following site for computeRank() and computeScore() methods
//https://www.javatips.net/api/smile-master/nlp/src/main/java/smile/nlp/relevance/RelevanceRanker.java

public class BM25 extends Corpus{

	private double k1;	// typically k1 = 2.0
	private double b;	// typically b = 0.75
	private double delta; 
	
	public BM25() {
		this.k1 = 2;
		this.b = 0.9;
		this.delta = 1.0;
		System.out.println("creating opaki weighting scheme...");
	}
	
	public BM25(double _k1, double _b, double _delta) {
		this.k1 = _k1;
		this.b = _b;
		this.delta = _delta;
	}
	
/* computeScore() implements the BM25 formula as described in the textbook (11.32)
 * It returns a relevance score between a term and a document, based on a corpus
 * --------------------------------------------------------------------------------	 
 * @param tf is the frequency of term t in document d	
 * @param ld is the length of the document d
 * @param lave is the average document length for the entire collection
 * @param N is the number of documents in the corpus
 * @param n is the number of documents that contain the term
 * @param k1 (k1 < 0) is a tuning parameter that calibrates the dtf scaling
 * @param b (0 < b < 1) 
 */
	public double computeScore(double tf, double ld, double lave, double N, double n) {
		
		double weight = ((k1 + 1) * tf) / (tf + k1 * (((1-b) + b * (ld/lave))));
		double idf = Math.log((N - n + 0.5) / (n + 0.5));
		
		return (weight + delta) * idf;
	}
	
/* computeRank() implements the BM25 formula as described in the textbook (11.32)
 * It returns a relevance score between a term and a document, based on a corpus
 * --------------------------------------------------------------------------------	 
 * @param tf is the frequency of term t in document d	
 * @param ld is the length of the document d
 * @param lave is the average document length for the entire collection
 * @param N is the number of documents in the corpus
 * @param n is the number of documents that contain the term
 * @param k1 (k1 < 0) is a tuning parameter that calibrates the dtf scaling
 * @param b (0 < b < 1)                    
 */
	public void computeRank() {
		
		System.out.println("\nBM25 ranking :");							
		
		Map<String, Double> topK = new TreeMap<String,Double>(); // <url, Score>
		ArrayList<String> termsOfDoc = new ArrayList<>();
		double score = 0;
		int tf = 0;
		int n = 0;
		docToTerm.toString();
		for (Map.Entry<String, ArrayList<String>> entry : docToTerm.entrySet()) {
			
			String url = entry.getKey();
			
			//iterate through terms associated to url
			termsOfDoc = entry.getValue();
			
			for(String t : termsOfDoc) {
		
				//getting tf of terms in doc
				tf = getTf(url, t);
				//System.out.println("frequency:	" + tf);
				
				//getting num docs that contain t
				n = get_n(t);
				
				score = computeScore(tf, getLd(url), getLave(), getN(), n);
				
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
	
}//end of BM25 class