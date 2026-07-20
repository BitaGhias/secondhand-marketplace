package com.secondhand.backend.service;

import com.secondhand.backend.constant.ItemStatus;
import com.secondhand.backend.constant.Role;
import com.secondhand.backend.dto.comment.CommentCreateRequest;
import com.secondhand.backend.dto.comment.CommentResponse;
import com.secondhand.backend.entity.Comment;
import com.secondhand.backend.entity.Item;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.exception.custom.ForbiddenException;
import com.secondhand.backend.exception.custom.ResourceNotFoundException;
import com.secondhand.backend.repository.CommentRepository;
import com.secondhand.backend.repository.ItemRepository;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.util.UserValidationHelper;
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
                comment.getUser() != null ? comment.getUser().getUsername() : "کاربر ناشناس",
                comment.getCreatedAt()
        );
    }

    public CommentResponse addComment(CommentCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        UserValidationHelper.validateActiveAndNotBlocked(user);  //  از helper مشترک

        if (request.getText() == null || request.getText().trim().isEmpty()) {
            throw new BadRequestException("متن کامنت نمی‌تواند خالی باشد!");
        }

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("آگهی یافت نشد"));

        if (item.getStatus() != ItemStatus.APPROVED) {
            throw new BadRequestException("این آگهی قابل کامنت‌گذاری نیست!");
        }

        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setUser(user);
        comment.setItem(item);

        Comment savedComment = commentRepository.save(comment);
        return convertToResponse(savedComment);
    }

    public List<CommentResponse> getCommentsByItem(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("آگهی یافت نشد");
        }

        List<Comment> comments = commentRepository.findByItemId(itemId);
        List<CommentResponse> responses = new ArrayList<>();
        for (Comment comment : comments) {
            responses.add(convertToResponse(comment));
        }
        return responses;
    }

    /**
     *  FIX: کاربر عادی می‌تواند کامنت خودش را حذف کند
     *         ادمین می‌تواند هر کامنتی را حذف کند
     *         قبلاً فقط ادمین می‌توانست حذف کند
     */
    public void deleteComment(Long commentId, Long requesterId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("کامنت یافت نشد"));

        boolean isAdmin = requester.getRole() == Role.ADMIN;
        boolean isOwner = comment.getUser().getId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new ForbiddenException("شما اجازه حذف این کامنت را ندارید!");
        }

        commentRepository.delete(comment);
    }

    public CommentResponse updateComment(Long commentId, Long userId, String newText) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        UserValidationHelper.validateActiveAndNotBlocked(user);  //  از helper مشترک

        if (newText == null || newText.trim().isEmpty()) {
            throw new BadRequestException("متن کامنت نمی‌تواند خالی باشد!");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("کامنت یافت نشد"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new ForbiddenException("شما اجازه ویرایش این کامنت را ندارید!");
        }

        comment.setText(newText);
        Comment updatedComment = commentRepository.save(comment);
        return convertToResponse(updatedComment);
    }
}