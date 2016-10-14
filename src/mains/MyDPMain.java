package mains;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import opennlp.tools.util.InvalidFormatException;
import structures._User;
import Analyzer.MultiThreadedLMAnalyzer;
import Analyzer.MultiThreadedUserAnalyzer;
import Analyzer.UserAnalyzer;
import Classifier.supervised.GlobalSVM;
import Classifier.supervised.modelAdaptation.Base;
import Classifier.supervised.modelAdaptation.ModelAdaptation;
import Classifier.supervised.modelAdaptation.MultiTaskSVM;
import Classifier.supervised.modelAdaptation.ReTrain;
import Classifier.supervised.modelAdaptation._AdaptStruct;
import Classifier.supervised.modelAdaptation.CoLinAdapt.LinAdapt;
import Classifier.supervised.modelAdaptation.DirichletProcess.MTCLRWithDP;
import Classifier.supervised.modelAdaptation.HDP.CLRWithHDP;
import Classifier.supervised.modelAdaptation.HDP.CLinAdaptWithHDP;
import Classifier.supervised.modelAdaptation.HDP.MTCLRWithHDP;
import Classifier.supervised.modelAdaptation.HDP.MTCLinAdaptWithHDP;

public class MyDPMain {
	
	//In the main function, we want to input the data and do adaptation 
	public static void main(String[] args) throws InvalidFormatException, FileNotFoundException, IOException{
	
		int classNumber = 2;
		int Ngram = 2; // The default value is unigram.
		int lengthThreshold = 5; // Document length threshold
		double trainRatio = 0, adaptRatio = 0.5;
		int displayLv = 1;
		int numberOfCores = Runtime.getRuntime().availableProcessors();

		double eta1 = 0.05, eta2 = 0.05, eta3 = 0.05, eta4 = 0.05;
		boolean enforceAdapt = true;

		String dataset = "Amazon"; // "Amazon", "AmazonNew", "Yelp"
		String tokenModel = "./data/Model/en-token.bin"; // Token model.
		
		String providedCV = String.format("./data/CoLinAdapt/%s/SelectedVocab.csv", dataset); // CV.
		String userFolder = String.format("./data/CoLinAdapt/%s/Users", dataset);
		String featureGroupFile = String.format("./data/CoLinAdapt/%s/CrossGroups_800.txt", dataset);
		String featureGroupFileB = String.format("./data/CoLinAdapt/%s/CrossGroups.txt", dataset);
		String globalModel = String.format("./data/CoLinAdapt/%s/GlobalWeights.txt", dataset);
		String lmFvFile = String.format("./data/CoLinAdapt/%s/fv_lm.txt", dataset);
		
//		String providedCV = String.format("/if15/lg5bt/DataSigir/%s/SelectedVocab.csv", dataset); // CV.
//		String userFolder = String.format("/if15/lg5bt/DataSigir/%s/Users", dataset);
//		String featureGroupFile = String.format("/if15/lg5bt/DataSigir/%s/CrossGroups_800.txt", dataset);
//		String featureGroupFileB = String.format("/if15/lg5bt/DataSigir/%s/CrossGroups_800.txt", dataset);
//		String globalModel = String.format("/if15/lg5bt/DataSigir/%s/GlobalWeights.txt", dataset);
//		String featureFile4LM = String.format("/if15/lg5bt/DataSigir/%s/fv_lm.txt", dataset);

		MultiThreadedLMAnalyzer analyzer = new MultiThreadedLMAnalyzer(tokenModel, classNumber, providedCV, lmFvFile, Ngram, lengthThreshold, numberOfCores);
		analyzer.config(trainRatio, adaptRatio, enforceAdapt);
		analyzer.loadUserDir(userFolder);
		analyzer.setFeatureValues("TFIDF-sublinear", 0);
		HashMap<String, Integer> featureMap = analyzer.getFeatureMap();
		
//		Base base = new Base(classNumber, analyzer.getFeatureSize(), featureMap, globalModel);
//		base.loadUsers(analyzer.getUsers());
//		base.setPersonalizedModel();
//		base.test();
//		for(_User u: analyzer.getUsers())
//			u.getPerfStat().clear();
//		
//		GlobalSVM gsvm = new GlobalSVM(classNumber, analyzer.getFeatureSize());
//		gsvm.loadUsers(analyzer.getUsers());
//		gsvm.train();
//		gsvm.test();
//		for(_User u: analyzer.getUsers())
//			u.getPerfStat().clear();
		
//		double[] globalLM = analyzer.estimateGlobalLM();
//		CLRWithHDP hdp = new CLRWithHDP(classNumber, analyzer.getFeatureSize(), featureMap, globalModel, globalLM);
		
//		MTCLRWithHDP hdp = new MTCLRWithHDP(classNumber, analyzer.getFeatureSize(), featureMap, globalModel, globalLM);
//		hdp.setQ(0.2);
		
//		CLinAdaptWithHDP hdp = new CLinAdaptWithHDP(classNumber, analyzer.getFeatureSize(), featureMap, globalModel, featureGroupFile, globalLM);

//		MTCLinAdaptWithHDP hdp = new MTCLinAdaptWithHDP(classNumber, analyzer.getFeatureSize(), featureMap, globalModel, featureGroupFile, null, globalLM);
//		hdp.setR2TradeOffs(eta3, eta4);
//		hdp.setsdB(0.1);
//
//		hdp.setsdA(0.1);
//		double alpha = 0.1, eta = 1, beta = 1;
//		hdp.setConcentrationParams(alpha, eta, beta);
//		hdp.setR1TradeOffs(eta1, eta2);
//		hdp.setNumberOfIterations(200);
//		hdp.loadUsers(analyzer.getUsers());
//		hdp.setDisplayLv(displayLv);
//		hdp.train();
//		hdp.test();
		
//		for(_User u: analyzer.getUsers())
//			u.getPerfStat().clear();
//			
		MultiTaskSVM mtsvm = new MultiTaskSVM(classNumber, analyzer.getFeatureSize());
		mtsvm.loadUsers(analyzer.getUsers());
		mtsvm.setBias(true);
		mtsvm.train();
		mtsvm.test();
		mtsvm.printEachUserPerf();
	}
}
