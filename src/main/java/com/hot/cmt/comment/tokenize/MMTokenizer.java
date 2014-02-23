package com.hot.cmt.comment.tokenize;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.Word;
/**
 * 
 * @author yongleixiao
 *
 */
public class MMTokenizer {
	
	private String DICT_PATH = "D:/changyancomments/worddict/data";
	private Dictionary dic = null;
	private Seg seg = null;
	
	public MMTokenizer() {
		init();
	}
	
	public void init() {
		dic = Dictionary.getInstance(DICT_PATH);
		seg = new ComplexSeg(dic);
	}
	
	public void setDictPath(String path) {
		if(StringUtils.isNotBlank(path)) {
			DICT_PATH = path;
		}
	}

	public List<String> getSplitedWords(String sentence) {
		if (StringUtils.isBlank(sentence)) {
			return null;
		}
		List<String> words = new ArrayList<String>();
		StringReader reader = getStringReader(sentence);
		MMSeg mmSeg = new MMSeg(reader, seg);  
        Word word = null; 
        try {
        	while ((word = mmSeg.next()) != null) {
        		words.add(word.getString()); 
        	}
        } catch (IOException e) {
        	
        }
        	return words;
	}
	
	private StringReader getStringReader(String sentence) {
		return new StringReader(sentence);
	}
	
	
//	public static void main(String[] args) throws IOException{
//		MMTokenizer tokenizer = new MMTokenizer();
//		tokenizer.init();
//		String txt = "那个好看的笑容里面全是悲伤，他在行尸走肉的活着，他的故事悲伤的像一场没有结局的黑白电影，他是她小说里的主角， 她懂他，他爱过她，她不知道自己是爱他的的外表，还是爱他的故事，还是爱他身上的那个自己。"; 
//		long start = System.currentTimeMillis();
//		for (int i = 0; i< 10000; i++) {
//			List<String> words = tokenizer.getSplitedWords(txt);
//			System.out.println(words.toString());
//		}
//		long end = System.currentTimeMillis();
//		System.out.printf("split times: %d", (end-start));
//	}
	 
}
