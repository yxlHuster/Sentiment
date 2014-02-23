package com.hot.cmt.comment.terms;

import java.util.List;
import java.util.ArrayList;

/**
 * 
 * @author yongleixiao
 *
 */
public class NGramGenerator {

	/**
	 * 得到unigram特征词
	 * @param	terms 原特征词数组
	 * @return	unigram 特征词数组
	 */
	public static List<String> getUniGram(String[] terms) {
		List<String> words = new ArrayList<String>();
		for (String str : terms) {
			words.add(str);
		}
		return words;
	}
	
	/**
	 * 得到bigram特征词
	 * @param	terms 原特征词数组
	 * @return	bigram 特征词数组
	 */
	public static List<String> getBiGram(String[] terms) {
		List<String> words = new ArrayList<String>();
		int len = terms.length - 1;
		for (int i = 0; i < len; i++) {
			words.add(terms[i] + terms[i+1]);
		}
		return words;
	}
	
	/**
	 * 得到trigram特征词
	 * @param	terms 原特征词数组
	 * @return	trigram 特征词数组
	 */
	public static List<String> getTriGram(String[] terms) {
		List<String> words = new ArrayList<String>();
		int len = terms.length - 2;
		for (int i = 0; i < len; i++) {
			words.add(terms[i]+terms[i+1]+terms[i+2]);
		}
		return words;
	}
	
}
