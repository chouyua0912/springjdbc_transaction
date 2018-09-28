package z.learn.springmybatis;

import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper {

    @Select("SELECT * FROM user_info WHERE id = #{userId}")
    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    User getUser(@Param("userId") Integer userId);
}
