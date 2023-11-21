package study.datajpa.repository;

import org.springframework.beans.factory.annotation.Value;

public interface UsernameOnly {
//    @Value("#{target.username + ' ' + target.age}") // username과 age를 더한 값
    String getUsername();
}
