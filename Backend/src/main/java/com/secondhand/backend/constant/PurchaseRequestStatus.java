package com.secondhand.backend.constant;

/** وضعیت درخواست خرید */
public enum PurchaseRequestStatus {
    PENDING,   // در انتظار تصمیم فروشنده
    ACCEPTED,  // تایید شده (فروش انجام شد)
    DECLINED   // رد شده
}
