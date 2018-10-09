package z.learn;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

/**
 * Connection：原生数据库连接。 DataSourceUtil中加入了针对 TransactionSynchronizationManager 绑定到线程的conn的处理
 * TransactionStatus：Spring定义的类，表示当前事务状态           ---- PlatformTransactionManager 中定义的事务操作commit，rollback的入参
 * DefaultTransactionStatus：具体的实现类， 持有Transaction对象
 * DataSourceTransactionObject：事务对象，持有数据库连接ConnectionHolder。继承自JdbcTransactionObjectSupport
 * <p>
 * TransactionAspectSupport.TransactionInfo                 ----AbstractPlatformTransactionManager中使用
 * |---- PlatformTransactionManager transactionManager;
 * |-----TransactionAttribute transactionAttribute;
 * |-----String joinpointIdentification;
 * |-----TransactionStatus transactionStatus;
 * |-----TransactionInfo oldTransactionInfo;
 * <p>
 * TransactionTemplate 使用 PlatformTransactionManager 获取事务 TransactionStatus（DefaultTransactionStatus）
 * TransactionAspectSupport 使用 PlatformTransactionManager 获取了 TransactionStatus 封装成 TransactionInfo
 */
@Component
public class TransactionUpdate1 implements InitializingBean {

    // JdbcTemplate will get connection from datasource directly, and has no automatic transaction control
    public void insert1() {
        jdbcTemplate.execute("insert into jdbc (name) values ('out_insert1')");

        update2.insert1();

        throw new RuntimeException("check");
    }

    /**
     * 被 TransactionAspectSupport 增强，会代理insert1_1方法。
     * 通过TransactionManager获取数据库连接conn，关闭autoCommit，设置isolation级别。
     * <p>
     * TransactionAspectSupport 被 TransactionInterceptor实现，通过适配MethodInterceptor，
     * 使 TransactionAspectSupport 支持了Spring Aop
     * <p>
     * jdbc.execute通过DataSourceUtil获取conn，没有事务控制。
     * DataSourceUtil会先检查 TransactionSynchronizationManager 是否为当前线程绑定了 connHolder，并获取到conn返回给调用
     * <p>
     * 在 TransactionAspectSupport 增强方法 invokeWithinTransaction 根据被增强方法执行结果决定commit或rollback
     */
    @Transactional
    public void insert1_1() {
        jdbcTemplate.execute("insert into jdbc (name) values ('outer_insert1_1')");

        update2.insert1_1();    // 方法内部同样使用jdbc.execute
        // 由于默认的传播规则，会获取到绑定在线程上的conn，从而实现了事务的传播。 调用方出现故障跑出异常通知 TransactionAspectSupport 后， 被调用的数据插入同样会被回滚掉

        throw new RuntimeException("check");
    }

    @Transactional
    public void insert2() {
        transactionTemplate.execute(status -> {
            jdbcTemplate.execute("insert into jdbc (name) values ('outer_insert2')");
            return status;
        });

        update2.insert2();
    }

    @Transactional
    public void insert3() {
        jdbcTemplate.execute("insert into jdbc (name) values ('outer_insert3')");

        update2.insert3();
    }

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private TransactionUpdate2 update2;
    private Integer count;

    @Override
    public void afterPropertiesSet() throws Exception {
        count = Integer.valueOf(150);
    }
}
