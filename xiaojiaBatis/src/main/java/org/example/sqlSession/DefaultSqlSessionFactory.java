package org.example.sqlSession;

import org.example.execute.Executor;
import org.example.execute.SimpleExecutor;
import org.example.pojo.Configuration;


public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 创建出sqlSession
     */
    @Override
    public SqlSession openSession() {
        //创建执行器对象
        Executor executor=new SimpleExecutor();

        return new DefaultSqlSession(configuration,executor);
    }
}
