package Classifier.supervised.modelAdaptation.MMB;

import java.util.HashMap;

import Classifier.supervised.modelAdaptation.HDP.CLRWithHDP;
import Classifier.supervised.modelAdaptation.HDP._HDPAdaptStruct;
import cern.jet.random.tdouble.Beta;
import structures._HDPThetaStar;
import structures._Review;
import structures._Review.rType;
import structures._thetaStar;
import utils.Utils;

public class CLRWithMMB extends CLRWithHDP {
	
	boolean m_mmb = false; // Flag whether we will generate edge from mmb.
	public CLRWithMMB(int classNo, int featureSize, HashMap<String, Integer> featureMap, String globalModel,
			double[] betas) {
		super(classNo, featureSize, featureMap, globalModel, betas);
	}
	
	@Override
	// The function is used in "sampleOneInstance".
	public double calcGroupPopularity(_HDPAdaptStruct user, int k, double gamma_k){
		return user.getHDPThetaMemSize(m_hdpThetaStars[k]) + m_eta*gamma_k + user.getHDPThetaEdgeSize(m_hdpThetaStars[k]);
	}
	
	// Sample one edge between (ui, uj)
	public void sampleOneEdge(_HDPAdaptStruct ui, _HDPAdaptStruct uj){
		int k;
		double likelihood, gamma_k, logSum = 0;
		for(k=0; k<m_kBar; k++){
			
			ui.setThetaStar(m_hdpThetaStars[k]);
			
			//log likelihood of the edge p(e_{ij}, z, B)
			// p(eij|z_{i->j}, z_{j->i}, B)*p(z_{i->j}|\pi_i)*p(z_{j->i|\pi_j})
			likelihood = calcLogLikelihoodE(ui, uj);
						
			//p(z=k|\gamma,\eta)
			gamma_k = m_hdpThetaStars[k].getGamma();
			likelihood += Math.log(calcGroupPopularity(ui, k, gamma_k));
			
			m_hdpThetaStars[k].setProportion(likelihood);//this is in log space!
			
			// ????Integrate out new cluster probability?
			
			if(k==0) 
				logSum = likelihood;
			else 
				logSum = Utils.logSum(logSum, likelihood);
		}
		//Sample group k with likelihood.
		k = sampleInLogSpace(logSum);
		
		//Step 3: update the setting after sampling z_ij.
		m_hdpThetaStars[k].updateEdgeCount(1);//-->1
		ui.updateNeighbors(uj, m_hdpThetaStars[k]);
		
		//Step 4: Update the user info with the newly sampled hdpThetaStar.
		ui.incHDPThetaStarEdgeSize(m_hdpThetaStars[k], 1);//-->3		

		if(k >= m_kBar){//sampled a new cluster
			
			//
			m_hdpThetaStars[k].initPsiModel(m_lmDim);
			m_D0.sampling(m_hdpThetaStars[k].getPsiModel(), m_betas, true);//we should sample from Dir(\beta)
			
			double rnd = Beta.staticNextDouble(1, m_alpha);
			m_hdpThetaStars[k].setGamma(rnd*m_gamma_e);
			m_gamma_e = (1-rnd)*m_gamma_e;
			
			swapTheta(m_kBar, k);
			m_kBar++;
		}
	}
	protected double calcLogLikelihoodE(_HDPAdaptStruct ui, _HDPAdaptStruct uj){
		// probability for Bernoulli distribution.
		//	p(e_ij|z_{i->j}, z_{j->i},B)
		double p = ui.getThetaStar().getB()[uj.getThetaStar().getIndex()];
		double loglikelihood = 0;
		
		// how to get eij for amazon data? How to observe it?
		int eij = 0;
		loglikelihood = eij == 0 ? (1 - p) : p;
		loglikelihood = Math.log(loglikelihood);
		
		return loglikelihood;
	}
	
	protected void calculate_E_step(){
		// sample z_{i,d}
		super.calculate_E_step();
		
		// sample z_{i->j}
		_HDPThetaStar curThetaStar;
		_HDPAdaptStruct ui, uj;
		int index, sampleSize=0;
		for(int i=0; i<m_userList.size(); i++){
			ui = (_HDPAdaptStruct) m_userList.get(i);
			for(int j=0; j<m_userList.size() && i!=j; j++){
				
				// sample from the Bernoulli distribution of one edge.
				// m_mmb = ;
				if(m_mmb){
					uj = (_HDPAdaptStruct) m_userList.get(j);
				
					// If there is connection between ui and uj, update the connection.
					if(ui.hasEdge(uj)){
						//Step 1: remove the current review from the thetaStar and user side.
						curThetaStar = ui.getThetaStar(uj);
						ui.incHDPThetaStarEdgeSize(curThetaStar, -1);				
						curThetaStar.updateEdgeCount(-1);

						if(curThetaStar.getMemSize() == 0 && curThetaStar.getEdgeSize() == 0) {// No data associated with the cluster.
							//????
							curThetaStar.resetPsiModel();
							m_gamma_e += curThetaStar.getGamma();
							index = findHDPThetaStar(curThetaStar);
							swapTheta(m_kBar-1, index); // move it back to \theta*
							m_kBar --;
						}
					}
					//Step 2: sample new cluster assignment for this pair (ui, uj).
					sampleOneEdge(ui, uj);
				
					if (++sampleSize%2000==0) {
						System.out.print('.');
						sampleGamma();//will this help sampling?
						if (sampleSize%100000==0)
							System.out.println();
					}
				}
			}
		}
	}
	@Override
	protected double calculate_M_step(){
		// sample gamma + estPsi + estPhi
		double likelihood = super.calculate_M_step();
		return likelihood + estB() + estRho();
	}
	
	// Estimate the Bernoulli rates matrix.
	public double estB(){
		return 0;
	}
	
	// Estimate the sparsity parameter.
	public double estRho(){
		return 0;
	}
	
}
