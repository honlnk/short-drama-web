package com.shortdrama.api.config;

import com.shortdrama.api.entity.Drama;
import com.shortdrama.api.entity.DramaStatus;
import com.shortdrama.api.entity.Episode;
import com.shortdrama.api.entity.User;
import com.shortdrama.api.entity.UserRole;
import com.shortdrama.api.repository.DramaRepository;
import com.shortdrama.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 空库种子导入：仅当用户表为空时执行，幂等。
 * 媒体文件由 scripts/allocate-seed-media.sh 落位 apps/api/uploads/，
 * 这里的 video_url / cover_url 与其一一对应（相对路径，经 /uploads/** 静态映射提供）。
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    /** 剧名 / 简介 / 分类 / 点赞数（定值，保证演示数据稳定）/ 各集标题 / 各集时长（与素材实际时长一致） */
    private record SeedDrama(String title, String description, String category, int likeCount,
                             String[] episodeTitles, int[] durations) {
    }

    private static final List<SeedDrama> SEED_DRAMAS = List.of(
            new SeedDrama("闪婚老公是豪门",
                    "被家人逼婚的普通女孩与神秘男人闪婚领证，婚后才发现丈夫竟是隐藏的豪门继承人。契约婚姻步步真心，甜宠升级不断。",
                    "甜宠", 2461,
                    new String[]{"闪婚领证", "假戏真做", "心动失控", "身份曝光"}, new int[]{20, 13, 14, 15}),
            new SeedDrama("都市之最强狂兵",
                    "昔日兵王隐姓埋名回归都市，本想低调生活，却屡屡被卷入是非。扮猪吃虎一路碾压，谁都别想动他在乎的人。",
                    "都市", 1987,
                    new String[]{"王者归来", "旧怨重提", "强势出手", "一战立威"}, new int[]{17, 12, 23, 27}),
            new SeedDrama("重生之逆袭人生",
                    "重生回到十年前，带着记忆逆天改命。商海布局步步为营，前世仇人一个都跑不掉，打脸反转一路爽到底。",
                    "逆袭", 3258,
                    new String[]{"重生觉醒", "暗中布局", "商战翻盘", "当面打脸"}, new int[]{13, 10, 15, 11}),
            new SeedDrama("王爷的替嫁王妃",
                    "姐姐逃婚，她被迫替嫁给传闻中残暴的王爷。深宅大院暗流涌动，她凭智慧步步为营，从弃子逆袭为当家主母。",
                    "古装", 2876,
                    new String[]{"替嫁入府", "暗流涌动", "步步为营", "绝地反杀"}, new int[]{12, 11, 15, 11}),
            new SeedDrama("午夜出租车",
                    "午夜过后的城市，一辆出租车接送着不该存在的乘客。每个订单背后都是一桩都市怪谈，连环反转直到最后一集。",
                    "悬疑", 963,
                    new String[]{"深夜首单", "诡异乘客", "车内有鬼", "真相大白"}, new int[]{12, 12, 14, 11}),
            new SeedDrama("霸总的小娇妻",
                    "一场误会让她与冷酷总裁纠缠不清，阴差阳错开始同居。嘴上说不要心里却诚实，高冷霸总的追妻之路笑料百出。",
                    "甜宠", 1752,
                    new String[]{"误会重逢", "被迫同居", "醋意大发", "追妻火葬场"}, new int[]{8, 11, 12, 15})
    );

    private final UserRepository userRepository;
    private final DramaRepository dramaRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataSeeder(UserRepository userRepository, DramaRepository dramaRepository) {
        this.userRepository = userRepository;
        this.dramaRepository = dramaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("数据已存在，跳过种子导入");
            return;
        }
        LocalDateTime now = LocalDateTime.now();

        User admin = newUser("admin", "admin123", "管理员", UserRole.ADMIN, now);
        User demo = newUser("demo", "demo123", "演示用户", UserRole.USER, now);

        int dramaNo = 0;
        for (SeedDrama seed : SEED_DRAMAS) {
            dramaNo++;
            Drama drama = new Drama();
            drama.setTitle(seed.title());
            drama.setDescription(seed.description());
            drama.setCategory(seed.category());
            drama.setCoverUrl("/uploads/covers/drama-%02d.svg".formatted(dramaNo));
            drama.setLikeCount(seed.likeCount());
            drama.setStatus(DramaStatus.ONLINE);
            drama.setCreatedAt(now);

            for (int i = 0; i < seed.episodeTitles().length; i++) {
                Episode episode = new Episode();
                episode.setDrama(drama);
                episode.setEpisodeNo(i + 1);
                episode.setTitle("第%d集 %s".formatted(i + 1, seed.episodeTitles()[i]));
                episode.setVideoUrl("/uploads/videos/drama-%02d/ep-%02d.mp4".formatted(dramaNo, i + 1));
                episode.setDurationSec(seed.durations()[i]);
                drama.getEpisodes().add(episode);
            }
            dramaRepository.save(drama);
        }

        log.info("种子导入完成：{} 部短剧 × 各 {} 集，账号 admin({}) / demo({})",
                SEED_DRAMAS.size(), SEED_DRAMAS.get(0).episodeTitles().length,
                admin.getRole(), demo.getRole());
    }

    private User newUser(String username, String rawPassword, String nickname, UserRole role, LocalDateTime now) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNickname(nickname);
        user.setRole(role);
        user.setCreatedAt(now);
        return userRepository.save(user);
    }
}
