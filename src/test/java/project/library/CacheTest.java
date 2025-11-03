//package project.library;
//
//
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import project.library.infrastructure.SearchHistoryImpl;
//import project.library.infrastructure.SearchHistoryPort;
//import project.library.repository.LibraryJpaRepository;
//import project.library.repository.LibraryRepository;
//import project.library.repository.LibraryRepositoryJpaAdapter;
//import project.library.repository.collection.Library;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.Executor;
//
//@SpringBootTest
//public class CacheTest {
//
//    @Autowired LibraryJpaRepository jpaRepository;
//
//    @Autowired StringRedisTemplate redisTemplate;
//
//    @Test
//    void 속도측정(){
//
//        Set<String> keys = redisTemplate.keys("library:*");
//        if (keys != null) {
//            keys.forEach(System.out::println);
//        }
//    }
//
//
//
//    @Test
//    void 최근검색(){
//
//        SearchHistoryPort searchHistory=new SearchHistoryImpl(redisTemplate);
//
////        searchHistory.addHistory("test4","title33");
////        searchHistory.addHistory("test5","title1");
////        searchHistory.addHistory("test6","title2");
//
//
//
//        List<String> test1 = searchHistory.getHistory("test4");
////        List<String> test2 = searchHistory.getHistory("test5");
////        List<String> test3 = searchHistory.getHistory("test6");
//
//        for (String s : test1) {
//            System.out.println(s);
//        }
//
//
//
//        Assertions.assertThat(test1.get(1)).isEqualTo("title");
//
//
//    }
//
//
//}
