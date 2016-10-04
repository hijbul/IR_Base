package structures;

import java.util.ArrayList;

/**
 * The structure wraps both \phi(_thetaStar) and \psi.
 * @author lin
 *
 */
public class _HDPThetaStar extends _thetaStar {
//	protected int m_dimFv; //dim for the language models.
	// beta in _thetaStar is \phi used in HDP.
	protected double[] m_psi;// psi used in multinomal distribution.
	public int m_hSize; //total number of local groups in the component.
	protected ArrayList<_Review> m_reviews;
	
	public _HDPThetaStar(int dim) {
		super(dim);
		m_psi = new double[m_dim];
		m_reviews = new ArrayList<_Review>();
	}

	public double[] getPsiModel(){
		return m_psi;
	}
	// Update \psi with the newly estimated prob. 
	public void updatePsiModel(double[] prob){
		System.arraycopy(prob, 0, m_psi, 0, prob.length);
	}
	public void addOneReview(_Review r){
		m_reviews.add(r);
	}
	
	public void rmReview(_Review r){
		m_reviews.remove(r);
	}
	
	public ArrayList<_Review> getReviews(){
		return m_reviews;
	}
}
