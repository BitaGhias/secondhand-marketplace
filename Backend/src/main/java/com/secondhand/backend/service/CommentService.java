package com.secondhand.backend.service;

import com.secondhand.backend.constant.ItemStatus;
import com.secondhand.backend.constant.Role;
import com.secondhand.backend.dto.CommentCreateRequest;
import com.secondhand.backend.dto.CommentResponse;
import com.secondhand.backend.entity.Comment;
import com.secondhand.backend.entity.Item;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.exception.custom.ForbiddenException;
import com.secondhand.backend.exception.custom.ResourceNotFoundException;
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

    private void validateUser(User user) {
        if (!user.isActive()) {
            throw new ForbiddenException("حساب کاربری شما فعال نیست!");
        }
        if (user.isBlocked()) {
            throw new ForbiddenException("حساب کاربری شما مسدود شده است!");
        }
    }

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

    public CommentResponse addComment(CommentCreateRequest request, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        validateUser(user);

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

    public void deleteComment(Long commentId, Long adminId) {

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("ادمین یافت نشد"));

        if (admin.getRole() != Role.ADMIN) {
            throw new ForbiddenException("شما دسترسی حذف کامنت را ندارید!");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("کامنت یافت نشد"));

        commentRepository.delete(comment);
    }

    public CommentResponse updateComment(Long commentId, Long userId, String newText) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        validateUser(user);

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