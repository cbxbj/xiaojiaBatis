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
    public void test() throws DocumentException, SQLException, IntrospectionException, NoSuchFieldException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {
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

    /**
     * 代理对象
     */
    @Test
    public void test1() throws DocumentException {
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
