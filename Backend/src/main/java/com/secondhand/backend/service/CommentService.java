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

/**
 * Business-logic service for "comment" operations.
 * <p>
 * This class implements the core business logic and sits between the controller layer and the repository layer. Validation and access control are enforced here and a proper exception is thrown when a rule is violated.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    /**
     * Converts to response.
     *
     * @param comment the comment object
     * @return the resulting {@code CommentResponse} instance
     */
    private CommentResponse convertToResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getText(),
                comment.getItem() != null ? comment.getItem().getId() : null,
                comment.getItem() != null ? comment.getItem().getTitle() : "آگهی حذف شده",
                comment.getUser() != null ? comment.getUser().getId() : null,
                comment.getUser() != null ? comment.getUser().getUsername() : "کاربر ناشناس",
                comment.getCreatedAt(),
                comment.isEdited() // FIX (مورد ۴)
        );
    }

    /**
     * Adds comment.
     *
     * @param request request body received from the client
     * @param userId id of the user
     * @return the resulting {@code CommentResponse} instance
     */
    public CommentResponse addComment(CommentCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        UserValidationHelper.validateActiveAndNotBlocked(user);

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

    /**
     * Gets comments by item.
     *
     * @param itemId id of the ad (item)
     * @return a {@code List<CommentResponse>} with the results; empty if nothing matches
     */
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
     * Deletes comment.
     *
     * @param commentId id of the comment
     * @param requesterId the "requester id" value of type {@code Long}
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

    /**
     * Updates comment.
     *
     * @param commentId id of the comment
     * @param userId id of the user
     * @param newText the "new text" value of type {@code String}
     * @return the resulting {@code CommentResponse} instance
     */
    public CommentResponse updateComment(Long commentId, Long userId, String newText) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        UserValidationHelper.validateActiveAndNotBlocked(user);

        if (newText == null || newText.trim().isEmpty()) {
            throw new BadRequestException("متن کامنت نمی‌تواند خالی باشد!");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("کامنت یافت نشد"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new ForbiddenException("شما اجازه ویرایش این کامنت را ندارید!");
        }

        comment.setText(newText);
        comment.setEdited(true); // FIX (مورد ۴): نشانگر ویرایش‌شده برای کامنت
        Comment updatedComment = commentRepository.save(comment);
        return convertToResponse(updatedComment);
    }
}