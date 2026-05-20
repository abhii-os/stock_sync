package com.stocksync.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import com.stocksync.userservice.enums.RoleType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleType name;

    private String description;
}
