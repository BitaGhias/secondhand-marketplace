package com.secondhand.backend.entity;

import jakarta.persistence.*;//کد های ما رو به دیتابیس وصل میکنه
import lombok.*;//جلوگیری از نوشتن کدهای تکراری

@Entity
@Table(name = "users")
@Data                //  تولید خودکار Getter، Setter، toString، equals و hashCode
@NoArgsConstructor   //  تولید سازنده بدون آرگومان
@AllArgsConstructor  //  تولید سازنده با تمام آرگومان‌ها
public class User {

    @Id //فیلد اصلی برای شناسایی
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//شناسه کاربران

    @Column(nullable = false, unique = true) //غیر تکراری و غیر خالی
    private String username;

    @Column(nullable = false)
    private String password;

    private String role = "ROLE_USER";
    private boolean active = true;
}