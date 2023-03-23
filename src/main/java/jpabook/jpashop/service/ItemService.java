package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item){
        itemRepository.save(item);
    }

    @Transactional  //변경감지 ! 이렇게해야댐 , 글고 set으로 하지말고 item에 메소드만드셈 changeItem같은거
    public void updateItem(UpdateItemDto dto) {
        Item findItem = itemRepository.findOne(dto.getId());//DB에있는거 꺼내서 영속상태됨
//        findItem.setPrice(dto.getPrice());
//        findItem.setName(dto.getName());
//        findItem.setStockQuantity(dto.getStockQuantity());
        findItem.change(dto.getName(),dto.getPrice(),dto.getStockQuantity());
        //값만세팅하고 암것도안해도됨! @Transactional에 의해 변경감지되고 update후 커밋됨
    }

    public List<Item> findItems(){
        return itemRepository.findAll();
    }
    public Item findItem(Long id){
        return itemRepository.findOne(id);
    }

}
