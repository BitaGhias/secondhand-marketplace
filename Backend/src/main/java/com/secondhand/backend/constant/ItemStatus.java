package com.secondhand.backend.constant;

/**
 * Shared constants for "item status".
 * <p>
 * This class/enum keeps the shared constant values of the project in one place.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public enum ItemStatus {
    PENDING,    // در انتظار بررسی
    APPROVED,   // تایید شده
    REJECTED,   // رد شده
    SOLD,       // فروخته شده
    DELETED     // حذف شده منطقی
}