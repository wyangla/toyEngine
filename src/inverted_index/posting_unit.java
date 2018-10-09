package inverted_index;
import java.util.*;



// the first unit of each posting list is empty, just for holding the place and acting as the starter of the link list
public class posting_unit {
	
	public long currentId = -1L; // unique identification of current link unit, set directly from outside
	
	public long nextId = -1L; // unique identification of next link unit
	public long previousId = -1L; // unique identification of previous link unit
	
	public posting_unit nextUnit = null; // link to the next unit, if == null means is the last one
	public posting_unit prevUnit = null; // link to the prvious unit, if == null means is the earliest one
	
	public HashMap uProp = new HashMap(); // for storing properties like tfidf, etc.
	
	public int status = 1; // 1 linked, 0 disconnected
	
	// link to the previous posting list unit
	public long link_to_previous(posting_unit previous_unit_ins) {
		prevUnit = previous_unit_ins;
		if (previous_unit_ins != null) {
			previousId = prevUnit.currentId;
		}else { // when links to nothing previous, means the current is the starter
			previousId = -1;
		}
		return previousId;
	}
	
	// link to the following posting list unit
	public long link_to_next(posting_unit next_unit_ins) {
		nextUnit = next_unit_ins;
		if(next_unit_ins != null) {
			nextId = nextUnit.currentId;
		}else {  // when links to nothing following, means the current is the ender
			nextId = -1;
		}

		return nextId;
	
	}
	
	
	
	// testing
	public static void main(String[] args) {
		posting_unit _self = new posting_unit();
		_self.uProp.put("testKey", 0.01);
		System.out.println("" + _self.uProp.entrySet());
		System.out.println("" + _self.link_to_next(null));
		
	}
}
