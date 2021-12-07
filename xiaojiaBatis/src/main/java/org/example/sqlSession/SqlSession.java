package org.example.sqlSession;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

public interface SqlSession {
    /**
     * 查询所有用户
     */
    <E> List<E> selectList(String statementId, Object param) throws SQLException, IntrospectionException, NoSuchFieldException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException;

    /**
     * 查询单个
     */
    <T> T selectOne(String statementId, Object param) throws SQLException, IntrospectionException, NoSuchFieldException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException;

    /**
     * 获取代理对象
     */
    <T> T getMapper(Class<?> mapperClass);
}
