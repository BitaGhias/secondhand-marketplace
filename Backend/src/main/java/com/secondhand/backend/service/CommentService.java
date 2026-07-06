package com.secondhand.backend.service;

import com.secondhand.backend.dto.CommentCreateRequest;
import com.secondhand.backend.dto.CommentResponse;
import com.secondhand.backend.entity.Comment;
import com.secondhand.backend.entity.Item;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.CommentRepository;
import com.secondhand.backend.repository.ItemRepository;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private CommentResponse convertToResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getText(),
                comment.getItem() != null ? comment.getItem().getId() : null,
                comment.getItem() != null ? comment.getItem().getTitle() : "آگهی حذف شده",
                comment.getUser() != null ? comment.getUser().getId() : null,
                comment.getUser() != null ? comment.getUser().getUsername() : "کاربر ناشناس"
        );
    }

    public CommentResponse addComment(CommentCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("کاربر یافت نشد"));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("آگهی یافت نشد"));

        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setUser(user);
        comment.setItem(item);

        Comment savedComment = commentRepository.save(comment);
        return convertToResponse(savedComment);
    }

    public List<CommentResponse> getCommentsByItem(Long itemId) {
        List<Comment> comments = commentRepository.findByItemId(itemId);
        List<CommentResponse> responses = new ArrayList<>();
        for (Comment comment : comments) {
            responses.add(convertToResponse(comment));
        }
        return responses;
    }
}