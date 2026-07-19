package com.smartcampus.app.dao.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartcampus.contract.entity.News;
import com.smartcampus.contract.vo.NewsVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface NewsMapper extends BaseMapper<News> {

    @Select("""
            SELECT n.news_id, n.title, n.content, n.news_type, n.publisher_id,
                   COALESCE(u.real_name, u.username) AS publisher_name,
                   n.is_pinned, n.create_time
            FROM news n
            LEFT JOIN `user` u ON u.user_id = n.publisher_id
            WHERE (#{newsType} IS NULL OR #{newsType} = '' OR n.news_type = #{newsType})
              AND (#{keyword} IS NULL OR #{keyword} = ''
                   OR n.title LIKE CONCAT('%', #{keyword}, '%')
                   OR n.content LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY n.is_pinned DESC, n.create_time DESC
            """)
    IPage<NewsVo> selectVoPage(IPage<NewsVo> page,
                               @Param("newsType") String newsType,
                               @Param("keyword") String keyword);
}
