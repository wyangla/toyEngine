package test;
import data_structures.*;
import inverted_index.*;
import java.util.*;
import entities.*;


public class tf_idf_test {
	
	public static void main(String[] args) {
		index idx = index.get_instance();
		String persistedPostUnit = "your 3483 5472 3266 {\"tf\":3,\"oh\":1} /test_1/EKAN4jw3LsE3631feSaA_g 1"; // tf = 3
		posting_unit pUnit = posting_unit.deflatten(persistedPostUnit);
		
		idx.docMap.put("bb", new doc());
		idx.docMap.put("aa", new doc());
		idx.docMap.put("cc", new doc());
		idx.docMap.put("dd", new doc());
		idx.docMap.put("ee", new doc()); // n = 5
		
		idx.lexicon.put("your", new ArrayList<Long>());
		idx.lexicon.get("your").add(1L);
		idx.lexicon.get("your").add(1L);
		idx.lexicon.get("your").add(1L); // df = 3
		
		// tfidf = 3 * log(5 / 3) 
		// score = 0.6 tfidf + 0.2 = 1.4259814970383777
		System.out.println(scorer.getInstance().cal_score(pUnit));
		
	}
	
	
}
