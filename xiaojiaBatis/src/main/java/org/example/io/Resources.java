package org.example.io;

import java.io.InputStream;

public class Resources {

    /**
     * 根据配置文件路径,将配置文件加载成字节输入流,存到内存中
     *
     * @param path 配置文件路径
     * @return 配置文件输入流
     */
    public static InputStream getResourceAsSteam(String path) {
        InputStream inputStream = Resources.class.getClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            throw new RuntimeException("配置文件加载不到");
        }
        return inputStream;
    }

}
