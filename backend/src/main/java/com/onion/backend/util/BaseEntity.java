package com.onion.backend.util;

import java.time.LocalDateTime;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@MappedSuperclass  // 상속받은 엔티티가 해당 필드를 가지도록 함
@EntityListeners(AuditingEntityListener.class)  // JPA Auditing 활성화
public class BaseEntity {

    @CreatedDate
    private LocalDateTime createdDate; // 생성일

    @LastModifiedDate
    private LocalDateTime updatedDate; // 수정일

}
