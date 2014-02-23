package com.hot.cmt.comment.common;

import com.hot.cmt.comment.util.Config;
import com.hot.cmt.comment.util.ConfigFactory;

/**
 * 
 * @author yongleixiao
 *
 */
public class Constants {

	private static Config config = ConfigFactory.config();
	
	public static final String WORD_PATH = config.get("Word_Path");
	
	/* 语气词库路径  */ 
	public static final String TONE_WORDS = WORD_PATH + config.get("Tone_Words");
	
	/* 正例情感词库 */
	public static final String POS_SEN_DICT = WORD_PATH + config.get("Pos_Sen_Dict");
	
	/* 经过同义词扩展的正例情感词库 */
	public static final String POS_SEM_DICT = WORD_PATH + config.get("Pos_Sem_Dict");
	
	/* 正例评价词库 */
	public static final String POS_CON_DICT = WORD_PATH + config.get("Pos_Con_Dict");
	
	/* 负例情感词库 */
	public static final String NEG_SEN_DICT = WORD_PATH + config.get("Neg_Sen_Dict");
	
	/* 经过同义词扩展的负例情感词库 */
	public static final String NEG_SEM_DICT = WORD_PATH + config.get("Neg_Sem_Dict");
	
	/* 负例评价词库 */
	public static final String NEG_CON_DICT = WORD_PATH + config.get("Neg_Con_Dict");
	
	/* 停用词表 */
	public static final String STOP_WORDS = WORD_PATH + config.get("Stop_Words");
	
	/* 与正例的pmiscore的路径  */
	public static final String POS_PMI_SCORE = WORD_PATH + config.get("Pos_Pmi_Score");
	
	/* 与负例的pmiscore的路径 */
	public static final String NEG_PMI_SCORE = WORD_PATH + config.get("Neg_Pmi_Score");
	
	/* 情感词的默认权重  */
	public static final Double NORMAL_WEIGHT = config.getDouble("Normal_Weight");
	
	/* 评价词的默认权重 */
	public static final Double COMMENT_WEIGHT = config.getDouble("Comment_Weight");
	
	/* 出现语气词的权重 */
	public static final Double TONE_WEIGHT = config.getDouble("Tone_Weight");
	
	/* 经过语义扩展的情感词权重 */
	public static final Double NORMAL_SEMANTIC_WEIGHT = config.getDouble("Normal_Semantic_Weight");
	
	
	/* 情感判定的阈值 */
	public static final Double SEN_THRESHOLD = config.getDouble("Sen_Threshold");
	
	
	/* 词性*/
	public static enum WordType {
        POS(1), //积极情感词
        NEG(2),  //消极情感词
        TONE(3), //语气词
        POS_COMMENT(4), //积极评价词
        NEG_COMMENT(5), //消极评价词
        POS_SEM(6), //积极情感词，语义扩展
        NEG_SEM(7), //消极情感词，语义扩展
        STOP_WORD(8), //停用词
        NEUTRAL_WORD(9), //中性词
        UN_KNOWN(10); //未知类型
        
        
        private int value = 0;

        private WordType (int value) {
            this.value = value;
        }

        public static WordType valueOf(int value) {
        	
            switch (value) {
                case 1:
                    return POS;
                case 2:
                    return NEG;
                case 3:
                	return TONE;
                case 4:
                	return POS_COMMENT;
                case 5:
                	return NEG_COMMENT;
                case 6:
                	return POS_SEM;
                case 7:
                	return NEG_SEM;
                case 8:
                	return STOP_WORD;
                case 9:
                	return NEUTRAL_WORD;
                default:
                    return UN_KNOWN;
            }
        }

        public int value() {
            return this.value;
        }
    }
	
	
	/* 情感属性分类 */
	public static enum SentimentType {
		POSITIVE(1),
		NEGATIVE(2),
		NEUTRAL(3),
		UN_KNOWN(4);
		
		private int value;
		
		private SentimentType(int value) {
			this.value = value;
		}
		
		public static SentimentType valueOf(int value) {
			switch (value) {
			case 1:
				return POSITIVE;
			case 2:
				return NEGATIVE;
			case 3:
				return NEUTRAL;
			default:
				return UN_KNOWN;	
			}
		}
		
		public int value() {
			return this.value;
		}
	}
}
