package org.example.execute;

import org.example.config.BoundSql;
import org.example.pojo.Configuration;
import org.example.pojo.MappedStatement;
import org.example.util.GenericTokenParser;
import org.example.util.ParameterMapping;
import org.example.util.ParameterMappingTokenHandler;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleExecutor implements Executor {
    /**
     * 执行jdbc操作
     */
    @Override
    public <E> List<E> query(Configuration configuration, MappedStatement mappedStatement, Object param) throws SQLException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException, IntrospectionException, InstantiationException, InvocationTargetException {
        //1.加载驱动,获得取数据库连接
        Connection connection = configuration.getDataSource().getConnection();
        //2.获取预编译对象 PrepareStatement
        //select * from user where id = #{id} and username = #{username}
        //替换占位符:
        // (1)#{}替换成?
        // (2)#{}里面的值保存下来
        //select * from user where id = ? and username = ?
        //#{}里面的值存到了BoundSql对象list集合中
        String sqlText = mappedStatement.getSqlText();
        BoundSql boundSql = getBoundSql(sqlText);
        PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getFinalSql());


        //3.设置参数
        //问题1:Object param(类型不确定)
        String parameterType = mappedStatement.getParameterType();
        if (parameterType != null) {
            Class<?> parameterTypeClass = Class.forName(parameterType);
            //问题2: 该把对象中的哪一个属性赋值给哪一个占位符呢
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                //#{}里面的值
                String content = parameterMapping.getContent();
                Field declaredField = parameterTypeClass.getDeclaredField(content);
                //暴力反射
                declaredField.setAccessible(true);
                Object value = declaredField.get(param);
                preparedStatement.setObject(i + 1, value);
            }

        }
        //4.执行sql
        ResultSet resultSet = preparedStatement.executeQuery();
        //5.封装返回结果集
        //问题1:要封装到哪个对象
        String resultType = mappedStatement.getResultType();
        Class<?> resultTypeClass = Class.forName(resultType);
        List<E> list = new ArrayList<>();
        while (resultSet.next()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            Object o = resultTypeClass.newInstance();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                //id username
                String columnName = metaData.getColumnName(i);
                Object columnValue = resultSet.getObject(columnName);

                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(columnName, resultTypeClass);
                Method writeMethod = propertyDescriptor.getWriteMethod();
                writeMethod.invoke(o, columnValue);

            }
            list.add((E) o);
        }

        return list;
    }

    //select * from user where id = #{id} and username = #{username}
    //替换占位符:
    // (1)#{}替换成?
    // (2)#{}里面的值保存下来
    private BoundSql getBoundSql(String sqlText) {
        //标记处理器:配合标记解析器完成标记的解析工作
        ParameterMappingTokenHandler parameterMappingTokenHandler = new ParameterMappingTokenHandler();
        GenericTokenParser genericTokenParser = new GenericTokenParser("#{", "}", parameterMappingTokenHandler);
        //带有?的sql语句
        String finalSql = genericTokenParser.parse(sqlText);
        List<ParameterMapping> parameterMappings = parameterMappingTokenHandler.getParameterMappings();

        return new BoundSql(finalSql, parameterMappings);
    }
}
