package com.secondhand.backend.constant;

/**
 * Shared constants for "purchase request status".
 * <p>
 * This class/enum keeps the shared constant values of the project in one place.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public enum PurchaseRequestStatus {
    PENDING,   // در انتظار تصمیم فروشنده
    ACCEPTED,  // تایید شده (فروش انجام شد)
    DECLINED   // رد شده
}
