package com.hot.cmt.comment.terms;

/**
 * 
 * @author yongleixiao
 *
 */
public class TermProbability {

	String term;
	double positive;
	double negtive;
	double objective;
	
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public double getPositive() {
		return positive;
	}
	public void setPositive(double positive) {
		this.positive = positive;
	}
	public double getNegtive() {
		return negtive;
	}
	public void setNegtive(double negtive) {
		this.negtive = negtive;
	}
	public double getObjective() {
		return objective;
	}
	public void setObjective(double objective) {
		this.objective = objective;
	}
}
