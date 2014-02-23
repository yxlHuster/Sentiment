package com.hot.cmt.comment.util;


/**
 * 单例模式config
 * @author yongleixiao
 *
 */
public class ConfigFactory {

    private static Config config = new Config("classpath*:*.properties");
    
    public static Config config() {
        return config;
    }
}