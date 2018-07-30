package z.learn;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

@Component
public class TransactionUpdate2 {

    public void insert1() {
        jdbcTemplate.execute("insert into jdbc (name) values ('inner_insert1')");
    }

    public void insert1_1() {
        jdbcTemplate.execute("insert into jdbc (name) values ('inner_insert1_1')");
    }

    public void insert2() {
        transactionTemplate.execute(status -> {
            jdbcTemplate.execute("insert into jdbc (name) values ('inner_insert2')");
            status.setRollbackOnly();
            return status;
        });
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void insert3() {
        jdbcTemplate.execute("insert into jdbc (name) values ('inner_insert3')");

        transactionTemplate.execute((status) -> {
            jdbcTemplate.execute("insert into jdbc (name) values ('inner_insert3_2')");
            return status;
        });

        throw new RuntimeException("execption 3");
    }

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private TransactionTemplate transactionTemplate;
}
