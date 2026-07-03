package com.secondhand.backend.service;

import com.secondhand.backend.entity.Item;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.ItemRepository;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    public Item createItem(Item item, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("کاربر یافت نشد"));

        //اگر کاربر وجود داشت، او را به عنوان «صاحب آگهی» معرفی می‌کنیم
        item.setUser(user);

        return itemRepository.save(item);
    }

    public List<Item> getApprovedItems() {
        return itemRepository.findByStatus("APPROVED");
    }
}
