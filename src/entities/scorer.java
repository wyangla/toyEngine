package entities;
import configs.*;
import data_structures.posting_unit;

import java.lang.reflect.*;
import exceptions.*;



// this class is used by the scanner_plugins.search_term class to
//make use of the configured plugin classes to calculate the score for one post unit
public class scorer {
	
	// not singleton here, as scorer is used in multiple-threading context like advanced_ops.search
	private scorer() {}
	public static scorer getInstance() {
		return new scorer();
	}
	
	
	// require manually input the models and weights
	public double cal_score(String[] targetModels, double[] modelWeights, posting_unit postUnit) {
		double docScoreFromUnit = 0.;
		
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
	
	
	// use the config file to do plan scoring
	// used in plain search_term
	// aso used in advanced_ops directly for scoring the chosen topK units
	public double cal_score(posting_unit postUnit) {
		double docScoreFromUnit = 0.;
		String[] targetModels = scorer_config.modelsInUse;
		double[] modelWeights = scorer_config.modelWeight;
		docScoreFromUnit = cal_score(targetModels, modelWeights, postUnit);
		return docScoreFromUnit;
	}
	
	

	
}
