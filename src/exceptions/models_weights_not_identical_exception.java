package exceptions;


// indicates the number of models and number of weights are not identical
public class models_weights_not_identical_exception extends Exception {
	public models_weights_not_identical_exception(String message) {
		super(message);
	}
	
	public static void main(String[] args){
		models_weights_not_identical_exception mwe = new models_weights_not_identical_exception("models and weights are not identical");
		mwe.printStackTrace();
	}
}
