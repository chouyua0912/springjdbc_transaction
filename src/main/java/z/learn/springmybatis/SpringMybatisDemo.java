package z.learn.springmybatis;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.support.*;

public class SpringMybatisDemo {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("context.xml");
        UserMapper userMapper = context.getBean(UserMapper.class);

        TransactionTemplate template = context.getBean(TransactionTemplate.class);
        SqlSessionFactory sessionFactory = context.getBean(SqlSessionFactory.class);

        template.execute((status) -> {
            User u = userMapper.getUser(1);
            System.out.println(u);

            User u2 = userMapper.getUser(1);
            System.out.println(u2);
            return true;
        });
    }

    private static void clearFirstLevelCache(SqlSessionFactory sessionFactory) {
        /**清空一级缓存**/
        DefaultSqlSession session = (DefaultSqlSession) SqlSessionUtils.getSqlSession(sessionFactory);
        session.clearCache();
    }
}
