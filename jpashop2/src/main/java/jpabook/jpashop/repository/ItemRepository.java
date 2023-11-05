package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    // 매개변수 객체에 id가 있으면 DB에 저장,
    // id가 있으면 merge로 update
    public void save(Item item){
        if(item.getId() == null){
            em.persist(item);
        } else {
            em.merge(item); // 영속성 컨텍스트의 관리를 받는 Item을 return 한다.
        }
    }

    public Item findOne(Long id){
        return em.find(Item.class, id);
    }

    public List<Item> findAll(){
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }



}
