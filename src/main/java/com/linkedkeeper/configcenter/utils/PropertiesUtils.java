package com.linkedkeeper.configcenter.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public final class PropertiesUtils {

    // 根据 Key 获取 value
    public static String readValue(String filePath, String key) throws Exception {
        Properties props = new Properties();

        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(filePath));
            props.load(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return props.getProperty(key);
    }

    // 读取 properties 的全部信息
    public static Map<String, String> readProperties(String filePath) throws IOException {
        Properties props = new Properties();

        Map<String, String> result = new HashMap<String, String>();
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(filePath));
            props.load(in);

            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                String key = entry.getKey().toString().trim();
                String property = entry.getValue().toString().trim();
                result.put(key, property);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return result;
    }

    // 写入 properties 信息
    public static void writeProperties(String filePath, Map<String, String> parameters) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }

        Properties prop = new Properties();

        InputStream fis = null;
        OutputStream fos = null;
        try {
            fis = new FileInputStream(file);
            // 从输入流读取属性列表(键和元素对)
            prop.load(fis);

            // 强制要求为属性饿键和值使用字符串,返回是 HastTable 调用 put 的结果
            fos = new FileOutputStream(filePath);
            for (Iterator<String> it = parameters.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                prop.setProperty(key.trim(), parameters.get(key).trim());
            }
            prop.store(fos, "DataSync Snapshot");
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }
}
