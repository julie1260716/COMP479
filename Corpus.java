import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Corpus { //in Project III, the corpus is the set of all relevant web pages (urls)

	static protected Map <String, ArrayList<Integer>> termFreq; //<url, TermFreq> frequency of term in 1 document
	static protected Map <String, ArrayList<String>> docToTerm; //<url, Term>
	static protected Map <String, Integer>  docLengths; //<url, Length>
	static protected int aveDocLength; //over entire corpus
	static protected int totalDocs; //total number of docs in corpus
	static protected Map <String, Integer> numDocsT; //<Term, #Docs>

	public Corpus() {
		termFreq = new HashMap<>(); 	// <url, TermFreq>
		docToTerm = new HashMap<>();	// <url, Term>
		docLengths = new HashMap<>(); 	// <url, Length>
		aveDocLength = 0;
		totalDocs = 0;
		numDocsT = new HashMap<>();		// <Term, #docs>
	}
	
	// given a term, we are updating its frequency (+1)
	public void updateFreq(String url, String aTerm) {
		
/*
		//get index of given term 
		int index = docToTerm.get(url).indexOf(aTerm);
		//use index in term frequency map to get value
		int anotherOne = termFreq.get(url).get(index).intValue();
		anotherOne++;
		
		termFreq.get(url).add(index, anotherOne);*/
	
	}
	
	public void setAvDocLength(int totalLength, int N) {	
		
		aveDocLength = totalLength / N;
	}
	
	public void setTotalDocs(int N) {	
		
		totalDocs = N;
	}
	
	public void updateNumDocsT(String aTerm) {

		if(aTerm != "" && numDocsT.containsKey(aTerm))
		{
			int oldNum = numDocsT.get(aTerm);
			numDocsT.put(aTerm, oldNum + 1);
		}		
	}
	
	//we will add the url to the term mapping + add docLength
	public void addDocToTerm(String url, String aTerm, int aLength)
	{				
		//if the map doesn't contain the url, create + add
		if (!docToTerm.containsKey(url)) {
			
			docToTerm.put(url, new ArrayList<>());
			docToTerm.get(url).add(aTerm);
			
			//also set the tf map in the corresponding index
			termFreq.put(url, new ArrayList<>());
			termFreq.get(url).add(1);
			
			docLengths.put(url, aLength);		
			numDocsT.put(aTerm, 0);	
		}
		
		//if it contains url, but not the term, we add it
		if (!docToTerm.get(url).contains(aTerm)) {
			
			docToTerm.get(url).add(aTerm);
			termFreq.get(url).add(1);
			numDocsT.put(aTerm, 0);	
		}
		
		//if it contains url AND term already, we just update frequency
		if (docToTerm.containsKey(url) && docToTerm.get(url).contains(aTerm)) {
			
			int index = docToTerm.get(url).indexOf(aTerm);
			
			//use index in term frequency map to get value
			int anotherOne = termFreq.get(url).get(index);
			anotherOne++;
			
			//remove current value and add updated one
			termFreq.get(url).remove(index);
			termFreq.get(url).add(index, anotherOne);
			numDocsT.put(url, 0);	
		}			
			
		//System.out.println("\nDOC TO TERM		"+ docToTerm.toString());
		//System.out.println("\nTERM FREQ		"+ termFreq.toString());			

	}

	public int getTf(String url, String aTerm) {
		
		//get index of given term 
		int index = docToTerm.get(url).indexOf(aTerm);
		//use index in term frequency map to get value
		return termFreq.get(url).get(index).intValue();
	}
	
	public int getNumDocs() {
		
		return docToTerm.size();
	}
	
	public int getLd(String url) {
		
		return docLengths.get(url).intValue();
	}
	
	public int getLave() {
		
		return aveDocLength;
	}
	
	public int getN() {
		
		return totalDocs;
	}
	
	public int get_n(String url) {
		
		return numDocsT.get(url);	
	}
	
	public void getMap() {
		
		for (Map.Entry<String, ArrayList<String>> term : docToTerm.entrySet()) 
		    System.out.println(term.getKey() + ":" + term.getValue().toString());
		
	}
	
	public void getInfo(String url, String term) {
		
			
		System.out.println("\n-----------TERM INFO-----------");
		System.out.println("-------------------------------");
			System.out.println("  TERM: "+ term);
			System.out.println("  term frequency: "+ getTf(url, term));			
			System.out.println("  document length: "+ getLd(url));
			System.out.println("  avg doc length: " + getLave());
			System.out.println("  total docs: " + getN());
			System.out.println("  # docs containing t: " + get_n(term));
	}


	
}// end of Corpus class
