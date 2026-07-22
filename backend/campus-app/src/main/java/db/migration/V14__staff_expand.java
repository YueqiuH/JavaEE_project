package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.Statement;

public class V14__staff_expand extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        exec(connection, "SET @pwd = '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy'");
        exec(connection, "SET @staff_role = (SELECT role_id FROM role WHERE role_code = 'STAFF' LIMIT 1)");

        // 计算机与人工智能学院（dept_id=1）
        exec(connection, """
            INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
            ('200010', @pwd, 2, '沈默然', 1, '13901001010', 'shenmr@campus.edu.cn', '教授', '副院长', 1, 1),
            ('200011', @pwd, 2, '蒋知远', 1, '13901001011', 'jiangzy@campus.edu.cn', '副教授', '教研室主任', 1, 1),
            ('200012', @pwd, 2, '韩雨桐', 2, '13901001012', 'hanyt@campus.edu.cn', '讲师', NULL, 1, 0)
        """);
        exec(connection, "INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200010','200011','200012')");

        // 经济管理学院（dept_id=2）
        exec(connection, """
            INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
            ('200020', @pwd, 2, '孟令仪', 2, '13902002020', 'mengly@campus.edu.cn', '教授', '院长', 2, 1),
            ('200021', @pwd, 2, '唐逸飞', 1, '13902002021', 'tangyf@campus.edu.cn', '副教授', '系主任', 2, 1),
            ('200022', @pwd, 2, '任晓楠', 2, '13902002022', 'renxn@campus.edu.cn', '讲师', NULL, 2, 1),
            ('200023', @pwd, 3, '丁建平', 1, '13902002023', 'dingjp@campus.edu.cn', NULL, '教务秘书', 2, 1),
            ('200024', @pwd, 2, '吕思源', 1, '13902002024', 'lvsy@campus.edu.cn', '助教', NULL, 2, 0)
        """);
        exec(connection, "INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200020','200021','200022','200023','200024')");

        // 外国语学院（dept_id=3）
        exec(connection, """
            INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
            ('200030', @pwd, 2, '顾婉清', 2, '13903003030', 'guwq@campus.edu.cn', '教授', '院长', 3, 1),
            ('200031', @pwd, 2, '温博文', 1, '13903003031', 'wenbw@campus.edu.cn', '副教授', '系主任', 3, 1),
            ('200032', @pwd, 2, '叶知秋', 2, '13903003032', 'yezq@campus.edu.cn', '讲师', NULL, 3, 1),
            ('200033', @pwd, 2, '姚锦程', 1, '13903003033', 'yaojc@campus.edu.cn', '讲师', NULL, 3, 1),
            ('200034', @pwd, 3, '常雅琴', 2, '13903003034', 'changyq@campus.edu.cn', NULL, '行政助理', 3, 1),
            ('200035', @pwd, 3, '傅长庚', 1, '13903003035', 'fucg@campus.edu.cn', NULL, '实验室管理员', 3, 1),
            ('200036', @pwd, 2, '乔若兰', 2, '13903003036', 'qiaorl@campus.edu.cn', '助教', NULL, 3, 0)
        """);
        exec(connection, "INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200030','200031','200032','200033','200034','200035','200036')");

        // 机械工程学院（dept_id=4）
        exec(connection, """
            INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
            ('200040', @pwd, 2, '谭景行', 1, '13904004040', 'tanjx@campus.edu.cn', '教授', '副院长', 4, 1),
            ('200041', @pwd, 2, '崔明哲', 1, '13904004041', 'cuimz@campus.edu.cn', '副教授', '系主任', 4, 1),
            ('200042', @pwd, 2, '万秋怡', 2, '13904004042', 'wanqy@campus.edu.cn', '副教授', NULL, 4, 1),
            ('200043', @pwd, 2, '石振华', 1, '13904004043', 'shizh@campus.edu.cn', '讲师', NULL, 4, 1),
            ('200044', @pwd, 3, '龙晓琳', 2, '13904004044', 'longxl@campus.edu.cn', NULL, '教务秘书', 4, 1),
            ('200045', @pwd, 3, '熊志远', 1, '13904004045', 'xiongzy@campus.edu.cn', NULL, '实验员', 4, 1),
            ('200046', @pwd, 2, '向美琪', 2, '13904004046', 'xiangmq@campus.edu.cn', '助教', NULL, 4, 0)
        """);
        exec(connection, "INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200040','200041','200042','200043','200044','200045','200046')");

        // 数学与统计学院（dept_id=5）
        exec(connection, """
            INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
            ('200050', @pwd, 2, '龚明远', 1, '13905005050', 'gongmy@campus.edu.cn', '教授', '院长', 5, 1),
            ('200051', @pwd, 2, '廖雨菲', 2, '13905005051', 'liaoyf@campus.edu.cn', '副教授', '系主任', 5, 1),
            ('200052', @pwd, 2, '范立诚', 1, '13905005052', 'fanlc@campus.edu.cn', '讲师', NULL, 5, 1),
            ('200053', @pwd, 2, '白露晞', 2, '13905005053', 'bailx@campus.edu.cn', '讲师', NULL, 5, 1),
            ('200054', @pwd, 3, '牛秋实', 1, '13905005054', 'niuqs@campus.edu.cn', NULL, '行政助理', 5, 1),
            ('200055', @pwd, 2, '骆天宇', 1, '13905005055', 'luoty@campus.edu.cn', '助教', NULL, 5, 1)
        """);
        exec(connection, "INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200050','200051','200052','200053','200054','200055')");

        // 艺术设计学院（dept_id=6）
        exec(connection, """
            INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
            ('200060', @pwd, 2, '夏柳依', 2, '13906006060', 'xialy@campus.edu.cn', '教授', '院长', 6, 1),
            ('200061', @pwd, 2, '费玉成', 1, '13906006061', 'feiyc@campus.edu.cn', '副教授', '系主任', 6, 1),
            ('200062', @pwd, 2, '褚明霞', 2, '13906006062', 'chumx@campus.edu.cn', '副教授', NULL, 6, 1),
            ('200063', @pwd, 2, '尤子涵', 1, '13906006063', 'youzh@campus.edu.cn', '讲师', NULL, 6, 1),
            ('200064', @pwd, 3, '银晓燕', 2, '13906006064', 'yinxy@campus.edu.cn', NULL, '教务秘书', 6, 1),
            ('200065', @pwd, 3, '查永康', 1, '13906006065', 'chayk@campus.edu.cn', NULL, '设备管理员', 6, 1),
            ('200066', @pwd, 2, '米思琪', 2, '13906006066', 'misq@campus.edu.cn', '助教', NULL, 6, 0)
        """);
        exec(connection, "INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200060','200061','200062','200063','200064','200065','200066')");

        // 法学院（dept_id=16）
        exec(connection, """
            INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
            ('200160', @pwd, 2, '简青松', 1, '13916016016', 'jianqs@campus.edu.cn', '教授', '院长', 16, 1),
            ('200161', @pwd, 2, '岳思娴', 2, '13916016017', 'yuesx@campus.edu.cn', '教授', '副院长', 16, 1),
            ('200162', @pwd, 2, '荣博韬', 1, '13916016018', 'rongbt@campus.edu.cn', '副教授', '系主任', 16, 1),
            ('200163', @pwd, 2, '池映月', 2, '13916016019', 'chiyy@campus.edu.cn', '副教授', NULL, 16, 1),
            ('200164', @pwd, 2, '巫承志', 1, '13916016020', 'wucz@campus.edu.cn', '讲师', NULL, 16, 1),
            ('200165', @pwd, 2, '娄晓晴', 2, '13916016021', 'louxq@campus.edu.cn', '讲师', NULL, 16, 1),
            ('200166', @pwd, 3, '瞿建华', 1, '13916016022', 'qujh@campus.edu.cn', NULL, '教务秘书', 16, 1),
            ('200167', @pwd, 3, '鄢秋月', 2, '13916016023', 'yanqy@campus.edu.cn', NULL, '资料管理员', 16, 1),
            ('200168', @pwd, 2, '冼文博', 1, '13916016024', 'xianwb@campus.edu.cn', '助教', NULL, 16, 0)
        """);
        exec(connection, "INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200160','200161','200162','200163','200164','200165','200166','200167','200168')");

        // 土木工程学院（dept_id=17）
        exec(connection, """
            INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
            ('200170', @pwd, 2, '洪锦华', 1, '13917017017', 'hongjh@campus.edu.cn', '教授', '院长', 17, 1),
            ('200171', @pwd, 2, '寇志远', 1, '13917017018', 'kouzy@campus.edu.cn', '教授', '副院长', 17, 1),
            ('200172', @pwd, 2, '丛慧心', 2, '13917017019', 'conghx@campus.edu.cn', '副教授', '系主任', 17, 1),
            ('200173', @pwd, 2, '焦文龙', 1, '13917017020', 'jiaowl@campus.edu.cn', '副教授', NULL, 17, 1),
            ('200174', @pwd, 2, '甄子萱', 2, '13917017021', 'zhenzx@campus.edu.cn', '讲师', NULL, 17, 1),
            ('200175', @pwd, 2, '郎思远', 1, '13917017022', 'langsy@campus.edu.cn', '讲师', NULL, 17, 1),
            ('200176', @pwd, 3, '楚云飞', 1, '13917017023', 'chuyf@campus.edu.cn', NULL, '实验员', 17, 1),
            ('200177', @pwd, 3, '黎若兰', 2, '13917017024', 'lirl@campus.edu.cn', NULL, '教务秘书', 17, 1),
            ('200178', @pwd, 2, '蓝一鸣', 1, '13917017025', 'lanym@campus.edu.cn', '助教', NULL, 17, 0)
        """);
        exec(connection, "INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200170','200171','200172','200173','200174','200175','200176','200177','200178')");

        // 电子信息学院（dept_id=18）
        exec(connection, """
            INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
            ('200180', @pwd, 2, '武俊彦', 1, '13918018018', 'wujy@campus.edu.cn', '教授', '院长', 18, 1),
            ('200181', @pwd, 2, '楼玉兰', 2, '13918018019', 'louyl@campus.edu.cn', '教授', '副院长', 18, 1),
            ('200182', @pwd, 2, '欧锦程', 1, '13918018020', 'oujc@campus.edu.cn', '副教授', '系主任', 18, 1),
            ('200183', @pwd, 2, '甄雅琴', 2, '13918018021', 'zhenyq@campus.edu.cn', '副教授', NULL, 18, 1),
            ('200184', @pwd, 2, '蒋思源', 1, '13918018022', 'jiangsy@campus.edu.cn', '讲师', '教研室主任', 18, 1),
            ('200185', @pwd, 2, '贝晓楠', 2, '13918018023', 'beixn@campus.edu.cn', '讲师', NULL, 18, 1),
            ('200186', @pwd, 3, '崔建平', 1, '13918018024', 'cuijp@campus.edu.cn', NULL, '实验室主任', 18, 1),
            ('200187', @pwd, 3, '万雨桐', 2, '13918018025', 'wanyt@campus.edu.cn', NULL, '行政助理', 18, 1),
            ('200188', @pwd, 2, '耿博文', 1, '13918018026', 'gengbw@campus.edu.cn', '助教', NULL, 18, 0)
        """);
        exec(connection, "INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200180','200181','200182','200183','200184','200185','200186','200187','200188')");

        // 化学化工学院（dept_id=19）
        exec(connection, """
            INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
            ('200190', @pwd, 2, '钱玉成', 1, '13919019019', 'qianyc@campus.edu.cn', '教授', '院长', 19, 1),
            ('200191', @pwd, 2, '薛若兰', 2, '13919019020', 'xuerl@campus.edu.cn', '教授', '副院长', 19, 1),
            ('200192', @pwd, 2, '于立诚', 1, '13919019021', 'yulc@campus.edu.cn', '副教授', '系主任', 19, 1),
            ('200193', @pwd, 2, '东方明霞', 2, '13919019022', 'dongfmx@campus.edu.cn', '副教授', NULL, 19, 1),
            ('200194', @pwd, 2, '邱思远', 1, '13919019023', 'qiusy@campus.edu.cn', '讲师', NULL, 19, 1),
            ('200195', @pwd, 3, '佘建华', 1, '13919019024', 'shejh@campus.edu.cn', NULL, '实验员', 19, 1),
            ('200196', @pwd, 3, '兰美琪', 2, '13919019025', 'lanmq@campus.edu.cn', NULL, '教务秘书', 19, 1),
            ('200197', @pwd, 2, '全子轩', 1, '13919019026', 'quanzx@campus.edu.cn', '助教', NULL, 19, 0)
        """);
        exec(connection, "INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200190','200191','200192','200193','200194','200195','200196','200197')");

        // 生命科学学院（dept_id=20）
        exec(connection, """
            INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
            ('200200', @pwd, 2, '成秋怡', 2, '13920020020', 'chengqy@campus.edu.cn', '教授', '院长', 20, 1),
            ('200201', @pwd, 2, '葛承志', 1, '13920020021', 'gecz@campus.edu.cn', '教授', '副院长', 20, 1),
            ('200202', @pwd, 2, '曾子涵', 2, '13920020022', 'zengzh@campus.edu.cn', '副教授', '系主任', 20, 1),
            ('200203', @pwd, 2, '邵明哲', 1, '13920020023', 'shaomz@campus.edu.cn', '副教授', NULL, 20, 1),
            ('200204', @pwd, 2, '栾晓琳', 2, '13920020024', 'luanxl@campus.edu.cn', '讲师', NULL, 20, 1),
            ('200205', @pwd, 2, '别天宇', 1, '13920020025', 'biety@campus.edu.cn', '讲师', NULL, 20, 1),
            ('200206', @pwd, 3, '燕俊彦', 1, '13920020026', 'yanjy@campus.edu.cn', NULL, '实验员', 20, 1),
            ('200207', @pwd, 3, '赫秋实', 2, '13920020027', 'heqs@campus.edu.cn', NULL, '教务秘书', 20, 1)
        """);
        exec(connection, "INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200200','200201','200202','200203','200204','200205','200206','200207')");

        // 教育学院（dept_id=21）
        exec(connection, """
            INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
            ('200210', @pwd, 2, '敖思远', 1, '13921021021', 'aosy@campus.edu.cn', '教授', '院长', 21, 1),
            ('200211', @pwd, 2, '安柳依', 2, '13921021022', 'anly@campus.edu.cn', '教授', '副院长', 21, 1),
            ('200212', @pwd, 2, '冷博韬', 1, '13921021023', 'lengbt@campus.edu.cn', '副教授', '系主任', 21, 1),
            ('200213', @pwd, 2, '池玉兰', 2, '13921021024', 'chiyl@campus.edu.cn', '副教授', NULL, 21, 1),
            ('200214', @pwd, 2, '乐晓楠', 2, '13921021025', 'lexn@campus.edu.cn', '讲师', NULL, 21, 1),
            ('200215', @pwd, 2, '巴锦程', 1, '13921021026', 'bajc@campus.edu.cn', '讲师', NULL, 21, 1),
            ('200216', @pwd, 3, '宗建华', 1, '13921021027', 'zongjh@campus.edu.cn', NULL, '行政助理', 21, 1),
            ('200217', @pwd, 2, '游思琪', 2, '13921021028', 'yousq@campus.edu.cn', '助教', NULL, 21, 0)
        """);
        exec(connection, "INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200210','200211','200212','200213','200214','200215','200216','200217')");

        // 新闻传播学院（dept_id=22）
        exec(connection, """
            INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
            ('200220', @pwd, 2, '徐雨桐', 2, '13922022022', 'xuyt@campus.edu.cn', '教授', '院长', 22, 1),
            ('200221', @pwd, 2, '国锦华', 1, '13922022023', 'guojh@campus.edu.cn', '教授', '副院长', 22, 1),
            ('200222', @pwd, 2, '邢明霞', 2, '13922022024', 'xingmx@campus.edu.cn', '副教授', '系主任', 22, 1),
            ('200223', @pwd, 2, '哈志远', 1, '13922022025', 'hazy@campus.edu.cn', '副教授', NULL, 22, 1),
            ('200224', @pwd, 2, '曾立诚', 1, '13922022026', 'zenglc@campus.edu.cn', '讲师', NULL, 22, 1),
            ('200225', @pwd, 3, '鄂若兰', 2, '13922022027', 'erl@campus.edu.cn', NULL, '教务秘书', 22, 1),
            ('200226', @pwd, 3, '楼玉成', 1, '13922022028', 'louyc@campus.edu.cn', NULL, '设备管理员', 22, 1),
            ('200227', @pwd, 2, '昝晓晴', 2, '13922022029', 'zanxq@campus.edu.cn', '助教', NULL, 22, 0)
        """);
        exec(connection, "INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200220','200221','200222','200223','200224','200225','200226','200227')");

        // 医学院（dept_id=23）
        exec(connection, """
            INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
            ('200230', @pwd, 2, '钟景行', 1, '13923023023', 'zhongjx@campus.edu.cn', '教授', '院长', 23, 1),
            ('200231', @pwd, 2, '巴慧心', 2, '13923023024', 'bahx@campus.edu.cn', '教授', '副院长', 23, 1),
            ('200232', @pwd, 2, '江明哲', 1, '13923023025', 'jiangmz@campus.edu.cn', '副教授', '系主任', 23, 1),
            ('200233', @pwd, 2, '卜玉兰', 2, '13923023026', 'buyl@campus.edu.cn', '副教授', '教研室主任', 23, 1),
            ('200234', @pwd, 2, '邢文龙', 1, '13923023027', 'xingwl@campus.edu.cn', '讲师', NULL, 23, 1),
            ('200235', @pwd, 2, '养秋怡', 2, '13923023028', 'yangqy@campus.edu.cn', '讲师', NULL, 23, 1),
            ('200236', @pwd, 3, '招俊彦', 1, '13923023029', 'zhaojy@campus.edu.cn', NULL, '实验室主任', 23, 1),
            ('200237', @pwd, 3, '荆晓琳', 2, '13923023030', 'jingxl@campus.edu.cn', NULL, '教务秘书', 23, 1),
            ('200238', @pwd, 2, '东郭天宇', 1, '13923023031', 'donggty@campus.edu.cn', '助教', NULL, 23, 0)
        """);
        exec(connection, "INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200230','200231','200232','200233','200234','200235','200236','200237','200238')");
    }

    private static void exec(Connection connection, String sql) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}
