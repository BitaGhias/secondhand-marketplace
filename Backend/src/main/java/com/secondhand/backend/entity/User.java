package com.secondhand.backend.entity;

import com.secondhand.backend.constant.Role;
import jakarta.persistence.*;//کد های ما رو به دیتابیس وصل میکنه
import lombok.*;//جلوگیری از نوشتن کدهای تکراری

@Entity
@Table(name = "users")
@Data                //  تولید خودکار Getter، Setter، toString، equals و hashCode
@NoArgsConstructor   //  تولید سازنده بدون آرگومان
@AllArgsConstructor  //  تولید سازنده با تمام آرگومان‌ها
public class User {

    @Id //فیلد اصلی برای شناسایی
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;//شناسه کاربران

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true) //غیر تکراری و غیر خالی
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active = true;

    @Column(nullable = false)
    public boolean isBlocked = false;
}