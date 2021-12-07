# xiaojiaBatis
mybatis(小贾版本)
# 原始JDBC

```java
public static void main(String[] args) {
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    User user = null;
    try {
        //1.注册驱动
        Class.forName("com.mysql.jdbc.Driver");
        //2.获取数据库连接
        con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mybatis", "root", "123456");
        //3.创建操作SQL对象
        String sql = "SELECT * FROM user WHERE username=? ";
        pstm = conn.prepareStatement(sql);
        //4.设置参数
        pstm.setString(1,"小贾");
        //5.执行sql语句，获取结果集
        rs = pstm.executeQuery();
        //6.获取结果集
        if (rs.next()) {
            //7.封装
            user = new User();
            user.setUid(rs.getString("uid"));
            user.setUsername(rs.getString("username"));
        }
    }catch (Exception e){
        throw new RuntimeException(e);
    }finally {
        JDBCUtils.close(conn,pstm,rs);
    }
}
```

## 问题分析

```text
1.硬编码问题
2.频繁创建释放数据库连接
3.需要手动封装返回结果集
```

## 解决办法

```text
1.配置文件
2.连接池
3.反射
```

# xiaojiaBatis

## 思路分析

| 使用端(项目)                                                 | 自定义持久层框架本身:本质对jdbc进行封装                      |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| 引入自定义持久层的jar包                                      | 1.加载配置文件<br />创建Resources类:负责加载配置文件,将配置文件加载成字节输入流,存到内存中<br />静态方法:InputSteam getResourceAsSteam(String path) |
| 创建SqlMapConfig.xml<br />数据库配置信息                     | 2.创建俩个JavaBean(容器对象)<br />Configuration:全局配置类,存储SqlMapConfig.xml配置文件解析出来的内容<br />MappedStatement:映射配置类,存储mapper.xml配置文件解析出来的内容 |
| 创建mapper.xml配置文件<br />sql配置信息(sql语句、参数类型、返回结果类型) | 3.解析配置文件,填充容器对象<br />创建SqlSessionFactoryBuilder类<br />方法: SqlSessionFactory build(InputSteam in);<br />(1)解析配置文件(dom4j + xpath),封装Configuration<br />(2)创建SqlSessionFactory对象 |
|                                                              | 4.创建SqlSessionFactory接口及实现类DefaultSqlSessionFactory<br />方法: SqlSession openSession(); |
|                                                              | 5.创建SqlSession接口及实现类DefaultSqlSession<br />方法: selectList(...); 查询所有<br />selectOne(...);查询单个<br />update();   delete(); |
|                                                              | 6.创建Execuotr接口及实现类SimpleExecuotr<br />方法: query(Configuration con,MappedStatement ms,Object param); <br />执行底层的JDBC代码 |

## 代码

### xiaojiaBatis_test

#### 依赖

```xml
	<properties>
        <!--Encoding-->
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <maven.compiler.encoding>UTF-8</maven.compiler.encoding>
        <java.version>8</java.version>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <!--引入自定义持久层框架的jar包-->
     <dependencies>
         <dependency>
             <groupId>org.example</groupId>
             <artifactId>xiaojiaBatis</artifactId>
             <version>1.0-SNAPSHOT</version>
         </dependency>
         <dependency>
             <groupId>org.projectlombok</groupId>
             <artifactId>lombok</artifactId>
             <version>1.18.20</version>
         </dependency>
     </dependencies>
```

#### xml

SqlMapConfig.xml

```xml
<configuration>

    <!--数据库配置信息-->
    <dataSource>
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://127.0.0.1:3306/mybatis"/>
        <property name="username" value="root"/>
        <property name="password" value="123456"/>
    </dataSource>

    <!--引入映射配置文件的路径-->
    <mappers>
        <mapper resource="mapper/UserMapper.xml"/>
        <mapper resource="mapper/ProductMapper.xml"/>
    </mappers>

</configuration>
```

Mapper.xml

```xml
<mapper namespace="org.example.mapper.UserMapper">

    <select id="selectList" resultType="org.example.pojo.User">
        select * from user
    </select>

    <select id="selectOne" resultType="org.example.pojo.User" parameterType="org.example.pojo.User">
        select * from user where id = #{id} and username = #{username}
    </select>

</mapper>
```

#### pojo

```java
@Data
public class User {
    private Integer id;
    private String username;
    private String address;
}
```

### xiaojiaBatis

#### 依赖

```xml
	<properties>
        <!--Encoding-->
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <maven.compiler.encoding>UTF-8</maven.compiler.encoding>
        <java.version>8</java.version>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>
        <!--mysql依赖-->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.6</version>
        </dependency>

        <!--junit依赖-->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
        </dependency>

        <!--dom4j依赖-->
        <dependency>
            <groupId>dom4j</groupId>
            <artifactId>dom4j</artifactId>
            <version>1.6.1</version>
        </dependency>

        <!--xpath依赖-->
        <dependency>
            <groupId>jaxen</groupId>
            <artifactId>jaxen</artifactId>
            <version>1.1.6</version>
        </dependency>

        <!--druid连接池-->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid</artifactId>
            <version>1.1.21</version>
        </dependency>

        <!--log日志-->
        <dependency>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
            <version>1.2.17</version>
        </dependency>

    </dependencies>
```

#### Resources

```java
package org.example.io;

import java.io.InputStream;

public class Resources {

    public static InputStream getResourceAsSteam(String path) {
        InputStream inputStream = Resources.class.getClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            throw new RuntimeException("配置文件加载不到");
        }
        return inputStream;
    }

}
```

#### MappedStatement

```java
package org.example.pojo;

public class MappedStatement {
    
    private String statementId;
    private String resultType;
    private String parameterType;
    private String sqlText;
    private String sqlCommandType;
    
}
```

#### Configuration

```java
package org.example.pojo;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class Configuration {

    private DataSource dataSource;
    private Map<String, MappedStatement> mappedStatementMap = new HashMap<>();

}
```

#### SqlSessionFactoryBuilder

```java
package org.example.sqlSession;

import org.dom4j.DocumentException;
import org.example.config.XMLConfigBuilder;
import org.example.pojo.Configuration;

import java.io.InputStream;

public class SqlSessionFactoryBuilder {

    public SqlSessionFactory build(InputStream inputStream) throws DocumentException {
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder();
        Configuration configuration = xmlConfigBuilder.parse(inputStream);
        return new DefaultSqlSessionFactory(configuration);
    }

}
```

#### XMLConfigBuilder

```java
package org.example.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.example.io.Resources;
import org.example.pojo.Configuration;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class XMLConfigBuilder {

    private final Configuration configuration;

    public XMLConfigBuilder() {
        this.configuration = new Configuration();
    }

    public Configuration parse(InputStream inputStream) throws DocumentException {
        Document document = new SAXReader().read(inputStream);
        Element rootElement = document.getRootElement();
        List<Element> propertyList = rootElement.selectNodes("//property");
        Properties properties = new Properties();
        for (Element element : propertyList) {
            String name = element.attributeValue("name");
            String value = element.attributeValue("value");
            properties.setProperty(name, value);
        }
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName(properties.getProperty("driverClassName"));
        druidDataSource.setUrl(properties.getProperty("url"));
        druidDataSource.setUsername(properties.getProperty("username"));
        druidDataSource.setPassword(properties.getProperty("password"));
        configuration.setDataSource(druidDataSource);
        List<Element> mapperList = rootElement.selectNodes("//mapper");
        for (Element element : mapperList) {
            String mapperPath = element.attributeValue("resource");
            InputStream resourceAsSteam = Resources.getResourceAsSteam(mapperPath);
            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(configuration);
            xmlMapperBuilder.parse(resourceAsSteam);
        }
        return configuration;
    }
}
```

#### XMLMapperBuilder

```java
package org.example.config;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.example.pojo.Configuration;
import org.example.pojo.MappedStatement;

import java.io.InputStream;
import java.util.List;

public class XMLMapperBuilder {
    private final Configuration configuration;

    public XMLMapperBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    public void parse(InputStream resourceAsSteam) throws DocumentException {
        Document document = new SAXReader().read(resourceAsSteam);
        Element rootElement = document.getRootElement();
        String namespace = rootElement.attributeValue("namespace");
        List<Element> selectList = rootElement.selectNodes("//select");
        for (Element element : selectList) {
            String id = element.attributeValue("id");
            String resultType = element.attributeValue("resultType");
            String parameterType = element.attributeValue("parameterType");
            String sql = element.getTextTrim();
            String statementId = namespace + "." + id;
            MappedStatement mappedStatement = new MappedStatement();
            mappedStatement.setStatementId(statementId);
            mappedStatement.setResultType(resultType);
            mappedStatement.setParameterType(parameterType);
            mappedStatement.setSqlText(sql);
            mappedStatement.setSqlCommandType("select");
            configuration.getMappedStatementMap().put(statementId, mappedStatement);
        }
    }
}
```

#### SqlSessionFactory

```java
package org.example.sqlSession;

public interface SqlSessionFactory {
    SqlSession openSession();
}
```

#### DefaultSqlSessionFactory

```java
package org.example.sqlSession;

import org.example.execute.Executor;
import org.example.execute.SimpleExecutor;
import org.example.pojo.Configuration;


public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        Executor executor=new SimpleExecutor();
        return new DefaultSqlSession(configuration,executor);
    }
}
```

#### Executor

```java
package org.example.execute;

import org.example.pojo.Configuration;
import org.example.pojo.MappedStatement;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

public interface Executor {
    <E> List<E> query(Configuration configuration, MappedStatement mappedStatement, Object param) throws Exception;
}
```

#### SimpleExecutor

```java
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
    @Override
    public <E> List<E> query(Configuration configuration, MappedStatement mappedStatement, Object param) throws Exception {
        Connection connection = configuration.getDataSource().getConnection();
        String sqlText = mappedStatement.getSqlText();
        BoundSql boundSql = getBoundSql(sqlText);
        PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getFinalSql());
        String parameterType = mappedStatement.getParameterType();
        if (parameterType != null) {
            Class<?> parameterTypeClass = Class.forName(parameterType);
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                String content = parameterMapping.getContent();
                Field declaredField = parameterTypeClass.getDeclaredField(content);
                declaredField.setAccessible(true);
                Object value = declaredField.get(param);
                preparedStatement.setObject(i + 1, value);
            }

        }
        ResultSet resultSet = preparedStatement.executeQuery();
        String resultType = mappedStatement.getResultType();
        Class<?> resultTypeClass = Class.forName(resultType);
        List<E> list = new ArrayList<>();
        while (resultSet.next()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            Object o = resultTypeClass.newInstance();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
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

    private BoundSql getBoundSql(String sqlText) {
        ParameterMappingTokenHandler parameterMappingTokenHandler = new ParameterMappingTokenHandler();
        GenericTokenParser genericTokenParser = new GenericTokenParser("#{", "}", parameterMappingTokenHandler);
        String finalSql = genericTokenParser.parse(sqlText);
        List<ParameterMapping> parameterMappings = parameterMappingTokenHandler.getParameterMappings();
        return new BoundSql(finalSql, parameterMappings);
    }
}
```

#### ParameterMappingTokenHandler

```java
/*
 *    Copyright 2009-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.example.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Clinton Begin
 */
public class ParameterMappingTokenHandler implements TokenHandler {

    private List<ParameterMapping> parameterMappings = new ArrayList<>();

    @Override
    public String handleToken(String content) {
        parameterMappings.add(buildParameterMapping(content));
        return "?";
    }

    private ParameterMapping buildParameterMapping(String content) {
        ParameterMapping parameterMapping = new ParameterMapping(content);
        return parameterMapping;
    }

    public List<ParameterMapping> getParameterMappings() {
        return parameterMappings;
    }
}
```

#### TokenHandler

```java
/*
 *    Copyright 2009-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.example.util;

/**
 * @author Clinton Begin
 */
public interface TokenHandler {
  String handleToken(String content);
}
```

#### GenericTokenParser

```java
/*
 *    Copyright 2009-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.example.util;

/**
 * @author Clinton Begin
 */
public class GenericTokenParser {

    private final String openToken;
    private final String closeToken;
    private final TokenHandler handler;

    public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
        this.openToken = openToken;
        this.closeToken = closeToken;
        this.handler = handler;
    }

    public String parse(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        // search open token
        int start = text.indexOf(openToken);
        if (start == -1) {
            return text;
        }
        char[] src = text.toCharArray();
        int offset = 0;
        final StringBuilder builder = new StringBuilder();
        StringBuilder expression = null;
        do {
            if (start > 0 && src[start - 1] == '\\') {
                // this open token is escaped. remove the backslash and continue.
                builder.append(src, offset, start - offset - 1).append(openToken);
                offset = start + openToken.length();
            } else {
                // found open token. let's search close token.
                if (expression == null) {
                    expression = new StringBuilder();
                } else {
                    expression.setLength(0);
                }
                builder.append(src, offset, start - offset);
                offset = start + openToken.length();
                int end = text.indexOf(closeToken, offset);
                while (end > -1) {
                    if (end > offset && src[end - 1] == '\\') {
                        // this close token is escaped. remove the backslash and continue.
                        expression.append(src, offset, end - offset - 1).append(closeToken);
                        offset = end + closeToken.length();
                        end = text.indexOf(closeToken, offset);
                    } else {
                        expression.append(src, offset, end - offset);
                        break;
                    }
                }
                if (end == -1) {
                    // close token was not found.
                    builder.append(src, start, src.length - start);
                    offset = src.length;
                } else {
                    builder.append(handler.handleToken(expression.toString()));
                    offset = end + closeToken.length();
                }
            }
            start = text.indexOf(openToken, offset);
        } while (start > -1);
        if (offset < src.length) {
            builder.append(src, offset, src.length - offset);
        }
        return builder.toString();
    }
}
```

#### ParameterMapping

```java
package org.example.util;

public class ParameterMapping {
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ParameterMapping(String content) {
        this.content = content;
    }
}
```

#### BoundSql

```java
package org.example.config;

import org.example.util.ParameterMapping;

import java.util.List;

public class BoundSql {
    private String finalSql;
    private List<ParameterMapping> parameterMappings;

    public BoundSql(String finalSql, List<ParameterMapping> parameterMappings) {
        this.finalSql = finalSql;
        this.parameterMappings = parameterMappings;
    }

    public String getFinalSql() {
        return finalSql;
    }

    public void setFinalSql(String finalSql) {
        this.finalSql = finalSql;
    }

    public List<ParameterMapping> getParameterMappings() {
        return parameterMappings;
    }

    public void setParameterMappings(List<ParameterMapping> parameterMappings) {
        this.parameterMappings = parameterMappings;
    }

}
```

#### SqlSession

```java
package org.example.sqlSession;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

public interface SqlSession {
  
    <E> List<E> selectList(String statementId, Object param) throws Exception;

    <T> T selectOne(String statementId, Object param) throws Exception;

    <T> T getMapper(Class<?> mapperClass);
}
```

#### DefaultSqlSession

```java
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
    public <E> List<E> selectList(String statementId, Object param) throws Exception {
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementId);
        List<E> list = executor.query(configuration, mappedStatement, param);
        return list;
    }

    @Override
    public <T> T selectOne(String statementId, Object param) throws Exception {
        List<Object> list = selectList(statementId, param);
        if (list.size() == 1)
            return (T) list.get(0);
        else if (list.size() > 1)
            throw new RuntimeException("返回结果过多...");
        else
            return null;
    }

    @Override
    public <T> T getMapper(Class<?> mapperClass) {
        Object proxyInstance = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{mapperClass}, new InvocationHandler() {
           
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Class<?> declaringClass = method.getDeclaringClass();
                String className = declaringClass.getName();
                String methodName = method.getName();
                String statementId = className + "." + methodName;
                MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementId);
                String sqlCommandType = mappedStatement.getSqlCommandType();
                switch (sqlCommandType) {
                    case "select":
                        Class<?> returnType = method.getReturnType();
                        boolean assignableFrom = Collection.class.isAssignableFrom(returnType);
                        if (assignableFrom) {
                            if (mappedStatement.getParameterType() != null)
                                return selectList(statementId, args[0]);
                            return selectList(statementId, null);
                        } else {
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
```

## 测试

```java
import org.dom4j.DocumentException;
import org.example.io.Resources;
import org.example.mapper.UserMapper;
import org.example.pojo.User;
import org.example.sqlSession.SqlSession;
import org.example.sqlSession.SqlSessionFactory;
import org.example.sqlSession.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

public class xiaojiaBatisTest {

    @Test
    public void test() throws Exception {
        InputStream resourceAsSteam = Resources.getResourceAsSteam("sqlMapConfig.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsSteam);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        List<User> list = sqlSession.selectList("org.example.mapper.UserMapper.selectList", null);
        for (User user : list) {
            System.out.println(user);
        }
        System.out.println("--------------------------------------------------");
        User user = new User();
        user.setId(1);
        user.setUsername("小贾");
        User one = sqlSession.selectOne("org.example.mapper.UserMapper.selectOne", user);
        System.out.println(one);
    }

    @Test
    public void test1() throws Exception {
        InputStream resourceAsSteam = Resources.getResourceAsSteam("sqlMapConfig.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsSteam);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper mapperProxy = sqlSession.getMapper(UserMapper.class);
        List<User> list = mapperProxy.selectList();
        for (User user : list) {
            System.out.println(user);
        }
        System.out.println("--------------------------------------------------");
        User user = new User();
        user.setId(1);
        user.setUsername("小贾");
        User one = mapperProxy.selectOne(user);
        System.out.println(one);
    }
}
```
