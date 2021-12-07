package org.example.sqlSession;

import org.dom4j.DocumentException;
import org.example.config.XMLConfigBuilder;
import org.example.pojo.Configuration;

import java.io.InputStream;

public class SqlSessionFactoryBuilder {

    /**
     * (1)配置文件解析,封装Configuration
     * (2)创建SqlSessionFactory实现类对象
     */
    public SqlSessionFactory build(InputStream inputStream) throws DocumentException {
        //解析核心配置文件
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder();
        Configuration configuration = xmlConfigBuilder.parse(inputStream);

        //创建SqlSessionFactory实现类对象
        return new DefaultSqlSessionFactory(configuration);
    }

}
