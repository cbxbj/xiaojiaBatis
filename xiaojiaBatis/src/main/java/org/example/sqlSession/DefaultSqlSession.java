package org.example.sqlSession;

import org.example.execute.Executor;
import org.example.pojo.Configuration;
import org.example.pojo.MappedStatement;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class DefaultSqlSession implements SqlSession {

    private final Configuration configuration;
    private final Executor executor;

    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }

    @Override
    public <E> List<E> selectList(String statementId, Object param) throws SQLException, IntrospectionException, NoSuchFieldException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementId);
        List<E> list = executor.query(configuration, mappedStatement, param);
        return list;
    }

    @Override
    public <T> T selectOne(String statementId, Object param) throws SQLException, IntrospectionException, NoSuchFieldException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {
        //调用selectList方法
        List<Object> list = selectList(statementId, param);
        if (list.size() == 1)
            return (T) list.get(0);
        else if (list.size() > 1)
            throw new RuntimeException("返回结果过多...");
        else
            return null;
    }

    /**
     * 生成代理对象
     */
    @Override
    public <T> T getMapper(Class<?> mapperClass) {
        //使用JDK动态代理生成代理对象
        Object proxyInstance = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{mapperClass}, new InvocationHandler() {
            /**
             * @param proxy 代理对象的引用
             * @param method 当前被调用的方法对象
             * @param args 被调用的方法参数
             */
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //具体逻辑,执行jdbc
                //思路:通过调用sqlSession的方法来完成执行
                //问题1:如何获取statementId:根据method获取
                Class<?> declaringClass = method.getDeclaringClass();//获取该方法所在的类
                String className = declaringClass.getName();//类名全路径
                String methodName = method.getName();//方法名
                String statementId = className + "." + methodName;
                MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementId);

                //问题2:该调用增删改查哪一个?
                String sqlCommandType = mappedStatement.getSqlCommandType();
                switch (sqlCommandType) {
                    case "select":
                        //查询操作
                        //问题3: selectOne、selectList
                        Class<?> returnType = method.getReturnType();
                        boolean assignableFrom = Collection.class.isAssignableFrom(returnType);
                        if (assignableFrom) {
                            //selectList
                            if (mappedStatement.getParameterType() != null)
                                return selectList(statementId, args[0]);
                            return selectList(statementId, null);
                        } else {
                            //selectOne
                            return selectOne(statementId, args[0]);
                        }
                    case "update":
                        //更新操作(增删改底层都是更新)
                    case "delete":
                        //删除操作
                    case "insert":
                        //插入操作
                }


                return null;
            }
        });
        return (T) proxyInstance;
    }
}
