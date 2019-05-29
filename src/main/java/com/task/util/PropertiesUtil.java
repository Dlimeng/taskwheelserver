package com.task.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author limeng
 * @version 1.0
 * @date 2019/5/29 10:33
 */
public class PropertiesUtil {
    private String properiesName;
    public PropertiesUtil(String properiesName){
        this.properiesName = properiesName;
    }

   /* public String getPropery(String key){
        Properties prop = new Properties();
        InputStream is = null;
        try{
            is = TimerStart.class.getClassLoader().getResourceAsStream(this.properiesName);
            prop.load(is);
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            try{
                is.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return prop.getProperty(key);
    }*/
}
