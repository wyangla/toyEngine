package inverted_index;
import configs.*;
import java.lang.reflect.*;
import exceptions.*;



// this class is used by the scanner_plugins.search_term class to
//make use of the configured plugin classes to calculate the score for one post unit
public class scorer {
	
	public double cal_score(posting_unit postUnit) {
		double docScoreFromUnit = 0.;
		String[] targetModels = scorer_config.modelsInUse;
		double[] modelWeights = scorer_config.modelWeight;
		
		if(targetModels.length == modelWeights.length) {
			
			for(int i = 0; i < targetModels.length; i ++) {
				try {
					String modelName = targetModels[i];
					double weight = modelWeights[i];
					Class modelCls = Class.forName(scorer_config.scorerPluginPath + modelName);
					Method calScore = modelCls.getMethod("cal_score", posting_unit.class);
					docScoreFromUnit += weight * (double)calScore.invoke(null, postUnit);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			new models_weights_not_identical_exception("the configuration of weights and models are not correct");
		}
		return docScoreFromUnit;
	}
}
