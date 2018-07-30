package z.learn;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

public class TransactionDemo {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("context.xml");
        JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);

        TransactionUpdate1 update1 = context.getBean(TransactionUpdate1.class);
        //  execute(() -> update1.insert1());
        //  execute(() -> update1.insert1_1());
        //  execute(() -> update1.insert2());
        execute(() -> update1.insert3());

        jdbc.query("select * from jdbc", rs -> {
            System.out.println(rs.getInt(1) + " " + rs.getString(2));
        });

        jdbc.execute("delete from jdbc");
    }

    private static void execute(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {

        }
    }
}
