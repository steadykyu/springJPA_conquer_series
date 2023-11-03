package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(value = SpringExtension.class)
@SpringBootTest
@Transactional
public class ItemServiceTest {
    @Autowired ItemService itemService;
    @Autowired ItemRepository itemRepository;

    @Test
    public void 상품등록() throws Exception{
        //given
        Item item = new Item();
        //when
        itemService.saveItem(item);
        //then
        Item findItem = itemRepository.findOne(item.getItemId());
        Assertions.assertThat(findItem).isEqualTo(item);
    }
}
