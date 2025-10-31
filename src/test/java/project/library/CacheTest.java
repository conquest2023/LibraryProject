//package project.library;
//
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.context.SpringBootTest;
//import project.library.repository.LibraryJpaRepository;
//import project.library.repository.LibraryRepository;
//import project.library.repository.LibraryRepositoryJpaAdapter;
//import project.library.repository.collection.Library;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.Executor;
//
//@SpringBootTest
//public class CacheTest {
//
//    @Autowired LibraryJpaRepository jpaRepository;
//    @Qualifier("taskExecutor")
//    Executor executor;
//
//
//
//    @Test
//    void 속도측정(){
//
//        LibraryRepository repository=new LibraryRepositoryJpaAdapter(jpaRepository);
//
//    }
//}
