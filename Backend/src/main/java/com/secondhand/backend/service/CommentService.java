package com.secondhand.backend.service;

import com.secondhand.backend.entity.Comment;
import com.secondhand.backend.entity.Item;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.CommentRepository;
import com.secondhand.backend.repository.ItemRepository;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    public CommentRepository commentRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public ItemRepository itemRepository;

    public Comment addComment(Long itemId, Long userId, String text) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("کاربر یافت نشد"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("آگهی یافت نشد"));

        Comment comment = new Comment();
        comment.text = text;
        comment.user = user;
        comment.item = item;

        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByItem(Long itemId) {
        return commentRepository.findByItemId(itemId);
    }
}