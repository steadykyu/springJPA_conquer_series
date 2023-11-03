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

    public void save(Item item){
        if(item.getItemId() == null) {
            em.persist(item);
        }
//        }else{ // 준영속일때 처리해준다.
//            em.merge(item); // 변경 감지를 이용하면 이 부분이 필요없다.
//        }
    }

    public Item findOne(Long itemId){
        return em.find(Item.class, itemId);
    }

    public List<Item> findAll(){
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }
}
